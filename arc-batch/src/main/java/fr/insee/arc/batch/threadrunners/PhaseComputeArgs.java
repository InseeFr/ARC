package fr.insee.arc.batch.threadrunners;

import java.util.Map;

public class PhaseComputeArgs {

	/**
	 * Compute the args for a batch
	 * 
	 * @param mapParam
	 * @param capacity
	 * @return
	 */
	public static String[] batchArgs(Map<String, String> mapParam, String capacityParameter) {
		return new String[] { //
				mapParam.get(PhaseParameterKeys.KEY_FOR_METADATA_ENVIRONMENT), //
				mapParam.get(PhaseParameterKeys.KEY_FOR_EXECUTION_ENVIRONMENT), //
				mapParam.get(PhaseParameterKeys.KEY_FOR_DIRECTORY_LOCATION), //
				mapParam.get(capacityParameter), //
				Boolean.parseBoolean(PhaseParameterKeys.KEY_FOR_KEEP_IN_DATABASE) ? null
						: mapParam.get(PhaseParameterKeys.KEY_FOR_BATCH_CHUNK_ID) };
	}

	private PhaseComputeArgs() {
		throw new IllegalStateException("Utility class");
	}

}
