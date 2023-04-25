package fr.insee.arc.web.gui.famillenorme.dao;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectHelperDao;
import fr.insee.arc.web.util.VObjectService;

public class GererFamilleNormeDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public GererFamilleNormeDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	/**
	 * dao call to build norm family vobject
	 * 
	 * @param viewFamilleNorme
	 */
	public void initializeViewFamilleNorme(VObject viewFamilleNorme) {
		ViewEnum dataModelNormFamily = ViewEnum.IHM_FAMILLE;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelNormFamily));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelNormFamily));
		query.append(SQL.ORDER_BY);
		query.append(ColumnEnum.ID_FAMILLE);
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewFamilleNorme, query, dataObjectService.getView(dataModelNormFamily), defaultInputFields);
	}
	
	/**
	 * dao call to build client vobject
	 * 
	 * @param viewClient
	 */
	public void initializeViewClient(VObject viewClient) {
		ViewEnum dataModelClient = ViewEnum.IHM_CLIENT;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelClient));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelClient));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE));
		// default value
		HashMap<String, String> defaultInputFields =
				buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE);
		// initialize vobject
		vObjectService.initialize(viewClient, query, dataObjectService.getView(dataModelClient), defaultInputFields);
	}
	
	/**
	 * dao call to build host allowed vobject
	 * 
	 * @param viewHostAllowed
	 */
	public void initializeViewHostAllowed(VObject viewHostAllowed) {
		ViewEnum dataModelHostAllowed = ViewEnum.IHM_WEBSERVICE_WHITELIST;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelHostAllowed));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelHostAllowed));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_APPLICATION));
		// default value
		HashMap<String, String> defaultInputFields =
				buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_APPLICATION);
		// initialize vobject
		vObjectService.initialize(viewHostAllowed, query, dataObjectService.getView(dataModelHostAllowed), defaultInputFields);
	}
	
	/**
	 * dao call to build business table vobject
	 * 
	 * @param viewTableMetier
	 */
	public void initializeViewTableMetier(VObject viewTableMetier) {
		ViewEnum dataModelTableMetier = ViewEnum.IHM_MOD_TABLE_METIER;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelTableMetier));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelTableMetier));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE));
		// default value
		HashMap<String, String> defaultInputFields =
				buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE);
		// initialize vobject
		vObjectService.initialize(viewTableMetier, query, dataObjectService.getView(dataModelTableMetier), defaultInputFields);
	}

}
