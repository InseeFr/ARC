package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.ResetEnvironmentService;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.core.util.BDParameters;
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
		try {
			dao.execQueryUpdateEnvDescription(views.getViewPilotageBAS(), ENV_DESCRIPTION, getBacASable());
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
				this.views.getViewPilotageBAS().setMessage("managementSandbox.upload.fail");
			}
		} else {
			String msg = "";
			if (views.getViewPilotageBAS().getFileUpload() == null) {
				msg = "managementSandbox.upload.noSelection\n";
				this.views.getViewPilotageBAS().setMessage("managementSandbox.upload.noSelection");
			}

			if (writingRepo == null || writingRepo.equals("")) {
				msg += "managementSandbox.upload.noFilestoreSelection\n";
			}

			this.views.getViewPilotageBAS().setMessage(msg);
		}
		this.views.getViewEntrepotBAS().setCustomValue(WRITING_REPO, null);
		if (!isEnvProd()) {
			// Lancement de l'initialisation dans la foulée
			ApiServiceFactory
					.getService(TraitementPhase.INITIALISATION.toString(), getBacASable(),
							this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null)
					.invokeApi();
			ApiServiceFactory
					.getService(TraitementPhase.RECEPTION.toString(), getBacASable(),
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

		try {
			ResetEnvironmentService.backToTargetPhase(phaseAExecuter, getBacASable(), this.repertoire, undoFilesSelection());
		} catch (ArcException e) {
			this.views.getViewPilotageBAS().setMessage("managementSandbox.undo.fail");
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Méthode renvoyant la requete pour un retour arriere selectif
	 * 
	 * @return
	 */
	private List<String> undoFilesSelection() {
		HashMap<String, ArrayList<String>> m = new HashMap<>(views.getViewFichierBAS().mapContentSelected());
		if (!m.isEmpty() && m.get(ColumnEnum.ID_SOURCE.getColumnName()) != null) {
			return m.get(ColumnEnum.ID_SOURCE.getColumnName());
		}
		return new ArrayList<>();
	}

	/**
	 * Service correspondant au bouton réiniitaliser bac à sable
	 * 
	 * @param model
	 * @return
	 */
	public String resetBAS(Model model) {

		ResetEnvironmentService.resetBAS(getBacASable(), this.repertoire);

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

		// by default, running an ARC step by GUI will synchronize the sandbox rules first
		// no need to do that if selected phase is INITIALISATION as INITIALISATION will synchronize the sandbox
		if (!phaseAExecuter.equals(TraitementPhase.INITIALISATION)) {
			try{
				new SynchronizeRulesAndMetadataOperation(new Sandbox(null, getBacASable())).synchroniserSchemaExecutionAllNods();
			}
			catch (ArcException e)
			{
				this.views.getViewPilotageBAS().setMessage(e.getMessage());
				return generateDisplay(model, RESULT_SUCCESS);
			}
		}
		
		// Maximum number of files processed in each phase iteration
		int maxFilesPerPhase = new BDParameters(ArcDatabase.COORDINATOR).getInt(null, "LanceurIHM.maxFilesPerPhase", 10000000);
		
		ApiServiceFactory.getService(phaseAExecuter.toString(), getBacASable(), this.repertoire,
				maxFilesPerPhase, null
		).invokeApi();
		return generateDisplay(model, RESULT_SUCCESS);
	}

}