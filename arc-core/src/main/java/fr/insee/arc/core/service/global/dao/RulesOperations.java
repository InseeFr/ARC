package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class RulesOperations {

	private RulesOperations() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Requete permettant de récupérer les règles pour un id_source donnée et une
	 * table de regle
	 * 
	 * @param idSource      : identifiant du fichier
	 * @param tableRegle    : table de regle
	 * @param tablePilotage : table de pilotage
	 * @return
	 */
	public static String getRegles(String tableRegle, String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM " + tableRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		return requete.toString();
	}

	public static String getRegles(String tableRegle, FileIdCard fileIdCard) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM " + tableRegle + " a WHERE ");
		requete.append(conditionRegle(fileIdCard));
		return requete.toString();
	}

	/**
	 * Récupère toutes les rubriques utilisées dans les regles relatives au fichier
	 * 
	 * @param idSource
	 * @param tablePilotage
	 * @param tableNormageRegle
	 * @param tableControleRegle
	 * @param tableMappingRegle
	 * @return
	 */
	public static String getAllRubriquesInRegles(String tablePilotage, String tableNormageRegle,
			String tableControleRegle, String tableMappingRegle) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM ( ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_col),'([iv]_{1,1}[[:alnum:]\\_\\$]+)','g')) as var from "
						+ tableMappingRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_pere) as var from "
				+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_fils) as var from "
				+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(condition),'([iv]_{1,1}[[:alnum:]\\_\\$]+)','g')) as var from "
						+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(pre_action),'([iv]_{1,1}[[:alnum:]\\_\\$]+)','g')) as var from "
						+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique) as var from "
				+ tableNormageRegle + " a where id_classe!='suppression' AND ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_nmcl) as var from "
				+ tableNormageRegle + " a where id_classe!='suppression' AND ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n ) ww where var is NOT NULL; ");
		return requete.toString();
	}

	/**
	 * Retourne la clause WHERE SQL qui permet de selectionne les bonne regles pour
	 * un fichier
	 * 
	 * @param idSource
	 * @param tablePilotage
	 * @return
	 */
	private static String conditionRegle(String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n ");
		requete.append("EXISTS ( SELECT * FROM " + tablePilotage + " b ");
		requete.append("WHERE a.id_norme=b.id_norme ");
		requete.append("AND a.periodicite=b.periodicite ");
		requete.append("AND a.validite_inf<=to_date(b.validite,'"
				+ ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat() + "') ");
		requete.append("AND a.validite_sup>=to_date(b.validite,'"
				+ ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat() + "') ");
		requete.append(") ");
		return requete.toString();
	}

	private static String conditionRegle(FileIdCard fileIdCard) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n ");
		requete.append("a.id_norme='" + fileIdCard.getIdNorme() + "' ");
		requete.append("AND a.periodicite='" + fileIdCard.getPeriodicite() + "' ");
		requete.append("AND a.validite_inf<=to_date('" + fileIdCard.getValidite() + "','"
				+ ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat() + "') ");
		requete.append("AND a.validite_sup>=to_date('" + fileIdCard.getValidite() + "','"
				+ ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat() + "') ");
		requete.append(";");
		return requete.toString();
	}

	/**
	 * Requete permettant de récupérer les règles pour un id_source donnée et une
	 * table de regle
	 * 
	 * @param id_source
	 * @param tableRegle
	 * @return SQL pil.id_source, pil.jointure, pil.id_norme, pil.validite,
	 *         pil.periodicite
	 */
	public static String getNormeAttributes(String idSource, String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT pil." + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", pil.jointure, pil.id_norme, pil.validite, pil.periodicite " + "FROM " + tablePilotage + " pil "
				+ " WHERE " + ColumnEnum.ID_SOURCE.getColumnName() + "='" + idSource + "' ");
		return requete.toString();
	}

	/**
	 * récupere le contenu d'une requete dans un map
	 * 
	 * @param c
	 * @param req
	 * @return
	 * @throws ArcException
	 */
	public static Map<String, List<String>> getBean(Connection c, String req) throws ArcException {
		GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(c, new ArcPreparedStatementBuilder(req)));
		return gb.mapContent(true);
	}

	/**
	 * Méthodes pour marquer la table de pilotage temporaire avec le jeu de règle
	 * appliqué
	 *
	 * @return
	 */
	public static String marqueJeuDeRegleApplique(TraitementPhase currentPhase, String envExecution, String pilTemp) {
		return marqueJeuDeRegleApplique(currentPhase, envExecution, pilTemp, null);
	}

	public static String marqueJeuDeRegleApplique(TraitementPhase currentPhase, String envExecution, String pilTemp,
			String defaultEtatTraitement) {
		StringBuilder requete = new StringBuilder();
		requete.append("WITH ");
		requete.append("prep AS (SELECT a." + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", a.id_norme, a.periodicite, b.validite_inf, b.validite_sup, b.version ");
		requete.append("	FROM " + pilTemp + " a  ");
		requete.append("	INNER JOIN " + ViewEnum.JEUDEREGLE.getFullName(envExecution)
				+ " b ON a.id_norme=b.id_norme AND a.periodicite=b.periodicite AND b.validite_inf <=a.validite::date AND b.validite_sup>=a.validite::date ");
		requete.append("	WHERE phase_traitement='" + currentPhase + "') ");
		requete.append("UPDATE " + pilTemp + " AS a ");
		requete.append("SET validite_inf=prep.validite_inf, validite_sup=prep.validite_sup, version=prep.version ");
		if (defaultEtatTraitement != null) {
			requete.append(", etat_traitement='{" + defaultEtatTraitement + "}'");
		}
		requete.append("FROM prep ");
		requete.append("WHERE a.phase_traitement='" + currentPhase + "'; ");
		return requete.toString();
	}

	/**
	 * Set the id card for a file idSource
	 * 
	 * @param connection
	 * @param idSource
	 * @param pilotageTable
	 * @return
	 * @throws ArcException
	 */
	public static FileIdCard fileIdCardFromPilotage(Connection connection, String pilotageTable, String idSource)
			throws ArcException {

		FileIdCard fileIdCard = new FileIdCard(idSource);

		Map<String, List<String>> fileIds = RulesOperations.getBean(connection,
				RulesOperations.getNormeAttributes(idSource, pilotageTable));

		fileIdCard.setFileIdCard(fileIds.get(ColumnEnum.ID_NORME.getColumnName()).get(0),
				fileIds.get(ColumnEnum.VALIDITE.getColumnName()).get(0),
				fileIds.get(ColumnEnum.PERIODICITE.getColumnName()).get(0),
				fileIds.get(ColumnEnum.JOINTURE.getColumnName()).get(0));

		return fileIdCard;

	}

}
