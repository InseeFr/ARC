package fr.insee.arc.core.service.api.query;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.NormeFichier;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ServiceRules {

	private ServiceRules() {
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

	public static String getRegles(String tableRegle, NormeFichier normeFichier) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM " + tableRegle + " a WHERE ");
		requete.append(conditionRegle(normeFichier));
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
		requete.append("AND a.validite_inf<=to_date(b.validite,'YYYY-MM-DD') ");
		requete.append("AND a.validite_sup>=to_date(b.validite,'YYYY-MM-DD') ");
		requete.append(") ");
		return requete.toString();
	}

	private static String conditionRegle(NormeFichier normeFichier) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n ");
		requete.append("a.id_norme='" + normeFichier.getIdNorme() + "' ");
		requete.append("AND a.periodicite='" + normeFichier.getPeriodicite() + "' ");
		requete.append("AND a.validite_inf<=to_date('" + normeFichier.getValidite() + "','YYYY-MM-DD') ");
		requete.append("AND a.validite_sup>=to_date('" + normeFichier.getValidite() + "','YYYY-MM-DD') ");
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
	 *         pil.periodicite, pil.validite
	 */
	public static String getNormeAttributes(String idSource, String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT pil." + ColumnEnum.ID_SOURCE.getColumnName()
				+ ", pil.jointure, pil.id_norme, pil.validite, pil.periodicite, pil.validite " + "FROM " + tablePilotage
				+ " pil " + " WHERE " + ColumnEnum.ID_SOURCE.getColumnName() + "='" + idSource + "' ");
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
	public static HashMap<String, ArrayList<String>> getBean(Connection c, String req) throws ArcException {
		GenericBean gb = new GenericBean(
				UtilitaireDao.get(0).executeRequest(c, new ArcPreparedStatementBuilder(req)));
		return gb.mapContent(true);
	}
}
