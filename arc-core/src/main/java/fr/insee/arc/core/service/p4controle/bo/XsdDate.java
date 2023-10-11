package fr.insee.arc.core.service.p4controle.bo;

import static java.util.Map.entry;

import java.util.Map;

public class XsdDate {

	private XsdDate() {
		throw new IllegalStateException("Utility class");
	}

	
	public static final Map<String, String[]> XSD_DATE_RULES = Map.ofEntries(
			
			// it is not really the absolute hour and minute but a relative time zone delta
			// TZ parameter would be a better implementation but need more info and likely
			// not needed
			// ,"YYYY-MM-DD+HH24:MI"
			// "YYYY-MM-DD-HH24:MI"
			entry(ControleXsdCode.XSD_DATE_NAME.toLowerCase(), new String[] { "YYYY-MM-DD", "YYYY-MM-DD\"Z\""}) ,
			
			entry(ControleXsdCode.XSD_DATETIME_NAME.toLowerCase(),
					new String[] { "YYYY-MM-DD\"T\"HH24:MI:SS", "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"" }) ,
			
			entry(ControleXsdCode.XSD_TIME_NAME.toLowerCase(), new String[] { "HH24:MI:SS" })

			
			);

}
