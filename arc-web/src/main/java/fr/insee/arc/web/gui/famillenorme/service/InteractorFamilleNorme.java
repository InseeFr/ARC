package fr.insee.arc.web.gui.famillenorme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.famillenorme.dao.GererFamilleNormeDao;
import fr.insee.arc.web.gui.famillenorme.model.ModelGererFamille;
import fr.insee.arc.web.gui.famillenorme.model.ViewVariableMetier;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorFamilleNorme extends ArcWebGenericService<ModelGererFamille> {

	protected static final String MODEL_VARIABLE_NAME = "nom_variable_metier";

	protected static final String NOM_TABLE_METIER = "nom_table_metier";

	protected static final String ID_FAMILLE = "id_famille";

	protected static final String ID_APPLICATION = "id_application";

	protected static final String RESULT_SUCCESS = "jsp/gererFamilleNorme.jsp";

	protected static final String IHM_MOD_VARIABLE_METIER = "ihm_mod_variable_metier";

	private static final Logger LOGGER = LogManager.getLogger(InteractorFamilleNorme.class);
	
	protected static final int NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER = 5;

	@Autowired
	protected ModelGererFamille views;

	@Override
	public String getActionName() {
		return "familyManagement";
	}
	
	private GererFamilleNormeDao dao;

	@Override
	public void putAllVObjects(ModelGererFamille arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);
		
		dao = new GererFamilleNormeDao(vObjectService, dataObjectService);

		views.setViewClient(vObjectService.preInitialize(arcModel.getViewClient()));
		views.setViewFamilleNorme(vObjectService.preInitialize(arcModel.getViewFamilleNorme()));
		views.setViewTableMetier(vObjectService.preInitialize(arcModel.getViewTableMetier()));
		views.setViewHostAllowed(vObjectService.preInitialize(arcModel.getViewHostAllowed()));
		views.setViewVariableMetier(vObjectService.preInitialize(arcModel.getViewVariableMetier()));

		putVObject(views.getViewFamilleNorme(), t -> initializeFamilleNorme(t));
		putVObject(views.getViewClient(), t -> initializeClient(t, views.getViewFamilleNorme()));
		putVObject(views.getViewTableMetier(), t -> initializeTableMetier(t, views.getViewFamilleNorme()));
		putVObject(views.getViewHostAllowed(), t -> initializeHostAllowed(t, views.getViewClient()));
		putVObject(views.getViewVariableMetier(), t -> initializeVariableMetier(t, views.getViewFamilleNorme()));

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	/**
	 * Initializes {@code ModelGererFamille#viewFamilleNorme}. Calls dao to create the view.
	 * 
	 * @param viewFamilleNorme
	 */
	private void initializeFamilleNorme(VObject viewFamilleNorme) {
		LoggerHelper.debug(LOGGER, "/* initializeFamilleNorme */");
		dao.initializeViewFamilleNorme(viewFamilleNorme);
	}

	/**
	 * Initializes {@code ModelGererFamille#viewClient}. Only gets the clients linked
	 * to the selected norm family.
	 * 
	 * @param viewClient
	 * @param viewFamilleNorme
	 */
	private void initializeClient(VObject viewClient, VObject viewFamilleNorme) {
		LoggerHelper.debug(LOGGER, "/* initializeClient */");
		try {
			// get the norm family selected records
			Map<String, ArrayList<String>> selectionFamilleNorme = viewFamilleNorme.mapContentSelected();
			// if norm family selected, trigger call to dao to construct client view
			if (!selectionFamilleNorme.isEmpty()) {
				dao.setSelectedRecords(selectionFamilleNorme);
				dao.initializeViewClient(viewClient);
			} else {
				vObjectService.destroy(viewClient);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error(LOGGER, "Error in InteractorFamilleNorme.initializeClient");
		}
	}

	/**
	 * Initializes {@code ModelGererFamille#viewHostAllowed}. Only gets the allowed hosts
	 * linked to the selected client.
	 * 
	 * @param viewHostAllowed
	 * @param viewClient
	 */
	private void initializeHostAllowed(VObject viewHostAllowed, VObject viewClient) {
		LoggerHelper.debug(LOGGER, "/* initializeHostAllowed */");
		try {
			// get the client selected records
			Map<String, ArrayList<String>> selectionClient = viewClient.mapContentSelected();
			// if client selected, trigger call to dao to construct host allowed view
			if (!selectionClient.isEmpty()) {
				dao.setSelectedRecords(selectionClient);
				dao.initializeViewHostAllowed(viewHostAllowed);
			} else {
				vObjectService.destroy(viewHostAllowed);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error(LOGGER, "Error in InteractorFamilleNorme.initializeHostAllowed");
		}
	}

	/**
	 * Initializes {@code ModelGererFamille#viewTableMetier}. Only gets the business tables
	 * linked to the selected norm family.
	 * 
	 * @param viewTableMetier
	 * @param viewFamilleNorme
	 */
	private void initializeTableMetier(VObject viewTableMetier, VObject viewFamilleNorme) {
		LoggerHelper.debug(LOGGER, "/* initializeTableMetier */");
		try {
			// get the norm family selected records
			Map<String, ArrayList<String>> selectionFamilleNorme = viewFamilleNorme.mapContentSelected();
			// if norm family selected, trigger call to dao to construct business table view
			if (!selectionFamilleNorme.isEmpty()) {
				dao.setSelectedRecords(selectionFamilleNorme);
				dao.initializeViewTableMetier(viewTableMetier);
			} else {
				vObjectService.destroy(viewTableMetier);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error(LOGGER, "Error in InteractorFamilleNorme.initializeTableMetier");
		}
	}
	
	/**
	 * Initializes {@code ModelGererFamille#viewVariableMetier}. Only gets the business variables
	 * linked to the selected norm family.
	 * 
	 * @param viewVariableMetier
	 * @param viewFamilleNorme
	 */
	private void initializeVariableMetier(VObject viewVariableMetier, VObject viewFamilleNorme) {
		// get the norm family selected records
		Map<String, ArrayList<String>> selectionFamilleNorme = viewFamilleNorme.mapContentSelected();
		// if norm family selected, trigger call to dao to render column
		if (!selectionFamilleNorme.isEmpty()) {
			dao.setSelectedRecords(selectionFamilleNorme);
			List<String> listeTableFamille = dao.getListeTableMetierFamille();
			// render column
			HashMap<String, ColumnRendering> rendering = ViewVariableMetier
					.getInitialRenderingViewVariableMetier(new HashMap<String, ColumnRendering>());
			rendering.putAll(ViewVariableMetier.getInitialRendering(listeTableFamille));
			// initialize column rendering
			vObjectService.initialiserColumnRendering(viewVariableMetier, rendering);
			try {
				LoggerHelper.debug(LOGGER, "/* initializeVariableMetier */");
				dao.initializeViewVariableMetier(viewVariableMetier, listeTableFamille);
			} catch (Exception ex) {
				StaticLoggerDispatcher.error(LOGGER, "Error in InteractorFamilleNorme.initializeVariableMetier");
			}
		} else {
			vObjectService.destroy(viewVariableMetier);
		}
	}

	protected static String synchronizeRegleWithVariableMetier(String idFamille) {
		/**
		 * Sélection des règles à détruire
		 */
		StringBuilder requeteListeSupprRegleMapping = new StringBuilder("DELETE FROM arc.ihm_mapping_regle regle\n");
		requeteListeSupprRegleMapping.append("  WHERE NOT EXISTS (");
		requeteListeSupprRegleMapping
				.append("    SELECT 1 FROM arc." + IHM_MOD_VARIABLE_METIER + " var INNER JOIN arc.ihm_famille fam\n");
		requeteListeSupprRegleMapping.append("    ON var.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.variable_sortie=var.nom_variable_metier\n");
		requeteListeSupprRegleMapping.append("    INNER JOIN arc.ihm_norme norme\n");
		requeteListeSupprRegleMapping.append("    ON norme.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.id_norme=norme.id_norme\n");
		requeteListeSupprRegleMapping.append("    WHERE fam.id_famille = '" + idFamille + "'");
		requeteListeSupprRegleMapping.append("  )");
		requeteListeSupprRegleMapping
				.append("    AND EXISTS (SELECT 1 FROM arc.ihm_norme norme INNER JOIN arc.ihm_famille fam");
		requeteListeSupprRegleMapping.append("      ON norme.id_famille=fam.id_famille");
		requeteListeSupprRegleMapping.append("      AND regle.id_norme=norme.id_norme");
		requeteListeSupprRegleMapping.append("      WHERE fam.id_famille = '" + idFamille + "')");
		/**
		 * Sélection des règles à créer
		 */
		StringBuilder requeteListeAddRegleMapping = new StringBuilder("INSERT INTO arc.ihm_mapping_regle (");
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
				.append("\n  SELECT (SELECT max(id_regle) FROM arc.ihm_mapping_regle) + row_number() over ()");
		requeteListeAddRegleMapping.append(", norme.id_norme");
		requeteListeAddRegleMapping.append(", calendrier.validite_inf");
		requeteListeAddRegleMapping.append(", calendrier.validite_sup");
		requeteListeAddRegleMapping.append(", jdr.version");
		requeteListeAddRegleMapping.append(", norme.periodicite");
		requeteListeAddRegleMapping.append(", var.nom_variable_metier");
		requeteListeAddRegleMapping.append(", '" + FormatSQL.NULL + "'");
		requeteListeAddRegleMapping.append(", " + FormatSQL.NULL + "::text ");
		requeteListeAddRegleMapping.append("\n  FROM (SELECT DISTINCT id_famille, nom_variable_metier FROM arc."
				+ IHM_MOD_VARIABLE_METIER + ") var INNER JOIN arc.ihm_famille fam");
		requeteListeAddRegleMapping.append("\n    ON var.id_famille=fam.id_famille");
		requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_norme norme");
		requeteListeAddRegleMapping.append("\n    ON fam.id_famille=norme.id_famille");
		requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_calendrier calendrier");
		requeteListeAddRegleMapping
				.append("\n    ON calendrier.id_norme=norme.id_norme AND calendrier.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_jeuderegle jdr");
		requeteListeAddRegleMapping
				.append("\n    ON calendrier.id_norme=jdr.id_norme AND calendrier.periodicite=jdr.periodicite");
		requeteListeAddRegleMapping.append(
				"\n      AND calendrier.validite_inf=jdr.validite_inf AND calendrier.validite_sup=jdr.validite_sup");
		requeteListeAddRegleMapping.append("\n  WHERE fam.id_famille = '" + idFamille + "'");
		requeteListeAddRegleMapping.append("\n    AND lower(jdr.etat) <> 'inactif'");
		requeteListeAddRegleMapping.append("\n    AND lower(calendrier.etat) = '1'");
		requeteListeAddRegleMapping.append("\n    AND NOT EXISTS (");
		requeteListeAddRegleMapping.append("\n      SELECT 1 FROM arc.ihm_mapping_regle regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.variable_sortie=var.nom_variable_metier");
		requeteListeAddRegleMapping.append("\n        AND regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    ) AND EXISTS (");
		requeteListeAddRegleMapping.append("\n      SELECT 1 FROM arc.ihm_mapping_regle regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    )");
		StringBuilder requete = new StringBuilder();
		requete.append(requeteListeAddRegleMapping.toString() + ";\n");
		requete.append(requeteListeSupprRegleMapping.toString() + ";");
		return requete.toString();
	}


	static final boolean isNomTableMetierValide(String nomTable, String phase, String famille) {
		return nomTable.matches("(?i)^" + phase.toLowerCase() + "_" + famille + "_[a-z]([a-z]|[0-9]|_)+_ok$");
	}
}
