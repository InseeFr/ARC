package fr.insee.arc.core.service.engine.controle;

public class ControleRegleService {

	private ControleRegleService() {
		throw new IllegalStateException("Utility class");
	}

	/** Name of the XSD date format that should be translated in SQL */
	public static final String XSD_DATE_NAME = "xs:date";
	public static final String XSD_DATETIME_NAME = "xs:dateTime";
	public static final String XSD_TIME_NAME = "xs:time";

}
