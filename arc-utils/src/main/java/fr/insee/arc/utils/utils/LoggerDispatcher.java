package fr.insee.arc.utils.utils;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;

public class LoggerDispatcher {
	/**
	 * met un message en console
	 *
	 * @param message
	 * @param logger
	 * @param reset
	 */
	public static void error(Object message, Logger logger) {
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
		}
		catch (Exception e) {
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
	public static void error(Object message, Exception ex, Logger logger) {
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void warn(Object message, Logger logger) {
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void warn(Object message, Exception ex, Logger logger) {
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void info(Object message, Logger logger) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void info(Object message, Exception ex, Logger logger) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void debug(Object message, Logger logger) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void debug(Object message, Exception ex, Logger logger) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void trace(Object message, Logger logger) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
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
	public static void trace(Object message, Exception ex, Logger logger) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		try {
			HttpSession session = ServletActionContext.getRequest().getSession(false);
			session.setAttribute("console", (String) session.getAttribute("console") + new java.util.Date() + " - "
					+ message + "\n");
		} catch (NoClassDefFoundError f) {
		} catch (Exception e) {
		} finally {
			logger.trace(message, ex);
		}
	}
}
