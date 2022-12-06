package fr.insee.arc.batch.threadrunners;

import java.util.HashMap;

import fr.insee.arc.batch.threadrunners.parameter.ParameterKey;
import fr.insee.arc.batch.unitarylauncher.ChargerBatch;
import fr.insee.arc.batch.unitarylauncher.ComputeBatchArgs;
import fr.insee.arc.batch.unitarylauncher.ControlerBatch;
import fr.insee.arc.batch.unitarylauncher.FiltrerBatch;
import fr.insee.arc.batch.unitarylauncher.InitialiserBatch;
import fr.insee.arc.batch.unitarylauncher.MapperBatch;
import fr.insee.arc.batch.unitarylauncher.NormerBatch;
import fr.insee.arc.batch.unitarylauncher.RecevoirBatch;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;

public class ArcThreadFactory extends Thread {

	// parameters map for the thread
	// keys in parameterKey class
	private HashMap<String, String> mapParam;

	private TraitementPhase phaseName;

	private int previousNumberOfFilesLeft = -1;

	
	private ServiceReporting report = new ServiceReporting();
	
	
	public ArcThreadFactory(HashMap<String, String> mapParam, TraitementPhase phase) {
		initializeThreadRunnerTemplate(mapParam, phase);
	}

	@Override
	public void run()
	{
		execute();
	}
	
	
	public void execute() {

		switch (getPhaseName()) {
		case INITIALISATION:
			InitialiserBatch i = new InitialiserBatch(
					ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_SIZE_RECEPTION));
			i.execute();
			break;

		case RECEPTION:
			RecevoirBatch r = new RecevoirBatch(
					ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_SIZE_RECEPTION));
			r.execute();
			setReport(r.getReport());
			break;

		case CHARGEMENT:
			new ChargerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_TO_LOAD))
					.execute();
			break;

		case NORMAGE:
			new NormerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;

		case CONTROLE:
			new ControlerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;
			
		case FILTRAGE:
			new FiltrerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;

		case MAPPING:
			new MapperBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;

		default:
			break;
		}

	}


	private void initializeThreadRunnerTemplate(HashMap<String, String> mapParam, TraitementPhase phaseName) {
		setPhaseName(phaseName);
		setMapParam(mapParam);
	}

	
	/**
	 * return the status of the thread
	 * it queries the pilotage table to see if there is a blockage in the thread 
	 * by comparing the current files left with the previous recorded files left
	 * @return
	 */
	public boolean isBlocked() {

		boolean blocked;
		
		// count the number of file left to be processed in the phase
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT count(*) FROM " + this.getMapParam().get(ParameterKey.KEY_FOR_EXECUTION_ENVIRONMENT)
				+ ".pilotage_fichier WHERE etape=1 and phase_traitement='" + this.getPhaseName()
				+ "' and etat_traitement='{" + TraitementEtat.ENCOURS + "}'");

		int currentNumberOfFilesLeft = UtilitaireDao.get("arc").getInt(null, query);

		// compare it to the previous recorded number of files left
		// if still some files to go and if the stack hadn't moved, the thread will be marked as blocked
		if (currentNumberOfFilesLeft>0 && currentNumberOfFilesLeft == previousNumberOfFilesLeft) {
			blocked = true;
		} else {
			this.previousNumberOfFilesLeft = currentNumberOfFilesLeft;
			blocked = false;
		}
		return blocked;
	}

	public HashMap<String, String> getMapParam() {
		return mapParam;
	}

	public void setMapParam(HashMap<String, String> mapParam) {
		this.mapParam = mapParam;
	}

	public TraitementPhase getPhaseName() {
		return phaseName;
	}


	public void setPhaseName(TraitementPhase phaseName) {
		this.phaseName = phaseName;
	}


	public ServiceReporting getReport() {
		return report;
	}

	public void setReport(ServiceReporting report) {
		this.report = report;
	}
	
}
