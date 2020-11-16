package fr.insee.arc.core.util;

import org.apache.logging.log4j.Logger;

public interface LoggerDispatcher {

	/** Prevention against log injection.*/
	default String sanitize(Object message) {
		return message.toString().replaceAll("[\n|\r|\t]", "_");
	}

	/**
	 * Log a message as error. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param logger
	 */
	void error(Object message, Logger logger);

	/**
	 * Log an exception and a message as error. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param ex
	 * @param logger
	 */
	void error(Object message, Exception ex, Logger logger);

	/**
	 * Log a message as warn. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param logger
	 */
	void warn(Object message, Logger logger);

	/**
	 * Log an exception and a message as warn. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param ex
	 * @param logger
	 */
	void warn(Object message, Exception ex, Logger logger);

	/**
	 * Log a message as info. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param logger
	 */
	void info(Object message, Logger logger);

	/**
	 * Log an exception and a message as info. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param ex
	 * @param logger
	 */
	void info(Object message, Exception ex, Logger logger);

	/**
	 * Log a message as debug. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param logger
	 */
	void debug(Object message, Logger logger) ;

	/**
	 * Log an exception and a message as debug. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param ex
	 * @param logger
	 */
	public void debug(Object message, Exception ex, Logger logger);

	/**
	 * Log a message as trace. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void trace(Object message, Logger logger);

	/**
	 * Log an exception and a message as trace. Might perform one or more additional actions.
	 *
	 * @param message
	 * @param ex
	 * @param logger
	 */
	public void trace(Object message, Exception ex, Logger logger);
	
}