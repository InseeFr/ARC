package fr.insee.arc.ws.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.ws.actions.SendResponse;

/**
 * Cette classe implémente l'interface DAO. C'est elle qui est responsable de récupérer les données de la base de données.
 *
 * @author N6YF91
 *
 */
public class QueryDaoImpl implements QueryDao {

    protected static final Logger LOGGER = LogManager.getLogger(QueryDaoImpl.class);

    /*
     * (non-Javadoc)
     *
     * @see dao.ResponseDao#doRequest(java.lang.String[], java.lang.String)
     */
    @Override
    @SQLExecutor
    public void doRequest(String id, SendResponse resp, long timestamp) throws ArcException {
        LoggerHelper.debugAsComment(LOGGER, timestamp, "QueryDaoImpl.doRequest()");
        Connection connection = null;
        ArrayList<ArrayList<String>> result = new ArrayList<>();

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();

            result = UtilitaireDao.get("arc").executeRequest(connection, new PreparedStatementBuilder("SELECT * FROM " + id + ";"));

            UtilitaireDao.get("arc").executeImmediate(connection, "DROP TABLE " + id + ";");

            if (result != null) {
                map(result, resp);
            }
        } finally {
            close(connection);
        }
        LoggerHelper.debugFinMethodeAsComment(getClass(), "doRequest()", LOGGER);
    }

    /*
     * (non-Javadoc)
     *
     * @see dao.ResponseDao#createImage(java.util.List, java.util.HashMap, java.lang.String)
     */
    @Override
    public void createImage(List<String> ids, HashMap<String, String> sqlRequests, long timestamp) throws ArcException {
        LoggerHelper.debugAsComment(LOGGER, timestamp, "QueryDaoImpl.createImage()");
        long beginning = System.currentTimeMillis();
        Connection connection = null;
        StringBuilder createImageSqlRequest = new StringBuilder();

        for (String id : ids) {
            createImageSqlRequest.append("CREATE TABLE " + id + " AS " + sqlRequests.get(id) + "; ");
        }

        try {
            long beginning1 = System.currentTimeMillis();
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            long time1 = System.currentTimeMillis() - beginning1;
            LoggerHelper.debugAsComment(LOGGER, timestamp, "QueryDaoImpl.createImage() : Connection Done -", time1, "ms");
            long beginning2 = System.currentTimeMillis();
            UtilitaireDao.get("arc").executeBlock(connection, createImageSqlRequest);
            long time2 = System.currentTimeMillis() - beginning2;
            LoggerHelper.debugAsComment(LOGGER, timestamp, "QueryDaoImpl.createImage() : ExecuteQuery(Create temp_) Done -", time2, "ms");

        } finally {
            close(connection);
        }
        long time = System.currentTimeMillis() - beginning;
        LoggerHelper.debugAsComment(LOGGER, time, "ms");
    }

    /**
     * Met en une table reçue par la base données en JSON.
     *
     * @param result
     *            Resultat reçu de la base de données.
     * @param resp
     *            Flux où écrire la réponse une fois mise en forme.
     */
    private void map(ArrayList<ArrayList<String>> result, SendResponse resp) {
        JSONArray table = new JSONArray();
        StringBuilder row = new StringBuilder();
        String cell;

        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.get(i).size(); j++) {
                cell = result.get(i).get(j);
                row.append(cell + ";");
            }
            table.put(row.toString());
            row.delete(0, row.length());
        }
        resp.send(table.toString());
    }

    /**
     * Ferme la connexion placée en paramètre
     *
     * @param connection
     *            Connexion à fermer.
     */
    private static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {
                LoggerHelper.errorGenTextAsComment(QueryDaoImpl.class, "close(Connection)", LOGGER, ex);
            }
        }
    }

}