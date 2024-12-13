package fr.insee.arc.ws.services.importServlet.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.SecurityDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.services.importServlet.bo.RemoteHost;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WsSecurityDao {

	private static final Logger LOGGER = LogManager.getLogger(WsSecurityDao.class);

	private WsSecurityDao() {
		throw new IllegalStateException("Webservice SecurityDao class");
	}

	public static String validateClientIdentifier(String unsafe) throws ArcException
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT "+query.quoteText(unsafe)+" as client FROM arc.ihm_client where lower(id_application) = "+query.quoteText(unsafe.toLowerCase()));
		String result = UtilitaireDao.get(0).getString(null, query);
		return SecurityDao.validateOrThrow(result, unsafe);
	}
	
	/**
	 * Manage the security accesses and traces for the data retrieval webservice
	 * returns true if security acess is ok
	 * 
	 * @param request
	 * @param response
	 * @param dsnRequest
	 * @return
	 * @throws ArcException
	 */
	public static void securityAccessAndTracing(String familyName, String clientRealName, RemoteHost remoteHost)
			throws ArcException {

		// get the family name and client name

		ArcPreparedStatementBuilder query;
		// check if security is enable
		query = new ArcPreparedStatementBuilder();
		query.append("SELECT count(*) ");
		query.append("FROM arc.ihm_webservice_whitelist ");
		query.append("WHERE id_famille=" + query.quoteText(familyName) + " ");
		query.append("AND id_application=" + query.quoteText(clientRealName) + " ");

		if (UtilitaireDao.get(0).getInt(null, query) == 0) {
			LoggerHelper.warn(LOGGER, "Security is not enabled for (" + familyName + "," + clientRealName + ")");
			return;
		}

		// check the host
		String hostName = remoteHost.getName();

		query = new ArcPreparedStatementBuilder();
		query.append("SELECT is_secured ");
		query.append("FROM arc.ihm_webservice_whitelist ");
		query.append("WHERE id_famille=" + query.quoteText(familyName) + " ");
		query.append("AND id_application=" + query.quoteText(clientRealName) + " ");
		query.append("AND " + query.quoteText(hostName) + " like host_allowed ");

		Map<String, List<String>> result = new HashMap<>();
		try {
			result = new GenericBean(UtilitaireDao.get(0).executeRequest(null, query)).mapContent();
		} catch (ArcException e1) {
			LoggerHelper.error(LOGGER, "Error in querying host allowed");
			throw new ArcException(ArcExceptionMessage.HOST_NOT_RESOLVED);
		}

		if (result.isEmpty() || result.get("is_secured").isEmpty()) {
			LoggerHelper.error(LOGGER, "The host " + hostName + " has not been allowed to retrieved data of ("
					+ familyName + "," + clientRealName + "). Check the family norm interface to declare it.");

			throw new ArcException(ArcExceptionMessage.HOST_NOT_RESOLVED);
		}

		// check security and log query if security required
		boolean hostDeclaredAsSecured = !StringUtils.isBlank(result.get("is_secured").get(0));
		boolean requestSecured = remoteHost.isSecure();

		// if the request is not secured and the host had been declared as secured,
		// return
		// forbidden
		if (hostDeclaredAsSecured && !requestSecured) {
			LoggerHelper.error(LOGGER, hostName + " connexion is not secured. Abort.");
			throw new ArcException(ArcExceptionMessage.CONNEXION_NOT_SECURE);
		}

		// log the access
		query = new ArcPreparedStatementBuilder();
		
		// delete old logs
		query.build(SQL.DELETE, ViewEnum.SECURITY_WEBSERVICE_LOG.getFullName(), SQL.WHERE, "event_timestamp < current_timestamp - INTERVAL '3 MONTHS'", SQL.END_QUERY);
		// insert the new access
		query.build(SQL.INSERT_INTO,ViewEnum.SECURITY_WEBSERVICE_LOG.getFullName());
		query.build("(id_famille, id_application, host_allowed, event_timestamp)");
		query.append(SQL.SELECT)
			.build(query.quoteText(familyName))
			.build(",", query.quoteText(clientRealName))
			.build(",", query.quoteText(hostName))
			.build(",", "current_timestamp");
		// avoid spam; just writer real new access
		query.build(SQL.WHERE, "(")
			.build(query.quoteText(familyName))
			.build(",", query.quoteText(clientRealName))
			.build(",", query.quoteText(hostName))
			.build(") NOT IN ")
			.build("(SELECT id_famille, id_application, host_allowed FROM ", ViewEnum.SECURITY_WEBSERVICE_LOG.getFullName())
			.build(SQL.ORDER_BY, "id_webservice_logging desc")
			.build(SQL.LIMIT, "1", ")")
			.build(SQL.END_QUERY);
		
		try {
			UtilitaireDao.get(0).executeRequest(null, query);
		} catch (ArcException e) {
			LoggerHelper.error(LOGGER, "Error in logging webservice access");
			throw new ArcException(ArcExceptionMessage.HOST_NOT_RESOLVED);
		}

	}

	public static void sendForbidden(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException e) {
			LoggerHelper.error(LOGGER, "Error in sending forbidden to host " + request.getRemoteHost());
		}
	}

}
