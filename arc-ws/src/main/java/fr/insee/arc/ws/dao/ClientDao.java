package fr.insee.arc.ws.dao;

import java.util.ArrayList;

import org.json.JSONObject;

import fr.insee.arc.utils.exception.ArcException;
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
    void verificationClientFamille(long timestamp, String client, String idFamille, String environnement) throws ArcException;

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
     */
    ArrayList<ArrayList<String>> getIdSrcTableMetier(long timestamp, String client, boolean reprise, String environnement, String idFamille,
            String validiteInf, String validiteSup, String periodicite) throws ArcException;

    /**
     * Créer une image des ids sources répondants aux critères et récupère la liste des noms des tables métiers
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param JSONObject
     *            contient les paramètres de la requête
     * @return La liste des noms des tables métiers.
     * @throws ArcException 
     */
    ArrayList<ArrayList<String>> getIdSrcTableMetier(long timestamp, JSONObject requeteJSON) throws ArcException;

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
     * @throws ArcException 
     */
    ArrayList<String> createImages(long timestamp, String client, String environnement, ArrayList<ArrayList<String>> tablesMetierNames) throws ArcException;

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
     * @throws ArcException 
     */
    void addImage(long timestamp, String client, String environnement, ArrayList<String> tableMetier, ArrayList<String> mesTablesImagesCrees) throws ArcException;

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
     * @throws ArcException 
     */
    void getResponse(long timestamp, String client, String tableMetierName, String environnement, SendResponse resp) throws ArcException;

    /**
     * Met à jours les colonnes client et date_client de la table environnement_pilotage_fichier.
     *
     * @param timestamp
     *            Identifiant de la requête du client.
     * @param client
     *            Nom du client.
     * @param environnement
     *            Exemple : arc.bas
     * @throws ArcException 
     */
    void updatePilotage(long timestamp, String environnement, String tableSource) throws ArcException;

    /**
     * Renvoie les tables de nomenclatures.
     *
     * @param resp
     *            Flux dans lequel on écrit la requête.
     * @throws ArcException 
     */
    void createNmcl(long timestamp, String client, String environnement) throws ArcException;

    /**
     * Renvoie la table des variables métier.
     *
     * @param environnement
     *            Exemple : arc.bas
     * @param resp
     *            Flux dans lequel on écrit la requête.
     * @throws ArcException 
     */
    void createVarMetier(long timestamp, String client, String idFamille, String environnement) throws ArcException;

    /**
     * Renvoie la table des variables métier.
     *
     * @param environnement
     *            Exemple : arc.bas
     * @param resp
     *            Flux dans lequel on écrit la requête.
     * @throws ArcException 
     */
    void createTableMetier(long timestamp, String client, String idFamille, String environnement) throws ArcException;

    /**
     * Renvoie la table famille
     *
     * @param timestamp
     * @param client
     * @throws ArcException 
     */
    void createTableFamille(long timestamp, String client, String environnement) throws ArcException;

    String getAClientTable(String client) throws ArcException;

    String getIdTable(String client) throws ArcException;

    void dropTable(String clientTable) throws ArcException;

    /**
     * Renvoie la table périodicité
     *
     * @param timestamp
     * @param client
     * @throws ArcException 
     */
    void createTablePeriodicite(long timestamp, String client, String environnement) throws ArcException;

}
