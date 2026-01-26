package fr.insee.arc.core.service.p6export.dao;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.ExportOption;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.DuckdbDao;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.dataobjects.ColumnAttributes;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class ExportMasterNodDao {

	private Sandbox coordinatorSandbox;
	
	private DuckdbDao duckdbDao;


	public ExportMasterNodDao(Sandbox coordinatorSandbox) {
		this.coordinatorSandbox = coordinatorSandbox;
		this.duckdbDao = new DuckdbDao();
	}
	/**
	 * select mapping tables found on the executor by listing table in
	 * IHM_MOD_TABLE_METIER
	 * 
	 * @return
	 * @throws ArcException
	 */
	public Set<String> selectBusinessTableToExport() throws ArcException {
		Set<String> mappingTablesName = new HashSet<>();

		int numberOfNods = ArcDatabase.numberOfNods();

		queryBusinessTablesFromNods(mappingTablesName, ArcDatabase.COORDINATOR.getIndex(), numberOfNods);
		
		return mappingTablesName;
	}

	/**
	 * Query table name to export from mod_table_metier add them to set of tables to
	 * export
	 * Query is sent on all nods in order as their could be some synchronization issue if user
	 * change tables
	 * 
	 * @param mappingTablesName
	 * @param startNodConnectionIndex
	 * @param endNodConnectionIndex
	 * @throws ArcException
	 */
	protected void queryBusinessTablesFromNods(Set<String> mappingTablesName, int startNodConnectionIndex,
			int endNodConnectionIndex) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_A), SQL.FROM,
				ViewEnum.MOD_TABLE_METIER.getFullName(coordinatorSandbox.getSchema()), SQL.ALIAS_A
				, SQL.LEFT_JOIN
				,ViewEnum.EXPORT_OPTION.getFullName(coordinatorSandbox.getSchema()), SQL.ALIAS_B
				, SQL.ON
				, ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_A), "=",ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_B)
				, SQL.WHERE
				, ColumnEnum.EXPORT_COORDINATOR_OPTION.alias(SQL.ALIAS_B), "=", query.quoteText(ExportOption.ACTIVE.getStatus())
				);

		for (int connectionIndex = startNodConnectionIndex; connectionIndex < endNodConnectionIndex; connectionIndex++) {
			mappingTablesName.addAll(new GenericBean(UtilitaireDao.get(connectionIndex).executeRequest(null, query))
					.getColumnValues(ColumnEnum.NOM_TABLE_METIER.getColumnName()));
		}
	}

	
	/**
	 * Copy the mappin table to masterNod
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	public void copyMappingTablesToMasterNod(Set<String> mappingTablesName) throws ArcException {

		dropImagesOfMappingTable(mappingTablesName);

		transfertDataFromExecutorNodsToMasterNodImageTables(mappingTablesName);
			
		copyImageTablesToParentMappingTables(mappingTablesName);

	}

	/**
	 * Create an image table for each mapping table to retrieve
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	private void dropImagesOfMappingTable(Set<String> mappingTablesName) throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		
		// create the image table of mapping tables
		for (String mappingTableName : mappingTablesName)
		{
			String tableName = ViewEnum.getFullName(coordinatorSandbox.getSchema(), mappingTableName);
			String tableNameImage = FormatSQL.imageObjectName(tableName);
			query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, tableNameImage
					, SQL.END_QUERY);			
		}
		UtilitaireDao.get(0).executeRequest(coordinatorSandbox.getConnection(), query);		
	}
	
	
	/**
	 * 
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	public void transfertDataFromExecutorNodsToMasterNodImageTables(Set<String> mappingTablesName) throws ArcException {
		
		ThrowingConsumer<Connection> transfert = duckdbConnection ->
			{
				for (String mappingTableName : mappingTablesName)
				{
					String tableName = ViewEnum.getFullName(coordinatorSandbox.getSchema(), mappingTableName);
					String tableNameImage = FormatSQL.imageObjectName(tableName);
					
					ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
					query.build(SQL.CREATE, SQL.TABLE, duckdbDao.attachedTableName(0, tableNameImage), SQL.AS);
					query.build(duckdbDao.selectTableFromAllExecutorNods(tableName));
					duckdbDao.executeQuery(duckdbConnection, query);
				}
			}
			;

			duckdbDao.executeOnDuckdb(transfert);

	}
	
	

	/**
	 * Transactional query to finish the copy to the real table
	 * No commit
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	private void copyImageTablesToParentMappingTables(Set<String> mappingTablesName) throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		
		
		// create the image table of mapping tables
		for (String mappingTableName : mappingTablesName)
		{
			String tableName = ViewEnum.getFullName(coordinatorSandbox.getSchema(), mappingTableName);
			String tableNameImage = FormatSQL.imageObjectName(tableName);
			
			ColumnAttributes columnsOfImageTable = UtilitaireDao.get(0).retrieveColumnAttributes(coordinatorSandbox.getConnection(), tableNameImage);
			
			query.build(SQL.INSERT_INTO, tableName, "(", columnsOfImageTable.getCols(), ")" 
					, SQL.SELECT, columnsOfImageTable.getCols(), SQL.FROM, tableNameImage, SQL.END_QUERY);
			query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, tableNameImage, SQL.END_QUERY);
			
		}
		
		UtilitaireDao.get(0).executeRequestNoCommit(coordinatorSandbox.getConnection(), query);				
	}


}
