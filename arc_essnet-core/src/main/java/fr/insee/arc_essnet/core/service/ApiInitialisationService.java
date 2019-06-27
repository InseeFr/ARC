package fr.insee.arc_essnet.core.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.model.BddTable;
import fr.insee.arc_essnet.core.model.DbConstant;
import fr.insee.arc_essnet.core.model.TraitementRapport;
import fr.insee.arc_essnet.core.model.TraitementState;
import fr.insee.arc_essnet.core.model.TraitementTableExecution;
import fr.insee.arc_essnet.core.model.TraitementTableParametre;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.format.Format;
import fr.insee.arc_essnet.utils.structure.AttributeValue;
import fr.insee.arc_essnet.utils.structure.GenericBean;
import fr.insee.arc_essnet.utils.structure.tree.HierarchicalView;
import fr.insee.arc_essnet.utils.utils.FormatSQL;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import fr.insee.arc_essnet.utils.utils.ManipString;
import fr.insee.arc_essnet.utils.utils.Pair;
import fr.insee.arc_essnet.utils.utils.SQLExecutor;

/**
 * ApiInitialisationService
 *
 * 1- Implémenter des maintenances sur la base de donnée </br>
 * 2- Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
 * l'environnement d'excécution courant</br>
 * 3- Gestion des fichiers en doublon</br>
 * 4- Assurer la cohérence entre les table de données et la table de pilotage de
 * l'environnement qui fait foi</br>
 * 5- Maintenance base de données</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiInitialisationService extends AbstractPhaseService implements IApiServiceWithoutOutputTable {
    private static final String TODO = "_TODO";

    private static final String ARCHIVE = "_ARCHIVE";

    private static final String ARC_IHM_ENTREPOT = "arc.ihm_entrepot";

    private static final String BDD_SCRIPT_FUNCTION_SQL = "BdD/script_function.sql";
    private static final String BDD_SCRIPT_TABLE_SQL = "BdD/script_table.sql";

    public ApiInitialisationService() {
	super();
    }

    // Number of file in each archive
    private static final int NB_FICHIER_PER_ARCHIVE = 10000;

    private static final int TIME_TO_KEEP = 365;

    private static final Logger LOGGER = Logger.getLogger(ApiInitialisationService.class);
    private String tablePilTemp2;

    public ApiInitialisationService(String aCurrentPhase, String anParametersEnvironment, String aexecutionEnv,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(aCurrentPhase, anParametersEnvironment, aexecutionEnv, aDirectoryRoot, aNbEnr, paramBatch);
	this.tablePilTemp2 = FormatSQL
		.temporaryTableName(dbEnv(aexecutionEnv) + TraitementTableExecution.PILOTAGE_FICHIER, "2");
    }

    public ApiInitialisationService(Connection connexion, String aCurrentPhase, String anParametersEnvironment,
	    String aexecutionEnv, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(connexion, aCurrentPhase, anParametersEnvironment, aexecutionEnv, aDirectoryRoot, aNbEnr, paramBatch);
	this.tablePilTemp2 = FormatSQL
		.temporaryTableName(dbEnv(aexecutionEnv) + TraitementTableExecution.PILOTAGE_FICHIER, "2");
    }

    @Override
    public void process() throws Exception {

	cleanPilotagetable(this.connection, this.executionEnv);

	// Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
	// l'environnement d'excécution courant
	copyTablesToExecutionThrow(connection, parameterEnv, executionEnv);

	// mettre à jour les tables métier avec les paramêtres de la famille de norme
	mettreAJourSchemaTableMetierThrow(connection, parameterEnv, executionEnv);

	// marque les fichiers ou les archives à rejouer
	reinstate(this.connection, this.getTablePil());

	// efface des fichiers de la table de pilotage
	cleanToDelete(this.connection, this.getTablePil());

	// Met en cohérence les table de données avec la table de pilotage de
	// l'environnement
	// La table de pilotage fait foi
	synchroniserEnvironmentByPilotage(this.connection, this.executionEnv);

	// remettre les archives ou elle doivent etre en cas de restauration de la base
	rebuildFileSystem();

    }

    /**
     * 
     * Restore the filesystem in case of database restoration
     *
     * @throws Exception
     */
    @SQLExecutor
    public void rebuildFileSystem() throws Exception {
	LoggerDispatcher.info("rebuildFileSystem", LOGGER);

	// parcourir toutes les archives dans le répertoire d'archive
	String repertoire = properties.getBatchParametreRepertoire();
	String envDir = this.executionEnv.replace(".", "_").toUpperCase();
	String nomTableArchive = dbEnv(executionEnv) + "pilotage_archive";

	/*
	 * For each data repository Check archive in repo / archive register in
	 * database. Database in old state so we can fin in the repo the archive that
	 * are missing in the database
	 */

	if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists(ARC_IHM_ENTREPOT))) {
	    ArrayList<String> repoList = new GenericBean(
		    UtilitaireDao.get("arc").executeRequest(null, "select id_entrepot from arc.ihm_entrepot"))
			    .mapContent().get("id_entrepot");

	    processFileInRepo(repertoire, envDir, nomTableArchive, repoList);

	}
    }

    @SQLExecutor
    private void processFileInRepo(String repertoire, String envDir, String nomTableArchive, ArrayList<String> repoList)
	    throws SQLException, IOException {
	if (repoList != null) {
	    for (String s : repoList) {
		String dirIn = repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + s + ARCHIVE;
		String dirOut = repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + s;
		
		rebuildDirectories(dirIn,dirOut);
		
		// on itère sur les fichiers trouvé dans le répertoire d'archive
		File f = new File(dirIn);
		File[] files = f.listFiles();
		String fileTable = "t_files";
		// Insert table in the temporary table t_files
		StringBuilder request = new StringBuilder();

		request.append(FormatSQL.dropUniqueTable(fileTable));
		request.append(
			FormatSQL.createTemporaryTableWithColumn("t_files", new Pair<String, String>("fname", "text")));

		boolean first = true;

		System.out.println("$$$$$ Répertoire : " + dirIn);

		for (File fichier : files) {
		    if (!fichier.isDirectory()) {
			if (first || request.length() > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {
			    UtilitaireDao.get("arc").executeRequest(this.connection, request + ";");
			    request = new StringBuilder();
			    request.append(
				    "INSERT INTO t_files values ('" + fichier.getName().replace("'", "''") + "')");
			    first = false;
			} else {
			    request.append(",('" + fichier.getName().replace("'", "''") + "')");
			}
		    }
		}
		UtilitaireDao.get("arc").executeRequest(this.connection, request + ";");

		/*
		 * Looking for files in archive repo not in archive table. If some are found
		 * it's not constitent and those files have to be put in register directory to
		 * be register again
		 */

		processInconsistentFiles(nomTableArchive, dirIn, dirOut);

		// Traitement des # dans le repertoire de reception
		// on efface les # dont le fichier existe déjà avec un autre nom sans # ou un
		// numéro # inférieur
		fileWithHashtagInName(dirOut);

	    }
	}
    }

    /**
     * Create directory if not exists
     * @param dirs
     */
    public static void rebuildDirectories(String... dirs)
    {
    	for (String d:dirs)
    	{
    		File f = new File(d);
    		if (!f.exists())
    		{
    			f.mkdirs();
    		}
    	}
    }
    
    private void processInconsistentFiles(String nomTableArchive, String dirIn, String dirOut) throws SQLException {
	StringBuilder request;
	request = new StringBuilder();
	request.append(" SELECT fname FROM t_files a ");
	request.append(" WHERE NOT EXISTS (SELECT * FROM " + nomTableArchive + " b WHERE b.nom_archive=a.fname) ");

	ArrayList<String> fileToBeMoved = new GenericBean(
		UtilitaireDao.get("arc").executeRequest(this.connection, request)).mapContent().get("fname");

	if (fileToBeMoved != null) {
	    for (String fname : fileToBeMoved) {
		ApiReceptionService.deplacerFichier(dirIn, dirOut, fname, fname);
	    }
	}
    }

    private void fileWithHashtagInName(String dirOut) throws IOException {
	File f;
	File[] files;
	f = new File(dirOut);
	files = f.listFiles();

	for (File fichier : files) {
	    String nameNoExt = ManipString.substringBeforeFirst(fichier.getName(), ".");
	    String ext = "." + ManipString.substringAfterFirst(fichier.getName(), ".");

	    if (nameNoExt.contains("#")) {
		Integer number = ManipString.parseNumber(ManipString.substringAfterLast(nameNoExt, "#"));

		processValidFile(dirOut, fichier, nameNoExt, ext, number);
	    }

	}
    }

    private void processValidFile(String dirOut, File fichier, String nameNoExt, String ext, Integer number)
	    throws IOException {
	if (number != null) {

	    String nameSource = ManipString.substringBeforeLast(nameNoExt, "#");

	    // comparer au fichier sans index
	    File autreFichier = new File(dirOut + File.separator + nameSource + ext);
	    if (autreFichier.exists() && FileUtils.contentEquals(autreFichier, fichier) && fichier.delete()) {
		LoggerDispatcher.info(String.format("The file %s have been delete", fichier.getName()), LOGGER);
	    }

	    // Compare files with previous index
	    for (int i = 2; i < number; i++) {
		autreFichier = new File(dirOut + File.separator + nameSource + "#" + i + ext);

		if (autreFichier.exists() && FileUtils.contentEquals(autreFichier, fichier) && fichier.delete()) {
		    LoggerDispatcher.info(String.format("The file %s have been delete", fichier.getName()), LOGGER);

		}

	    }

	}
    }

    /**
     * Méthode pour implémenter des maintenances sur la base de donnée
     *
     * @param connection
     * @throws Exception
     */
    public void bddScript() {

	String user = "arc";
	try {
	    user = UtilitaireDao.get("arc").getString(null, "select user ");
	} catch (SQLException ex) {
	    LoggerHelper.error(LOGGER, ex, "bddScript()");
	}

	StringBuilder request = new StringBuilder();

	// création des tables si elles n'xistent pas

	if (!executionEnv.contains(".")) {
	    request.append(createSchema(executionEnv, user));
	}

	requestCreateMonitoringTableT(request);
	requestCreateMonitoringTable(request);
	requestCreateMonitoringtableArchive(request);

	try {
	    // table script
	    request.append(
		    IOUtils.toString(getClass().getClassLoader().getResourceAsStream(BDD_SCRIPT_TABLE_SQL), "UTF-8"));
	    // fonction script
	    request.append(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(BDD_SCRIPT_FUNCTION_SQL),
		    "UTF-8"));
	    request.append(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(BDD_SCRIPT_FUNCTION_SQL),
			    "UTF-8"));
	} catch (IOException e) {
	    LoggerDispatcher.error("Error dowload sql function file", e, LOGGER);
	}

	try {
	    UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(connection, request);
	} catch (SQLException e) {
	    LoggerDispatcher.error("Error in creation table request", e, LOGGER);
	}

	ArrayList<String> lTable = new ArrayList<>(
		Arrays.asList(dbEnv(executionEnv) + TypeTraitementPhase.LOAD + "_" + TraitementState.OK,
			dbEnv(executionEnv) + TypeTraitementPhase.STRUCTURIZE_XML + "_" + TraitementState.OK,
			dbEnv(executionEnv) + TypeTraitementPhase.STRUCTURIZE_XML + "_" + TraitementState.KO,
			dbEnv(executionEnv) + TypeTraitementPhase.CONTROL + "_" + TraitementState.OK,
			dbEnv(executionEnv) + TypeTraitementPhase.CONTROL + "_" + TraitementState.KO,
			dbEnv(executionEnv) + TypeTraitementPhase.FILTER + "_" + TraitementState.OK,
			dbEnv(executionEnv) + TypeTraitementPhase.FILTER + "_" + TraitementState.KO,
			dbEnv(executionEnv) + TypeTraitementPhase.FORMAT_TO_MODEL + "_" + TraitementState.KO,
			dbEnv(executionEnv) + TypeTraitementPhase.LOAD + "_" + TraitementState.OK + TODO,
			dbEnv(executionEnv) + TypeTraitementPhase.STRUCTURIZE_XML + "_" + TraitementState.OK + TODO,
			dbEnv(executionEnv) + TypeTraitementPhase.CONTROL + "_" + TraitementState.OK + TODO,
			dbEnv(executionEnv) + TypeTraitementPhase.FILTER + "_" + TraitementState.OK + TODO));

	String dataDef = "(id_source text COLLATE pg_catalog.\"C\", id integer, date_integration text COLLATE pg_catalog.\"C\", id_norme text COLLATE pg_catalog.\"C\",  periodicite text COLLATE pg_catalog.\"C\", validite text COLLATE pg_catalog.\"C\")"
		+ FormatSQL.WITH_NO_VACUUM + ";";

	try {

	    // If empty table -> recreate
	    for (String t : lTable) {
		UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(connection,
			"CREATE TABLE IF NOT EXISTS " + t + dataDef);

		if (!UtilitaireDao.get(DbConstant.POOL_NAME).hasResults(connection,
			"select 1 FROM " + t + " limit 1")) {
		    UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(connection,
			    "DROP TABLE IF EXISTS " + t + " CASCADE; CREATE TABLE IF NOT EXISTS " + t + dataDef);
		}
	    }

	} catch (Exception e) {
	    LoggerDispatcher.error("Error in creation table phase", e, LOGGER);

	}
    }

    private void requestCreateMonitoringtableArchive(StringBuilder request) {
	request.append(createTableNoVacum(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_ARCHIVE)));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_ARCHIVE), "entrepot",
		FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_ARCHIVE),
		"nom_archive", FormatSQL.TEXT_COLLATE_C));
    }

    private void requestCreateMonitoringTableT(StringBuilder request) {
	request.append(createTableNoVacum(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T)));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T),
		"date_entree", FormatSQL.TEXT_COLLATE_C));
    }
    


    private void requestCreateMonitoringTable(StringBuilder request) {
	request.append(createTableNoVacum(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "id_source",
		FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "id_norme",
				FormatSQL.TEXT_COLLATE_C));  
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "validite",
		FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"periodicite", FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"phase_traitement", FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"etat_traitement", "text[] COLLATE pg_catalog.\"C\""));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"date_traitement", "timestamp without time zone"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "rapport",
		FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "taux_ko",
		"numeric"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "nb_enr",
		"integer"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "nb_essais",
		"integer"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "etape",
		"integer"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"validite_inf", "date"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"validite_sup", "date"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "version",
		FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"date_entree", "text"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "container",
		FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"v_container", FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"o_container", FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
			"to_delete", FormatSQL.TEXT_COLLATE_C));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "client",
		"text[]"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		"date_client", "timestamp without time zone[]"));
	request.append(addColumnToTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "jointure",
		FormatSQL.TEXT_COLLATE_C));
    }

    private StringBuilder createSchema(String executionEnv, String user) {
	StringBuilder returned = new StringBuilder();
	returned.append("\n CREATE SCHEMA IF NOT EXISTS " + executionEnv + " AUTHORIZATION " + user + "; ");
	returned.append("\n GRANT ALL ON SCHEMA " + executionEnv + " TO " + user + "; ");
	returned.append("\n GRANT ALL ON SCHEMA " + executionEnv + " TO public; ");
	return returned;
    }

    private String createTableNoVacum(String theTableName) {
	String returned = String.format("%n CREATE TABLE IF NOT EXISTS %s ()", theTableName);
	return returned + FormatSQL.WITH_NO_VACUUM + "; ";
    }

    private String addColumnToTable(String theTableName, String theColumnName, String theColumnType) {
	return String.format("%n ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s %s;", theTableName, theColumnName,
		theColumnType);
    }

    /**
     * Recopy/replace user defined rules (_ihm tables) in the current process env
     * 
     * @throws Exception
     */
    public static void synchroniserSchemaExecution(Connection connection, String envParameters, String executionEnv) {
	copyTablesToExecution(connection, envParameters, executionEnv);
	updateSchemaBusinessTable(connection, envParameters, executionEnv);
    }

    /**
     * Re process files
     *
     * @param connexion
     * @param tablePil
     * @throws SQLException
     */
    @SQLExecutor
    public void reinstate(Connection connexion, String tablePil) throws Exception {
	LoggerDispatcher.info("reinstateWithRename", LOGGER);

	// Looking for containers with a reinstate file
	// Get back the archive to root

	ArrayList<String> containerList = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
		"select distinct container from " + tablePil + " where to_delete in ('R','RA')")).mapContent()
			.get("container");

	if (containerList != null) {
	    String repertoire = properties.getBatchParametreRepertoire();
	    String envDir = this.executionEnv.replace(".", "_").toUpperCase();

	    for (String s : containerList) {

		String repository = ManipString.substringBeforeFirst(s, "_");
		String archive = ManipString.substringAfterFirst(s, "_");

		String dirIn = repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + repository
			+ ARCHIVE;
		String dirOut = repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + repository;

		ApiReceptionService.deplacerFichier(dirIn, dirOut, archive, archive);

	    }

	}

	// Clean archive tag by RA
	UtilitaireDao.get("arc").executeImmediate(connexion,
		"DELETE FROM " + this.getTablePil() + " a where exists (select 1 from " + this.getTablePil()
			+ " b where a.container=b.container and b.to_delete='RA')");

    }

    /**
     * Create or destroy column in business table by comparing the columns of table
     * and the norme familly definition
     *
     * @param connexion
     * @throws Exception
     */

    public static void updateSchemaBusinessTable(Connection connexion, String envParameters, String executionEnv) {
	try {
	    LoggerDispatcher.info("Update business table avec the familly norme definition", LOGGER);
	    mettreAJourSchemaTableMetierThrow(connexion, envParameters, executionEnv);
	} catch (Exception e) {
	    LoggerDispatcher.info("Error updateSchemaBusinessTable", LOGGER);
	}

    }

    public static void mettreAJourSchemaTableMetierThrow(Connection connexion, String envParameters,
	    String executionEnv) throws Exception {
	try {
	    LoggerDispatcher.info("mettreAJourSchemaTableMetier", LOGGER);
	    /*
	     * Récupérer la table qui mappe : famille / table métier / variable métier et
	     * type de la variable
	     */
	    StringBuilder requeteRef = new StringBuilder(
		    "SELECT lower(id_famille), lower('" + AbstractPhaseService.dbEnv(executionEnv)
			    + "'||nom_table_metier), lower(nom_variable_metier), lower(type_variable_metier) FROM "
			    + envParameters + "_mod_variable_metier");

	    List<List<String>> relationalViewRef = Format.patch(
		    UtilitaireDao.get(DbConstant.POOL_NAME).executeRequestWithoutMetadata(connexion, requeteRef));
	    HierarchicalView familleToTableToVariableToTypeRef = HierarchicalView.asRelationalToHierarchical(
		    "(Réf) Famille -> Table -> Variable -> Type",
		    Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"),
		    relationalViewRef);
	    /*
	     * Récupérer dans le méta-modèle de la base les tables métiers correspondant à
	     * la famille chargée
	     */
	    StringBuilder requete = new StringBuilder(
		    "SELECT lower(id_famille), lower(table_schema||'.'||table_name) nom_table_metier, lower(column_name) nom_variable_metier");

	    // les types dans postgres sont horribles :(
	    // udt_name : float8 = float, int8=bigint, int4=int
	    // data_type : double precision = float, integer=int
	    requete.append(
		    ", case when lower(data_type)='array' then replace(replace(replace(ltrim(udt_name,'_'),'int4','int'),'int8','bigint'),'float8','float')||'[]' ");
	    requete.append(
		    "	else replace(replace(lower(data_type),'double precision','float'),'integer','int') end type_variable_metier ");
	    requete.append("\n FROM information_schema.columns, " + envParameters + "_famille ");
	    requete.append("\n WHERE table_schema='"
		    + ManipString.substringBeforeFirst(AbstractPhaseService.dbEnv(executionEnv), ".").toLowerCase()
		    + "' ");
	    requete.append("\n and table_name LIKE '"
		    + ManipString.substringAfterFirst(AbstractPhaseService.dbEnv(executionEnv), ".").toLowerCase()
		    + "mapping\\_%' ");
	    requete.append("\n and table_name LIKE '"
		    + ManipString.substringAfterFirst(AbstractPhaseService.dbEnv(executionEnv), ".").toLowerCase()
		    + "mapping\\_'||lower(id_famille)||'%';");

	    List<List<String>> relationalView = Format
		    .patch(UtilitaireDao.get(DbConstant.POOL_NAME).executeRequestWithoutMetadata(connexion, requete));

	    HierarchicalView familleToTableToVariableToType = HierarchicalView.asRelationalToHierarchical(
		    "(Phy) Famille -> Table -> Variable -> Type",
		    Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"),
		    relationalView);
	    StringBuilder requeteMAJSchema = new StringBuilder("BEGIN;\n");
	    /*
	     * AJOUT/MODIFICATION DES COLONNES DE REFERENCE
	     */
	    for (HierarchicalView famille : familleToTableToVariableToTypeRef.children()) {
		/**
		 * Pour chaque table de référence
		 */
		for (HierarchicalView table : famille.children()) {
		    /**
		     * Est-ce que la table existe physiquement ?
		     */
		    if (familleToTableToVariableToType.hasPath(famille, table)) {
			/**
			 * Pour chaque variable de référence
			 */
			for (HierarchicalView variable : table.children()) {
			    /*
			     * Si la variable*type n'existe pas
			     */
			    if (!familleToTableToVariableToType.hasPath(famille, table, variable,
				    variable.getUniqueChild())) {

				// BUG POSTGRES : pb drop et add column : recréer la table sinon ca peut excéder
				// la limite postgres de 1500
				requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + "_IMG ;");
				requeteMAJSchema.append("CREATE TABLE " + table.getLocalRoot() + "_IMG "
					+ FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM " + table.getLocalRoot() + ";");
				requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + " ;");
				requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + "_IMG RENAME TO "
					+ ManipString.substringAfterFirst(table.getLocalRoot(), ".") + ";");

				/*
				 * Si la variable existe
				 */
				if (familleToTableToVariableToType.hasPath(famille, table, variable)) {
				    /*
				     * Drop de la variable
				     */
				    requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " DROP COLUMN "
					    + variable.getLocalRoot() + ";");
				}
				/*
				 * Ajout de la variable
				 */
				requeteMAJSchema.append(
					"ALTER TABLE " + table.getLocalRoot() + " ADD COLUMN " + variable.getLocalRoot()
						+ " " + variable.getUniqueChild().getLocalRoot() + " ");
				if (variable.getUniqueChild().getLocalRoot().equals("text")) {
				    requeteMAJSchema.append(" collate \"C\" ");
				}
				requeteMAJSchema.append(";");

			    }
			}
		    } else {
			AttributeValue[] attr = new AttributeValue[table.children().size()];
			int i = 0;
			for (HierarchicalView variable : table.children()) {
			    attr[i++] = new AttributeValue(variable.getLocalRoot(),
				    variable.getUniqueChild().getLocalRoot());
			}
			requeteMAJSchema.append("CREATE TABLE " + table.getLocalRoot() + " (");
			for (int j = 0; j < attr.length; j++) {
			    if (j > 0) {
				requeteMAJSchema.append(", ");
			    }
			    requeteMAJSchema.append(attr[j].getFirst() + " " + attr[j].getSecond());
			    if (attr[j].getSecond().equals("text")) {
				requeteMAJSchema.append(" collate \"C\" ");
			    }
			}
			requeteMAJSchema.append(") " + FormatSQL.WITH_NO_VACUUM + ";\n");
		    }

		}
	    }
	    /*
	     * SUPPRESSION DES COLONNES QUI NE SONT PAS CENSEES EXISTER
	     */
	    for (HierarchicalView famille : familleToTableToVariableToType.children()) {
		/**
		 * Pour chaque table physique
		 */
		for (HierarchicalView table : familleToTableToVariableToType.get(famille).children()) {
		    /**
		     * Est-ce que la table devrait exister ?
		     */
		    if (!familleToTableToVariableToTypeRef.hasPath(famille, table)) {
			requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + ";\n");
		    } else {
			/**
			 * Pour chaque variable de cette table
			 */
			for (HierarchicalView variable : table.children()) {
			    /**
			     * Est-ce que la variable devrait exister ?
			     */
			    if (!familleToTableToVariableToTypeRef.hasPath(famille, table, variable))
				requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " DROP COLUMN "
					+ variable.getLocalRoot() + ";\n");
			}
		    }
		}
	    }
	    requeteMAJSchema.append("END;");
	    UtilitaireDao.get("arc").executeBlock(connexion, requeteMAJSchema);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw ex;
	}
    }

    /**
     * Delete to_delete tagged row in pilotage table
     *
     * @param connexion
     * @param tablePil
     * @throws Exception
     */
    public void cleanToDelete(Connection connexion, String tablePil) throws Exception {
	LoggerDispatcher.info("cleanToDelete", LOGGER);

	StringBuilder requete = new StringBuilder();
	requete.append("DELETE FROM " + tablePil + " a WHERE exists (select 1 from " + tablePil
		+ " b where b.to_delete='1' and a.id_source=b.id_source and a.container=b.container); ");
	UtilitaireDao.get("arc").executeBlock(connexion, requete);
    }

    /**
     * 
     * Delete ine pilotage table useless files
     * <ul>
     * <li>a copy of the data have to be send to each RG1 client</li>
     * <li>a file with a last transfer older than 7 days (RG2)</li>
     * </ul>
     *
     * @param connexion
     * @param tablePil
     * @param tablePil
     * @throws SQLException
     */
    public void cleanPilotagetable(Connection connexion, String executionEnv) throws Exception {

	LoggerDispatcher.info("nettoyerTablePilotage", LOGGER);

	String nomTablePilotage = dbEnv(executionEnv) + "pilotage_fichier";
	String nomTableArchive = dbEnv(executionEnv) + "pilotage_archive";

	StringBuilder requete = new StringBuilder();

	requete.append("DROP TABLE IF EXISTS fichier_to_delete; ");
	requete.append("CREATE TEMPORARY TABLE fichier_to_delete AS ");
	requete.append("WITH ")

		// 1. on récupère sous forme de tableau les clients de chaque famille
		.append("clientsParFamille AS ( ").append("SELECT array_agg(id_application) as client, id_famille ")
		.append("FROM arc.ihm_client ").append("GROUP BY id_famille ").append(") ")

		// 2. on fait une première selection des fichiers candidats au Delete
		.append(",isFichierToDelete AS (	 ").append("SELECT id_source, container, date_client ")
		.append("FROM ").append(nomTablePilotage).append(" a ").append(", arc.ihm_norme b ")
		.append(", clientsParFamille c ")
		.append("WHERE a.phase_traitement='" + TypeTraitementPhase.FORMAT_TO_MODEL + "' ")
		.append("AND a.etat_traitement='{" + TraitementState.OK + "}' ").append("AND a.client is not null ")
		.append("AND a.id_norme=b.id_norme ").append("AND a.periodicite=b.periodicite ")
		.append("AND b.id_famille=c.id_famille ")
		// on filtre selon RG1
		.append("AND (a.client <@ c.client AND c.client <@ a.client) ")
		// test d'égalité des 2 tableaux (a.client,c.client)
		.append(") ")
		// par double inclusion (A dans B & B dans A)

		// 3. on selectionne les fichiers éligibles
		.append("SELECT id_source, container FROM (SELECT unnest(date_client) as t, id_source, container FROM isFichierToDelete) ww ")
		.append("GROUP BY id_source, container ")
		// on filtre selon RG2
		.append("HAVING (current_date - max(t) ::date ) >=" + TIME_TO_KEEP + " ").append("; ");

	UtilitaireDao.get("arc").executeRequest(connexion, requete);

	// requete sur laquelle on va itérer : on selectionne un certain nombre de
	// fichier et on itere
	requete = new StringBuilder();

	// 3b. on selectionne les fichiers éligibles et on limite le nombre de retour
	// pour que l'update ne soit pas trop massif (perf)
	requete.append("WITH fichier_to_delete_limit AS ( ")
		.append(" SELECT * FROM fichier_to_delete LIMIT " + NB_FICHIER_PER_ARCHIVE + " ").append(") ")

		// 4. suppression des archive de la table d'archive (bien retirer le nom de
		// l'entrepot du début du container)
		.append(",delete_archive AS (").append("DELETE FROM ").append(nomTableArchive).append(" a ")
		.append("USING fichier_to_delete_limit b ")
		.append("WHERE a.nom_archive=substring(b.container,strpos(b.container,'_')+1) ").append("returning *) ")

		// 5. suppression des fichier de la table de pilotage
		.append(",delete_idsource AS (").append("DELETE FROM ").append(nomTablePilotage).append(" a ")
		.append("USING fichier_to_delete_limit b ").append("WHERE a.id_source=b.id_source ").append(") ")

		// 5b. suppression de la tgable des fichiers eligibles
		.append(",delete_source as (DELETE FROM fichier_to_delete a using fichier_to_delete_limit b where row(a.id_source,a.container)::text=row(b.id_source,b.container)::text) ")
		// 6. récuperer la liste des archives
		.append("SELECT entrepot, nom_archive FROM delete_archive ");

	// initialisation de la liste contenant les archives à déplacer
	HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>();
	m.put("entrepot", new ArrayList<String>());
	m.put("nom_archive", new ArrayList<String>());

	HashMap<String, ArrayList<String>> n = new HashMap<String, ArrayList<String>>();

	// on continue jusqu'a ce qu'on ne trouve plus rien à effacer
	do {
	    // récupérer le résultat de la requete
	    System.out.println("Archivage de " + NB_FICHIER_PER_ARCHIVE + " fichiers - Début");
	    n = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requete)).mapContent();

	    // ajouter à la liste m les enregistrements qu'ils n'existent pas déjà dans m

	    // on parcours n
	    if (!n.isEmpty()) {
		for (int k = 0; k < n.get("entrepot").size(); k++) {
		    boolean toInsert = true;

		    // vérifier en parcourant m si on doit réaliser l'insertion
		    for (int l = 0; l < m.get("entrepot").size(); l++) {
			if (n.get("entrepot").get(k).equals(m.get("entrepot").get(l))
				&& n.get("nom_archive").get(k).equals(m.get("nom_archive").get(l))) {
			    toInsert = false;
			    break;
			}
		    }

		    // si aprés avoir parcouru tout m, l'enreigstrement de n n'est pas trouvé on
		    // l'insere
		    if (toInsert) {
			m.get("entrepot").add(n.get("entrepot").get(k));
			m.get("nom_archive").add(n.get("nom_archive").get(k));
		    }

		}
	    }
	    System.out.println("Archivage Fin");

	} while (UtilitaireDao.get("arc").hasResults(connexion, "select 1 from fichier_to_delete limit 1"));

	// y'a-til des choses à faire ?
	if (m.get("entrepot").size() > 0) {

	    // 7. Déplacer les archives effacées dans le répertoire de sauvegarde "OLD"
	    String repertoire = properties.getBatchParametreRepertoire();
	    String envDir = this.executionEnv.replace(".", "_").toUpperCase();

	    String entrepotSav = "";
	    for (int i = 0; i < m.get("entrepot").size(); i++) {
		String entrepot = m.get("entrepot").get(i);
		String archive = m.get("nom_archive").get(i);
		String dirIn = repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + entrepot
			+ ARCHIVE;
		String dirOut = repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + entrepot
			+ ARCHIVE + File.separator + "OLD";

		// création du répertoire "OLD" s'il n'existe pas
		if (!entrepotSav.equals(entrepot)) {
		    File f = new File(dirOut);
		    if (!f.exists()) {
			f.mkdir();
		    }
		    entrepotSav = entrepot;
		}

		// déplacement de l'archive de dirIn vers dirOut
		ApiReceptionService.deplacerFichier(dirIn, dirOut, archive, archive);

	    }

	    requete.setLength(0);
	    requete.append("vacuum analyze " + nomTablePilotage + "; ");
	    requete.append("vacuum analyze " + nomTableArchive + "; ");
	    UtilitaireDao.get("arc").executeImmediate(connexion, requete);
	}

    }

    /**
     * Copy global parameters table to environement
     *
     * @param connexion
     * @param anParametersEnvironment
     * @param anExecutionEnvironment
     * @throws Exception
     */

    public static void copyTablesToExecution(Connection connexion, String anParametersEnvironment,
	    String anExecutionEnvironment) {
	try {
	    LoggerDispatcher.info("Recopie des regles dans l'environnement", LOGGER);
	    copyTablesToExecutionThrow(connexion, anParametersEnvironment, anExecutionEnvironment);
	} catch (Exception e) {
	    LoggerDispatcher.info("Erreur copyTablesToExecution", LOGGER);
	}
    }

    @SQLExecutor
    public static void copyTablesToExecutionThrow(Connection connexion, String anParametersEnvironment,
	    String anExecutionEnvironment) throws Exception {
	LoggerDispatcher.info("copyTablesToExecution", LOGGER);
	try {
	    StringBuilder requete = new StringBuilder();
	    TraitementTableParametre[] listeTableParamettre = TraitementTableParametre.values();
	    StringBuilder condition = new StringBuilder();
	    String modaliteEtat = anExecutionEnvironment.replace("_", ".");
	    String tablePil = AbstractPhaseService.dbEnv(anExecutionEnvironment)
		    + TraitementTableExecution.PILOTAGE_FICHIER;
	    String tableImage;
	    String tableCurrent;
	    for (int i = 0; i < listeTableParamettre.length; i++) {
		// on créé une table image de la table venant de l'ihm
		// (environnement de parametre)
		tableCurrent = AbstractPhaseService.dbEnv(anExecutionEnvironment) + listeTableParamettre[i];
		tableImage = FormatSQL.temporaryTableName(
			AbstractPhaseService.dbEnv(anExecutionEnvironment) + listeTableParamettre[i]);

		// recopie partielle (en fonction de l'environnement
		// d'exécution)
		// pour les tables JEUDEREGLE, CONTROLE_REGLE et MAPPING_REGLE
		condition.setLength(0);
		if (listeTableParamettre[i] == TraitementTableParametre.NORME) {
		    condition.append(" WHERE etat='1'");
		}
		if (listeTableParamettre[i] == TraitementTableParametre.CALENDRIER) {
		    condition.append(" WHERE etat='1' ");
		    condition.append(" and exists (select 1 from " + anParametersEnvironment
			    + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
		}
		if (listeTableParamettre[i] == TraitementTableParametre.JEUDEREGLE) {
		    condition.append(" WHERE etat=lower('" + modaliteEtat + "')");
		    condition.append(" and exists (select 1 from " + anParametersEnvironment
			    + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
		    condition.append(" and exists (select 1 from " + anParametersEnvironment
			    + "_calendrier b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
		}
		if (listeTableParamettre[i] == TraitementTableParametre.CHARGEMENT_REGLE //
			|| listeTableParamettre[i] == TraitementTableParametre.NORMAGE_REGLE //
			|| listeTableParamettre[i] == TraitementTableParametre.CONTROLE_REGLE//
			|| listeTableParamettre[i] == TraitementTableParametre.MAPPING_REGLE //
			|| listeTableParamettre[i] == TraitementTableParametre.FILTRAGE_REGLE //
//			|| listeTableParamettre[i] == TraitementTableParametre.PARAMETTRAGE_ORDRE_PHASE
			) 
		{
		    condition.append(" WHERE exists (select 1 from " + anParametersEnvironment
			    + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
		    condition.append(" and exists (select 1 from " + anParametersEnvironment
			    + "_calendrier b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
		    condition.append(" and exists (select 1 from " + anParametersEnvironment
			    + "_jeuderegle b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup AND a.version=b.version and b.etat=lower('"
			    + modaliteEtat + "'");
		    condition.append("))");
//		    if (listeTableParamettre[i] == TraitementTableParametre.PARAMETTRAGE_ORDRE_PHASE) {
//			condition.append(" OR id_norme ISNULL");
//		    }
		}
		requete.append(FormatSQL.dropTable(tableImage).toString());
		requete.append("CREATE TABLE " + tableImage + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT a.* FROM "
			+ anParametersEnvironment + "_" + listeTableParamettre[i] + " AS a " + condition + ";\n");
		// Identifier les changements en comparant la table image et la
		// table courante dans l'environnement
		// Marquer les changement dans la norme (changement de nom,
		// changement de définition ou disparition)
		if (!UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists(tableCurrent))) {
		    requete.append("CREATE TABLE " + tableCurrent + " " + FormatSQL.WITH_NO_VACUUM
			    + " AS SELECT a.* FROM " + anParametersEnvironment + "_" + listeTableParamettre[i]
			    + " AS a " + condition + ";\n");
		}
		if (listeTableParamettre[i] == TraitementTableParametre.NORME) {
		    requete.append("with prep as ( ");
		    requete.append("	select * from ( ");
		    requete.append(
			    "		select b.id_norme as id_norme_new, b.def_validite as def_validite_new, b.def_norme as def_norme_new, a.id_norme, a.def_validite, a.def_norme ");
		    requete.append("		,case when a.id_norme!=b.id_norme then 1 else 0 end as chgt_norme ");
		    requete.append(
			    "		,case when a.def_norme!=b.def_norme or a.def_validite!=b.def_validite then 1 else 0 end as chgt_def ");
		    requete.append(
			    "		,case when a.id_norme is not null and b.id_norme is null then 1 else 0 end as erase_norme ");
		    requete.append("		FROM " + tableImage + " b FULL OUTER JOIN " + tableCurrent + " a ");
		    requete.append("		ON a.id=b.id ");
		    requete.append(") u ");
		    requete.append("where chgt_norme+chgt_def+erase_norme>0 ) ");
		    requete.append("UPDATE " + tablePil + " a ");
		    requete.append("set id_norme=case when chgt_norme=1 then b.id_norme_new else a.id_norme end ");
		    requete.append(
			    ",rapport=case when chgt_def=1 and phase_traitement='" + TypeTraitementPhase.STRUCTURIZE_XML
				    + "' then '" + TraitementRapport.INITIALISATION_CHGT_DEF_NORME + "' ");
		    requete.append("when erase_norme='1'  and phase_traitement='" + TypeTraitementPhase.STRUCTURIZE_XML
			    + "' then '" + TraitementRapport.INITIALISATION_NORME_OBSOLETE + "' ");
		    requete.append("else a.rapport end ");
		    requete.append("FROM prep b ");
		    requete.append("WHERE a.id_norme=b.id_norme ");
		    requete.append("and exists (select 1 from prep); \n");
		}
		// Marquer les changement dans le calendrier (hors validite)
		if (listeTableParamettre[i] == TraitementTableParametre.CALENDRIER) {
		    requete.append("with prep as ( ");
		    requete.append("select * from (");
		    requete.append(
			    "select b.id_norme as id_norme_new, b.periodicite as periodicite_new, b.validite_inf as validite_inf_new, b.validite_sup as validite_sup_new ");
		    requete.append(",a.id_norme, a.periodicite, a.validite_inf, a.validite_sup ");
		    requete.append(
			    ",case when a.validite_inf!=b.validite_inf or a.validite_sup!=b.validite_sup then 1 else 0 end as chgt_cal ");
		    requete.append(
			    ",case when a.id_norme is not null and b.id_norme is null then 1 else 0 end as erase_cal ");
		    requete.append("FROM " + tableImage + " b FULL OUTER JOIN " + tableCurrent + " a ");
		    requete.append("ON a.id=b.id ");
		    requete.append(") u ");
		    requete.append("where chgt_cal+erase_cal>0 ");
		    requete.append(") ");
		    requete.append("UPDATE " + tablePil + " a ");
		    requete.append("set rapport= ");
		    requete.append(
			    "case when erase_cal=1 then '" + TraitementRapport.CONTROLE_CALENDRIER_OBSOLETE + "' ");
		    requete.append(
			    "when a.validite::date>b.validite_sup_new or a.validite::date<b.validite_inf_new then '"
				    + TraitementRapport.CONTROLE_VALIDITE_HORS_CALENDRIER + "' ");
		    requete.append("else a.rapport end ");
		    requete.append("from prep b ");
		    requete.append("where a.id_norme=b.id_norme ");
		    requete.append("and a.periodicite=b.periodicite ");
		    requete.append("and a.validite_inf=b.validite_inf ");
		    requete.append("and a.validite_sup=b.validite_sup ");
		    requete.append("and a.phase_traitement='" + TypeTraitementPhase.CONTROL + "' ");
		    requete.append("and exists (select 1 from prep); \n ");
		}
		// Une fois que c'est fait, on drop la table courante et on la
		// remplace par la table image
		requete.append(FormatSQL.dropTable(tableCurrent).toString());
		requete.append("ALTER TABLE " + tableImage + " rename to "
			+ ManipString.substringAfterLast(tableCurrent, ".") + "; \n");
	    }
	    UtilitaireDao.get("arc").executeBlock(connexion, requete);

	    // Dernière étape : recopie des tables de nomenclature et des tables prefixées
	    // par ext_ du schéma arc vers schéma courant

	    requete.setLength(0);

	    // 1.Préparation des requêtes de suppression des tables nmcl_ et ext_ du schéma
	    // courant

	    String requeteSelectDrop = " SELECT 'DROP TABLE IF EXISTS '||schemaname||'.'||tablename||';'  AS requete_drop"
		    + " FROM pg_tables where schemaname = '" + anExecutionEnvironment.toLowerCase() + "'"
		    + " AND tablename SIMILAR TO '%nmcl%|%ext%'";

	    ArrayList<String> requetesDeSuppressionTablesNmcl = new GenericBean(
		    UtilitaireDao.get("arc").executeRequest(connexion, requeteSelectDrop)).mapContent()
			    .get("requete_drop");

	    if (requetesDeSuppressionTablesNmcl != null) {
		for (String requeteDeSuppression : requetesDeSuppressionTablesNmcl) {
		    requete.append("\n ").append(requeteDeSuppression);
		}
	    }

	    // 2.Préparation des requêtes de création des tables
	    ArrayList<String> requetesDeCreationTablesNmcl = new GenericBean(UtilitaireDao.get("arc").executeRequest(
		    connexion,
		    "select tablename from pg_tables where (tablename like 'nmcl\\_%' OR tablename like 'ext\\_%') and schemaname='arc'"))
			    .mapContent().get("tablename");

	    if (requetesDeCreationTablesNmcl != null) {
		for (String tableName : requetesDeCreationTablesNmcl) {
		    requete.append("\n CREATE TABLE " + AbstractPhaseService.dbEnv(anExecutionEnvironment) + tableName
			    + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM arc." + tableName + ";");
		}
	    }

	    // 3.Execution du script Sql de suppression/création
	    UtilitaireDao.get("arc").executeBlock(connexion, requete);

	} catch (Exception e) {
	    LoggerDispatcher.info(
		    "Problème lors de la copie des tables vers l'environnement : " + anExecutionEnvironment, LOGGER);
	    LoggerDispatcher.info(e.getMessage().toString(), LOGGER);
	    e.printStackTrace();
	    throw e;
	}
    }

    /**
     * Set the data base in previous phase state Clean table_ok/_ko and update
     * pilotage table
     *
     * @param phase
     * @param querySelection
     * @param listEtat
     */
    public void backToPreviousPhase(TypeTraitementPhase phase, String querySelection, List<TraitementState> listEtat) {
	LoggerDispatcher.info("Retour arrière pour la phase :" + phase, LOGGER);
	StringBuilder requete = new StringBuilder();
	// Update pilotage table
	Integer nbLignes = 0;

	for (TypeTraitementPhase phaseNext : phase.nextPhases()) {
	    requete.setLength(0);
	    requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.getTablePil() + " WHERE phase_traitement = '"
		    + phaseNext + "' ");
	    if (querySelection != null) {
		requete.append("AND " + FormatSQL.writeInQuery("id_source", querySelection));
	    }
	    requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
	    nbLignes = nbLignes + UtilitaireDao.get("arc").getInt(this.connection, requete);
	}

	requete.setLength(0);
	requete.append(
		"WITH TMP_DELETE AS (DELETE FROM " + this.getTablePil() + " WHERE phase_traitement = '" + phase + "' ");
	if (querySelection != null) {
	    requete.append("AND " + FormatSQL.writeInQuery("id_source", querySelection));
	}
	requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
	nbLignes = nbLignes + UtilitaireDao.get("arc").getInt(this.connection, requete);

	try {
	    synchroniserEnvironmentByPilotage(this.connection, this.executionEnv);
	} catch (Exception e) {
	    LoggerDispatcher.info("error in synchroniserEnvironmentByPilotage()", LOGGER);

	}

	if (nbLignes > 0) {
	    pilotageMaintenance(this.connection, this.executionEnv, "");
	}

	// Penser à tuer la connexion
    }

    public void resetEnvironnement() {
	try {
	    synchroniserEnvironmentByPilotage(this.connection, this.executionEnv);
	    pilotageMaintenance(this.connection, this.executionEnv, "");
	} catch (Exception e) {
	    LoggerDispatcher.info("error in resetEnvironnement()", LOGGER);
	}
    }

    /**
     * Get all the "temporary" (not sql temporary, but table tag as temporary) table
     * of an env
     * 
     * @param env
     * @return
     */
    public StringBuilder requeteListAllTablesEnvTmp() {
	StringBuilder requete = new StringBuilder();
	TypeTraitementPhase[] phase = TypeTraitementPhase.values();
	// Begin after initialize phase i=2
	for (int i = 2; i < phase.length; i++) {
	    if (i > 2) {
		requete.append(" UNION ALL ");
	    }
	    requete.append(
		    FormatSQL.tableExists(AbstractPhaseService.dbEnv(this.executionEnv) + phase[i] + "$%$tmp$%", " "));
	    requete.append(" UNION ALL ");
	    requete.append(FormatSQL
		    .tableExists(AbstractPhaseService.dbEnv(this.executionEnv) + phase[i] + "\\_%$tmp$%", " "));
	}
	return requete;
    }

    /**
     * Get all table of an environnement recupere toutes les tables d'état d'un
     * envrionnement
     *
     * @param env
     * @return
     */
    public static StringBuilder requeteListAllTablesEnv(String env) {
	StringBuilder requete = new StringBuilder();
	TypeTraitementPhase[] phase = TypeTraitementPhase.values();
	Boolean insert = false;
	String r;

	for (int i = 0; i < phase.length; i++) {
	    if (insert) {
		requete.append(" UNION ALL ");
	    }
	    r = requsteListTableEnv(env, phase[i].toString()).toString();
	    insert = (r.length() != 0);
	    requete.append(r);
	}

	return requete;
    }

    /**
     * Generate a sql request to get all temporary table of a phase in an env
     * 
     * @param env
     * @param phase
     * @return an sql query
     */
    public static StringBuilder requsteListTableEnv(String env, String phase) {
	// Les tables dans l'environnement sont de la forme
	TraitementState[] etat = TraitementState.values();
	StringBuilder requete = new StringBuilder();
	for (int j = 0; j < etat.length; j++) {
	    if (!etat[j].equals(TraitementState.ENCOURS)) {
		if (j > 0) {
		    requete.append(" UNION ALL ");
		}
		requete.append(
			FormatSQL.tableExists(AbstractPhaseService.dbEnv(env) + "%" + phase + "%\\_" + etat[j], " "));
	    }
	}
	return requete;
    }

    /**
     * Consistence between data tables and pilotage tables
     *
     * @param connexion
     * @param executionEnv
     * @throws Exception
     */
    @SQLExecutor
    public void synchroniserEnvironmentByPilotage(Connection connexion, String executionEnv) throws Exception {
	LoggerDispatcher.info("synchronisationEnvironmentByPilotage", LOGGER);
	try {
	    // maintenance de la table de pilotage
	    // retirer les "encours" de la table de pilotage
	    LoggerDispatcher.info("** Maintenance table de pilotage **", LOGGER);
	    UtilitaireDao.get("arc").executeBlock(connexion, "alter table " + this.getTablePil()
		    + " alter column date_entree type text COLLATE pg_catalog.\"C\"; ");
	    UtilitaireDao.get("arc").executeBlock(connexion,
		    "delete from " + this.getTablePil() + " where etat_traitement='{ENCOURS}';");

	    // pour chaque fichier de la phase de pilotage, remet à etape='1' pour sa
	    // derniere phase valide
	    remettreEtapePilotage();

	    // recrée la table de pilotage, ses index, son trigger
	    rebuildPilotage(connexion, this.getTablePil());

	    // drop des tables temporaires
	    GenericBean g = new GenericBean(
		    UtilitaireDao.get("arc").executeRequest(connexion, requeteListAllTablesEnvTmp()));
	    if (!g.mapContent().isEmpty()) {
		ArrayList<String> envTables = g.mapContent().get("table_name");
		for (String nomTable : envTables) {
		    UtilitaireDao.get("arc").executeBlock(connexion, FormatSQL.dropTable(nomTable).toString());
		}
	    }

	    // pour chaque table de l'environnement d'execution courant
	    g = new GenericBean(
		    UtilitaireDao.get("arc").executeRequest(connexion, requeteListAllTablesEnv(executionEnv)));
	    if (!g.mapContent().isEmpty()) {
		ArrayList<String> envTables = g.mapContent().get("table_name");
		for (String nomTable : envTables) {

		    String phase = ManipString.substringBeforeFirst(nomTable.substring(executionEnv.length() + 1), "_")
			    .toUpperCase();
		    String etat = ManipString.substringAfterLast(nomTable, "_").toUpperCase();

		    // la table a-t-elle des héritages ?
		    HashMap<String, ArrayList<String>> m = new GenericBean(
			    UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(connexion,
				    "SELECT schemaname||'.'||tablename as tablename FROM pg_tables WHERE schemaname||'.'||tablename like '"
					    + nomTable + "\\_child\\_%'")).mapContent();

		    StringBuilder query = new StringBuilder();
		    // Oui elle a des héritages
		    if (!m.isEmpty()) {

			// on parcourt les tables héritées
			for (String t : m.get("tablename")) {
			    // on récupère la variable etape dans la phase
			    // si on ne trouve la source de la table dans la phase, on drop !
			    String etape = UtilitaireDao.get(DbConstant.POOL_NAME).getString(connexion,
				    "SELECT etape FROM " + getTablePil() + " WHERE phase_traitement='" + phase
					    + "' AND '" + etat
					    + "'=ANY(etat_traitement) AND id_source=(select id_source from " + t
					    + " limit 1)");

			    if (etape == null) {
				query.append("\n DROP TABLE IF EXISTS " + t + ";");
			    } else {
				// si on ne trouve pas la table dans la phase en etape=1, on détruit le lien
				// avec todo
				if (!etape.equals("1")) {
				    query.append(FormatSQL.tryQuery("\n ALTER TABLE " + t + " NO INHERIT "
					    + ManipString.substringBeforeFirst(t, "_child_") + "_todo;"));
				} else
				// sinon on pose le lien (etape 1 ou 2)
				{
				    query.append(FormatSQL.tryQuery("\n ALTER TABLE " + t + " INHERIT "
					    + ManipString.substringBeforeFirst(t, "_child_") + "_todo;"));
				}
			    }
			}
			UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(connexion, query);

		    } else {

			UtilitaireDao.get("arc").executeBlock(this.connection,
				deleteTableByPilotage(nomTable, nomTable, this.getTablePil(), phase, etat, ""));
			UtilitaireDao.get("arc").executeImmediate(connexion,
				"set default_statistics_target=1; vacuum analyze " + nomTable
					+ "(id_source); set default_statistics_target=100;");

			if (!nomTable.contains(TypeTraitementPhase.FORMAT_TO_MODEL.toString().toLowerCase())
				&& nomTable.endsWith("_" + TraitementState.OK.toString().toLowerCase())) {
			    UtilitaireDao.get("arc").executeBlock(this.connection, deleteTableByPilotage(
				    nomTable + "_todo", nomTable, this.getTablePil(), phase, etat, "AND etape=1"));
			    UtilitaireDao.get("arc").executeImmediate(connexion,
				    "set default_statistics_target=1; vacuum analyze " + nomTable
					    + "_todo (id_source); set default_statistics_target=100;");
			}

		    }
		}

	    }

	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, ex, "synchroniserEnvironnementByPilotage()");
	    throw ex;
	}

	UtilitaireDao.get(DbConstant.POOL_NAME).maintenancePgCatalog(this.connection, "full");

    }

    public static void rebuildPilotage(Connection connexion, String tablePilotage) throws SQLException {
	UtilitaireDao.get("arc").executeBlock(connexion,
		FormatSQL.rebuildTableAsSelectWhere(tablePilotage, "true",
			"create index idx1_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (id_source);",
			"create index idx2_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (phase_traitement, etat_traitement, etape);",
			"create index idx3_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (date_entree);",
			"create index idx4_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (rapport) where rapport is not null;",
			"create index idx5_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (o_container,v_container);",
			"create index idx6_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on "
				+ tablePilotage + " (to_delete);",
			"CREATE TRIGGER tg_pilotage_fichier_calcul AFTER INSERT OR UPDATE OR DELETE ON " + tablePilotage
				+ " FOR EACH ROW EXECUTE PROCEDURE arc.transpose_pilotage_calcul();",
			"CREATE TRIGGER tg_pilotage_fichier_fin AFTER INSERT OR UPDATE OR DELETE ON " + tablePilotage
				+ " FOR EACH STATEMENT EXECUTE PROCEDURE arc.transpose_pilotage_fin();"));
	UtilitaireDao.get("arc").executeBlock(connexion, "analyze " + tablePilotage + ";");
    }

    /**
     * la variable etape indique si c'est bien l'etape à considerer pour traitement
     * ou pas etape='1' : phase à considerer, sinon etape='0'
     *
     * @return
     * @throws SQLException
     */
    public boolean remettreEtapePilotage() throws SQLException {

	StringBuilder requete = new StringBuilder();
	requete.append("WITH tmp_1 as (select id_source, max(");

	new StringBuilder();
	requete.append("case ");

	for (TypeTraitementPhase p : TypeTraitementPhase.values()) {
	    requete.append("when phase_traitement='" + p.toString() + "' then " + p.ordinal() + " ");
	}
	requete.append("end ) as p ");
	requete.append("FROM " + this.getTablePil() + " ");
	requete.append("GROUP BY id_source ");
	requete.append("having max(etape)=0 ) ");
	requete.append("update " + this.getTablePil() + " a ");
	requete.append("set etape=1 ");
	requete.append("from tmp_1 b ");
	requete.append("where a.id_source=b.id_source ");
	requete.append("and a.phase_traitement= case ");
	for (TypeTraitementPhase p : TypeTraitementPhase.values()) {
	    requete.append("when p=" + p.ordinal() + " then '" + p.toString() + "' ");
	}
	requete.append("end ; ");

	UtilitaireDao.get("arc").executeBlock(this.connection, requete);

	return true;
    }

    public boolean nettoyerTableBufferColonne(String nomTable) throws Exception {
	return nettoyerTableColonne(nomTable + "_todo");
    }

    /**
     * Destruction des colonnes inutiles de la table si elle est vide
     * 
     * @param nomTable
     * @return
     * @throws Exception
     */
    @SQLExecutor
    public boolean nettoyerTableColonne(String nomTable) throws Exception {
	LoggerDispatcher.info("nettoyage en colonne :" + nomTable, LOGGER);
	Boolean destroyedColumn = false;
	StringBuilder requeteDestruction = new StringBuilder();

	/** nettoyage en colonne : on va supprimer les colonnes inutiles **/
	if (!(nomTable.toLowerCase().contains("_" + TypeTraitementPhase.FORMAT_TO_MODEL.toString().toLowerCase() + "_")
		&& nomTable.toLowerCase().endsWith("_ok"))) {
	    // ArrayList<Integer> r=new ArrayList<Integer>();

	    // si pas d'enregistrement, on cherche pas à faire les stats et on detruit les
	    // colonnes directement
	    if (!UtilitaireDao.get("arc").hasResults(this.connection, "select 1 from " + nomTable + " limit 1;")) {

		destroyedColumn = true;

		ArrayList<String> l = UtilitaireDao.get(DbConstant.POOL_NAME).listeCol(connection, nomTable);

		for (int i = 0; i < l.size(); i++) {
		    String c = l.get(i);
		    if (c.toLowerCase().startsWith("i_") || c.toLowerCase().startsWith("v_")
			    || c.toLowerCase().startsWith("m_")) {
			LoggerDispatcher.info("Destruction de la colonne :" + c + " de la table " + nomTable, LOGGER);
			requeteDestruction.append("alter table " + nomTable + " drop column " + c + " cascade; \n");
		    }
		}
	    }

	}

	if (destroyedColumn) {
	    UtilitaireDao.get("arc").executeRequest(this.connection, requeteDestruction);

	    // patch postgres pour reset l'index de colonne (posgres ne fait pas -1 quand on
	    // drop une colonne)
	    UtilitaireDao.get("arc").executeRequest(this.connection,
		    "DROP TABLE IF EXISTS " + nomTable + "_TMP CASCADE;" + " CREATE TABLE " + nomTable + "_TMP "
			    + FormatSQL.WITH_NO_VACUUM + " as select * from " + nomTable + "; "
			    + " DROP TABLE IF EXISTS " + nomTable + " CASCADE; " + " ALTER TABLE " + nomTable
			    + "_TMP rename to " + ManipString.substringAfterFirst(nomTable, ".") + "; ");

	}

	return destroyedColumn;
    }

    /**
     * Enleve les lignes d'une table en fonction du contenu dans la table de
     * pilotage
     *
     * @param executionEnv
     *            , bac à sable(bas) ou batch
     * @param phaseSel
     *            , phase servant de sélection
     * @param nomTable
     *            , nom de la table que doit être nettoyé
     * @return
     * @throws Exception
     */
    public Boolean nettoyerTableLigne(String executionEnv, String nomTable) throws Exception {
	LoggerDispatcher.info("nettoyer en ligne :" + nomTable, LOGGER);
	// On retrouve la phase et l'état à vérifier à partir du nom de la table
	String phase = ManipString.substringBeforeFirst(nomTable.substring(executionEnv.length() + 1), "_")
		.toUpperCase();
	String etat = ManipString.substringAfterLast(nomTable, "_").toUpperCase();

	try {
	    StringBuilder requete = new StringBuilder();
	    // effacer les enregistrements present dans la table de stockage dont on ne
	    // trouve plus référence dans le pilotage
	    requete.append(deleteTableByPilotage(nomTable, nomTable, this.getTablePil(), phase, etat, ""));
	    UtilitaireDao.get("arc").executeBlock(this.connection, requete);

	} catch (Exception e) {
	    LoggerDispatcher.error("nettoyerTableLigne()", e, LOGGER);
	    throw e;
	}

	return true;

    }

    // rebuild de la table buffer relative à la table
    /**
     * rebuild de la table todo
     * 
     * @throws Exception
     */
    public Boolean nettoyerTableBufferLigne(String executionEnv, String nomTable) throws Exception {

	LoggerDispatcher.info("nettoyer en ligne :" + nomTable, LOGGER);
	// On retrouve la phase et l'état à vérifier à partir du nom de la table
	String phase = ManipString.substringBeforeFirst(nomTable.substring(executionEnv.length() + 1), "_")
		.toUpperCase();
	String etat = ManipString.substringAfterLast(nomTable, "_").toUpperCase();

	try {
	    StringBuilder requete = new StringBuilder();
	    // effacer les enregistrements present dans la table de stockage dont on ne
	    // trouve plus référence dans le pilotage

	    requete.append(deleteTableByPilotage(nomTable + "_todo", nomTable, this.getTablePil(), phase, etat,
		    "AND etape=1"));

	    UtilitaireDao.get("arc").executeBlock(this.connection, requete);
	} catch (Exception e) {
	    LoggerDispatcher.error("nettoyerTableBufferLigne()", e, LOGGER);
	    throw e;
	}

	return true;

    }

    /**
     * Rebuild des grosses tables attention si on touche parameteres de requetes ou
     * à la clause exists; forte volumétrie !
     */
    public static String deleteTableByPilotage(String nomTable, String nomTableSource, String tablePil, String phase,
	    String etat, String extraCond) {
	StringBuilder requete = new StringBuilder();

	String tableDestroy = FormatSQL.temporaryTableName(nomTable, "D");
	requete.append("\n SET enable_nestloop=off; ");

	requete.append("\n DROP TABLE IF EXISTS " + tableDestroy + " CASCADE; ");
	requete.append("\n DROP TABLE IF EXISTS TMP_SOURCE_SELECTED CASCADE; ");

	// PERF : selection des id_source dans une table temporaire pour que postgres
	// puisse partir en semi-hash join
	requete.append("\n CREATE TEMPORARY TABLE TMP_SOURCE_SELECTED AS ");
	requete.append("\n SELECT id_source from " + tablePil + " ");
	requete.append("\n WHERE phase_traitement='" + phase + "' ");
	requete.append("\n AND '" + etat + "'=ANY(etat_traitement) ");
	requete.append("\n " + extraCond + " ");
	requete.append("\n ; ");

	requete.append("\n ANALYZE TMP_SOURCE_SELECTED; ");

	requete.append("\n CREATE  TABLE " + tableDestroy + " " + FormatSQL.WITH_NO_VACUUM + " ");
	requete.append("\n AS select * from " + nomTableSource + " a ");
	requete.append("\n WHERE exists (select 1 from TMP_SOURCE_SELECTED b WHERE a.id_source=b.id_source) ");
	requete.append("\n ; ");

	requete.append("\n DROP TABLE IF EXISTS " + nomTable + " CASCADE; ");
	requete.append("\n ALTER TABLE " + tableDestroy + " rename to " + ManipString.substringAfterFirst(nomTable, ".")
		+ ";\n");

	requete.append("\n DROP TABLE IF EXISTS TMP_SOURCE_SELECTED; ");

	requete.append("\n SET enable_nestloop=on; ");

	return requete.toString();

    }

    @SQLExecutor
    public static void clearPilotageAndDirectories(String repertoire, String env) throws Exception {
	try {
	    UtilitaireDao.get("arc").executeBlock(null, "truncate " + dbEnv(env) + "pilotage_fichier; ");
	    UtilitaireDao.get("arc").executeBlock(null, "truncate " + dbEnv(env) + "pilotage_fichier_t; ");
	    UtilitaireDao.get("arc").executeBlock(null, "truncate " + dbEnv(env) + "pilotage_archive; ");

	    String envDir = env.replace(".", "_").toUpperCase();

	    if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists(ARC_IHM_ENTREPOT))) {
		ArrayList<String> entrepotList = new GenericBean(
			UtilitaireDao.get("arc").executeRequest(null, "select id_entrepot from arc.ihm_entrepot"))
				.mapContent().get("id_entrepot");
		if (entrepotList != null) {
		    for (String s : entrepotList) {
			FileUtils.cleanDirectory(new File(
				repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + s));
			FileUtils.cleanDirectory(new File(repertoire + envDir + File.separator
				+ TypeTraitementPhase.REGISTER + "_" + s + ARCHIVE));
		    }
		}
	    }
	    FileUtils.cleanDirectory(new File(repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_"
		    + TraitementState.ENCOURS));
	    FileUtils.cleanDirectory(new File(
		    repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + TraitementState.OK));
	    FileUtils.cleanDirectory(new File(
		    repertoire + envDir + File.separator + TypeTraitementPhase.REGISTER + "_" + TraitementState.KO));
	    try {
		FileUtils.cleanDirectory(new File(repertoire + envDir + File.separator + "EXPORT"));
	    } catch (Exception e) {
	    }

	} catch (IOException ex) {
	    LoggerHelper.error(LOGGER, ex, "clearPilotageAndDirectories()");
	} catch (SQLException ex) {
	    LoggerHelper.error(LOGGER, ex, "clearPilotageAndDirectories()");
	    throw ex;
	}
    }

    /**
     * Créer le fichier dummy qui sert à déclencher l'initialisation suite à une
     * mise en production
     *
     * @param todo
     *
     *            todo = true : on crée le fichier todo = false : on efface le
     *            fichier
     *
     *            Ne sert à priori plus car le batch ne se lance plus sous condition
     *            de présence de fichier mais se lance toutes les 5 minutes
     *            quoiqu'il arrive Garder commentée cette méthode si jamais la
     *            production exige que le scan se fasse sous condition de présence
     *            de fichier !!
     */
    public static void setDummyFilePROD(Boolean todo) {

    }

}
