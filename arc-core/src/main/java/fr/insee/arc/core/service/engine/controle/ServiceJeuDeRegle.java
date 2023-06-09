package fr.insee.arc.core.service.engine.controle;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.rulesobjects.RegleDao;
import fr.insee.arc.core.service.api.ApiService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;


@Component
public class ServiceJeuDeRegle {

	private static final Logger logger = LogManager.getLogger(ServiceJeuDeRegle.class);
	private ServiceRequeteSqlRegle servSql;
	
	/**
	 * Liste des rubriques de la table de données DSN
	 */
	private List<String> listRubTable = new ArrayList<>();
	private String tableControleRegle;

    public ServiceJeuDeRegle(){
    	this.servSql = new ServiceRequeteSqlRegle();
    }
	
    public ServiceJeuDeRegle(String tableControleRegle){
    	this.servSql = new ServiceRequeteSqlRegle();
    	this.tableControleRegle=tableControleRegle;
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
	 * @throws ArcException
	 */	
	public void fillRegleControle(Connection connexion, JeuDeRegle jdr, String tableRegle, String tableIn) throws ArcException {
		StaticLoggerDispatcher.info("recherche de regle dans la table : " + tableRegle,logger);
		ArrayList<RegleControleEntity> listRegleC = RegleDao.getRegle(connexion, tableRegle, tableIn);
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
     * @throws ArcException
     */
	public void executeJeuDeRegle(Connection connexion, JeuDeRegle jdr, String table, String structure) throws ArcException {
		StaticLoggerDispatcher.debug("executeJeuDeRegle", logger);

		java.util.Date date = new java.util.Date();
		
		// register the columns from the source table into listRubTable
		registerRubriquesFromSourceTable(connexion, table);
		
		// execute the correction rules (aka "preAction") before the control step
		preAction(connexion, jdr, table);

		// execute the control rules
		control(connexion, jdr, table, structure);

		StaticLoggerDispatcher.info("Temps de controle : " + (new java.util.Date().getTime() - date.getTime()), logger);

	}
	
	/**
	 * Get the columns (also named "rubriques") list in uppercase from the table in order to know what controls must be evaluated
	 * @param connexion
	 * @param table
	 * @throws ArcException
	 */
	private void registerRubriquesFromSourceTable(Connection connexion, String table) throws ArcException
	{
		this.listRubTable = UtilitaireDao.get(0).getColumns(connexion, table);
		this.listRubTable.replaceAll(String::toUpperCase);
	}
	
	
	/**
	 * Execute the correction actions before control
	 * @param connexion
	 * @param jdr
	 * @param table
	 * @param structure
	 * @throws ArcException 
	 */
	private void preAction(Connection connexion, JeuDeRegle jdr, String table) throws ArcException {
		
		// exécuter les préactions
				StaticLoggerDispatcher.info("Debut Pré-actions", logger);

				ArrayList<String> p=new ArrayList<>();

				// récupérer les préactions du jeu de regle
				/**
				 * Attention, on suppose que la preaction ne contient qu'une seule rubrique et en plus celle de la règle
				 */
				for (RegleControleEntity reg : jdr.getListRegleControle()) {
					if (reg.getPreAction()!=null && !StringUtils.isEmpty(reg.getPreAction())) {
						/**
						 * si la rubrique de la preaction n'est pas dans la table, il ne faut rien calculer
						 */
						p.add(ManipString.extractAllRubrique(reg.getPreAction())+" as " + reg.getRubriquePere());
					}
				}

				// appliquer les pré-actions ; modifier la table avec un fast update
				if (!p.isEmpty())
				{
					String pa[]= p.toArray(new String[0]);
					LoggerHelper.debug(logger, "Longueur de mon tableau de préaction :",pa.length);
					LoggerHelper.debug(logger, "Contenu a priori :",p.toString());
					UtilitaireDao.get(0).fastUpdate(connexion
							, table
							, "id"
							, this.listRubTable
							, "true"
							, pa
							);
				}

				StaticLoggerDispatcher.debug("Fin Pré-actions", logger);
	}
	
	
	/**
	 * Execute the control rules.
	 * @param connexion
	 * @param jdr
	 * @param table
	 * @param structure
	 * @throws ArcException
	 */
	private void control(Connection connexion, JeuDeRegle jdr, String table, String structure) throws ArcException {

		StringBuilder blocRequete = new StringBuilder();
		blocRequete.append(this.servSql.initTemporaryTable(table));


		int nbRegles = 0;
		int nbTotalRegles = jdr.getListRegleControle().size();

		for (RegleControleEntity reg : jdr.getListRegleControle()) {
			nbRegles++;
			reg.setTable(table);
			
			StaticLoggerDispatcher.info("n° " + reg.getIdRegle() + " / classe : " + reg.getIdClasse() + " / commentaire : "
					+ reg.getCommentaire(), logger);
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

				if (blocRequete.length()>FormatSQL.TAILLE_MAXIMAL_BLOC_SQL)
				{
					UtilitaireDao.get(0).executeImmediate(connexion, "SET enable_nestloop=off; "+blocRequete.toString()+"SET enable_nestloop=on; ");
					blocRequete.setLength(0);
				}



		}


		StaticLoggerDispatcher.info("Execution de " + nbRegles + "/" + nbTotalRegles, logger);
		blocRequete.append(this.servSql.markTableResultat());
		blocRequete.append(this.servSql.dropControleTemporaryTables());

		UtilitaireDao.get(0).executeImmediate(connexion, "SET enable_nestloop=off; "+blocRequete+"SET enable_nestloop=on; ");
		StaticLoggerDispatcher.info("Fin executeJeuDeRegle", logger);
		
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
	 * @throws ArcException
	 */
	private String executeRegleCondition(JeuDeRegle jdr, RegleControleEntity reg) {
		StaticLoggerDispatcher.info("Je lance executeRegleCondition()",logger);
		String requete = "";

		ArrayList<String> listRubrique = ManipString.extractRubriques(reg.getCondition());
		listRubrique.replaceAll(String::toUpperCase);
		
		if (this.listRubTable.containsAll(listRubrique)) {
			Map<String, RegleControleEntity> mapRubrique = new HashMap<>();
			for (String rub : listRubrique) {
				StaticLoggerDispatcher.debug("Je parcours la liste listRubrique sur l'élément : " + rub, logger);
				RegleControleEntity regle = findType(jdr, rub);
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
	 * @throws ArcException
	 */
	private String executeRegleCardinalite(JeuDeRegle jdr, RegleControleEntity reg) {
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