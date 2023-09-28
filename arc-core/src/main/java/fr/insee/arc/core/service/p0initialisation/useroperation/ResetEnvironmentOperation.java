package fr.insee.arc.core.service.p0initialisation.useroperation;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class ResetEnvironmentOperation {


	private static final Logger LOGGER = LogManager.getLogger(ResetEnvironmentOperation.class);

	public ResetEnvironmentOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}
	
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
	public void retourPhasePrecedente(TraitementPhase phase, ArcPreparedStatementBuilder querySelection) throws ArcException {
		LOGGER.info("Retour arrière pour la phase : {}", phase);
		
		Connection connection = sandbox.getConnection();
		String envExecution = sandbox.getSchema();
		
		String tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);
		
		ArcPreparedStatementBuilder requete;
		// MAJ de la table de pilotage
		Integer nbLignes = 0;

		// reset etape=3 file to etape=0
		UtilitaireDao.get(0).executeRequest(connection,
					new ArcPreparedStatementBuilder(PilotageOperations.resetPreviousPhaseMark(tablePil, null, null)));


		// Delete the selected file entries from the pilotage table from all the phases
		// after the undo phase
		for (TraitementPhase phaseNext : phase.nextPhases()) {
			requete = new ArcPreparedStatementBuilder();
			requete.append("WITH TMP_DELETE AS (DELETE FROM " + tablePil + " WHERE phase_traitement = "
					+ requete.quoteText(phaseNext.toString()) + " ");
			if (querySelection.length() > 0) {
				requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
						+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
				requete.append(querySelection);
				requete.append(") q1 ) ");
			}
			requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
			nbLignes = nbLignes + UtilitaireDao.get(0).getInt(connection, requete);
		}

		// Mark the selected file entries to be reload then rebuild the file system for
		// the reception phase
		if (phase.equals(TraitementPhase.RECEPTION)) {
			requete = new ArcPreparedStatementBuilder();
			requete.append("UPDATE  " + tablePil + " set to_delete='R' WHERE phase_traitement = "
					+ requete.quoteText(phase.toString()) + " ");
			if (querySelection.length() > 0) {
				requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
						+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
				requete.append(querySelection);
				requete.append(") q1 ) ");
			}
			
			UtilitaireDao.get(0).executeRequest(connection, requete);
			new ReplayOrDeleteFiles(this.sandbox).replayMarkedFiles();

			nbLignes++;
		}

		// Delete the selected file entries from the pilotage table from the undo phase
		requete = new ArcPreparedStatementBuilder();
		requete.append("WITH TMP_DELETE AS (DELETE FROM " + tablePil + " WHERE phase_traitement = "
				+ requete.quoteText(phase.toString()) + " ");
		if (querySelection.length() > 0) {
			requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
					+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
			requete.append(querySelection);
			requete.append(") q1 ) ");
		}
		requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
		nbLignes = nbLignes + UtilitaireDao.get(0).getInt(connection, requete);

		// Run a database synchronization with the pilotage table
		new SynchronizeDataByPilotage(this.sandbox).synchronizeDataByPilotage();


		if (nbLignes > 0) {
			DatabaseMaintenance.maintenanceDatabaseClassic(connection, envExecution);
		}

		// Penser à tuer la connexion
	}

	
	/**
	 * Delete file and pilotage table to reset a sandbox
	 * @param repertoire
	 * @throws ArcException
	 */
	public void clearPilotageAndDirectories(String repertoire) throws ArcException {
		
		Connection connection = sandbox.getConnection();
		String envExecution = sandbox.getSchema();
		
		UtilitaireDao.get(0).executeBlock(connection, "truncate " + ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution)+ ";");
		UtilitaireDao.get(0).executeBlock(connection, "truncate " + ViewEnum.PILOTAGE_ARCHIVE.getFullName(envExecution) + ";");

		if (Boolean.TRUE.equals(UtilitaireDao.get(0).hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot")))) {
			ArrayList<String> entrepotList = new GenericBean(UtilitaireDao.get(0).executeRequest(null,
					new ArcPreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent()
					.get("id_entrepot");
			if (entrepotList != null) {
				for (String s : entrepotList) {
					FileUtilsArc.deleteAndRecreateDirectory(
							Paths.get(DirectoryPath.directoryReceptionEntrepot(repertoire, envExecution, s)).toFile());
					FileUtilsArc.deleteAndRecreateDirectory(Paths
							.get(DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, s)).toFile());
				}
			}
		}
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(DirectoryPath.directoryReceptionEtatEnCours(repertoire, envExecution)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(DirectoryPath.directoryReceptionEtatOK(repertoire, envExecution)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(DirectoryPath.directoryReceptionEtatKO(repertoire, envExecution)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(FileSystemManagement.directoryEnvExport(repertoire, envExecution)).toFile());
	}
	
}
