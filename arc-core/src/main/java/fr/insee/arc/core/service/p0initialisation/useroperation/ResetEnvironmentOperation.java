package fr.insee.arc.core.service.p0initialisation.useroperation;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DataStorage;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotageOperation;
import fr.insee.arc.core.service.p0initialisation.useroperation.dao.ResetEnvironementDao;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;

public class ResetEnvironmentOperation {

	private static final Logger LOGGER = LogManager.getLogger(ResetEnvironmentOperation.class);

	public ResetEnvironmentOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
		this.resetEnvironmentDao = new ResetEnvironementDao(sandbox);
	}

	private ResetEnvironementDao resetEnvironmentDao;

	private Sandbox sandbox;

	/**
	 * Méthode pour remettre le système d'information dans la phase précédente
	 * Nettoyage des tables _ok et _ko ainsi que mise à jour de la table de pilotage
	 * de fichier
	 *
	 * @param phase
	 * @param querySelection
	 * @param listEtat
	 * @throws ArcException
	 */
	public void retourPhasePrecedente(TraitementPhase phase, List<String> querySelection) throws ArcException {
		LOGGER.info("Retour arrière pour la phase : {}", phase);

		Connection connection = sandbox.getConnection();
		String envExecution = sandbox.getSchema();

		// MAJ de la table de pilotage
		Integer nbLignes = 0;

		// reset etape=3 file to etape=0
		resetEnvironmentDao.executeQueryResetAllPreviousPhaseMark();

		// Delete the selected file entries from the pilotage table from all the phases
		// after the undo phase
		for (TraitementPhase phaseNext : phase.nextPhases()) {
			nbLignes += resetEnvironmentDao.executeDeletePhaseEntriesInPilotageAndCount(phaseNext, querySelection);
		}

		// Mark the selected file entries to be reload then rebuild the file system for
		// the reception phase
		if (phase.equals(TraitementPhase.RECEPTION)) {

			resetEnvironmentDao.executeReplayPhaseEntriesInPilotage(phase, querySelection);

			new ReplayOrDeleteFilesOperation(this.sandbox).replayMarkedFiles();

			nbLignes++;
		}

		// Delete the selected file entries from the pilotage table from the undo phase
		nbLignes += resetEnvironmentDao.executeDeletePhaseEntriesInPilotageAndCount(phase, querySelection);

		// Run a database synchronization with the pilotage table
		new SynchronizeDataByPilotageOperation(this.sandbox).synchronizeDataByPilotage();

		if (nbLignes > 0) {
			DatabaseMaintenance.maintenanceDatabaseClassic(connection, envExecution);
		}

	}

	/**
	 * Delete file and pilotage table to reset a sandbox
	 * 
	 * @param repertoire
	 * @throws ArcException
	 */
	public void clearPilotageAndDirectories(String repertoire) throws ArcException {

		Connection connection = sandbox.getConnection();
		String envExecution = sandbox.getSchema();

		resetEnvironmentDao.executeQueryResetPilotage();

		List<String> entrepotList = DataStorage.execQuerySelectEntrepots(connection);

		for (String s : entrepotList) {
			FileUtilsArc.deleteAndRecreateDirectory(
					Paths.get(DirectoryPath.directoryReceptionEntrepot(repertoire, envExecution, s)).toFile());
			FileUtilsArc.deleteAndRecreateDirectory(
					Paths.get(DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, s)).toFile());
		}

		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(DirectoryPath.directoryReceptionEtatEnCours(repertoire, envExecution)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(DirectoryPath.directoryReceptionEtatOK(repertoire, envExecution)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(DirectoryPath.directoryReceptionEtatKO(repertoire, envExecution)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(FileSystemManagement.directoryPhaseRoot(repertoire, envExecution, TraitementPhase.EXPORT)).toFile());
	}

}
