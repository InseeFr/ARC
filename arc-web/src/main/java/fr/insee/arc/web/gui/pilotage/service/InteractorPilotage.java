package fr.insee.arc.web.gui.pilotage.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.gui.all.util.LineObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.pilotage.dao.PilotageDao;
import fr.insee.arc.web.gui.pilotage.model.ModelPilotage;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorPilotage extends ArcWebGenericService<ModelPilotage, PilotageDao> {

	protected static final String ENV_DESCRIPTION = "envDescription";

	protected static final String ENTRY_DATE = "date_entree";

	private static final String ACTION_NAME = "EnvManagement";

	protected static final String RESULT_SUCCESS = "jsp/gererPilotageBAS.jsp";

	private static final Logger LOGGER = LogManager.getLogger(InteractorPilotage.class);

	@Autowired
	protected MessageSource messageSource;

	/**
	 * Liste des phase pour générer les boutons d'actions executer et retour arriere
	 * sur chaque phase.
	 */
	private List<TraitementPhase> listePhase;

	@Autowired
	protected ModelPilotage views;

	public InteractorPilotage() {
		this.setListePhase(TraitementPhase.getListPhaseC());
	}

	@ModelAttribute
	public void specificModelAttributes(Model model) {
		model.addAttribute("listePhase", listePhase);
	}

	@Override
	public void putAllVObjects(ModelPilotage arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		views.setViewPilotageBAS(vObjectService.preInitialize(arcModel.getViewPilotageBAS()));
		views.setViewRapportBAS(vObjectService.preInitialize(arcModel.getViewRapportBAS()));
		views.setViewArchiveBAS(vObjectService.preInitialize(arcModel.getViewArchiveBAS()));
		views.setViewEntrepotBAS(vObjectService.preInitialize(arcModel.getViewEntrepotBAS()));
		views.setViewFichierBAS(vObjectService.preInitialize(arcModel.getViewFichierBAS()));

		// If the sandbox changed, have to destroy all table and re create later
		if (this.isRefreshMonitoring) {
			vObjectService.destroy(views.getViewPilotageBAS());
			vObjectService.destroy(views.getViewRapportBAS());
			vObjectService.destroy(views.getViewArchiveBAS());
			vObjectService.destroy(views.getViewEntrepotBAS());
			vObjectService.destroy(views.getViewFichierBAS());
			this.isRefreshMonitoring = false;
		}

		putVObject(views.getViewPilotageBAS(), t -> initializePilotageBAS(t));
		putVObject(views.getViewRapportBAS(), t -> initializeRapportBAS(t));
		putVObject(views.getViewArchiveBAS(), t -> initializeArchiveBAS(t, views.getViewEntrepotBAS()));
		putVObject(views.getViewEntrepotBAS(), t -> initializeEntrepotBAS(t));
		putVObject(views.getViewFichierBAS(),
				t -> initializeFichierBAS(t, views.getViewPilotageBAS(), views.getViewRapportBAS()));

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	// visual des Pilotages du bac à sable
	public void initializePilotageBAS(VObject viewPilotageBAS) {
		LoggerHelper.debug(LOGGER, "* initializePilotageBAS *");

		dao.initializePilotageBAS(viewPilotageBAS);

		ArrayList<String> columns = viewPilotageBAS.getHeadersDLabel();
		Map<String, ColumnRendering> columnRendering = viewPilotageBAS.getConstantVObject().columnRender;

		// for all columns, set rendering visibility to false
		for (int i = 1; i < columns.size(); i++) {
			ColumnRendering renderAttributes = new ColumnRendering(false, ManipString.translateAscii(columns.get(i)),
					null, "text", null, false);
			columnRendering.put(columns.get(i), renderAttributes);
		}

		// now display the columns only which have positive values

		for (LineObject l : viewPilotageBAS.getContent()) {
			for (int i = 1; i < columns.size(); i++) {
				if (!l.getD().get(i).equals("0")) {
					columnRendering.get(columns.get(i)).visible = true;
				}
			}
		}

		this.vObjectService.initialiserColumnRendering(viewPilotageBAS, columnRendering);
		this.vObjectService.applyColumnRendering(viewPilotageBAS, columns);

		// display comment for the sandbox
		try {
			String envDescription = dao.getSandboxDescription(getBacASable());
			viewPilotageBAS.setCustomValue(ENV_DESCRIPTION, envDescription);
		} catch (ArcException e) {
			loggerDispatcher.error("Error in initializePilotageBAS", e, LOGGER);
		}
	}

	// visual des Pilotages du bac à sable
	public void initializeRapportBAS(VObject viewRapportBAS) {
		LoggerHelper.debug(LOGGER, "* initializeRapportBAS *");
		if (viewRapportBAS.getHeaderSortDLabels() == null) {
			viewRapportBAS.setHeaderSortDLabels(new ArrayList<>(Arrays.asList(ENTRY_DATE)));
			viewRapportBAS.setHeaderSortDOrders(new ArrayList<>(Arrays.asList(false)));
		}
		dao.initializeRapportBAS(viewRapportBAS);
	}

	/**
	 * Initialisation de la vue sur la table contenant la liste des fichiers du
	 * répertoire d'archive
	 */
	public void initializeArchiveBAS(VObject viewArchiveBAS, VObject viewEntrepotBAS) {
		LoggerHelper.debug(LOGGER, "* /* initializeArchiveBAS  */ *");
		dao.initializeArchiveBAS(viewArchiveBAS, viewEntrepotBAS);
	}

	public void initializeEntrepotBAS(VObject viewEntrepotBAS) {
		LoggerHelper.debug(LOGGER, "* initializeEntrepotBAS *");
		try {
			dao.initializeEntrepotBAS(viewEntrepotBAS);
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, "error when initialize repository", e);
		}
	}

	// visual des Fichiers
	public void initializeFichierBAS(VObject viewFichierBAS, VObject viewPilotageBAS, VObject viewRapportBAS) {
		LoggerHelper.debug(LOGGER, "initializeFichierBAS");
		dao.initializeFichierBAS(viewFichierBAS, viewPilotageBAS, viewRapportBAS);
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	public List<TraitementPhase> getListePhase() {
		return listePhase;
	}

	public void setListePhase(List<TraitementPhase> listePhase) {
		this.listePhase = listePhase;
	}

}
