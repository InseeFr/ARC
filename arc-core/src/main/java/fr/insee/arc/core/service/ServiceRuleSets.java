package fr.insee.arc.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.dao.RegleDao;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.model.RuleSets;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

@Component
public class ServiceRuleSets {

	private static final Logger LOGGER = Logger.getLogger(ServiceRuleSets.class);
	@Autowired
	public ServiceRequeteSqlRegle servSql;


	/**
	 * Liste des rubriques de la table de données DSN
	 */
	List<String> listRubTable = new ArrayList<String>();
	
	
    public ServiceRuleSets(){
    	this.servSql = new ServiceRequeteSqlRegle();
    }
    
    public ServiceRuleSets(ArrayList<String> listRubTable){
        this.setListRubTable(listRubTable);
        this.servSql = new ServiceRequeteSqlRegle();
    }

	/**
	 * Récupération des Jeu de règle à appliquer sur la table à controler, la liste des (validité, norme) permet de sélectionner uniquement
	 * les jeux de règles utiles
	 *
	 * @param tableControle
	 * @param tableJeuDeRegle
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<RuleSets> recupJeuDeRegle(Connection connexion, String tableControle, String tableJeuDeRegle)
			throws SQLException {
		LoggerDispatcher.info("Récupération des Jeu de règle à appliquer sur la table à controler",LOGGER);
		ArrayList<RuleSets> listCal = new ArrayList<>();
		listCal = JeuDeRegleDao.recupJeuDeRegle(connexion, tableControle, tableJeuDeRegle);
		return listCal;
	}

	/**
	 * pour remplir un jeu de règle avec les règles y afférant
	 *
	 * @param jdr
	 *            , le jeu de règle à "complèter"
	 * @param espace
	 *            , la table des règles de controle
	 * @throws SQLException
	 */
	public void fillRegleControle(Connection connexion, RuleSets jdr, String espace) throws SQLException {
		LoggerDispatcher.info("recherche de regle dans la table : " + espace,LOGGER);
		ArrayList<RegleControleEntity> listRegleC = new ArrayList<>();
		listRegleC = RegleDao.getRegle(connexion, jdr, espace);
		jdr.setListRegleControle(listRegleC);
	}

    /**
     * Executer les règles liées à un jeu de règle sur une table donnée
     *
     * @param connexion
     *
     * @param jdr
     *            le jeu de règle dont il faut appliquer les règles
     * @param table
     *            la table de travail dont les enregistrement seront "marqués"
     * @throws SQLException
     */
	public void executeJeuDeRegle(Connection connexion, RuleSets jdr, String table) throws Exception {
		LoggerDispatcher.debug("executeJeuDeRegle", LOGGER);


		// TODO il faut enlever le nom du schema de la table sinon la requete
		// renvoie un ensemble null
		java.util.Date date = new java.util.Date();

		this.listRubTable = UtilitaireDao.get("arc").listeCol(connexion, table);

		if (this.listRubTable==null)
		{
			this.listRubTable = new ArrayList<>();
		}

		// exécuter les préactions
		LoggerDispatcher.info("Debut Pré-actions", LOGGER);

		ArrayList<String> p=new ArrayList<>();

		// récupérer les préactions du jeu de regle
		/**
		 * Attention, on suppose que la preaction ne contient qu'une seule rubrique et en plus celle de la règle
		 */
		for (RegleControleEntity reg : jdr.getListRegleControle()) {
		    LoggerHelper.debug(LOGGER, "reg.getPreAction() ", reg.getPreAction());
		    // Check is prea ction is null, "" or the String "null"
		    String preAction = reg.getPreAction();
			if (!(StringUtils.isEmpty(preAction))) {
				/**
				 * si la rubrique de la preaction n'est pas dans la table, il ne faut rien calculer
				 */
					p.add(ManipString.extractAllRubrique(reg.getPreAction())+" as " + reg.getRubriquePere());

			}
		}

		// appliquer les pré-actions ; modifier la table avec un fast update
		if (p.size()>0)
		{
			String pa[]= p.toArray(new String[0]);
			LoggerHelper.debug(LOGGER, "Longueur de mon tableau de préaction :",pa.length);
			LoggerHelper.debug(LOGGER, "Contenu a priori :",p.toString());
			UtilitaireDao.fastUpdate("arc",
					connexion
					, table
					, "id_source,id"
					, "id_norme = '" + jdr.getIdNorme() + "'::text  AND periodicite = '" + jdr.getPeriodicite() + "'::text AND to_date(validite,'yyyy-mm-dd')>='" + jdr.getValiditeInfString() + "'::date AND to_date(validite,'yyyy-mm-dd')<='" + jdr.getValiditeSupString() + "'::date "
					, pa
					);
		}

		LoggerDispatcher.debug("Fin Pré-actions", LOGGER);

		StringBuilder blocRequete = new StringBuilder();
		blocRequete.append(this.servSql.initTemporaryTable(jdr, table));
		UtilitaireDao.get("arc").executeBlock(connexion, blocRequete);
		blocRequete.setLength(0);

		int nbRegles = 0;
		int nbTotalRegles = jdr.getListRegleControle().size();

		for (RegleControleEntity reg : jdr.getListRegleControle()) {
			nbRegles++;

			reg.setTable(table);


			LoggerDispatcher.info("n° " + reg.getIdRegle() + " / classe : " + reg.getIdClasse() + " / commentaire : "
					+ reg.getCommentaire(), LOGGER);
			switch (reg.getIdClasse()) {
			case "NUM":
				if (this.listRubTable.contains(reg.getRubriquePere())) {
					blocRequete.append(this.servSql.ctlIsNumeric(reg));
					blocRequete.append(System.lineSeparator());
				} else {
					LoggerDispatcher.info("la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier",
							LOGGER);
				}
				break;
			case "DATE":
				if (this.listRubTable.contains(reg.getRubriquePere())) {
					blocRequete.append(this.servSql.ctlIsDate(reg));
					blocRequete.append(System.lineSeparator());
				} else {
					LoggerDispatcher.info("la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier",
							LOGGER);
				}
				break;
			case "ALPHANUM":
				if (this.listRubTable.contains(reg.getRubriquePere())) {
					blocRequete.append(this.servSql.ctlIsAlphanum(reg));
					blocRequete.append(System.lineSeparator());
				} else {
					LoggerDispatcher.info("la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier",
							LOGGER);
				}
				break;
			case "CARDINALITE":
				if (Integer.parseInt(reg.getBorneInf()) == 0) {// borneInf vaut
					// 0 donc
					// l'absence de
					// rubrique
					// moins gènant
					if (!this.listRubTable.contains(reg.getRubriquePere()) || !this.listRubTable.contains(reg.getRubriqueFils())) {
						// une des deux rubrique n'existe pas
						LoggerDispatcher.info(
								"la rubrique : " + reg.getRubriquePere() + " ou la rubrique : " + reg.getRubriqueFils()
								+ " n'existe pas dans ce fichier", LOGGER);
					} else {
						blocRequete.append(this.servSql.ctlCardinalite(reg));
						blocRequete.append(System.lineSeparator());
					}
				} else {// borneInf vaut 1 ou plus
					if (this.listRubTable.contains(reg.getRubriquePere()) && this.listRubTable.contains(reg.getRubriqueFils())) {
						// les deux existent
						blocRequete.append(this.servSql.ctlCardinalite(reg));
						blocRequete.append(System.lineSeparator());
					} else if (!this.listRubTable.contains(reg.getRubriquePere())
							&& !this.listRubTable.contains(reg.getRubriqueFils())) {
						// les deux n'existent pas
						LoggerDispatcher.info(
								"la rubrique : " + reg.getRubriquePere() + " ET la rubrique : " + reg.getRubriqueFils()
								+ " n'existe pas dans ce fichier", LOGGER);
						LoggerDispatcher.info("On ne fait rien", LOGGER);
					} else if (!this.listRubTable.contains(reg.getRubriqueFils())) {
						// rubrique2 n'existe pas
						LoggerDispatcher.info("la rubrique : " + reg.getRubriqueFils()
								+ " n'existe pas dans ce fichier", LOGGER);
						blocRequete.append(this.servSql.ctlCardinaliteSansMembre(reg, reg.getRubriquePere()));
						blocRequete.append(System.lineSeparator());
					} else {
						// rubrique1 n'existe pas
						LoggerDispatcher.info("la rubrique : " + reg.getRubriquePere()
								+ " n'existe pas dans ce fichier", LOGGER);
						blocRequete.append(this.servSql.ctlCardinaliteSansMembre(reg, reg.getRubriqueFils()));
						blocRequete.append(System.lineSeparator());
					}
				}
				break;
			case "CONDITION":
				blocRequete.append(executeRegleCondition(jdr, reg));
				blocRequete.append(System.lineSeparator());
				break;
			default:
				LoggerDispatcher.info("Classe pas connue", LOGGER);
			}

			if (nbRegles % 1 == 0) {
				LoggerDispatcher.info("Execution de " + nbRegles + "/" + nbTotalRegles, LOGGER);
			}

				if (blocRequete.length()>FormatSQL.TAILLE_MAXIMAL_BLOC_SQL)
				{
					UtilitaireDao.get("arc").executeBlock(connexion, "SET enable_nestloop=off; "+blocRequete.toString()+"SET enable_nestloop=on; ");
					blocRequete.setLength(0);
				}



		}

		UtilitaireDao.get("arc").executeBlock(connexion, blocRequete);
		blocRequete.setLength(0);



		LoggerDispatcher.info("Execution de " + nbRegles + "/" + nbTotalRegles, LOGGER);
		blocRequete.append(this.servSql.markTableResultat());
		blocRequete.append(this.servSql.dropControleTemporaryTables());

		UtilitaireDao.get("arc").executeBlock(connexion, "SET enable_nestloop=off; "+blocRequete+"SET enable_nestloop=on; ");
		LoggerDispatcher.info("Fin executeJeuDeRegle", LOGGER);
		LoggerDispatcher.info("Temps de controle : " + (new java.util.Date().getTime() - date.getTime()), LOGGER);

	}

	/**
	 * Préparation à l'exécution d'une règle de type CONDITION Pour chaque rubrique de la condition, il faut lui associé sa règle de typage
	 *
	 * @param reg
	 * @param jdr
	 * @param reg
	 * @param table
	 * @throws SQLException
	 */
	public String executeRegleCondition(RuleSets jdr, RegleControleEntity reg) throws SQLException {
		LoggerDispatcher.info("Je lance executeRegleCondition()",LOGGER);
		String requete = "";

		ArrayList<String> listRubrique = new ArrayList<String>();

		// Passage en MAJUSCULE de la condition
		String condition;
		if (reg.getCondition() == null) {
			condition = reg.getCondition();
		} else {
			condition = reg.getCondition().toUpperCase();
		}
		//		LoggerDispatcher.info("Ancien : " + reg.getCondition() + ", nouveau : " + condition,logger);
		listRubrique = ManipString.extractRubriques(condition);

		// listRubrique = ManipString.extractRubriques(reg.getCondition());

		if (this.listRubTable.containsAll(listRubrique)) {
			Map<String, RegleControleEntity> mapRubrique = new HashMap<String, RegleControleEntity>();
			for (String rub : listRubrique) {
				LoggerDispatcher.debug("Je parcours la liste listRubrique sur l'élément : " + rub, LOGGER);
				RegleControleEntity regle = new RegleControleEntity();
				regle = findType(jdr, rub);
				mapRubrique.put(rub, regle);
			}
			LoggerDispatcher.debug("MapRubrique contient : " + mapRubrique.toString(), LOGGER);
			requete = this.servSql.ctlCondition(reg, mapRubrique);
		} else {
			LoggerDispatcher.info("Exécution de CONDITION non appliquée car il manque des rubriques",LOGGER);
		}
		return requete;
	}

	/**
	 * Trouver le typage d'une rubrique dans un jeu de règle donné par défaut, le type sera considéré comme ALPHANUMERIQUE
	 *
	 * @param jdr
	 *            , le jeu de règle contenant la rubrique
	 * @param rub
	 * @return
	 */
	private RegleControleEntity findType(RuleSets jdr, String rub) {
		RegleControleEntity reg = new RegleControleEntity();
		boolean isFind = false;
		LoggerDispatcher.debug("La rubrique dont on cherche le type : " + rub, LOGGER);
		String rubriquePere = "";
		String idClasse = "";
		for (RegleControleEntity regC : jdr.getListRegleControle()) {
			rubriquePere = regC.getRubriquePere();
			idClasse = regC.getIdClasse();
			LoggerDispatcher.debug("La rubrique de la regle testée : " + rubriquePere + " et le type : " + idClasse,
					LOGGER);
			if (rub.equals(rubriquePere)
					&& (idClasse.equals("NUM") || idClasse.equals("DATE") || idClasse.equals("ALPHANUM"))) {
				LoggerDispatcher.debug("J'ai trouvé une règle de typage", LOGGER);
				reg.setIdRegle(regC.getIdRegle());
				reg.setIdClasse(regC.getIdClasse());
				reg.setRubriquePere(rub);
				reg.setCondition(regC.getCondition());
				isFind = true;
				break;// je ne m'interesse qu'au premier
			}
		}
		if (!isFind) {// par défaut le type sera considéré comme numérique
			reg.setIdClasse("ALPHANUM");
			reg.setRubriquePere(rub);
		}
		return reg;
	}

	// Getters et Setters
	public ServiceRequeteSqlRegle getServSql() {
		return this.servSql;
	}

	public void setServSql(ServiceRequeteSqlRegle servSql) {
		this.servSql = servSql;
	}
	

    public List<String> getListRubTable() {
        return listRubTable;
    }

    public void setListRubTable(List<String> listeAttribut) {
        this.listRubTable = listeAttribut;
    }

}