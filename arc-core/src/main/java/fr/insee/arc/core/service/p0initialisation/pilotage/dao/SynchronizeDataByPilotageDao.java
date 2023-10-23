package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import java.sql.Connection;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class SynchronizeDataByPilotageDao {

	private SynchronizeDataByPilotageDao() {
		throw new IllegalStateException("static dao : most method could be used by functional interface to be sent on several connections");
	}

	/**
	 * remove temporary states from pilotage table
	 * 
	 * @param connection
	 * @param envExecution
	 * @throws ArcException
	 */
	public static void resetEtapePilotageDao(Connection connection, String envExecution) throws ArcException {
		String tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);

		StringBuilder requete = new StringBuilder();

		requete.append("DELETE FROM " + tablePil + " WHERE etat_traitement='{ENCOURS}';");

		requete.append(PilotageOperations.queryResetPreviousPhaseMark(tablePil, null, null));

		requete.append("WITH tmp_1 as (select " + ColumnEnum.ID_SOURCE.getColumnName() + ", max(");
		new StringBuilder();
		requete.append("case ");
		for (TraitementPhase p : TraitementPhase.values()) {
			requete.append("when phase_traitement='" + p.toString() + "' then " + p.ordinal() + " ");
		}
		requete.append("end ) as p ");
		requete.append("FROM " + tablePil + " ");
		requete.append("GROUP BY " + ColumnEnum.ID_SOURCE.getColumnName() + " ");
		requete.append("having max(etape)=0 ) ");
		requete.append("update " + tablePil + " a ");
		requete.append("set etape=1 ");
		requete.append("from tmp_1 b ");
		requete.append(
				"where a." + ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName() + " ");
		requete.append("and a.phase_traitement= case ");
		for (TraitementPhase p : TraitementPhase.values()) {
			requete.append("when p=" + p.ordinal() + " then '" + p.toString() + "' ");
		}
		requete.append("end ; ");

		UtilitaireDao.get(0).executeBlock(connection, requete);
	}

	/**
	 * rebuild to defragment pilotage table
	 * 
	 * @param connexion
	 * @param envExecution
	 * @throws ArcException
	 */
	public static void rebuildPilotageDao(Connection connexion, String envExecution) throws ArcException {

		String tablePilotage = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);

		StringBuilder query = FormatSQL.rebuildTableAsSelectWhere(tablePilotage, "true");

		query.append("create index idx1_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage
				+ " (" + ColumnEnum.ID_SOURCE.getColumnName() + ");");

		query.append("create index idx2_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage
				+ " (phase_traitement, etape);");

		query.append("create index idx4_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage
				+ " (rapport) where rapport is not null;");

		query.append("create index idx5_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage
				+ " (o_container,v_container);");

		query.append("create index idx6_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage
				+ " (to_delete);");

		query.append("create index idx7_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage
				+ " (date_entree, phase_traitement, etat_traitement);");

		query.append("analyze " + tablePilotage + ";");

		UtilitaireDao.get(0).executeBlock(connexion, "analyze " + tablePilotage + ";");
	}

	/**
	 * Récupere toutes les tables temporaires d'un environnement
	 *
	 * @param env
	 * @return
	 */
	public static ArcPreparedStatementBuilder requeteListAllTemporaryTablesInEnv(String envExecution) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		TraitementPhase[] phase = TraitementPhase.values();
		// on commence après la phase "initialisation". i=2
		for (int i = 0; i < phase.length; i++) {
			if (i > 0) {
				requete.append(SQL.UNION_ALL);
			}
			requete.append(FormatSQL.tableExists(ViewEnum.getFullName(envExecution, phase[i] + "%" + FormatSQL.TMP+ "%")));
		}
		return requete;
	}


	/**
	 * materialize on executor nod a table containing the list of idSource provided
	 * @param executorConnection
	 * @param idSourceToDelete
	 * @throws ArcException
	 */
	public static void execQueryMaterializeOnExecutorIdSource(Connection executorConnection,
			List<String> idSourceToDelete) throws ArcException {
		
		GenericBean gb = new GenericBean(ColumnEnum.ID_SOURCE.getColumnName(), TypeEnum.TEXT.getTypeName(),
				idSourceToDelete);
		execQueryMaterializeOnExecutorIdSource(executorConnection, gb);
		}
	
	

	/**
	 * materialize on executor nod a table containing the GenericBean of idSource provided
	 * @param executorConnection
	 * @param idSourceToDelete
	 * @throws ArcException
	 */
	public static void execQueryMaterializeOnExecutorIdSource(Connection executorConnection,
			GenericBean idSourceInPilotageToKeep) throws ArcException {

		CopyObjectsToDatabase.execCopyFromGenericBean(executorConnection, ViewEnum.T1.getFullName(), idSourceInPilotageToKeep);
		
		// analyze table
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(FormatSQL.analyzeSecured(ViewEnum.T1.getFullName()));
		UtilitaireDao.get(0).executeImmediate(executorConnection, query);

	}
	
	/**
	 * Delete the records from a target data table according to a given list of id_source
	 * to delete
	 * 
	 * @param executorConnection
	 * @param targetDataTable
	 * @throws ArcException
	 */
	public static void deleteDataRecordsFoundInIdSource(Connection executorConnection, String targetDataTable) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.DELETE, targetDataTable, SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.EXISTS);
		query.build("(");
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.T1.getFullName(), SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_A), "=", ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_B));
		query.build(")");
		query.build(SQL.END_QUERY);
		UtilitaireDao.get(0).executeImmediate(executorConnection, query);
	}



	/**
	 * Delete the records from a target data table that are not found in a given list of id_source
	 * 
	 * @param executorConnection
	 * @param targetDataTable
	 * @throws ArcException
	 */
	public static void keepDataRecordsFoundInIdSourceOnly(Connection executorConnection, String targetDataTable) throws ArcException {
		
		
		// recreate full table instead of delete as large volume of data may be involved
		// this operation occurs once a week
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, ViewEnum.T2.getFullName(), SQL.END_QUERY);
		
		query.build(SQL.CREATE, SQL.TEMPORARY, SQL.TABLE, ViewEnum.T2.getFullName(), SQL.AS);
		query.build(SQL.SELECT, "*", SQL.FROM, targetDataTable, SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.EXISTS);
		query.build("(");
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.T1.getFullName(), SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_A), "=", ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_B));
		query.build(")");
		query.build(SQL.END_QUERY);
		
		query.build(SQL.TRUNCATE, SQL.TABLE, targetDataTable, SQL.END_QUERY);
		
		query.build(SQL.INSERT_INTO, targetDataTable, SQL.SELECT, "*", SQL.FROM, ViewEnum.T2.getFullName(), SQL.END_QUERY);
		
		UtilitaireDao.get(0).executeImmediate(executorConnection, query);
	}

	

	public static void dropDataTables(Connection executorConnection, List<String> dataTablesToDrop) {
		UtilitaireDao.get(0).dropTable(executorConnection, dataTablesToDrop);
	}
	

	/**
	 * drop the unused temporary table on the target connection / environment
	 * 
	 * @param targetConnexion
	 * @throws ArcException
	 */
	public static void dropUnusedTemporaryTablesOnConnection(Connection targetConnexion, String envExecution) throws ArcException {
		GenericBean g = new GenericBean(
				UtilitaireDao.get(0).executeRequest(targetConnexion, SynchronizeDataByPilotageDao.requeteListAllTemporaryTablesInEnv(envExecution)));
		if (!g.mapContent().isEmpty()) {
			List<String> envTables = g.mapContent().get("table_name");
			for (String nomTable : envTables) {
				UtilitaireDao.get(0).executeBlock(targetConnexion, FormatSQL.dropTable(nomTable));
			}
		}
	}


	
}
