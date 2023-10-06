package fr.insee.arc.web.gui.norme.service;

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

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.model.GuiModules;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.norme.dao.GererNormeDao;
import fr.insee.arc.web.gui.norme.model.ModelNorme;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorNorme extends ArcWebGenericService<ModelNorme, GererNormeDao> {
	
	protected static final String RESULT_SUCCESS = "/jsp/gererNorme.jsp";
	
	protected static final String SELECTED_RULESET_TABLE = "SELECTED_RULESET_TABLE";
	protected static final String SELECTED_RULESET_NAME = "SELECTED_RULESET_NAME";

	private static final Logger LOGGER = LogManager.getLogger(InteractorNorme.class);
	
	@Autowired
	protected ModelNorme views;

	// The action Name
	public static final String ACTION_NAME="normManagement";

	@Override
	public void putAllVObjects(ModelNorme model) {

		views.setViewNorme(vObjectService.preInitialize(model.getViewNorme()));
		views.setViewCalendrier(vObjectService.preInitialize(model.getViewCalendrier()));
		views.setViewJeuxDeRegles(vObjectService.preInitialize(model.getViewJeuxDeRegles()));
		views.setViewModules(vObjectService.preInitialize(model.getViewModules()));
		views.setViewChargement(vObjectService.preInitialize(model.getViewChargement()));
		views.setViewNormage(vObjectService.preInitialize(model.getViewNormage()));
		views.setViewControle(vObjectService.preInitialize(model.getViewControle()));
		views.setViewMapping(vObjectService.preInitialize(model.getViewMapping()));
		views.setViewExpression(vObjectService.preInitialize(model.getViewExpression()));
		views.setViewJeuxDeReglesCopie(vObjectService.preInitialize(model.getViewJeuxDeReglesCopie()));
		
		putVObject(views.getViewNorme(),
				t -> initializeViewNorme(t));
		//
		putVObject(views.getViewCalendrier(), t -> initializeViewCalendar(t, views.getViewNorme()));
		//
		putVObject(views.getViewJeuxDeRegles(), t -> initializeViewRulesSet(t, views.getViewCalendrier()));
		//
		putVObject(views.getViewModules(), t -> initializeViewModules(t, views.getViewJeuxDeRegles()));
		//
		putVObject(views.getViewChargement(), t -> initializeChargement(t, views.getViewJeuxDeRegles(), views.getViewModules()));
		//
		putVObject(views.getViewNormage(), t -> initializeNormage(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_NORMAGE_REGLE) ));
		//
		putVObject(views.getViewControle(), t -> initializeControle(t, views.getViewJeuxDeRegles(), views.getViewModules(),
				dataObjectService.getView(ViewEnum.IHM_CONTROLE_REGLE) ));
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
	 * Initialize the {@value InteractorNorme#viewNorme}. Call dao to create the view
	 */
	public void initializeViewNorme(VObject viewNorme) {
		LoggerHelper.debug(LOGGER, "/* initializeNorme */");
		dao.initializeViewNorme(viewNorme);
	}

	/**
	 * Initialize the {@value InteractorNorme#viewCalendar}. Only get the calendar
	 * link to the selected norm.
	 */
	public void initializeViewCalendar(VObject viewCalendar, VObject viewNorme) {
		LoggerHelper.debug(LOGGER, "/* initializeCalendar */");

		// get the norm selected records
		Map<String, List<String>> viewNormSelectedRecords = viewNorme.mapContentSelected();

		// if a norm is selected, trigger the call to dao to construct calendar view
		if (!viewNormSelectedRecords.isEmpty()) {

			dao.setSelectedRecords(viewNormSelectedRecords);
			dao.initializeViewCalendar(viewCalendar);
			
		} else {
			vObjectService.destroy(viewCalendar);
		}
	}

	/**
	 * Initialize the {@value InteractorNorme#viewRulesSet}. Only get the rulesset
	 * link to the selected norm and calendar.
	 */
	public void initializeViewRulesSet(VObject viewRulesSet, VObject viewCalendar) {
		loggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

		// Get the selected calendar for requesting the rule set
		Map<String, List<String>> viewCalendarSelectedRecords = viewCalendar.mapContentSelected();
		
		// if a calendar is selected, trigger the call to dao to construct rulesset view
		if (!viewCalendarSelectedRecords.isEmpty()) {
			
			dao.setSelectedRecords(viewCalendarSelectedRecords);
			dao.initializeViewRulesSet(viewRulesSet);
		} else {
			vObjectService.destroy(viewRulesSet);
		}
	}
	
	/**
	 * Initialize the {@value InteractorNorme#viewRulesSet}. Only get the rulesset
	 * link to the selected norm and calendar.
	 */
	public void initializeViewModules(VObject viewModules, VObject viewRulesSet) {
		loggerDispatcher.info("/* initializeViewRulesSet *", LOGGER);

		// Get the selected ruleset
		Map<String, List<String>> viewRulesSetSelectedRecords = viewRulesSet.mapContentSelected();
		
		// if any ruleset selected, display and initialized the modules
		if (!viewRulesSetSelectedRecords.isEmpty()) {
			
			dao.setSelectedRecords(viewRulesSetSelectedRecords);
			dao.initializeViewModules(viewModules, InteractorNorme::moduleIdentifier);
			
			// select the first panel if nothing is selected in the module
			if (viewModules.mapContentSelected().isEmpty())
			{
				viewModules.setSelectedLines(new ArrayList<>(Arrays.asList(true)));
			}
			
		} else {
			vObjectService.destroy(viewModules);
		}
	}
	
	/**
	 * Initialize the {@link VObject} of a load ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeChargement(VObject viewChargement, VObject viewRulesSet, VObject viewModules) {
		
		
		Map<String, List<String>> viewRulesSetSelectedRecords = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
				
		if (!viewRulesSetSelectedRecords.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.load)))
		{
			dao.setSelectedRecords(viewRulesSetSelectedRecords);
			dao.initializeViewChargement(viewChargement);
			
		} else {
			vObjectService.destroy(viewChargement);
		}
	}

	/**
	 * Initialize the {@link VObject} of a load ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeNormage(VObject viewNormage, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.structurize)))
		{
			dao.setSelectedRecords(selection);
			dao.initializeNormage(viewNormage);
		} else {
			vObjectService.destroy(viewNormage);
		}
	}
	
	/**
	 * Initialize the {@link VObject} of a control ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeControle(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
				
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.control)))
		{
            Map<String, String> type = viewRulesSet.mapHeadersType();
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition,blocking_threshold,error_row_processing,pre_action,xsd_ordre,xsd_label_fils,xsd_role,commentaire from arc.ihm_controle_regle");
            whereRuleSetEquals(requete, selection, type);
            
            vObjectService.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			vObjectService.destroy(moduleView);
		}
	}
	
	/**
	 * Initialize the {@link VObject} of the mapping rule. Only get the load
	 * rule link to the selected rule set.
	 */
	public void initializeMapping(VObject viewMapping, VObject viewRulesSet, VObject viewModules, String theTableName) {
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.mapmodel)))
		{
			Map<String, String> type = viewRulesSet.mapHeadersType();

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
            
            vObjectService.initialize(viewMapping,requete,theTableName, defaultRuleInputFields(selection));
		} else {
			vObjectService.destroy(viewMapping);
		}
	}

	/**
	 * Initialize the {@link VObject} of the expression. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeExpression(VObject moduleView, VObject viewRulesSet, VObject viewModules, String theTableName) {
		loggerDispatcher.info(String.format("Initialize view table %s", theTableName), LOGGER);
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.expression)))
		{
            Map<String, String> type = viewRulesSet.mapHeadersType();
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();;
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,expr_nom, expr_valeur, commentaire from arc.ihm_expression");
            whereRuleSetEquals(requete, selection, type);
            vObjectService.initialize(moduleView, requete, theTableName, defaultRuleInputFields(selection));
		} else {
			vObjectService.destroy(moduleView);
		}
	}

	/**
	 * Initialize the {@value InteractorNorme#viewJeuxDeReglesCopie}. Get in
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
			vObjectService.initialize(viewJeuxDeReglesCopie, requete, theTableName, defaultInputFields);
		} else {
			vObjectService.destroy(viewJeuxDeReglesCopie);
		}

	}
	


	/** Appends a where clause for rulesets. */
	protected void whereRuleSetEquals(ArcPreparedStatementBuilder requete, Map<String, List<String>> selection,
			Map<String, String> type) {
		requete.append(" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
        requete.append(" and periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
        requete.append(" and validite_inf" + requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
        requete.append(" and validite_sup" + requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
        requete.append(" and version" + requete.sqlEqual(selection.get("version").get(0), type.get("version")));
	}

	/**
	 * Return the module identifier
	 * "normManagement.load" identify the load module in the normManagement action and its international alias
	 * @param moduleName
	 * @return
	 */
	private static String moduleIdentifier(GuiModules moduleName)
	{
		return InteractorNorme.ACTION_NAME+"."+moduleName.toString();
	}

	
	/** 
	 * Default fields for arc rules set
	 * @param selection
	 * @return
	 */
	private HashMap<String, String> defaultRuleInputFields(Map<String, List<String>> selection) {
		HashMap<String, String> defaultInputFields = new HashMap<>();
		defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
		defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
		defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
		defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
		defaultInputFields.put("version", selection.get("version").get(0));
		return defaultInputFields;
	}




	/**
	 * @return the selectedJeuDeRegle
	 */
	public String getSelectedJeuDeRegle() {
		return this.views.getViewJeuxDeReglesCopie().getCustomValue(SELECTED_RULESET_TABLE);
	}
	
}
