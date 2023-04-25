package fr.insee.arc.web.gui.export.dao;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectHelperDao;
import fr.insee.arc.web.util.VObjectService;

public class ExportDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public ExportDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	/**
	 * dao call to build export vobject
	 * 
	 * @param viewExport
	 * @param bacASable
	 */
	public void initializeViewExport(VObject viewExport, String bacASable) {
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append("file_name, zip, table_to_export, headers, nulls, filter_table, order_table, nomenclature_export, columns_array_header, columns_array_value, etat");
		query.append(SQL.FROM);
		query.append(bacASable + ".export");
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewExport, query, bacASable + ".export", defaultInputFields);
	}

}
