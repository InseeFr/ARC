package fr.insee.arc.core.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;

/**
 * FIXME make abstract Classe des variables d'environnement liées au batch
 */
public class BatchEnv {

    private static final Logger LOGGER = Logger.getLogger(BatchEnv.class);
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

	this.repertoireReception = properties.getRepertoireReception();
	this.repertoireChargement = properties.getRepertoireChargement();
	this.repertoireStockage = properties.getRepertoireStockage();

	try {
	    Class.forName("org.postgresql.Driver");
	    this.connexion = DriverManager.getConnection(properties.getDatabaseArcUrl(),
		    properties.getDatabaseArcUsername(), properties.getDatabaseArcPassword());
	} catch (SQLException ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "BatchEnv()", LOGGER, ex);
	} catch (ClassNotFoundException ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "BatchEnv()", LOGGER, ex);
	}

	this.databaseSchema = properties.getDatabaseArcSchema();
    }

}
