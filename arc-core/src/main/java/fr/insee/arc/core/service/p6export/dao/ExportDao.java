package fr.insee.arc.core.service.p6export.dao;

import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase.ConditionExecution;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ExportDao {

	private Sandbox coordinatorSandbox;
	private String exportTimeStamp;

	public static final String EXPORT_CLIENT_NAME = "EXPORT";
	
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
	 * mark exported data
	 * 
	 * @throws ArcException
	 */
	public void markExportedData() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.UPDATE, ViewEnum.PILOTAGE_FICHIER.getFullName(this.coordinatorSandbox.getSchema()));
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
	 * truncated exported tables
	 * @param mappingTablesNameExported
	 * @throws ArcException
	 */
	public void truncateExportedMappingTables(List<TableToRetrieve> mappingTablesNameExported) throws ArcException {
		
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		for (TableToRetrieve t : mappingTablesNameExported)
		{
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.build(SQL.TRUNCATE, SQL.TABLE, ViewEnum.getFullName(coordinatorSandbox.getSchema(),t.getTableName()));
			
			if (t.getNod().equals(ArcDatabase.COORDINATOR))
			{
				UtilitaireDao.get(0).executeRequest(coordinatorSandbox.getConnection(), query);				
			}
			else
			{
				for (int executorConnectionIndex=ArcDatabase.EXECUTOR.getIndex(); executorConnectionIndex<ArcDatabase.EXECUTOR.getIndex()+numberOfExecutorNods; executorConnectionIndex++ )
				{
					UtilitaireDao.get(executorConnectionIndex).executeRequest(null, query);
				}
			}
		}
		
	}


}
