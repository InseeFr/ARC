package fr.insee.arc.core.service.p0initialisation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.core.service.p0initialisation.useroperation.ResetEnvironmentOperation;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

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
			ArcPreparedStatementBuilder undoFilesSelection) throws ArcException {
		if (phaseAExecuter.getOrdre() == TraitementPhase.INITIALISATION.getOrdre()) {
			resetBAS(env, rootDirectory);
		} else {
			ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), env,
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

		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), env,
				rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null);
		try {
			// delete files and pilotage tables
			new ResetEnvironmentOperation(service.getCoordinatorSandbox()).clearPilotageAndDirectories(rootDirectory);
			
			// synchronize
			new SynchronizeDataByPilotage(service.getCoordinatorSandbox()).synchronizeDataByPilotage();
			
		} catch (ArcException e) {
			e.logFullException();
		} finally {
			service.finaliser();
		}
	}

}
