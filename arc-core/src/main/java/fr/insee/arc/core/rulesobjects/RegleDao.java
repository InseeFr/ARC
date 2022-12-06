package fr.insee.arc.core.rulesobjects;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class RegleDao {

	private RegleDao() {
		throw new IllegalStateException("Utility class");
	}

	private static final Logger logger = LogManager.getLogger(RegleDao.class);

	public static ArrayList<RegleControleEntity> getRegle(Connection connexion, String tableRegle, String tableIn)
			throws ArcException {
		StaticLoggerDispatcher.debug("getRegle", logger);

		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();

		ArcPreparedStatementBuilder sb = new ArcPreparedStatementBuilder();
		sb.append("SELECT id_regle, id_classe, ");
		sb.append(" rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, blocking_threshold, error_row_processing  ");
		sb.append(" FROM " + tableRegle + " a ");
		sb.append(" WHERE EXISTS (SELECT 1 FROM (SELECT id_norme, periodicite, validite FROM " + tableIn
				+ " LIMIT 1) b ");
		sb.append(" WHERE a.id_norme=b.id_norme ");
		sb.append(" AND a.periodicite=b.periodicite ");
		sb.append(" AND to_date(b.validite,'YYYY-MM-DD')>=a.validite_inf ");
		sb.append(" AND to_date(b.validite,'YYYY-MM-DD')<=a.validite_sup) ");
		sb.append("; ");

		HashMap<String, ArrayList<String>> g = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, sb))
				.mapContent();

		if (!g.isEmpty()) {
			for (int i = 0; i < g.get("id_regle").size(); i++) {
				// Instanciation
				RegleControleEntity reg = new RegleControleEntity();
				// Remplissage
				reg.setIdRegle(g.get("id_regle").get(i));
				reg.setIdClasse(g.get("id_classe").get(i));
				reg.setRubriquePere(g.get("rubrique_pere").get(i));
				reg.setRubriqueFils(g.get("rubrique_fils").get(i));
				reg.setBorneInf(g.get("borne_inf").get(i));
				reg.setBorneSup(g.get("borne_sup").get(i));
				reg.setCondition(g.get("condition").get(i));
				reg.setPreAction(g.get("pre_action").get(i));
				reg.setBlockingThreshold(g.get("blocking_threshold").get(i));
				reg.setErrorRowProcessing(g.get("error_row_processing").get(i));

				// Ajout à la liste de résultat
				listRegle.add(reg);
			}
		}

		StaticLoggerDispatcher.debug("J'ai trouvé " + listRegle.size() + " rattaché à ce jeu de règle", logger);
		return listRegle;
	}

}
