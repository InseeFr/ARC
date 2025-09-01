package fr.insee.arc.core.service.p6export.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class ExportMasterNodDao {

	private Sandbox coordinatorSandbox;


	public ExportMasterNodDao(Sandbox coordinatorSandbox) {
		this.coordinatorSandbox = coordinatorSandbox;
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
				, ColumnEnum.EXPORT_COORDINATOR_OPTION.alias(SQL.ALIAS_B), "='1'"
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

		// create link extension
		CopyObjectsToDatabase.createExtensionDblink(coordinatorSandbox.getConnection());
		
		createImagesOfMappingTable(mappingTablesName);

		try {
			transfertDataFromExecutorNodsToMasterNodImageTables(mappingTablesName);
			
			copyImageTablesToParentMappingTables(mappingTablesName);
			
		}
		finally
		{
			// always drop link extension at the end : musn't keep that for security concern
			CopyObjectsToDatabase.dropExtensionDblink(coordinatorSandbox.getConnection());
		}

	}

	/**
	 * Create an image table for each mapping table to retrieve
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	private void createImagesOfMappingTable(Set<String> mappingTablesName) throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		
		// create the image table of mapping tables
		for (String mappingTableName : mappingTablesName)
		{
			String tableName = ViewEnum.getFullName(coordinatorSandbox.getSchema(), mappingTableName);
			String tableNameImage = FormatSQL.imageObjectName(tableName);
			query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, tableNameImage
					, SQL.END_QUERY, SQL.BR);
			query.build(SQL.CREATE, SQL.UNLOGGED, SQL.TABLE, tableNameImage, SQL.WITH, SQL.NO_VACUUM,
				SQL.AS, SQL.SELECT, "*", SQL.FROM, tableName, SQL.WHERE, "false"
				, SQL.END_QUERY, SQL.BR);
			
		}
		UtilitaireDao.get(0).executeRequest(coordinatorSandbox.getConnection(), query);		
	}
	
	
	/**
	 * 
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	private void transfertDataFromExecutorNodsToMasterNodImageTables(Set<String> mappingTablesName) throws ArcException {
		
		
		ThrowingConsumer<Connection> functionCoordinator = coordinatorConnection -> 
		{
		};
		
		ThrowingConsumer<Connection> functionExecutor = executorConnection -> 
		{
			try (Connection coordinatorConnection = UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getDriverConnexion())
			{
				// iterate table to copy
				for (String mappingTableName : mappingTablesName)
				{
					String tableName = ViewEnum.getFullName(coordinatorSandbox.getSchema(), mappingTableName);
					String tableNameImage = FormatSQL.imageObjectName(tableName);

					CopyObjectsToDatabase.execCopyFromTableWithoutDroppingTargetTableNorDblinkExtension(executorConnection, coordinatorConnection, tableName, tableNameImage);
					
					// truncate table after copy to master nod
					UtilitaireDao.get(0).executeRequest(executorConnection, FormatSQL.truncate(tableName));
				}
			} catch (SQLException e) {
				throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_COORDINATOR_FAILED);
			}
		};
		
		// open dblink to exery executors nod and execute copy in parallel 
		ServiceScalability.dispatchOnNods(coordinatorSandbox.getConnection(), functionCoordinator, functionExecutor);
		
	}
	
	

	/**
	 * Transactional query to finish the copy to the real table
	 * @param mappingTablesName
	 * @throws ArcException
	 */
	private void copyImageTablesToParentMappingTables(Set<String> mappingTablesName) throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		
		query.build(SQL.BEGIN);
		
		// create the image table of mapping tables
		for (String mappingTableName : mappingTablesName)
		{
			String tableName = ViewEnum.getFullName(coordinatorSandbox.getSchema(), mappingTableName);
			String tableNameImage = FormatSQL.imageObjectName(tableName);
			query.build(SQL.INSERT_INTO, tableName , SQL.SELECT, "*", SQL.FROM, tableNameImage, SQL.END_QUERY, SQL.BR);
			query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, tableNameImage, SQL.END_QUERY, SQL.BR);
			
		}
		query.build(SQL.COMMIT, SQL.END_QUERY);
		
		UtilitaireDao.get(0).executeRequest(coordinatorSandbox.getConnection(), query);				
	}
	
	

}
