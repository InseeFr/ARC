package fr.insee.arc.core.service.engine.controle;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dao.RegleControleDao;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.ApiControleService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.EntityDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;



@Component
public class ControleRegleService {

	/** Name of the XSD date format that should be translated in SQL*/
	public static final String XSD_DATE_NAME = "xs:date";
	public static final String XSD_DATETIME_NAME = "xs:dateTime";
	public static final String XSD_TIME_NAME = "xs:time";

	private static final Logger logger = LogManager.getLogger(ControleRegleService.class);

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
		StaticLoggerDispatcher.info("Mon nombre de ligne dans mon fichier : " + tabLines.length,logger);

		String someNames=tabLines[0];
		String someTypes=tabLines[1];
		EntityDao<RegleControleEntity> dao = new RegleControleDao(nomTable, someNames, someTypes, ";");
		for (int i = 2; i < tabLines.length; i++) {
			// ne pas prendre en compte les lignes de commentaires
			if (tabLines[i].startsWith("#")) {
				continue;}
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
	public boolean ajouterRegles(JeuDeRegle jdr, String env, ArrayList<RegleControleEntity> listRegle) throws Exception {
		
		StaticLoggerDispatcher.info("Je lance l'ajout du fichier de règle",logger);
		boolean isAjouter = true;
		boolean isFirstRegle = true;

		// Récupération de la liste des classe de controle
		listClasseCtl = new ArrayList<>();
		listClasseCtl = recupListClasseCtl();
		StaticLoggerDispatcher.info("Nombre de classe de controle : " + listClasseCtl.size(),logger);

		// preparer le debut de la requete SQL
		StringBuilder blocInsert = new StringBuilder();
		int chunk=700;

		try {// pour attraper n'importe quelle erreur
				// Bouclage sur l'ensemble des règles
			
			// on va tester les regles par paquet de 500 (chunk) pour ne pas faire exploser le nombre de colonne de la table test
			int i=0;
			
			for (RegleControleEntity reg : listRegle) {
				
				if ((i%chunk)==0)
				{
					blocInsert.append("TRUNCATE TABLE "+nomTableRegleControle(env, true)+";");
					blocInsert
							.append("INSERT INTO "
									+ nomTableRegleControle(env, true)
									+ " (id_norme,periodicite, validite_inf, validite_sup, version, id_regle, id_classe, rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, commentaire, todo) VALUES ");
					blocInsert.append(System.lineSeparator());
					isFirstRegle = true;
				}
				
				i++;
				
				
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
					throw new Exception(msgErreur);
				}
				
				if (i>0 && (i%chunk==0 || i==listRegle.size()))
				{
					// execution du bloc d'insert
					StaticLoggerDispatcher.info("Mon ordre SQL vaut : " + blocInsert.toString(),logger);
					UtilitaireDao.get("arc").executeBlock(null, blocInsert.append("; COMMIT;"));
					// Execution pour verifier la syntaxe
					executeABlanc(jdr, env, TraitementPhase.CONTROLE.toString());
					UtilitaireDao.get("arc").executeBlock(null, "COMMIT;");
				}
				StaticLoggerDispatcher.info(i+" règles insérées",logger);
			}
			StaticLoggerDispatcher.info("Fin de la vérification des règles!!!",logger);


		} catch (Exception e) {
			StaticLoggerDispatcher.info("L'insertion de règles n'a pas abouti",logger);
			throw e;
		}
		return isAjouter;
	}

	public boolean ajouterReglesValidees(JeuDeRegle jdr, String env, ArrayList<RegleControleEntity> listRegle) throws Exception {
		StaticLoggerDispatcher.info("Je lance l'ajout du fichier de règle",logger);
		boolean isFirstRegle = true;

		// Récupération de la liste des classe de controle
		listClasseCtl = new ArrayList<>();
		listClasseCtl = recupListClasseCtl();
		StaticLoggerDispatcher.info("Nombre de classe de controle : " + listClasseCtl.size(),logger);

		// preparer le debut de la requete SQL
		StringBuilder blocInsert = new StringBuilder();
		blocInsert
				.append("INSERT INTO "
						+ nomTableRegleControle(env, false)
						+ " (id_norme,periodicite, validite_inf, validite_sup, version, id_regle, id_classe, rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, commentaire, todo) VALUES ");
		blocInsert.append(System.lineSeparator());
		isFirstRegle = true;
		
		try {
			
			for (RegleControleEntity reg : listRegle) {
					if (isFirstRegle) {// la première ligne ne commence pas par une virgule
						blocInsert.append(insertRegle(jdr, reg));
						isFirstRegle = false;
					} else {// les suivantes si !
						blocInsert.append("," + System.lineSeparator());
						blocInsert.append(insertRegle(jdr, reg));
					}

			}

			// execution du bloc d'insert
			StaticLoggerDispatcher.info("Mon ordre SQL vaut : " + blocInsert.toString(),logger);
			UtilitaireDao.get("arc").executeBlock(null, blocInsert.append(";"));

		} catch (Exception e) {
			StaticLoggerDispatcher.info("L'insertion de règles n'a pas abouti",logger);
			throw e;
		}
		return true;
	}
	
	
	
	
    /**
     * @param anEnv
     * @param isTest TODO
     * @return
     */
    public static String nomTableRegleControle(String anEnv, boolean isTest) {
    	if (isTest){
    		return ApiService.dbEnv(anEnv)+"test_ihm_" + TraitementTableParametre.CONTROLE_REGLE;
    	}else{
    		return ApiService.dbEnv(anEnv)+"ihm_"+ TraitementTableParametre.CONTROLE_REGLE;
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
	public void executeABlanc(JeuDeRegle jdr, String env, String phase) throws Exception {

	        Connection connexion=UtilitaireDao.get("arc").getDriverConnexion();
	        try {
	        	
	        String emptyTable = ApiService.dbEnv(env) + phase + "_" + TraitementEtat.ENCOURS;

			// récupération de l'ensemble de règle à tester
			StaticLoggerDispatcher.info("récupération de l'ensemble de règle à tester",logger);
			
			String nomTableRCTestComplet = nomTableRegleControle(env, true);
			
			
			ServiceJeuDeRegle sjdrA= new ServiceJeuDeRegle(null);
			sjdrA.fillRegleControle(connexion, jdr, nomTableRCTestComplet);
			
			// Fabrication de la table vide
			StaticLoggerDispatcher.info("Fabrication de la table vide",logger);
			createTableTest(jdr, emptyTable,  nomTableRCTestComplet);
			
			// execution
			StaticLoggerDispatcher.info("execution",logger);
			// bidouille pour pouvoir lancer la méthode execute
			ArrayList<JeuDeRegle> listJdr = new ArrayList<>();
			listJdr.add(jdr);
			
			StaticLoggerDispatcher.info("Execution sur la table vide : " + emptyTable,logger);
			ApiControleService.executeABlanc(connexion, env, phase, emptyTable, sjdrA, listJdr);
	        }
	         finally
			 {
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
	private void createTableTest(JeuDeRegle jdr, String tableDeTest, String tableRegleContrôle) throws SQLException {
		
		// récupérer la liste des rubriques d'une table de règle
		ArrayList<String> listRubrique = new ArrayList<>();
		listRubrique = rubriqueInJeuDeRegle(jdr, tableRegleContrôle);
		
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
//		StaticLoggerDispatcher.info("Ma requete de création de table vide: " + sb.toString(),logger);
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
	private ArrayList<String> rubriqueInJeuDeRegle(JeuDeRegle jdr, String nomtableJeuDeRegle) throws SQLException {
		ArrayList<String> listRubrique = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("WITH ");
		sb.append("prep AS (SELECT 	id_classe, rubrique_pere, rubrique_fils, condition ");
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
		sb.append("		WHERE condition is not null");
		sb.append("           AND id_classe!='REGEXP') ");
		sb.append("	SELECT DISTINCT lower(rubrique) ");
		sb.append("	FROM sel; ");

		ArrayList<ArrayList<String>> res = new ArrayList<>();
		res = UtilitaireDao.get("arc").executeRequest(null, sb.toString());
		for (int i = 0; i < res.size(); i++) {
			if (i == 0 || i == 1) {
				continue;
			}
			listRubrique.add(res.get(i).get(0));
		}
		StaticLoggerDispatcher.info("Ma requête pour récupérer la liste des rubriques: " + sb.toString(),logger);

		return listRubrique;
	}

	/**
	 * Code SQL pour une ligne de VALUES (a,b,...)
	 *
	 * @param jdr
	 * @param reg
	 * @return
	 * @throws SQLException 
	 */
	private String insertRegle(JeuDeRegle jdr, RegleControleEntity reg) throws SQLException {
		StringBuilder requete = new StringBuilder();
		// les données issues du JeuDeRegle
		requete.append("(" + FormatSQL.quoteText(jdr.getIdNorme()));
		requete.append("," + FormatSQL.quoteText(jdr.getPeriodicite()));
		requete.append("," + FormatSQL.quoteText(jdr.getValiditeInfString()));
		requete.append("," + FormatSQL.quoteText(jdr.getValiditeSupString()));
		requete.append("," + FormatSQL.quoteText(jdr.getVersion()));
		// les données issue de la règle
		requete.append("," + FormatSQL.quoteText(reg.getIdRegle()));
		requete.append("," + FormatSQL.quoteText(reg.getIdClasse()));
		requete.append("," + FormatSQL.quoteText(reg.getRubriquePere()));
		requete.append("," + FormatSQL.quoteText(reg.getRubriqueFils()));
		requete.append("," + FormatSQL.quoteText(reg.getBorneInf()));
		requete.append("," + FormatSQL.quoteText(reg.getBorneSup()));
		requete.append("," + FormatSQL.quoteText(reg.getCondition()));
		requete.append("," + FormatSQL.quoteText(reg.getPreAction()));
		requete.append("," + FormatSQL.quoteText(reg.getCommentaire()));
		requete.append(",'1'");
		requete.append(")");
		return requete.toString();
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
			isVerifier = remplissage(reg, reg.getRubriquePere()) && formatDateLegal(reg);
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
			isVerifier = remplissage(reg, reg.getRubriquePere()) && remplissage(reg, reg.getRubriqueFils())
					&& remplissageBorne(reg);
			break;
		case "CONDITION":
			StaticLoggerDispatcher.debug("je vérifie l'exactitude d'une règle de type CONDITION", logger);
			reg.setRubriquePere(null);
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getCondition());
			break;
		case "REGEXP":
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getRubriquePere())
					&& remplissage(reg, reg.getCondition())
					&& isValidRegex(reg);
			break;
		case "ENUM_BRUTE": case "ENUM_TABLE":
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getRubriquePere())
					&& remplissage(reg, reg.getCondition());
			break;
		case "ALIAS":
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getRubriquePere())
					&& remplissage(reg, reg.getCondition());
			break;
		case "ORDRE":
			reg.setRubriqueFils(null);
			reg.setBorneInf(null);
			reg.setBorneSup(null);
			isVerifier = remplissage(reg, reg.getRubriquePere())
					&& remplissage(reg, reg.getCondition())
					&& isValidPosition(reg.getCondition());
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
	public boolean formatDateLegal(RegleControleEntity reg) {
		boolean isLegal = true;
		String format = reg.getCondition();
		if (format.equalsIgnoreCase(XSD_DATE_NAME)
			||	format.equalsIgnoreCase(XSD_DATETIME_NAME)
			||	format.equalsIgnoreCase(XSD_TIME_NAME)	
				) {
			return isLegal;
		}
		try {
			StaticLoggerDispatcher.debug("Le format à tester est : " + format, logger);
			new SimpleDateFormat(format);
			return isLegal;
		} catch (Exception e) {
			msgErreur = "le format de la date semble incorrect. Format : " + format;
			return false;
		}
	}

	/** Checks if the regex is valid.*/
	public boolean isValidRegex(RegleControleEntity reg) {
		String format = reg.getCondition();
		try {
			Pattern.compile(format);
		} catch (PatternSyntaxException e) {
			msgErreur = "le format de l'expresssion régulière semble incorrecte. Format :" + format;
			return false;
		}
		return true;
	}

	/** Check if the position is a valid position .*/
	public boolean isValidPosition(String position) {
		Integer parsedNumber = ManipString.parseNumber(position);
		if (parsedNumber == null || parsedNumber < 0) {
			msgErreur = "la condition renseignée n'est pas une position valide. Condition : " + position;
			return false;
		}
		return true;
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
				StaticLoggerDispatcher.debug("les deux sont null", logger);
				reg.setBorneInf(null);
				reg.setBorneSup(null);
				return true;
			} else if (!StringUtils.isNotBlank(reg.getBorneInf())) {
				StaticLoggerDispatcher.debug("borneInf est null", logger);
				reg.setBorneInf(null);
				j = Integer.parseInt(reg.getBorneSup());
				if (j < 0) {
					msgErreur = "Les bornes doivent être des entiers positifs et croissants";
					return false;
				}
				return true;
			} else if (!StringUtils.isNotBlank(reg.getBorneSup())) {
				StaticLoggerDispatcher.debug("borneSup est null", logger);
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
			StaticLoggerDispatcher.debug("le parseInt a levé une exception", logger);
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
	private ArrayList<String> recupListClasseCtl() throws SQLException {
		ArrayList<String> listClasseCtl = new ArrayList<>();
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		res = UtilitaireDao.get("arc").executeRequest(null, "SELECT id FROM arc.ext_type_controle;");
		for (int i = 0; i < res.size(); i++) {
			if (i == 0 || i == 1) {
				continue;
			}
			listClasseCtl.add(res.get(i).get(0));
		}
		return listClasseCtl;
	}


}
