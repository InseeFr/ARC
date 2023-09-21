package fr.insee.arc.batch.threadrunners;

public class PhaseParameterKeys {


	// keys name for the hashmap mapParam containing the batch parameters
	public static final String KEY_FOR_EXECUTION_ENVIRONMENT = "envExecution";
	public static final String KEY_FOR_DIRECTORY_LOCATION = "repertoire";
	public static final String KEY_FOR_BATCH_CHUNK_ID = "numlot";
	public static final String KEY_FOR_MAX_SIZE_RECEPTION = "tailleMaxReceptionEnMb";
	public static final String KEY_FOR_MAX_FILES_TO_LOAD = "maxFilesToLoad";
	public static final String KEY_FOR_MAX_FILES_PER_PHASE = "maxFilesPerPhase";
	public static final String KEY_FOR_KEEP_IN_DATABASE = "keepInDatabase";
	
	
	 private PhaseParameterKeys() {
		    throw new IllegalStateException("Utility class");
		  }

}
