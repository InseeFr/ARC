package fr.insee.arc.core.service.p4controle.bo;

public class ControleXsdCode {

	private ControleXsdCode() {
		throw new IllegalStateException("Utility class");
	}

	/** Name of the XSD date format that should be translated in SQL */
	public static final String XSD_DATE_NAME = "xs:date";
	public static final String XSD_DATETIME_NAME = "xs:dateTime";
	public static final String XSD_TIME_NAME = "xs:time";

}
