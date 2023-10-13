package fr.insee.arc.core.model;

public class Delimiters {

	private Delimiters() {
		throw new IllegalStateException("Utility class for constants delimiters");
	}

	public static final String HEADERS_DELIMITER = ",";
	public static final String DEFAULT_CSV_DELIMITER = ";";

	public static final String RENAME_SUFFIX= "$new$";
	public static final String PARTITION_NUMBER_PLACEHOLDER = "#pn#";
	
	public static final String SQL_TOKEN_DELIMITER = "_";
	public static final String SQL_SCHEMA_DELIMITER = ".";
		
}
