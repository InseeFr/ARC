package fr.insee.arc.web.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.CalendarDAO;
import fr.insee.arc.core.dao.NormeDAO;
import fr.insee.arc.core.dao.module_dao.AbstractRuleDAO;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.RegleMappingEntity;
import fr.insee.arc.core.model.RuleSets;
import fr.insee.arc.core.service.mapping.MappingService;
import fr.insee.arc.core.service.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.mapping.VariableMapping;
import fr.insee.arc.core.service.mapping.regles.AbstractRegleMapping;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.dao.EntityDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.ICharacterConstant;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.action.GererNormeAction;
import fr.insee.arc.web.util.ConstanteBD;
import fr.insee.arc.web.util.EAlphaNumConstante;
import fr.insee.arc.web.util.VObject;

/**
 * Will own all the utilitary methode used in the {@link GererNormeAction}
 * 
 * @author Pépin Rémi
 *
 */
public class NormManagementDao {
    private static final String JDR = " jdr.";
    private static final String MAPPING = "mapping.";
    private static final Logger LOGGER = Logger.getLogger(NormManagementDao.class);

    private static final String CLEF_CONSOLIDATION = "{clef}";
    public static final int INDEX_COLONNE_VARIABLE_TABLE_REGLE_MAPPING = 6;

    private static final String TOKEN_NOM_VARIABLE = "{tokenNomVariable}";

    private static final String MESSAGE_VARIABLE_CLEF_NULL = "La variable {tokenNomVariable} est une variable clef pour la consolidation.\nVous devez vous assurer qu'elle ne soit jamais null.";

    private NormManagementDao() {
	throw new IllegalStateException("Utility class");
    }

    /**
     * Return the SQL to get all the rules bond to a rule set. It suppose the a rule
     * set is selected
     * 
     * @param viewRulesSet
     *            : the Vobject containing the rules
     * @param table
     *            : the sql to get the rules in the database
     * @return an sql query to get all the rules bond to a rule set
     */
    public static String recupRegle(VObject viewRulesSet, String table) {
	StringBuilder requete = new StringBuilder();
	Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
	HashMap<String, String> type = viewRulesSet.mapHeadersType();
	requete.append("select * from " + table + " ");
	requete.append(" where id_norme" + ManipString.sqlEqual(selection.get(AbstractRuleDAO.ID_NORME).get(0),
		type.get(AbstractRuleDAO.ID_NORME)));
	requete.append(
		" and periodicite" + ManipString.sqlEqual(selection.get(AbstractRuleDAO.PERIODICITE).get(0),
			type.get(AbstractRuleDAO.PERIODICITE)));
	requete.append(
		" and validite_inf" + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_INF).get(0),
			type.get(AbstractRuleDAO.VALIDITE_INF)));
	requete.append(
		" and validite_sup" + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0),
			type.get(AbstractRuleDAO.VALIDITE_SUP)));
	requete.append(" and version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
	LoggerDispatcher.info("donwload request : " + requete.toString(), LOGGER);
	return requete.toString();
    }

    /**
     * Initialize the {@value GererNormeAction#viewNorme}. Request the full general
     * norm table.
     */
    public static void initializeViewNorme(VObject viewNorme, String theTableName) {
	LoggerHelper.debug(LOGGER, "/* initializeNorme */");
	HashMap<String, String> defaultInputFields = new HashMap<>();

	List<String> theColumnToGet = new ArrayList<>();
	theColumnToGet.add(NormeDAO.ID_NORME);
	theColumnToGet.add(NormeDAO.PERIODICITE);
	theColumnToGet.add(NormeDAO.DEF_NORME);
	theColumnToGet.add(NormeDAO.DEF_VALIDITE);
	theColumnToGet.add(NormeDAO.ETAT);
	theColumnToGet.add(NormeDAO.ID_FAMILLE);

	viewNorme.initialize(
		// format the request to get the data
		FormatSQL
			.getAllReccordsFromATableAscOrder(theColumnToGet, theTableName, AbstractRuleDAO.ID_NORME)
			.toString(), //
		theTableName, //
		defaultInputFields);
    }

    /**
     * Initialize the {@value GererNormeAction#viewCalendar}. Only get the calendar
     * link to the selected norm.
     */
    public static void initializeViewCalendar(VObject viewCalendar, VObject viewNorme, String theTableName) {
	LoggerHelper.debug(LOGGER, "/* initializeCalendar */");

	// get the norm selected
	Map<String, ArrayList<String>> selection = viewNorme.mapContentSelected();

	if (!selection.isEmpty()) {
	    // Get the type of the column for casting
	    HashMap<String, String> type = viewNorme.mapHeadersType();

	    // Construct the default value for insertion
	    HashMap<String, String> defaultInputFields = new HashMap<>();
	    defaultInputFields.put(AbstractRuleDAO.ID_NORME,
		    selection.get(AbstractRuleDAO.ID_NORME).get(0));
	    defaultInputFields.put(AbstractRuleDAO.PERIODICITE,
		    selection.get(AbstractRuleDAO.PERIODICITE).get(0));
	    viewCalendar.setAfterInsertQuery("select arc.fn_check_calendrier(); ");
	    viewCalendar.setAfterUpdateQuery("select arc.fn_check_calendrier(); ");

	    /*
	     * The wanted column
	     */
		List<String> theColumnToGet = new ArrayList<>();
		theColumnToGet.add(CalendarDAO.ID_NORME);
		theColumnToGet.add(CalendarDAO.PERIODICITE);
		theColumnToGet.add(CalendarDAO.VALIDITE_INF);
		theColumnToGet.add(CalendarDAO.VALIDITE_SUP);
		theColumnToGet.add(CalendarDAO.ETAT);
		
	    // Create the vobject
	    viewCalendar.initialize(
		    // Get the reccord for the selected norm
		    FormatSQL.getSomeReccordFromATable(theColumnToGet//
			    , theTableName//
			    ,
			    AbstractRuleDAO.ID_NORME
				    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.ID_NORME).get(0),
					    type.get(AbstractRuleDAO.ID_NORME)),
			    AbstractRuleDAO.PERIODICITE
				    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.PERIODICITE).get(0),
					    type.get(AbstractRuleDAO.PERIODICITE)))
			    .toString(), //
		    theTableName, //
		    defaultInputFields); // set the default value

	} else {
	    viewCalendar.destroy();
	}
    }

    /**
     * Initialize the {@value GererNormeAction#viewRulesSet}. Only get the rulesset
     * link to the selected norm and calendar.
     */
    public static void initializeViewRulesSet(VObject viewRulesSet, VObject viewCalendar, String theTableName) {
	LoggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

	// Get the selected calendar for requesting the rule set
	Map<String, ArrayList<String>> selection = viewCalendar.mapContentSelected();
	if (!selection.isEmpty()) {

	    HashMap<String, String> type = viewCalendar.mapHeadersType();

	    // Construct the default value for insertion
	    HashMap<String, String> defaultInputFields = new HashMap<>();
	    defaultInputFields.put(AbstractRuleDAO.ID_NORME,
		    selection.get(AbstractRuleDAO.ID_NORME).get(0));
	    defaultInputFields.put(AbstractRuleDAO.PERIODICITE,
		    selection.get(AbstractRuleDAO.PERIODICITE).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VALIDITE_INF,
		    selection.get(AbstractRuleDAO.VALIDITE_INF).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VALIDITE_SUP,
		    selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0));
	    viewRulesSet.setAfterInsertQuery("select arc.fn_check_jeuderegle(); ");
	    viewRulesSet.setAfterUpdateQuery("select arc.fn_check_jeuderegle(); ");

	    viewRulesSet
		    .initialize(
			    FormatSQL.getSomeReccordFromATable(theTableName,
				    AbstractRuleDAO.ID_NORME + ManipString.sqlEqual(
					    selection.get(AbstractRuleDAO.ID_NORME).get(0),
					    type.get(AbstractRuleDAO.ID_NORME)),
				    AbstractRuleDAO.PERIODICITE + ManipString.sqlEqual(
					    selection.get(AbstractRuleDAO.PERIODICITE).get(0),
					    type.get(AbstractRuleDAO.PERIODICITE)),
				    AbstractRuleDAO.VALIDITE_INF + ManipString.sqlEqual(
					    selection.get(AbstractRuleDAO.VALIDITE_INF).get(0),
					    type.get(AbstractRuleDAO.VALIDITE_INF)),
				    AbstractRuleDAO.VALIDITE_SUP + ManipString.sqlEqual(
					    selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0),
					    type.get(AbstractRuleDAO.VALIDITE_SUP)))
				    .toString(), //
			    theTableName, //
			    defaultInputFields);
	} else {
	    viewRulesSet.destroy();
	}
    }

    /**
     * Initialize the the {@link VObject} of a module rule (except mapping). Only
     * get the load rule link to the selected rule set.
     * 
     * The request to initialize look like that
     * 
     * SELECT * FROM theTableName WHERE id_norme=id_norme_selected::text AND
     * PERIODICITE=PERIODICITE_selected::text AND
     * VALIDITE_INF=VALIDITE_INF_selected::date AND
     * VALIDITE_SUP=VALIDITE_SUP_selected::date AND VERSION=VERSION_selected::text
     * 
     */
    public static void initializeModuleRules(VObject moduleView, VObject viewRulesSet, String theTableName) {
	LoggerDispatcher.info(String.format("Initialize view table %s", theTableName), LOGGER);
	Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
	if (!selection.isEmpty()) {
	    HashMap<String, String> type = viewRulesSet.mapHeadersType();

	    HashMap<String, String> defaultInputFields = new HashMap<>();
	    defaultInputFields.put(AbstractRuleDAO.ID_NORME,
		    selection.get(AbstractRuleDAO.ID_NORME).get(0));
	    defaultInputFields.put(AbstractRuleDAO.PERIODICITE,
		    selection.get(AbstractRuleDAO.PERIODICITE).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VALIDITE_INF,
		    selection.get(AbstractRuleDAO.VALIDITE_INF).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VALIDITE_SUP,
		    selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0));
	    defaultInputFields.put("version", selection.get("version").get(0));
	    moduleView.initialize(FormatSQL.getSomeReccordFromATable(theTableName,
		    // create sql condition like : id_norme=id_norme_selected::text
		    AbstractRuleDAO.ID_NORME
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.ID_NORME).get(0),
				    type.get(AbstractRuleDAO.ID_NORME)),

		    // create sql condition like : PERIODICITE=PERIODICITE_selected::text
		    AbstractRuleDAO.PERIODICITE
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.PERIODICITE).get(0),
				    type.get(AbstractRuleDAO.PERIODICITE)),

		    // create sql condition like : VALIDITE_INF=VALIDITE_INF_selected::date
		    AbstractRuleDAO.VALIDITE_INF
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_INF).get(0),
				    type.get(AbstractRuleDAO.VALIDITE_INF)),

		    // create sql condition like : VALIDITE_SUP=VALIDITE_SUP_selected::date
		    AbstractRuleDAO.VALIDITE_SUP
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0),
				    type.get(AbstractRuleDAO.VALIDITE_SUP)),

		    // create sql condition like : VERSION=VERSION_selected::text
		    AbstractRuleDAO.VERSION
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VERSION).get(0),
				    type.get(AbstractRuleDAO.VERSION)))
		    .toString(), theTableName, defaultInputFields);
	} else {
	    moduleView.destroy();
	}
    }

    /**
     * Initialize the the {@link VObject} of the mapping rule. Only get the load
     * rule link to the selected rule set.
     * 
     * The initialize request look like that
     * 
     * SELECT mapping.id_regle , mapping.id_norme , mapping.validite_inf ,
     * mapping.validite_sup , mapping.version , mapping.periodicite ,
     * mapping.variable_sortie , mapping.expr_regle_col , mapping.commentaire ,
     * variables.type_variable_metier type_sortie , variables.type_consolidation
     * type_consolidation </br>
     * FROM arc.ihm_mapping_regle mapping </br>
     * INNER JOIN arc.ihm_jeuderegle jdr ON </br>
     * mapping.id_norme= jdr.id_norme </br>
     * AND mapping.periodicite= jdr.periodicite </br>
     * AND mapping.validite_inf= jdr.validite_inf </br>
     * AND mapping.validite_sup=jdr.validite_sup </br>
     * AND mapping.version= jdr.version </br>
     * INNER JOIN arc.ihm_norme </br>
     * norme ON norme.id_norme= jdr.id_norme </br>
     * AND norme.periodicite= jdr.periodicite </br>
     * INNER JOIN ( </br>
     * SELECT DISTINCT id_famille, nom_variable_metier, type_variable_metier,
     * type_consolidation </br>
     * FROM arc.ihm_mod_variable_metier) </br>
     * variables ON variables.id_famille = norme.id_famille </br>
     * AND variables.nom_variable_metier = mapping.variable_sortie</br>
     * </br>
     * WHERE mapping.id_norme=id_norme_selected::text </br>
     * AND mapping.periodicite=periodicite_selected::text </br>
     * AND mapping.validite_inf=validite_inf_selected::date </br>
     * AND mapping.validite_sup=validite_sup_selected::date </br>
     * AND mapping.version=version_selected::text</br>
     * 
     */
    public static void initializeMapping(VObject viewMapping, VObject viewRulesSet, String theTableName) {
	System.out.println("/* initializeMapping */");
	Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
	if (!selection.isEmpty()) {
	    HashMap<String, String> type = viewRulesSet.mapHeadersType();
	    List<String> listColumnSelect = new ArrayList<>();
	    listColumnSelect.add("mapping.id_regle");
	    listColumnSelect.add(MAPPING + AbstractRuleDAO.ID_NORME);
	    listColumnSelect.add(MAPPING + AbstractRuleDAO.VALIDITE_INF);
	    listColumnSelect.add(MAPPING + AbstractRuleDAO.VALIDITE_SUP);
	    listColumnSelect.add(MAPPING + AbstractRuleDAO.VERSION);
	    listColumnSelect.add(MAPPING + AbstractRuleDAO.PERIODICITE);
	    listColumnSelect.add("mapping.variable_sortie");
	    listColumnSelect.add("mapping.expr_regle_col");
	    listColumnSelect.add("mapping.commentaire");
	    listColumnSelect.add("variables.type_variable_metier type_sortie");
	    listColumnSelect.add("variables.type_consolidation type_consolidation");

	    StringBuilder requeteFrom = new StringBuilder();
	    requeteFrom.append(" arc.ihm_mapping_regle mapping INNER JOIN arc.ihm_jeuderegle jdr\n");
	    requeteFrom.append(String.join("\n\tAND ",
		    "ON mapping." + AbstractRuleDAO.ID_NORME + " = " + JDR + AbstractRuleDAO.ID_NORME,
		    MAPPING + AbstractRuleDAO.PERIODICITE + " = " + JDR + AbstractRuleDAO.PERIODICITE,
		    MAPPING + AbstractRuleDAO.VALIDITE_INF + " = " + JDR + AbstractRuleDAO.VALIDITE_INF,
		    MAPPING + AbstractRuleDAO.VALIDITE_SUP + " = " + JDR + AbstractRuleDAO.VALIDITE_SUP,
		    MAPPING + AbstractRuleDAO.VERSION + " = " + JDR + AbstractRuleDAO.VERSION));

	    requeteFrom.append("  INNER JOIN arc.ihm_norme norme");
	    requeteFrom.append(
		    " ON norme." + AbstractRuleDAO.ID_NORME + " = " + JDR + AbstractRuleDAO.ID_NORME);
	    requeteFrom.append(" AND norme." + AbstractRuleDAO.PERIODICITE + " = " + JDR
		    + AbstractRuleDAO.PERIODICITE);
	    requeteFrom.append(
		    "  INNER JOIN (SELECT DISTINCT id_famille, nom_variable_metier, type_variable_metier, type_consolidation FROM arc.ihm_mod_variable_metier) variables\n");
	    requeteFrom.append(
		    "  ON variables.id_famille = norme.id_famille AND variables.nom_variable_metier = mapping.variable_sortie\n");

	    HashMap<String, String> defaultInputFields = new HashMap<>();
	    defaultInputFields.put(AbstractRuleDAO.ID_NORME,
		    selection.get(AbstractRuleDAO.ID_NORME).get(0));
	    defaultInputFields.put(AbstractRuleDAO.PERIODICITE,
		    selection.get(AbstractRuleDAO.PERIODICITE).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VALIDITE_INF,
		    selection.get(AbstractRuleDAO.VALIDITE_INF).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VALIDITE_SUP,
		    selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0));
	    defaultInputFields.put(AbstractRuleDAO.VERSION,
		    selection.get(AbstractRuleDAO.VERSION).get(0));

	    viewMapping.initialize(FormatSQL.getSomeReccordFromATable(listColumnSelect, requeteFrom.toString(),
		    // create sql condition like : mapping.id_norme=id_norme_selected::text
		    MAPPING + AbstractRuleDAO.ID_NORME
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.ID_NORME).get(0),
				    type.get(AbstractRuleDAO.ID_NORME)),

		    // create sql condition like : mapping.PERIODICITE=PERIODICITE_selected::text
		    MAPPING + AbstractRuleDAO.PERIODICITE
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.PERIODICITE).get(0),
				    type.get(AbstractRuleDAO.PERIODICITE)),

		    // create sql condition like : mapping.VALIDITE_INF=VALIDITE_INF_selected::date
		    MAPPING + AbstractRuleDAO.VALIDITE_INF
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_INF).get(0),
				    type.get(AbstractRuleDAO.VALIDITE_INF)),

		    // create sql condition like :
		    // mapping.VALIDITE_SEUP=VALIDITE_SEUP_selected::date
		    MAPPING + AbstractRuleDAO.VALIDITE_SUP
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0),
				    type.get(AbstractRuleDAO.VALIDITE_SUP)),

		    // create sql condition like : mapping.VERSIONe=VERSION_selected::text
		    MAPPING + AbstractRuleDAO.VERSION
			    + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VERSION).get(0),
				    type.get(AbstractRuleDAO.VERSION)))
		    .toString(), //
		    theTableName, //
		    defaultInputFields);
	} else {
	    viewMapping.destroy();
	}
    }

    
    
    /**
     *  Initialize the {@value GererNormeAction#viewJeuxDeReglesCopie}. Get in database all the reccord the rule sets.
     * @param viewJeuxDeReglesCopie
     */
    public static void initializeJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie,VObject viewRulesSet, String theTableName) {
	LoggerHelper.info(LOGGER, "initializeJeuxDeReglesCopie");
	Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
	if (!selection.isEmpty()) {
	    List<String> listColumnSelect = new ArrayList<>();
	    listColumnSelect.add( AbstractRuleDAO.ID_NORME);
	    listColumnSelect.add( AbstractRuleDAO.PERIODICITE);
	    listColumnSelect.add( AbstractRuleDAO.VALIDITE_INF);
	    listColumnSelect.add( AbstractRuleDAO.VALIDITE_SUP);
	    listColumnSelect.add( AbstractRuleDAO.VERSION);
	    listColumnSelect.add( ConstanteBD.STATE.getValue());
	    
	    HashMap<String, String> defaultInputFields = new HashMap<>();
	    viewJeuxDeReglesCopie.initialize(FormatSQL.getAllReccordsFromATable(listColumnSelect, theTableName).toString()//
		    , theTableName, defaultInputFields);
	} else {
	    viewJeuxDeReglesCopie.destroy();
	}
	    
    }
    
    /**
     * Send a rule set to production.
     */
    @SQLExecutor
    public static void sendRuleSetToProduction(VObject viewRulesSet, String theTable) {
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
	Date dNow = new Date();
	LoggerDispatcher.warn("Rule set send to production", LOGGER);

	try {
	    UtilitaireDao.get("arc").executeRequest(null, "update " + theTable + " set last_init='"
		    + dateFormat.format(dNow) + "', operation=case when operation='R' then 'O' else operation end;");
	    viewRulesSet.setMessage("Go to production registered");

	} catch (SQLException e) {
	    viewRulesSet.setMessage("Error in the go to production");
	    LoggerHelper.warn(LOGGER, "Error in the go to production");

	}
    }

    /**
     * Create a table to check the update of a rule. Caution to database constrainte
     *
     * @param env
     * @param table
     * @return
     */
    public static String createTableTempTest(String tableACopier) {

	String nomTableTest = "arc.test_ihm_" + tableACopier;

	StringBuilder create = new StringBuilder();

	create.append("DROP TABLE IF EXISTS " + nomTableTest + "; ");
	create.append("CREATE ");

	create.append(" TABLE " + nomTableTest + " AS SELECT * FROM arc.ihm_" + tableACopier + ";");
	create.append("ALTER TABLE " + nomTableTest
		+ " ADD PRIMARY KEY (");
	create.append(String.join(" , ", AbstractRuleDAO.ID_NORME, AbstractRuleDAO.PERIODICITE,
		AbstractRuleDAO.VALIDITE_INF, AbstractRuleDAO.VALIDITE_SUP, AbstractRuleDAO.VERSION, AbstractRuleDAO.ID_REGLE));

	create.append(");");
	create.append("CREATE CONSTRAINT TRIGGER doublon ");
	create.append("AFTER INSERT OR UPDATE OF rubrique_pere, rubrique_fils ");
	create.append("ON " + nomTableTest + " DEFERRABLE INITIALLY DEFERRED ");
	create.append("FOR EACH ROW ");
	create.append("EXECUTE PROCEDURE arc.verif_doublon(); ");

	create.append("CREATE TRIGGER tg_insert_controle ");
	create.append("before INSERT ON " + nomTableTest + " ");
	create.append("FOR EACH ROW ");
	create.append("EXECUTE PROCEDURE arc.insert_controle(); ");

	return create.toString();
    }

    /**
     * 
     * @param viewRulesSet
     * @return
     */
    public static RuleSets fetchJeuDeRegle(VObject viewRulesSet) {
	Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
	/*
	 * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et
	 * calendrier
	 */
	RuleSets jdr = new RuleSets();
	jdr.setIdNorme(selection.get(AbstractRuleDAO.ID_NORME).get(0));
	jdr.setPeriodicite(selection.get(AbstractRuleDAO.PERIODICITE).get(0));
	jdr.setValiditeInfString(selection.get(AbstractRuleDAO.VALIDITE_INF).get(0),
		EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	jdr.setValiditeSupString(selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0),
		EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	jdr.setVersion(selection.get(AbstractRuleDAO.VERSION).get(0));
	jdr.setEtat(selection.get(ConstanteBD.STATE.getValue()).get(0));
	return jdr;
    }

    /**
     * Empty all the rules of a norm modul
     * 
     * @param table
     * @return
     */
    public static void emptyRuleTable(VObject viewRulesSet, String table) {
	LoggerDispatcher.info("Empty all the rules of a module", LOGGER);
	Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
	HashMap<String, String> type = viewRulesSet.mapHeadersType();
	StringBuilder requete = FormatSQL.deleteSomeLine(table,
		AbstractRuleDAO.ID_NORME + ManipString.sqlEqual(selection.get(AbstractRuleDAO.ID_NORME).get(0),
			type.get(AbstractRuleDAO.ID_NORME)),
		AbstractRuleDAO.PERIODICITE + ManipString.sqlEqual(selection.get(AbstractRuleDAO.PERIODICITE).get(0),
			type.get(AbstractRuleDAO.PERIODICITE)),
		AbstractRuleDAO.VALIDITE_INF + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_INF).get(0),
			type.get(AbstractRuleDAO.VALIDITE_INF)),
		AbstractRuleDAO.VALIDITE_SUP + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0),
			type.get(AbstractRuleDAO.VALIDITE_SUP)),
		AbstractRuleDAO.VERSION + ManipString.sqlEqual(selection.get(AbstractRuleDAO.VERSION).get(0),
			type.get(AbstractRuleDAO.VERSION)));


	try {
	    UtilitaireDao.get("arc").executeRequest(null, requete);
	} catch (SQLException e) {
	    LoggerHelper.error(LOGGER, String.format("Error when emptying the rules %s", e.toString()));

	}
    }

    public static String createTableTest(String aNomTable, List<String> listRubrique) {
	StringBuilder sb = new StringBuilder();

	List <List <String>> attributeNameAndType = new ArrayList<>();
	attributeNameAndType.add(Arrays.asList(AbstractRuleDAO.ID_NORME, "text"));
	attributeNameAndType.add(Arrays.asList(AbstractRuleDAO.PERIODICITE, "text"));
	attributeNameAndType.add(Arrays.asList(ConstanteBD.ID_SOURCE.getValue(), "text"));
	attributeNameAndType.add(Arrays.asList(ConstanteBD.VALIDITY.getValue(), "text"));
	attributeNameAndType.add(Arrays.asList(ConstanteBD.ID.getValue(), "integer"));
	attributeNameAndType.add(Arrays.asList(ConstanteBD.CONTROL.getValue(), "text"));
	attributeNameAndType.add(Arrays.asList(ConstanteBD.BROKENRULES.getValue(), "text[]"));
	/*
	 * Test if the core columns are not in the filter column list. If so the request
	 * will be invalid Can be the case when, for isntance, the column validity is in
	 * the filter rule
	 */
	
	listRubrique.remove(AbstractRuleDAO.ID_NORME);
	listRubrique.remove(AbstractRuleDAO.PERIODICITE);
	listRubrique.remove(ConstanteBD.ID_SOURCE.getValue());
	listRubrique.remove(ConstanteBD.VALIDITY.getValue());
	listRubrique.remove(ConstanteBD.ID.getValue());
	listRubrique.remove(ConstanteBD.CONTROL.getValue());
	listRubrique.remove(ConstanteBD.BROKENRULES.getValue());
	
	for (String rub : listRubrique) {
	    if (rub.toUpperCase().startsWith("I")) {
		attributeNameAndType.add(Arrays.asList(rub, "integer"));
	    } else {
		attributeNameAndType.add(Arrays.asList(rub, "text"));
	    }
	    
	}
	
	sb.append(FormatSQL.requeteCreate(aNomTable, attributeNameAndType));
	
	LoggerDispatcher.info("Creation test table request : " + sb, LOGGER);
	return sb.toString();
    }

    private static List<String> getTableEnvironnement(String state) {
	StringBuilder requete = new StringBuilder();

	String zeEnv = ManipString.substringAfterFirst(state, EAlphaNumConstante.DOT.getValue());
	String zeSchema = ManipString.substringBeforeFirst(state, EAlphaNumConstante.DOT.getValue());

	requete.append("SELECT replace(lower(relname), '" + zeEnv + "', '') ")//
		.append("\n  FROM pg_class a ")//
		.append("\n  INNER JOIN pg_namespace b ON a.relnamespace=b.oid ")//
		.append("\n  WHERE lower(b.nspname)=lower('" + zeSchema + "') ")//
		.append("\n  AND lower(relname) LIKE '" + zeEnv.toLowerCase() + "\\_%'; ");
	return UtilitaireDao.get(DbConstant.POOL_NAME).getList(null, requete, new ArrayList<String>());
    }

    /**
     * @param afterUpdate
     * @param isRegleOk
     * @return
     */
    public static boolean testerReglesMapping(VObject viewMapping, VObject viewRulesSet, VObject viewNorme,
	    Map<String, ArrayList<String>> afterUpdate) {
	boolean isRegleOk = true;
	List<String> tableADropper = new ArrayList<>();
	String zeExpression = null;
	String zeVariable = null;
	if (!afterUpdate.isEmpty() && !afterUpdate.get("expr_regle_col").isEmpty()) {

	    try {
		/*
		 * Récupération du jeu de règle
		 */
		RuleSets jdr = NormManagementDao.fetchJeuDeRegle(viewRulesSet);
		/*
		 * recopie des tables de l'environnement
		 */
		List<String> tables = getTableEnvironnement(jdr.getEtat());
		List<String> sources = new ArrayList<>();
		List<String> targets = new ArrayList<>();
		String envTarget = jdr.getEtat().replace(EAlphaNumConstante.DOT.getValue(), ".test_");
		for (int i = 0; i < tables.size(); i++) {
		    sources.add(jdr.getEtat() + tables.get(i));
		    targets.add(envTarget + tables.get(i));
		}
		tableADropper.addAll(targets);

		UtilitaireDao.get(DbConstant.POOL_NAME).dupliquerVers(null, sources, targets, "false");
		List<AbstractRegleMapping> listRegle = new ArrayList<>();
		RegleMappingFactory regleMappingFactory = new RegleMappingFactory(null, envTarget,
			new HashSet<String>(), new HashSet<String>());
		String idFamille = viewNorme.mapContentSelected().get("id_famille").get(0);
		regleMappingFactory.setIdFamille(idFamille);
		for (int i = 0; i < afterUpdate.get("expr_regle_col").size(); i++) {
		    String expression = afterUpdate.get("expr_regle_col").get(i);
		    String variable = afterUpdate.get("variable_sortie").get(i);
		    String type = afterUpdate.get("type_sortie").get(i);
		    VariableMapping variableMapping = new VariableMapping(regleMappingFactory, variable, type);
		    variableMapping.setExpressionRegle(regleMappingFactory.get(expression, variableMapping));
		    listRegle.add(variableMapping.getExpressionRegle());
		}
		/*
		 * on dérive pour avoir de belles expressions à tester et connaitre les noms de
		 * colonnes
		 */
		for (int i = 0; i < listRegle.size(); i++) {
		    listRegle.get(i).deriverTest();
		}
		Set<Integer> groupesUtiles = groupesUtiles(listRegle);
		/*
		 * on fait remonter les noms de colonnes dans colUtiles
		 */
		Set<String> colUtiles = new TreeSet<>();
		for (int i = 0; i < listRegle.size(); i++) {
		    for (Integer groupe : groupesUtiles) {
			colUtiles.addAll(listRegle.get(i).getEnsembleIdentifiantsRubriques(groupe));
			colUtiles.addAll(listRegle.get(i).getEnsembleNomsRubriques(groupe));
		    }
		    colUtiles.addAll(listRegle.get(i).getEnsembleIdentifiantsRubriques());
		    colUtiles.addAll(listRegle.get(i).getEnsembleNomsRubriques());
		}

		createTablePhasePrecedente(envTarget, colUtiles, tableADropper);
		for (int i = 0; i < listRegle.size(); i++) {
		    zeExpression = listRegle.get(i).getExpression();
		    zeVariable = listRegle.get(i).getVariableMapping().getNomVariable();
		    // Test de l'expression
		    groupesUtiles = groupesUtiles(Arrays.asList(listRegle.get(i)));
		    if (groupesUtiles.isEmpty()) {
			if (createRequeteSelect(envTarget, listRegle.get(i))
				&& CLEF_CONSOLIDATION.equalsIgnoreCase(afterUpdate.get("type_consolidation").get(i))) {
			    throw new IllegalArgumentException(MESSAGE_VARIABLE_CLEF_NULL.replace(TOKEN_NOM_VARIABLE,
				    listRegle.get(i).getVariableMapping().getNomVariable()));
			}
		    } else {
			for (Integer groupe : groupesUtiles) {
			    if (createRequeteSelect(envTarget, listRegle.get(i), groupe) && CLEF_CONSOLIDATION
				    .equalsIgnoreCase(afterUpdate.get("type_consolidation").get(i))) {
				throw new IllegalArgumentException(MESSAGE_VARIABLE_CLEF_NULL.replace(
					TOKEN_NOM_VARIABLE, listRegle.get(i).getVariableMapping().getNomVariable()
						+ "(groupe " + groupe + ")"));
			    }
			}
		    }
		}
	    } catch (Exception ex) {
		isRegleOk = false;
		viewMapping.setMessage((zeVariable == null ? EAlphaNumConstante.EMPTY.getValue()
			: "La règle " + zeVariable + " ::= " + zeExpression + " est erronée.\n") + "Exception levée : "
			+ ex.getMessage());
	    } finally {
		UtilitaireDao.get(DbConstant.POOL_NAME).dropTable(null, tableADropper.toArray(new String[0]));
	    }
	}
	return isRegleOk;
    }

    /**
     * @param listRegle
     * @param returned
     * @return
     * @throws Exception
     */
    private static Set<Integer> groupesUtiles(List<AbstractRegleMapping> listRegle) throws Exception {
	Set<Integer> returned = new TreeSet<>();
	for (int i = 0; i < listRegle.size(); i++) {
	    returned.addAll(listRegle.get(i).getEnsembleGroupes());
	}
	return returned;
    }

    /**
     * Exécution d'une règle sur la table <anEnvTarget>_filtrage_ok
     *
     * @param anEnvTarget
     * @param regleMapping
     * @return
     * @throws SQLException
     */
    private static Boolean createRequeteSelect(String anEnvTarget, AbstractRegleMapping regleMapping) throws Exception {
	StringBuilder requete = new StringBuilder("SELECT CASE WHEN ")//
		.append("(" + regleMapping.getExpressionSQL() + ")::" + regleMapping.getVariableMapping().getType())//
		// .append(" IS NULL THEN true ELSE false END")//
		.append(" IS NULL THEN false ELSE false END")//
		.append(" AS " + regleMapping.getVariableMapping().getNomVariable());
	requete.append("\n  FROM " + anEnvTarget + EAlphaNumConstante.UNDERSCORE.getValue() + "filtrage_ok ;");
	return UtilitaireDao.get(DbConstant.POOL_NAME).getBoolean(null, requete);
    }

    private static Boolean createRequeteSelect(String anEnvTarget, AbstractRegleMapping regleMapping, Integer groupe)
	    throws Exception {
	StringBuilder requete = new StringBuilder("SELECT CASE WHEN ");//
	requete.append("(" + regleMapping.getExpressionSQL(groupe) + ")::"
		+ regleMapping.getVariableMapping().getType().replace("[]", ""))//
		// .append(" IS NULL THEN true ELSE false END")//
		.append(" IS NULL THEN false ELSE false END")//
		.append(" AS " + regleMapping.getVariableMapping().getNomVariable());
	requete.append("\n  FROM " + anEnvTarget + EAlphaNumConstante.UNDERSCORE.getValue() + "filtrage_ok ;");
	return UtilitaireDao.get(DbConstant.POOL_NAME).getBoolean(null, requete);
    }

    /**
     * Creation d'une table vide avec les colonnes adéquates.<br/>
     * En particulier, si la règle n'utilise pas du tout de noms de colonnes, une
     * colonne {@code col$null} est créée, qui permette un requêtage.
     *
     * @param anEnvTarget
     * @param colUtiles
     * @param tableADropper
     * @throws SQLException
     */
    private static void createTablePhasePrecedente(String anEnvTarget, Set<String> colUtiles,
	    List<String> tableADropper) throws SQLException {
	StringBuilder requete = new StringBuilder(
		"DROP TABLE IF EXISTS " + anEnvTarget + EAlphaNumConstante.UNDERSCORE.getValue() + "filtrage_ok;");
	requete.append("CREATE TABLE " + anEnvTarget + EAlphaNumConstante.UNDERSCORE.getValue() + "filtrage_ok (");
	requete.append(Format.untokenize(colUtiles, " text, "));
	requete.append(colUtiles.isEmpty() ? "col$null text" : " text")//
		.append(");");

	requete.append("\nINSERT INTO " + anEnvTarget + EAlphaNumConstante.UNDERSCORE.getValue() + "filtrage_ok (")//
		.append(Format.untokenize(colUtiles, ", "))//
		.append(colUtiles.isEmpty() ? "col$null" : EAlphaNumConstante.EMPTY.getValue())//
		.append(") VALUES (");
	boolean isFirst = true;
	for (String variable : colUtiles) {
	    if (isFirst) {
		isFirst = false;
	    } else {
		requete.append(", ");
	    }
	    if (MappingService.colNeverNull.contains(variable)) {
		requete.append("'" + variable + "'");
	    } else {
		requete.append("null");
	    }
	}
	requete.append(colUtiles.isEmpty() ? "null" : EAlphaNumConstante.EMPTY.getValue())//
		.append(");");

	tableADropper.add(anEnvTarget + EAlphaNumConstante.UNDERSCORE.getValue() + "filtrage_ok");
	UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(null, requete);
    }

    public static void calculerVariableToType(VObject viewNorme, Map<String, String> mapVariableToType,
	    Map<String, String> mapVariableToTypeConso) throws SQLException {
	ArrayList<ArrayList<String>> resultat = UtilitaireDao.get("arc").executeRequest(null, new StringBuilder(
		"SELECT DISTINCT lower(nom_variable_metier) AS nom_variable_metier, type_variable_metier, type_consolidation AS type_sortie FROM arc.ihm_mod_variable_metier WHERE id_famille='"
			+ viewNorme.mapContentSelected().get("id_famille").get(0) + "'"));
	for (int i = 2; i < resultat.size(); i++) {
	    mapVariableToType.put(resultat.get(i).get(0), resultat.get(i).get(1));
	    mapVariableToTypeConso.put(resultat.get(i).get(0), resultat.get(i).get(2));
	}
    }

    public static Map<String, ArrayList<String>> calculerReglesAImporter(File aFileUpload,
	    List<RegleMappingEntity> listeRegle, EntityDao<RegleMappingEntity> dao,
	    Map<String, String> mapVariableToType, Map<String, String> mapVariableToTypeConso) throws IOException {
	Map<String, ArrayList<String>> returned = new HashMap<>();
	returned.put("type_sortie", new ArrayList<String>());
	returned.put("type_consolidation", new ArrayList<String>());
	if (!aFileUpload.exists()) {
	    throw new FileNotFoundException(aFileUpload.getAbsolutePath());
	}
	if (aFileUpload.isDirectory()) {
	    throw new IOException(aFileUpload.getAbsolutePath() + " n'est pas un chemin de fichier valide.");
	}
	try {
	    BufferedReader br = Files.newBufferedReader(aFileUpload.toPath(), Charset.forName("UTF-8"));
	    try {
		dao.setSeparator(";");
		String line = br.readLine();
		String someNames = line;
		dao.setNames(someNames);
		line = br.readLine();
		String someTypes = line;
		dao.setTypes(someTypes);
		while ((line = br.readLine()) != null) {
		    RegleMappingEntity entity = dao.get(line);
		    listeRegle.add(entity);
		    for (String colName : entity.colNames()) {
			if (!returned.containsKey(colName)) {
			    /*
			     * La colonne n'existe pas encore ? Je l'ajoute.
			     */
			    returned.put(colName, new ArrayList<String>());
			}
			/*
			 * J'ajoute la valeur en fin de colonne.
			 */
			returned.get(colName).add(entity.get(colName));
		    }
		    returned.get("type_sortie").add(mapVariableToType.get(entity.getVariableSortie()));
		    returned.get("type_consolidation").add(mapVariableToTypeConso.get(entity.getVariableSortie()));
		}
	    } finally {
		br.close();
	    }
	} catch (Exception ex) {
	   LoggerHelper.error(LOGGER, "error in calculerReglesAImporter", ex.getStackTrace());
	}
	return returned;
    }

    public static boolean testerConsistanceRegleMapping(List<List<String>> data, String anEnvironnement,
	    String anIdFamille, StringBuilder aMessage) {
	Set<String> variableRegleCharge = new HashSet<String>();
	for (int i = 0; i < data.size(); i++) {
	    variableRegleCharge.add(data.get(i).get(INDEX_COLONNE_VARIABLE_TABLE_REGLE_MAPPING));
	}
	Set<String> variableTableModele = new HashSet<String>();
	variableTableModele
		.addAll(UtilitaireDao.get("arc").getList(null,
			new StringBuilder("SELECT DISTINCT nom_variable_metier FROM " + anEnvironnement
				+ "_mod_variable_metier WHERE id_famille='" + anIdFamille + "'"),
			new ArrayList<String>()));
	LoggerDispatcher.info(
		"La requete de construction de variableTableMetier : \n" + "SELECT DISTINCT nom_variable_metier FROM "
			+ anEnvironnement + "_mod_variable_metier WHERE id_famille='" + anIdFamille + "'",
		LOGGER);
	Set<String> variableToute = new HashSet<String>();
	variableToute.addAll(variableRegleCharge);
	variableToute.addAll(variableTableModele);
	boolean ok = true;
	LoggerDispatcher.info("Les variables du modèle : " + variableTableModele, LOGGER);
	LoggerDispatcher.info("Les variables des règles chargées : " + variableRegleCharge, LOGGER);
	for (String variable : variableToute) {
	    if (!variableRegleCharge.contains(variable)) {
		ok = false;
		aMessage.append("La variable " + variable + " n'est pas présente dans les règles chargées.\n");
	    }
	    if (!variableTableModele.contains(variable)) {
		ok = false;
		aMessage.append(variable + " ne correspond à aucune variable existant.\n");
	    }
	}
	if (!ok) {
	    aMessage.append("Les règles ne seront pas chargées.");
	}
	return ok;
    }

    public static String getIdFamille(String anEnvironnement, String anIdNorme) throws SQLException {
	return UtilitaireDao.get("arc").getString(null, "SELECT id_famille FROM " + anEnvironnement + "_norme"
		+ " WHERE AbstractRuleDAO.ID_NORME='" + anIdNorme + "'");
    }

    /**
     *
     * @param returned
     * @param index
     *            -1 : en fin de tableau<br/>
     *            [0..size[ : le premier élément est ajouté à l'emplacement
     *            {@code index}, les suivants, juste après
     * @param args
     * @return
     */
    public static List<List<String>> ajouterInformationTableau(List<List<String>> returned, int index, String... args) {
	for (int i = 0; i < returned.size(); i++) {
	    for (int j = 0; j < args.length; j++) {
		returned.get(i).add((index == -1 ? returned.get(i).size() : index + j), args[j]);
	    }
	}
	return returned;
    }
    
    
    /**
     * 
     * @param vObjectToUpdate
     *            the vObject to update with file
     * @param tableName
     *            the
     */
    @SQLExecutor
    public static void uploadFileRule(VObject vObjectToUpdate,VObject viewRulesSet, File theFileToUpload) {
	StringBuilder requete = new StringBuilder();

	// Check if there is file
	if (theFileToUpload == null || StringUtils.isBlank(theFileToUpload.getPath())) {
	    // No file -> ko
	    vObjectToUpdate.setMessage("Please choose a file !!");
	} else {
	    //A file -> can process it
	    LoggerHelper.debug(LOGGER, " filesUpload  : " + theFileToUpload);
	    
	    // before inserting in the final table, the rules will be inserted in a table to test them
		String nomTableImage = FormatSQL.temporaryTableName(vObjectToUpdate.getTable() + "_img" + 0);
		BufferedReader bufferedReader = null;
		try {
		    bufferedReader = Files.newBufferedReader(theFileToUpload.toPath(), Charset.forName("UTF-8"));

		    // Get headers
		    List<String> listHeaders = getHeaderFromFile(bufferedReader);
		    
		    /*
		     * Création d'une table temporaire (qui ne peut pas être TEMPORARY)
		     */
		    requete.append(FormatSQL.dropTable(nomTableImage, ";"));
		    requete.append(FormatSQL.createIfNotExistsAsSelectFrom(listHeaders, nomTableImage, vObjectToUpdate.getTable(), "false"));

		    UtilitaireDao.get("arc").executeRequest(null, requete.toString());

		    // Throwing away the first line
		    bufferedReader.readLine();

		    // Importing the file in the database (COPY command)
		    UtilitaireDao.get("arc").importing(null, nomTableImage, bufferedReader, true, false,
			    ICharacterConstant.SEMI_COLON);

		} catch (Exception ex) {
		    vObjectToUpdate.setMessage("Error when uploading the file : " + ex.getMessage());
		    LoggerHelper.error(LOGGER, ex, "uploadOutils()", "\n");
		    // After the exception, the methode cant go further, so the better thing to do is to quit it
		    return;
		    
		} finally {
		    try {
			bufferedReader.close();
		    } catch (IOException e) {
			 LoggerHelper.error(LOGGER, e, "uploadOutils()", "\n");
		    }
		}
		LoggerHelper.debug(LOGGER, "Insert file in the " + nomTableImage +" table");

		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();

		requete.setLength(0);

		requete.append("\n UPDATE " + nomTableImage + " SET ");
		requete.append(
			"\n "+AbstractRuleDAO.ID_NORME+"='" + selection.get(AbstractRuleDAO.ID_NORME).get(0) + "'");
		requete.append("\n, "+AbstractRuleDAO.PERIODICITE+"='"
			+ selection.get(AbstractRuleDAO.PERIODICITE).get(0) + "'");
		requete.append("\n, "+AbstractRuleDAO.VALIDITE_INF+"='"
			+ selection.get(AbstractRuleDAO.VALIDITE_INF).get(0) + "'");
		requete.append("\n, "+AbstractRuleDAO.VALIDITE_SUP+"='"
			+ selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0) + "'");
		requete.append("\n, version='" + selection.get("version").get(0) + "';");
		requete.append("\n DELETE FROM " + vObjectToUpdate.getTable());
		requete.append("\n WHERE ");
		requete.append(
			"\n  "+AbstractRuleDAO.ID_NORME+"='" + selection.get(AbstractRuleDAO.ID_NORME).get(0) + "'");
		requete.append("\n AND   "+AbstractRuleDAO.PERIODICITE+"='"
			+ selection.get(AbstractRuleDAO.PERIODICITE).get(0) + "'");
		requete.append("\n AND   "+AbstractRuleDAO.VALIDITE_INF+"='"
			+ selection.get(AbstractRuleDAO.VALIDITE_INF).get(0) + "'");
		requete.append("\n AND   "+AbstractRuleDAO.VALIDITE_SUP+"='"
			+ selection.get(AbstractRuleDAO.VALIDITE_SUP).get(0) + "'");
		requete.append("\n AND  version='" + selection.get("version").get(0) + "';");
		requete.append("\n INSERT INTO " + vObjectToUpdate.getTable() + " ");
		requete.append("\n SELECT * FROM " + nomTableImage + " ;");
		requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");

		try {
		    UtilitaireDao.get("arc").executeRequest(null, requete.toString());
		} catch (Exception ex) {
		    vObjectToUpdate.setMessage("Error when uploading the file : "  + ex.getMessage());
		    LoggerHelper.error(LOGGER, ex, "uploadOutils()");
		}
	}

	

    }

    private static List<String> getHeaderFromFile(BufferedReader bufferedReader) throws IOException {
	String listeColonnesAggregees = bufferedReader.readLine();
	List<String> listeColonnes = Arrays.asList(listeColonnesAggregees.split(ICharacterConstant.SEMI_COLON));
	LoggerHelper.debug(LOGGER, "Columns list : ", Format.untokenize(listeColonnes, ", "));
	return listeColonnes;
    }
    


}
