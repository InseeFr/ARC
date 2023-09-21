package fr.insee.arc.core.service.p0initialisation;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.p0initialisation.filesystem.RestoreFileSystem;
import fr.insee.arc.core.service.p0initialisation.pilotage.CleanPilotage;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.core.service.p0initialisation.userdata.SynchronizeUserRulesAndMetadata;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * ApiNormageService
 *
 * 1- Implémenter des maintenances sur la base de donnée </br>
 * 2- Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
 * l'environnement d'excécution courant</br>
 * 3- Gestion des fichiers en doublon</br>
 * 4- Assurer la cohérence entre les table de données et la table de pilotage de
 * l'environnement qui fait foi</br>
 * 5- Maintenance base de données</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiInitialisationService extends ApiService {
	public ApiInitialisationService() {
		super();
	}

	private static final Logger LOGGER = LogManager.getLogger(ApiInitialisationService.class);

	public ApiInitialisationService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution,
			String aDirectoryRoot, Integer aNbEnr, String paramBatch) {
		super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	}

	@Override
	public void executer() throws ArcException {
		
		// Supprime les lignes devenues inutiles récupérées par le webservice de la
		// table pilotage_fichier
		// Déplace les archives dans OLD
		new CleanPilotage(this.coordinatorSandbox).execute();

		// Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
		// l'environnement d'excécution courant
		// mettre à jour les tables métier avec les paramêtres de la famille de norme
		SynchronizeUserRulesAndMetadata.synchroniserSchemaExecutionAllNods(connexion.getCoordinatorConnection(), envExecution);

		// marque les fichiers ou les archives à rejouer
		reinstate(this.connexion.getCoordinatorConnection());

		// efface des fichiers de la table de pilotage
		cleanToDelete(this.connexion.getCoordinatorConnection(), this.tablePil);

		// Met en cohérence les table de données avec la table de pilotage de
		// l'environnement
		// La table de pilotage fait foi
		new SynchronizeDataByPilotage(this.coordinatorSandbox).execute();

		// remettre les archives ou elle doivent etre en cas de restauration de la base
		new RestoreFileSystem(this.connexion.getCoordinatorConnection(), envExecution).execute();

	}





	/**
	 * Méthode pour rejouer des fichiers
	 *
	 * @param connexion
	 * @param tablePil
	 * @throws ArcException
	 */
	private void reinstate(Connection connexion) throws ArcException {
		LoggerHelper.info(LOGGER, "reinstateWithRename");

		// on cherche tous les containers contenant un fichier à rejouer
		// on remet l'archive à la racine

		ArrayList<String> containerList = new GenericBean(UtilitaireDao.get(0).executeRequest(null,
				new ArcPreparedStatementBuilder(
						"select distinct container from " + tablePil + " where to_delete in ('R','RA')")))
				.mapContent().get("container");

		if (containerList != null) {
			PropertiesHandler properties = PropertiesHandler.getInstance();
			String repertoire = properties.getBatchParametersDirectory();
			String envDir = this.envExecution.replace(".", "_").toUpperCase();

			for (String s : containerList) {

				String entrepot = ManipString.substringBeforeFirst(s, "_");
				String archive = ManipString.substringAfterFirst(s, "_");

				String dirIn = ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envDir, entrepot);
				String dirOut = ApiReceptionService.directoryReceptionEntrepot(repertoire, envDir, entrepot);

				ApiReceptionService.deplacerFichier(dirIn, dirOut, archive, archive);

			}

		}

		// effacer les archives marquées en RA
		UtilitaireDao.get(0).executeImmediate(connexion,
				"DELETE FROM " + this.tablePil + " a where exists (select 1 from " + this.tablePil
						+ " b where a.container=b.container and b.to_delete='RA')");

	}



	/**
	 * Suppression dans la table de pilotage des fichiers qui ont été marqué par la
	 * MOA (via la colonne to_delete de la table de pilotage);
	 *
	 * @param connexion
	 * @param tablePil
	 * @throws ArcException
	 */
	private void cleanToDelete(Connection connexion, String tablePil) throws ArcException {
		LoggerHelper.info(LOGGER, "cleanToDelete");

		StringBuilder requete = new StringBuilder();
		requete.append("DELETE FROM " + tablePil + " a WHERE exists (select 1 from " + tablePil
				+ " b where b.to_delete='1' and a." + ColumnEnum.ID_SOURCE.getColumnName() + "=b."
				+ ColumnEnum.ID_SOURCE.getColumnName() + " and a.container=b.container); ");
		UtilitaireDao.get(0).executeBlock(connexion, requete);
	}



	/**
	 * Méthode pour remettre le système d'information dans la phase précédente
	 * Nettoyage des tables _ok et _ko ainsi que mise à jour de la table de pilotage
	 * de fichier
	 *
	 * @param phase
	 * @param querySelection
	 * @param listEtat
	 */
	public void retourPhasePrecedente(TraitementPhase phase, ArcPreparedStatementBuilder querySelection,
			ArrayList<TraitementEtat> listEtat) {
		LOGGER.info("Retour arrière pour la phase : {}", phase);
		ArcPreparedStatementBuilder requete;
		// MAJ de la table de pilotage
		Integer nbLignes = 0;

		// reset etape=3 file to etape=0
		try {
			UtilitaireDao.get(0).executeRequest(this.connexion.getCoordinatorConnection(),
					new ArcPreparedStatementBuilder(resetPreviousPhaseMark(this.tablePil, null, null)));
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e);
		}

		// Delete the selected file entries from the pilotage table from all the phases
		// after the undo phase
		for (TraitementPhase phaseNext : phase.nextPhases()) {
			requete = new ArcPreparedStatementBuilder();
			requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.tablePil + " WHERE phase_traitement = "
					+ requete.quoteText(phaseNext.toString()) + " ");
			if (querySelection.length() > 0) {
				requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
						+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
				requete.append(querySelection);
				requete.append(") q1 ) ");
			}
			requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
			nbLignes = nbLignes + UtilitaireDao.get(0).getInt(this.connexion.getCoordinatorConnection(), requete);
		}

		// Mark the selected file entries to be reload then rebuild the file system for
		// the reception phase
		if (phase.equals(TraitementPhase.RECEPTION)) {
			requete = new ArcPreparedStatementBuilder();
			requete.append("UPDATE  " + this.tablePil + " set to_delete='R' WHERE phase_traitement = "
					+ requete.quoteText(phase.toString()) + " ");
			if (querySelection.length() > 0) {
				requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
						+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
				requete.append(querySelection);
				requete.append(") q1 ) ");
			}
			try {
				UtilitaireDao.get(0).executeRequest(connexion.getCoordinatorConnection(), requete);
			} catch (ArcException e) {
				LoggerHelper.error(LOGGER, e);
			}

			try {
				reinstate(this.connexion.getCoordinatorConnection());
			} catch (Exception e) {
				LoggerHelper.error(LOGGER, e);
			}

			nbLignes++;
		}

		// Delete the selected file entries from the pilotage table from the undo phase
		requete = new ArcPreparedStatementBuilder();
		requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.tablePil + " WHERE phase_traitement = "
				+ requete.quoteText(phase.toString()) + " ");
		if (querySelection.length() > 0) {
			requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
					+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
			requete.append(querySelection);
			requete.append(") q1 ) ");
		}
		requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
		nbLignes = nbLignes + UtilitaireDao.get(0).getInt(this.connexion.getCoordinatorConnection(), requete);

		// Run a database synchronization with the pilotage table
		try {
			new SynchronizeDataByPilotage(this.coordinatorSandbox).execute();
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e);
		}

		if (nbLignes > 0) {
			DatabaseMaintenance.maintenanceDatabaseClassic(connexion.getCoordinatorConnection(), envExecution);
		}

		// Penser à tuer la connexion
	}

	public void resetEnvironnement() {
		try {
			new SynchronizeDataByPilotage(this.coordinatorSandbox).execute();
			DatabaseMaintenance.maintenanceDatabaseClassic(connexion.getCoordinatorConnection(), envExecution);
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e);
		}
	}





	public static void clearPilotageAndDirectories(String repertoire, String env) throws ArcException {
		UtilitaireDao.get(0).executeBlock(null, "truncate " + TableNaming.dbEnv(env) + "pilotage_fichier; ");
		UtilitaireDao.get(0).executeBlock(null, "truncate " + TableNaming.dbEnv(env) + "pilotage_archive; ");

		if (Boolean.TRUE.equals(UtilitaireDao.get(0).hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot")))) {
			ArrayList<String> entrepotList = new GenericBean(UtilitaireDao.get(0).executeRequest(null,
					new ArcPreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent()
					.get("id_entrepot");
			if (entrepotList != null) {
				for (String s : entrepotList) {
					FileUtilsArc.deleteAndRecreateDirectory(
							Paths.get(ApiReceptionService.directoryReceptionEntrepot(repertoire, env, s)).toFile());
					FileUtilsArc.deleteAndRecreateDirectory(Paths
							.get(ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, env, s)).toFile());
				}
			}
		}
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(ApiReceptionService.directoryReceptionEtatEnCours(repertoire, env)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(ApiReceptionService.directoryReceptionEtatOK(repertoire, env)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(ApiReceptionService.directoryReceptionEtatKO(repertoire, env)).toFile());
		FileUtilsArc.deleteAndRecreateDirectory(
				Paths.get(FileSystemManagement.directoryEnvExport(repertoire, env)).toFile());
	}



}
