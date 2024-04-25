package fr.insee.arc.batch.dao;

import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementPhase.ConditionExecution;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.structure.GenericBean;

public class BatchArcDao {

	/**
	 * Query to check what archives had not been fully proceeded depending on the phase execution condition
	 * 
	 * @param envExecution
	 * @param condition
	 * @return
	 */
	private static ArcPreparedStatementBuilder queryPipelineNotFinished(String envExecution,
			ConditionExecution condition) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.CONTAINER);
		query.build(SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.build(SQL.WHERE, condition.getSqlFilter());
		return query;
	}

	/**
	 * Select the archives not fully proceeded by former batch (etape=1)
	 * 
	 * @param volatileOn
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectArchiveEnCours(String envExecution) throws ArcException {

		ArcPreparedStatementBuilder query = queryPipelineNotFinished(envExecution,
				ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE);

		return new GenericBean(UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null, query))
				.getColumnValues(ColumnEnum.CONTAINER.getColumnName());
	}

	/**
	 * Select the archives not exported (date_client = null) used for volatile mode
	 * 
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectArchiveNotExported(String envExecution) throws ArcException {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(queryPipelineNotFinished(envExecution, ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE));
		query.build(SQL.UNION);
		query.append(queryPipelineNotFinished(envExecution, ConditionExecution.PIPELINE_TERMINE_DONNEES_NON_EXPORTEES));
		
		return new GenericBean(UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null, query))
				.getColumnValues(ColumnEnum.CONTAINER.getColumnName());
	}

	/**
	 * Reset the status of interrupted archives in the pilotage table Archives entry
	 * marked as "encours" are deleted and set back to "finished" in the former
	 * phase
	 * 
	 * @param envExecution
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	public static void execQueryResetPendingFilesInPilotageTable(String envExecution) throws ArcException {
		// delete files that are en cours
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("\n DELETE FROM " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.append("\n WHERE etape=1 AND etat_traitement='{" + TraitementEtat.ENCOURS + "}' ");
		query.append(";");

		// update these files to etape=1
		query.append("\n UPDATE " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.append("\n set etape=1 ");
		query.append("\n WHERE etape=3");
		query.append(";");
		UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeBlock(null, query);

	}
	

	/**
	 * reset to reception phase all files witch hasn't been proceed fully
	 * @param envExecution
	 * @throws ArcException 
	 */
	public static void execQueryResetPendingFilesInPilotageTableVolatile(String envExecution) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("WITH tmp_pending_files AS ( ");
		query.append("\n SELECT id_source FROM " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.append("\n WHERE "+ConditionExecution.PIPELINE_TERMINE_DONNEES_NON_EXPORTEES.getSqlFilter());
		query.append("\n UNION ALL ");
		query.append("\n SELECT id_source FROM " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.append("\n WHERE "+ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE.getSqlFilter());
		query.append("\n )");
		query.append("\n , tmp_delete_pendings_not_in_reception AS ( ");
		query.append("\n DELETE FROM " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution)+ " a USING tmp_pending_files b ");
		query.append("\n WHERE a.id_source = b.id_source ");
		query.append("\n AND a.phase_traitement != "+query.quoteText(TraitementPhase.RECEPTION.toString())); 
		query.append("\n ) ");
		query.append("\n UPDATE " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution)+ " a ");
		query.append("\n SET etape=1 ");
		query.append("\n FROM tmp_pending_files b ");
		query.append("\n WHERE a.id_source=b.id_source ");
		query.append("\n AND a.phase_traitement = "+query.quoteText(TraitementPhase.RECEPTION.toString()));
		query.append("\n AND a.etat_traitement = "+query.quoteText(TraitementEtat.OK.getSqlArrayExpression())+"::text[]");

		UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null, query);
					
	}

	/**
	 * Query the initialization timestamp
	 * 
	 * @return
	 * @throws ArcException
	 */
	public static String execQueryLastInitialisationTimestamp(String envExecution) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("select last_init from " + ViewEnum.PILOTAGE_BATCH.getFullName(envExecution));
		return UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getString(null, query);
	}

	/**
	 * Update the initialization timestamp
	 * 
	 * @param intervalForInitializationInDay
	 * @param hourToTriggerInitializationInProduction
	 * @throws ArcException
	 */
	public static void execUpdateLastInitialisationTimestamp(String envExecution, Integer intervalForInitializationInDay,
			Integer hourToTriggerInitializationInProduction) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.UPDATE, ViewEnum.PILOTAGE_BATCH.getFullName(envExecution));
		query.build(SQL.SET, "last_init=to_char(current_date+interval '" + intervalForInitializationInDay
				+ " days','yyyy-mm-dd')||':" + hourToTriggerInitializationInProduction + "'");
		query.build(",", "operation=case when operation='R' then 'O' else operation end ");
		query.build(SQL.END_QUERY);

		UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null, query);
	}

	public static Integer execQueryAnythingLeftTodo(String envExecution) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.build(SQL.SELECT, "count(*)", SQL.FROM);
		query.build("(");
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.build(SQL.WHERE, ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE.getSqlFilter());
		query.build(SQL.LIMIT, "1");
		query.build(")", ViewEnum.ALIAS_A);

		return UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getInt(null, query);
	}

	public static Boolean execQueryIsProductionOn(String envExecution) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "1", SQL.FROM, ViewEnum.PILOTAGE_BATCH.getFullName(envExecution));
		query.build(SQL.WHERE, ColumnEnum.OPERATION, "=", query.quoteText("O"));
		return UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).hasResults(null, query);
	}

}
