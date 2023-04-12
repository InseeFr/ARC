package fr.insee.arc.core.service.utility;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

public class ServiceResetEnvironment {

	private ServiceResetEnvironment() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER = LogManager.getLogger(ServiceResetEnvironment.class);

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
					ApiService.IHM_SCHEMA, env, rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(),
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
			StaticLoggerDispatcher.info(e, LOGGER);
		}
		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				ApiService.IHM_SCHEMA, env, rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null);
		try {
			service.resetEnvironnement();
		} finally {
			service.finaliser();
		}
	}

}
