package fr.insee.arc.core.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

public class RegleDao {

	private static final Logger logger = LogManager.getLogger(RegleDao.class);

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
		StaticLoggerDispatcher.debug("getRegle", logger);

		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();

		PreparedStatementBuilder sb = new PreparedStatementBuilder();
		sb.append("SELECT 	id_regle, id_classe, ");
		sb.append("		rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action  ");
		sb.append("FROM "+espace+" ");
		sb.append("WHERE id_norme='"+jdr.getIdNorme()+"'::text ");
		sb.append("		AND periodicite='"+jdr.getPeriodicite()+"'::text ");
		sb.append("		AND validite_inf='"+jdr.getValiditeInfString()+"'::date ");
		sb.append("		AND validite_sup='"+jdr.getValiditeSupString()+"'::date ");
		sb.append("		AND version='"+jdr.getVersion()+"'::text ");
		sb.append("; ");

		HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, sb)).mapContent();
		
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

		StaticLoggerDispatcher.debug("J'ai trouvé " + listRegle.size() + " rattaché à ce jeu de règle", logger);
		return listRegle;
	}

	
	public static ArrayList<RegleControleEntity> getRegle(Connection connexion, String tableRegle, String tableIn) throws SQLException {
		StaticLoggerDispatcher.debug("getRegle", logger);

		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();

		PreparedStatementBuilder sb = new PreparedStatementBuilder();
		sb.append("SELECT 	id_regle, id_classe, ");
		sb.append("		rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action  ");
		sb.append("FROM "+tableRegle+" a ");
		sb.append(" WHERE EXISTS (SELECT 1 FROM (SELECT id_norme, periodicite, validite FROM "+tableIn+" LIMIT 1) b "); 
		sb.append("		WHERE a.id_norme=b.id_norme ");
		sb.append("		AND a.periodicite=b.periodicite "); 
		sb.append("		AND to_date(b.validite,'YYYY-MM-DD')>=a.validite_inf "); 
		sb.append("		AND to_date(b.validite,'YYYY-MM-DD')<=a.validite_sup) ");
		sb.append("; ");

//		StaticLoggerDispatcher.debug("Ma requête : " + requete, logger);

			HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, sb)).mapContent();
		
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

		StaticLoggerDispatcher.debug("J'ai trouvé " + listRegle.size() + " rattaché à ce jeu de règle", logger);
		return listRegle;
	}

}
