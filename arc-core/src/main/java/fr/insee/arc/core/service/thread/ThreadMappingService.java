package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiMappingService;
import fr.insee.arc.core.service.engine.mapping.RequeteMapping;
import fr.insee.arc.core.service.engine.mapping.ServiceMapping;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.ModeRequete;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.Sleep;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends ApiMappingService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ThreadMappingService.class);

    int indice;
    protected String tableTempFiltrageOk;
    protected String tableMappingPilTemp;

    public ThreadMappingService(Connection connexion, int currentIndice, ApiMappingService anApi) {

        this.connexion = connexion;
        this.indice = currentIndice;
        this.idSource = anApi.getTabIdSource().get(ID_SOURCE).get(indice);
        this.setEnvExecution(anApi.getEnvExecution());

        
        this.tablePilTemp = anApi.getTablePilTemp();

        this.setPreviousPhase(anApi.getPreviousPhase());
        this.setCurrentPhase(anApi.getCurrentPhase());

        this.setTablePrevious(anApi.getTablePrevious());

        this.setTabIdSource(anApi.getTabIdSource());

        this.setParamBatch(anApi.getParamBatch());
        
        this.tableTempFiltrageOk = "tableTempFiltrageOk".toLowerCase();
        this.tableMappingPilTemp = "tableMappingPilTemp".toLowerCase();
        
        this.jdrDAO = new JeuDeRegleDao();
        this.setTableJeuDeRegle(anApi.getTableJeuDeRegle());
        this.setTableNorme(anApi.getTableNorme());
        this.setTableOutKo(anApi.getTableOutKo());
        this.tablePil = anApi.getTablePil();
    }

    public void start() {
        StaticLoggerDispatcher.debug("Starting ThreadmappingService", LOGGER);
        if (t == null) {
            t = new Thread(this, indice + "");
            t.start();
        }

    }

    @Override
	public void run() {
        try {
            this.preparerExecution();
            
            executionMapping();

        } catch (Exception e) {
            StaticLoggerDispatcher.error(e, LOGGER);

	    try {
			this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
				"aucuneTableADroper");
		    } catch (SQLException e2) {
	            StaticLoggerDispatcher.error(e, LOGGER);
	
		    }
            Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }

    /**
     * @throws SQLException
     */
    private void preparerExecution() throws SQLException {

        StringBuilder requete = new StringBuilder();

    	/*
         * Insertion dans la table temporaire des fichiers marqués dans la table de pilotage
         */
        requete.append(cleanThread());
        
        requete.append(createTablePilotageIdSource(this.tablePilTemp, this.tableMappingPilTemp, this.idSource));
        /*
         * Marquer le jeu de règles
         */
        requete.append(this.marqueJeuDeRegleApplique(this.tableMappingPilTemp));
        
        requete.append(createTableTravailIdSource(this.getTablePrevious(),this.tableTempFiltrageOk, this.idSource));
        UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);


    }

    
    private void executionMapping() throws Exception
    {
        /*
         * Construire l'ensemble des jeux de règles
         */
        List<JeuDeRegle> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(this.connexion, this.tableTempFiltrageOk, this.getTableJeuDeRegle());

        /*
         * Construction de la factory pour les règles de mapping
         */
        ServiceMapping serviceMapping = new ServiceMapping();
		this.regleMappingFactory = serviceMapping.construireRegleMappingFactory(this.connexion, this.getEnvExecution(), this.tableTempFiltrageOk, getPrefixidentifiantrubrique());
        /*
         * Pour chaque jeu de règles
         */
        for (int i = 0; i < listeJeuxDeRegles.size(); i++) {
            /*
             * Récupération de l'id_famille
             */
            String idFamille = serviceMapping.fetchIdFamille(this.connexion, listeJeuxDeRegles.get(i),	this.getTableNorme());
            /*
             * Instancier une requête de mapping générique pour ce jeu de règles.
             */
            RequeteMapping requeteMapping = new RequeteMapping(this.connexion, this.regleMappingFactory, idFamille, listeJeuxDeRegles.get(i),
                    this.getEnvExecution(), this.tableTempFiltrageOk, this.indice);
            /*
             * Construire la requête de mapping (dérivation des règles)
             */
            requeteMapping.construire();

            /*
             * Récupérer la liste des fichiers concernés
             */
            List<String> listeFichier = new ArrayList<>();
            listeFichier.add(idSource);


            StaticLoggerDispatcher.trace("Mapping : " + listeFichier.get(0), LOGGER);                

            StringBuilder query=new StringBuilder();

            // Créer les tables temporaires métier
            query.append(requeteMapping.requeteCreationTablesTemporaires());

            // calculer la requete du fichier
            query.append(ModeRequete.NESTLOOP_OFF);
            query.append(requeteMapping.getRequete(listeFichier.get(0)));
            query.append(ModeRequete.NESTLOOP_ON);


            /**
             * Marquer les OK
             */
            query.append("UPDATE " + this.tableMappingPilTemp + " SET etape=2, etat_traitement = '{" + TraitementEtat.OK + "}' WHERE etat_traitement='{"
                            + TraitementEtat.ENCOURS + "}' AND id_source = '" + idSource + "' ;");

            /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
             * Opérations de fin d'opération
             */

            /*
             * Transfert des tables métier temporaires vers les tables définitives
             */
            query.append(requeteMapping.requeteTransfertVersTablesMetierDefinitives());

        	// promote the application user account to full right
            query.append(switchToFullRightRole());
            	
            /*
             * Transfert de la table mapping_ko temporaire vers la table mapping_ko définitive
             */
            query.append(marquageFinal(this.tablePil, this.tableMappingPilTemp, this.idSource));
            UtilitaireDao.get(poolName).executeBlock(this.connexion, query);
        }
    }



    @Override
	public Connection getConnexion() {
        return connexion;
    }

    @Override
	public Thread getT() {
        return t;
    }
}
