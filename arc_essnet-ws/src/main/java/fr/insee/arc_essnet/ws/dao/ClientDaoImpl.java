package fr.insee.arc_essnet.ws.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc_essnet.core.model.TraitementState;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.core.service.AbstractPhaseService;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.format.Format;
import fr.insee.arc_essnet.utils.utils.FormatSQL;
import fr.insee.arc_essnet.utils.utils.JsonKeys;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import fr.insee.arc_essnet.utils.utils.ManipString;
import fr.insee.arc_essnet.utils.utils.SQLExecutor;
import fr.insee.arc_essnet.ws.actions.SendResponse;

public class ClientDaoImpl implements ClientDao {

    protected static final Logger LOGGER = Logger.getLogger(ClientDaoImpl.class);

    private static final char CHAR_SEPARATOR = (char) 1;
    private static final String FIELD_SEPARATOR = Character.toString(CHAR_SEPARATOR);

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#verificationClientFamille(long, java.lang.String, java.lang.String)
     */
    @Override
    public void verificationClientFamille(long timestamp, String client, String idFamille, String environnement) throws DAOException {
        LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl#verificationClientFamille()");
        Connection connection = null;

        String request = new String("SELECT EXISTS (SELECT 1 FROM arc.ihm_client WHERE id_application='" + client + "' AND id_famille='" + idFamille
                + "' LIMIT 1);");

        try {
            long beginning1 = System.currentTimeMillis();
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            long time1 = System.currentTimeMillis() - beginning1;
            LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl#verificationClientFamille() : Connection Done -", time1, "ms");
            long beginning2 = System.currentTimeMillis();
            String bool = UtilitaireDao.get("arc").executeRequestWithoutMetadata(connection, request).get(0).get(0);
            long time2 = System.currentTimeMillis() - beginning2;
            LoggerHelper.debugAsComment(LOGGER, timestamp, ": ClientDaoImpl.verificationClientFamille() : ExecuteQuery Done -", time2, "ms");

            if (!bool.equals("t")) {
                throw new DAOException("Vous ne pouvez pas accéder à cette famille de norme.");
            }
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#getIdSrcTableMetier(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @SQLExecutor
    public ArrayList<ArrayList<String>> getIdSrcTableMetier(long timestamp, String client, boolean reprise, String environnement, String idFamille,
            String validiteInf, String validiteSup, String periodicite) {
        //
        LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl#getIdSrcTableMetier()");
        Connection connection = null;
        ArrayList<ArrayList<String>> tablesMetierNames = new ArrayList<ArrayList<String>>();

        StringBuilder request = new StringBuilder("DROP TABLE IF EXISTS " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp
                + "_id_source; ");

        request.append("CREATE TABLE " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_id_source ");
        request.append("AS SELECT id_source FROM " + AbstractPhaseService.dbEnv(environnement) + "pilotage_fichier T1 ");
        request.append("WHERE '" + TraitementState.OK + "'=ANY(T1.etat_traitement) AND T1.periodicite='" + periodicite + "' ");
        if (validiteInf != "") {
            request.append("AND validite>='" + validiteInf + "' ");
        }
        request.append("AND validite<='" + validiteSup + "' AND T1.phase_traitement='" + TypeTraitementPhase.FORMAT_TO_MODEL + "' ");
        request.append("AND EXISTS (SELECT 1 FROM " + AbstractPhaseService.dbEnv(environnement) + "norme T2 WHERE T2.id_famille='" + idFamille
                + "' AND T1.id_norme=T2.id_norme) ");
        if (!reprise) {
            LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = false");
            request.append("AND NOT '" + client + "' = ANY(coalesce(T1.client, ARRAY[]::text[])) GROUP BY id_source");
        } else {
            LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = true");
            request.append("GROUP BY id_source");
        }
        request.append("; ");
        request.append("DROP TABLE IF EXISTS " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_mod_table_metier; ");

        request.append("CREATE TABLE " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_mod_table_metier ");
        request.append("AS SELECT nom_table_metier FROM " + AbstractPhaseService.dbEnv(environnement) + "mod_table_metier T1 ");
        request.append("WHERE T1.id_famille='" + idFamille + "' ");
        request.append("AND exists (select 1 from pg_tables T2 where ");
        request.append("T2.schemaname='" + ManipString.substringBeforeFirst(AbstractPhaseService.dbEnv(environnement), ".") + "' ");
        request.append("AND '" + ManipString.substringAfterFirst(AbstractPhaseService.dbEnv(environnement), ".") + "'||T1.nom_table_metier=T2.tablename);");

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            UtilitaireDao.get("arc").executeBlock(connection, request);
            tablesMetierNames = UtilitaireDao.get("arc").executeRequestWithoutMetadata(connection,
                    "SELECT nom_table_metier FROM " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_mod_table_metier;");
            UtilitaireDao.get("arc").executeRequest(connection,
                    "DROP TABLE " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_mod_table_metier;");
        } catch (Exception ex) {
            throw new DAOException(ex);
        } finally {
            close(connection);
        }
        return tablesMetierNames;
    }

    @Override
    @SQLExecutor
    public ArrayList<ArrayList<String>> getIdSrcTableMetier(long timestamp, JSONObject requeteJSON) {

        LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl#getIdSrcTableMetier()");

        // Initialisation des variables
        Connection connection = null;
        ArrayList<ArrayList<String>> tablesMetierNames = new ArrayList<ArrayList<String>>();
        final String env = AbstractPhaseService.dbEnv(requeteJSON.getString(JsonKeys.ENVIRONNEMENT.getKey()));
        final String client = requeteJSON.getString(JsonKeys.CLIENT.getKey());
        final String periodicite = requeteJSON.getString(JsonKeys.PERIODICITE.getKey());
        final String validiteSup = requeteJSON.getString(JsonKeys.VALSUP.getKey());
        final boolean reprise = requeteJSON.getBoolean(JsonKeys.REPRISE.getKey());
        final String idFamille = requeteJSON.getString(JsonKeys.FAMILLE.getKey());

        // Nombre de fichiers à recupérer (optionnel pour l'instant)
        int nbFichiers;

        try {
            nbFichiers = requeteJSON.getInt(JsonKeys.NBFICHIERS.getKey());
        } catch (Exception e) {
            LoggerHelper.debugAsComment(LOGGER, "Le nombre de fichiers à récupérer n'a pas été renseigné " + e);
            // par défaut, on considère que tous les fichiers sont à charger.
            nbFichiers = 0;
        }

        /**************************************************************************************************************************/
        /************************ CREATION DE LA TABLE TEMPORAIRE CONTENANT LES idsources A CHARGER ********************************/
        /**************************************************************************************************************************/

        // Préparation du block de requêtes à executer
        StringBuilder request = new StringBuilder("DROP TABLE IF EXISTS " + env + client + "_" + timestamp + "_id_source; ");

        // Création de la requête de création de la table temporaire contenant la liste des id_sources
        StringBuilder query = new StringBuilder();

        // Cas 1 : Avec limitation du nombre de fichiers à récupérer
        if (nbFichiers > 0) {

            query.append("CREATE TABLE " + env + client + "_" + timestamp + "_id_source ");
            query.append("AS SELECT id_source FROM (");
            query.append("SELECT id_source, substr(date_entree,1,10)::date as date_entree FROM " + env + "pilotage_fichier T1 ");
            query.append("WHERE '" + TraitementState.OK + "'=ANY(T1.etat_traitement) AND T1.periodicite='" + periodicite + "' ");

            if (requeteJSON.keySet().contains(JsonKeys.VALINF.getKey())) {
                query.append("AND validite>='" + requeteJSON.getString(JsonKeys.VALINF.getKey()) + "' ");
            }

            query.append("AND validite<='" + validiteSup + "' AND T1.phase_traitement='" + TypeTraitementPhase.FORMAT_TO_MODEL + "' ");
            query.append("AND EXISTS (SELECT 1 FROM " + env + "norme T2 WHERE T2.id_famille='" + idFamille + "' AND T1.id_norme=T2.id_norme) ");

            if (!reprise) {
                LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = false");
                query.append("AND NOT '" + client + "' = ANY(coalesce(T1.client, ARRAY[]::text[])) GROUP BY id_source, date_entree");
            } else {
                LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = true");
                query.append("GROUP BY id_source, date_entree ");
            }

            // on trie par ordre decroissant de date d'entree
            query.append("ORDER BY date_entree DESC LIMIT ");
            query.append(nbFichiers);
            query.append(") as foo; ");
        }

        // Cas 2 : Sans limitation du nombre de fichiers à récupérer
        else {
            query.append("CREATE TABLE " + env + client + "_" + timestamp + "_id_source ");
            query.append("AS SELECT id_source FROM " + env + "pilotage_fichier T1 ");
            query.append("WHERE '" + TraitementState.OK + "'=ANY(T1.etat_traitement) AND T1.periodicite='" + periodicite + "' ");

            if (requeteJSON.keySet().contains(JsonKeys.VALINF.getKey())) {
                query.append("AND validite>='" + requeteJSON.getString(JsonKeys.VALINF.getKey()) + "' ");
            }

            query.append("AND validite<='" + validiteSup + "' AND T1.phase_traitement='" + TypeTraitementPhase.FORMAT_TO_MODEL + "' ");
            query.append("AND EXISTS (SELECT 1 FROM " + env + "norme T2 WHERE T2.id_famille='" + idFamille + "' AND T1.id_norme=T2.id_norme) ");

            if (!reprise) {
                LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = false");
                query.append("AND NOT '" + client + "' = ANY(coalesce(T1.client, ARRAY[]::text[])) GROUP BY id_source");
            } else {
                LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = true");
                query.append("GROUP BY id_source ");
            }

            query.append("; ");
        }

        request.append(query);

        /**************************************************************************************************************************/
        /************* CREATION DE LA TABLE TEMPORAIRE CONTENANT LES NOMS DE TABLES METIERS CONCERNES POUR LE DECHARGEMENT *********/
        /**************************************************************************************************************************/

        request.append("DROP TABLE IF EXISTS " + env + client + "_" + timestamp + "_mod_table_metier; ");

        request.append("CREATE TABLE " + env + client + "_" + timestamp + "_mod_table_metier ");
        request.append("AS SELECT nom_table_metier FROM " + env + "mod_table_metier T1 ");
        request.append("WHERE T1.id_famille='" + idFamille + "' ");
        request.append("AND exists (select 1 from pg_tables T2 where ");
        request.append("T2.schemaname='" + ManipString.substringBeforeFirst(env, ".") + "' ");
        request.append("AND '" + ManipString.substringAfterFirst(env, ".") + "'||T1.nom_table_metier=T2.tablename);");

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            UtilitaireDao.get("arc").executeBlock(connection, request);
            tablesMetierNames = UtilitaireDao.get("arc").executeRequestWithoutMetadata(connection,
                    "SELECT nom_table_metier FROM " + env + client + "_" + timestamp + "_mod_table_metier;");
            UtilitaireDao.get("arc").executeRequest(connection, "DROP TABLE IF EXISTS " + env + client + "_" + timestamp + "_mod_table_metier;");
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }

        return tablesMetierNames;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#createImage(long, java.lang.String, java.lang.String, java.util.ArrayList)
     */
    @Override
    public ArrayList<String> createImages(long timestamp, String client, String environnement, ArrayList<ArrayList<String>> tablesMetierNames) {
        LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl.createImage()");

        ArrayList<String> mesTablesImagesCrees = new ArrayList<String>();

        for (ArrayList<String> tableMetier : tablesMetierNames) {

            addImage(timestamp, client, environnement, tableMetier, mesTablesImagesCrees);

        }

        return mesTablesImagesCrees;
    }

    @Override
    public void addImage(long timestamp, String client, String environnement, ArrayList<String> tableMetier, ArrayList<String> mesTablesImagesCrees) {
        Connection connection = null;
        StringBuilder request = new StringBuilder();
        String prefixeNomTableImage = new StringBuilder().append(AbstractPhaseService.dbEnv(environnement)).append(client).append("_").append(timestamp)
                .append("_").toString();

        String nomTableImage = prefixeNomTableImage + tableMetier.get(0);

        request.append("DROP TABLE IF EXISTS " + nomTableImage + "; ");

        request.append("CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS ");
        request.append("SELECT * ");
        request.append("FROM " + AbstractPhaseService.dbEnv(environnement) + tableMetier.get(0) + " T1 WHERE true ");
        // request.append("AND T1.id_source IN (SELECT id_source FROM " + ApiService.dbEnv(environnement) + client + "_" + timestamp +
        // "_id_source); ");
        request.append("AND exists (SELECT 1 FROM " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp
                + "_id_source T2 where T2.id_source=T1.id_source); ");

        mesTablesImagesCrees.add(nomTableImage);

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            UtilitaireDao.get("arc").executeBlock(connection, request);
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }

    }

    /**
     *
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#getResponse(long, java.lang.String, java.lang.String, fr.insee.arc_essnet.ws.actions.SendResponse)
     */
    @Override
    @SQLExecutor
    public void getResponse(long timestamp, String client, String tableMetierName, String environnement, SendResponse resp) {
        LoggerHelper.debugAsComment(LOGGER, timestamp, ": ClientDaoImpl.getResponse()");
        Connection connection = null;
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

        int nbLines = 0;
        int blockSize = 10000;
        int nbBlock = 0;

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();

            nbLines = UtilitaireDao.get("arc").getInt(connection,
                    "select max(rowid) FROM " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_" + tableMetierName);
            nbBlock = (nbLines - 1) / blockSize + 1;

            for (int i = 0; i < nbBlock; i++) {
                LoggerHelper.debugAsComment(LOGGER, "Traitement du bloc ", i, "/", (nbBlock - 1));
                result = UtilitaireDao.get("arc").executeRequest(
                        connection,
                        "SELECT * FROM " + AbstractPhaseService.dbEnv(environnement) + client + "_" + timestamp + "_" + tableMetierName + " u where rowid>" + i
                                * blockSize + " and rowid<=" + blockSize * (i + 1));
                resp.send("{\"" + JsonKeys.ID.getKey() + "\":\"" + ManipString.substringAfterFirst(AbstractPhaseService.dbEnv(environnement), ".")
                        + tableMetierName + "\",\"" + JsonKeys.TABLE.getKey() + "\":");
                mapJsonResponse(result, resp);
                resp.send("}");
            }

            // UtilitaireDao.get("arc").executeRequest(connection,
            // "DROP TABLE " + environnement + "_" + client + "_" + timestamp + "_" + tableMetierName + ";");

        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#updatePilotage(long, java.lang.String, java.lang.String)
     */
    public void updatePilotage(long timestamp, String environnement, String tableSource) {
        LoggerHelper.debugAsComment(LOGGER, timestamp, ": ClientDaoImpl.updatePilotage()");
        Connection connection = null;

        String client = ManipString.substringBeforeFirst(ManipString.substringAfterFirst(tableSource, "."), "_").toUpperCase();

        StringBuilder columnClient = new StringBuilder();
        columnClient.append("UPDATE " + AbstractPhaseService.dbEnv(environnement) + "pilotage_fichier T1 ");
        columnClient.append("SET client = array_append(client, '" + client + "') ");
        columnClient.append(", date_client = array_append( date_client, localtimestamp ) ");
        columnClient.append("WHERE true ");
        // columnClient.append("AND T1.id_source IN (SELECT id_source FROM " + ApiService.dbEnv(environnement) + client + "_" + timestamp+
        // "_id_source) ");
        columnClient.append("AND EXISTS (SELECT 1 FROM " + tableSource + " T2 where T1.id_source=T2.id_source) ");
        columnClient.append("AND T1.phase_traitement='" + TypeTraitementPhase.FORMAT_TO_MODEL + "';");

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            UtilitaireDao.get("arc").executeBlock(connection, columnClient.toString());
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#sendNmcl(fr.insee.arc_essnet.ws.actions.SendResponse)
     */
    @SQLExecutor
    public void createNmcl(long timestamp, String client, String environnement) {
        LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createNmcl()");
        Connection connection = null;
        ArrayList<ArrayList<String>> nmclNames = new ArrayList<>();
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        String schema = ManipString.substringBeforeFirst(AbstractPhaseService.dbEnv(environnement), ".");

        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            // System.out.println("SHOW client_encoding; "+UtilitaireDao.get("arc").executeRequest(connection, "SHOW client_encoding;"));
            nmclNames = UtilitaireDao.get("arc").executeRequestWithoutMetadata(connection,
                    "SELECT tablename FROM pg_tables WHERE schemaname = '" + schema + "' AND tablename LIKE 'nmcl_%'");

            String prefixeNomTableImage = new StringBuilder().append(AbstractPhaseService.dbEnv(environnement)).append(client).append("_").append(timestamp)
                    .append("_").toString();

            int i = 0;
            for (ArrayList<String> nmcl : nmclNames) {
                i++;
                String nomTableImage = prefixeNomTableImage + nmcl.get(0);

                UtilitaireDao.get("arc").executeRequest(connection,
                        "CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM " + schema + "." + nmcl.get(0) + ";");
            }
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#sendVarMetier(java.lang.String, fr.insee.arc_essnet.ws.actions.SendResponse)
     */
    public void createVarMetier(long timestamp, String client, String idFamille, String environnement) {
        LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createVarMetier()");
        Connection connection = null;
        // resp.send("{\"" + JsonKeys.ID.getKey() + "\":\"var_" + ManipString.substringAfterFirst(environnement, ".") + "\",\""
        // + JsonKeys.TABLE.getKey() + "\":");
        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();

            String prefixeNomTableImage = new StringBuilder().append(AbstractPhaseService.dbEnv(environnement)).append(client).append("_").append(timestamp)
                    .append("_").toString();
            String nomTableImage = prefixeNomTableImage + "mod_variable_metier";

            StringBuilder requete = new StringBuilder("CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS");
            requete.append("\n SELECT * FROM " + AbstractPhaseService.dbEnv(environnement)+"mod_variable_metier");
            requete.append("\n WHERE lower(id_famille) = lower('" + idFamille + "')");
            requete.append(";");
            UtilitaireDao.get("arc").executeRequest(
                    connection,
                    requete);
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#createTablesFamilles(long, java.lang.String)
     */
    @Override
    @SQLExecutor
    public void createTableFamille(long timestamp, String client, String environnement) {
        LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createTableFamille()");
        Connection connection = null;
        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            String schema = ManipString.substringBeforeFirst(AbstractPhaseService.dbEnv(environnement), ".");
            String prefixeNomTableImage = new StringBuilder().append(AbstractPhaseService.dbEnv(environnement)).append(client).append("_").append(timestamp)
                    .append("_").toString();
            String nomTableImage = prefixeNomTableImage + "ext_mod_famille";

            UtilitaireDao.get("arc").executeRequest(
                    connection,
                    "CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM
                            + " AS SELECT DISTINCT f.id_famille FROM arc.ihm_famille f INNER JOIN  "
                            + "arc.ihm_client c ON f.id_famille = c.id_famille WHERE lower(c.id_application) = lower('" + client + "');");
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#createTablesFamilles(long, java.lang.String)
     */
    @Override
    @SQLExecutor
    public void createTablePeriodicite(long timestamp, String client, String environnement) {
        LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createTablePeriodicite()");
        Connection connection = null;
        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();
            String prefixeNomTableImage = new StringBuilder().append(AbstractPhaseService.dbEnv(environnement)).append(client).append("_").append(timestamp)
                    .append("_").toString();
            String nomTableImage = prefixeNomTableImage + "ext_mod_periodicite";
            String schema = ManipString.substringBeforeFirst(AbstractPhaseService.dbEnv(environnement), ".");
            UtilitaireDao.get("arc").executeRequest(connection,
                    "CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS SELECT DISTINCT id, val FROM " + "arc.ext_mod_periodicite;");
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.arc_essnet.ws.dao.ClientDao#sendTableMetier(java.lang.String, fr.insee.arc_essnet.ws.actions.SendResponse)
     */
    public void createTableMetier(long timestamp, String client, String idFamille, String environnement) {
        LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.sendTableMetier()");
        Connection connection = null;
        try {
            connection = UtilitaireDao.get("arc").getDriverConnexion();

            String prefixeNomTableImage = new StringBuilder().append(AbstractPhaseService.dbEnv(environnement)).append(client).append("_").append(timestamp)
                    .append("_").toString();
            String nomTableImage = prefixeNomTableImage + "mod_table_metier";

            StringBuilder requete = new StringBuilder("\n CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS");
            requete.append("\n SELECT * FROM " + AbstractPhaseService.dbEnv(environnement) + "mod_table_metier");
            requete.append("\n WHERE lower(id_famille) = lower('" + idFamille + "')");
            requete.append(";");
            UtilitaireDao.get("arc").executeRequest(connection, requete);
        } catch (Exception e) {
            throw new DAOException(e);
        } finally {
            close(connection);
        }
    }

    /**
     * Met en une table reçue par la base données en JSON.
     *
     * @param result
     *            Resultat reçu de la base de données.
     * @param resp
     *            Flux où écrire la réponse une fois mise en forme.
     */
    private void mapJsonResponse(ArrayList<ArrayList<String>> result, SendResponse resp) {
        // JSONArray table = new JSONArray();
        List<String> table = new ArrayList<String>();
        StringBuilder row = new StringBuilder("\"");
        String cell;

        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.get(i).size(); j++) {
                cell = result.get(i).get(j);
                row.append(cell + FIELD_SEPARATOR);
            }
            // table.put(row.toString());
            table.add(row.append("\"").toString());
            row.delete(0, row.length());
            row.append("\"");
        }
        // System.out.println("LALALALA " + "[" + Format.untokenize(table, ",") + "]");
        resp.send("[" + Format.untokenize(table, ",") + "]");
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
                LoggerHelper.errorGenTextAsComment(ClientDaoImpl.class, "close(Connection)", LOGGER, ex);
            }
        }
    }

    public String getAClientTable(String client) throws Exception {
        String clientLc = client.toLowerCase();
        String schema = ManipString.substringBeforeFirst(clientLc, ".");
        String clientDb = ManipString.substringAfterFirst(clientLc, ".").replace("_", "\\_") + "%";

        String realClient = ManipString.substringBeforeFirst(ManipString.substringAfterFirst(client, "."), "_");

        String r = UtilitaireDao.get("arc").getString(
                null,
                "SELECT schemaname||'.'||tablename FROM pg_tables WHERE tablename like '" + clientDb + "' and schemaname='" + schema
                        + "' and tablename not like '%id\\_source%'");
        if (r != null) {
            r = r.replace(realClient.toLowerCase(), realClient);
        }
        return r;

    }

    public String getIdTable(String client) throws Exception {
        String clientLc = client.toLowerCase();
        String schema = ManipString.substringBeforeFirst(clientLc, ".");
        String clientDb = ManipString.substringAfterFirst(clientLc, ".").replace("_", "\\_") + "%";

        String realClient = ManipString.substringBeforeFirst(ManipString.substringAfterFirst(client, "."), "_");

        String r = UtilitaireDao.get("arc").getString(
                null,
                "SELECT schemaname||'.'||tablename FROM pg_tables WHERE tablename like '" + clientDb + "' and schemaname='" + schema
                        + "' and tablename like '%id\\_source%'");
        if (r != null) {
            r = r.replace(realClient.toLowerCase(), realClient);
        }
        return r;
    }

    public void dropTable(String clientTable) throws Exception {
        if (StringUtils.isBlank(clientTable)) {
            return;
        }
        UtilitaireDao.get("arc").dropTable(null, clientTable);
    }

}
