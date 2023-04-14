package fr.insee.arc.core.service.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.rulesobjects.JeuDeRegleDao;
import fr.insee.arc.core.service.api.ApiMappingService;
import fr.insee.arc.core.service.api.query.ServiceTableOperation;
import fr.insee.arc.core.service.engine.mapping.RequeteMapping;
import fr.insee.arc.core.service.engine.mapping.ServiceMapping;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.ModeRequete;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.Sleep;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends ApiMappingService implements Runnable, ArcThread<ApiMappingService> {

    private static final Logger LOGGER = LogManager.getLogger(ThreadMappingService.class);

    private Thread t;
    
    private int indice;
    private String tableTempFiltrageOk;
    private String tableMappingPilTemp;

	private ArcThreadGenericDao arcThreadGenericDao;

    @Override
    public void configThread(ScalableConnection connexion, int currentIndice, ApiMappingService anApi) {

        this.connexion = connexion;
        this.indice = currentIndice;
        this.idSource = anApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(indice);
        this.setEnvExecution(anApi.getEnvExecution());

        
        this.tablePilTemp = anApi.getTablePilTemp();

        this.setPreviousPhase(anApi.getPreviousPhase());
        this.setCurrentPhase(anApi.getCurrentPhase());

        this.setTablePrevious(anApi.getTablePrevious());

        this.setTabIdSource(anApi.getTabIdSource());

        this.setParamBatch(anApi.getParamBatch());
        
        this.tableTempFiltrageOk = "tableTempFiltrageOk".toLowerCase();
        this.tableMappingPilTemp = "tableMappingPilTemp".toLowerCase();
        
        this.setTableJeuDeRegle(anApi.getTableJeuDeRegle());
        this.setTableNorme(anApi.getTableNorme());
        this.setTableOutKo(anApi.getTableOutKo());
        this.tablePil = anApi.getTablePil();
        
    	// thread generic dao
    	arcThreadGenericDao=new ArcThreadGenericDao(connexion, tablePil, tablePilTemp, tableMappingPilTemp, tablePrevious, paramBatch, idSource);
    	
    }

    public void start() {
		StaticLoggerDispatcher.debug("Starting ThreadMappingService", LOGGER);
    	t = new Thread(this);
        t.start();
    }

    @Override
	public void run() {
        try {
            this.preparerExecution();
            
            executionMapping();

        } catch (ArcException e) {
            StaticLoggerDispatcher.error(e, LOGGER);

	    try {
			this.repriseSurErreur(this.connexion.getExecutorConnection(), this.getCurrentPhase(), this.tablePil, this.idSource, e,
				"aucuneTableADroper");
		    } catch (ArcException e2) {
	            StaticLoggerDispatcher.error(e, LOGGER);
	
		    }
            Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }

    /**
     * @throws ArcException
     */
    private void preparerExecution() throws ArcException {

    	ArcPreparedStatementBuilder query= arcThreadGenericDao.preparationDefaultDao();
        
        /*
         * Marquer le jeu de règles
         */
    	query.append(this.marqueJeuDeRegleApplique(this.tableMappingPilTemp));
        
        query.append(ServiceTableOperation.createTableTravailIdSource(this.getTablePrevious(),this.tableTempFiltrageOk, this.idSource));
        UtilitaireDao.get(poolName).executeBlock(this.connexion.getExecutorConnection(), query.getQueryWithParameters());

    }

    
    private void executionMapping() throws ArcException
    {
        /*
         * Construire l'ensemble des jeux de règles
         */
        List<JeuDeRegle> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(this.connexion.getExecutorConnection(), this.tableTempFiltrageOk, this.getTableJeuDeRegle());

        /*
         * Construction de la factory pour les règles de mapping
         */
        ServiceMapping serviceMapping = new ServiceMapping();
		this.regleMappingFactory = serviceMapping.construireRegleMappingFactory(this.connexion.getExecutorConnection(), this.getEnvExecution(), this.tableTempFiltrageOk, getPrefixidentifiantrubrique());
        /*
         * Pour chaque jeu de règles
         */
        for (int i = 0; i < listeJeuxDeRegles.size(); i++) {
            /*
             * Récupération de l'id_famille
             */
            String idFamille = serviceMapping.fetchIdFamille(this.connexion.getExecutorConnection(), listeJeuxDeRegles.get(i),	this.getTableNorme());
            /*
             * Instancier une requête de mapping générique pour ce jeu de règles.
             */
            RequeteMapping requeteMapping = new RequeteMapping(this.connexion.getExecutorConnection(), this.regleMappingFactory, idFamille, listeJeuxDeRegles.get(i),
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

            ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();

            // Créer les tables temporaires métier
            query.append(requeteMapping.requeteCreationTablesTemporaires());

            // calculer la requete du fichier
            query.append(ModeRequete.NESTLOOP_OFF.toString());
            query.append(requeteMapping.getRequete(listeFichier.get(0)));
            query.append(ModeRequete.NESTLOOP_ON.toString());


            /**
             * Marquer les OK
             */
            query.append("UPDATE " + this.tableMappingPilTemp + " SET etape=2, etat_traitement = '{" + TraitementEtat.OK + "}' WHERE etat_traitement='{"
                            + TraitementEtat.ENCOURS + "}' AND "+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ;");

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
