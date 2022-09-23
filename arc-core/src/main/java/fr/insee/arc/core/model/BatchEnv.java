package fr.insee.arc.core.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

/**
 * FIXME make abstract Classe des variables d'environnement liées au batch
 */
public class BatchEnv {

    private static final Logger LOGGER = LogManager.getLogger(BatchEnv.class);
    /*
     * Répertoires en entrée
     */
    public String repertoireReception;
    public String repertoireChargement;
    public String repertoireStockage;

    /*
     * Schéma de la base de données
     */
    public String databaseSchema;

    /*
     * Connexion
     */
    protected Connection connexion;

    public BatchEnv() {

	PropertiesHandler properties = PropertiesHandler.getInstance();

	this.repertoireReception = properties.getRegistrationDirectory();
	this.repertoireChargement = properties.getLoadingDirectory();
	this.repertoireStockage = properties.getStorageDirectory();

        try {
        	 Class.forName("org.postgresql.Driver");
     	    this.connexion = DriverManager.getConnection(properties.getDatabaseUrl(),
     		    properties.getDatabaseUsername(), properties.getDatabasePassword());
        } catch (SQLException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "BatchEnv()", LOGGER, ex);
        } catch (ClassNotFoundException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "BatchEnv()", LOGGER, ex);
        }

	this.databaseSchema = properties.getDatabaseSchema();
    }

}
