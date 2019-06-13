package fr.insee.arc_essnet.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.dao.RegleControleDao;
import fr.insee.arc_essnet.core.model.RegleControleEntity;
import fr.insee.arc_essnet.core.model.RuleSets;
import fr.insee.arc_essnet.core.model.TraitementState;
import fr.insee.arc_essnet.core.model.TraitementTableParametre;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.utils.dao.EntityDao;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.arc_essnet.utils.utils.ManipString;
import fr.insee.arc_essnet.utils.utils.SQLExecutor;

@Component
public class ControleRegleService {

	@Autowired
	private ApiControleService lanc;

	private static final Logger LOGGER = Logger.getLogger(ControleRegleService.class);

	/**
	 * Liste des modalités pour la classe de controle
	 */
	private ArrayList<String> listClasseCtl;
	/**
	 * raison du refus de l'insertion du fichier de règle
	 */
	private String msgErreur;

	/**
	 * Passage d'un fichier csv à une liste de règle de controle
	 * @param nomTable
	 * @param fichierRegle
	 *
	 * @return
	 */
	public ArrayList<RegleControleEntity> miseEnRegleC(String nomTable, String fichierRegle) {
		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();
//		String lineSeparator = System.getProperty("line.separator");
		String lineSeparator = "\n";
		String[] tabLines = fichierRegle.split(lineSeparator);
		LoggerDispatcher.info("Mon nombre de ligne dans mon fichier : " + tabLines.length,LOGGER);

		String someNames=tabLines[0];
		String someTypes=tabLines[1];
		EntityDao<RegleControleEntity> dao = new RegleControleDao(nomTable, someNames, someTypes, ";");
		for (int i = 2; i < tabLines.length; i++) {
			// ne pas prendre en compte les lignes de commentaires
			if (tabLines[i].startsWith("#")) {
				continue;}
//			// Completion de la ligne
//			tabLines[i] = tabLines[i] + ";;;;;;;;;";
//			String[] elementRegle = tabLines[i].split(";", -1);
//			// Mise en bean
//			RegleControleEntity reg = new RegleControleEntity();
//			reg.setIdRegle(elementRegle[0]);
//			reg.setIdClasse(elementRegle[1]);
//			reg.setRubriquePere(elementRegle[2]);
//			reg.setRubriqueFils(elementRegle[3]);
//			reg.setBorneInf(elementRegle[4]);
//			reg.setBorneSup(elementRegle[5]);
//			reg.setCondition(elementRegle[6]);
//			reg.setPreAction(elementRegle[7]);
//			reg.setCommentaire(elementRegle[8]);
			RegleControleEntity reg = dao.get(tabLines[i]);
			listRegle.add(reg);
		}

		return listRegle;
	}

	/**
	 * Ajout de règle de contrôle dans la table adéquate
	 *
	 * @param jdr
	 *            , jeu de règle auquel rattaché les règles insérées
	 * @param env
	 *            , préfixe du nom de la table
	 * @param listRegle
	 *            , liste des règles à insérer
	 * @return
	 * @throws Exception
	 */
	public boolean ajouterRegles(RuleSets jdr, String env, ArrayList<RegleControleEntity> listRegle) throws Exception {
		
		LoggerDispatcher.info("Je lance l'ajout du fichier de règle",LOGGER);
		boolean isAjouter = true;
		boolean isFirstRegle = true;

		// Récupération de la liste des classe de controle
		listClasseCtl = new ArrayList<>();
		listClasseCtl = recupListClasseCtl();
		LoggerDispatcher.info("Nombre de classe de controle : " + listClasseCtl.size(),LOGGER);

		// preparer le debut de la requete SQL
		StringBuilder blocInsert = new StringBuilder();
		blocInsert
				.append("INSERT INTO "
						+ nomTableRegleControle(env, true)
						+ " (id_norme,periodicite, validite_inf, validite_sup, version, id_regle, id_classe, rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, commentaire, todo) VALUES ");
		blocInsert.append(System.lineSeparator());

		try {// pour attraper n'importe quelle erreur
				// Bouclage sur l'ensemble des règles
			for (RegleControleEntity reg : listRegle) {
				// controle logique
				isAjouter = verificationRegle(reg);
				
				// Ecriture de l'ordre SQL
				if (isAjouter) {
					if (isFirstRegle) {// la première ligne ne commence pas par une virgule
						blocInsert.append(insertRegle(jdr, reg));
						isFirstRegle = false;
					} else {// les suivantes si !
						blocInsert.append("," + System.lineSeparator());
						blocInsert.append(insertRegle(jdr, reg));
					}
				} else {
					Exception e = new Exception(msgErreur);
					throw e;
				}

			}
			LoggerDispatcher.info("Fin de la vérification des règles!!!",LOGGER);
			
			
			// execution du bloc d'insert
			LoggerDispatcher.info("Mon ordre SQL vaut : " + blocInsert.toString(),LOGGER);
			UtilitaireDao.get("arc").executeBlock(null, blocInsert.append(";"));
			// Execution pour verifier la syntaxe
			executeABlanc(jdr, env, TypeTraitementPhase.CONTROL.toString());

			// Si on arrive jusqu'ici c'est que tout est OK
			// je remets le marqueur todo à null
			// UtilitaireDao.get("arc").executeBlock(null, "UPDATE arc.ihm_controle_regle SET todo=null");

		} catch (Exception e) {
			LoggerDispatcher.info("L'insertion de règles n'a pas abouti",LOGGER);
			// deleteTodo(jdr,"arc.ihm_controle_regle");
			throw e;
		}
		return isAjouter;
	}

	public boolean ajouterReglesValidees(RuleSets jdr, String env, ArrayList<RegleControleEntity> listRegle) throws Exception {
		LoggerDispatcher.info("Je lance l'ajout du fichier de règle",LOGGER);
		boolean isAjouter = true;
		boolean isFirstRegle = true;

		// Récupération de la liste des classe de controle
		listClasseCtl = new ArrayList<>();
		listClasseCtl = recupListClasseCtl();
		LoggerDispatcher.info("Nombre de classe de controle : " + listClasseCtl.size(),LOGGER);

		// preparer le debut de la requete SQL
		StringBuilder blocInsert = new StringBuilder();
		blocInsert
				.append("INSERT INTO "
						+ nomTableRegleControle(env, false)
						+ " (id_norme,periodicite, validite_inf, validite_sup, version, id_regle, id_classe, rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, commentaire, todo) VALUES ");
		blocInsert.append(System.lineSeparator());

		try {// pour attraper n'importe quelle erreur
				// Bouclage sur l'ensemble des règles
			for (RegleControleEntity reg : listRegle) {
				// controle logique
				isAjouter = verificationRegle(reg);
				
				// Ecriture de l'ordre SQL
				if (isAjouter) {
					if (isFirstRegle) {// la première ligne ne commence pas par une virgule
						blocInsert.append(insertRegle(jdr, reg));
						isFirstRegle = false;
					} else {// les suivantes si !
						blocInsert.append("," + System.lineSeparator());
						blocInsert.append(insertRegle(jdr, reg));
					}
				} else {
					Exception e = new Exception(msgErreur);
					throw e;
				}

			}
			LoggerDispatcher.info("Fin de la vérification des règles!!!!",LOGGER);
			
			
			// execution du bloc d'insert
			LoggerDispatcher.info("Mon ordre SQL vaut : " + blocInsert.toString(),LOGGER);
			UtilitaireDao.get("arc").executeBlock(null, blocInsert.append(";"));
			// Execution pour verifier la syntaxe
			executeABlanc(jdr, env, TypeTraitementPhase.CONTROL.toString());

			// Si on arrive jusqu'ici c'est que tout est OK
			// je remets le marqueur todo à null
			// UtilitaireDao.get("arc").executeBlock(null, "UPDATE arc.ihm_controle_regle SET todo=null");

		} catch (Exception e) {
			LoggerDispatcher.info("L'insertion de règles n'a pas abouti",LOGGER);
			// deleteTodo(jdr,"arc.ihm_controle_regle");
			throw e;
		}
		return isAjouter;
	}
	
	
	
	
    /**
     * @param anEnv
     * @param isTest TODO
     * @return
     */
    public static String nomTableRegleControle(String anEnv, boolean isTest) {
    	if (isTest){
    		return AbstractPhaseService.dbEnv(anEnv)+"test_ihm_" + TraitementTableParametre.CONTROLE_REGLE;
    	}else{
    		return AbstractPhaseService.dbEnv(anEnv)+"ihm_"+ TraitementTableParametre.CONTROLE_REGLE;
    	}
    }
    
    
    

	/**
	 * Execution du jeu de regle sur une table vide pour vérifier que la syntaxe SQL est correcte En cas de problème cela remonte une
	 * exception
	 *
	 * @param jdr
	 * @param env
	 *            , environnement ou encore prefixe du nom de la table de controle
	 * @param phase
	 *            , sert à nommer la table vide (et ne pas être en concurrence avec d'autre service)
	 * @throws SQLException
	 */
    public void executeABlanc(RuleSets jdr, String env, String phase) throws Exception {

	Connection connexion = UtilitaireDao.get("arc").getDriverConnexion();
	try {

	    String emptyTable = AbstractPhaseService.dbEnv(env) + phase + "_" + TraitementState.ENCOURS;

	    // récupération de l'ensemble de règle à tester
	    LoggerDispatcher.info("récupération de l'ensemble de règle à tester", LOGGER);

	    String nomTableRCTestComplet = nomTableRegleControle(env, true);

	    lanc.sjdr.fillRegleControle(connexion, jdr, nomTableRCTestComplet);

	    // Fabrication de la table vide
	    LoggerDispatcher.info("Fabrication de la table vide", LOGGER);
	    createTableTest(jdr, emptyTable, nomTableRCTestComplet);

	    // execution
	    LoggerDispatcher.info("execution", LOGGER);
	    // bidouille pour pouvoir lancer la méthode execute
	    ArrayList<RuleSets> listJdr = new ArrayList<>();
	    listJdr.add(jdr);
	    lanc.setListJdr(listJdr);
	    LoggerDispatcher.info("Execution sur la table vide : " + emptyTable, LOGGER);
	    lanc.execute(connexion, env, phase, emptyTable);
	} finally {
	    connexion.close();
	}

    }

	/**
	 * Création d'une table vide avec les bonnes colonnes pour tester une execution à blanc
	 * Attention avec la logique suivante, si la rubrique commnence par un I elle est de type integer, sinon elle est de type text
	 *
	 * @param jdr
	 * @param tableDeTest: table qui servira à réaliser les tests
	 * @param tableRegleContrôle: correspond à la table arc.test_ihm_regle_contrôle qui est une copie de la table arc.ihm_regle_contrôle. 
	 * @throws SQLException
	 */
	private void createTableTest(RuleSets jdr, String tableDeTest, String tableRegleContrôle) throws SQLException {
		
		// récupérer la liste des rubriques d'une table de règle
		ArrayList<String> listRubrique = rubriqueInJeuDeRegle(jdr, tableRegleContrôle);
		
		// fabrication concrète de la table
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE IF EXISTS " + tableDeTest + ";");
		sb.append("CREATE  TABLE " + tableDeTest);
		sb.append("(id_norme text collate \"C\", periodicite text collate \"C\", id_source text collate \"C\", validite text collate \"C\", id integer, controle text collate \"C\", brokenrules text[] collate \"C\" ");
		
		// rubrique de base à ne pas ajouter
		ArrayList<String> basicRubrique = new ArrayList<>();
		basicRubrique.add("id_norme");
		basicRubrique.add("periodicite");
		basicRubrique.add("id_source");
		basicRubrique.add("validite");
		basicRubrique.add("id");
		basicRubrique.add("controle");
		basicRubrique.add("brokenrules");

		
		for (String rub : listRubrique) {
			String r=rub.toLowerCase();
			if (!basicRubrique.contains(r))
			{
				if(r.startsWith("i_")){
					sb.append("," + r + " integer");
				}else{
					if (!listRubrique.contains("i_" + ManipString.substringAfterFirst(r , "_")))
					{
						sb.append(", i_" + ManipString.substringAfterFirst(r , "_") + " integer ");
					}
					sb.append("," + r + " text collate \"C\"");
				}
			}
		}
		sb.append(") with (autovacuum_enabled = false, toast.autovacuum_enabled = false);");
//		LoggerDispatcher.info("Ma requete de création de table vide: " + sb.toString(),logger);
		UtilitaireDao.get("arc").executeBlock(null, sb.toString());
	}

	/**
	 * Récupération de la liste des variables d'un jeu de regle TODO un autre DAO serait peut être plus pertinent
	 * Attention la liste en sortie ne doit pas avoir de doublon (d'où le select disctinct sur lower)
	 * @param jdr
	 * @param nomtableJeuDeRegle : nom de la table contenant les jeux de règles
	 * @return
	 * @throws SQLException
	 */
	    @SQLExecutor
	private ArrayList<String> rubriqueInJeuDeRegle(RuleSets jdr, String nomtableJeuDeRegle) throws SQLException {
		ArrayList<String> listRubrique = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("WITH ");
		sb.append("prep AS (SELECT 	rubrique_pere, rubrique_fils, condition ");
//		sb.append("			FROM " + ApiService.dbEnv(env) + TraitementTableParametre.CONTROLE_REGLE + " ");
		sb.append("			FROM " +nomtableJeuDeRegle+ " ");
		sb.append("			WHERE id_norme='" + jdr.getIdNorme() + "'::text ");
		sb.append("				AND periodicite='" + jdr.getPeriodicite() + "'::text ");
		sb.append("				AND validite_inf='" + jdr.getValiditeInfString() + "'::date ");
		sb.append("				AND validite_sup='" + jdr.getValiditeSupString() + "'::date ");
		sb.append("				AND version='" + jdr.getVersion() + "'::text ), ");
		sb.append("sel AS ( ");
		sb.append("		SELECT rubrique_pere as rubrique ");
		sb.append("		FROM prep ");
		sb.append("		WHERE rubrique_pere is not null ");
		sb.append("			UNION ");
		sb.append("		SELECT rubrique_fils as rubrique ");
		sb.append("		FROM prep ");
		sb.append("		WHERE rubrique_fils is not null ");
		sb.append("			UNION ");
		sb.append("		SELECT regexp_replace(array_to_string(regexp_matches(condition, '{\\w+}','g'),';'),'[{}]','','g') as rubrique ");
		sb.append("		FROM prep ");
		sb.append("		WHERE condition is not null) ");
		sb.append("	SELECT DISTINCT lower(rubrique) ");
		sb.append("	FROM sel; ");

		ArrayList<ArrayList<String>> res = UtilitaireDao.get("arc").executeRequest(null, sb.toString());
		for (int i = 0; i < res.size(); i++) {
			if (i == 0 || i == 1)
				continue;
			listRubrique.add(res.get(i).get(0));
		}
		LoggerDispatcher.info("The returned columns " + listRubrique,LOGGER);

		return listRubrique;
	}

	/**
	 * Code SQL pour une ligne de VALUES (a,b,...)
	 *
	 * @param jdr
	 * @param reg
	 * @return
	 */
	private String insertRegle(RuleSets jdr, RegleControleEntity reg) {
		StringBuilder requete = new StringBuilder();
		// les données issues du JeuDeRegle
		requete.append("(" + preparaStringSql(jdr.getIdNorme()));
		requete.append("," + preparaStringSql(jdr.getPeriodicite()));
		requete.append("," + preparaStringSql(jdr.getValiditeInfString()));
		requete.append("," + preparaStringSql(jdr.getValiditeSupString()));
		requete.append("," + preparaStringSql(jdr.getVersion()));
		// les données issue de la règle
		requete.append("," + preparaStringSql(reg.getIdRegle()));
		requete.append("," + preparaStringSql(reg.getIdClasse()));
		requete.append("," + preparaStringSql(reg.getRubriquePere()));
		requete.append("," + preparaStringSql(reg.getRubriqueFils()));
		requete.append("," + preparaStringSql(reg.getBorneInf()));
		requete.append("," + preparaStringSql(reg.getBorneSup()));
		requete.append("," + preparaStringSql(reg.getCondition()));
		requete.append("," + preparaStringSql(reg.getPreAction()));
		requete.append("," + preparaStringSql(reg.getCommentaire()));
		requete.append(",'1'");
		requete.append(")");
		return requete.toString();
	}

	/**
	 * Méthode pour mettre entre cote et doubler les cotes internes Cela fonctionne même pour id_regle qui est un integer, car postgresql
	 * semble faire un cast implicite
	 *
	 * @param champ
	 * @return
	 */
	private String preparaStringSql(String champ) {
		String s;
		if (StringUtils.isNotBlank(champ)) {
			s = "'" + champ.replace("'", "''") + "'";
		} else {
			s = null;
		}
		;
		return s;
	}

	/**
	 * Méthode pour vérifier que les champs d'une règle de controle sont correctement renseignés (or syntaxe SQL) La méthode en profite pour
	 * remettre à null les champs qui n'ont pas à être renseignés
	 *
	 * @param reg
	 * @return
	 */
	private boolean verificationRegle(RegleControleEntity reg) {
		boolean isVerifier = true;
		// Verification que la classe de controle fait bien parti des possible
		if (!listClasseCtl.contains(reg.getIdClasse())) {
			msgErreur = "Cette classe de controle n'existe pas : " + reg.getIdClasse();
			return false;
		}
		// En fonction de la classe de controle, les champs sont-il correctement rempli (hors syntaxe SQL)
		switch (reg.getIdClasse()) {
		case "NUM":
			reg.setCondition(null);
			reg.setRubriqueFils(null);
			isVerifier = remplissage(reg, reg.getRubriquePere()) && remplissageBorne(reg);
			break;
		case "DATE":
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getRubriquePere()) && formatLegal(reg);
			break;
		case "ALPHANUM":
			reg.setCondition(null);
			reg.setRubriqueFils(null);
			isVerifier = remplissage(reg, reg.getRubriquePere()) && remplissageBorne(reg);
			break;
		case "CARDINALITE":
			// pour bétoner le code
			if (StringUtils.isBlank(reg.getBorneInf())) {
				reg.setBorneInf("0");
			}
			reg.setCondition(null);
			isVerifier = remplissage(reg, reg.getRubriquePere()) && remplissage(reg, reg.getRubriqueFils())
					&& remplissageBorne(reg);
			break;
		case "CONDITION":
			LoggerDispatcher.debug("je vérifie l'exactitude d'une règle de type CONDITION", LOGGER);
			reg.setRubriquePere(null);
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getCondition());
			break;
		default:
			break;
		}
		return isVerifier;
	}

	/**
	 * Méthode pour vérifier qu'un champ n'est pas vide ou blanc
	 *
	 * @param reg
	 *            , la règle afin d'en extraire le numéro de règle
	 * @param rubrique
	 *            , le contenu du champ
	 * @return
	 */
	private boolean remplissage(RegleControleEntity reg, String rubrique) {
		boolean isValide = true;
		if (!StringUtils.isNotBlank(rubrique)) {
			isValide = false;
			msgErreur = "Un champ obligatoire n'est pas rempli pour la règle : " + reg.getIdRegle();
		}
		return isValide;
	}

	/**
	 * Test java pour savoir si le format de date est un format accepté
	 *
	 * @return
	 */
	private boolean formatLegal(RegleControleEntity reg) {
		boolean isLegal = true;
		String format = reg.getCondition();
		try {
			LoggerDispatcher.debug("Le format à tester est : " + format, LOGGER);
			new SimpleDateFormat(format);
			return isLegal;
		} catch (Exception e) {
			msgErreur = "le format de la date semble incorrect. Format : " + format;
			return false;
		}

	}

	/**
	 * Pour vérifier que les champs de bornes sont bien remplis (numérique croissant)
	 *
	 * @param reg
	 * @return
	 */
	private boolean remplissageBorne(RegleControleEntity reg) {
		boolean isValide = true;
		int i, j;

		try {
			if (!StringUtils.isNotBlank(reg.getBorneInf()) && !StringUtils.isNotBlank(reg.getBorneSup())) {
				LoggerDispatcher.debug("les deux sont null", LOGGER);
				reg.setBorneInf(null);
				reg.setBorneSup(null);
				return true;
			} else if (!StringUtils.isNotBlank(reg.getBorneInf())) {
				LoggerDispatcher.debug("borneInf est null", LOGGER);
				reg.setBorneInf(null);
				j = Integer.parseInt(reg.getBorneSup());
				if (j < 0) {
					msgErreur = "Les bornes doivent être des entiers positifs et croissants";
					return false;
				}
				return true;
			} else if (!StringUtils.isNotBlank(reg.getBorneSup())) {
				LoggerDispatcher.debug("borneSup est null", LOGGER);
				reg.setBorneSup(null);
				i = Integer.parseInt(reg.getBorneInf());
				if (i < 0) {
					msgErreur = "Les bornes doivent être des entiers positifs et croissants";
					return false;
				}
				return true;
			} else {
				i = Integer.parseInt(reg.getBorneInf());
				j = Integer.parseInt(reg.getBorneSup());
				if (i > j || i < 0 || j < 0) {
					msgErreur = "Les bornes doivent être des entiers positifs et croissants";
					return false;
				}
			}
			return isValide;
		} catch (Exception e) {
			LoggerDispatcher.debug("le parseInt a levé une exception", LOGGER);
			msgErreur = "Les bornes doivent être des entiers positifs et croissants";
			return false;
		}
	}

	/**
	 * Méthode pour récupérer la liste des classes de controle TODO le nom de la table est en dur un autre DAO serait peut être plus
	 * pertinent
	 *
	 * @return
	 * @throws SQLException
	 */
	    @SQLExecutor
	private ArrayList<String> recupListClasseCtl() throws SQLException {
		ArrayList<String> listClasseCtl = new ArrayList<>();
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		res = UtilitaireDao.get("arc").executeRequest(null, "SELECT id FROM arc.ext_type_controle;");
		for (int i = 0; i < res.size(); i++) {
			if (i == 0 || i == 1)
				continue;
			listClasseCtl.add(res.get(i).get(0));
		}
		return listClasseCtl;
	}


	public AbstractPhaseService getLanc() {
		return lanc;
	}

	public void setLanc(ApiControleService lanc) {
		this.lanc = lanc;
	}
	


}
