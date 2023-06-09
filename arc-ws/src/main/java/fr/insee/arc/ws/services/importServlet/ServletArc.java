package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.webutils.WebUtils;
import fr.insee.arc.ws.actions.InitiateRequest;
import fr.insee.arc.ws.actions.SendResponse;

public class ServletArc extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String NOMENCLATURE = "nomenclature";
	public static final String METADATA = "metadata";
	public static final String MAPPING = "mapping";

	public static final String CONF_DAO_FACTORY = "daofactory";

	private static final List<String> DEFAULT_SOURCE = Arrays.asList(MAPPING, NOMENCLATURE, METADATA);

	private static final Logger LOGGER = LogManager.getLogger(ServletArc.class);

	@Override
	public void init() throws ServletException {
		// nothing
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getRequestURI().endsWith("/healthcheck")) {

				Map<String, Object> map = new HashMap<>();
				boolean status = WebUtils.getHealthCheckStatus(map);
				if (!status) {
					response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				} else {
					response.setStatus(HttpStatus.OK.value());
				}
				ServletOutputStream outputStream = response.getOutputStream();
				outputStream.write(map.toString().getBytes(StandardCharsets.UTF_8));
			}
		} catch (IOException ex) {
			LoggerHelper.error(LOGGER, "index()", ex);
		}
	}

	/**
	 * Manage the security accesses and traces for the data retrieval webservice
	 * returns true if security acess is ok
	 * 
	 * @param request
	 * @param response
	 * @param dsnRequest
	 * @return
	 */
	public boolean securityAccessAndTracing(HttpServletRequest request, HttpServletResponse response,
			JSONObject dsnRequest) {

		// get the family name and client name
		String familyName = dsnRequest.get("familleNorme").toString();
		String clientDeclared = dsnRequest.get("client").toString();
		String clientRealName = ManipString.substringBeforeFirst(ManipString.substringAfterFirst(clientDeclared, "."),
				"_");

		ArcPreparedStatementBuilder query;

		// check if security is enable
		query = new ArcPreparedStatementBuilder();
		query.append("SELECT count(*) ");
		query.append("FROM arc.ihm_webservice_whitelist ");
		query.append("WHERE id_famille=" + query.quoteText(familyName) + " ");
		query.append("AND id_application=" + query.quoteText(clientRealName) + " ");

		if (UtilitaireDao.get(0).getInt(null, query) == 0) {
			LoggerHelper.warn(LOGGER, "Security is not enabled for (" + familyName + "," + clientRealName + ")");
			return true;
		}

		// check the host
		String hostName;
		try {
			hostName = InetAddress.getByName(request.getRemoteHost()).getHostName();
		} catch (UnknownHostException e2) {
			LoggerHelper.warn(LOGGER, "No dns name found for host " + request.getRemoteHost());
			hostName = request.getRemoteHost();
		}

		query = new ArcPreparedStatementBuilder();
		query.append("SELECT is_secured ");
		query.append("FROM arc.ihm_webservice_whitelist ");
		query.append("WHERE id_famille=" + query.quoteText(familyName) + " ");
		query.append("AND id_application=" + query.quoteText(clientRealName) + " ");
		query.append("AND " + query.quoteText(hostName) + " like host_allowed ");

		HashMap<String, ArrayList<String>> result = new HashMap<>();
		try {
			result = new GenericBean(UtilitaireDao.get(0).executeRequest(null, query)).mapContent();
		} catch (ArcException e1) {
			LoggerHelper.error(LOGGER, "Error in querying host allowed");
		}

		if (result.isEmpty() || result.get("is_secured").isEmpty()) {

			sendForbidden(request, response);

			LoggerHelper.error(LOGGER,
					"The host " + request.getRemoteHost() + "/" + request.getRemoteAddr() + "/" + hostName
							+ " has not been allowed to retrieved data of (" + familyName + "," + clientRealName
							+ "). Check the family norm interface to declare it.");
			return false;
		}

		// check security and log query if security required
		boolean hostDeclaredAsSecured = !StringUtils.isBlank(result.get("is_secured").get(0));
		boolean requestSecured = request.isSecure();

		if (hostDeclaredAsSecured) {
			// if query is not secured and the host had been declared as secured, return
			// forbidden
			if (!requestSecured) {
				LoggerHelper.error(LOGGER, hostName + " connexion is not secured. Abort.");
				sendForbidden(request, response);
				return false;
			}
		}

		// log the access
		query = new ArcPreparedStatementBuilder();
		query.append(
				"DELETE FROM arc.ihm_webservice_log  where event_timestamp < current_timestamp - INTERVAL '1 YEAR';");
		query.append("INSERT INTO arc.ihm_webservice_log (id_famille, id_application, host_allowed, event_timestamp) ");
		query.append("SELECT " + query.quoteText(familyName) + ", " + query.quoteText(clientRealName) + ", "
				+ query.quoteText(hostName) + ", current_timestamp;");

		try {
			UtilitaireDao.get(0).executeRequest(null, query);
		} catch (ArcException e) {
			LoggerHelper.error(LOGGER, "Error in querying to register the connection entry");
		}

		return true;
	}

	public void sendForbidden(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException e) {
			LoggerHelper.error(LOGGER, "Error in sending forbidden to host " + request.getRemoteHost());
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		LoggerHelper.info(LOGGER, "doPost() begin");
		JSONObject dsnRequest = null;

		if (request.getParameter("requests") != null) {

			dsnRequest = buildRequest(request);

			if (securityAccessAndTracing(request, response, dsnRequest)) {

				LoggerHelper.info(LOGGER, "ServletArc.doPost(): Requête reçue : " + dsnRequest);

				SendResponse resp = new SendResponse(response);
				try {
					new InitiateRequest(dsnRequest).doRequest(resp);
				} catch (ArcException e) {
					resp.send("{\"type\":\"jsonwsp/JSONObject\",\"error\":\"" + e.getMessage() + "\"}");
					resp.endSending();
				}

				LoggerHelper.info(LOGGER, "doPost() end");
			}

		}
	}

	/**
	 * read JSON parameters provide by the http request return the JSON object
	 * 
	 * @param request
	 * @return
	 */
	private JSONObject buildRequest(HttpServletRequest request) {

		// get parameters from request
		JSONObject returned = new JSONObject(request.getParameter("requests"));

		// if SOURCE key is not specified, add all the default sources to be retrieved
		if (returned.isNull(JsonKeys.SOURCE.getKey())) {
			return returned.put(JsonKeys.SOURCE.getKey(), DEFAULT_SOURCE);
		}

		// if any correct source provided, exit
		JSONArray sourcesProvidedByClient = returned.getJSONArray(JsonKeys.SOURCE.getKey());
		for (int i = 0; i < sourcesProvidedByClient.length(); i++) {
			if (DEFAULT_SOURCE.contains(sourcesProvidedByClient.getString(i))) {
				return returned;
			}
		}

		// if no sources provided, add all the default sources to be retrieved
		return returned.put(JsonKeys.SOURCE.getKey(), DEFAULT_SOURCE);

	}

}