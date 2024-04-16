package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.Sanitize;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.webutils.WebAttributesName;
import fr.insee.arc.ws.services.importServlet.actions.InitiateRequest;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.RemoteHost;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletArc extends HttpServlet {

	private static final long serialVersionUID = 1L;

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
				boolean status = WebAttributesName.getHealthCheckStatus(map);
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

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		LoggerHelper.info(LOGGER, "doPost() begin");

		SendResponse resp = new SendResponse(response);
		
		try {

		// refactor with json body...
		String jsonInput = Sanitize.htmlParameter(request.getParameter("requests"));
		
		new InitiateRequest(parseParameterToJson(jsonInput), new RemoteHost(request)).doRequest(resp);
		
		} catch (ArcException e) {
			resp.sendError(e);
			e.logFullException();
		}

		LoggerHelper.info(LOGGER, "doPost() end");

	}
	
	
	private JSONObject parseParameterToJson(String jsonInput) throws ArcException
	{
		if (jsonInput == null) {
			throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
		}
		
		try {
			return new JSONObject(jsonInput);
		} catch (JSONException e)
		{
			throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
		}
	}


}