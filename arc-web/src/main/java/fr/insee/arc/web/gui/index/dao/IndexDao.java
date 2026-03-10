package fr.insee.arc.web.gui.index.dao;

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
public class IndexDao extends VObjectHelperDao {

	/**
	 * dao call to build index vobject (sandboxes and users overview)
	 * 
	 * @param viewIndex
	 */
	public void initializeViewIndex(VObject viewIndex) {
		ViewEnum dataModelIndex = ViewEnum.EXT_ETAT_JEUDEREGLE;
		String nameOfViewIndex = dataObjectService.getView(dataModelIndex);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "nullif(substring(id from '[0123456789]+'),'')", SQL.CAST_OPERATOR, "int", SQL.AS, ColumnEnum.ID, SQL.COMMA); 
		query.build("upper(substring(id from '\\.(.*)'))", SQL.AS, ColumnEnum.VAL, SQL.COMMA, ColumnEnum.ENV_DESCRIPTION);
		query.build(SQL.FROM, nameOfViewIndex, SQL.WHERE, ColumnEnum.ISENV);
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewIndex, query, nameOfViewIndex, defaultInputFields);
	}

}
