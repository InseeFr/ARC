package fr.insee.arc.core.service.p5mapping.dao;

public class MappingQueriesForLargeFile {
	
	private static int PARALLEL_THRESHOLD_NUMBER_OF_RECORD_IN_FILE = 100000;
	private boolean largeFile;
	
	private static int NUMBER_OF_WORKER = 1;


	public MappingQueriesForLargeFile(int numberOfRecordInSourceTable)
	{
		largeFile = (numberOfRecordInSourceTable>PARALLEL_THRESHOLD_NUMBER_OF_RECORD_IN_FILE);
	}

	public StringBuilder kickParallelModeForLargeTable() {
		StringBuilder returned = new StringBuilder();
		if (largeFile)
		{
			returned.append("SET local max_parallel_workers_per_gather=").append(NUMBER_OF_WORKER).append("; SET local enable_sort=off; SET local debug_parallel_query=on;");
		}
		return returned;
	}

}
