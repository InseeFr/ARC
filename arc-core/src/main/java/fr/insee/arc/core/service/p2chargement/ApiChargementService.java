package fr.insee.arc.core.service.p2chargement;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.thread.MultiThreading;
import fr.insee.arc.core.service.p1reception.provider.DirectoriesReception;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

/**
 * ApiChargementService
 *
 * 1- Créer les tables de reception du chargement</br>
 * 2- Récupérer la liste des fichiers à traiter et le nom de leur entrepôt 3-
 * Pour chaque fichier, determiner son format de lecture (zip, tgz, raw) et le
 * chargeur à utlisé (voir entrepot) 4- Pour chaque fichier, Invoquer le
 * chargeur 4-1 Parsing du fichier 4-2 Insertion dans les tables I et A des
 * données lues dans le fichier 4-3 Fin du parsing. Constituer la requete de
 * mise en relation des données chargées et la stocker pour son utilisation
 * ultérieure au normage 5- Fin chargement. Insertion dans la table applicative
 * CHARGEMENT_OK. Mise à jour de la table de pilotage
 *
 * @author Manuel SOULIER
 *
 */

@Component
public class ApiChargementService extends ApiService {
	private static final Logger LOGGER = LogManager.getLogger(ApiChargementService.class);

	public ApiChargementService() {
		super();
	}

	public ApiChargementService(String aCurrentPhase, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
			String paramBatch) {
		super(aCurrentPhase, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	}
	
	protected List<NormeRules> listeNorme;
	protected String directoryIn;

	@Override
	public void executer() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** executer **");

		PropertiesHandler properties = PropertiesHandler.getInstance();

		BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);

		// input directory is reception_ok directory
		this.directoryIn = new DirectoriesReception(this.coordinatorSandbox).getDirectoryReceptionOK() + File.separator;

		// récupération des différentes normes dans la base
		this.listeNorme = NormeRules.getNormesBase(this.connexion.getCoordinatorConnection(), this.envExecution);

		this.maxParallelWorkers = bdParameters.getInt(this.connexion.getCoordinatorConnection(),
				"ApiChargementService.MAX_PARALLEL_WORKERS", 4);

		// Récupérer la liste des fichiers selectionnés
		StaticLoggerDispatcher.info(LOGGER, "Récupérer la liste des fichiers selectionnés");
		
		this.tabIdSource = pilotageListIdsource(this.tablePilTemp, this.currentPhase, TraitementEtat.ENCOURS.toString());

		MultiThreading<ApiChargementService, ThreadChargementService> mt = new MultiThreading<>(this,
				new ThreadChargementService());
		mt.execute(maxParallelWorkers, getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()), this.envExecution,
				properties.getDatabaseRestrictedUsername());

	}

	public List<NormeRules> getListeNorme() {
		return listeNorme;
	}

	public String getDirectoryIn() {
		return directoryIn;
	}
	
	
}
