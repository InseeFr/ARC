package fr.insee.arc.ws.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Services;
import fr.insee.arc.ws.dao.DAOException;
import fr.insee.arc.ws.services.ExecuteProcessService;
import fr.insee.arc.ws.services.GetQueryResultService;
import fr.insee.arc.ws.services.ImportStep1InitializeClientTablesService;
import fr.insee.arc.ws.services.ImportStep2GetTableNameService;
import fr.insee.arc.ws.services.ImportStep3GetTableDataService;

/**
 * Cette classe permet d'initier le requêtage auprès de la base de données.
 *
 * @author N6YF91
 *
 */
public class InitiateRequest implements IDbConstant {

	protected static final Logger LOGGER = LogManager.getLogger(InitiateRequest.class);
	private JSONObject dsnRequest;

	/**
	 * Identifie le service solicité et préparation pour le traitement.
	 *
	 * Voici les formes des JSON reçus pour atteindre un des deux services : <br/>
	 * - service "query": { "type":"jsonwsp/request", "client":"string",
	 * "service":"query", "requests": [ { "id":"string", "sql":"string" }, ] } <br/>
	 * - service "arcClient": { "type":"jsonwsp/request",
	 *
	 * "client":"string", "service":"arcClient", "reprise":"boolean",
	 * "environnement":"string", "familleNorme":"string", "validiteInf":"string",
	 * "validiteSup":"string", "periodicite":"string" } <br/>
	 *
	 * @param queryDao   Objet responsable d'obtenir le données auprès de la base de
	 *                   donnée pour le service query.
	 * @param clientDao  Objet responsable d'obtenir le données auprès de la base de
	 *                   donnée pour le service arcClient.
	 * @param dsnRequest Le JSON contenant les paramètres de la requête.
	 */
	public InitiateRequest(JSONObject dsnRequest) {
		this.dsnRequest = dsnRequest;
	}

	/**
	 * Initie le requêtage en faisant appel au DAO.
	 *
	 * @param resp Le flux dans lequel on écrit la réponse.
	 */
	public void doRequest(SendResponse resp) {
		LoggerHelper.debugDebutMethodeAsComment(getClass(), "doRequest()", LOGGER);

		if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.QUERY.getService())) {
			new GetQueryResultService(dsnRequest).buildParam().execute(resp);
		} else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.CLIENT.getService())) {
			new ImportStep1InitializeClientTablesService(dsnRequest).buildParam().execute(resp);
		} else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.TABLE_NAME.getService())) {
			new ImportStep2GetTableNameService(dsnRequest).buildParam().execute(resp);
		} else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.TABLE_CONTENT.getService())) {
			new ImportStep3GetTableDataService(dsnRequest).buildParam().execute(resp);
		} else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.RUN.getService())) {
			new ExecuteProcessService(dsnRequest).buildParam().execute(resp);
		}
		else {
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"Le service n'est pas reconnu.\"}");
			resp.endSending();
			throw new DAOException("Le JSON n'est pas conforme");
		}
	}

	// resp.send("{\"type\":\"jsonwsp/response\",\"responses\":[");
	// int i = 0;
	// for (ArrayList<String> tableMetier : tablesMetierNames) {
	// String tableMetierString = tableMetier.get(0);
	// i++;
	// this.clientDao.getResponse(this.timestamp, client, tableMetierString,
	// environnement, resp);
	// if (i < tablesMetierNames.size()) {
	// resp.send(",");
	// }
	// }
	//
	// resp.send("],\"" + JsonKeys.NOMENCLATURES.getKey() + "\":[");
	// this.clientDao.sendNmcl(environnement, resp);
	// resp.send("],\"" + JsonKeys.TABLEMETIER.getKey() + "\":[");
	// this.clientDao.sendTableMetier(environnement, resp);
	// resp.send("],\"" + JsonKeys.VARMETIER.getKey() + "\":[");
	// this.clientDao.sendVarMetier(environnement, resp);
	// resp.send("]}");

	// ajouter la table normage
	// ArrayList<String> tableNormage=new ArrayList<String>();
	// tableNormage.add("normage_ok");
	//
	//
	// this.clientDao.addImage(this.timestamp, client, environnement, tableNormage,
	// tablesImagesCrees);

	// Ici on lance un thread parallèle pour decharger les données envoyées dans des
	// fichiers de sauvegarde
	// ... et pour supprimer les tables de travail dans la base.
	// tablesASupprimer.addAll(tablesImagesCrees);
	//
	// // on ajoute la table id_source
	// String nomTableIdSource = ApiService.dbEnv(environnement) + client + "_" +
	// timestamp + "_id_source";
	// tablesASupprimer.add(nomTableIdSource);
	//
	//
	// //On n'exporte les tables que si la paramètre json reprise = false
	// if(!reprise){
	// tablesAExporter.addAll(tablesImagesCrees);
	// }
	//
	//
	// dechargerDonneesDansFichiers(tablesAExporter,tablesASupprimer);

//    /**
//     * Decharger le contenu de tables metiers dans des fichiers
//     *
//     * @param tablesAExporter
//     *            : les tables que l'on veut decharger
//     * @param tablesASupprimer
//     */
//    private void dechargerDonneesDansFichiers(ArrayList<String> tablesAExporter, ArrayList<String> tablesASupprimer) {
//
//        String client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());
//        String environnement = this.dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());
//        List<String> requetes = new ArrayList<String>();
//
//        // Chemin de stockage de l'archive
//        String pathZip = null;
//        // Nom de strockage de l'archive
//        String nomZip = null;
//
//        // Si on a des tables à exporter, on construit les requêtes
//        // qui serviront pour la suite du traitement + instanciation du pathZip et du nomZip
//        if (!tablesAExporter.isEmpty()) {
//            requetes = getRequetes(tablesAExporter);
//
//            /* Constitution du nom du zip et du path */
//            nomZip = "wsimport_" + client.toLowerCase() + "_" + this.timestamp + ".zip";
//            pathZip = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire") + environnement.replace(".", "_").toUpperCase()
//                    + File.separator + EXPORT;
//
//            // vérifier si le directory existe. Sinon le créer
//            File f = new File(pathZip);
//            if (!f.exists()) {
//                f.mkdir();
//            }
//
//            pathZip = pathZip + File.separator;
//        }
//
//        // lancement de l'export
//        ExportThread exportThread = new ExportThread(tablesAExporter, requetes, pathZip, nomZip, tablesASupprimer);
//        if (!exportThread.isAlive()) {
//            exportThread.start();
//        }
//
//    }
//
//    /**
//     * Création des requêtes de récupération des données envoyées par le WS
//     *
//     * @param tablesMetierNames
//     * @return liste de requêtes
//     */
//    private List<String> getRequetes(ArrayList<String> tablesMetierNames) {
//        List<String> requetes = new ArrayList<>();
//
//        for (String tableName : tablesMetierNames) {
//            // il s'agit d'une table image et on récupère toute la table
//            requetes.add("SELECT  * FROM  " + tableName + " ");
//        }
//
//        return requetes;
//    }
//
//    /**
//     * Création d'un thread asynchrone pour ne pas que le client attende la fin de l'opération de décharge
//     *
//     * */
//    public static class ExportThread extends Thread {
//
//        /** Liste des tables à exporter */
//        private List<String> tableAExporter;
//
//        /** Liste des tables à supprimer */
//        private List<String> tablesASupprimer;
//
//        /** Liste des requetes pour l'export */
//        private List<String> requetes;
//        /** Nom et chemin du zip */
//        private String nomZip;
//        private String pathZip;
//
//        /** Logger */
//        public static final Logger LOGGER = LogManager.getLogger(InitiateRequest.class);
//
//        public ExportThread(List<String> tableAExporter, List<String> requetes, String pathZip, String nomZip, List<String> tablesASupprimer) {
//
//            super();
//            this.tableAExporter = tableAExporter;
//            this.requetes = requetes;
//            this.pathZip = pathZip;
//            this.nomZip = nomZip;
//            this.tablesASupprimer = tablesASupprimer;
//        }
//
//        @Override
//        public void run() {
//            try {
//                // Si on a des tables à exporter
//                if (!this.tableAExporter.isEmpty()) {
//                    export(this.tableAExporter, this.requetes, this.pathZip, this.nomZip);
//                }
//                // On supprime les tables créees au cours du traitement
//                supprimerTablesTemp(this.tablesASupprimer);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        /**
//         *
//         * //TODO : Méthode à exporter dans un service
//         *
//         * Téléchargement dans un zip de N fichiers csv, les données étant extraites de la base de données
//         *
//         * @param tableNames
//         * @param requetes
//         * @param nomApplicationCliente
//         * @param environnementSource
//         * @throws SQLException
//         * @throws ClassNotFoundException       * @throws IOException
//         */
//        public void export(List<String> tableNames, List<String> requetes, String pathZip, String nomZip) throws Exception {
//
//            File zipFile = new File(Paths.get(pathZip).resolve(nomZip).toString());
//            FileOutputStream fop = new FileOutputStream(zipFile);
//            ZipOutputStream zos = new ZipOutputStream(fop);
//            Connection connexion = UtilitaireDao.get(poolName).getDriverConnexion();
//
//            try {
//                for (int i = 0; i < tableNames.size(); i++) {
//                    // Ajout d'un nouveau fichier
//                    ZipEntry entry = new ZipEntry(tableNames.get(i) + ".csv");
//                    zos.putNextEntry(entry);
//                    UtilitaireDao.get(poolName).outStreamRequeteSelect(connexion, requetes.get(i), zos);
//                    zos.closeEntry();
//                }
//
//            } finally {
//                zos.close();
//                fop.flush();
//                fop.close();
//                connexion.close();
//                connexion = null;
//            }
//
//        }
//
//        private void supprimerTablesTemp(List<String> nomTablesASupprimer) throws Exception {
//
//            Connection connexion = UtilitaireDao.get(poolName).getDriverConnexion();
//
//            try {
//                UtilitaireDao.get("arc").dropTable(connexion, nomTablesASupprimer);
//            } finally {
//                connexion.close();
//            }
//
//        }

//    }

}
