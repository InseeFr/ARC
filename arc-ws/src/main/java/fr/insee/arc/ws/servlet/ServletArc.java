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
import fr.insee.arc.ws.dao.ClientDao;
import fr.insee.arc.ws.dao.ClientDaoImpl;
import fr.insee.arc.ws.dao.DAOException;
import fr.insee.arc.ws.dao.QueryDao;
import fr.insee.arc.ws.dao.QueryDaoImpl;

public class ServletArc extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String CONF_DAO_FACTORY = "daofactory";

    private static final Logger LOGGER = Logger.getLogger(ServletArc.class);

    private QueryDao queryDao;
    private ClientDao clientDao;

    public void init() throws ServletException {
        this.queryDao = new QueryDaoImpl();
        this.clientDao = new ClientDaoImpl();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.getServletContext().getRequestDispatcher("/jsp/testPost.jsp").forward(request, response);
        } catch (ServletException |IOException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "index()", LOGGER, ex);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LoggerHelper.debugDebutMethodeAsComment(getClass(), "doPost()", LOGGER);
        JSONObject dsnRequest = null;

        long beginning = System.currentTimeMillis();

        if (request.getParameter("requests") != null) {
            dsnRequest = new JSONObject(request.getParameter("requests"));
            LoggerHelper.debugAsComment(LOGGER, "ServletArc.doPost(): Requête reçue : " + dsnRequest);
            SendResponse resp = new SendResponse(response);
            try {
                InitiateRequest action = new InitiateRequest(this.queryDao, this.clientDao, dsnRequest);
                action.doRequest(resp);
            } catch (DAOException e) {
                resp.send("{\"type\":\"jsonwsp/JSONObject\",\"error\":\"" + e.getMessage() + "\"}");
                resp.endSending();
            }

            long time = System.currentTimeMillis() - beginning;
            LoggerHelper.debugFinMethodeAsComment(getClass(), "doPost()", LOGGER);
        }
    }

}