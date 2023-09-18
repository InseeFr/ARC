package fr.insee.arc.batch.threadrunners;

import java.util.Map;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ServiceReporting;

public class PhaseThreadFactory extends Thread {

	// parameters map for the thread
	// keys in parameterKey class
	private Map<String, String> mapParam;

	private TraitementPhase phaseName;

	private ServiceReporting report = new ServiceReporting();

	public PhaseThreadFactory(Map<String, String> mapParam, TraitementPhase phase) {
		initializeThreadRunnerTemplate(mapParam, phase);
	}

	@Override
	public void run() {
		execute();
	}

	public void execute() {

		this.report = ApiServiceFactory.getService( //
				getPhaseName().toString(), //
				mapParam.get(PhaseParameterKeys.KEY_FOR_METADATA_ENVIRONMENT), //
				mapParam.get(PhaseParameterKeys.KEY_FOR_EXECUTION_ENVIRONMENT), //
				mapParam.get(PhaseParameterKeys.KEY_FOR_DIRECTORY_LOCATION), //
				Integer.parseInt(mapParam.get(capacityParameterName())), //
				Boolean.parseBoolean(PhaseParameterKeys.KEY_FOR_KEEP_IN_DATABASE) ? null
						: mapParam.get(PhaseParameterKeys.KEY_FOR_BATCH_CHUNK_ID)) //
				.invokeApi();

	}

	/**
	 * The capacity parameter is the maximum number of objects that must take care a
	 * phase launch this method returns the parameter name to pick for the phase
	 * 
	 * @return
	 */
	protected String capacityParameterName() {

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
