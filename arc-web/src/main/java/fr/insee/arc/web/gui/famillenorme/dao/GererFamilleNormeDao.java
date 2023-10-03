package fr.insee.arc.web.gui.famillenorme.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;


@Component
public class GererFamilleNormeDao extends VObjectHelperDao {

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
	
	public static String querySynchronizeRegleWithVariableMetier(String idFamille) {
		StringBuilder requeteListeSupprRegleMapping = requeteListeSupprRegleMapping(idFamille);
		StringBuilder requeteListeAddRegleMapping = requeteListeAddRegleMapping(idFamille);
		
		StringBuilder requete = new StringBuilder();
		requete.append(requeteListeAddRegleMapping.toString() + ";\n");
		requete.append(requeteListeSupprRegleMapping.toString() + ";");
		return requete.toString();
	}
	
	/**
	 * Sélection des règles à détruire
	 */
	private static StringBuilder requeteListeSupprRegleMapping(String idFamille) {
		StringBuilder requeteListeSupprRegleMapping = new StringBuilder("DELETE FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " regle\n");
		requeteListeSupprRegleMapping.append("  WHERE NOT EXISTS (");
		requeteListeSupprRegleMapping
		.append("    SELECT 1 FROM " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + " var INNER JOIN " + ViewEnum.IHM_FAMILLE.getFullName() + " fam\n");
		requeteListeSupprRegleMapping.append("    ON var.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.variable_sortie=var.nom_variable_metier\n");
		requeteListeSupprRegleMapping.append("    INNER JOIN arc.ihm_norme norme\n");
		requeteListeSupprRegleMapping.append("    ON norme.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.id_norme=norme.id_norme\n");
		requeteListeSupprRegleMapping.append("    WHERE fam.id_famille = '" + idFamille + "'");
		requeteListeSupprRegleMapping.append("  )");
		requeteListeSupprRegleMapping
		.append("    AND EXISTS (SELECT 1 FROM " + ViewEnum.IHM_NORME.getFullName() + " norme INNER JOIN " + ViewEnum.IHM_FAMILLE.getFullName() + " fam");
		requeteListeSupprRegleMapping.append("      ON norme.id_famille=fam.id_famille");
		requeteListeSupprRegleMapping.append("      AND regle.id_norme=norme.id_norme");
		requeteListeSupprRegleMapping.append("      WHERE fam.id_famille = '" + idFamille + "')");
		return requeteListeSupprRegleMapping;
	}

	/**
	 * Sélection des règles à créer
	 */
	private static StringBuilder requeteListeAddRegleMapping(String idFamille) {
		StringBuilder requeteListeAddRegleMapping = new StringBuilder("INSERT INTO " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + "(");
		requeteListeAddRegleMapping.append("id_regle");
		requeteListeAddRegleMapping.append(", id_norme");
		requeteListeAddRegleMapping.append(", validite_inf");
		requeteListeAddRegleMapping.append(", validite_sup");
		requeteListeAddRegleMapping.append(", version");
		requeteListeAddRegleMapping.append(", periodicite");
		requeteListeAddRegleMapping.append(", variable_sortie");
		requeteListeAddRegleMapping.append(", expr_regle_col");
		requeteListeAddRegleMapping.append(", commentaire)");
		requeteListeAddRegleMapping
				.append("\n  SELECT (SELECT max(id_regle) FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + ") + row_number() over ()");
		requeteListeAddRegleMapping.append(", norme.id_norme");
		requeteListeAddRegleMapping.append(", calendrier.validite_inf");
		requeteListeAddRegleMapping.append(", calendrier.validite_sup");
		requeteListeAddRegleMapping.append(", jdr.version");
		requeteListeAddRegleMapping.append(", norme.periodicite");
		requeteListeAddRegleMapping.append(", var.nom_variable_metier");
		requeteListeAddRegleMapping.append(", '" + FormatSQL.NULL + "'");
		requeteListeAddRegleMapping.append(", " + FormatSQL.NULL + "::text ");
		requeteListeAddRegleMapping.append("\n  FROM (SELECT DISTINCT id_famille, nom_variable_metier FROM "
				+ ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + ") var INNER JOIN " + ViewEnum.IHM_FAMILLE.getFullName() + " fam");
		requeteListeAddRegleMapping.append("\n    ON var.id_famille=fam.id_famille");
		requeteListeAddRegleMapping.append("\n  INNER JOIN " + ViewEnum.IHM_NORME.getFullName() + " norme");
		requeteListeAddRegleMapping.append("\n    ON fam.id_famille=norme.id_famille");
		requeteListeAddRegleMapping.append("\n  INNER JOIN " + ViewEnum.IHM_CALENDRIER.getFullName() + " calendrier");
		requeteListeAddRegleMapping
				.append("\n    ON calendrier.id_norme=norme.id_norme AND calendrier.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n  INNER JOIN " + ViewEnum.IHM_JEUDEREGLE.getFullName() + " jdr");
		requeteListeAddRegleMapping
				.append("\n    ON calendrier.id_norme=jdr.id_norme AND calendrier.periodicite=jdr.periodicite");
		requeteListeAddRegleMapping.append(
				"\n      AND calendrier.validite_inf=jdr.validite_inf AND calendrier.validite_sup=jdr.validite_sup");
		requeteListeAddRegleMapping.append("\n  WHERE fam.id_famille = '" + idFamille + "'");
		requeteListeAddRegleMapping.append("\n    AND lower(jdr.etat) <> 'inactif'");
		requeteListeAddRegleMapping.append("\n    AND lower(calendrier.etat) = '1'");
		requeteListeAddRegleMapping.append("\n    AND NOT EXISTS (");
		requeteListeAddRegleMapping.append("\n      SELECT 1 FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.variable_sortie=var.nom_variable_metier");
		requeteListeAddRegleMapping.append("\n        AND regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    ) AND EXISTS (");
		requeteListeAddRegleMapping.append("\n      SELECT 1 FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    )");
		return requeteListeAddRegleMapping;
	}
	
	

	/**
	 * Delete a model table
	 * A synchronization with rules table is required after deletion
	 * Full transaction is required for consistency
	 * @param viewTableMetier
	 * @param idFamille
	 * @throws ArcException
	 */
	public void execQueryDeleteTableMetier(VObject viewTableMetier, String idFamille) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(viewTableMetier));
		query.append(querySynchronizeRegleWithVariableMetier(idFamille));
		query.asTransaction();

		UtilitaireDao.get(0).executeRequest(null, query);
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
