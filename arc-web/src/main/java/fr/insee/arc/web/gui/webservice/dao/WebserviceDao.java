package fr.insee.arc.web.gui.webservice.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;

@Component
public class WebserviceDao extends VObjectHelperDao {
	
	/**
	 * dao call to build webservice vobject
	 * 
	 * @param viewWsContext
	 */
	public void initializeWebserviceContext(VObject viewWsContext) {
        ViewEnum dataModelWsContext = ViewEnum.IHM_WS_CONTEXT;
		String nameOfViewWsContext = dataObjectService.getView(dataModelWsContext);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, nameOfViewWsContext);
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewWsContext, query, nameOfViewWsContext, defaultInputFields);
	}
	
	/**
	 * dao call to build selected webservice vobject
	 * 
	 * @param viewWsQuery
	 */
	public void initializeWebserviceQuery(VObject viewWsQuery) {
        ViewEnum dataModelWsQuery = ViewEnum.IHM_WS_QUERY;
		String nameOfViewWsQuery = dataObjectService.getView(dataModelWsQuery);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, nameOfViewWsQuery);
		query.build(SQL.WHERE, sqlEqualWithFirstSelectedRecord(ColumnEnum.SERVICE_NAME), SQL.AND, sqlEqualWithFirstSelectedRecord(ColumnEnum.CALL_ID));
		// default value
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.SERVICE_NAME, ColumnEnum.CALL_ID);
		// initialize vobject
		vObjectService.initialize(viewWsQuery, query, nameOfViewWsQuery, defaultInputFields);
	}

}
