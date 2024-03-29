package fr.insee.arc.batch.dao;

import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class BatchArcDao {

	/**
	 * select archive not fully proceed by former batch (etape=1)
	 * 
	 * @param volatileOn
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectArchiveEnCours(String envExecution)
			throws ArcException {
			return new GenericBean(UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null,
					new ArcPreparedStatementBuilder("select distinct container from "
							+ ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution) + " where etape=1")))
					.getColumnValues(ColumnEnum.CONTAINER.getColumnName());
	}

	/**
	 * select archives not exported (date_client = null)
	 * used for volatile mode
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectArchiveNotExported(String envExecution)
			throws ArcException {
		return new GenericBean(UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null,
				new ArcPreparedStatementBuilder("select distinct container from "
						+ ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution) + " where date_client is null")))
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
	public static void execQueryResetPendingFilesInPilotageTable(String envExecution) throws ArcException {
		// delete files that are en cours
		StringBuilder query = new StringBuilder();
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
	 * Create the pilotage batch table if it doesn't exist It may happen only if the
	 * application is started firstly and exclusively in batch mode without any
	 * built database
	 * 
	 * @throws ArcException
	 */
	public static void execQueryCreatePilotageBatch() throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("\n CREATE TABLE IF NOT EXISTS " + ViewEnum.PILOTAGE_BATCH.getFullName()
				+ " (last_init text, operation text); ");
		requete.append(
				"\n insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select 1 from arc.pilotage_batch); ");
		UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null, requete);
	}

	/**
	 * Query the initialization timestamp
	 * 
	 * @return
	 * @throws ArcException
	 */
	public static String execQueryLastInitialisationTimestamp() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("select last_init from " + ViewEnum.PILOTAGE_BATCH.getFullName());
		return UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getString(null, query);
	}

	/**
	 * Update the initialization timestamp
	 * 
	 * @param intervalForInitializationInDay
	 * @param hourToTriggerInitializationInProduction
	 * @throws ArcException
	 */
	public static void execUpdateLastInitialisationTimestamp(Integer intervalForInitializationInDay,
			Integer hourToTriggerInitializationInProduction) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.UPDATE, ViewEnum.PILOTAGE_BATCH.getFullName());
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
		query.build(SQL.WHERE, ColumnEnum.ETAPE, "=", "1");
		query.build(SQL.LIMIT, "1");
		query.build(")", ViewEnum.ALIAS_A);

		return UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getInt(null, query);
	}

	public static Boolean execQueryIsProductionOn() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "1", SQL.FROM, ViewEnum.PILOTAGE_BATCH.getFullName());
		query.build(SQL.WHERE, ColumnEnum.OPERATION, "=", query.quoteText("O"));
		return UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).hasResults(null, query);
	}

}
