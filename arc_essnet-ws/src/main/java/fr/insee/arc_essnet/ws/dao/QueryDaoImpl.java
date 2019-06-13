package fr.insee.arc_essnet.ws.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import fr.insee.arc_essnet.utils.utils.SQLExecutor;
import fr.insee.arc_essnet.ws.actions.SendResponse;

/**
 * Cette classe implémente l'interface DAO. C'est elle qui est responsable de récupérer les données de la base de données.
 *
 * @author N6YF91
 *
 */
public class QueryDaoImpl implements QueryDao {

    protected static final Logger LOGGER = Logger.getLogger(QueryDaoImpl.class);

    /*
     * (non-Javadoc)
     *
     * @see dao.ResponseDao#doRequest(java.lang.String[], java.lang.String)
     */
    @Override
    @SQLExecutor
    public void doRequest(String id, SendResponse resp, long timestamp) {
        LoggerHelper.debugAsComment(LOGGER, timestamp, "QueryDaoImpl.doRequest()");
        long beginning = System.currentTimeMillis();
        Connection connection = null;
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

        try {
            long beginning1 = System.currentTimeMillis();
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            long time1 = System.currentTimeMillis() - beginning1;
            LoggerHelper.debugAsComment(LOGGER, timestamp, ": QueryDaoImpl.doRequest() : Connection Done - ", time1, "ms");

            long beginning2 = System.currentTimeMillis();
            result = UtilitaireDao.get("arc").executeRequest(connection, "SELECT * FROM " + id + ";");
            long time2 = System.currentTimeMillis() - beginning2;
            LoggerHelper.debugAsComment(LOGGER, timestamp + "QueryDaoImpl.doRequest() : ExecuteQuery(Get ", id, ") Done -", time2, "ms");

            long beginning3 = System.currentTimeMillis();
            UtilitaireDao.get("arc").executeRequest(connection, "DROP TABLE " + id + ";");
            long time3 = System.currentTimeMillis() - beginning3;
            LoggerHelper.debugAsComment(LOGGER, timestamp, "QueryDaoImpl.doRequest() : DropTable(", id, ") Done - ", time3, "ms");

            if (result != null) {
                map(result, resp);
            }
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
        long time = System.currentTimeMillis() - beginning;
        LoggerHelper.debugFinMethodeAsComment(getClass(), "doRequest()", LOGGER);
    }

    /*
     * (non-Javadoc)
     *
     * @see dao.ResponseDao#createImage(java.util.List, java.util.HashMap, java.lang.String)
     */
    @Override
    public void createImage(List<String> ids, HashMap<String, String> sqlRequests, long timestamp) {
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

        } catch (Exception e1) {
            throw new DAOException(e1);
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
            } catch (SQLException ex) {
                LoggerHelper.errorGenTextAsComment(QueryDaoImpl.class, "close(Connection)", LOGGER, ex);
            }
        }
    }

}