package fr.insee.arc_essnet.ws.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc_essnet.core.model.DbConstant;
import fr.insee.arc_essnet.core.service.AbstractPhaseService;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.JsonKeys;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import fr.insee.arc_essnet.utils.utils.SQLExecutor;
import fr.insee.arc_essnet.utils.utils.Services;
import fr.insee.arc_essnet.ws.dao.ClientDao;
import fr.insee.arc_essnet.ws.dao.DAOException;
import fr.insee.arc_essnet.ws.dao.QueryDao;

/**
 * Cette classe permet d'initier le requêtage auprès de la base de données.
 *
 * @author N6YF91
 *
 */
public class InitiateRequest{

    protected static final Logger LOGGER = Logger.getLogger(InitiateRequest.class);
    private static final String EXPORT = "EXPORT";
    private QueryDao queryDao;
    private ClientDao clientDao;
    private JSONObject dsnRequest;
    private List<String> ids;
    private HashMap<String, String> sqlRequests;
    private int service;
    private long timestamp;

    /**
     * Identifie le service solicité et préparation pour le traitement.
     *
     * Voici les formes des JSON reçus pour atteindre un des deux services : <br/>
     * - service "query": { "type":"jsonwsp/request", "client":"string", "service":"query", "requests": [ { "id":"string", "sql":"string" },
     * ] } <br/>
     * - service "arcClient": { "type":"jsonwsp/request",
     *
     * "client":"string", "service":"arcClient", "reprise":"boolean", "environnement":"string", "familleNorme":"string",
     * "validiteInf":"string", "validiteSup":"string", "periodicite":"string" } <br/>
     *
     * @param queryDao
     *            Objet responsable d'obtenir le données auprès de la base de donnée pour le service query.
     * @param clientDao
     *            Objet responsable d'obtenir le données auprès de la base de donnée pour le service arcClient.
     * @param dsnRequest
     *            Le JSON contenant les paramètres de la requête.
     */
    public InitiateRequest(QueryDao queryDao, ClientDao clientDao, JSONObject dsnRequest) {
        this.queryDao = queryDao;
        this.clientDao = clientDao;
        this.timestamp = System.currentTimeMillis();
        if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.QUERY.getService())) {
            this.ids = new ArrayList<String>();
            this.sqlRequests = new HashMap<String, String>();
            this.service = 0;
            parseRequests(dsnRequest);
        } else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.CLIENT.getService())) {
            this.service = 1;
            this.dsnRequest = new JSONObject();
            this.dsnRequest = dsnRequest;
        } else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.TABLE_NAME.getService())) {
            this.service = 2;
            this.dsnRequest = new JSONObject();
            this.dsnRequest = dsnRequest;
        } else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.TABLE_CONTENT.getService())) {
            this.service = 3;
            this.dsnRequest = new JSONObject();
            this.dsnRequest = dsnRequest;
        } else {
            throw new DAOException("Le JSON n'est pas conforme");
        }
    }

    /**
     * Cette fonction permet de parser les requêtes sql contenue dans le fichier JSON reçu.
     *
     * @param dsnRequest
     */
    private void parseRequests(JSONObject dsnRequest) {
        JSONObject sqlRequest = new JSONObject();
        for (int i = 0; i < dsnRequest.getJSONArray(JsonKeys.REQUESTS.getKey()).length(); i++) {
            sqlRequest = dsnRequest.getJSONArray(JsonKeys.REQUESTS.getKey()).getJSONObject(i);
            if (this.sqlRequests.containsKey(sqlRequest.getString(JsonKeys.ID.getKey())) == false) {
                if (sqlRequest.getString(JsonKeys.ID.getKey()) != "") {
                    this.ids.add("temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_"
                            + sqlRequest.getString(JsonKeys.ID.getKey()));
                    this.sqlRequests.put(
                            "temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_"
                                    + sqlRequest.getString(JsonKeys.ID.getKey()), sqlRequest.getString(JsonKeys.SQL.getKey()));
                } else {
                    this.ids.add("temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_r" + i);
                    this.sqlRequests.put("temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_r" + i,
                            sqlRequest.getString(JsonKeys.SQL.getKey()));
                }
            } else {
                throw new DAOException("Id présent plusieurs fois : " + sqlRequest.getString(JsonKeys.ID.getKey()));
            }
        }
    }

    /**
     * Initie le requêtage en faisant appel au DAO.
     *
     * @param resp
     *            Le flux dans lequel on écrit la réponse.
     */
    @SQLExecutor
    public void doRequest(SendResponse resp) {
        LoggerHelper.debugDebutMethodeAsComment(getClass(), "doRequest()", LOGGER);

        String environnement;
        String client;
        boolean reprise;

        // Identifie le service
        switch (this.service) {
        // Cas requête générique :
        case 0:
            try {
                this.queryDao.createImage(this.ids, this.sqlRequests, this.timestamp);
                resp.send("{\"type\":\"jsonwsp/response\",\"responses\":[");
                int i = 0;
                for (String id : this.ids) {
                    i++;
                    resp.send("{\"" + JsonKeys.ID.getKey() + "\":\"");
                    for (int j = 3; j < id.split("_").length; j++) {
                        if (j != 3) {
                            resp.send("_");
                        }
                        resp.send(id.split("_")[j]);
                    }
                    resp.send("\",\"" + JsonKeys.TABLE.getKey() + "\":");
                    this.queryDao.doRequest(id, resp, this.timestamp);
                    resp.send("}");
                    if (this.ids.size() != i) {
                        resp.send(",");
                    }
                }
                resp.send("]}");
                resp.endSending();
            } catch (DAOException e) {
                resp.send("{\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
                resp.endSending();
            }
            break;

        // requete arc 1
        case 1:
            ArrayList<ArrayList<String>> tablesMetierNames = new ArrayList<ArrayList<String>>();
            environnement = this.dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());
            client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());
            reprise = this.dsnRequest.getBoolean(JsonKeys.REPRISE.getKey());

            try {

                if (!environnement.equalsIgnoreCase("arc")) {
                    this.clientDao.verificationClientFamille(this.timestamp, client, this.dsnRequest.getString(JsonKeys.FAMILLE.getKey()),
                            environnement);
                    tablesMetierNames = this.clientDao.getIdSrcTableMetier(this.timestamp, this.dsnRequest);
                    this.clientDao.createImages(this.timestamp, client, environnement, tablesMetierNames);
                    this.clientDao.createTableMetier(this.timestamp, client,this.dsnRequest.getString(JsonKeys.FAMILLE.getKey()), environnement);
                    this.clientDao.createVarMetier(this.timestamp, client, this.dsnRequest.getString(JsonKeys.FAMILLE.getKey()), environnement);
                }
                this.clientDao.createNmcl(this.timestamp, client, environnement);
                this.clientDao.createTableFamille(this.timestamp, client, environnement);
                this.clientDao.createTablePeriodicite(this.timestamp, client, environnement);
                // on renvoie l'id du client avec son timestamp
                resp.send(AbstractPhaseService.dbEnv(environnement) + client + "_" + this.timestamp);
                resp.endSending();

            } catch (DAOException e) {
                e.printStackTrace();
                resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
                resp.endSending();
            }
            break;

        case 2:
            try {
                StringBuilder type = new StringBuilder();

                client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());
                environnement = this.dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());
                reprise = this.dsnRequest.getBoolean(JsonKeys.REPRISE.getKey());

                String tableName = this.clientDao.getAClientTable(client);

                if (tableName == null) {
                    tableName = this.clientDao.getIdTable(client);

                    if (!reprise) {
                        this.clientDao.updatePilotage(this.timestamp, environnement, tableName);
                    }

                    this.clientDao.dropTable(tableName);
                    tableName = "";
                } else {
                    // récupération du type
                    ArrayList<ArrayList<String>> l = UtilitaireDao.get("arc").executeRequest(null, "select * from " + tableName + " where false ");

                    for (int j = 0; j < l.get(0).size(); j++) {
                        if (j > 0) {
                            type.append(",");
                        }

                        for (int i = 0; i < l.size(); i++) {
                            type.append(" " + l.get(i).get(j));
                        }
                    }
                }

                // renvoie un nom de table du client si il en reste une
                resp.send(tableName + " " + type);
                resp.endSending();

            } catch (Exception e) {
                e.printStackTrace();
                resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
                resp.endSending();
            }

            break;

        case 3:
            try {
                client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());

                UtilitaireDao.get("arc").exporting(null, client, resp.getWr(), false);
                this.clientDao.dropTable(client);

                resp.endSending();

                // renvoie un nom de table du client si il en reste une

            } catch (Exception e) {
                e.printStackTrace();
                resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
                resp.endSending();
            }

            break;

        default:
            resp.send("\"type\":\"jsonwsp/response\",\"error\":\"Le service n'est pas reconnu.\"}");
            resp.endSending();
            break;
        }
    }


  
    /**
     * Création d'un thread asynchrone pour ne pas que le client attende la fin de l'opération de décharge
     *
     * */
    public static class ExportThread extends Thread {

        /** Liste des tables à exporter */
        private List<String> tableAExporter;

        /** Liste des tables à supprimer */
        private List<String> tablesASupprimer;

        /** Liste des requetes pour l'export */
        private List<String> requetes;
        /** Nom et chemin du zip */
        private String nomZip;
        private String pathZip;

        /** Logger */
        public static final Logger LOGGER = Logger.getLogger(InitiateRequest.class);

        public ExportThread(List<String> tableAExporter, List<String> requetes, String pathZip, String nomZip, List<String> tablesASupprimer) {

            super();
            this.tableAExporter = tableAExporter;
            this.requetes = requetes;
            this.pathZip = pathZip;
            this.nomZip = nomZip;
            this.tablesASupprimer = tablesASupprimer;
        }

        @Override
        public void run() {
            try {
                // Si on a des tables à exporter
                if (!this.tableAExporter.isEmpty()) {
                    export(this.tableAExporter, this.requetes, this.pathZip, this.nomZip);
                }
                // On supprime les tables créees au cours du traitement
                supprimerTablesTemp(this.tablesASupprimer);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         *
         * //TODO : Méthode à exporter dans un service
         *
         * Téléchargement dans un zip de N fichiers csv, les données étant extraites de la base de données
         *
         * @param tableNames
         * @param requetes
         * @param nomApplicationCliente
         * @param environnementSource
         * @throws SQLException
         * @throws ClassNotFoundException
         * @throws IOException
         */
        public void export(List<String> tableNames, List<String> requetes, String pathZip, String nomZip) throws Exception {

            File zipFile = new File(Paths.get(pathZip).resolve(nomZip).toString());
            
            Connection connexion = UtilitaireDao.get(DbConstant.POOL_NAME).getDriverConnexion();

            try (FileOutputStream fop = new FileOutputStream(zipFile)) {
        	ZipOutputStream zos = new ZipOutputStream(fop);
                for (int i = 0; i < tableNames.size(); i++) {
                    // Ajout d'un nouveau fichier
                    ZipEntry entry = new ZipEntry(tableNames.get(i) + ".csv");
                    zos.putNextEntry(entry);
                    UtilitaireDao.get(DbConstant.POOL_NAME).outStreamRequeteSelect(connexion, requetes.get(i), zos);
                    zos.closeEntry();
                }

            } finally {
                connexion.close();
                connexion = null;
            }

        }

        private void supprimerTablesTemp(List<String> nomTablesASupprimer) throws Exception {

            Connection connexion = UtilitaireDao.get(DbConstant.POOL_NAME).getDriverConnexion();

            try {
                UtilitaireDao.get("arc").dropTable(connexion, nomTablesASupprimer);
            } finally {
                connexion.close();
            }

        }

    }

}
