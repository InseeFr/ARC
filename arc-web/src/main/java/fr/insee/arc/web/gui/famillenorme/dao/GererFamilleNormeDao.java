package fr.insee.arc.web.gui.famillenorme.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.web.gui.all.util.ArcStringUtils;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

@Component
public class GererFamilleNormeDao extends VObjectHelperDao {

	protected static final int NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER = ViewEnum.VIEW_VARIABLE_METIER.getColumns()
			.size();

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
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewFamilleNorme, query, dataObjectService.getView(dataModelNormFamily),
				defaultInputFields);
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
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(
				ColumnEnum.ID_FAMILLE);
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
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(
				ColumnEnum.ID_FAMILLE, ColumnEnum.ID_APPLICATION);
		// initialize vobject
		vObjectService.initialize(viewHostAllowed, query, dataObjectService.getView(dataModelHostAllowed),
				defaultInputFields);
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
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(
				ColumnEnum.ID_FAMILLE);
		// initialize vobject
		vObjectService.initialize(viewTableMetier, query, dataObjectService.getView(dataModelTableMetier),
				defaultInputFields);
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

		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(
				ColumnEnum.ID_FAMILLE);
		vObjectService.initialize(viewVariableMetier, query, dataObjectService.getView(dataModelVariableMetier),
				defaultInputFields);
	}

	/**
	 * Delete a model table A synchronization with rules table is required after
	 * deletion Full transaction is required for consistency
	 * 
	 * @param viewTableMetier
	 * @param idFamille
	 * @throws ArcException
	 */
	public void execQueryDeleteTableMetier(VObject viewTableMetier, String idFamilleSelected) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(viewTableMetier));
		query.append(querySynchronizeRegleWithVariableMetier(idFamilleSelected));
		query.asTransaction();
	
		UtilitaireDao.get(0).executeRequest(null, query);
	}

	public void execQueryDeleteFamilleNorme(VObject viewFamilleNorme, String idFamilleSelected) throws ArcException {
	
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(viewFamilleNorme));
		query.append(GererFamilleNormeDao.querySynchronizeRegleWithVariableMetier(idFamilleSelected));
		query.asTransaction();
	
		UtilitaireDao.get(0).executeRequest(null, query);
	}

	public void execQueryUpdateVariableMetier(VObject viewVariableMetier, String idFamilleSelected)
			throws ArcException {
	
		isAnyNullVariablesDeclared(viewVariableMetier.mapUpdatedContent());
		
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
	
		// update name
		requete.append(queryUpdateRulesAndModelTablesOnVariableNameUpdate(viewVariableMetier, idFamilleSelected));
	
		// update other data model fields
	
		requete.append(
				addExistingVariableMetierWithoutSync(viewVariableMetier, viewVariableMetier.listOnlyUpdatedContent()));
	
		requete.append(mettreAJourInformationsVariables(viewVariableMetier));
	
		requete.append(GererFamilleNormeDao.querySynchronizeRegleWithVariableMetier(idFamilleSelected));
	
		UtilitaireDao.get(0).executeBlock(null, requete);
	
	}

	public void execQueryDeleteVariableMetier(VObject viewVariableMetier, String idFamilleSelected) throws ArcException {
	
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(deleteVariableMetierWithoutSync(viewVariableMetier));
		requete.append(GererFamilleNormeDao.querySynchronizeRegleWithVariableMetier(idFamilleSelected));
		UtilitaireDao.get(0).executeBlock(null, requete);
		
	}

	public void execQueryAddVariableMetier(VObject viewVariableMetier, String idFamilleSelected) throws ArcException {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(addNonExistingVariableMetierWithoutSync(viewVariableMetier));
		query.append(
				GererFamilleNormeDao.querySynchronizeRegleWithVariableMetier(idFamilleSelected));
		UtilitaireDao.get(0).executeRequest(null, query);
	}

	private static ArcPreparedStatementBuilder querySynchronizeRegleWithVariableMetier(String idFamille) {
		ArcPreparedStatementBuilder requeteListeSupprRegleMapping = requeteListeSupprRegleMapping(idFamille);
		ArcPreparedStatementBuilder requeteListeAddRegleMapping = requeteListeAddRegleMapping(idFamille);

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(requeteListeAddRegleMapping.toString() + ";\n");
		requete.append(requeteListeSupprRegleMapping.toString() + ";");
		
		return requete;
	}

	/**
	 * Sélection des règles à détruire
	 */
	private static ArcPreparedStatementBuilder requeteListeSupprRegleMapping(String idFamille) {
		ArcPreparedStatementBuilder requeteListeSupprRegleMapping = new ArcPreparedStatementBuilder(
				"DELETE FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " regle\n");
		requeteListeSupprRegleMapping.append("  WHERE NOT EXISTS (");
		requeteListeSupprRegleMapping.append("    SELECT 1 FROM " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName()
				+ " var INNER JOIN " + ViewEnum.IHM_FAMILLE.getFullName() + " fam\n");
		requeteListeSupprRegleMapping.append("    ON var.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.variable_sortie=var.nom_variable_metier\n");
		requeteListeSupprRegleMapping.append("    INNER JOIN arc.ihm_norme norme\n");
		requeteListeSupprRegleMapping.append("    ON norme.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.id_norme=norme.id_norme\n");
		requeteListeSupprRegleMapping.append("    WHERE fam.id_famille = " + requeteListeSupprRegleMapping.quoteText(idFamille));
		requeteListeSupprRegleMapping.append("  )");
		requeteListeSupprRegleMapping.append("    AND EXISTS (SELECT 1 FROM " + ViewEnum.IHM_NORME.getFullName()
				+ " norme INNER JOIN " + ViewEnum.IHM_FAMILLE.getFullName() + " fam");
		requeteListeSupprRegleMapping.append("      ON norme.id_famille=fam.id_famille");
		requeteListeSupprRegleMapping.append("      AND regle.id_norme=norme.id_norme");
		requeteListeSupprRegleMapping.append("      WHERE fam.id_famille = " + requeteListeSupprRegleMapping.quoteText(idFamille) + ")");
		return requeteListeSupprRegleMapping;
	}

	/**
	 * Sélection des règles à créer
	 */
	private static ArcPreparedStatementBuilder requeteListeAddRegleMapping(String idFamille) {
		ArcPreparedStatementBuilder requeteListeAddRegleMapping = new ArcPreparedStatementBuilder(
				"INSERT INTO " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + "(");
		requeteListeAddRegleMapping.append("id_regle");
		requeteListeAddRegleMapping.append(", id_norme");
		requeteListeAddRegleMapping.append(", validite_inf");
		requeteListeAddRegleMapping.append(", validite_sup");
		requeteListeAddRegleMapping.append(", version");
		requeteListeAddRegleMapping.append(", periodicite");
		requeteListeAddRegleMapping.append(", variable_sortie");
		requeteListeAddRegleMapping.append(", expr_regle_col");
		requeteListeAddRegleMapping.append(", commentaire)");
		requeteListeAddRegleMapping.append("\n  SELECT (SELECT max(id_regle) FROM "
				+ ViewEnum.IHM_MAPPING_REGLE.getFullName() + ") + row_number() over ()");
		requeteListeAddRegleMapping.append(", norme.id_norme");
		requeteListeAddRegleMapping.append(", calendrier.validite_inf");
		requeteListeAddRegleMapping.append(", calendrier.validite_sup");
		requeteListeAddRegleMapping.append(", jdr.version");
		requeteListeAddRegleMapping.append(", norme.periodicite");
		requeteListeAddRegleMapping.append(", var.nom_variable_metier");
		requeteListeAddRegleMapping.append(", '" + FormatSQL.NULL + "'");
		requeteListeAddRegleMapping.append(", " + FormatSQL.NULL + "::text ");
		requeteListeAddRegleMapping.append("\n  FROM (SELECT DISTINCT id_famille, nom_variable_metier FROM "
				+ ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + ") var INNER JOIN "
				+ ViewEnum.IHM_FAMILLE.getFullName() + " fam");
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
		requeteListeAddRegleMapping.append("\n  WHERE fam.id_famille = " + requeteListeAddRegleMapping.quoteText(idFamille));
		requeteListeAddRegleMapping.append("\n    AND lower(jdr.sandbox) <> 'inactif'");
		requeteListeAddRegleMapping.append("\n    AND lower(calendrier.etat) = '1'");
		requeteListeAddRegleMapping.append("\n    AND NOT EXISTS (");
		requeteListeAddRegleMapping
				.append("\n      SELECT 1 FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.variable_sortie=var.nom_variable_metier");
		requeteListeAddRegleMapping.append("\n        AND regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    ) AND EXISTS (");
		requeteListeAddRegleMapping
				.append("\n      SELECT 1 FROM " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    )");
		return requeteListeAddRegleMapping;
	}

	/**
	 * Build the query that update 1- the metadata table containing model variables
	 * 2- the model table by changing the variable name
	 * 
	 * @param viewVariableMetier
	 * @param idFamilleSelected
	 * @return
	 * @throws ArcException
	 */
	private ArcPreparedStatementBuilder queryUpdateRulesAndModelTablesOnVariableNameUpdate(VObject viewVariableMetier, String idFamilleSelected)
			throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		List<List<String>> lBefore = viewVariableMetier.listContentBeforeUpdate();
		List<List<String>> lAfter = viewVariableMetier.listContentAfterUpdate();

		int nameIndex = viewVariableMetier.getHeadersDLabel().indexOf(ColumnEnum.NOM_VARIABLE_METIER.getColumnName());

		for (List<String> modifiedLine : lAfter) {
			int indexOfVar = nameIndex;
			modifiedLine.set(indexOfVar, ArcStringUtils.cleanUpVariable(modifiedLine.get(indexOfVar)));
		}

		// part 1 : update data field names
		for (int i = 0; i < lAfter.size(); i++) {
			String nameAfter = lAfter.get(i).get(nameIndex);
			String nameBefore = lBefore.get(i).get(nameIndex);
			if (nameAfter != null && !nameBefore.equals(nameAfter)) {
				requete.append(queryUpdateMetadataVariablesTable(idFamilleSelected, nameBefore, nameAfter));

				requete.append(queryUpdateRulesTableUsingModifiedVariable(idFamilleSelected, nameBefore, nameAfter));

				requete.append(queryAlterColumnNameInModelTables(viewVariableMetier, i, nameBefore, nameAfter));

			}

		}

		requete.append("\n");

		return requete;
	}

	/**
	 * Mise à jour du nom de la variable dans la table des règles de mapping
	 * @param idFamilleSelected
	 * @param nameBefore
	 * @param nameAfter
	 * @return
	 */
	private ArcPreparedStatementBuilder queryUpdateRulesTableUsingModifiedVariable(String idFamilleSelected, String nameBefore,
			String nameAfter) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		requete.append("\n");
		requete.append("update " + ViewEnum.IHM_MAPPING_REGLE.getFullName() + " a set variable_sortie=" + requete.quoteText(nameAfter));
		requete.append("where variable_sortie=" + requete.quoteText(nameBefore));
		requete.append("and exists (select from arc.ihm_norme b where a.id_norme=b.id_norme and b.id_famille="
				+ requete.quoteText(idFamilleSelected) + "); ");
		return requete;

	}

	/**
	 * Mise à jour du nom de la variable dans la table contenant les variables métier des modeles
	 * @param idFamilleSelected
	 * @param nameBefore
	 * @param nameAfter
	 * @return
	 */
	private ArcPreparedStatementBuilder queryUpdateMetadataVariablesTable(String idFamilleSelected, String nameBefore, String nameAfter) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		requete.append("\n");
		requete.append("update " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + " set nom_variable_metier=" + requete.quoteText(nameAfter));
		requete.append("where nom_variable_metier=" + requete.quoteText(nameBefore));
		requete.append("and id_famille=" + requete.quoteText(idFamilleSelected) + "; ");
		return requete;
	}

	/**
	 * modification du nom de la variable dans les tables métiers des différents environements
	 * @param viewVariableMetier
	 * @param tableIndexInView
	 * @param nameBefore
	 * @param nameAfter
	 * @return
	 * @throws ArcException
	 */
	private ArcPreparedStatementBuilder queryAlterColumnNameInModelTables(VObject viewVariableMetier, int tableIndexInView,
			String nameBefore, String nameAfter) throws ArcException {

		List<String> listeEnvironnement = UtilitaireDao.get(0).getList(null,
				new ArcPreparedStatementBuilder("SELECT distinct replace(id,'.','_') FROM arc.ext_etat_jeuderegle where isenv").toString(),
				new ArrayList<>());

		Map<String, List<String>> mBefore = viewVariableMetier.mapContentBeforeUpdate();
		List<List<String>> lBefore = viewVariableMetier.listContentBeforeUpdate();

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		for (String envName : listeEnvironnement) {
			for (int k = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; k < mBefore.size(); k++) {
				String nomVeridique = envName + "." + viewVariableMetier.getHeadersDLabel().get(k);

				// si la variable est définie pour cette table
				// et si la table existe
				// et si la colonne existe
				if (StringUtils.isNotBlank(lBefore.get(tableIndexInView).get(k))
						&& Boolean.TRUE.equals(UtilitaireDao.get(0).isTableExiste(null, nomVeridique))
						&& UtilitaireDao.get(0).isColonneExiste(null, nomVeridique, nameBefore)) {

					requete.append("\n");
					requete.append("ALTER TABLE " + nomVeridique + " RENAME " + nameBefore + " TO " + nameAfter + ";");

				}
			}
		}

		return requete;
	}

	private void isAnyNullVariablesDeclared(Map<String, List<String>> mapContentAfterUpdate)
			throws ArcException {
		for (int i = 0; i < mapContentAfterUpdate.get(ColumnEnum.NOM_VARIABLE_METIER.getColumnName()).size(); i++) {
			String nomVariable = mapContentAfterUpdate.get(ColumnEnum.NOM_VARIABLE_METIER.getColumnName()).get(i);
			if (nomVariable == null) {
				throw new ArcException(ArcExceptionMessage.GUI_FAMILLENORME_VARIABLE_NULL);
			}
		}
	}

	/**
	 * Ajoute une variable métier à des tables par UPDATE (la variable existe déjà)
	 *
	 * @param message
	 */
	private ArcPreparedStatementBuilder addExistingVariableMetierWithoutSync(VObject viewVariableMetier,
			List<List<String>> listContent) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		/**
		 * Pour chaque ligne à UPDATE
		 */
		for (int i = 0; i < listContent.size(); i++) {
			/**
			 * Et pour l'ensemble des tables métier
			 */
			for (int j = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; j < viewVariableMetier.mapContentAfterUpdate(i)
					.size(); j++) {
				/**
				 * Si une variable est à "oui" pour cette table alors qu'elle n'y était pas...
				 */
				if (StringUtils.isNotBlank(listContent.get(i).get(j))
						&& StringUtils.isBlank(viewVariableMetier.listContentBeforeUpdate().get(i).get(j))) {
					/**
					 * ... on l'ajoute
					 */
					requete.append("INSERT INTO " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + " (");
					ArcPreparedStatementBuilder values = new ArcPreparedStatementBuilder();
					for (int k = 0; k < NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; k++) {
						if (k > 0) {
							requete.append(", ");
							values.append(", ");
						}
						requete.append(viewVariableMetier.getHeadersDLabel().get(k));
						
						values.append(values.quoteText(listContent.get(i).get(k)));
						values.append("::" + viewVariableMetier.getHeadersDType().get(k));
					}
					
					requete.append(", nom_table_metier) VALUES (");
					values.append(",") //
					.append(values.quoteText(viewVariableMetier.getHeadersDLabel().get(j))) //
					.append("::text);\n");
					
					requete.append(values);
				}
			}
		}
		return requete;
	}

	/**
	 * Ajoute une variable métier par INSERT (la variable métier n'existfe pas et
	 * doit être ajoutée)
	 *
	 * @param message
	 * @throws ArcException
	 */
	private ArcPreparedStatementBuilder addNonExistingVariableMetierWithoutSync(VObject viewVariableMetier) throws ArcException {
		
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		
		boolean blank = true;
		for (int i = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; i < viewVariableMetier.getInputFields().size(); i++) {
			if (StringUtils.isNotBlank(viewVariableMetier.getInputFields().get(i))) {

				// au moins une table est renseignée pour la variable
				blank = false;

				String nomVariableMetier = viewVariableMetier
						.getInputFieldFor(ColumnEnum.NOM_VARIABLE_METIER.getColumnName());
				viewVariableMetier.setInputFieldFor(ColumnEnum.NOM_VARIABLE_METIER.getColumnName(),
						ArcStringUtils.cleanUpVariable(nomVariableMetier));

				checkIsValide(viewVariableMetier.getInputFields());

				ArcPreparedStatementBuilder values = new ArcPreparedStatementBuilder();
				
				requete.append("INSERT INTO " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + " (");

				for (int j = 0; j < NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; j++) {
					if (j > 0) {
						requete.append(", ");
						values.append(", ");
					}
					requete.append(viewVariableMetier.getHeadersDLabel().get(j));
					values.append(values.quoteText(viewVariableMetier.getInputFields().get(j))+ "::"	+ viewVariableMetier.getHeadersDType().get(j));
				}
				requete.append(", nom_table_metier) VALUES (");
				values.append("," + values.quoteText(viewVariableMetier.getHeadersDLabel().get(i)) + "::text);\n");
				
				requete.append(values);

			}
		}

		if (blank) {
			throw new ArcException(ArcExceptionMessage.GUI_FAMILLENORME_VARIABLE_NO_TARGET_TABLE);
		}

		return requete;
	}

	/**
	 * check if the input field doesn't exist in IHM_MOD_VARIABLE_METIER
	 * 
	 * @param inputFields
	 * @return
	 * @throws ArcException
	 */
	private static void checkIsValide(List<String> inputFields) throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT count(1) FROM " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName())//
				.append("\n WHERE id_famille=" + requete.quoteText(inputFields.get(0)))//
				.append("\n AND nom_variable_metier=" + requete.quoteText(inputFields.get(1)) + ";");
		if (UtilitaireDao.get(0).getInt(null, requete) > 0) {
			throw new ArcException(ArcExceptionMessage.GUI_FAMILLENORME_VARIABLE_ALREADY_EXISTS);
		}
	}

	/**
	 * Update the description fields for variable
	 * 
	 * @param someViewVariableMetier
	 * @return
	 */
	private ArcPreparedStatementBuilder mettreAJourInformationsVariables(VObject someViewVariableMetier) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		for (int i = 0; i < someViewVariableMetier.listOnlyUpdatedContent().size(); i++) {
			if (i > 0) {
				requete.append("\n");
			}

			Map<String, List<String>> content = someViewVariableMetier.mapOnlyUpdatedContent();
			ArcPreparedStatementBuilder requeteLocale = new ArcPreparedStatementBuilder(
					"UPDATE " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName() + " a ");
			requeteLocale.append("\n  SET type_consolidation = ");
			requeteLocale.append(computeMapcontent(content, "type_consolidation", i));
			requeteLocale.append(",\n    description_variable_metier = ");
			requeteLocale.append(computeMapcontent(content, "description_variable_metier", i));
			requeteLocale.append("\n  WHERE id_famille = "
					+ requeteLocale.quoteText(someViewVariableMetier.mapOnlyUpdatedContent().get(ColumnEnum.ID_FAMILLE.getColumnName()).get(i)));
			requeteLocale.append("\n    AND nom_variable_metier = '" + someViewVariableMetier.mapOnlyUpdatedContent()
					.get(ColumnEnum.NOM_VARIABLE_METIER.getColumnName()).get(i) + "'");
			requete.append(requeteLocale).append(";");
		}
		return requete;
	}

	private String computeMapcontent(Map<String, List<String>> content, String columnName, int index) {
		if (content.get(columnName) == null || content.get(columnName).get(index) == null) {
			return columnName;
		} else {
			return FormatSQL.quoteText(content.get(columnName).get(index));
		}
	}

	/**
	 * Détruit une variable métier dans la table de référence
	 * ihm_mod_variable_metier. Ne détruit pas les colonnes correspondantes dans les
	 * tables d'environnement concernées.
	 *
	 * @param message
	 * @param listContentBeforeUpdate Peut être à null
	 */
	private ArcPreparedStatementBuilder deleteVariableMetierWithoutSync(VObject viewVariableMetier) {
		
		Map<String, List<String>> map = viewVariableMetier.mapContentSelected();
		
		ArcPreparedStatementBuilder delete = new ArcPreparedStatementBuilder();
		/**
		 * Pour chaque variable :<br/>
		 * 1. Lister les tables<br/>
		 * 2. Supprimer cette colonne des tables listées<br/>
		 * 3. Supprimer cette variable*table de ihm_mod_variable_metier<br/>
		 * 4. Supprimer la règle correspondante de ihm_mapping_regle
		 */
		StringBuilder listeTable = new StringBuilder();
		for (int j = 0; j < map.get(ColumnEnum.NOM_VARIABLE_METIER.getColumnName()).size(); j++) {
			String nomVariable = map.get(ColumnEnum.NOM_VARIABLE_METIER.getColumnName()).get(j);
			/**
			 * On prépare la liste des tables comportant effectivement la variable
			 */
			listeTable.setLength(0);
			/**
			 * Pour chaque table trouvée
			 */
			for (int i = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; i < map.size(); i++) {
				listeTable.append("[" + viewVariableMetier.getHeadersDLabel().get(i) + "]");
			}
			delete.append("DELETE FROM " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName()
					+ " WHERE id_famille=" + delete.quoteText(map.get(ColumnEnum.ID_FAMILLE.getColumnName()).get(j))
					+ " AND nom_variable_metier=" + delete.quoteText(nomVariable) + "::text"
					+ " AND " + delete.quoteText(listeTable.toString()) + " like '%['||nom_table_metier||']%';\n");
		}
		return delete;
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
