package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.SqlInjectionChecked;
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
	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder queryUpdateNbEnr(String tablePilTemp, String tableTravailTemp, String... jointure) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		// mise à jour du nombre d'enregistrement et du type composite
		StaticLoggerDispatcher.info(LOGGER_APISERVICE, "** updateNbEnr **");
		query.append("\n UPDATE " + tablePilTemp + " a ");
		query.append("\n SET nb_enr=(select count(*) from " + tableTravailTemp + ") ");

		if (jointure.length > 0) {
			query.append(", jointure= ").appendText(ManipString.nullIfEmptyTrim(jointure[0]));
		}
		query.append(";");
		return query;
	}


	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder queryUpdatePilotageMapping(String tableMappingPilTemp, String idSource) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
        query.append("UPDATE " + tableMappingPilTemp + " SET etape=2, etat_traitement = '{" + TraitementEtat.OK + "}' WHERE "+ColumnEnum.ID_SOURCE.getColumnName()+" = ").appendText(idSource).append(";");
		return query;
	}

	
	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder queryBuildTablePilotage(String tablePil, String tablePilTemp) {
		
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("\n DROP TABLE IF EXISTS " + tablePilTemp + "; ");

		requete.append("\n CREATE ");
		if (!tablePilTemp.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("\n TABLE " + tablePilTemp
				+ " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS  ");
		requete.append("\n SELECT *  FROM " + tablePil + " a ");
		requete.append("\n LIMIT 0; ");
		
		requete.append("CREATE INDEX idx_table_pil_temp ON " + tablePilTemp + "(id_source);");
		
		return requete;
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
	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder queryCopieTablePilotage(String tablePil, String tablePilTemp, TraitementPhase phaseAncien,
			TraitementPhase phaseNouveau, Integer nbEnr) {
		
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		Date date = new Date();

		requete.append("\n BEGIN; ");
		requete.append("\n WITH prep AS (");
		requete.append("\n SELECT a.*, count(1) OVER (ORDER BY date_traitement, " + ColumnEnum.ID_SOURCE.getColumnName()
				+ ") as cum_enr ");
		requete.append("\n FROM " + tablePil + " a ");
		requete.append("\n WHERE phase_traitement='" + phaseAncien + "'  AND '" + TraitementEtat.OK
				+ "'=ANY(etat_traitement) and etape=1 ) ");
		requete.append("\n , mark AS MATERIALIZED (SELECT a.* FROM prep a WHERE cum_enr<" + nbEnr + " ");
		requete.append("\n UNION   (SELECT a.* FROM prep a LIMIT 1)) ");

		// update the line in pilotage with etape=3 for the previous step
		requete.append("\n , update as ( UPDATE " + tablePil + " a set etape=3 from mark b where a."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName()
				+ " and a.etape=1 AND a.phase_traitement='" + phaseAncien + "'  AND '" + TraitementEtat.OK
				+ "'=ANY(a.etat_traitement)) ");

		// insert the line in pilotage with etape=1 for the current step
		requete.append("\n , insert as (INSERT INTO " + tablePil + " ");
		requete.append("\n (container, " + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", date_entree, id_norme, validite, periodicite, phase_traitement, etat_traitement, date_traitement, nb_enr, etape, jointure) ");
		requete.append("\n SELECT container, " + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", date_entree, id_norme, validite, periodicite, '" + phaseNouveau + "' as phase_traitement, '{"
				+ TraitementEtat.ENCOURS + "}' as etat_traitement ");
		requete.append("\n , to_timestamp('" + new SimpleDateFormat(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getApplicationFormat()).format(date) + "','" + ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getDatastoreFormat()
				+ "') , nb_enr, 1 as etape, jointure ");
		requete.append("\n FROM mark ");
		requete.append("\n RETURNING *) ");

		requete.append("\n INSERT INTO " + tablePilTemp + " SELECT * from insert; ");
	
		requete.append("\n COMMIT; ");
		
		return requete;
	}
	
	
	/**
	 * Query to update pilotage table when error occurs
	 * 
	 * @param phase
	 * @param tablePil
	 * @param exception
	 * @return
	 */
	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder queryUpdatePilotageError(TraitementPhase[] phase, String tablePil, Exception exception) {
		
		List<String> phaseList = Stream.of(phase).map(p -> p.toString()).toList();
		
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("UPDATE " + tablePil + " ");
		requete.append("SET etape=2, etat_traitement= '{" + TraitementEtat.KO + "}'");
		requete.append(", rapport=").appendText(exception.toString().replace("\r", ""));
		requete.append("WHERE phase_traitement IN (");
		requete.append(requete.sqlListeOfValues(phaseList));
		requete.append(") AND etat_traitement='{" + TraitementEtat.ENCOURS + "}' ");
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
	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder queryResetPreviousPhaseMark(String tablePil, String idSource, String tableSource) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		// mettre à etape = 0 la phase marquée à 3
		requete.append("\n UPDATE " + tablePil + " a ");
		requete.append("\n SET etape=0 ");
		requete.append("\n WHERE a.etape=3 ");
		if (idSource != null) {
			requete.append("\n AND a."+ColumnEnum.ID_SOURCE.getColumnName()+" = ").appendText(idSource);
		}

		if (tableSource != null) {
			requete.append("\n AND EXISTS (SELECT 1 FROM " + tableSource + " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ");
		}

		requete.append("\n ;");
		return requete;
	}

	/**
	 * Remise dans l'état juste avant le lancement des controles et insertion dans
	 * une table d'erreur pour un fichier particulier
	 *
	 * @param connexion
	 * @param phase
	 * @param tablePil
	 * @param exception
	 * @param tableDrop
	 * @throws ArcException
	 */
	public static void traitementSurErreur(Connection connexion, TraitementPhase phase, String tablePil, String idSource,
			ArcException exception) throws ArcException {
		// nettoyage de la connexion
		// comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de
		// la transaction
		// si on lui renvoie une requete SQL, il la refuse avec le message
		// ERROR: current transaction is aborted, commands ignored until end of
		// transaction block
		try {
			connexion.setAutoCommit(false);
			connexion.rollback();
		} catch (SQLException rollbackException) {
			throw new ArcException(rollbackException, ArcExceptionMessage.DATABASE_ROLLBACK_FAILED);
		}
		// promote the application user account to full right
		UtilitaireDao.get(0).executeImmediate(connexion, DatabaseConnexionConfiguration.switchToFullRightRole());

		traitementSurErreurPilotage(connexion, phase, tablePil, idSource, exception);

	}

	/**
	 * For the given file idSourc, mark the error in pilotage table and reset etape 3 to 0 
	 * @param connexion
	 * @param phase
	 * @param tablePil
	 * @param idSource
	 * @param exception
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	private static void traitementSurErreurPilotage(Connection connexion, TraitementPhase phase, String tablePil,
			String idSource, ArcException exception) throws ArcException 
	{
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(PilotageOperations.queryUpdatePilotageError(new TraitementPhase[] {phase}, tablePil, exception));
		requete.append("\n AND "+ColumnEnum.ID_SOURCE.getColumnName()+" = ").appendText(idSource);
		requete.append("\n ;");
		requete.append(PilotageOperations.queryResetPreviousPhaseMark(tablePil, idSource, null));
		UtilitaireDao.get(0).executeBlock(connexion, requete);
	}

}
