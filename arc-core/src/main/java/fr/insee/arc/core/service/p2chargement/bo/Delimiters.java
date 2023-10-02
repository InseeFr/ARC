package fr.insee.arc.core.service.p2chargement.bo;

public class Delimiters {

	private Delimiters() {
		throw new IllegalStateException("Utility class for constants delimiters");
	}

	public static final String HEADERS_DELIMITER = ",";
	public static final String DEFAULT_CSV_DELIMITER = ";";
	
}
