package fr.insee.arc.web.gui.nomenclature.dao;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectHelperDao;
import fr.insee.arc.web.util.VObjectService;

public class GererNomenclatureDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public GererNomenclatureDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	/**
	 * dao call to build list nomenclatures vobject
	 * 
	 * @param viewListNomenclatures
	 */
	public void initializeViewListNomenclatures(VObject viewListNomenclatures) {
		ViewEnum dataModelListNomenclatures = ViewEnum.IHM_NMCL;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(ViewEnum.IHM_NMCL));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelListNomenclatures));
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewListNomenclatures, query, dataObjectService.getView(dataModelListNomenclatures), defaultInputFields);
	}
	
	/**
	 * dao call to build nomenclature vobject
	 * 
	 * @param viewNomenclature
	 * @param nomTable
	 * @param tableSelected
	 */
	public void initializeViewNomenclature(VObject viewNomenclature, String nomTable, String tableSelected) {
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append("*"); // ?
		query.append(SQL.FROM);
		query.append("arc." + tableSelected); // ?
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
        defaultInputFields.put(nomTable, tableSelected);
		// initialize vobject
		vObjectService.initialize(viewNomenclature, query, "arc." + tableSelected, defaultInputFields);
	}
	
    
	public void initializeViewSchemaNmcl(VObject viewSchemaNmcl) {
		ViewEnum dataModelSchemaNmcl = ViewEnum.IHM_SCHEMA_NMCL;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelSchemaNmcl));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelSchemaNmcl));
		query.append(SQL.WHERE);
		query.append(ColumnEnum.TYPE_NMCL).append("=");
		query.append(query.quoteText(typeNomenclature(selectedRecords.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0))));

		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
	    defaultInputFields.put(ColumnEnum.TYPE_NMCL.getColumnName(), typeNomenclature(selectedRecords.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0)));
	    
		// initialize vobject
		vObjectService.initialize(viewSchemaNmcl, query, dataObjectService.getView(dataModelSchemaNmcl), defaultInputFields);
	}

	
	/**
	 * compute the nomenclature type
	 * @param nomTable
	 * @return
	 */
	private static String typeNomenclature(String nomTable) {
		String[] tokens = nomTable.split(fr.insee.arc.utils.textUtils.IConstanteCaractere.underscore);
		StringBuilder typeNomenclature = new StringBuilder();
		for (int i = 0; i < tokens.length - 1; i++) {
			typeNomenclature
					.append((i > 0 ? fr.insee.arc.utils.textUtils.IConstanteCaractere.underscore : "") + tokens[i]);
		}
		return typeNomenclature.toString();
	}
	
}
