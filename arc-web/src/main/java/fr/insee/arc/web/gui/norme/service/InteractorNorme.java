package fr.insee.arc.web.gui.norme.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.model.GuiModules;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.norme.dao.GererNormeDao;
import fr.insee.arc.web.gui.norme.model.ModelNorme;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorNorme extends ArcWebGenericService<ModelNorme, GererNormeDao> {
	
	protected static final String RESULT_SUCCESS = "jsp/gererNorme.jsp";
	
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
		
		putVObject(views.getViewNorme(), t -> initializeViewNorme(t));
		//
		putVObject(views.getViewCalendrier(), t -> initializeViewCalendar(t, views.getViewNorme()));
		//
		putVObject(views.getViewJeuxDeRegles(), t -> initializeViewRulesSet(t, views.getViewCalendrier()));
		//
		putVObject(views.getViewModules(), t -> initializeViewModules(t, views.getViewJeuxDeRegles()));
		//
		putVObject(views.getViewChargement(), t -> initializeChargement(t, views.getViewJeuxDeRegles(), views.getViewModules()));
		//
		putVObject(views.getViewNormage(), t -> initializeNormage(t, views.getViewJeuxDeRegles(), views.getViewModules()));
		//
		putVObject(views.getViewControle(), t -> initializeControle(t, views.getViewJeuxDeRegles(), views.getViewModules()));
		//
		putVObject(views.getViewMapping(), t -> initializeMapping(t, views.getViewJeuxDeRegles(), views.getViewModules()));
		//
		putVObject(views.getViewExpression(), t -> initializeExpression(t, views.getViewJeuxDeRegles(), views.getViewModules()));
		//
		putVObject(views.getViewJeuxDeReglesCopie(), t -> initializeJeuxDeReglesCopie(t, getScope()));
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
	 * Initialize the {@link VObject} of a structurize ruleset. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeNormage(VObject viewNormage, VObject viewRulesSet, VObject viewModules) {
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
	public void initializeControle(VObject viewControle, VObject viewRulesSet, VObject viewModules) {
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
				
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.control)))
		{
			dao.setSelectedRecords(selection);
			dao.initializeControle(viewControle);
		} else {
			vObjectService.destroy(viewControle);
		}
	}
	
	/**
	 * Initialize the {@link VObject} of the mapping rule. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeMapping(VObject viewMapping, VObject viewRulesSet, VObject viewModules) {
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.mapmodel)))
		{
			dao.setSelectedRecords(selection);
			dao.initializeMapping(viewMapping);
		} else {
			vObjectService.destroy(viewMapping);
		}
	}

	/**
	 * Initialize the {@link VObject} of the expression. Only
	 * get the load rule link to the selected rule set.
	 */
	public void initializeExpression(VObject viewExpression, VObject viewRulesSet, VObject viewModules) {
		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		List<List<String>> moduleSelection =viewModules.listContentSelected();
		
		if (!selection.isEmpty() && !moduleSelection.isEmpty()
				&& moduleSelection.get(0).get(1).equals(moduleIdentifier(GuiModules.expression)))
		{
			dao.setSelectedRecords(selection);
			dao.initializeExpression(viewExpression);
		} else {
			vObjectService.destroy(viewExpression);
		}
	}

	/**
	 * Initialize the {@value InteractorNorme#viewJeuxDeReglesCopie}. Get in
	 * database all the reccord the rule sets.
	 * 
	 * @param viewJeuxDeReglesCopie
	 */
	public void initializeJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie, String scope) {
		LoggerHelper.info(LOGGER, "initializeJeuxDeReglesCopie");
		if (scope != null) {
			dao.initializeJeuxDeReglesCopie(viewJeuxDeReglesCopie);
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
	 * @return the selectedJeuDeRegle
	 */
	public String getSelectedJeuDeRegle() {
		return this.views.getViewJeuxDeReglesCopie().getCustomValue(SELECTED_RULESET_TABLE);
	}
	
}
