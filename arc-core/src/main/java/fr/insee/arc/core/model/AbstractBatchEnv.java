package fr.insee.arc.core.model;

import java.sql.Connection;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class AbstractBatchEnv {

    public AbstractBatchEnv() {
		super();
		PropertiesHandler properties = PropertiesHandler.getInstance();
		this.repertoireReception=properties.getRegistrationDirectory();
		this.repertoireChargement=properties.getLoadingDirectory();
		this.repertoireStockage=properties.getStorageDirectory();
	    this.databaseSchema=properties.getDatabaseSchema();
	    this.urlDatabase=properties.getDatabaseUrl();
	    this.usernameDatabase=properties.getDatabaseUsername();
	    this.passwordDatabase=properties.getDatabasePassword();
	}

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
    public String urlDatabase;
    public String usernameDatabase;
    public String passwordDatabase;

    /*
     * Connexion
     */
    protected Connection connexion;
    

}
