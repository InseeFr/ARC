package fr.insee.arc.web.gui.maintenanceparametre.dao;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectHelperDao;
import fr.insee.arc.web.util.VObjectService;

public class MaintenanceParametreDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public MaintenanceParametreDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	/**
	 * dao call to build parameters object
	 * 
	 * @param viewParameters
	 */
	public void initializeViewParameters(VObject viewParameters) {
		ViewEnum dataModelParameters = ViewEnum.PARAMETER;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append("row_number() over (order by description,key,val)"); // ?
		query.append(SQL.AS);
		query.append("i");
		query.append(SQL.COMMA);
		query.append(query.sqlListeOfColumnsFromModel(dataModelParameters));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelParameters));
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewParameters, query, dataObjectService.getView(dataModelParameters), defaultInputFields);
	}

}
