package fr.insee.arc_essnet.core.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import fr.insee.config.InseeConfig;

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

        this.repertoireReception = InseeConfig.getConfig().getString("fr.insee.arc.repertoire.reception");
        this.repertoireChargement = InseeConfig.getConfig().getString("fr.insee.arc.repertoire.chargement");
        this.repertoireStockage = InseeConfig.getConfig().getString("fr.insee.arc.repertoire.stockage");

        try {
            Class.forName("org.postgresql.Driver");
            this.connexion = DriverManager.getConnection(InseeConfig.getConfig().getString("fr.insee.database.arc.url"), InseeConfig.getConfig()
                    .getString("fr.insee.database.arc.username"), UtilitaireDao.getProtectedConfig("fr.insee.database.arc.password"));
        } catch (SQLException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "BatchEnv()", LOGGER, ex);
        } catch (ClassNotFoundException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "BatchEnv()", LOGGER, ex);
        }

        this.databaseSchema = InseeConfig.getConfig().getString("fr.insee.database.arc.schema");
    }

}
