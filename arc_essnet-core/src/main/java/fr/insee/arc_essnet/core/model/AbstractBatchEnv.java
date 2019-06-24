package fr.insee.arc_essnet.core.model;

import java.sql.Connection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:fr/insee/config/devarc.properties")
public class AbstractBatchEnv {

    /*
     * Répertoires en entrée
     */
    @Value("fr.insee.arc.repertoire.reception")
    public String repertoireReception;
    @Value("fr.insee.arc.repertoire.chargement")
    public String repertoireChargement;
    @Value("fr.insee.arc.repertoire.stockage")
    public String repertoireStockage;

    /*
     * Schéma de la base de données
     */
    @Value("fr.insee.database.arc.schema")
    public String databaseSchema;
    
    @Value("fr.insee.database.arc.url")
    public String urlDatabase;
    
    @Value("fr.insee.database.arc.username")
    public String usernameDatabase;
    
    @Value("fr.insee.database.arc.password")
    public String passwordDatabase;

    /*
     * Connexion
     */
    protected Connection connexion;
    

}
