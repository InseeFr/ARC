package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;

@Service
public class ServicePilotageBAS extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServicePilotageBAS.class);

	private static final String WRITING_REPO = "entrepotEcriture";

	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * 
	 * @return
	 */
	public String enterPilotageBAS(Model model) {
		ApiInitialisationService.bddScript(null, new String[] { getBacASable() });
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
			UtilitaireDao.get(POOLNAME).executeRequest(null, envQuery);
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
			this.vObjectService.upload(views.getViewPilotageBAS(), repertoireUpload);
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
							this.repertoire, String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter()))
					.invokeApi();
			ApiServiceFactory
					.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, getBacASable(),
							this.repertoire, String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter()))
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

		ApiService.backToTargetPhase(phaseAExecuter, getBacASable(), this.repertoire, undoFilesSelection());

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

		ApiService.resetBAS(getBacASable(), this.repertoire);

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

		ApiServiceFactory.getService(phaseAExecuter.toString(), ApiService.IHM_SCHEMA, getBacASable(), this.repertoire,
				"10000000"
		// ,"1" // to set batch mode or not
		).invokeApi();
		return generateDisplay(model, RESULT_SUCCESS);
	}

}