package fr.insee.arc.batch.operation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.batch.dao.BatchArcDao;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class PhaseInitializationOperation {

	private PhaseInitializationOperation() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * The initialization phase can trigger when the current date is more than
	 * the initialization date stored in database
	 * true if Initialization date 
	 * @return
	 * @throws ArcException
	 */
	public static boolean isInitializationMustTrigger(String envExecution) throws ArcException
	{
		String lastInitialize = BatchArcDao.execQueryLastInitialisationTimestamp(envExecution);

		Date dNow = new Date();
		Date dLastInitialize;

		try {
			dLastInitialize = new SimpleDateFormat(ArcDateFormat.DATE_HOUR_FORMAT_CONVERSION.getApplicationFormat())
					.parse(lastInitialize);
		} catch (ParseException dateParseException) {
			throw new ArcException(dateParseException, ArcExceptionMessage.BATCH_INITIALIZATION_DATE_PARSE_FAILED);
		}
		
		return (dLastInitialize.compareTo(dNow) < 0);
	}
	
	
}
