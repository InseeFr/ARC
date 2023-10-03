package fr.insee.arc.core.service.p0initialisation;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p0initialisation.filesystem.RestoreFileSystem;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.core.service.p0initialisation.pilotage.CleanPilotageOperation;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotageOperation;
import fr.insee.arc.core.service.p0initialisation.useroperation.ReplayOrDeleteFilesOperation;
import fr.insee.arc.utils.exception.ArcException;

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

	public ApiInitialisationService(String aCurrentPhase, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
			String paramBatch) {
		super(aCurrentPhase, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	}

	@Override
	public void executer() throws ArcException {

		// Supprime les lignes devenues inutiles récupérées par le webservice de la
		// table pilotage_fichier
		// Déplace les archives dans OLD
		new CleanPilotageOperation(this.coordinatorSandbox).removeDeprecatedFiles();

		// Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
		// l'environnement d'excécution courant
		// mettre à jour les tables métier avec les paramêtres de la famille de norme
		new SynchronizeRulesAndMetadataOperation(this.coordinatorSandbox).synchroniserSchemaExecutionAllNods();

		// marque les fichiers ou les archives à rejouer
		// efface des fichiers de la table de pilotage marqués par l'utilisateur comme
		// étant à effacer
		new ReplayOrDeleteFilesOperation(this.coordinatorSandbox).processMarkedFiles();

		// Met en cohérence les tables de données avec la table de pilotage de
		// l'environnement
		// La table de pilotage fait foi
		new SynchronizeDataByPilotageOperation(this.coordinatorSandbox).synchronizeDataByPilotage();

		// remettre les archives ou elle doivent etre en cas de restauration de la base
		new RestoreFileSystem(this.coordinatorSandbox).execute();

	}

}
