package fr.insee.arc.web.gui.index.dao;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectHelperDao;
import fr.insee.arc.web.util.VObjectService;

public class IndexDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public IndexDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	/**
	 * dao call to build index vobject (sandboxes and users overview)
	 * 
	 * @param viewIndex
	 */
	public void initializeViewIndex(VObject viewIndex) {
		ViewEnum dataModelIndex = ViewEnum.EXT_ETAT_JEUDEREGLE;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append("nullif(substring(id from '[0123456789]+'),'')"); // ?
		query.append(SQL.CAST_OPERATOR);
		query.append("int");
		query.append(SQL.AS);
		query.append(ColumnEnum.ID);
		query.append(SQL.COMMA);
		query.append("upper(substring(id from '\\\\.(.*)'))"); // ?
		query.append(SQL.AS);
		query.append(ColumnEnum.VAL);
		query.append(SQL.COMMA);
		query.append(ColumnEnum.ENV_DESCRIPTION);
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelIndex));
		query.append(SQL.WHERE);
		query.append(ColumnEnum.ISENV);
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewIndex, query, dataObjectService.getView(dataModelIndex), defaultInputFields);
	}

}
