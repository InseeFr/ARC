package fr.insee.arc.core.service.p5mapping.dao;

import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import fr.insee.arc.utils.utils.FormatSQL;

public class MappingQueriesForLargeFile {

	public static final String TEMPORARY_UNION_TABLE = "tu";
	
	private static int PARALLEL_THRESHOLD_NUMBER_OF_RECORD_IN_FILE = 100000;
	private MappingQueriesFactory regleMappingFactory;
	private String nomTableSource;
	private boolean largeFile;
	
	private static int NUMBER_OF_WORKER = 1;


	public MappingQueriesForLargeFile(MappingQueriesFactory regleMappingFactory, String nomTableSource, int numberOfRecordInSourceTable)
	{
		this.regleMappingFactory = regleMappingFactory;
		this.nomTableSource = nomTableSource;
		largeFile = (numberOfRecordInSourceTable>PARALLEL_THRESHOLD_NUMBER_OF_RECORD_IN_FILE);
	}
	
	public void materializeUsefulColumnForLargeFile(StringBuilder returned, String expressionSQLCalculMapping) {
		if (!largeFile)
		{
			return;
		}
		
		returned.append("\n DROP TABLE IF EXISTS ").append(TEMPORARY_UNION_TABLE).append(";");
		
		returned.append("\n CREATE TEMPORARY TABLE ").append(TEMPORARY_UNION_TABLE).append(FormatSQL.WITH_NO_VACUUM);
		returned.append("\n AS SELECT ").append(computeSourceColumnsUsedInExpression(expressionSQLCalculMapping));
		returned.append("\n FROM ").append(this.nomTableSource).append(";");
	}

	public String sourceTable() {
		return largeFile?TEMPORARY_UNION_TABLE:this.nomTableSource;
	}
	
	public StringBuilder kickParallelModeForLargeTable() {
		StringBuilder returned = new StringBuilder();
		if (largeFile)
		{
			returned.append("SET local max_parallel_workers_per_gather=").append(NUMBER_OF_WORKER).append("; SET local parallel_setup_cost = 0; SET local parallel_tuple_cost = 0; SET local cpu_tuple_cost=0.05; SET local min_parallel_table_scan_size = '100MB';");
		}
		return returned;
	}
	
	/**
	 * Parse expression to identify what source column are used in
	 * @param expressionSQLCalculMapping
	 * @return
	 */
	private String computeSourceColumnsUsedInExpression (String expressionSQLCalculMapping)
	{
		return Streams.concat(regleMappingFactory.getEnsembleNomRubriqueExistante().stream()
				, regleMappingFactory.getEnsembleIdentifiantRubriqueExistante().stream())
		.filter(t-> expressionSQLCalculMapping.toLowerCase().contains(t)).collect(Collectors.joining(","));
	}
	
}
