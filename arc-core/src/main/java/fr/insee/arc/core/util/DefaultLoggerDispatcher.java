package fr.insee.arc.core.util;

import org.apache.logging.log4j.Logger;

/** Perform the minimum logging action.
 * */
public class DefaultLoggerDispatcher implements LoggerDispatcher {

	@Override
	public void error(Object message, Logger logger) {
		logger.error(sanitize(message));		
	}

	@Override
	public void error(Object message, Exception ex, Logger logger) {
		logger.error(message, ex);
	}

	@Override
	public void warn(Object message, Logger logger) {
		if (!logger.isWarnEnabled()) {
			return;
		}
		logger.warn(sanitize(message));		
	}

	@Override
	public void warn(Object message, Exception ex, Logger logger) {
		if (!logger.isWarnEnabled()) {
			return;
		}
		logger.warn(sanitize(message), ex);
	}

	@Override
	public void info(Object message, Logger logger) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		logger.info(sanitize(message));
	}

	@Override
	public void info(Object message, Exception ex, Logger logger) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		logger.info(sanitize(message), ex);
	}

	@Override
	public void debug(Object message, Logger logger) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		logger.debug(sanitize(message));
	}

	@Override
	public void debug(Object message, Exception ex, Logger logger) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		logger.debug(sanitize(message), ex);
	}

	@Override
	public void trace(Object message, Logger logger) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		logger.trace(sanitize(message));
	}

	@Override
	public void trace(Object message, Exception ex, Logger logger) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		logger.trace(sanitize(message), ex);
	}

}
