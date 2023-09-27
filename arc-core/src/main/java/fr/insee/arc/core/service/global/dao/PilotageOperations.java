package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class PilotageOperations {

	private PilotageOperations() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER_APISERVICE = LogManager.getLogger(PilotageOperations.class);

	/**
	 * Met à jour le comptage du nombre d'enregistrement par fichier; nos fichiers
	 * de blocs XML sont devenus tous plats :)
	 * 
	 * @throws ArcException
	 */
	public static String queryUpdateNbEnr(String tablePilTemp, String tableTravailTemp, String... jointure) {
		StringBuilder query = new StringBuilder();

		// mise à jour du nombre d'enregistrement et du type composite
		StaticLoggerDispatcher.info(LOGGER_APISERVICE, "** updateNbEnr **");
		query.append("\n UPDATE " + tablePilTemp + " a ");
		query.append("\n SET nb_enr=(select count(*) from " + tableTravailTemp + ") ");

		if (jointure.length > 0) {
			query.append(", jointure= " + FormatSQL.textToSql(ManipString.nullIfEmptyTrim(jointure[0])));
		}
		query.append(";");
		return query.toString();
	}

	/**
	 * Selection d'un lot d'id_source pour appliquer le traitement Les id_sources
	 * sont selectionnés parmi les id_source présent dans la phase précédentes avec
	 * etape =1 Ces id_source sont alors mis à jour dans la phase précédente à étape
	 * =0 et une nouvelle ligne est créee pour la phase courante et pour chaque
	 * id_source avec etape=1 Fabrique une copie de la table de pilotage avec
	 * uniquement les fichiers concernés par le traitement
	 * 
	 * @param phase
	 * @param tablePil
	 * @param tablePilTemp
	 * @param phaseAncien
	 * @param phaseNouveau
	 * @param nbEnr
	 * @return
	 */
	public static String queryCopieTablePilotage(String tablePil, String tablePilTemp, String phaseAncien,
			String phaseNouveau, Integer nbEnr) {
		StringBuilder requete = new StringBuilder();

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		requete.append("\n DROP TABLE IF EXISTS " + tablePilTemp + "; ");

		requete.append("\n CREATE ");
		if (!tablePilTemp.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("\n TABLE " + tablePilTemp
				+ " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS  ");

		requete.append("\n WITH prep AS (");
		requete.append("\n SELECT a.*, count(1) OVER (ORDER BY date_traitement, " + ColumnEnum.ID_SOURCE.getColumnName()
				+ ") as cum_enr ");
		requete.append("\n FROM " + tablePil + " a ");
		requete.append("\n WHERE phase_traitement='" + phaseAncien + "'  AND '" + TraitementEtat.OK
				+ "'=ANY(etat_traitement) and etape=1 ) ");
		requete.append("\n , mark AS (SELECT a.* FROM prep a WHERE cum_enr<" + nbEnr + " ");
		requete.append("\n UNION   (SELECT a.* FROM prep a LIMIT 1)) ");

		// update the line in pilotage with etape=3 for the previous step
		requete.append("\n , update as ( UPDATE " + tablePil + " a set etape=3 from mark b where a."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName()
				+ " and a.etape=1 AND a.phase_traitement='" + phaseAncien + "'  AND '" + TraitementEtat.OK
				+ "'=ANY(a.etat_traitement)) ");

		// insert the line in pilotage with etape=1 for the current step
		requete.append("\n , insert as (INSERT INTO " + tablePil + " ");
		requete.append("\n (container, " + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", date_entree, id_norme, validite, periodicite, phase_traitement, etat_traitement, date_traitement, rapport, taux_ko, nb_enr, etape, generation_composite,jointure) ");
		requete.append("\n SELECT container, " + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", date_entree, id_norme, validite, periodicite, '" + phaseNouveau + "' as phase_traitement, '{"
				+ TraitementEtat.ENCOURS + "}' as etat_traitement ");
		requete.append("\n , to_timestamp('" + formatter.format(date) + "','" + ApiService.DATABASE_DATE_FORMAT
				+ "') , rapport, taux_ko, nb_enr, 1 as etape, generation_composite, jointure ");
		requete.append("\n FROM mark ");
		requete.append("\n RETURNING *) ");

		requete.append("\n SELECT * from insert; ");
		requete.append("\n ANALYZE " + tablePilTemp + ";");
		return requete.toString();
	}

	/**
	 * Query to update pilotage table when error occurs
	 * 
	 * @param phase
	 * @param tablePil
	 * @param exception
	 * @return
	 */
	public static StringBuilder queryUpdatePilotageError(String phase, String tablePil, Exception exception) {
		StringBuilder requete = new StringBuilder();
		requete.append("UPDATE " + tablePil + " SET etape=2, etat_traitement= '{" + TraitementEtat.KO + "}', rapport='"
				+ exception.toString().replace("'", "''").replace("\r", "") + "' ");
		requete.append(
				"\n WHERE phase_traitement='" + phase + "' AND etat_traitement='{" + TraitementEtat.ENCOURS + "}' ");
		return requete;
	}

	
	/**
	 * retrieve the list of documents (i.e. files referenced in the field "id_source") for the given phase and state
	 * @param tablePilotage
	 * @param phase
	 * @param etat
	 * @return
	 */
	public static ArcPreparedStatementBuilder querySelectIdSourceFromPilotage(String envExecution, TraitementPhase phase, TraitementEtat etat)
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(); 
		
		query.build(SQL.SELECT, ColumnEnum.ID_SOURCE.getColumnName(), SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.build(SQL.WHERE, ColumnEnum.PHASE_TRAITEMENT, "=" , query.quoteText(phase.toString()));
		query.build(SQL.AND, ColumnEnum.ETAT_TRAITEMENT, "=", query.quoteText(etat.getSqlArrayExpression()), SQL.CAST_OPERATOR, "text[]");		
		return query;
	}

	public static String execQuerySelectEtapeForIdSource(Connection connection, String envExecution, TraitementPhase phase, TraitementEtat etat, String idSource) throws ArcException
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(SQL.SELECT, ColumnEnum.ETAPE, SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution));
		query.build(SQL.WHERE, ColumnEnum.PHASE_TRAITEMENT, "=", query.quoteText(phase.toString()));
		query.build(SQL.AND, ColumnEnum.ETAT_TRAITEMENT, "=", query.quoteText(etat.getSqlArrayExpression()), SQL.CAST_OPERATOR, ColumnEnum.ETAT_TRAITEMENT.getColumnType().getTypeName() );
		query.build(SQL.AND, ColumnEnum.ID_SOURCE.getColumnName(), "=", query.quoteText(idSource));

		return UtilitaireDao.get(0).getString(connection,query);
	}
	
	/**
	 * Return the query that marks the files or all file if idSource not provided
	 * The mark indicates reset etape to 0 for the previous phase, meaning the file
	 * is no longer processed in the current phase
	 * 
	 * @param idSource
	 * @return
	 */
	public static StringBuilder resetPreviousPhaseMark(String tablePil, String idSource, String tableSource) {
		StringBuilder requete = new StringBuilder();

		// mettre à etape = 0 la phase marquée à 3
		requete.append("\n UPDATE " + tablePil + " a ");
		requete.append("\n SET etape=0 ");
		requete.append("\n WHERE a.etape=3 ");
		if (idSource != null) {
			requete.append("\n AND a."+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		}

		if (tableSource != null) {
			requete.append("\n AND EXISTS (SELECT 1 FROM " + tableSource + " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ");
		}

		requete.append("\n ;");
		return requete;
	}
}