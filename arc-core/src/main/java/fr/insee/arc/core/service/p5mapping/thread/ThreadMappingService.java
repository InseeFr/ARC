package fr.insee.arc.core.service.p5mapping.thread;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.dao.GenericQueryDao;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.global.dao.ThreadOperations;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.thread.IThread;
import fr.insee.arc.core.service.p5mapping.ApiMappingService;
import fr.insee.arc.core.service.p5mapping.engine.RequeteMapping;
import fr.insee.arc.core.service.p5mapping.engine.ServiceMapping;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.Sleep;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends ApiMappingService implements Runnable, IThread<ApiMappingService> {

    private static final Logger LOGGER = LogManager.getLogger(ThreadMappingService.class);

    private Thread t;
    
    private int indice;
    private String tableTempControleOk;
    private String tableMappingPilTemp;

	private ThreadOperations arcThreadGenericDao;
	
	private GenericQueryDao genericExecutorDao;

    @Override
    public void configThread(ScalableConnection connexion, int currentIndice, ApiMappingService anApi) {

        this.connexion = connexion;
        this.indice = currentIndice;
        this.idSource = anApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(indice);
        this.envExecution = anApi.getEnvExecution();
        this.tablePilTemp = anApi.getTablePilTemp();
        this.currentPhase = anApi.getCurrentPhase();
        this.tablePrevious = anApi.getTablePrevious();
        this.tabIdSource = anApi.getTabIdSource();
        this.paramBatch = anApi.getParamBatch();
        
        this.tableTempControleOk = "tableTempControleOk".toLowerCase();
        this.tableMappingPilTemp = "tableMappingPilTemp".toLowerCase();
        
        this.tablePil = anApi.getTablePil();
        
    	// thread generic dao
    	arcThreadGenericDao=new ThreadOperations(connexion, tablePil, tablePilTemp, tableMappingPilTemp, tablePrevious, paramBatch, idSource);
    	genericExecutorDao = new GenericQueryDao(this.connexion.getExecutorConnection());
    	
    }

    public void start() {
		StaticLoggerDispatcher.debug(LOGGER, "Starting ThreadMappingService");
    	t = new Thread(this);
        t.start();
    }

    @Override
	public void run() {
        try {
            this.preparerExecution();
            
            executionMapping();

        } catch (ArcException e) {
            StaticLoggerDispatcher.error(LOGGER, e);

	    try {
				PilotageOperations.traitementSurErreur(this.connexion.getCoordinatorConnection(), this.getCurrentPhase(), this.tablePil, this.idSource, e);
		    } catch (ArcException e2) {
	            StaticLoggerDispatcher.error(LOGGER, e);
	
		    }
            Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }

    /**
     * @throws ArcException
     */
    private void preparerExecution() throws ArcException {
    	genericExecutorDao.initialize();
    	genericExecutorDao.addOperation(this.arcThreadGenericDao.preparationDefaultDao());
    	genericExecutorDao.addOperation(RulesOperations.marqueJeuDeRegleApplique(this.getCurrentPhase(), this.envExecution, this.tableMappingPilTemp));
    	genericExecutorDao.addOperation(TableOperations.createTableTravailIdSource(this.getTablePrevious(),this.tableTempControleOk, this.idSource));
    	genericExecutorDao.executeAsTransaction();
    }

    
    private void executionMapping() throws ArcException
    {
        /*
         * Construire l'ensemble des jeux de règles
         */
        List<JeuDeRegle> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(this.connexion.getExecutorConnection(), this.getEnvExecution(), this.tableTempControleOk);

        /*
         * Construction de la factory pour les règles de mapping
         */
        ServiceMapping serviceMapping = new ServiceMapping();
		this.regleMappingFactory = serviceMapping.construireRegleMappingFactory(this.connexion.getExecutorConnection(), this.getEnvExecution(), this.tableTempControleOk, getPrefixidentifiantrubrique());
        /*
         * Pour chaque jeu de règles
         */
        for (int i = 0; i < listeJeuxDeRegles.size(); i++) {
            /*
             * Récupération de l'id_famille
             */
            String idFamille = serviceMapping.fetchIdFamille(this.connexion.getExecutorConnection(), listeJeuxDeRegles.get(i), this.getEnvExecution());
            /*
             * Instancier une requête de mapping générique pour ce jeu de règles.
             */
            RequeteMapping requeteMapping = new RequeteMapping(this.connexion.getExecutorConnection(), this.regleMappingFactory, idFamille, listeJeuxDeRegles.get(i),
                    this.getEnvExecution(), this.tableTempControleOk, this.indice);
            /*
             * Construire la requête de mapping (dérivation des règles)
             */
            requeteMapping.construire();

            
            ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
            // Créer les tables temporaires métier
            query.append(requeteMapping.requeteCreationTablesTemporaires());
            // calculer la requete du fichier
            query.append(requeteMapping.getRequete(idSource));
            

            /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
             * Opérations de fin d'opération
             */

            /*
             * Transfert des tables métier temporaires vers les tables définitives
             */
            query.append(requeteMapping.requeteTransfertVersTablesMetierDefinitives());

        	query.append(PilotageOperations.queryUpdatePilotageMapping(this.tableMappingPilTemp, this.idSource));
            
        	// promote the application user account to full right
            query.append(DatabaseConnexionConfiguration.switchToFullRightRole());
            	
            /*
             * Transfert de la table mapping_ko temporaire vers la table mapping_ko définitive
             */
            
            arcThreadGenericDao.marquageFinalDefaultDao(query);

        }
    }


    @Override
	public ScalableConnection getConnexion() {
        return connexion;
    }

    @Override
	public Thread getT() {
        return t;
    }
}
