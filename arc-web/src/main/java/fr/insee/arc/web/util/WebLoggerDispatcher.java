package fr.insee.arc.web.util;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.util.LoggerDispatcher;

@Component
public class WebLoggerDispatcher implements LoggerDispatcher {
	
	@Autowired
	private Session session;

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void error(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.error(message);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void error(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.error(message, ex);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void warn(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.warn(message);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void warn(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.warn(message, ex);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void info(Object message, Logger logger) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.info(message);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void info(Object message, Exception ex, Logger logger) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.info(message, ex);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void debug(Object message, Logger logger) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.debug(message);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void debug(Object message, Exception ex, Logger logger) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.debug(message, ex);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void trace(Object message, Logger logger) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.trace(message);
		}
	}

	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public void trace(Object message, Exception ex, Logger logger) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		try {
			registerMessage(message);
		} catch (Exception e) {
		} finally {
			logger.trace(message, ex);
		}
	}

	private void registerMessage(Object message) {
		session.put("console", (String) session.get("console") + new java.util.Date() + " - "
				+ message + "\n");
	}
}
