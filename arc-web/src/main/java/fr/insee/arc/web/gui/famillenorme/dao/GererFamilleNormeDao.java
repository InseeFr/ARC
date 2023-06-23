package fr.insee.arc.web.gui.famillenorme.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
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
	
	/**
	 * dao call to list table metier
	 */
	public List<String> getListeTableMetierFamille() {
		ViewEnum dataModelTableMetier = ViewEnum.IHM_MOD_TABLE_METIER;
		// query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(ColumnEnum.NOM_TABLE_METIER);
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelTableMetier));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE));
		// return list
		return UtilitaireDao.get(0).getList(null, query.toString(), new ArrayList<>());
	}
	
	/**
	 * dao call to build business variable vobject
	 * 
	 * @param viewVariableMetier
	 * @param listeTableMetier 
	 */
	public void initializeViewVariableMetier(VObject viewVariableMetier, List<String> listeTableFamille) {
		ViewEnum dataModelVariableMetier = ViewEnum.IHM_MOD_VARIABLE_METIER;

		ArcPreparedStatementBuilder left = new ArcPreparedStatementBuilder("\n (SELECT nom_variable_metier");
		for (int i = 0; i < listeTableFamille.size(); i++) {
			left.append(
					",\n  CASE WHEN '['||string_agg(nom_table_metier,'][' ORDER BY nom_table_metier)||']' LIKE '%['||'"
							+ listeTableFamille.get(i) + "'||']%' then 'x' else '' end " + listeTableFamille.get(i));
		}
		left.append("\n FROM " + dataObjectService.getView(dataModelVariableMetier) + " ");
		left.append("\n WHERE ");
		left.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE));
		left.append("\n GROUP BY nom_variable_metier) left_side");

		ArcPreparedStatementBuilder right = new ArcPreparedStatementBuilder();
		right.append(
				"\n (SELECT id_famille, nom_variable_metier, type_variable_metier, type_consolidation, description_variable_metier\n");
		right.append("\n FROM " + dataObjectService.getView(dataModelVariableMetier) + "\n");
		right.append("\n WHERE ");
		right.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE));
		right.append(
				"\n GROUP BY id_famille, nom_variable_metier, type_variable_metier, type_consolidation, description_variable_metier) right_side");

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(
				"SELECT right_side.id_famille, right_side.nom_variable_metier, right_side.type_variable_metier, right_side.type_consolidation, right_side.description_variable_metier");
		for (int i = 0; i < listeTableFamille.size(); i++) {
			query.append(", " + listeTableFamille.get(i));
		}
		query.append("\n FROM ");
		query.append(left);
		query.append(" INNER JOIN ");
		query.append(right);

		query.append("\n ON left_side.nom_variable_metier = right_side.nom_variable_metier");
		
		HashMap<String, String> defaultInputFields = 
				buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_FAMILLE);
		vObjectService.initialize(viewVariableMetier, query, dataObjectService.getView(dataModelVariableMetier),
				defaultInputFields);
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
