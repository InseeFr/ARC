package fr.insee.arc.core.service.api.query;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.utils.FormatSQL;

public class ServiceDate {

	private ServiceDate() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Query that converts a java date to database data
	 * @param dateToCompute
	 * @return
	 */
	public static String queryDateConversion(Date dateToCompute) 
	{
		return FormatSQL.toDate(
				FormatSQL.quoteText(new SimpleDateFormat(EDateFormat.DATE_DASH.getApplicationFormat()).format(dateToCompute)),
				FormatSQL.quoteText(EDateFormat.DATE_DASH.getDatastoreFormat()));
	}
}
