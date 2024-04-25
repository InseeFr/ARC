package fr.insee.arc.core.service.global.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.utils.FormatSQL;

public class DateConversion {

	private DateConversion() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Query that converts a java date to database data
	 * @param dateToCompute
	 * @return
	 */
	public static GenericPreparedStatementBuilder queryDateConversion(Date dateToCompute) 
	{		
		return FormatSQL.toDate(
				FormatSQL.quoteText(new SimpleDateFormat(ArcDateFormat.DATE_FORMAT_CONVERSION.getApplicationFormat()).format(dateToCompute)),
				FormatSQL.quoteText(ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat()));
	}
}
