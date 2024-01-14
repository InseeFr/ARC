package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.PhaseOperations;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;

@Service
public class ServiceViewFichierBAS extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewFichierBAS.class);

	public String selectFichierBAS(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortFichierBAS(Model model) {

		this.vObjectService.sort(views.getViewFichierBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

	public void downloadFichierBAS(HttpServletResponse response) {
		loggerDispatcher.trace("*** Téléchargement des fichiers ***", LOGGER);
		dao.downloadFichierBAS(this.views.getViewFichierBAS(), response, this.repertoire, getBacASable());
		loggerDispatcher.trace("*** Fin du téléchargement des fichiers XML ***", LOGGER);
	}

	/**
	 * Marquage de fichier pour le rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	public String toRestoreBAS(Model model) {
		return restore(model, "'R'", "managementSandbox.batch.replay.files");
	}

	/**
	 * Marquage des archives à rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	public String toRestoreArchiveBAS(Model model) {
		return restore(model, "'RA'", "managementSandbox.batch.replay.archives");
	}

	private String restore(Model model, String code, String messageOk) {
		loggerDispatcher.trace("*** Marquage de fichier à rejouer ***", LOGGER);
		ArcPreparedStatementBuilder updateToDelete = dao.queryUpdateToDelete(views.getViewFichierBAS(), code);
		String message;
		try {
			dao.execQueryUpdateToDelete(updateToDelete);
			message = messageOk;
		} catch (ArcException e) {
			loggerDispatcher.error("Error in PilotageBASAction.restore", LOGGER);
			message = "managementSandbox.batch.replay.error";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		if (!isEnvProd()) {
			// Lancement de l'initialisation dans la foulée
			loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
			ApiServiceFactory
					.getService(TraitementPhase.INITIALISATION, getBacASable(),
							this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null)
					.invokeApi();
			ApiServiceFactory
					.getService(TraitementPhase.RECEPTION, getBacASable(),
							this.repertoire, TraitementPhase.RECEPTION.getNbLigneATraiter(), null)
					.invokeApi();
		}
		this.views.getViewPilotageBAS().setMessage(message);

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public void downloadBdBAS(HttpServletResponse response) throws ArcException {
		
		Map<String, List<String>> selectionLigne = views.getViewPilotageBAS().mapContentSelected();
		List<String> selectionColonne = views.getViewPilotageBAS().listHeadersSelected();

		TraitementPhase phase = TraitementPhase.valueOf(selectionColonne.get(0).split("_")[0].toUpperCase());
		TraitementEtat etat = TraitementEtat.valueOf(selectionColonne.get(0).split("_")[1].toUpperCase());
		
		
		String date = selectionLigne.get(ColumnEnum.DATE_ENTREE.toString()).get(0);

		// Sélection des table métiers en fonction de la phase sélectionner (5 pour
		// mapping 1 sinon)
		List<String> tableDownload = new ArrayList<>();
		try {
			
			List<String> dataTables = PhaseOperations.selectPhaseDataTablesFoundInEnv(null, getBacASable());

			if (!dataTables.isEmpty()) {
				for (String table : dataTables) {
					// selection des tables qui contiennent la phase dans leur nom
					for (int i = 0; i < etat.getArrayExpression().length; i++) {
						if (ManipString.substringAfterFirst(table.toUpperCase(), ".")
								.startsWith(phase + Delimiters.SQL_TOKEN_DELIMITER)
								&& table.toUpperCase().endsWith(Delimiters.SQL_TOKEN_DELIMITER + etat.getArrayExpression()[i])
								&& !tableDownload.contains(table)) {
							tableDownload.add(table);

						}
					}
				}
			}
		} catch (ArcException e) {
			loggerDispatcher.error(e, LOGGER);
		}
		
		dao.downloadBdBAS(this.views.getViewFichierBAS(), response, tableDownload, phase, etat, date);

	}

	public String downloadEnveloppeBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des enveloppes ***", LOGGER);
		
		initializeFichierBAS(views.getViewFichierBAS(), views.getViewPilotageBAS(), views.getViewRapportBAS());

		List<String> listRepertoire = new ArrayList<>();
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.OK);
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.KO);
		String chemin = Paths.get(this.repertoire, getBacASable().toUpperCase()).toString();
		dao.downloadEnvelopeBAS(this.views.getViewFichierBAS(), response, chemin, listRepertoire);
		loggerDispatcher.trace("*** Fin du téléchargement des enveloppes ***", LOGGER);

		return "none";
	}

	/**
	 * Marquage de fichier pour suppression lors de la prochaine initialisation
	 *
	 * @return
	 */
	public String toDeleteBAS(Model model) {

		loggerDispatcher.trace("*** Marquage de fichier à supprimer ***", LOGGER);
		ArcPreparedStatementBuilder updateToDelete = dao.queryUpdateToDelete(views.getViewFichierBAS(), "'1'");
		String message;
		try {
			dao.execQueryUpdateToDelete(updateToDelete);
			message = "managementSandbox.batch.delete.ok";
		} catch (ArcException e) {
			loggerDispatcher.error("Error in PilotageBASAction.toDeleteBAS", LOGGER);
			message = "managementSandbox.batch.delete.error";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		if (!isEnvProd()) {
			loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
			ApiServiceFactory
					.getService(TraitementPhase.INITIALISATION, getBacASable(),
							this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null)
					.invokeApi();
		}

		this.views.getViewPilotageBAS().setMessage(message);

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Suppression du marquage de fichier pour suppression lors de la prochaine
	 * initialisation
	 *
	 * @return
	 */
	public String undoActionBAS(Model model) {

		loggerDispatcher.trace("*** Suppression du marquage de fichier à supprimer ***", LOGGER);
		ArcPreparedStatementBuilder updateToDelete = dao.queryUpdateToDelete(views.getViewFichierBAS(), "null");
		try {
			dao.execQueryUpdateToDelete(updateToDelete);
		} catch (ArcException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			loggerDispatcher.error("Error in PilotageBASAction.undoActionBAS", LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	

}
