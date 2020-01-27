package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.module_dao.SctructurizeRuleDAO;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.rules.RuleStructurizeEntity;
import fr.insee.arc.core.service.ApiNormageService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;

/**
 * ThreadNormageService
 *
 * 1- créer la table des données à traiter dans le module</br>
 * 2- calcul de la norme, validité, periodicité sur chaque ligne de la table de
 * donnée</br>
 * 3- déterminer pour chaque fichier si le normage s'est bien déroulé et marquer
 * sa norme, sa validité et sa périodicité</br>
 * 4- créer les tables OK et KO; marquer les info de normage(norme, validité,
 * périodicité) sur chaque ligne de donnée</br>
 * 5- transformation de table de donnée; mise à plat du fichier; suppression et
 * relation</br>
 * 6- mettre à jour le nombre d'enregistrement par fichier après sa
 * transformation</br>
 * 7- sortir les données du module vers l'application</br>
 *
 * @author Manuel SOULIER
 *
 */
public class ThreadStructurizeService extends AbstractThreadService implements IRulesUserService {

    public static final String[] NEEDED_VARIABLES = new String[] { "id_source", "id","date_integration", "id_norme","validite",  "periodicite"  };
    
    private static final Logger LOGGER = Logger.getLogger(ThreadStructurizeService.class);

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private String columnToBeAdded = "";

    private String jointure;

    private String norme;
    private Date validite;
    private String periodicite;
    private String validiteText;

    public ThreadStructurizeService(int currentIndice, ApiNormageService aApi, Connection connexion) {
	super(currentIndice, aApi, connexion);

	this.jointure = aApi.getFilesToProcess().get(currentIndice).getJointure();
	this.norme = aApi.getFilesToProcess().get(currentIndice).getIdNorme();
	this.validiteText = aApi.getFilesToProcess().get(currentIndice).getValidite();
	try {
	    this.validite = this.formatter.parse(validiteText);
	} catch (ParseException e) {
	    LoggerDispatcher.error("Error in validite date forlat", LOGGER);
	}
	this.periodicite = aApi.getFilesToProcess().get(currentIndice).getPeriodicite();
    }

    @Override
    public void initialisationTodo() throws Exception {
	// Clean the connection
	UtilitaireDao.get("arc").executeImmediate(this.connection, "DISCARD TEMP;");

	// Create the working table
	createWorkingTables();

    }

    @Override
    public void process() throws Exception {

	joinXMLBlocks();

    }

    @Override
    public void finalizePhase() throws Exception {
	finalInsertion();

    }

    /**
     * Create the thread working tables. The tables create are
     * <ul>
     * <li>a pilotage table</li>
     * <li>a table with the data</li>
     * </ul>
     * 
     * @throws SQLException
     */
    private void createWorkingTables() throws SQLException {
	LoggerDispatcher.info("createWorkingTable", LOGGER);
	StringBuilder request = new StringBuilder();

	// create thread's pilotage table
	request.append(getRequestTocreateTablePilotageIdSource(this.getTablePilTemp(), this.getTablePilTempThread(),
		this.idSource));
	request.append(getRequestToCreateWorkingTable(this.getTablePrevious(),
		this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA), this.idSource));

	LoggerDispatcher.debug(String.format("Thread's pilotage table : %s", this.getTablePilTempThread()), LOGGER);

	// Tag the structurize phase as OK
	request.append("\n UPDATE " + this.getTablePilTempThread());
	request.append("\n	SET etat_traitement = '{" + TraitementState.OK + "}'");
	request.append("\n	, phase_traitement = '" + this.tokenInputPhaseName + "'");
	request.append("\n	WHERE id_source='" + this.idSource + "';");

	LoggerDispatcher
		.debug(String.format("Thread's %s : %s", this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_KO),
			this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_KO)), LOGGER);
	request.append(this.getRequestToCreateWorkingTableWithState("",
		this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA),
		this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_KO), this.getTablePilTempThread(),
		TraitementState.KO.toString()));

	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.getConnection(), request);

    }

    /**
     * 
     * Perform the join between XML blocks to tabulate the file. It modify the join
     * request create in the LOAD phase.
     * <ol>
     * <li>Delete blocks from delete rules</li>
     * <li>Add relationnal join condition between 2 elements from relationnal
     * rules</li>
     * </ol>
     * 
     * 
     * Fait sur une maintenance urgente après réception des fichiers lot2 en moins
     * de 2j ... La méthode devrait etre refactor (pour séparer "deletion" et
     * "relation") La réécriture de la requete selon les règles utilisateurs devrait
     * être moins adhérente à la structure de la requete issu du chargement (trop
     * dépendant des mot clés ou saut de ligne pour l'instant)
     * 
     * @throws Exception
     *
     */
    private void joinXMLBlocks() throws Exception {

	LoggerDispatcher.info("jointureBlocXML()", LOGGER);

	StringBuilder request = new StringBuilder();

	LoggerDispatcher.info("Get file's rules", LOGGER);

	SctructurizeRuleDAO structurizeRuleDao = new SctructurizeRuleDAO(this.queyHandler,
		this.bddTable.getContextName(BddTable.ID_TABLE_NORMAGE_REGLE));

	List<RuleStructurizeEntity> listRuleStructurize = structurizeRuleDao.getListRules(this.norme);

	LoggerDispatcher.info(String.format("There is %s rules to process", listRuleStructurize.size()), LOGGER);

	Map<String, ArrayList<String>> regle = getBean(getRegles(this.idSource,
		this.bddTable.getQualifedName(BddTable.ID_TABLE_NORMAGE_REGLE), this.getTablePilTempThread()));

	Map<String, ArrayList<String>> columnUsedInRules = getAllUsedColumnInFullProcess();

	if (StringUtils.isEmpty(jointure)) {

	    deleteAllUnnecessaryVariables(request, columnUsedInRules);

	} else {
	    jointure = jointure.toLowerCase();

	    if (Format.size(regle.get("id_regle"))>0)
	    {
	    // ORDRE IMPORTANT
	    // on supprime les rubriques inutilisées quand le service est invoqué en batch
	    // (this.paramBatch!=null)
	    // i.e. en ihm (this.paramBatch==null) on garde toutes les rubriques
	    // pour que les gens qui testent en bac à sable n'aient pas de probleme
	    if (this.paramBatch != null) {
		ajouterRegleSuppression(listRuleStructurize, norme, periodicite, jointure, columnUsedInRules);

		jointure = appliquerRegleSuppression(regle, norme, validite, periodicite, jointure);
	    }

	    ajouterRegleDuplication(regle, norme, validite, periodicite, jointure);

	    jointure = appliquerRegleDuplication(regle, norme, validite, periodicite, jointure);

	    ajouterRegleIndependance(regle, norme, validite, periodicite, jointure);

	    jointure = appliquerRegleIndependance(regle, norme, validite, periodicite, jointure);

	    jointure = appliquerRegleUnicite(regle, norme, validite, periodicite, jointure);

	    jointure = appliquerRegleRelation(regle, norme, validite, periodicite, jointure);
	    
	    }
	    
	    // optimisation manu 9.6
	    // retravaille de la requete pour éliminer UNION ALL
	    jointure = optimisation96(jointure);

	    // on ne fait le remplacement que maintenant (passage en minuscule avant pour le
	    // parsing)
	    // jointure="DISCARD TEMP;\n"+jointure;
	    jointure = "set enable_nestloop=off;\n" + jointure;
	    jointure = jointure.replace("{table_source}", this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA));
	    jointure = jointure.replace("{table_destination}",
		    this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_OK));
	    jointure = jointure.replace("{id_norme}", norme);
	    jointure = jointure.replace("{validite}", validiteText);
	    jointure = jointure.replace("{periodicite}", periodicite);
	    jointure = jointure.replace("{nom_fichier}", idSource);
	    jointure = jointure + "set enable_nestloop=on;\n ";
	    request.append(jointure);

	}

	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.getConnection(), request);
	request.setLength(0);

    }

    /**
     * Generate an CREATE TABLE AS SELECT sql request without the unnecessary
     * variables of the input table
     * 
     * @param request
     * @param columnUsedInRules
     */
    private void deleteAllUnnecessaryVariables(StringBuilder request,
	    Map<String, ArrayList<String>> columnUsedInRules) {
	List<String> listeRubriqueSource = new ArrayList<>();

	// Get all column name of ID_TABLE_POOL_DATA
	UtilitaireDao.get(DbConstant.POOL_NAME).getColumns(this.connection, listeRubriqueSource,
		this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA));

	Set<String> alreadyAdded = new LinkedHashSet<>();
	alreadyAdded.addAll(Arrays.asList(NEEDED_VARIABLES));
	
	for (String variable : listeRubriqueSource) {
	    /**
	     * Keeping variables if interactive mode (this.paramBatch == null), or variable
	     * used later
	     */
	    if (this.paramBatch == null || columnUsedInRules.get("var").contains(variable)) {
		// Check if the variable exist in input table
		if (listeRubriqueSource.contains("i_" + variable.substring(2))) {
		    alreadyAdded.add("i_" + variable.substring(2));
		}

		if (listeRubriqueSource.contains("v_" + variable.substring(2))) {
		    alreadyAdded.add("v_" + variable.substring(2));
		}

	    }
	}

	request.append("\n CREATE TABLE " + this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_OK) + " ");
	request.append("\n  AS SELECT ");
	request.append(String.join(",", alreadyAdded));
	request.append(" FROM " + this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA) + " ;");
    }
    @SQLExecutor
    private Map<String, ArrayList<String>> getAllUsedColumnInFullProcess() throws SQLException {
	LoggerDispatcher.info("get all columns used in the arc process for that file", LOGGER);
	String tableTmpRubriqueDansregles = "TMP_RUBRIQUE_DANS_REGLES";
	UtilitaireDao.get("arc").executeRequest(this.connection,
		"\n DROP TABLE IF EXISTS " + tableTmpRubriqueDansregles + "; " + "\n CREATE TEMPORARY TABLE "
			+ tableTmpRubriqueDansregles + " AS "
			+ getAllRubriquesInRegles(this.idSource, this.getTablePilTempThread()//
				, this.bddTable.getQualifedName(BddTable.ID_TABLE_NORMAGE_REGLE)//
				, this.bddTable.getQualifedName(BddTable.ID_TABLE_CONTROLE_REGLE)//
				, this.bddTable.getQualifedName(BddTable.ID_TABLE_FILTRAGE_REGLE)//
				, this.bddTable.getQualifedName(BddTable.ID_TABLE_MAPPING_REGLE)));

	Map<String, ArrayList<String>> rubriqueUtiliseeDansRegles = getBean(
		getRegles(this.idSource, tableTmpRubriqueDansregles, this.getTablePilTempThread()));
	return rubriqueUtiliseeDansRegles;
    }

    /**
     * Remplace les UNION ALL par des inserts
     * 
     * @param jointure
     * @return
     */
    private String optimisation96(String jointure) {
	LoggerDispatcher.info("optimisation96()", LOGGER);

	// on enleve l'id
	String r = jointure;
	r = " \n " + r;

	boolean blocInsert = false;

	String[] lines = r.split("\n");
	String insert = null;

	r = "";
	for (int i = 0; i < lines.length; i++) {
	    // on garde l'insert
	    if (lines[i].contains("insert into {table_destination}")) {
		insert = ";\n" + lines[i];
		if (!lines[i].contains(")")) {
		    insert = insert + ")";
		}
	    }

	    if (insert != null && lines[i].contains("UNION ALL")) {
		lines[i] = insert;
	    }

	    if (blocInsert) {
		lines[i] = lines[i].replace(" select ",
			" select row_number() over () + (select count(*) from {table_destination}),");
	    }

	    if (lines[i].contains("row_number() over (), ww.*")) {
		lines[i] = "";
		blocInsert = true;

	    }

	    // on retire l'alias sur les deniere lignes
	    if (i == lines.length - 1 || i == lines.length - 2) {
		lines[i] = lines[i].replace(") ww", ";");
	    }

	    r = r + lines[i] + "\n";

	}

	// r=r+"INSERT INTO
	// "+this.this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_OK)+" SELECT
	// "+fieldsToBeInserted+"
	// FROM {table_destination}; \n ";

	// bug mémoire : analyze sur les tables générées;
	StringBuilder analyze = new StringBuilder();

	Pattern p = Pattern.compile("create temporary table ([^ ]+) as ");
	Matcher m = p.matcher(jointure);
	while (m.find()) {
	    analyze.append("\n analyze " + m.group(1) + ";");
	}

	// on intègre le calcul des statistiques des tables utilisées
	r = ManipString.substringBeforeFirst(r, "insert into {table_destination}") + analyze
		+ "\n insert into {table_destination}"
		+ ManipString.substringAfterFirst(r, "insert into {table_destination}");

	// on crée la table destination avec les bonnes colonnes
    // on crée la table destination avec les bonnes colonnes		    
	r="DROP TABLE IF EXISTS {table_destination}; CREATE TEMPORARY TABLE {table_destination} as SELECT * FROM {table_source} where false; \n "
	   + this.columnToBeAdded+"\n"
	   + r;
	return r;
    }

    /**
     * Ajouter les règles de duplication si dans une relation, je trouve un "." dans
     * une rubrique de nomenclature, j'ordonne la duplication et je rectifie la
     * regle de relation
     * 
     * @param regle
     * @param norme
     * @param validite
     * @param periodicite
     * @param jointure
     * @throws Exception
     */
    private void ajouterRegleDuplication(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {
	LoggerDispatcher.info("ajouterRegleDuplication()", LOGGER);

	// pour toutes les règles de relation,
	for (int j = 0; j < regle.get("id_regle").size(); j++) {
	    String type = regle.get("id_classe").get(j);

	    // en gros on parcours les regles de relation
	    // on va exclure les bloc en relation des blocs à calculer comme indépendants

	    if (type.equals("relation")) {
		String rubrique = regle.get("rubrique").get(j).toLowerCase();
		String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();

		String rub = ManipString.substringAfterFirst(rubriqueNmcl, ".");
		String alias = ManipString.substringBeforeFirst(rubriqueNmcl, ".");

		// la duplication ne peut se faire que :
		// 1- sur une rubrique de nomenclature
		// 2- si les rubriques existent dans la requete initiale
		// 3- l'alias = la chaine avant le point de la rubrique nomenclature
		if (rubriqueNmcl.contains(".") && jointure.contains(" " + rubrique + " ")
			&& jointure.contains(" " + rub + " ")) {

		    // modifier la regle de rubrique_nmcl : alias.rubrique devient rubrique_alias
		    regle.get("rubrique_nmcl").set(j, rub + "_" + alias);

		    // ajout de la règle
		    regle.get("id_regle").add("G" + System.currentTimeMillis());
		    regle.get("id_norme").add(norme);
		    regle.get("periodicite").add(periodicite);
		    regle.get("validite_inf").add("1900-01-01");
		    regle.get("validite_sup").add("3000-01-01");
		    regle.get("id_classe").add("duplication");
		    regle.get("rubrique").add(rub);
		    regle.get("rubrique_nmcl").add(alias);
		}

	    }

	    if (type.equals("unicité")) {
		String rubrique = regle.get("rubrique").get(j).toLowerCase();

		String rub = ManipString.substringAfterFirst(rubrique, ".");
		String alias = ManipString.substringBeforeFirst(rubrique, ".");

		// la duplication ne peut se faire que :
		// 1- sur une rubrique de nomenclature
		// 2- si les rubriques existent dans la requete initiale
		// 3- l'alias = la chaine avant le point de la rubrique nomenclature
		if (rubrique.contains(".")) {
		    // modifier la regle de rubrique : alias.rubrique devient rubrique_alias
		    regle.get("rubrique").set(j, rub + "_" + alias);

		}

	    }

	}

    }

    private String appliquerRegleDuplication(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {

	LoggerDispatcher.info("appliquerRegleDuplication()", LOGGER);

	String returned = jointure;

	String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");
	String blocInsert = " insert into {table_destination} "
		+ ManipString.substringAfterFirst(returned, "insert into {table_destination} ");

	for (int j = 0; j < regle.get("id_regle").size(); j++) {

	    String type = regle.get("id_classe").get(j);

	    if (type.equals("duplication")) {
		String rubrique = regle.get("rubrique").get(j).toLowerCase();
		String alias = regle.get("rubrique_nmcl").get(j).toLowerCase();

		// retrouver la table qui doit faire l'objet d'une duplication
		String rubriqueM = getM(blocCreate, rubrique);

		// si on trouve une rubrique mère
		if (rubriqueM != null) {
		    ArrayList<String> aTraiter = getChildrenTree(blocCreate, rubriqueM);
		    aTraiter.add(0, rubriqueM);

		    // System.out.println(aTraiter);

		    StringBuilder blocCreateNew = new StringBuilder();
		    StringBuilder blocInsertNew = new StringBuilder();
		    HashSet<String> colonnesAAjouter = new HashSet<String>();

		    String[] lines = blocCreate.split("\n");
		    for (int i = 0; i < lines.length; i++) {
			String rubriqueLine = getM(lines[i]);
			String pereLine = getFather(lines[i]);
			String tableLine = getTable(lines[i]);

			if (aTraiter.contains(rubriqueLine)) {
			    this.todo = true;

			    String pattern = " as ([^ ()]*) ";

			    // on a trouvé et on effectue les remplacements
			    // on remplace le nom des colonnes (ajout de l'alias) avant le premier from
			    // on remplace le nom de la table (ajout de l'alias)
			    String newLine = ManipString.substringBeforeFirst(lines[i], " from ")
				    .replaceAll(pattern, " as $1_" + alias + " ")
				    .replace(" " + tableLine + " ", " " + tableLine + "_" + alias + " ") + " from "
				    + ManipString.substringAfterFirst(lines[i], " from ");

			    //
			    Pattern p = Pattern.compile(pattern);
			    Matcher m = p.matcher(ManipString.substringBeforeFirst(lines[i], " from "));
			    while (m.find()) {
				colonnesAAjouter.add(m.group(1) + "_" + alias);
			    }

			    // cas particulier : ne pas changer le pere de la table maitre à dupliquer
			    // on ne change le pere que pour les tables enfants

			    if (aTraiter.indexOf(rubriqueLine) == 0) {
				newLine = newLine.replace(" as " + pereLine + "_" + alias + " ",
					" as " + pereLine + " ");
				colonnesAAjouter.remove(pereLine + "_" + alias);
			    }

			    blocCreateNew.append(newLine + "\n");
			    blocCreateNew.append("create temporary table " + tableLine + "_" + alias
				    + "_null as (select * from " + tableLine + "_" + alias + " where false); " + "\n");
			}

			blocCreateNew.append(lines[i] + "\n");

			for (String c : colonnesAAjouter) {
			    c = mToI(c);

			    if (!blocCreate.contains(" add " + c + " ")
				    && !blocCreateNew.toString().contains(" add " + c + " ")) {
				if (c.startsWith("i_")) {
				    String addCol = "do $$ begin alter table {table_destination} add " + c
					    + " integer; exception when others then end; $$;";
				    // blocCreateNew.insert(0,addCol);
				    if (!this.columnToBeAdded.contains(addCol)) {
					this.columnToBeAdded = this.columnToBeAdded + addCol;
				    }
				} else {
				    String addCol = "do $$ begin alter table {table_destination} add " + c
					    + " text; exception when others then end; $$;";
				    // blocCreateNew.insert(0,addCol);
				    if (!this.columnToBeAdded.contains(addCol)) {
					this.columnToBeAdded = this.columnToBeAdded + addCol;
				    }
				}
			    }
			}

		    }

		    blocCreate = blocCreateNew.toString();

		    // bloc insert
		    // faut ajouter les variables et les relations

		    lines = blocInsert.split("\n");
		    for (int i = 0; i < lines.length; i++) {
			// insertion des variables
			blocInsertNew.append(lines[i]);

			// ligne des inserts
			// la colonne m_ devient i_
			if (i == 0) {
			    for (String c : colonnesAAjouter) {
				c = mToI(c);
				if (!lines[i].contains("," + c)) {
				    blocInsertNew.append("," + c);
				}
			    }
			}

			// ligne du select
			// on ne change pas m_ en i_
			if (i == 3) {
			    for (String c : colonnesAAjouter) {
				if (!lines[i].contains("," + c)) {
				    blocInsertNew.append("," + c);
				}
			    }
			}

			blocInsertNew.append("\n");

			// ajout de la relation
			if (i > 3) {
			    for (int k = 0; k < aTraiter.size(); k++) {
				if (lines[i].contains(" t_" + aTraiter.get(k).substring(2) + " ")) {
				    // remplacement du nom de la table
				    String newLine = lines[i]
					    .replace(" t_" + aTraiter.get(k).substring(2) + " ",
						    " t_" + aTraiter.get(k).substring(2) + "_" + alias + " ")
					    .replace("=t_" + aTraiter.get(k).substring(2) + ".",
						    "=t_" + aTraiter.get(k).substring(2) + "_" + alias + ".");
				    // si enfant, on change aussi le nom des variables
				    if (k > 0) {
					newLine = newLine.replace("=", "_" + alias + "=") + "_" + alias;
				    }
				    blocInsertNew.append(newLine + "\n");
				}
			    }

			}

		    }

		    blocInsert = blocInsertNew.toString();
		}
	    }
	}

	returned = blocCreate.replaceAll("\n$", "") + "\n" + blocInsert.replaceAll("\n$", "");

	// System.out.println(jointure);

	return returned;
    }

    /**
     * On va parse la jointure ; on rend les frères indépendants On exclus de
     * l'indépendance le pere commun de frere relié par une relation
     * 
     * @param regle
     * @param norme
     * @param validite
     * @param periodicite
     * @param jointure
     * @throws Exception
     */
    private void ajouterRegleIndependance(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {
	LoggerDispatcher.info("ajouterRegleIndependance()", LOGGER);

	String blocCreate = ManipString.substringBeforeFirst(jointure, "insert into {table_destination}");
	HashSet<String> rubriqueExclusion = new HashSet<String>();

	// pour toutes les règles de relation,
	for (int j = 0; j < regle.get("id_regle").size(); j++) {

	    String type = regle.get("id_classe").get(j);

	    // en gros on parcours les regles de relation
	    // on va exclure les bloc en relation des blocs à calculer comme indépendants

	    if (type.equals("relation")) {
		String rubrique = regle.get("rubrique").get(j).toLowerCase();
		String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();

		// System.out.println(rubrique+" : "+rubriqueNmcl);
		// System.out.println(blocCreate);

		String rubriqueM = getM(blocCreate, rubrique);
		String rubriqueNmclM = getM(blocCreate, rubriqueNmcl);

		if (rubriqueM != null && rubriqueNmclM != null) {
		    ArrayList<String> parentM = getParentsTree(blocCreate, rubriqueM, true);
		    ArrayList<String> parentNmclM = getParentsTree(blocCreate, rubriqueNmclM, true);

		    // on exclus tout les blocs présent dans les listes jusqu'a ce qu'on trouve
		    // l'élément en commun
		    String pppc = pppc(blocCreate, rubriqueM, rubriqueNmclM);

		    for (int i = 0; i < parentM.indexOf(pppc); i++) {
			rubriqueExclusion.add(parentM.get(i));
		    }

		    for (int i = 0; i < parentNmclM.indexOf(pppc); i++) {
			rubriqueExclusion.add(parentNmclM.get(i));
		    }
		}
	    }

	    if (type.equals("cartesian")) {
		String rubrique = regle.get("rubrique").get(j).toLowerCase();
		String rubriqueM = getM(blocCreate, rubrique);
		rubriqueExclusion.add(rubriqueM);
	    }

	}

	// on déroule sur les fils pour ajouter les regles d'indépendance
	ArrayList<String> r = new ArrayList<String>();
	addIndependanceToChildren(r, blocCreate, getM(blocCreate), regle, norme, periodicite, rubriqueExclusion);

    }

    /**
     * Generate and add deletion rules for unsued columns. Need to find the unsued
     * blocks and column (a block contains many columns)
     * 
     * @param rules
     *            : regle dans lesquels le bloc a été ajouté
     * @param norme
     * @param periodicite
     * @param jointure
     * @param rubriqueUtiliseeDansRegles
     * @throws Exception
     */

    private void ajouterRegleSuppression(List<RuleStructurizeEntity> rules, String norme, String periodicite,
	    String jointure, Map<String, ArrayList<String>> rubriqueUtiliseeDansRegles) throws Exception {

	// on va jouer au "qui enleve-t-on" ??
	// on va parcourir les regles de normage, controle, filtrage, mapping et voir
	// quelles sont les rubriques utilisées

	List<String> listVarUtilisee = new ArrayList<>();
	List<Integer> lineASupprimer = new ArrayList<>();

	for (int j = 0; j < rubriqueUtiliseeDansRegles.get("id_norme").size(); j++) {
	    listVarUtilisee.add(rubriqueUtiliseeDansRegles.get("var").get(j));
	}

	String[] joinLines = jointure.split("\n");

	// op 1 : identifier les blocs inutiles
	findUnusedBlocks(rules, norme, periodicite, listVarUtilisee, lineASupprimer, joinLines);

	// op 2 : identifier les variables inutilisées
	findUnsuedColumn(rules, norme, periodicite, listVarUtilisee, lineASupprimer, joinLines);
    }

    private void findUnsuedColumn(List<RuleStructurizeEntity> rules, String norme, String periodicite,
	    List<String> listVarUtilisee, List<Integer> lineASupprimer, String[] joinLines) {
	int nbJoinLines = joinLines.length - 1;
	int index = nbJoinLines;
	while (index >= 1) {
	    String line0 = ManipString.substringBeforeFirst(joinLines[index], "from (select ");

	    if (line0.startsWith("create temporary table ") && !line0.contains("_null as (select * from ")
		    && !lineASupprimer.contains(index)) {
		Pattern p = Pattern.compile(" as [iv][^, (]*");
		Matcher m = p.matcher(line0);
		int nbMatch = 0;
		while (m.find()) {
		    nbMatch++;

		    // on ne considère ni les identifiant de blocs, ni les peres (donc nbMatch>2 car
		    // ceux sont les deux premieres variables
		    // d'un bloc)
		    if (nbMatch > 2) {
			String rubrique = ManipString.substringAfterFirst(m.group(), " as ").substring(2);
			String rubriqueI = "i_" + rubrique;
			String rubriqueV = "v_" + rubrique;
			if (checkIfRuleDoesNotExist(rules,rubriqueI, rubriqueV,rubrique)) {
			    addDeletionRulesColumn(rules, norme, periodicite, index, rubrique);

			}
		    }
		}

	    }
	    index--;

	}
    }

    /**
     * 
     * @param rules
     * @param norme
     * @param periodicite
     * @param listVarUtilisee
     * @param lineToDelete
     * @param joinLines
     */
    private void findUnusedBlocks(List<RuleStructurizeEntity> rules, String norme, String periodicite,
	    List<String> listVarUtilisee, List<Integer> lineToDelete, String[] joinLines) {

	int nbJoinLines = joinLines.length - 1;
	int index = nbJoinLines;

	while (index >= 1) {
	    String line0 = ManipString.substringBeforeFirst(joinLines[index], "from (select ");

	    if (line0.startsWith("create temporary table ") && !line0.contains("_null as (select * from ")) {
		// pour chaque ligne valide, on parcours la liste des variables du controle et
		// du mapping; si on trouve qq'un on ne fait
		// rien
		// sinon on va noter le groupe comme eventuellement à supprimer

		boolean foundVarControleMapping = false;
		for (String varControleMapping : listVarUtilisee) {
		    // on teste toutes les variables sauf le pere du bloc (qui est en 2ieme
		    // position)
		    String line2 = ManipString.substringBeforeFirst(line0, ",") + ","
			    + ManipString.substringAfterFirst(ManipString.substringAfterFirst(line0, ","), ",");

		    if (line2.contains(" " + varControleMapping + " ")) {
			foundVarControleMapping = true;
			break;
		    }
		}

		// on a trouvé aucune variable de mapping dans ce groupe
		if (!foundVarControleMapping) {
		    // extraire la rubrique parente
		    String rubriquePere = ManipString
			    .substringBeforeFirst(ManipString.substringAfterFirst(line0, "as (select "), " as m_");

		    boolean foundRubriquePere = false;
		    // faut vérifier qu'on peut bien supprimer la table : aucune table qui suit ne
		    // doit contenir la rubrique pere de la
		    // table

		    Integer k1 = index + 1;
		    String line1 = ManipString.substringBeforeFirst(joinLines[k1], "from (select ");

		    while (k1 <= nbJoinLines && line1.startsWith("create temporary table ")) {

			// if (!line1.startsWith("create temporary table ")) {
			// break;
			// }

			// si on retrouve le pere dans une table qui n'est pas à supprimer, la table
			// doit etre gardée
			if (line1.contains(" " + rubriquePere + " ") && !lineToDelete.contains(k1)) {
			    foundRubriquePere = true;
			    break;
			}

			k1++;
			line1 = ManipString.substringBeforeFirst(joinLines[k1], "from (select ");
		    }

		    if (!foundRubriquePere) {
			String rubriquePereBloc = rubriquePere.substring(2);

			// ajouter le bloc aux regles de suppression si pas dans la table de regle
			if (checkIfRuleDoesNotExist(rules,rubriquePere, rubriquePereBloc)) {
			    lineToDelete.add(index);

			    addDeletionRulesBlok(rules, norme, periodicite, index, rubriquePereBloc);

			}
		    }
		}

	    }

	    index--;
	}
    }

    private void addDeletionRules(List<RuleStructurizeEntity> rules, String norme, String periodicite, int index,
	    String rubriquePereBloc, String idRule) {

	RuleStructurizeEntity ruleStructurizeEntity = new RuleStructurizeEntity();
	ruleStructurizeEntity.setIdNorme(norme);
	ruleStructurizeEntity.setPeriodicite(periodicite);
	ruleStructurizeEntity.setValiditeInf("1900-01-01");
	ruleStructurizeEntity.setValiditeSup("3000-01-01");
	ruleStructurizeEntity.setIClasse("deletion");
	ruleStructurizeEntity.setRubrique(rubriquePereBloc);
	ruleStructurizeEntity.setRubriqueNmcl(null);
	ruleStructurizeEntity.setIdRule(idRule);
	rules.add(ruleStructurizeEntity);
    }

    private void addDeletionRulesBlok(List<RuleStructurizeEntity> rules, String norme, String periodicite, int index,
	    String rubriquePereBloc) {
	addDeletionRules(rules, norme, periodicite, index, rubriquePereBloc, "B");

    }

    private void addDeletionRulesColumn(List<RuleStructurizeEntity> rules, String norme, String periodicite,
	    int index, String rubriquePereBloc) {
	addDeletionRules(rules, norme, periodicite, index, rubriquePereBloc, "R");

    }

    /**
     * Check if a column is used in the structurize rule.
     * 
     * @param rules
     * @param rubriquePere
     * @param column
     * @return true is the column exists, false otherwise
     */
    private boolean checkIfRuleDoesNotExist(List<RuleStructurizeEntity> rules, String... column) {
	boolean isColumnInRules = false;

	Iterator<RuleStructurizeEntity> it = rules.iterator();

	while (it.hasNext() && !isColumnInRules) {
	    RuleStructurizeEntity rule = it.next();
	    isColumnInRules = rule.containListColumn(column);
	}

	return isColumnInRules;
    }

    //
    //
    /**
     * Modifie la requete pour appliquer les regles de suppression
     * 
     * @param regle
     * @param norme
     * @param validite
     * @param periodicite
     * @param jointure
     * @return
     * @throws Exception
     */
    private String appliquerRegleSuppression(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {

	LoggerDispatcher.info("appliquerRegleSuppression()", LOGGER);

	String returned = jointure;
	int max = 0;

	// ajout des regles
	// parcourt des regles : faut parcourir les suppression d'abord
	for (int j = 0; j < regle.get("id_regle").size(); j++) {

	    String type = regle.get("id_classe").get(j);
	    String rubrique = regle.get("rubrique").get(j);

	    if (type.equals("deletion")) {
		String[] lines = returned.split("\n");
		max = lines.length - 1;

		// on met tout en minuscule
		rubrique = rubrique.toLowerCase();

		// on va iterer sur les lignes pour identifier les groupes à enlever
		// si le groupe est pere d'autre groupe, faut aussi retirer les autres groupes
		int k = max - 1;

		ArrayList<String> grpAEnlever = new ArrayList<String>();
		grpAEnlever.add(rubrique);

		ArrayList<Integer> ligneAEnlever = new ArrayList<Integer>();

		while (k > 0 && !lines[k].startsWith(" from")) {

		    if (lines[k].startsWith(" left join ")) {
			// on ne reteste pas les lignes déjà vues
			if (!ligneAEnlever.contains(k)) {
			    String line = lines[k];

			    String grpTrouve = null;
			    // tester si les rubriques de grpAEnlever sont pere d'une autre rubrique
			    for (String r : grpAEnlever) {
				// cas 1 : ajouter la rubrique trouvée à la liste des rubrique a enlever
				if (line.endsWith("i_" + r)) {
				    // extraire la rubrique à enlever et la mettre dans variable grpTrouve
				    grpTrouve = ManipString
					    .substringBeforeFirst(ManipString.substringAfterLast(line, "=t_"), ".");
				    // marquer la ligne à enlever;
				    ligneAEnlever.add(k);
				    // revenir au début
				    k = max - 1;
				    break;
				}

				// cas 2 : tester si la rubrique est fille
				if (line.startsWith(" left join t_" + r + " ")) {
				    ligneAEnlever.add(k);
				    break;
				}

			    }

			    if (grpTrouve != null) {
				grpAEnlever.add(grpTrouve);
			    }

			}

		    }
		    k = k - 1;
		}

		// on connait desormais les groupes a enlever de la requete

		ArrayList<String> rubriqueAEnlever = new ArrayList<String>();

		k = 1;
		while (k <= max) {
		    String line = lines[k];
		    if (!line.startsWith("create temporary table ")) {
			break;
		    }

		    for (String r : grpAEnlever) {
			if (line.startsWith("create temporary table t_" + r + " ")
				|| line.startsWith("create temporary table t_" + r + "_null ")) {

			    if (!rubriqueAEnlever.contains("i_" + r)) {
				rubriqueAEnlever.add("i_" + r);
			    }

			    if (!rubriqueAEnlever.contains("m_" + r)) {
				rubriqueAEnlever.add("m_" + r);
			    }

			    ligneAEnlever.add(k);

			    Pattern p = Pattern.compile(" as [iv][^, (]* ");
			    Matcher m = p.matcher(line);
			    boolean notFirst = false;
			    while (m.find()) {
				// on n'enleve pas l'identifiant technique de la table pere
				if (notFirst) {
				    rubriqueAEnlever.add(ManipString.substringAfterFirst(m.group(), " as ").trim());
				} else {
				    notFirst = true;
				}
			    }

			    break;
			}
		    }

		    k++;

		}

		// on met la rubrique de base : meme si c'est pas un groupe, ca permet d'enlever
		// des colonnes betement
		if (!rubriqueAEnlever.contains("i_" + rubrique)) {
		    rubriqueAEnlever.add("i_" + rubrique);
		}

		if (!rubriqueAEnlever.contains("v_" + rubrique)) {
		    rubriqueAEnlever.add("v_" + rubrique);
		}

		// on en termine
		// on crée le nouveau stringBuilder
		StringBuilder f = new StringBuilder();
		k = 0;
		while (k <= max) {
		    String line = lines[k];

		    // if (line.startsWith(" insert ") || line.startsWith(" select "))
		    if (true) {
			line = line + ",";
			for (String r : rubriqueAEnlever) {
			    line = line.replace("," + r + ",", ",");
			    line = line.replace(", " + r + " as " + r + " ,", ",");
			    line = line.replace(", " + r + " as " + r + "  ", " ");
			    line = line.replace(",min( " + r + " ) as " + r + ",", ",");
			    line = line.replace(",min( " + r + " ) as  " + r + ",", ",");
			    line = line.replace(",min( " + r + " ) as " + r + " ", " ");
			    line = line.replace(",min( " + r + " ) as  " + r + " ", " ");
			}
			line = line.substring(0, line.length() - 1);
		    }

		    if (!ligneAEnlever.contains(k)) {
			f.append(line + "\n");
		    }

		    k++;

		}

		returned = f.toString();
	    }

	}
	return returned;
    }

    /**
     * Modifie la requete pour appliquer les regles d'indépendance
     * 
     * @param regle
     * @param norme
     * @param validite
     * @param periodicite
     * @param jointure
     * @return
     * @throws Exception
     */
    private String appliquerRegleIndependance(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {

	LoggerDispatcher.info("appliquerRegleIndependance()", LOGGER);

	String returned = jointure;

	String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");
	String blocInsert = " insert into {table_destination} "
		+ ManipString.substringAfterFirst(returned, "insert into {table_destination} ");

	for (int j = 0; j < regle.get("id_regle").size(); j++) {
	    String type = regle.get("id_classe").get(j);

	    if (type.equals("independance")) {

		String rubriqueRegle[] = regle.get("rubrique").get(j).replace(" ", "").toLowerCase().split(",");
		ArrayList<String> rubrique = new ArrayList<String>();

		// ne garder que les rubriques qui existent dans la requete
		// vérifier qu'elles ont le même pere
		String fatherSav = null;
		for (int i = 0; i < rubriqueRegle.length; i++) {
		    if (blocCreate.contains(" " + rubriqueRegle[i] + " ")) {
			String m_rubrique = "m_" + rubriqueRegle[i].substring(2);

			// si on trouve la rubrique mais qu'elle n'est pas identifiant de bloc (m_), on
			// sort
			if (!blocCreate.contains(m_rubrique)) {
			    LoggerDispatcher.info("La rubrique " + rubriqueRegle[i] + " n'identifie pas un bloc",
				    LOGGER);
			    throw new Exception("La rubrique " + rubriqueRegle[i] + " n'identifie pas un bloc");
			}

			rubriqueRegle[i] = m_rubrique;
			rubrique.add(rubriqueRegle[i]);

			if (fatherSav == null) {
			    fatherSav = getFatherM(blocCreate, rubriqueRegle[i]);

			    // System.out.println(rubriqueRegle[i]+"->"+fatherSav);

			} else {
			    // System.out.println(rubriqueRegle[i]+"->"+getFatherM(blocCreate,rubriqueRegle[i]));

			    if (!fatherSav.equals(getFatherM(blocCreate, rubriqueRegle[i]))) {
				LoggerDispatcher.info(
					"La rubrique " + rubriqueRegle[i] + " n'a pas le même pere que les autres",
					LOGGER);
				throw new Exception(
					"La rubrique " + rubriqueRegle[i] + " n'a pas le même pere que les autres");
			    }
			}

		    }
		}

		// si 0 ou 1 rubrique, on sort : pas besoin de calculer l'indépendance
		if (rubrique.size() > 1) {
		    // modifier le blocCreate

		    // 1 ajouter un $ a la fin du nom des table concernée par l'indépendance et le
		    // calcul du rang
		    String[] lines = blocCreate.split("\n");

		    HashMap<String, String> table = new HashMap<String, String>();
		    HashMap<String, String> pere = new HashMap<String, String>();
		    HashMap<String, String> autreCol = new HashMap<String, String>();

		    StringBuilder blocCreateNew = new StringBuilder();
		    StringBuilder blocInsertNew = new StringBuilder();

		    for (int i = 0; i < lines.length; i++) {

			// System.out.println(">"+lines[i]);

			boolean changed = false;

			for (String r : rubrique) {
			    if (testRubriqueInCreate(lines[i], r)) {

				table.put(r, getTable(lines[i]));
				pere.put(r, getFather(lines[i]));
				autreCol.put(r, getOthers(lines[i]));

				// on met le "$" au nom de la table
				blocCreateNew
					.append(lines[i].replace(" as (select ",
						"$ as (select row_number() over (partition by " + pere.get(r)
							+ ") as r, xx.* from    (select ")
						.replace(";", "") + " xx ); \n");

				// on saute une ligne : on ne veut pas garder la table null
				i++;

				changed = true;
			    }
			}

			if (!changed) {
			    blocCreateNew.append(lines[i] + "\n");
			}
			// System.out.println(">"+i);

		    }

		    // pour n bloc indépendants A, B, C on crée une unique table A contenant à la
		    // fois A, B, C en mettant ce qu'on peut en face l'un de l'autre (a.r=b.r=c.r)

		    // ajout des lignes vides pour pouvoir faire les jointures sans jointure externe
		    // si on retrouve (pere,r) sans A et B, pas de soucis, on pourra mettre en face
		    // simplemet
		    // sinon, on ajoute cet identifiant aux tables dans lesquel il manque pour
		    // pouvoir faire la jointure normalement aussi
		    // attention de ne pas générer des valeur null factices (d'ou le left join qui
		    // réintroduit une valeur valide au hasard)
		    for (String r0 : rubrique) {
			for (String r1 : rubrique) {
			    if (!r1.equals(r0)) {
				blocCreateNew.append("insert into " + table.get(r0) + "$ ");
				blocCreateNew.append(" select a.r, c." + r0 + ", a." + pere.get(r1) + " ");

				String pattern = " as ([^ ()]*) ";
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(autreCol.get(r0));
				while (m.find()) {
				    blocCreateNew.append(", c." + m.group(1) + " ");
				}

				blocCreateNew.append(" from " + table.get(r1) + "$ a ");
				blocCreateNew.append(" left join ");
				blocCreateNew.append(" (select distinct on (" + pere.get(r0) + ") * from "
					+ table.get(r0) + "$ ) c ");
				blocCreateNew.append(" on a." + pere.get(r1) + "=c." + pere.get(r0) + " ");
				blocCreateNew.append(" where not exists (select 1 from " + table.get(r0)
					+ "$ b where a.r=b.r and a." + pere.get(r1) + "=b." + pere.get(r0) + ");");
				blocCreateNew.append("\n");

			    }

			}
		    }

		    // création de la table finale; on prend comme id de table celle de la premiere
		    // rubrique
		    blocCreateNew.append("create temporary table " + table.get(rubrique.get(0)) + " as (select");
		    boolean first = true;

		    // les colonnes identifiantes m_
		    for (String r0 : rubrique) {
			if (!first) {
			    blocCreateNew.append(",");
			}
			blocCreateNew.append(" " + r0 + " as " + r0 + " ");
			first = false;
		    }

		    // la colonne pere
		    blocCreateNew.append(", " + table.get(rubrique.get(0)) + "$." + pere.get(rubrique.get(0)) + " as "
			    + pere.get(rubrique.get(0)) + " ");

		    // les autres colonnes
		    for (String r0 : rubrique) {

			String pattern = " as ([^ ()]*) ";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(autreCol.get(r0));
			while (m.find()) {
			    blocCreateNew.append(", " + m.group(1) + " as " + m.group(1) + " ");
			}

		    }

		    blocCreateNew.append("from ");

		    // bloc from
		    first = true;
		    for (String r0 : rubrique) {
			if (!first) {
			    blocCreateNew.append(",");
			}
			blocCreateNew.append(" " + table.get(r0) + "$ ");
			first = false;
		    }

		    // jointure et fini
		    blocCreateNew.append("where ");
		    first = true;
		    for (int i = 1; i < rubrique.size(); i++) {
			if (!first) {
			    blocCreateNew.append("and ");
			}
			blocCreateNew.append(" " + table.get(rubrique.get(i - 1)) + "$." + pere.get(rubrique.get(i - 1))
				+ "=" + table.get(rubrique.get(i)) + "$." + pere.get(rubrique.get(i)));
			blocCreateNew.append(" and " + table.get(rubrique.get(i - 1)) + "$.r="
				+ table.get(rubrique.get(i)) + "$.r ");
			first = false;
		    }
		    blocCreateNew.append(");\n");

		    // la table "null" pour les relation
		    blocCreateNew.append("create temporary table " + table.get(rubrique.get(0))
			    + "_null as (select * from " + table.get(rubrique.get(0)) + " where false);\n");

		    // System.out.println("#"+blocCreateNew);

		    blocCreate = blocCreateNew.toString();

		    // ne reste plus qu'a retirer les conditions de jointure sur les tables
		    // supprimées

		    // System.out.println("#"+blocInsert);

		    String[] linesI = blocInsert.split("\n");
		    for (int i = 0; i < linesI.length; i++) {
			boolean insert = true;
			for (int k = 1; k < rubrique.size(); k++) {
			    if (linesI[i].contains(" " + table.get(rubrique.get(k)) + " ")) {
				insert = false;
			    }

			}

			if (insert) {
			    blocInsertNew.append(linesI[i] + "\n");
			}

		    }

		    blocInsert = blocInsertNew.toString();

		}
	    }

	}

	returned = blocCreate.replaceAll("\n$", "") + "\n" + blocInsert.replaceAll("\n$", "");
	return returned;

    }

    /**
     * Modifie la requete pour appliquer les regles d'unicite
     * 
     * @param regle
     * @param norme
     * @param validite
     * @param periodicite
     * @param jointure
     * @return
     * @throws Exception
     */
    private String appliquerRegleUnicite(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {
	LoggerDispatcher.info("appliquerRegleUnicite()", LOGGER);

	String returned = jointure;
	// extraction de la clause select

	String viewAndInsert = ManipString.substringBeforeLast(returned, " select ");
	String selectBase = " select " + ManipString.substringAfterLast(returned, " select ");

	String[] lines = returned.split("\n");
	int max = lines.length - 1;

	// on parcourt maintenant les regles d'unicité
	for (int j = 0; j < regle.get("id_regle").size(); j++) {
	    String type = regle.get("id_classe").get(j);
	    if (type.equals("unicité")) {

		String rubrique = regle.get("rubrique").get(j).toLowerCase();

		// LoggerDispatcher.info("Filtrage relationnel : "+rubrique+" - "+rubriqueNmcl,
		// logger);

		// vérifier l'existance des rubriques
		if (returned.contains(" " + rubrique + " ")) {

		    // parcourir les ligne pour trouver la table correpondant à la rubrique
		    int k = 1;
		    while (k <= max) {
			String line = lines[k];

			if (line.startsWith(" ")) {
			    break;
			}

			if (!line.startsWith("insert ") && !line.contains("$ as (select ")
				&& testRubriqueInCreate(line, rubrique)) {
			    // récupère l'identifiant pere du block (c'est l'identifiant juste aprés celui
			    // du block m_...)
			    String idBlock = getFather(line);

			    // déclarer une colonne en nomenclature indique en quelque sorte que c'est une
			    // clé dans son groupe :
			    // on garde donc des valeurs uniques dans le groupe (pour un meme pere, une
			    // seule valeur)
			    // et non null (si c'est null, la jointure sera false de toutes façons)
			    // si la colonne nomenclature n'a pas déjà été traitée, on fait ce traitement
			    // d'unicité
			    if (!testRubriqueInCreate(lines[k], "rk_" + rubrique)) {
				lines[k] = ManipString.substringBeforeFirst(line, " (")
					+ " (select * from    (select case when " + rubrique
					+ " is null then 1 else row_number() over (partition by " + idBlock + ","
					+ rubrique + ") end as rk_" + rubrique + " , * from    ("
					+ ManipString.substringAfterFirst(line.replace(";", ""), " (") + " t0_"
					+ rubrique + " ) t1_" + rubrique + " where rk_" + rubrique + "=1);";

				// System.out.println(lines[k]);

			    }

			    break;
			}

			k++;
		    }

		}
	    }
	}

	viewAndInsert = "";
	for (int k = 0; k < lines.length; k++) {
	    viewAndInsert += lines[k] + "\n";
	}
	viewAndInsert = ManipString.substringBeforeLast(viewAndInsert, " select ");

	returned = viewAndInsert + selectBase;

	return returned;
    }

    /**
     * Modifie la requete pour appliquer les regles de relation
     * 
     * @param regle
     * @param norme
     * @param validite
     * @param periodicite
     * @param jointure
     * @return
     * @throws Exception
     */
    private String appliquerRegleRelation(Map<String, ArrayList<String>> regle, String norme, Date validite,
	    String periodicite, String jointure) throws Exception {

	LoggerDispatcher.info("appliquerRegleRelation()", LOGGER);

	String returned = jointure;
	// extraction de la clause select
	String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");

	String viewAndInsert = ManipString.substringBeforeLast(returned, " select ");
	String selectBase = " select " + ManipString.substringAfterLast(returned, " select ");
	String finBase = "\n)" + ManipString.substringAfterLast(selectBase, ")");
	selectBase = ManipString.substringBeforeLast(selectBase, ")");

	String[] lines = returned.split("\n");
	int max = lines.length - 1;

	// on parcourt maintenant les regles de relation
	ArrayList<String> listRubrique = new ArrayList<String>();
	ArrayList<String> listRubriqueNmcl = new ArrayList<String>();
	ArrayList<String> listTableNmcl = new ArrayList<String>();

	// System.out.println(regle);

	for (int j = 0; j < regle.get("id_regle").size(); j++) {
	    String type = regle.get("id_classe").get(j);
	    if (type.equals("relation")) {

		String rubrique = regle.get("rubrique").get(j).toLowerCase();
		String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();

		// LoggerDispatcher.info("Filtrage relationnel : "+rubrique+" - "+rubriqueNmcl,
		// logger);

		// cérifier l'existance des rubriques
		if (returned.contains(" " + rubriqueNmcl + " ") && returned.contains(" " + rubrique + " ")) {
		    listRubrique.add(rubrique);
		    listRubriqueNmcl.add(rubriqueNmcl);

		    // parcourir les ligne pour trouver la table correpondant à la rubriqueNcml
		    int k = 1;
		    while (k <= max) {
			String line = lines[k];

			if (line.startsWith(" ")) {
			    break;
			}

			if (!line.startsWith("insert ") && !line.contains("$ as (select ")
				&& testRubriqueInCreate(line, rubriqueNmcl)) {
			    // extraction du nom de la table
			    listTableNmcl.add(getTable(line));

			    // récupère l'identifiant pere du block (c'est l'identifiant juste aprés celui
			    // du block m_...)
			    // String idBlock=getFather(line);

			    // déclarer une colonne en nomenclature indique en quelque sorte que c'est une
			    // clé dans son groupe :
			    // on garde donc des valeurs uniques dans le groupe (pour un meme pere, une
			    // seule valeur)
			    // et non null (si c'est null, la jointure sera false de toutes façons)
			    // si la colonne nomenclature n'a pas déjà été traitée, on fait ce traitement
			    // d'unicité
			    // if (!testRubriqueInCreate(lines[k],"rk_"+rubriqueNmcl))
			    // {
			    // lines[k]=ManipString.substringBeforeFirst(line," (")
			    // +" (select * from (select case when "+rubriqueNmcl+" is null then -1 else
			    // row_number() over (partition by "+idBlock+","+rubriqueNmcl+") end as
			    // rk_"+rubriqueNmcl+" , * from ("
			    // + ManipString.substringAfterFirst(line.replace(";", "")," (")
			    // +" t0_"+rubriqueNmcl+" ) t1_"+rubriqueNmcl+" where rk_"+rubriqueNmcl+"=1);"
			    // ;
			    // }

			    break;
			}

			k++;
		    }

		    // jointure=jointure+"\n AND ("+rubrique+"="+rubriqueNmcl+" ";
		    // jointure=jointure+"\n or "+rubrique+" is null ";
		    // jointure=jointure+"\n or "+rubrique+" not in (select distinct
		    // "+rubriqueNmcl+" from
		    // "+this.this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_OK)2+" where
		    // id_source='"+id_source+"' and "+rubriqueNmcl+" is not null)) ";
		}
	    }
	}

	// retravailler le viewAndInsert
	// la valeur nomenclature est une clé; prendre le premier enregistrement pour
	// chaque clé
	// on reecrit les with en create temporary table (problème de mémoire) (on fait
	// donc sauter la premiere ligne qui est juste un placeholder)
	// on ajoute une premiere ligne : discard temp

	viewAndInsert = "";
	for (int k = 0; k < lines.length; k++) {

	    viewAndInsert += lines[k] + "\n";

	}

	viewAndInsert = ManipString.substringBeforeLast(viewAndInsert, " select ");

	// faire toutes les compositions de jointure possible
	// compter en binaire
	String select;
	int r;

	for (int k = 0; k < Math.pow(2, listRubrique.size()); k++) {
	    if (k > 0) {
		viewAndInsert = viewAndInsert + "\n UNION ALL \n";
	    }
	    select = selectBase;

	    // on le fait en deux passages; traiter les r=1 (pas de lien) d'abord puis les
	    // r=0 (lien trouvé)
	    // pour traiter le cas de test suivant : si s40 est lié à s51 et s40 n'est pas
	    // lié à s52
	    // alors on garde s40
	    int z = k;
	    for (int l = 0; l < listRubrique.size(); l++) {
		r = z % 2;
		z = (z - r) / 2;

		if (r == 1) {
		    // cas 2 : jointure non trouvée : la rubrique est null ou la rubrique n'a aucune
		    // correspondace dans la table de nomenclature
		    // on substitue la vue de la rubrique nomenclature à la rubrique vide
		    // on ajoute la clause de jointure
		    select = select.replace(" " + listTableNmcl.get(l) + " ", " " + listTableNmcl.get(l) + "_null ")
			    .replace("=" + listTableNmcl.get(l) + ".", "=" + listTableNmcl.get(l) + "_null.");

		    // select=select+"\n AND ("+listRubrique.get(l)+" is null or
		    // "+listRubrique.get(l)+" not in (select distinct "+listRubriqueNmcl.get(l)+"
		    // from "+getTable(blocCreate,listRubriqueNmcl.get(l))+" where
		    // "+listRubriqueNmcl.get(l)+" is not null))";
		    select = select + "\n AND NOT EXISTS (select 1 from (select distinct " + listRubriqueNmcl.get(l)
			    + " as g_rub," + getFather(getLine(blocCreate, listRubriqueNmcl.get(l)))
			    + " as g_pere from " + getTable(blocCreate, listRubriqueNmcl.get(l)) + ") xx where "
			    + listRubrique.get(l) + "=g_rub and "
			    + iToM(getFather(getLine(blocCreate, listRubriqueNmcl.get(l)))) + "=g_pere) ";
		    // System.out.println(listRubriqueNmcl.get(l)+">"+iToM(getFather(getLine(blocCreate,
		    // listRubriqueNmcl.get(l)))));

		}
	    }

	    z = k;
	    for (int l = 0; l < listRubrique.size(); l++) {
		r = z % 2;
		z = (z - r) / 2;

		if (r == 0) {
		    // cas 1 : jointure trouvée
		    // on substitue la vue de la rubrique vide à la rubrique nomenclature
		    // (on défait ce qu'on a éventuellement fait en r=1)
		    // on ajoute la clause de jointure
		    select = select.replace(" " + listTableNmcl.get(l) + "_null ", " " + listTableNmcl.get(l) + " ")
			    .replace("=" + listTableNmcl.get(l) + "_null.", "=" + listTableNmcl.get(l) + ".");
		    select = select + "\n AND " + listRubrique.get(l) + "=" + listRubriqueNmcl.get(l) + " ";
		    // select=select+"\n AND ("+listRubrique.get(l)+"="+listRubriqueNmcl.get(l)+" or
		    // "+getM(blocCreate,listRubrique.get(l))+" is null)";
		}
	    }

	    viewAndInsert = viewAndInsert + select;

	}

	returned = viewAndInsert + finBase;

	return returned;

    }

    /**
     * determine le pere d'une rubrique "m_<***>" dans un bloc
     * 
     * @param bloc
     * @param rubrique
     * @return
     */
    private String getFather(String bloc, String rubrique) {
	return ManipString
		.substringBeforeFirst(
			ManipString.substringAfterFirst(
				ManipString.substringBeforeFirst(
					ManipString.substringAfterFirst(bloc, " as " + rubrique + " "), " from ("),
				" as "),
			" ,")
		.trim();
    }

    /**
     * determine le pere d'une rubrique "m_<***>" dans un bloc
     * 
     * @param bloc
     * @param rubrique
     * @return
     */
    private String getFatherM(String bloc, String rubrique) {
	String r = getFather(bloc, rubrique);
	if (!r.equals("")) {
	    r = "m_" + r.substring(2);
	} else {
	    r = rubrique;
	}
	return r;

    }

    /**
     * A partir du blocCreate, determine l'arbre vers une rubrique "m_<***>"
     * 
     * @param blocCreate
     * @param mRubrique
     * @return
     */
    private ArrayList<String> getParentsTree(String blocCreate, String mRubrique, boolean... keep) {
	ArrayList<String> r = new ArrayList<String>();
	r.add(mRubrique);
	String s;

	while (!(s = getFatherM(blocCreate, r.get(r.size() - 1))).equals(r.get(r.size() - 1))) {
	    r.add(s);
	}

	if (!(keep.length > 0 && keep[0])) {
	    r.remove(0);
	}

	return r;

    }

    /**
     * A partir du blocCreate, determine récursivement les enfant d'une rubrique
     * "m_<***>"
     * 
     * @param blocCreate
     * @param mRubrique
     * @return
     */
    private ArrayList<String> getChildrenTree(String blocCreate, String mRubrique) {
	ArrayList<String> s = new ArrayList<String>();
	getChildrenTree(s, blocCreate, mRubrique);
	return s;
    }

    /**
     * A partir du blocCreate, determine récursivement les enfant d'une rubrique
     * "m_<***>"
     * 
     * @param r
     * @param blocCreate
     * @param mRubrique
     */

    private void addIndependanceToChildren(ArrayList<String> r, String blocCreate, String mRubrique,
	    Map<String, ArrayList<String>> regle, String norme, String periodicite, HashSet<String> exclusion) {
	ArrayList<String> s = getChildren(blocCreate, mRubrique);

	if (s.isEmpty()) {
	    return;
	} else {
	    r.addAll(s);

	    // si on a exclus le bloc, on ne le met pas dans la regle
	    int nbRubriqueRetenue = 0;
	    StringBuilder untoken = new StringBuilder();
	    for (String z : s) {
		if (!exclusion.contains(z)) {
		    if (untoken.length() > 0) {
			untoken.append(",");
		    }
		    untoken.append(z);
		    nbRubriqueRetenue++;
		}

	    }

	    // ne créer une regle que si y'a plus d'une rubrique retenue; sinon pas la peine
	    if (nbRubriqueRetenue > 1) {
		regle.get("id_regle").add("G" + System.currentTimeMillis());
		regle.get("id_norme").add(norme);
		regle.get("periodicite").add(periodicite);
		regle.get("validite_inf").add("1900-01-01");
		regle.get("validite_sup").add("3000-01-01");
		regle.get("id_classe").add("independance");
		regle.get("rubrique").add(untoken.toString());
		regle.get("rubrique_nmcl").add(null);

	    }

	    for (String rub : s) {
		addIndependanceToChildren(r, blocCreate, rub, regle, norme, periodicite, exclusion);
	    }

	}

    }

    /**
     * A partir du blocCreate, determine récursivement les enfant d'une rubrique
     * "m_<***>"
     * 
     * @param r
     * @param blocCreate
     * @param mRubrique
     */

    private void getChildrenTree(ArrayList<String> r, String blocCreate, String mRubrique) {
	ArrayList<String> s = getChildren(blocCreate, mRubrique);

	if (s.isEmpty()) {
	    return;
	} else {
	    r.addAll(s);

	    for (String rub : s) {
		getChildrenTree(r, blocCreate, rub);
	    }

	}

    }

    /**
     * A partir du blocCreate, determine les enfants d'une rubrique "m_<***>"
     * 
     * @param blocCreate
     * @param mRubrique
     * @return
     */
    private ArrayList<String> getChildren(String blocCreate, String mRubrique) {
	ArrayList<String> r = new ArrayList<String>();

	if (mRubrique == null) {
	    return r;
	}

	String lines[] = blocCreate.split("\n");

	for (int i = 0; i < lines.length; i++) {

	    if (!testRubriqueInCreate(lines[i], mRubrique)
		    && testRubriqueInCreate(lines[i], "i_" + mRubrique.substring(2))) {
		r.add("m_" + ManipString.substringBeforeFirst(ManipString.substringAfterFirst(lines[i], " as m_"), " ")
			.trim());
	    }

	}
	return r;

    }

    /**
     * la rubrique identifiante <m_...> trouvée dans le bloc
     * 
     * @param blocCreate
     * @return
     */
    private String getM(String bloc) {
	return "m_" + ManipString.substringBeforeFirst(ManipString.substringAfterFirst(bloc, " as m_"), " ");
    }

    /**
     * le pere du bloc
     * 
     * @param bloc
     * @return
     */
    private String getFather(String bloc) {
	return getFather(bloc, getM(bloc));
    }

    /**
     * la table du bloc
     * 
     * @param bloc
     * @return
     */
    private String getTable(String bloc) {

	return ManipString.substringBeforeFirst(ManipString.substringAfterFirst(bloc, "create temporary table "),
		" as ");

    }

    /**
     * les autres colonnes du blocs
     * 
     * @param bloc
     * @return
     */
    private String getOthers(String bloc) {
	String r = ManipString
		.substringBeforeFirst(ManipString.substringAfterFirst(bloc, " as " + getFather(bloc) + " "), " from (");
	if (r.startsWith(",")) {
	    r = r.substring(1);
	}
	return r;
    }

    /**
     * renvoie la ligne a laquelle la rubrique appartient
     * 
     * @param blocCreate
     * @param rubrique
     * @return
     */
    private String getLine(String blocCreate, String rubrique) {
	String lines[] = blocCreate.split("\n");

	for (int i = 0; i < lines.length; i++) {

	    if (testRubriqueInCreate(lines[i], rubrique)) {
		return lines[i];
	    }
	}
	return null;
    }

    /**
     * l'identifiant de la ligne a laquelle la rubrique appartient
     * 
     * @param blocCreate
     * @param rubrique
     * @return
     */
    private String getM(String blocCreate, String rubrique) {
	String lines[] = blocCreate.split("\n");

	for (int i = 0; i < lines.length; i++) {

	    if (testRubriqueInCreate(lines[i], rubrique)) {
		return getM(lines[i]);
	    }
	}
	return null;
    }

    private String mToI(String rubrique) {
	if (rubrique.startsWith("m_")) {
	    return "i_" + rubrique.substring(2);
	}
	return rubrique;
    }

    private String iToM(String rubrique) {
	if (rubrique.startsWith("i_")) {
	    return "m_" + rubrique.substring(2);
	}
	return rubrique;
    }

    /**
     * le nom de la table de la ligne a laquelle la rubrique appartient
     * 
     * @param blocCreate
     * @param rubrique
     * @return
     */
    private String getTable(String blocCreate, String rubrique) {
	String lines[] = blocCreate.split("\n");

	for (int i = 0; i < lines.length; i++) {

	    if (testRubriqueInCreate(lines[i], rubrique)) {
		return getTable(lines[i]);
	    }
	}
	return null;
    }

    /**
     * test si la rubrique est dans le bloc fourni
     * 
     * @param bloc
     * @param rubrique
     * @return
     */
    private boolean testRubriqueInCreate(String bloc, String rubrique) {
	return ManipString.substringBeforeFirst(bloc, " from (").contains(" as " + rubrique + " ");
    }

    /**
     * pppc = plus petit pere en commun renvoir le pppc
     * 
     * @param blocCreate
     * @param rubriqueM
     * @param rubriqueNmclM
     * @return
     */
    private String pppc(String blocCreate, String rubriqueM, String rubriqueNmclM) {

	ArrayList<String> rubriqueParents = getParentsTree(blocCreate, rubriqueM, true);
	ArrayList<String> rubriqueNmclParents = getParentsTree(blocCreate, rubriqueNmclM, true);

	for (int k = 0; k < rubriqueNmclParents.size(); k++) {
	    for (int l = 0; l < rubriqueParents.size(); l++) {
		if (rubriqueParents.get(l).equals(rubriqueNmclParents.get(k))) {
		    return rubriqueNmclParents.get(k);
		}

	    }
	}

	return null;

    }

    /**
     * On sort les données des tables temporaires du module vers : - les tables
     * définitives du normage (normage_ok et normage_ko) de l'application - la vraie
     * table de pilotage - la table buffer
     *
     * IMPORTANT : les ajouts ou mise à jours de données sur les tables de
     * l'application doivent avoir lieu dans un même bloc de transaction (ACID)
     * 
     * @throws Exception
     *
     */
    private void finalInsertion() throws Exception {

	updateNbEnr(this.getTablePilTempThread(), this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_OK));

	String tableIdSourceOK = tableOfIdSource(this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_OK),
		this.idSource);
	createTableInherit(this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_OK), tableIdSourceOK);
	String tableIdSourceKO = tableOfIdSource(this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_KO),
		this.idSource);
	createTableInherit(this.bddTable.getQualifedName(BddTable.ID_WORKING_TABLE_KO), tableIdSourceKO);

	StringBuilder requete = new StringBuilder();

	if (paramBatch == null) {
	    requete.append(FormatSQL.tryQuery("alter table " + tableIdSourceOK + " inherit "
		    + this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_OK) + "_todo;"));
	    requete.append(FormatSQL.tryQuery("alter table " + tableIdSourceOK + " inherit "
		    + this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_OK) + ";"));
	    requete.append(FormatSQL.tryQuery("alter table " + tableIdSourceKO + " inherit "
		    + this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_KO) + ";"));
	} else {
	    requete.append(FormatSQL.tryQuery("alter table " + tableIdSourceOK + " inherit "
		    + this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_OK) + "_todo;"));
	    requete.append(FormatSQL.tryQuery("alter table " + tableIdSourceKO + " inherit "
		    + this.bddTable.getQualifedName(BddTable.ID_TABLE_OUT_OK) + ";"));
	}

	UtilitaireDao.get("arc").executeBlock(connection, requete);

    }

    /**
     * récupere le contenu d'une requete; iniitalise si le résultat est null
     * 
     * @param aConnection
     * @param aRequest
     * @return
     * @throws SQLException
     */
    @SQLExecutor
    public Map<String, ArrayList<String>> getBean(String aRequest) throws SQLException {
	GenericBean genericBean = new GenericBean(
		UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(this.connection, aRequest));
	Map<String, ArrayList<String>> genericBeanMapcontent = genericBean.mapContent();

	if (genericBean.headers.isEmpty()) {
	    for (int i = 0; i < genericBean.headers.size(); i++) {
		genericBeanMapcontent.put(genericBean.headers.get(i), new ArrayList<String>());
	    }
	}

	return genericBeanMapcontent;
    }

    @Override
    public boolean initialize() {
	return false;
    }

}
