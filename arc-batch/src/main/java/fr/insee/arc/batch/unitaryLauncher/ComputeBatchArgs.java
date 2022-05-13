package fr.insee.arc.batch.unitaryLauncher;

import java.util.HashMap;

import fr.insee.arc.batch.threadRunners.parameter.ParameterKey;

public class ComputeBatchArgs {


	/**
	 * Compute the args for a batch
	 * @param mapParam
	 * @param capacity
	 * @return
	 */
	public static String[] batchArgs(HashMap<String, String> mapParam, String capacityParameter) {
		return new String[] { mapParam.get(ParameterKey.KEY_FOR_METADATA_ENVIRONMENT),
				mapParam.get(ParameterKey.KEY_FOR_EXECUTION_ENVIRONMENT),
				mapParam.get(ParameterKey.KEY_FOR_DIRECTORY_LOCATION), mapParam.get(capacityParameter),
				Boolean.parseBoolean(ParameterKey.KEY_FOR_KEEP_IN_DATABASE) ? null : mapParam.get(ParameterKey.KEY_FOR_BATCH_CHUNK_ID) };
	}

	  private ComputeBatchArgs() {
		    throw new IllegalStateException("Utility class");
		  }
	
	
}
