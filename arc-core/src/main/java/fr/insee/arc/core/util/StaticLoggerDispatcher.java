package fr.insee.arc.core.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.utils.LoggerHelper;

/** Transition class. Try to use a LoggerDispatcher as an @Autowired attribute instead. */
@Component
public class StaticLoggerDispatcher {


	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Exception e, Logger logger) {
		LoggerHelper.error(logger,ExceptionUtils.getStackTrace(e));
	}
	
	public static void error(Object message, Logger logger) {
		LoggerHelper.error(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Object message, Exception ex, Logger logger) {
		LoggerHelper.error(logger,message,ExceptionUtils.getStackTrace(ex));
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Object message, Logger logger) {
		LoggerHelper.warn(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Object message, Exception ex, Logger logger) {
		LoggerHelper.warn(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Object message, Logger logger) {
		LoggerHelper.info(logger,message);

	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Object message, Exception ex, Logger logger) {
		LoggerHelper.info(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Object message, Logger logger) {
		LoggerHelper.debug(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Object message, Exception ex, Logger logger) {
		LoggerHelper.debug(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Object message, Logger logger) {
		LoggerHelper.trace(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Object message, Exception ex, Logger logger) {
		LoggerHelper.trace(logger,message,ex);
	}
	
}
