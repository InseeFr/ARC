package fr.insee.arc.batch.threadrunners;

import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;

public class PhaseThreadFactory extends Thread {

	// parameters map for the thread
	// keys in parameterKey class
	private Map<String, String> mapParam;

	private TraitementPhase phaseName;

	private int previousNumberOfFilesLeft = -1;

	
	private ServiceReporting report = new ServiceReporting();
	
	
	public PhaseThreadFactory(Map<String, String> mapParam, TraitementPhase phase) {
		initializeThreadRunnerTemplate(mapParam, phase);
	}

	@Override
	public void run()
	{
		execute();
	}
	
	
	public void execute() {
		this.report = ApiServiceFactory.getService(getPhaseName().toString(), PhaseComputeArgs.batchArgs(this.getMapParam(), capacityParameterName())).invokeApi();
	}

	/**
	 * The capacity parameter is the maximum number of objects that must take care a phase launch
	 * this method returns the parameter name to pick for the phase
	 * @return
	 */
	protected String capacityParameterName()
	{
		
		String capacityParameter;

		
		switch (getPhaseName()) {
		case INITIALISATION:
			capacityParameter = PhaseParameterKeys.KEY_FOR_MAX_SIZE_RECEPTION;
			break;

		case RECEPTION:
			capacityParameter = PhaseParameterKeys.KEY_FOR_MAX_SIZE_RECEPTION;
			break;

		case CHARGEMENT:
			capacityParameter = PhaseParameterKeys.KEY_FOR_MAX_FILES_TO_LOAD;
			break;

		default:
			capacityParameter = PhaseParameterKeys.KEY_FOR_MAX_FILES_PER_PHASE;
			break;
		}
		
		return capacityParameter;

	}

	private void initializeThreadRunnerTemplate(Map<String, String> mapParam, TraitementPhase phaseName) {
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
		query.append("SELECT count(*) FROM " + this.getMapParam().get(PhaseParameterKeys.KEY_FOR_EXECUTION_ENVIRONMENT)
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

	public Map<String, String> getMapParam() {
		return mapParam;
	}

	public void setMapParam(Map<String, String> mapParam) {
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
