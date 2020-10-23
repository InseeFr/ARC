package fr.insee.arc.core.service.engine.controle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.dao.RegleDao;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.core.util.StaticLoggerDispatcher;


@Component
public class ServiceJeuDeRegle {

	private static final Logger logger = LogManager.getLogger(ServiceJeuDeRegle.class);
	public ServiceRequeteSqlRegle servSql;
	
	/**
	 * Liste des rubriques de la table de données DSN
	 */
	List<String> listRubTable = new ArrayList<String>();
	String tableControleRegle;

    public ServiceJeuDeRegle(){
    	this.servSql = new ServiceRequeteSqlRegle();
    }
	
    public ServiceJeuDeRegle(String tableControleRegle){
    	this.servSql = new ServiceRequeteSqlRegle();
    	this.tableControleRegle=tableControleRegle;
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
	public ArrayList<JeuDeRegle> recupJeuDeRegle(Connection connexion, String tableControle, String tableJeuDeRegle)
			throws SQLException {
		StaticLoggerDispatcher.info("Récupération des Jeu de règle à appliquer sur la table à controler",logger);
		ArrayList<JeuDeRegle> listCal = new ArrayList<>();
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
	@Deprecated
	public void fillRegleControle(Connection connexion, JeuDeRegle jdr, String espace) throws SQLException {
		StaticLoggerDispatcher.info("recherche de regle dans la table : " + espace,logger);
		ArrayList<RegleControleEntity> listRegleC = new ArrayList<RegleControleEntity>();
		listRegleC = RegleDao.getRegle(connexion, jdr, espace);
		jdr.setListRegleControle(listRegleC);
	}

	/**
	 * pour remplir un jeu de règle avec les règles y afférant
	 *
	 * @param jdr
	 *            , le jeu de règle à "complèter"
	 * @param tableRegle
	 *            , la table des règles de controle
	 * @param tableIn
	 *            , la table à controler
	 * @throws SQLException
	 */	
	public void fillRegleControle(Connection connexion, JeuDeRegle jdr, String tableRegle, String tableIn) throws SQLException {
		StaticLoggerDispatcher.info("recherche de regle dans la table : " + tableRegle,logger);
		ArrayList<RegleControleEntity> listRegleC = new ArrayList<RegleControleEntity>();
		listRegleC = RegleDao.getRegle(connexion, tableRegle, tableIn);
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
	public void executeJeuDeRegle(Connection connexion, JeuDeRegle jdr, String table, String structure) throws Exception {
		StaticLoggerDispatcher.debug("executeJeuDeRegle", logger);

//		ArrayList<QueryThread> threadList=new ArrayList<QueryThread>();
//		ArrayList<Connection> connexionList=ApiService.prepareThreads(parallel, connexion);

		// TODO il faut enlever le nom du schema de la table sinon la requete
		// renvoie un ensemble null
		// listRubTable=daoR.getColumnTable(table);
		java.util.Date date = new java.util.Date();

		this.listRubTable = UtilitaireDao.get("arc").listeCol(connexion, table);

		if (this.listRubTable==null)
		{
			this.listRubTable = new ArrayList<String>();
		}

		// exécuter les préactions
		StaticLoggerDispatcher.info("Debut Pré-actions", logger);

		ArrayList<String> p=new ArrayList<String>();

		// récupérer les préactions du jeu de regle
		/**
		 * Attention, on suppose que la preaction ne contient qu'une seule rubrique et en plus celle de la règle
		 */
		for (RegleControleEntity reg : jdr.getListRegleControle()) {
			if (reg.getPreAction()!=null && !StringUtils.isEmpty(reg.getPreAction())) {
//				String rubrique = ManipString.extractAllRubrique(reg.getPreAction());
				/**
				 * si la rubrique de la preaction n'est pas dans la table, il ne faut rien calculer
				 */
//				if(this.listRubTable.contains(rubrique)){
					p.add(ManipString.extractAllRubrique(reg.getPreAction())+" as " + reg.getRubriquePere());
//				}else{
//					LoggerHelper.debugAsComment(logger,"la rubrique : ",rubrique, " n'est pas présente dans la table de travail");
//				}
			}
		}

		// appliquer les pré-actions ; modifier la table avec un fast update
		if (p.size()>0)
		{
			String pa[]= p.toArray(new String[0]);
			LoggerHelper.debug(logger, "Longueur de mon tableau de préaction :",pa.length);
			LoggerHelper.debug(logger, "Contenu a priori :",p.toString());
			UtilitaireDao.fastUpdate("arc",
					connexion
					, table
					, "id_source,id"
					// condition not needed anymore : one file at a time thread
					, "true"
					, pa
					);
		}

		StaticLoggerDispatcher.debug("Fin Pré-actions", logger);

		//		StaticLoggerDispatcher.info("Les noms de colonne de la table : " + table + ", " + listRubTable.toString(),logger);
		StringBuilder blocRequete = new StringBuilder();
		blocRequete.append(this.servSql.initTemporaryTable(jdr, table));
//		UtilitaireDao.get("arc").executeBlock(connexion, blocRequete);
//		blocRequete.setLength(0);

		int nbRegles = 0;
		int nbTotalRegles = jdr.getListRegleControle().size();

		for (RegleControleEntity reg : jdr.getListRegleControle()) {
			nbRegles++;

			reg.setTable(table);
			// reg.setIdSource(idSource);
			// message.append("/ regle : "+reg.getIdRegle() +
			// " -> Ma classe de contrôle est : " + reg.getClasse_ctl()+" ");

			StaticLoggerDispatcher.info("n° " + reg.getIdRegle() + " / classe : " + reg.getIdClasse() + " / commentaire : "
					+ reg.getCommentaire(), logger);
			// StaticLoggerDispatcher.debug("sur l'idsource : "+reg.getIdSource());
			switch (reg.getIdClasse()) {
			case "NUM":
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.servSql.ctlIsNumeric(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case "DATE":
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.servSql.ctlIsDate(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case "ALPHANUM":
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.servSql.ctlIsAlphanum(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case "CARDINALITE":
				if (this.listRubTable.contains(reg.getRubriquePere()) 
						// rules to set tree root and father label are ignored
						&& !(reg.getRubriquePere().equalsIgnoreCase(ApiService.ROOT)))
				{
					blocRequete.append(executeRegleCardinalite(jdr, reg));
					blocRequete.append(System.lineSeparator());
				}
				else
				{
					StaticLoggerDispatcher.info(
							"la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier", logger);
				}
				break;
			case "STRUCTURE":
				{
					if (tableControleRegle!=null && structure!=null)
					{
						blocRequete.append(this.servSql.ctlStructure(reg,structure,tableControleRegle));
					}
				}
			break;
			case "CONDITION":
				blocRequete.append(executeRegleCondition(jdr, reg));
				blocRequete.append(System.lineSeparator());
				break;
			case "REGEXP":
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.servSql.ctlMatchesRegexp(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case "ENUM_BRUTE": case "ENUM_TABLE":
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.servSql.ctlIsValueIn(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			default:
				StaticLoggerDispatcher.info("Classe pas connue", logger);
			}

			if (nbRegles % 1 == 0) {
				StaticLoggerDispatcher.info("Execution de " + nbRegles + "/" + nbTotalRegles, logger);
			}

//    		ApiService.waitForThreads(parallel,threadList,connexionList);
//    		QueryThread r=new QueryThread("SET enable_nestloop=off; "+blocRequete.toString()+"SET enable_nestloop=on; ",threadList,connexionList,ManipString.substringBeforeFirst(table, "."));
//    		threadList.add(r);
//    		r.start();
//    		blocRequete.setLength(0);
//
//
				if (blocRequete.length()>FormatSQL.TAILLE_MAXIMAL_BLOC_SQL)
				{
					UtilitaireDao.get("arc").executeImmediate(connexion, "SET enable_nestloop=off; "+blocRequete.toString()+"SET enable_nestloop=on; ");
					blocRequete.setLength(0);
				}



		}
//
//		UtilitaireDao.get("arc").executeBlock(connexion, "SET enable_nestloop=off; "+blocRequete.toString()+"SET enable_nestloop=on; ");
//		blocRequete.setLength(0);

//		ApiService.waitForThreads(0,threadList,connexionList);


		StaticLoggerDispatcher.info("Execution de " + nbRegles + "/" + nbTotalRegles, logger);
		blocRequete.append(this.servSql.markTableResultat());
		blocRequete.append(this.servSql.dropControleTemporaryTables());
		// StaticLoggerDispatcher.info("Mon bloc SQL d'execution de regles : " + blocRequete,logger);
		// System.out.println("Mon Stringbuilder : " + blocRequete);

		UtilitaireDao.get("arc").executeImmediate(connexion, "SET enable_nestloop=off; "+blocRequete+"SET enable_nestloop=on; ");
		StaticLoggerDispatcher.info("Fin executeJeuDeRegle", logger);
		StaticLoggerDispatcher.info("Temps de controle : " + (new java.util.Date().getTime() - date.getTime()), logger);

	}

	/** Vérifie si la règle est à appliquer pour ces rubriques.*/
	private boolean regleEstAAppliquer(List<String> listRubriques, RegleControleEntity reg) {
		if (!listRubriques.contains(reg.getRubriquePere())){
			StaticLoggerDispatcher.info("la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier",
					logger);
			return false;
		}
		return true;
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
	public String executeRegleCondition(JeuDeRegle jdr, RegleControleEntity reg) throws SQLException {
		StaticLoggerDispatcher.info("Je lance executeRegleCondition()",logger);
		String requete = "";

		ArrayList<String> listRubrique = ManipString.extractRubriques(reg.getCondition());
		listRubrique.replaceAll(String::toUpperCase);
		
		// listRubrique = ManipString.extractRubriques(reg.getCondition());

		if (this.listRubTable.containsAll(listRubrique)) {
			Map<String, RegleControleEntity> mapRubrique = new HashMap<String, RegleControleEntity>();
			for (String rub : listRubrique) {
				StaticLoggerDispatcher.debug("Je parcours la liste listRubrique sur l'élément : " + rub, logger);
				RegleControleEntity regle = new RegleControleEntity();
				regle = findType(jdr, rub);
				mapRubrique.put(rub, regle);
			}
			StaticLoggerDispatcher.debug("MapRubrique contient : " + mapRubrique.toString(), logger);
			requete = this.servSql.ctlCondition(reg, mapRubrique);
		} else {
			StaticLoggerDispatcher.info("Exécution de CONDITION non appliquée car il manque des rubriques",logger);
		}
		return requete;
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
	public String executeRegleCardinalite(JeuDeRegle jdr, RegleControleEntity reg) throws SQLException {
		StaticLoggerDispatcher.info("Je lance executeRegleCardinalite()",logger);
		String requete = "";

		ArrayList<String> listRubrique = ManipString.extractRubriques(reg.getCondition());
		listRubrique.replaceAll(String::toUpperCase);

		requete = this.servSql.ctlCardinalite(reg, listRubrique, listRubTable);
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
	private RegleControleEntity findType(JeuDeRegle jdr, String rub) {
		RegleControleEntity reg = new RegleControleEntity();
		boolean isFind = false;
		StaticLoggerDispatcher.debug("La rubrique dont on cherche le type : " + rub, logger);
		String rubriquePere = "";
		String idClasse = "";
		for (RegleControleEntity regC : jdr.getListRegleControle()) {
			rubriquePere = regC.getRubriquePere();
			idClasse = regC.getIdClasse();
			StaticLoggerDispatcher.debug("La rubrique de la regle testée : " + rubriquePere + " et le type : " + idClasse,
					logger);
			if (rub.equals(rubriquePere)
					&& (idClasse.equals("NUM") || idClasse.equals("DATE") || idClasse.equals("ALPHANUM"))) {
				StaticLoggerDispatcher.debug("J'ai trouvé une règle de typage", logger);
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