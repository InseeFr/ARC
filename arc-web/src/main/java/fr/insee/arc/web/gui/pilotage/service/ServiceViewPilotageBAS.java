package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.ApiInitialisationService;
import fr.insee.arc.core.service.api.ApiService;
import fr.insee.arc.core.service.api.query.ServiceResetEnvironment;
import fr.insee.arc.core.service.engine.initialisation.BddPatcher;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;

@Service
public class ServiceViewPilotageBAS extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewPilotageBAS.class);

	private static final String WRITING_REPO = "entrepotEcriture";

	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * 
	 * @return
	 */
	public String enterPilotageBAS(Model model) {
		new BddPatcher().bddScript(null, getBacASable());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectPilotageBAS(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateEnvDescription(Model model) {
		ArcPreparedStatementBuilder envQuery = new ArcPreparedStatementBuilder();
		envQuery.append("update arc.ext_etat_jeuderegle set env_description = ");
		envQuery.append(envQuery.quoteText(views.getViewPilotageBAS().getCustomValue(ENV_DESCRIPTION)));
		envQuery.append("where replace(id,'.','_') = ");
		envQuery.append(envQuery.quoteText(getBacASable()));
		try {
			UtilitaireDao.get(0).executeRequest(null, envQuery);
		} catch (ArcException e) {
			loggerDispatcher.error("Error in updateEnvDescription", e, LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortPilotageBAS(Model model) {
		this.vObjectService.sort(views.getViewPilotageBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

	// Actions du bac à sable
	public String filesUploadBAS(Model model) {
		LoggerHelper.debug(LOGGER, "* /* filesUploadBAS : */ *");

		String writingRepo = this.views.getViewEntrepotBAS().getCustomValue(WRITING_REPO);
		if (writingRepo != null && !writingRepo.equals("") && views.getViewPilotageBAS().getFileUpload() != null) {

			String repertoireUpload = Paths
					.get(this.repertoire + getBacASable().toUpperCase(), TraitementPhase.RECEPTION + "_" + writingRepo)
					.toString();
			LoggerHelper.trace(LOGGER, "repertoireUpload :", repertoireUpload);
			
			try {
				this.vObjectService.upload(views.getViewPilotageBAS(), repertoireUpload);
			} catch (ArcException e) {
				this.views.getViewPilotageBAS().setMessage("Upload Failed");
			}
		} else {
			String msg = "";
			if (views.getViewPilotageBAS().getFileUpload() == null) {
				msg = "Erreur : aucun fichier selectionné\n";
				this.views.getViewPilotageBAS().setMessage("Erreur : aucun fichier selectionné.");
			}

			if (writingRepo == null || writingRepo.equals("")) {
				msg += "Erreur : aucun entrepot selectionné\n";
			}

			this.views.getViewPilotageBAS().setMessage(msg);
		}
		this.views.getViewEntrepotBAS().setCustomValue(WRITING_REPO, null);
		if (!isEnvProd()) {
			// Lancement de l'initialisation dans la foulée
			ApiServiceFactory
					.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, getBacASable(),
							this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null)
					.invokeApi();
			ApiServiceFactory
					.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, getBacASable(),
							this.repertoire, TraitementPhase.RECEPTION.getNbLigneATraiter(), null)
					.invokeApi();
		}

		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Bouton retour arriere
	 * 
	 * @param model
	 * @param phaseAExecuter
	 * @return
	 */
	public String undoBatch(Model model, TraitementPhase phaseAExecuter) {
		loggerDispatcher.debug("undoBatch", LOGGER);
		loggerDispatcher.debug(String.format("undo service %s", phaseAExecuter), LOGGER);

		ServiceResetEnvironment.backToTargetPhase(phaseAExecuter, getBacASable(), this.repertoire, undoFilesSelection());

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Méthode renvoyant la requete pour un retour arriere selectif
	 * 
	 * @return
	 */
	private ArcPreparedStatementBuilder undoFilesSelection() {
		ArcPreparedStatementBuilder selectedSrc = new ArcPreparedStatementBuilder();

		HashMap<String, ArrayList<String>> m = new HashMap<>(views.getViewFichierBAS().mapContentSelected());

		if (!m.isEmpty() && m.get("id_source") != null) {
			for (int i = 0; i < m.get("id_source").size(); i++) {
				if (selectedSrc.length() > 0) {
					selectedSrc.append("\n UNION ALL SELECT ");
				} else {
					selectedSrc.append("SELECT ");
				}
				selectedSrc.append(" " + selectedSrc.quoteText(m.get("id_source").get(i)) + "::text as id_source ");
			}
		}
		return selectedSrc;
	}

	/**
	 * Service correspondant au bouton réiniitaliser bac à sable
	 * 
	 * @param model
	 * @return
	 */
	public String resetBAS(Model model) {

		ServiceResetEnvironment.resetBAS(getBacASable(), this.repertoire);

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Bouton executer batch
	 * 
	 * @param model
	 * @param phaseAExecuter
	 * @return
	 */
	public String executerBatch(Model model, TraitementPhase phaseAExecuter) {
		loggerDispatcher.debug("executerBatch", LOGGER);
		loggerDispatcher.debug(String.format("Service %s", phaseAExecuter), LOGGER);
		
		if (!phaseAExecuter.equals(TraitementPhase.INITIALISATION)) {
			ApiInitialisationService.synchroniserSchemaExecution(null, ApiService.IHM_SCHEMA, getBacASable());
		}

		// copy to executor
		ApiInitialisationService.copyMetadataToExecutors(null, getBacASable());
		
		// Maximum number of files processed in each phase iteration
		int maxFilesPerPhase = new BDParameters(ArcDatabase.COORDINATOR).getInt(null, "LanceurIHM.maxFilesPerPhase", 10000000);
		
		ApiServiceFactory.getService(phaseAExecuter.toString(), ApiService.IHM_SCHEMA, getBacASable(), this.repertoire,
				maxFilesPerPhase, null
		).invokeApi();
		return generateDisplay(model, RESULT_SUCCESS);
	}

}