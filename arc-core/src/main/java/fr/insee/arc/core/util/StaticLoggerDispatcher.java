package fr.insee.arc.core.util;

import org.apache.logging.log4j.Logger;

/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
@Deprecated
public class StaticLoggerDispatcher {

	private static LoggerDispatcher loggerDispatcher = new DefaultLoggerDispatcher();

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Object message, Logger logger) {
		loggerDispatcher.error(message, logger);		
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void error(Object message, Exception ex, Logger logger) {
		loggerDispatcher.error(message, ex, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Object message, Logger logger) {
		loggerDispatcher.warn(message, logger);		
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void warn(Object message, Exception ex, Logger logger) {
		loggerDispatcher.warn(message, ex, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Object message, Logger logger) {
		loggerDispatcher.info(message, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void info(Object message, Exception ex, Logger logger) {
		loggerDispatcher.info(message, ex, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Object message, Logger logger) {
		loggerDispatcher.debug(message, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void debug(Object message, Exception ex, Logger logger) {
		loggerDispatcher.debug(message, ex, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Object message, Logger logger) {
		loggerDispatcher.trace(message, logger);
	}

	/** Transition class. Use a LoggerDispatcher as an @Autowired attribute instead. */
	public static void trace(Object message, Exception ex, Logger logger) {
		loggerDispatcher.trace(message, ex, logger);
	}
	
}
