package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import java.sql.Connection;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class PilotageDao {

	
	/**
	 * remove temporary states from pilotage table
	 * @param connection
	 * @param envExecution
	 * @throws ArcException
	 */
	public static void resetEtapePilotageDao(Connection connection, String envExecution) throws ArcException
	{
		String tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);
		
		StringBuilder requete = new StringBuilder();
	
		requete.append("DELETE FROM " + tablePil + " WHERE etat_traitement='{ENCOURS}';");
	
		requete.append(ApiService.resetPreviousPhaseMark(tablePil, null, null));
	
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
	 * @param connexion
	 * @param envExecution
	 * @throws ArcException
	 */
	public static void rebuildPilotageDao(Connection connexion, String envExecution) throws ArcException {
		
		String tablePilotage = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);
		
		StringBuilder query = FormatSQL.rebuildTableAsSelectWhere(tablePilotage, "true");
		
		query.append("create index idx1_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
								+ tablePilotage + " (" + ColumnEnum.ID_SOURCE.getColumnName() + ");");
		
		query.append("create index idx2_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (phase_traitement, etape);");
		
		query.append("create index idx4_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (rapport) where rapport is not null;");
		
		query.append("create index idx5_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (o_container,v_container);");
		
		query.append("create index idx6_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
								+ tablePilotage + " (to_delete);");
		
		query.append("create index idx7_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
								+ tablePilotage + " (date_entree, phase_traitement, etat_traitement);");
		
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
		for (int i = 2; i < phase.length; i++) {
			if (i > 2) {
				requete.append(" UNION ALL ");
			}
			requete.append(FormatSQL.tableExists(TableNaming.dbEnv(envExecution) + phase[i] + "$%$tmp$%"));
			requete.append(" UNION ALL ");
			requete.append(FormatSQL.tableExists(TableNaming.dbEnv(envExecution) + phase[i] + "\\_%$tmp$%"));
		}
		return requete;
	}
	
	
	/**
	 * Delete data records from a target table according to a given list of source
	 * to delete
	 * 
	 * @param executorConnection
	 * @param idSourceToDelete
	 * @param targetDataTable
	 * @throws ArcException
	 */
	public static void deleteDataRecords(Connection executorConnection, List<String> idSourceToDelete,
			String targetDataTable) throws ArcException {

		GenericBean gb = new GenericBean(ColumnEnum.ID_SOURCE.getColumnName(), TypeEnum.TEXT.getTypeName(),
				idSourceToDelete);

		CopyObjectsToDatabase.execCopyFromGenericBean(executorConnection, ViewEnum.T1.getTableName(), gb);

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.DELETE, targetDataTable);
		query.build(SQL.WHERE, ColumnEnum.ID_SOURCE, SQL.IN);
		query.build("(", SQL.SELECT, ColumnEnum.ID_SOURCE, SQL.FROM, ViewEnum.T1.getTableName(), ")");

		UtilitaireDao.get(0).executeRequest(executorConnection, query);

	}
}
