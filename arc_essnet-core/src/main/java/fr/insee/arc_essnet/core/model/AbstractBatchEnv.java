package fr.insee.arc_essnet.core.model;

import java.sql.Connection;
import fr.insee.arc_essnet.utils.ressourceUtils.PropertiesHandler;

public class AbstractBatchEnv {

    public AbstractBatchEnv() {
		super();
		PropertiesHandler properties = PropertiesHandler.getInstance();
		this.repertoireReception=properties.getRepertoireReception();
		this.repertoireChargement=properties.getRepertoireChargement();
		this.repertoireStockage=properties.getRepertoireStockage();
	    this.databaseSchema=properties.getDatabaseArcSchema();
	    this.urlDatabase=properties.getDatabaseArcUrl();
	    this.usernameDatabase=properties.getDatabaseArcUsername();
	    this.passwordDatabase=properties.getDatabaseArcPassword();
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
