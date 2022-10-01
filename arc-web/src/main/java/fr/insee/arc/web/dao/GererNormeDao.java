package fr.insee.arc.web.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleMappingEntity;
import fr.insee.arc.utils.dao.EntityDao;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.action.GererNormeAction;
import fr.insee.arc.web.model.GuiModules;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;
import fr.insee.arc.web.util.WebLoggerDispatcher;

/**
 * Will own all the utilitary methode used in the {@link GererNormeAction}
 * 
 * @author Pépin Rémi
 *
 */
@Component
public class GererNormeDao implements IDbConstant {

	private static final Logger LOGGER = LogManager.getLogger(GererNormeDao.class);

	private static final int INDEX_COLONNE_VARIABLE_TABLE_REGLE_MAPPING = 6;

    @Autowired
    private WebLoggerDispatcher loggerDispatcher;
    
    @Autowired
	@Qualifier("defaultVObjectService")
    private VObjectService viewObject;

	/**
	 * Return the SQL to get all the rules bond to a rule set. It suppose the a rule
	 * set is selected
	 * 
	 * @param viewRulesSet : the Vobject containing the rules
	 * @param table        : the sql to get the rules in the database
	 * @return an sql query to get all the rules bond to a rule set
	 */
	public PreparedStatementBuilder recupRegle(VObject viewRulesSet, String table) {
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		HashMap<String, String> type = viewRulesSet.mapHeadersType();
        requete.append("select * from " + table + " ");
        whereRuleSetEquals(requete, selection, type);
		return requete;
	}

	/** Appends a where clause for rulesets. */
	private void whereRuleSetEquals(PreparedStatementBuilder requete, Map<String, ArrayList<String>> selection,
			HashMap<String, String> type) {
		requete.append(" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
        requete.append(" and periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
        requete.append(" and validite_inf" + requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
        requete.append(" and validite_sup" + requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
        requete.append(" and version" + requete.sqlEqual(selection.get("version").get(0), type.get("version")));
	}

	/**
	 * Initialize the {@value GererNormeAction#viewNorme}. Request the full general
	 * norm table.
	 */
	public void initializeViewNorme(VObject viewNorme, String theTableName) {
		LoggerHelper.debug(LOGGER, "/* initializeNorme */");
		HashMap<String, String> defaultInputFields = new HashMap<>();

		viewObject.initialize(
				viewNorme,
				new PreparedStatementBuilder("SELECT id_famille, id_norme, periodicite, def_norme, def_validite, etat FROM arc.ihm_norme order by id_norme"), theTableName, defaultInputFields);
	}

	/**
	 * Initialize the {@value GererNormeAction#viewCalendar}. Only get the calendar
	 * link to the selected norm.
	 */
	public void initializeViewCalendar(VObject viewCalendar, VObject viewNorme, String theTableName) {
		LoggerHelper.debug(LOGGER, "/* initializeCalendar */");

		// get the norm selected
		Map<String, ArrayList<String>> selection = viewNorme.mapContentSelected();

		if (!selection.isEmpty()) {
			// Get the type of the column for casting
			HashMap<String, String> type = viewNorme.mapHeadersType();
			// requete de la vue
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
			requete.append("select id_norme, periodicite, validite_inf, validite_sup, etat from arc.ihm_calendrier");
			requete.append(
					" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
			requete.append(" and periodicite"
					+ requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));

			// construction des valeurs par défaut pour les ajouts
			HashMap<String, String> defaultInputFields = new HashMap<String, String>();
			defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
			defaultInputFields.put("periodicite", selection.get("periodicite").get(0));

			viewCalendar.setAfterInsertQuery(new PreparedStatementBuilder("select arc.fn_check_calendrier(); "));
			viewCalendar.setAfterUpdateQuery(new PreparedStatementBuilder("select arc.fn_check_calendrier(); "));

			// Create the vobject
			viewObject.initialize(viewCalendar, requete, theTableName, defaultInputFields);

		} else {
			viewObject.destroy(viewCalendar);
		}
	}

	/**
	 * Initialize the {@value GererNormeAction#viewRulesSet}. Only get the rulesset
	 * link to the selected norm and calendar.
	 */
	public void initializeViewRulesSet(VObject viewRulesSet, VObject viewCalendar, String theTableName) {
		loggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

		// Get the selected calendar for requesting the rule set
		Map<String, ArrayList<String>> selection = viewCalendar.mapContentSelected();
		if (!selection.isEmpty()) {
			HashMap<String, String> type = viewCalendar.mapHeadersType();
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
			requete.append(
					"select id_norme, periodicite, validite_inf, validite_sup, version, etat from arc.ihm_jeuderegle ");
			requete.append(
					" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
			requete.append(" and periodicite"
					+ requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
			requete.append(" and validite_inf"
					+ requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
			requete.append(" and validite_sup"
					+ requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));

			HashMap<String, String> defaultInputFields = new HashMap<>();
			defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
			defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
			defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
			defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));

			viewRulesSet.setAfterInsertQuery(new PreparedStatementBuilder("select arc.fn_check_jeuderegle(); "));
			viewRulesSet.setAfterUpdateQuery(new PreparedStatementBuilder("select arc.fn_check_jeuderegle(); "));

			viewObject.initialize(viewRulesSet, requete, theTableName, defaultInputFields);
		} else {
			viewObject.destroy(viewRulesSet);
		}
	}
	
	/**
	 * Initialize the {@value GererNormeAction#viewRulesSet}. Only get the rulesset
	 * link to the selected norm and calendar.
	 */
	public void initializeViewModules(VObject viewModules, VObject viewRulesSet) {
		loggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

		// Get the selected calendar for requesting the rule set
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		if (!selection.isEmpty()) {
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
			
			
			boolean union=false;
			int i=0;
			for (GuiModules module:GuiModules.values())
			{
				if (union)
				{
					requete.append("\n UNION ALL ");
				}
				else
				{
					union=true;
				}
				
				requete.append("\n SELECT "+(i++)+" as module_order, "+requete.quoteText(moduleIdentifier(module))+" as module_name");
			}
			

			HashMap<String, String> defaultInputFields = new HashMap<>();
			viewObject.initialize(viewModules, requete, null, defaultInputFields);

			if (viewModules.mapContentSelected().isEmpty())
			{
				viewModules.setSelectedLines(new ArrayList<>(Arrays.asList(true)));
			}
			
			
		} else {
			viewObject.destroy(viewModules);
		}
	}

	
	/** 
	 * Default fields for arc rules set
	 * @param selection
	 * @return
	 */
	private HashMap<String, String> defaultRuleInputFields(Map<String, ArrayList<String>> selection) {
		HashMap<String, String> defaultInputFields = new HashMap<>();
		defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
		defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
		defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
		defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
		defaultInputFields.put("version", selection.get("version").get(0));
		return defaultInputFields;
	}
	
	
	/**
	 * Initialize the {@link VObject} of a load ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeChargement(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		ArrayList<ArrayList<String>> moduleSelection =viewModules.listContentSelected();
				
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.load)))
		{
		    HashMap<String, String> type = viewRulesSet.mapHeadersType();
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,type_fichier, delimiter, format, commentaire from arc.ihm_chargement_regle");
            whereRuleSetEquals(requete, selection, type);
            
            viewObject.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(moduleView);
		}
	}

	
	/**
	 * Initialize the {@link VObject} of a load ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeNormage(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		ArrayList<ArrayList<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.structurize)))
		{
            HashMap<String, String> type = viewRulesSet.mapHeadersType();
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,id_classe,rubrique,rubrique_nmcl,commentaire from arc.ihm_normage_regle");
            whereRuleSetEquals(requete, selection, type);
            
            viewObject.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(moduleView);
		}
	}
	
	/**
	 * Initialize the {@link VObject} of a control ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeControle(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		ArrayList<ArrayList<String>> moduleSelection =viewModules.listContentSelected();
				
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.control)))
		{
            HashMap<String, String> type = viewRulesSet.mapHeadersType();
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition,blocking_threshold,error_row_processing,pre_action,xsd_ordre,xsd_label_fils,xsd_role,commentaire from arc.ihm_controle_regle");
            whereRuleSetEquals(requete, selection, type);
            
            viewObject.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(moduleView);
		}
	}

	
	/**
	 * Initialize the {@link VObject} of a filter ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeFiltrage(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		ArrayList<ArrayList<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.filter)))
		{
            HashMap<String, String> type = viewRulesSet.mapHeadersType();
            
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("select * from arc.ihm_filtrage_regle");
            whereRuleSetEquals(requete, selection, type);
            
            viewObject.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(moduleView);
		}
	}
	
	/**
	 * Initialize the {@link VObject} of the mapping rule. Only get the load
	 * rule link to the selected rule set.
	 */
	public void initializeMapping(VObject viewMapping, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		ArrayList<ArrayList<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.mapmodel)))
		{
			HashMap<String, String> type = viewRulesSet.mapHeadersType();

            PreparedStatementBuilder requete = new PreparedStatementBuilder(
                    "SELECT mapping.id_regle, mapping.id_norme, mapping.validite_inf, mapping.validite_sup, mapping.version, mapping.periodicite, mapping.variable_sortie, mapping.expr_regle_col, mapping.commentaire, variables.type_variable_metier type_sortie, variables.nom_table_metier nom_table_metier /*, variables.nom_table_metier nom_table_metier */ ");
            requete.append("\n  FROM arc.ihm_mapping_regle mapping INNER JOIN arc.ihm_jeuderegle jdr");
            requete.append("\n  ON mapping.id_norme     = jdr.id_norme     AND mapping.periodicite           = jdr.periodicite AND mapping.validite_inf = jdr.validite_inf AND mapping.validite_sup = jdr.validite_sup AND mapping.version = jdr.version");
            requete.append("\n  INNER JOIN arc.ihm_norme norme");
            requete.append("\n  ON norme.id_norme       = jdr.id_norme AND norme.periodicite   = jdr.periodicite");
            requete.append("\n  LEFT JOIN (SELECT id_famille, nom_variable_metier, type_variable_metier, string_agg(nom_table_metier,',') as nom_table_metier  FROM arc.ihm_mod_variable_metier group by id_famille, nom_variable_metier, type_variable_metier) variables");
            requete.append("\n  ON variables.id_famille = norme.id_famille AND variables.nom_variable_metier = mapping.variable_sortie");
            requete.append("\n  WHERE mapping.id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
            requete.append("\n  AND mapping.periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
            requete.append("\n  AND mapping.validite_inf" + requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
            requete.append("\n  AND mapping.validite_sup" + requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
            requete.append("\n  AND mapping.version" + requete.sqlEqual(selection.get("version").get(0), type.get("version")));
            
			viewObject.initialize(viewMapping,requete,theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(viewMapping);
		}
	}

	/**
	 * Initialize the {@link VObject} of the expression. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeExpression(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		loggerDispatcher.info(String.format("Initialize view table %s", theTableName), LOGGER);
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		ArrayList<ArrayList<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.expression)))
		{
            HashMap<String, String> type = viewRulesSet.mapHeadersType();
            PreparedStatementBuilder requete = new PreparedStatementBuilder();;
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,expr_nom, expr_valeur, commentaire from arc.ihm_expression");
            whereRuleSetEquals(requete, selection, type);
            viewObject.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(moduleView);
		}
	}

	/**
	 * Initialize the {@value GererNormeAction#viewJeuxDeReglesCopie}. Get in
	 * database all the reccord the rule sets.
	 * 
	 * @param viewJeuxDeReglesCopie
	 */
	public void initializeJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie, VObject viewRulesSet, VObject viewModules, 
			String theTableName, String scope) {
		LoggerHelper.info(LOGGER, "initializeJeuxDeReglesCopie");
		if (scope != null) {
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
	        requete.append("select id_norme, periodicite, validite_inf, validite_sup, version, etat from arc.ihm_jeuderegle ");
			HashMap<String, String> defaultInputFields = new HashMap<>();
			viewObject.initialize(viewJeuxDeReglesCopie, requete, theTableName, defaultInputFields);
		} else {
			viewObject.destroy(viewJeuxDeReglesCopie);
		}

	}

	/**
	 * Send a rule set to production.
	 */
	@SQLExecutor
	public void sendRuleSetToProduction(VObject viewRulesSet, String theTable) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		Date dNow = new Date();
		loggerDispatcher.warn("Rule set send to production", LOGGER);

		try {
			
			PreparedStatementBuilder requete= new PreparedStatementBuilder();
			requete.append("update " + theTable + " set last_init='"+ dateFormat.format(dNow) + "', operation=case when operation='R' then 'O' else operation end;");
			
			UtilitaireDao.get("arc").executeRequest(null, requete);
			viewRulesSet.setMessage("Go to production registered");

		} catch (ArcException e) {
			viewRulesSet.setMessage("Error in the go to production");
			LoggerHelper.warn(LOGGER, "Error in the go to production");

		}
	}


	/**
	 * 
	 * @param viewRulesSet
	 * @return
	 */
	public JeuDeRegle fetchJeuDeRegle(VObject viewRulesSet) {
        HashMap<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
        /*
         * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et calendrier
         */
        JeuDeRegle jdr = new JeuDeRegle();
        jdr.setIdNorme(selection.get("id_norme").get(0));
        jdr.setPeriodicite(selection.get("periodicite").get(0));
        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
        jdr.setVersion(selection.get("version").get(0));
        jdr.setEtat(selection.get("etat").get(0));
        return jdr;
	}

	/**
	 * Empty all the rules of a norm modul
	 * 
	 * @param table
	 * @return
	 */
	public void emptyRuleTable(VObject viewRulesSet, String table) {
		loggerDispatcher.info("Empty all the rules of a module", LOGGER);
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		HashMap<String, String> type = viewRulesSet.mapHeadersType();
		PreparedStatementBuilder requete= new PreparedStatementBuilder();
		requete.append("DELETE FROM " + table);
        requete.append(" WHERE id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
        requete.append(" AND periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
        requete.append(" AND validite_inf" + requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
        requete.append(" AND validite_sup" + requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
        requete.append(" AND version" + requete.sqlEqual(selection.get("version").get(0), type.get("version")));
        requete.append(" ;");

		try {
			UtilitaireDao.get("arc").executeRequest(null, requete);
		} catch (ArcException e) {
			LoggerHelper.error(LOGGER, String.format("Error when emptying the rules %s", e.toString()));

		}
	}

	public String createTableTest(String aNomTable, List<String> listRubrique) {
		 StringBuilder sb = new StringBuilder();
	        sb.append("DROP TABLE IF EXISTS " + aNomTable + ";");
	        sb.append("CREATE TABLE " + aNomTable);
	        sb.append("(id_norme text, periodicite text, id_source text, validite text, id integer, controle text, brokenrules text[] ");
	        
	        //Je m'assure que les ubriques de base ne sont pas dans la liste des rubriques du filtre (sinon requÃªte invalide),
	        //--> cas possible lorsque par exemple, le paramÃ¨tre {validite} apparait dans la rÃ¨gle de filtrage.
	        listRubrique.remove("id_norme");
	        listRubrique.remove("periodicite");
	        listRubrique.remove("id_source");
	        listRubrique.remove("validite");
	        listRubrique.remove("id");
	        listRubrique.remove("controle");
	        listRubrique.remove("brokenrules");

	        
	        for (String rub : listRubrique) {
	            if (rub.toUpperCase().startsWith("I")) {
	                sb.append("," + rub + " integer");
	            } else {
	                sb.append("," + rub + " text");
	            }

	        }
	        sb.append(");");
		return sb.toString();
	}

	
	public void calculerVariableToType(VObject viewNorme, Map<String, String> mapVariableToType,
			Map<String, String> mapVariableToTypeConso) throws ArcException {
		
		PreparedStatementBuilder requete=new PreparedStatementBuilder();
		requete.append("SELECT DISTINCT lower(nom_variable_metier) AS nom_variable_metier, type_variable_metier, type_consolidation AS type_sortie ");
		requete.append("\n FROM arc.ihm_mod_variable_metier ");
		requete.append("\n WHERE id_famille="+requete.quoteText(viewNorme.mapContentSelected().get("id_famille").get(0))+" ");
		
		ArrayList<ArrayList<String>> resultat = UtilitaireDao.get("arc").executeRequest(null, requete);
		
		for (int i = 2; i < resultat.size(); i++) {
			mapVariableToType.put(resultat.get(i).get(0), resultat.get(i).get(1));
			mapVariableToTypeConso.put(resultat.get(i).get(0), resultat.get(i).get(2));
		}
	}

	public Map<String, ArrayList<String>> calculerReglesAImporter(MultipartFile aFileUpload,
			List<RegleMappingEntity> listeRegle, EntityDao<RegleMappingEntity> dao,
			Map<String, String> mapVariableToType, Map<String, String> mapVariableToTypeConso) throws IOException {
		Map<String, ArrayList<String>> returned = new HashMap<>();
		returned.put("type_sortie", new ArrayList<String>());
		returned.put("type_consolidation", new ArrayList<String>());
		try (BufferedReader br = new BufferedReader(new InputStreamReader(aFileUpload.getInputStream(), StandardCharsets.UTF_8));){
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
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER, "error in calculerReglesAImporter", ex.getStackTrace());
		}
		return returned;
	}

	public boolean testerConsistanceRegleMapping(List<List<String>> data, String anEnvironnement,
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
		Set<String> variableToute = new HashSet<String>();
		variableToute.addAll(variableRegleCharge);
		variableToute.addAll(variableTableModele);
		boolean ok = true;
		loggerDispatcher.info("Les variables du modèle : " + variableTableModele, LOGGER);
		loggerDispatcher.info("Les variables des règles chargées : " + variableRegleCharge, LOGGER);
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


	/**
	 *
	 * @param returned
	 * @param index    -1 : en fin de tableau<br/>
	 *                 [0..size[ : le premier élément est ajouté à l'emplacement
	 *                 {@code index}, les suivants, juste après
	 * @param args
	 * @return
	 */
	public List<List<String>> ajouterInformationTableau(List<List<String>> returned, int index, String... args) {
		for (int i = 0; i < returned.size(); i++) {
			for (int j = 0; j < args.length; j++) {
				returned.get(i).add((index == -1 ? returned.get(i).size() : index + j), args[j]);
			}
		}
		return returned;
	}

	/**
	 * 
	 * @param vObjectToUpdate the vObject to update with file
	 * @param tableName       the
	 */
	@SQLExecutor
	public void uploadFileRule(VObject vObjectToUpdate, VObject viewRulesSet, MultipartFile theFileToUpload) {

		// Check if there is file
		if (theFileToUpload == null || theFileToUpload.isEmpty()) {
			// No file -> ko
			vObjectToUpdate.setMessage("Please select a file.");
		} else {
			// A file -> can process it
			LoggerHelper.debug(LOGGER, " filesUpload  : " + theFileToUpload);

			// before inserting in the final table, the rules will be inserted in a table to
			// test them
			String nomTableImage = FormatSQL.temporaryTableName(vObjectToUpdate.getTable() + "_img" + 0);
			
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(theFileToUpload.getInputStream(), StandardCharsets.UTF_8));) {
				// Get headers
				List<String> listHeaders = getHeaderFromFile(bufferedReader);

				/*
				 * Création d'une table temporaire (qui ne peut pas être TEMPORARY)
				 */
				PreparedStatementBuilder requete=new PreparedStatementBuilder();
			    requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");
			    requete.append("\n CREATE TABLE " + nomTableImage + " AS SELECT "//
				    + Format.untokenize(listHeaders, ", ") //
				    + "\n\t FROM " //
				    + vObjectToUpdate.getTable() //
				    + "\n\t WHERE false");


				UtilitaireDao.get("arc").executeRequest(null, requete);

				// Throwing away the first line
				String uselessLine = bufferedReader.readLine();
				LoggerHelper.debug(LOGGER, uselessLine + "is thrown away");
				

				// Importing the file in the database (COPY command)
				UtilitaireDao.get("arc").importing(null, nomTableImage, bufferedReader, true, false,
						IConstanteCaractere.semicolon);

			} catch (Exception ex) {
				vObjectToUpdate.setMessage("Error when uploading the file : " + ex.getMessage());
				LoggerHelper.error(LOGGER, ex, "uploadOutils()", "\n");
				// After the exception, the methode cant go further, so the better thing to do
				// is to quit it
				return;
			}
			LoggerHelper.debug(LOGGER, "Insert file in the " + nomTableImage + " table");

			Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();

			PreparedStatementBuilder requete=new PreparedStatementBuilder();

			requete.append("\n UPDATE " + nomTableImage + " SET ");
			requete.append("\n id_norme=" + requete.quoteText(selection.get("id_norme").get(0)));
			requete.append("\n, periodicite=" + requete.quoteText(selection.get("periodicite").get(0)));
			requete.append("\n, validite_inf=" + requete.quoteText(selection.get("validite_inf").get(0)) + "::date");
			requete.append("\n, validite_sup=" + requete.quoteText(selection.get("validite_sup").get(0)) + "::date");
			requete.append("\n, version=" + requete.quoteText(selection.get("version").get(0)));
			requete.append("\n ; ");
			
			requete.append("\n DELETE FROM " + vObjectToUpdate.getTable());
			requete.append("\n WHERE ");
			requete.append("\n id_norme=" + requete.quoteText(selection.get("id_norme").get(0)));
			requete.append("\n AND  periodicite=" + requete.quoteText(selection.get("periodicite").get(0)));
			requete.append("\n AND  validite_inf=" + requete.quoteText(selection.get("validite_inf").get(0)) + "::date");
			requete.append("\n AND  validite_sup=" + requete.quoteText(selection.get("validite_sup").get(0)) + "::date");
			requete.append("\n AND  version=" + requete.quoteText(selection.get("version").get(0)));
			requete.append("\n ; ");

			requete.append("\n INSERT INTO " + vObjectToUpdate.getTable() + " ");
			requete.append("\n SELECT * FROM " + nomTableImage + " ;");
			requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");

			try {
				UtilitaireDao.get("arc").executeRequest(null, requete);
			} catch (Exception ex) {
				vObjectToUpdate.setMessage("Error when uploading the file : " + ex.getMessage());
				LoggerHelper.error(LOGGER, ex, "uploadOutils()");
			}
		}

	}

	private static List<String> getHeaderFromFile(BufferedReader bufferedReader) throws IOException {
		String listeColonnesAggregees = bufferedReader.readLine();
		List<String> listeColonnes = Arrays.asList(listeColonnesAggregees.split(IConstanteCaractere.semicolon));
		LoggerHelper.debug(LOGGER, "Columns list : ", Format.untokenize(listeColonnes, ", "));
		return listeColonnes;
	}

	
	/**
	 * Return the module identifier
	 * "norManagement.load" identify the load module in the normManagement action and its international alias
	 * @param moduleName
	 * @return
	 */
	private String moduleIdentifier(GuiModules moduleName)
	{
		return GererNormeAction.ACTION_NAME+"."+moduleName.toString();
	}
	
}
