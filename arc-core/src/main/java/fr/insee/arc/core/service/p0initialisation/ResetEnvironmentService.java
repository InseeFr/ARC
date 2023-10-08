package fr.insee.arc.core.service.p0initialisation;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotageOperation;
import fr.insee.arc.core.service.p0initialisation.useroperation.ResetEnvironmentOperation;
import fr.insee.arc.utils.exception.ArcException;

public class ResetEnvironmentService {

	private ResetEnvironmentService() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER = LogManager.getLogger(ResetEnvironmentService.class);

	/**
	 * Retour arriere vers une phase
	 * 
	 * @param phaseAExecuter
	 * @param env
	 * @param rootDirectory
	 * @param undoFilesSelection
	 * @throws ArcException
	 */
	public static void backToTargetPhase(TraitementPhase phaseAExecuter, String env, String rootDirectory,
			List<String> undoFilesSelection) throws ArcException {
		if (phaseAExecuter.getOrdre() == TraitementPhase.INITIALISATION.getOrdre()) {
			resetBAS(env, rootDirectory);
		} else {
			ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION, env,
					rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null);
			try {
				new ResetEnvironmentOperation(serv.getCoordinatorSandbox()).retourPhasePrecedente(phaseAExecuter, undoFilesSelection);
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

		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION, env,
				rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null);
		try {
			// delete files and pilotage tables
			new ResetEnvironmentOperation(service.getCoordinatorSandbox()).clearPilotageAndDirectories(rootDirectory);
			
			// synchronize
			new SynchronizeDataByPilotageOperation(service.getCoordinatorSandbox()).synchronizeDataByPilotage();
			
		} catch (ArcException e) {
			e.logFullException();
		} finally {
			service.finaliser();
		}
	}

}
