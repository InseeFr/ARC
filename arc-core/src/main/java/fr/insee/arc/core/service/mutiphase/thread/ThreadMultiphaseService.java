package fr.insee.arc.core.service.mutiphase.thread;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.thread.IThread;
import fr.insee.arc.core.service.mutiphase.ApiMultiphaseService;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.service.p3normage.thread.ThreadNormageService;
import fr.insee.arc.core.service.p4controle.thread.ThreadControleService;
import fr.insee.arc.core.service.p5mapping.thread.ThreadMappingService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

public class ThreadMultiphaseService extends ApiMultiphaseService implements Runnable, IThread<ApiMultiphaseService> {

	private static final Logger LOGGER = LogManager.getLogger(ThreadMultiphaseService.class);

	private Thread t;
	private int currentIndice;
	private HashSet<TraitementPhase> currentPhaseSet;

	@Override
	public void configThread(ScalableConnection connexion, int currentIndice, ApiMultiphaseService aApi) {

		this.connexion = connexion;
		this.currentIndice = currentIndice;
		this.currentPhaseSet = new HashSet<>(Arrays.asList(aApi.getCurrentPhase()));
		this.envExecution = aApi.getEnvExecution();
		this.tabIdSource = aApi.getTabIdSource();
		this.tablePilTemp = aApi.getTablePilTemp();
		this.tablePil = aApi.getTablePil();
		this.paramBatch = aApi.getParamBatch();
		this.directoryIn = aApi.getDirectoryIn();
		this.listeNorme = aApi.getListeNorme();
	}

	@Override
	public Thread getT() {
		return t;
	}

	@Override
	public void start() {
		StaticLoggerDispatcher.debug(LOGGER, "Starting ThreadChargementService");
		this.t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {

		// From the source table, get the required starting phase
		int startingPhase = TraitementPhase
				.valueOf(this.tabIdSource.get(ColumnEnum.PHASE_TRAITEMENT.getColumnName()).get(currentIndice))
				.getOrdre();
		
		if (startingPhase <= TraitementPhase.CHARGEMENT.getOrdre() && this.currentPhaseSet.contains(TraitementPhase.CHARGEMENT)) {
			ThreadChargementService chargementService = new ThreadChargementService();
			chargementService.configThread(connexion, this.currentIndice, this, this.currentPhaseSet.contains(TraitementPhase.NORMAGE),
					startingPhase == TraitementPhase.CHARGEMENT.getOrdre());
			chargementService.run();
		}

		if (startingPhase <= TraitementPhase.NORMAGE.getOrdre() && this.currentPhaseSet.contains(TraitementPhase.NORMAGE)) {
			ThreadNormageService normageService = new ThreadNormageService();
			normageService.configThread(connexion, this.currentIndice, this, this.currentPhaseSet.contains(TraitementPhase.CONTROLE),
					startingPhase == TraitementPhase.NORMAGE.getOrdre());
			normageService.run();
		}

		if (startingPhase <= TraitementPhase.CONTROLE.getOrdre() && this.currentPhaseSet.contains(TraitementPhase.CONTROLE)) {
			ThreadControleService controleService = new ThreadControleService();
			controleService.configThread(connexion, this.currentIndice, this, this.currentPhaseSet.contains(TraitementPhase.MAPPING),
					startingPhase == TraitementPhase.CONTROLE.getOrdre());
			controleService.run();
		}

		if (startingPhase <= TraitementPhase.MAPPING.getOrdre() && this.currentPhaseSet.contains(TraitementPhase.MAPPING)) {
			ThreadMappingService mappingService = new ThreadMappingService();
			mappingService.configThread(connexion, this.currentIndice, this, false,
					startingPhase == TraitementPhase.MAPPING.getOrdre());
			mappingService.run();
		}

	}

}
