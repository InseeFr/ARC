package fr.insee.arc.ws.dao;

import java.util.ArrayList;

import org.json.JSONObject;

import fr.insee.arc.ws.actions.SendResponse;

public interface ClientDao {

    /**
     * Vérifie que le client peut consulter les tables métiers de la famille de normes
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param idFamille
     *            Id de la famille de norme.
     */
    void verificationClientFamille(long timestamp, String client, String idFamille, String environnement);

    /**
     * Créer une image des ids sources répondants aux critères et récupère la liste des noms des tables métiers
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param reprise
     *            Paramètre définissant si on renvoie juste les lignes non consultées (false ou non) ou toutes les lignes (true ou oui);
     * @param environnement
     *            Exemple : arc.bas
     * @param idFamille
     *            Id de la famille de norme.
     * @param validiteInf
     *            Validité inférieure.
     * @param validiteSup
     *            Validité supérieure.
     * @param periodicite
     *            Périodicité.
     * @return La liste des noms des tables métiers.
     * @deprecated
     */
    ArrayList<ArrayList<String>> getIdSrcTableMetier(long timestamp, String client, boolean reprise, String environnement, String idFamille,
            String validiteInf, String validiteSup, String periodicite);

    /**
     * Créer une image des ids sources répondants aux critères et récupère la liste des noms des tables métiers
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param JSONObject
     *            contient les paramètres de la requête
     * @return La liste des noms des tables métiers.
     */
    ArrayList<ArrayList<String>> getIdSrcTableMetier(long timestamp, JSONObject requeteJSON);

    /**
     * Créer une image des tables métiers.
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param environnement
     *            Exemple : arc.bas
     * @param tablesMetierNames
     *            La liste des noms des tables métiers.
     *
     * @return liste des noms de tables images crées
     */
    ArrayList<String> createImages(long timestamp, String client, String environnement, ArrayList<ArrayList<String>> tablesMetierNames);

    /**
     * Créer une image des tables métiers.
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param environnement
     *            Exemple : arc.bas
     * @param tablesMetierNames
     *            La liste des noms des tables métiers.
     *
     * @return liste des noms de tables images crées
     */
    void addImage(long timestamp, String client, String environnement, ArrayList<String> tableMetier, ArrayList<String> mesTablesImagesCrees);

    /**
     * Récupère les tables métiers.
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param tableMetierName
     *            La liste des noms des tables métiers.
     * @param environnement
     *            Exemple : arc.bas
     * @param resp
     *            Flux dans lequel on écrit la requête.
     * @return Retourne true s'il y a de nouvelles lignes.
     */
    void getResponse(long timestamp, String client, String tableMetierName, String environnement, SendResponse resp);

    /**
     * Met à jours les colonnes client et date_client de la table environnement_pilotage_fichier.
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param environnement
     *            Exemple : arc.bas
     */
    void updatePilotage(long timestamp, String environnement, String tableSource);

    /**
     * Renvoie les tables de nomenclatures.
     *
     * @param resp
     *            Flux dans lequel on écrit la requête.
     */
    void createNmcl(long timestamp, String client, String environnement);

    /**
     * Renvoie la table des variables métier.
     *
     * @param environnement
     *            Exemple : arc.bas
     * @param resp
     *            Flux dans lequel on écrit la requête.
     */
    void createVarMetier(long timestamp, String client, String idFamille, String environnement);

    /**
     * Renvoie la table des variables métier.
     *
     * @param environnement
     *            Exemple : arc.bas
     * @param resp
     *            Flux dans lequel on écrit la requête.
     */
    void createTableMetier(long timestamp, String client, String idFamille, String environnement);

    /**
     * Renvoie la table famille
     *
     * @param timestamp
     * @param client
     */
    void createTableFamille(long timestamp, String client, String environnement);

    String getAClientTable(String client) throws Exception;

    String getIdTable(String client) throws Exception;

    void dropTable(String clientTable) throws Exception;

    /**
     * Renvoie la table périodicité
     *
     * @param timestamp
     * @param client
     */
    void createTablePeriodicite(long timestamp, String client, String environnement);

}
