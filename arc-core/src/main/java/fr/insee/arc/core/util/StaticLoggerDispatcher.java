package fr.insee.arc.core.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.utils.LoggerHelper;

/** Transition class. Try to use a LoggerDispatcher as an @Autowired attribute instead. */
@Component
public class StaticLoggerDispatcher {


	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Logger logger, Exception e) {
		LoggerHelper.error(logger,ExceptionUtils.getStackTrace(e));
	}
	
	public static void error(Logger logger, Object message) {
		LoggerHelper.error(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Logger logger, Object message, Exception ex) {
		LoggerHelper.error(logger,message,ExceptionUtils.getStackTrace(ex));
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Logger logger, Object message) {
		LoggerHelper.warn(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Logger logger, Object message, Exception ex) {
		LoggerHelper.warn(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Logger logger, Object message) {
		LoggerHelper.info(logger,message);

	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Logger logger, Object message, Exception ex) {
		LoggerHelper.info(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Logger logger, Object message) {
		LoggerHelper.debug(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Logger logger, Object message, Exception ex) {
		LoggerHelper.debug(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Logger logger, Object message) {
		LoggerHelper.trace(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Logger logger, Object message, Exception ex) {
		LoggerHelper.trace(logger,message,ex);
	}
	
}
