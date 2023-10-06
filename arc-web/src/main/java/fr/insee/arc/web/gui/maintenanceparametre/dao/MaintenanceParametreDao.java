package fr.insee.arc.web.gui.maintenanceparametre.dao;

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
public class MaintenanceParametreDao extends VObjectHelperDao {

	/**
	 * dao call to build parameters object
	 * 
	 * @param viewParameters
	 */
	public void initializeViewParameters(VObject viewParameters) {
		ViewEnum dataModelParameters = ViewEnum.PARAMETER;
		String nameOfViewParameters = dataObjectService.getView(dataModelParameters);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		StringBuilder columns = query.sqlListeOfColumnsFromModel(dataModelParameters);
		query.build(SQL.SELECT, "row_number() over (order by description,key,val)", SQL.AS, ColumnEnum.I, SQL.COMMA, columns);
		query.build(SQL.FROM, nameOfViewParameters);
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewParameters, query, nameOfViewParameters, defaultInputFields);
	}

}
