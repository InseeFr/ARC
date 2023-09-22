package fr.insee.arc.core.service.p0initialisation.useroperation;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

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
	public void retourPhasePrecedente(TraitementPhase phase, ArcPreparedStatementBuilder querySelection,
			List<TraitementEtat> listEtat) throws ArcException {
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
			requete.append("WITH TMP_DELETE AS (DELETE FROM " + connection + " WHERE phase_traitement = "
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

	
}
