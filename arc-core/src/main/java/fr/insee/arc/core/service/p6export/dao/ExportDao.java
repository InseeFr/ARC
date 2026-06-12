package fr.insee.arc.core.service.p6export.dao;

import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementPhase.ConditionExecution;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.scalability.CopyFromCoordinatorToExecutors;
import fr.insee.arc.core.service.p6export.bo.TableToExport;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.TemporaryToken;

public class ExportDao {

	private Sandbox coordinatorSandbox;
	
	private String exportTimeStamp;
	private String tableOfIdSource;

	// by convention the database client for EXPORT phase is named the same as the phase
	// but this might change
	public static final String EXPORT_CLIENT_NAME = TraitementPhase.EXPORT.toString();
	
	public ExportDao(Sandbox coordinatorSandbox) {
		this.coordinatorSandbox = coordinatorSandbox;
	}

	/**
	 * Compute a timestamp that identifies the export This timestamp will be used to
	 * mark data as retrieved (column date_client in pilotage_fichier table) It is
	 * saved in exportTimeStamp variable. This timestamp will also be converted as a
	 * directory name where the files will be exported
	 * 
	 * @return
	 * @throws ArcException
	 */
	public String dateExport() throws ArcException {
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT,
				"localtimestamp as ts, to_char(localtimestamp,'YYYY_MM_DD_HH24_MI_SS_MS') as ts_as_directory");
		Map<String, List<String>> gb = new GenericBean(
				UtilitaireDao.get(0).executeRequest(this.coordinatorSandbox.getConnection(), query)).mapContent();
		this.exportTimeStamp = gb.get("ts").get(0);
		return gb.get("ts_as_directory").get(0);
	}

	
	/**
	 * Select the file to export
	 * @return
	 * @throws ArcException 
	 */
	public void materializeIdSourceToExport() throws ArcException {
		
		// if volatile mode is on, no need to select the files id_source to export
		// because all data has to be exported in volatile mode
		if (PropertiesHandler.getInstance().isVolatileOn())
		{
			return;
		}
		
		this.tableOfIdSource = TableNaming.buildTableNameWithTokens(coordinatorSandbox.getSchema()
				, ViewEnum.ID_SOURCE, ExportDao.EXPORT_CLIENT_NAME,	new TemporaryToken().getToken());
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(FormatSQL.dropTable(tableOfIdSource));
		
		query.build(SQL.CREATE, SQL.TABLE, tableOfIdSource, FormatSQL.WITH_NO_VACUUM);
		query.build(SQL.AS, SQL.SELECT, ColumnEnum.ID_SOURCE, SQL.FROM);
		query.build(ViewEnum.getFullName(coordinatorSandbox.getSchema(), ViewEnum.PILOTAGE_FICHIER.toString()));
		query.build(SQL.WHERE, ConditionExecution.PIPELINE_TERMINE_DONNEES_NON_EXPORTEES.getSqlFilter());
		
		UtilitaireDao.get(0).executeRequest(this.coordinatorSandbox.getConnection(), query);
		
		// copy table of id source to executors
		CopyFromCoordinatorToExecutors copy = new CopyFromCoordinatorToExecutors();
		copy.copyWithTee(tableOfIdSource);
				
	}
	
	
	/**
	 * mark exported data
	 * 
	 * @throws ArcException
	 */
	public void markExportedData() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.UPDATE, ViewEnum.PILOTAGE_FICHIER.getFullName(this.coordinatorSandbox.getSchema()), SQL.ALIAS_A);
		query.build(SQL.SET, ColumnEnum.DATE_CLIENT, "=",
				"array_append( date_client, " + query.quoteText(exportTimeStamp) + "::timestamp )");
		query.build(",", ColumnEnum.CLIENT, "=",
				"array_append( client, " + query.quoteText(EXPORT_CLIENT_NAME) + "::text)");
		query.build(SQL.WHERE, ConditionExecution.PIPELINE_TERMINE_DONNEES_NON_EXPORTEES.getSqlFilter());

		UtilitaireDao.get(0).executeRequestNoCommit(this.coordinatorSandbox.getConnection(), query);
	}
	
	
	// commit database operations
	// - commit the data copy to the mapping tables
	// - commit the changes in pilotage table i.e. the files (idsource) marked as exported in 
	public void commit() throws ArcException {
		UtilitaireDao.get(0).executeRequestCommit(coordinatorSandbox.getConnection());				
	}

	
	/**
	 * truncate tables exported to coordinator
	 * @param mappingTablesNameExportedToCoordinator
	 * @throws ArcException
	 */
	public void truncateMappingTablesExportedToCoordinator(List<TableToExport> mappingTablesNameExportedToCoordinator) throws ArcException {
		
		if (!ArcDatabase.isScaled()) {
			return;
		}
		
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		for (TableToExport t : mappingTablesNameExportedToCoordinator)
		{
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.build(SQL.TRUNCATE, SQL.TABLE, ViewEnum.getFullName(coordinatorSandbox.getSchema(),t.getTableName()));
			
			if (t.getNod().equals(ArcDatabase.EXECUTOR))
			{
				for (int executorConnectionIndex=ArcDatabase.EXECUTOR.getIndex(); executorConnectionIndex<ArcDatabase.EXECUTOR.getIndex()+numberOfExecutorNods; executorConnectionIndex++ )
				{
					UtilitaireDao.get(executorConnectionIndex).executeRequest(null, query);
				}
			}
		}
		
	}


}
