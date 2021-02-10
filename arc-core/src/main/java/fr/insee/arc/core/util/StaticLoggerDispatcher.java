package fr.insee.arc.core.util;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.utils.LoggerHelper;

/** Transition class. Try to use a LoggerDispatcher as an @Autowired attribute instead. */
@Component
public class StaticLoggerDispatcher {
	
//	private static LoggerDispatcher loggerDispatcher;
//	
//	public StaticLoggerDispatcher(@Autowired @Qualifier("activeLoggerDispatcher") LoggerDispatcher loggerDispatcherInstance) {
//		loggerDispatcher = loggerDispatcherInstance;
//	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Object message, Logger logger) {
//		loggerDispatcher.error(message, logger);
		LoggerHelper.error(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Object message, Exception ex, Logger logger) {
//		loggerDispatcher.error(message, ex, logger);
		LoggerHelper.error(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Object message, Logger logger) {
//		loggerDispatcher.warn(message, logger);
		LoggerHelper.warn(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Object message, Exception ex, Logger logger) {
//		loggerDispatcher.warn(message, ex, logger);
		LoggerHelper.warn(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Object message, Logger logger) {
//		loggerDispatcher.info(message, logger);
		LoggerHelper.info(logger,message);

	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Object message, Exception ex, Logger logger) {
//		loggerDispatcher.info(message, ex, logger);
		LoggerHelper.info(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Object message, Logger logger) {
//		loggerDispatcher.debug(message, logger);
		LoggerHelper.debug(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Object message, Exception ex, Logger logger) {
//		loggerDispatcher.debug(message, ex, logger);
		LoggerHelper.debug(logger,message,ex);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Object message, Logger logger) {
//		loggerDispatcher.trace(message, logger);
		LoggerHelper.trace(logger,message);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Object message, Exception ex, Logger logger) {
//		loggerDispatcher.trace(message, ex, logger);
		LoggerHelper.trace(logger,message,ex);
	}
	
}
