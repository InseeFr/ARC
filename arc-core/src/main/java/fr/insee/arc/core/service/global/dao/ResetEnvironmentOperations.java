package fr.insee.arc.core.service.global.dao;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

public class ResetEnvironmentOperations {

	private ResetEnvironmentOperations() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER = LogManager.getLogger(ResetEnvironmentOperations.class);

	/**
	 * Retour arriere vers une phase
	 * 
	 * @param phaseAExecuter
	 * @param env
	 * @param rootDirectory
	 * @param undoFilesSelection
	 */
	public static void backToTargetPhase(TraitementPhase phaseAExecuter, String env, String rootDirectory,
			ArcPreparedStatementBuilder undoFilesSelection) {
		if (phaseAExecuter.getOrdre() == TraitementPhase.INITIALISATION.getOrdre()) {
			resetBAS(env, rootDirectory);
		} else {
			ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
					env, rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(),
					null);
			try {
				serv.retourPhasePrecedente(phaseAExecuter, undoFilesSelection,
						new ArrayList<>(Arrays.asList(TraitementEtat.OK, TraitementEtat.KO)));
			} finally {
				serv.finaliser();
			}
		}
	}

	/**
	 * reset data in the sandbox
	 * 
	 * @param model
	 * @param env
	 * @param rootDirectory
	 */
	public static void resetBAS(String env, String rootDirectory) {
		try {
			ApiInitialisationService.clearPilotageAndDirectories(rootDirectory, env);
		} catch (Exception e) {
			StaticLoggerDispatcher.info(LOGGER, e);
		}
		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				env, rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null);
		try {
			service.resetEnvironnement();
		} finally {
			service.finaliser();
		}
	}

}
