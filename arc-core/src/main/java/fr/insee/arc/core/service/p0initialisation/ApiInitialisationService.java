package fr.insee.arc.core.service.p0initialisation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.p0initialisation.filesystem.RestoreFileSystem;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeUserRulesAndMetadata;
import fr.insee.arc.core.service.p0initialisation.pilotage.CleanPilotage;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.core.service.p0initialisation.useroperation.ReplayOrDeleteFiles;
import fr.insee.arc.core.service.p0initialisation.useroperation.ResetEnvironmentOperation;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

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

	public ApiInitialisationService(String aCurrentPhase, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
			String paramBatch) {
		super(aCurrentPhase, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	}

	@Override
	public void executer() throws ArcException {

		// Supprime les lignes devenues inutiles récupérées par le webservice de la
		// table pilotage_fichier
		// Déplace les archives dans OLD
		new CleanPilotage(this.coordinatorSandbox).removeDeprecatedFiles();

		// Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
		// l'environnement d'excécution courant
		// mettre à jour les tables métier avec les paramêtres de la famille de norme
		new SynchronizeUserRulesAndMetadata(this.coordinatorSandbox).synchroniserSchemaExecutionAllNods();

		// marque les fichiers ou les archives à rejouer
		// efface des fichiers de la table de pilotage marqués par l'utilisateur comme étant à effacer
		new ReplayOrDeleteFiles(this.coordinatorSandbox).processMarkedFiles();

		// Met en cohérence les table de données avec la table de pilotage de
		// l'environnement
		// La table de pilotage fait foi
		new SynchronizeDataByPilotage(this.coordinatorSandbox).synchronizeDataByPilotage();

		// remettre les archives ou elle doivent etre en cas de restauration de la base
		new RestoreFileSystem(this.coordinatorSandbox).execute();

	}

	public void retourPhasePrecedente(TraitementPhase phase, ArcPreparedStatementBuilder querySelection,
			List<TraitementEtat> listEtat) throws ArcException {
		new ResetEnvironmentOperation(this.coordinatorSandbox).retourPhasePrecedente(phase, querySelection, listEtat);
	}

	

	public void resetEnvironnement() {
		try {
			new SynchronizeDataByPilotage(this.coordinatorSandbox).synchronizeDataByPilotage();
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
