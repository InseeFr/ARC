package fr.insee.arc.web.gui.norme.dao;

import java.util.HashMap;
import java.util.function.Function;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.gui.all.model.GuiModules;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

public class GererNormeDao extends VObjectHelperDao {

	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public GererNormeDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}

	/**
	 * dao call to build norm vobject
	 * 
	 * @param viewObject
	 * @param viewNorme
	 * @param theTableName
	 */
	public void initializeViewNorme(VObject viewNorme) {

		ViewEnum dataModelNorm = ViewEnum.IHM_NORME;

		HashMap<String, String> defaultInputFields = new HashMap<>();

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelNorm));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelNorm));
		query.append(SQL.ORDER_BY);
		query.append(ColumnEnum.ID_NORME);

		// Initialize the vobject
		vObjectService.initialize(viewNorme, query, dataObjectService.getView(dataModelNorm), defaultInputFields);
	}

	/**
	 * dao call to build calendar vobject
	 * 
	 * @param viewObject
	 * @param viewCalendar
	 * @param theTableName
	 * @param selection
	 */
	public void initializeViewCalendar(VObject viewCalendar) {

		ViewEnum dataModelCalendar = ViewEnum.IHM_CALENDRIER;

		// requete de la vue
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelCalendar));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelCalendar));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));

		// build the default value when adding a record
		HashMap<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE);

		// check constraints on calendar after insert or update
		ArcPreparedStatementBuilder queryCheckConstraint = new ArcPreparedStatementBuilder();
		queryCheckConstraint.append(SQL.SELECT).append("arc.fn_check_calendrier()").append(SQL.END_QUERY);

		viewCalendar.setAfterInsertQuery(queryCheckConstraint);
		viewCalendar.setAfterUpdateQuery(queryCheckConstraint);

		// Initialize the vobject
		vObjectService.initialize(viewCalendar, query, dataObjectService.getView(dataModelCalendar),
				defaultInputFields);

	}

	/**
	 * dao call to build ruleset vobject
	 * 
	 * @param viewRulesSet
	 */
	public void initializeViewRulesSet(VObject viewRulesSet) {

		ViewEnum dataModelRulesSet = ViewEnum.IHM_JEUDEREGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelRulesSet));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelRulesSet));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));

		// build the default value when adding a record
		HashMap<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP);

		// check constraints on rulesets after insert or update
		ArcPreparedStatementBuilder queryCheckConstraint = new ArcPreparedStatementBuilder();
		queryCheckConstraint.append(SQL.SELECT).append("arc.fn_check_jeuderegle()").append(SQL.END_QUERY);

		viewRulesSet.setAfterInsertQuery(queryCheckConstraint);
		viewRulesSet.setAfterUpdateQuery(queryCheckConstraint);

		vObjectService.initialize(viewRulesSet, query, dataObjectService.getView(dataModelRulesSet),
				defaultInputFields);
	}

	/**
	 * dao call to build module menu vobject
	 * 
	 * @param viewRulesSet
	 */
	public void initializeViewModules(VObject viewModules, Function<GuiModules, String> functionGetModuleName) {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		boolean union = false;
		int i = 0;
		for (GuiModules module : GuiModules.values()) {
			if (union) {
				query.append(SQL.UNION_ALL);
			} else {
				union = true;
			}
			query.append(moduleQuery(i++, module, functionGetModuleName));
		}

		vObjectService.initialize(viewModules, query, null, new HashMap<>());
	}

	/**
	 * build query for module module are set in record each module record contains -
	 * a number (order in guimodules) that indexes the module - a name obtained by
	 * applying functionGetModuleName
	 * 
	 * @param moduleIndex
	 * @param module
	 * @param functionGetModuleName
	 * @return
	 */
	private ArcPreparedStatementBuilder moduleQuery(int moduleIndex, GuiModules module,
			Function<GuiModules, String> functionGetModuleName) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append(SQL.SELECT);
		query.append(moduleIndex);
		query.append(SQL.AS);
		query.append(ColumnEnum.MODULE_ORDER);

		query.append(SQL.COMMA);

		query.append(query.quoteText(functionGetModuleName.apply(module)));
		query.append(SQL.AS);
		query.append(ColumnEnum.MODULE_NAME);

		return query;
	}
	
	/**
	 * Query to get load rules view
	 * @param viewNormage
	 */
	public void initializeViewChargement(VObject viewChargement) {

		ViewEnum dataModelChargement = ViewEnum.IHM_CHARGEMENT_REGLE;

	    ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
	    
	    query.append(SQL.SELECT);
	    query.append(query.sqlListeOfColumnsFromModel(dataModelChargement));
	    query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelChargement));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));
	    
		// build the default value when adding a record
		HashMap<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);

		vObjectService.initialize(viewChargement, query, dataObjectService.getView(dataModelChargement), defaultInputFields);		
	}

	/**
	 * Query to get normage rules view
	 * @param viewNormage
	 */
	public void initializeNormage(VObject viewNormage) {
		
		ViewEnum dataModelChargement = ViewEnum.IHM_NORMAGE_REGLE;

	    ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
	    
	    query.append(SQL.SELECT);
	    query.append(query.sqlListeOfColumnsFromModel(dataModelChargement));
	    query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelChargement));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));
		
		// build the default value when adding a record
		HashMap<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
						ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);
				
		vObjectService.initialize(viewNormage, query, dataObjectService.getView(dataModelChargement), defaultInputFields);
	}

	public VObjectService getvObjectService() {
		return vObjectService;
	}

	public void setvObjectService(VObjectService vObjectService) {
		this.vObjectService = vObjectService;
	}

	public DataObjectService getDataObjectService() {
		return dataObjectService;
	}

	public void setDataObjectService(DataObjectService dataObjectService) {
		this.dataObjectService = dataObjectService;
	}
	
	
	
}
