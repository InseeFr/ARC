package fr.insee.arc.ws.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.actions.InitiateRequest;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.DAOException;

public class ServletArc extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String CONF_DAO_FACTORY = "daofactory";

    private static final Logger LOGGER = Logger.getLogger(ServletArc.class);


    @Override
    public void init() throws ServletException {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.getServletContext().getRequestDispatcher("/jsp/testPost.jsp").forward(request, response);
        } catch (ServletException |IOException ex) {
            LoggerHelper.error(LOGGER, "index()", ex);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LoggerHelper.info(LOGGER, "doPost() begin");
        JSONObject dsnRequest = null;

        if (request.getParameter("requests") != null) {
            dsnRequest = new JSONObject(request.getParameter("requests"));
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

}