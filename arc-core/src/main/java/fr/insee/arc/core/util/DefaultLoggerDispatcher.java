package fr.insee.arc.core.util;

import org.apache.logging.log4j.Logger;

/** Perform the minimum logging action.
 * */
public class DefaultLoggerDispatcher implements LoggerDispatcher {

	@Override
	public void error(Object message, Logger logger) {
		if (logger.isErrorEnabled()) {
			logger.error(sanitize(message));
		}		
	}

	@Override
	public void error(Object message, Exception ex, Logger logger) {
		if (logger.isErrorEnabled()) {
			logger.error(message, ex);
		}
	}

	@Override
	public void warn(Object message, Logger logger) {
		if (logger.isWarnEnabled()) {
			logger.warn(sanitize(message));		
		}
	}

	@Override
	public void warn(Object message, Exception ex, Logger logger) {
		if (logger.isWarnEnabled()) {
			logger.warn(sanitize(message), ex);
		}
	}

	@Override
	public void info(Object message, Logger logger) {
		if (logger.isInfoEnabled()) {
			logger.info(sanitize(message));
		}
	}

	@Override
	public void info(Object message, Exception ex, Logger logger) {
		if (logger.isInfoEnabled()) {
			logger.info(sanitize(message), ex);
		}		
	}

	@Override
	public void debug(Object message, Logger logger) {
		if (logger.isDebugEnabled()) {
			logger.debug(sanitize(message));
		}
		
	}

	@Override
	public void debug(Object message, Exception ex, Logger logger) {
		if (logger.isDebugEnabled()) {
			logger.debug(sanitize(message), ex);
		}		
	}

	@Override
	public void trace(Object message, Logger logger) {
		if (logger.isTraceEnabled()) {
			logger.trace(sanitize(message));
		}		
	}

	@Override
	public void trace(Object message, Exception ex, Logger logger) {
		if (logger.isTraceEnabled()) {
			logger.trace(sanitize(message), ex);
		}		
	}

}
