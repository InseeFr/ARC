package fr.insee.arc.core.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerHelper;

public class TestLoggers {

	private static final Logger LOGGER = LogManager.getLogger(TestLoggers.class);
	
	public static final String GENERATED_MESSAGE_TO_TEST_LOGGERS="GENERATED MESSAGE TO TEST LOGGERS CONFIGURATION";
	public static final String SUCCESS="SUCCESSFUL";

	public static String sendLoggersTest(String origin)
	{
		String message=origin+" "+GENERATED_MESSAGE_TO_TEST_LOGGERS;
		LoggerHelper.error(LOGGER, message);
		LoggerHelper.warn(LOGGER, message);
		LoggerHelper.info(LOGGER, message);
		LoggerHelper.debug(LOGGER, message);
		LoggerHelper.trace(LOGGER, message);
		
		return message+" "+SUCCESS;
	}
	
}
