package fr.insee.arc.core.model;

public class BatchMode {
	
	private BatchMode() {
		throw new IllegalStateException("Utility class");
	}
	
	public final static String UNSET = null;
	public final static String NORMAL = "N";
	public final static String KEEP_INTERMEDIATE_DATA = "K";

	
	public static String computeBatchMode(boolean isBatchMode, boolean isBatchModeMustKeepIntermediateData)
	{
		if (!isBatchMode) {
			return BatchMode.UNSET;
		}
		
		if (isBatchModeMustKeepIntermediateData) {
			return BatchMode.KEEP_INTERMEDIATE_DATA;
		}
		
		return BatchMode.NORMAL;
	}

}
