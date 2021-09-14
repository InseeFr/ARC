package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.actions.HealthCheck;
import fr.insee.arc.ws.actions.InitiateRequest;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.DAOException;

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
        	if (request.getRequestURI().endsWith("/status")) {
        		HealthCheck.status(response);
        	} else {
        		this.getServletContext().getRequestDispatcher("/jsp/testPost.jsp").forward(request, response);
        	}
        } catch (ServletException |IOException ex) {
            LoggerHelper.error(LOGGER, "index()", ex);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LoggerHelper.info(LOGGER, "doPost() begin");
        JSONObject dsnRequest = null;

        if (request.getParameter("requests") != null) {
        	dsnRequest = buildRequest(request);
        	
            LoggerHelper.info(LOGGER, "ServletArc.doPost(): Requête reçue : " + dsnRequest);
            
            SendResponse resp = new SendResponse(response);
            try {
                new InitiateRequest(dsnRequest).doRequest(resp);
            } catch (DAOException e) {
                resp.send("{\"type\":\"jsonwsp/JSONObject\",\"error\":\"" + e.getMessage() + "\"}");
                resp.endSending();
            }

            LoggerHelper.info(LOGGER, "doPost() end");
        }
    }

    /**
     * read JSON parameters provide by the http request
     * return the JSON object
     * @param request
     * @return
     */
    private JSONObject buildRequest(HttpServletRequest request) {

        // get parameters from request
    	JSONObject returned = new JSONObject(request.getParameter("requests"));
    	
    	// if SOURCE key is not specified, add all the default sources to be retrieved
    	if (returned.isNull(JsonKeys.SOURCE.getKey()))
    	{
    		return returned.put(JsonKeys.SOURCE.getKey(), DEFAULT_SOURCE);
    	}
    	
    	// if any correct source provided, exit
    	JSONArray sourcesProvidedByClient=returned.getJSONArray(JsonKeys.SOURCE.getKey());
		for (int i = 0; i < sourcesProvidedByClient.length(); i++) {
			if (DEFAULT_SOURCE.contains(sourcesProvidedByClient.getString(i))) {
				return returned;
			}
		}

		// if no sources provided, add all the default sources to be retrieved
        return returned.put(JsonKeys.SOURCE.getKey(), DEFAULT_SOURCE);

    }

    
}