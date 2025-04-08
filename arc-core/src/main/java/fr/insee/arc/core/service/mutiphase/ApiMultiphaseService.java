package fr.insee.arc.core.service.mutiphase;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.thread.MultiThreading2;
import fr.insee.arc.core.service.p1reception.provider.DirectoriesReception;
import fr.insee.arc.core.service.p2chargement.ApiChargementService;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class ApiMultiphaseService extends ApiService {
	private static final Logger LOGGER = LogManager.getLogger(ApiChargementService.class);

	public ApiMultiphaseService() {
		super();
	}

	public ApiMultiphaseService(String aEnvExecution, Integer aNbEnr,
			String paramBatch, TraitementPhase...aCurrentPhase) {
		super(aEnvExecution, aNbEnr, paramBatch, aCurrentPhase[0]);
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

		int maxParallelWorkers = bdParameters.getInt(this.connexion.getCoordinatorConnection(),
				"ApiChargementService.MAX_PARALLEL_WORKERS", 4);

		// Récupérer la liste des fichiers selectionnés
		StaticLoggerDispatcher.info(LOGGER, "Récupérer la liste des fichiers selectionnés");
		
		this.tabIdSource = pilotageListIdsource(this.tablePilTemp);

		MultiThreading2 mt = new MultiThreading2(this);
		
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
