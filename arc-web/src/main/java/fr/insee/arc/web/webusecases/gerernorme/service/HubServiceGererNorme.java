package fr.insee.arc.web.webusecases.gerernorme.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.model.GuiModules;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;
import fr.insee.arc.web.webusecases.ArcWebGenericService;
import fr.insee.arc.web.webusecases.gerernorme.dao.GererNormeDao;
import fr.insee.arc.web.webusecases.gerernorme.model.ModelGererNorme;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HubServiceGererNorme extends ArcWebGenericService<ModelGererNorme> implements IDbConstant {
	
	protected static final String RESULT_SUCCESS = "/jsp/gererNorme.jsp";
	
	protected static final String SELECTED_RULESET_TABLE = "SELECTED_RULESET_TABLE";
	protected static final String SELECTED_RULESET_NAME = "SELECTED_RULESET_NAME";

	private static final Logger LOGGER = LogManager.getLogger(HubServiceGererNorme.class);
	
	@Autowired
	protected ModelGererNorme views;
	
    @Autowired
    private VObjectService viewObject;
	
	// The action Name
	public static final String ACTION_NAME="normManagement";

	@Override
	public void putAllVObjects(ModelGererNorme model) {
		views.setViewNorme(vObjectService.preInitialize(model.getViewNorme()));
		views.setViewCalendrier(vObjectService.preInitialize(model.getViewCalendrier()));
		views.setViewJeuxDeRegles(vObjectService.preInitialize(model.getViewJeuxDeRegles()));
		views.setViewModules(vObjectService.preInitialize(model.getViewModules()));
		views.setViewChargement(vObjectService.preInitialize(model.getViewChargement()));
		views.setViewNormage(vObjectService.preInitialize(model.getViewNormage()));
		views.setViewControle(vObjectService.preInitialize(model.getViewControle()));
		views.setViewFiltrage(vObjectService.preInitialize(model.getViewFiltrage()));
		views.setViewMapping(vObjectService.preInitialize(model.getViewMapping()));
		views.setViewExpression(vObjectService.preInitialize(model.getViewExpression()));
		views.setViewJeuxDeReglesCopie(vObjectService.preInitialize(model.getViewJeuxDeReglesCopie()));
		
		putVObject(views.getViewNorme(),
				t -> initializeViewNorme(t, dataObjectService.getView(ViewEnum.IHM_NORME)));
		//
		putVObject(views.getViewCalendrier(), t -> initializeViewCalendar(t, views.getViewNorme(),
				dataObjectService.getView(ViewEnum.IHM_CALENDRIER) ));
		//
		putVObject(views.getViewJeuxDeRegles(), t -> initializeViewRulesSet(t, views.getViewCalendrier(),
				dataObjectService.getView(ViewEnum.IHM_JEUDEREGLE) ));
		//
		putVObject(views.getViewModules(), t -> initializeViewModules(t, views.getViewJeuxDeRegles()));
		//
		putVObject(views.getViewChargement(), t -> initializeChargement(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_CHARGEMENT_REGLE) ));
		//
		putVObject(views.getViewNormage(), t -> initializeNormage(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_NORMAGE_REGLE) ));
		//
		putVObject(views.getViewControle(), t -> initializeControle(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_CONTROLE_REGLE) ));
		//
		putVObject(views.getViewFiltrage(), t -> initializeFiltrage(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_FILTRAGE_REGLE) ));
		//
		putVObject(views.getViewMapping(), t -> initializeMapping(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_MAPPING_REGLE)  ));
		//
		putVObject(views.getViewExpression(), t -> initializeExpression(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_EXPRESSION) ));
		//
		putVObject(views.getViewJeuxDeReglesCopie(), t -> initializeJeuxDeReglesCopie(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_JEUDEREGLE) , getScope()));
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}
	
	
	/**
	 * Initialize the {@value HubServiceGererNorme#viewNorme}. Call dao to create the view
	 */
	public void initializeViewNorme(VObject viewNorme, String theDataViewName) {
		LoggerHelper.debug(LOGGER, "/* initializeNorme */");
	
		GererNormeDao.initializeViewNorme(viewObject, viewNorme, theDataViewName);
		
	}

	/**
	 * Initialize the {@value HubServiceGererNorme#viewCalendar}. Only get the calendar
	 * link to the selected norm.
	 */
	public void initializeViewCalendar(VObject viewCalendar, VObject viewNorme, String theTableName) {
		LoggerHelper.debug(LOGGER, "/* initializeCalendar */");

		// get the norm selected
		Map<String, ArrayList<String>> selection = viewNorme.mapContentSelected();

		// if a norm is selected, trigger the call to dao to construct calendar view
		if (!selection.isEmpty()) {
			
			// Get the type of the column for casting
			HashMap<String, String> type = viewNorme.mapHeadersType();
			
			GererNormeDao.initializeViewCalendar(viewObject, viewCalendar, theTableName, selection, type);
		} else {
			viewObject.destroy(viewCalendar);
		}
	}

	/**
	 * Initialize the {@value HubServiceGererNorme#viewRulesSet}. Only get the rulesset
	 * link to the selected norm and calendar.
	 */
	public void initializeViewRulesSet(VObject viewRulesSet, VObject viewCalendar, String theTableName) {
		loggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

		// Get the selected calendar for requesting the rule set
		Map<String, ArrayList<String>> selection = viewCalendar.mapContentSelected();
		if (!selection.isEmpty()) {
			HashMap<String, String> type = viewCalendar.mapHeadersType();
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
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

			viewRulesSet.setAfterInsertQuery(new ArcPreparedStatementBuilder("select arc.fn_check_jeuderegle(); "));
			viewRulesSet.setAfterUpdateQuery(new ArcPreparedStatementBuilder("select arc.fn_check_jeuderegle(); "));

			viewObject.initialize(viewRulesSet, requete, theTableName, defaultInputFields);
		} else {
			viewObject.destroy(viewRulesSet);
		}
	}
	
	/**
	 * Initialize the {@value HubServiceGererNorme#viewRulesSet}. Only get the rulesset
	 * link to the selected norm and calendar.
	 */
	public void initializeViewModules(VObject viewModules, VObject viewRulesSet) {
		loggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

		// Get the selected calendar for requesting the rule set
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		if (!selection.isEmpty()) {
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			
			
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
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
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
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
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
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
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
            
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
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

            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder(
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
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();;
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,expr_nom, expr_valeur, commentaire from arc.ihm_expression");
            whereRuleSetEquals(requete, selection, type);
            viewObject.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			viewObject.destroy(moduleView);
		}
	}

	/**
	 * Initialize the {@value HubServiceGererNorme#viewJeuxDeReglesCopie}. Get in
	 * database all the reccord the rule sets.
	 * 
	 * @param viewJeuxDeReglesCopie
	 */
	public void initializeJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie, VObject viewRulesSet, VObject viewModules, 
			String theTableName, String scope) {
		LoggerHelper.info(LOGGER, "initializeJeuxDeReglesCopie");
		if (scope != null) {
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
	        requete.append("select id_norme, periodicite, validite_inf, validite_sup, version, etat from arc.ihm_jeuderegle ");
			HashMap<String, String> defaultInputFields = new HashMap<>();
			viewObject.initialize(viewJeuxDeReglesCopie, requete, theTableName, defaultInputFields);
		} else {
			viewObject.destroy(viewJeuxDeReglesCopie);
		}

	}
	


	/** Appends a where clause for rulesets. */
	protected void whereRuleSetEquals(ArcPreparedStatementBuilder requete, Map<String, ArrayList<String>> selection,
			HashMap<String, String> type) {
		requete.append(" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
        requete.append(" and periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
        requete.append(" and validite_inf" + requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
        requete.append(" and validite_sup" + requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
        requete.append(" and version" + requete.sqlEqual(selection.get("version").get(0), type.get("version")));
	}

	/**
	 * Return the module identifier
	 * "norManagement.load" identify the load module in the normManagement action and its international alias
	 * @param moduleName
	 * @return
	 */
	private String moduleIdentifier(GuiModules moduleName)
	{
		return HubServiceGererNorme.ACTION_NAME+"."+moduleName.toString();
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
	 * Empty all the rules of a norm module
	 * 
	 * @param table
	 * @return
	 */
	protected void emptyRuleTable(VObject viewRulesSet, String table) {
		loggerDispatcher.info("Empty all the rules of a module", LOGGER);
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		HashMap<String, String> type = viewRulesSet.mapHeadersType();
		ArcPreparedStatementBuilder requete= new ArcPreparedStatementBuilder();
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

	/**
	 * 
	 * @param vObjectToUpdate the vObject to update with file
	 * @param tableName       the
	 */
	protected void uploadFileRule(VObject vObjectToUpdate, VObject viewRulesSet, MultipartFile theFileToUpload) {

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
				ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
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

			ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();

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
	 * @return the selectedJeuDeRegle
	 */
	public String getSelectedJeuDeRegle() {
		return this.views.getViewJeuxDeReglesCopie().getCustomValue(SELECTED_RULESET_TABLE);
	}

}
