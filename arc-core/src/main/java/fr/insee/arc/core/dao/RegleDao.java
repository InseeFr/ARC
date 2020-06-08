package fr.insee.arc.core.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;

public class RegleDao {

	private static final Logger logger = Logger.getLogger(RegleDao.class);

	/**
	 * Récupération des règles liés à un jeu de règle
	 *
	 * @param jdr
	 * @param espace
	 *            (production ou bac à sable)
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public static ArrayList<RegleControleEntity> getRegle(Connection connexion, JeuDeRegle jdr, String espace) throws SQLException {
		LoggerDispatcher.debug("getRegle", logger);

		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 	id_regle, id_classe, ");
		sb.append("		rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action  ");
		sb.append("FROM {0} ");
		sb.append("WHERE id_norme='{1}'::text ");
		sb.append("		AND periodicite='{2}'::text ");
		sb.append("		AND validite_inf='{3}'::date ");
		sb.append("		AND validite_sup='{4}'::date ");
		sb.append("		AND version='{5}'::text ");
		sb.append("; ");

		StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.getRequete(sb.toString(), espace, jdr.getIdNorme(), jdr.getPeriodicite(),
				jdr.getValiditeInfString(), jdr.getValiditeSupString(), jdr.getVersion()));
//		LoggerDispatcher.debug("Ma requête : " + requete, logger);

			HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requete)).mapContent();
		
			if (!g.isEmpty())
			{
				for (int i=0;i<g.get("id_regle").size();i++)
				{
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
	
					// Ajout à la liste de résultat
					listRegle.add(reg);
				}
			}

		LoggerDispatcher.debug("J'ai trouvé " + listRegle.size() + " rattaché à ce jeu de règle", logger);
		return listRegle;
	}

	
	public static ArrayList<RegleControleEntity> getRegle(Connection connexion, String tableRegle, String tableIn) throws SQLException {
		LoggerDispatcher.debug("getRegle", logger);

		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 	id_regle, id_classe, ");
		sb.append("		rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action  ");
		sb.append("FROM {0} a ");
		sb.append(" WHERE EXISTS (SELECT 1 FROM (SELECT id_norme, periodicite, validite FROM {1} LIMIT 1) b "); 
		sb.append("		WHERE a.id_norme=b.id_norme ");
		sb.append("		AND a.periodicite=b.periodicite "); 
		sb.append("		AND to_date(b.validite,'YYYY-MM-DD')>=a.validite_inf "); 
		sb.append("		AND to_date(b.validite,'YYYY-MM-DD')<=a.validite_sup) ");
		sb.append("; ");

		StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.getRequete(sb.toString(), tableRegle, tableIn));
//		LoggerDispatcher.debug("Ma requête : " + requete, logger);

			HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requete)).mapContent();
		
			if (!g.isEmpty())
			{
				for (int i=0;i<g.get("id_regle").size();i++)
				{
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
	
					// Ajout à la liste de résultat
					listRegle.add(reg);
				}
			}

		LoggerDispatcher.debug("J'ai trouvé " + listRegle.size() + " rattaché à ce jeu de règle", logger);
		return listRegle;
	}

}
