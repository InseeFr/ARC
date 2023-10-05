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
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.famillenorme.dao.GererFamilleNormeDao;
import fr.insee.arc.web.gui.famillenorme.model.ModelGererFamille;
import fr.insee.arc.web.gui.famillenorme.model.ViewVariableMetier;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorFamilleNorme extends ArcWebGenericService<ModelGererFamille,GererFamilleNormeDao> {

	protected static final String RESULT_SUCCESS = "jsp/gererFamilleNorme.jsp";

	private static final Logger LOGGER = LogManager.getLogger(InteractorFamilleNorme.class);

	@Autowired
	protected ModelGererFamille views;

	@Override
	public String getActionName() {
		return "familyManagement";
	}

	@Override
	public void putAllVObjects(ModelGererFamille arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);
		
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

	static final boolean isNomTableMetierValide(String nomTable, String phase, String famille) {
		return nomTable.matches("(?i)^" + phase.toLowerCase() + "_" + famille + "_[a-z]([a-z]|[0-9]|_)+_ok$");
	}
}
