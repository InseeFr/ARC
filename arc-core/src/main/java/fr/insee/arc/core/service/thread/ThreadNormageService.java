package fr.insee.arc.core.service.thread;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.api.ApiNormageService;
import fr.insee.arc.core.service.api.query.ServiceHashFileName;
import fr.insee.arc.core.service.api.query.ServicePilotageOperation;
import fr.insee.arc.core.service.api.query.ServiceRules;
import fr.insee.arc.core.service.api.query.ServiceTableNaming;
import fr.insee.arc.core.service.api.query.ServiceTableOperation;
import fr.insee.arc.core.service.engine.normage.NormageEngine;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Sleep;


/**
 * ThreadNormageService
 *
 *      1- créer la table des données à traiter dans le module</br>
 *      2- calcul de la norme, validité, periodicité sur chaque ligne de la table de donnée</br>
 *      3- déterminer pour chaque fichier si le normage s'est bien déroulé et marquer sa norme, sa validité et sa périodicité</br>
 *      4- créer les tables OK et KO; marquer les info de normage(norme, validité, périodicité) sur chaque ligne de donnée</br>
 *      5- transformation de table de donnée; mise à plat du fichier; suppression et relation</br>
 *      6- mettre à jour le nombre d'enregistrement par fichier après sa transformation</br>
 *      7- sortir les données du module vers l'application</br>
 *
 * @author Manuel SOULIER
 *
 */
public class ThreadNormageService extends ApiNormageService implements Runnable, ArcThread<ApiNormageService> {

    private static final Logger LOGGER = LogManager.getLogger(ThreadNormageService.class);

    private Thread t;
    
    private int indice ;
    
    private String tableNormageDataTemp;
    private String tableNormagePilTemp;

    private String tableNormageOKTemp;
    private String tableNormageKOTemp;
    
    private String tableNormageOK;
    private String tableNormageKO;

    private String structure;

	private ArcThreadGenericDao arcThreadGenericDao;

    @Override
    public void configThread(ScalableConnection connexion, int currentIndice, ApiNormageService theApi) {
        
        this.indice = currentIndice;
        this.idSource = theApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(indice);
        this.connexion = connexion;

        // tables du thread
        
        this.tableNormageDataTemp = FormatSQL.temporaryTableName("normage_data_temp");
        this.tableNormagePilTemp = FormatSQL.temporaryTableName("normage_pil_Temp");
        
        this.tableNormageOKTemp = FormatSQL.temporaryTableName("ok_Temp");
        this.tableNormageKOTemp = FormatSQL.temporaryTableName("ko_Temp");       
        
        this.tableNormageOK = ServiceTableNaming.globalTableName(theApi.getEnvExecution(), theApi.getCurrentPhase(), TraitementEtat.OK.toString());
        this.tableNormageKO = ServiceTableNaming.globalTableName(theApi.getEnvExecution(), theApi.getCurrentPhase(), TraitementEtat.KO.toString());

        // tables héritées
        this.setTableNormageRegle(theApi.getTableNormageRegle());
        this.setTableControleRegle(theApi.getTableControleRegle());
        this.setTableMappingRegle(theApi.getTableMappingRegle());
        
        this.setTablePil(theApi.getTablePil());
        this.setTablePilTemp(theApi.getTablePilTemp());
        this.setPreviousPhase(theApi.getPreviousPhase());
        this.setCurrentPhase(theApi.getCurrentPhase());
        this.setNbEnr(theApi.getNbEnr());
        this.setTablePrevious(theApi.getTablePrevious());
        this.setTabIdSource(theApi.getTabIdSource());
        this.setTableNorme(theApi.getTableNorme());
        this.setTableNormageRegle(theApi.getTableNormageRegle());
        this.setEnvExecution(theApi.getEnvExecution());
        this.setParamBatch(theApi.getParamBatch());
        
		// arc thread dao
		arcThreadGenericDao=new ArcThreadGenericDao(connexion, tablePil, tablePilTemp, tableNormagePilTemp, tablePrevious, paramBatch, idSource);

    }

    public void start() {
		StaticLoggerDispatcher.debug("Starting ThreadNormageService", LOGGER);
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try {

            // créer la table des données à traiter dans le module
            creerTableTravail();

            // transformation de table de donnée; mise à plat du fichier; suppression et relation
            jointureBlocXML();
            
            // sortir les données du module vers l'application
            insertionFinale();

        } catch (ArcException e) {
            StaticLoggerDispatcher.error(e, LOGGER);
	    try {
			this.repriseSurErreur(this.connexion.getExecutorConnection(), this.getCurrentPhase(), this.tablePil, this.idSource, e,
				"aucuneTableADroper");
		    } catch (ArcException e2) {
	            StaticLoggerDispatcher.error(e2, LOGGER);
		    }
            Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }


    /**
     * Créer la table de travail du normage Contient les donnée d'un seul id source. Cela est du au fait que le type composite varie d'un id
     * source à l'autre,
     * 
     * @throws ArcException
     */
    private void creerTableTravail() throws ArcException {
        StaticLoggerDispatcher.info("Créer les tables images", LOGGER);
    	ArcPreparedStatementBuilder query= arcThreadGenericDao.preparationDefaultDao();

        // Créer la table image de la phase précédente (ajouter les colonnes qu'il faut)
    	// création des tables temporaires de données
        query.append(ServiceTableOperation.createTableTravailIdSource(this.getTablePrevious(),this.tableNormageDataTemp, this.idSource));
       
        //On indique que le normage s'est bien passé par défaut
        query.append("\n UPDATE "+this.tableNormagePilTemp);
        query.append("\n SET etat_traitement = '{"+TraitementEtat.OK+"}'");
        query.append("\n , phase_traitement = '"+this.currentPhase+"'");
        query.append("\n WHERE "+ColumnEnum.ID_SOURCE.getColumnName()+"='"+this.idSource+"';");
        
        query.append(ServiceTableOperation.creationTableResultat(this.tableNormageDataTemp, this.tableNormageKOTemp));
                
        UtilitaireDao.get(poolName).executeBlock(this.getConnexion().getExecutorConnection(), query.getQueryWithParameters());

    }

    /**
     * Réaliser la jointure entre les blocs XML pour mettre les fichier à plat
     * Pour chaque fichier, on retravaille la requete de jointure obtenue en phase de chargement :
     * 1- en supprimant les blocs définis "à supprimer" par les regles utilisateurs du normage
     * 2- en ajoutant des conditions de jointures relationnelles entre 2 rubriques défini par les règle utilisateur de type "relation" dans les regles de normage
     *
     * Fait sur une maintenance urgente après réception des fichiers lot2 en moins de 2j ...
     * La méthode devrait etre refactor (pour séparer "deletion" et "relation")
     * La réécriture de la requete selon les règles utilisateurs devrait être moins adhérente à la structure de la requete issu du chargement
     * (trop dépendant des mot clés ou saut de ligne pour l'instant)
     * @throws ArcException
     *
     */
    private void jointureBlocXML() throws ArcException {

        StaticLoggerDispatcher.info("jointureBlocXML()", LOGGER);

        // on parcours les fichiers pour effectuer la requete de jointure avec les regles de normage
        // c'est une des requete la plus couteuse de l'application (reconstitution de la structure plate à partir du modele xml)
        // pour chaque fichier, on va executer sa requete


        		// récupérer les caractéristiques du fichier
			    HashMap<String, ArrayList<String>> pil = ServiceRules.getBean(this.connexion.getExecutorConnection(),ServiceRules.getNormeAttributes(this.idSource, tableNormagePilTemp));

			    // récupéreration des règles relative au fichier pour la phase courante
			    HashMap<String,ArrayList<String>> regle = ServiceRules.getBean(this.connexion.getExecutorConnection(),ServiceRules.getRegles(this.tableNormageRegle, this.tableNormagePilTemp));
			    
			    
		        // récupéreration des rubriques utilisées dans règles relative au fichier pour l'ensemble des phases
		        
			    HashMap<String,ArrayList<String>> rubriqueUtiliseeDansRegles=null;
			    
			    if (paramBatch!=null)
			    {
				    String tableTmpRubriqueDansregles="TMP_RUBRIQUE_DANS_REGLES";
			        UtilitaireDao.get("arc").executeImmediate(
			        		this.connexion.getExecutorConnection(),
			        		"\n DROP TABLE IF EXISTS "+tableTmpRubriqueDansregles+"; "
			        		+ "\n CREATE TEMPORARY TABLE "+tableTmpRubriqueDansregles+" AS "
			        		+ ServiceRules.getAllRubriquesInRegles(this.tableNormagePilTemp, this.tableNormageRegle, this.tableControleRegle, this.tableMappingRegle)
			        		);
				    
				    rubriqueUtiliseeDansRegles = ServiceRules.getBean(this.connexion.getExecutorConnection(),ServiceRules.getRegles(tableTmpRubriqueDansregles, this.tableNormagePilTemp));
			    }
		    
			    NormageEngine n=new NormageEngine(this.connexion.getExecutorConnection() , pil, regle, rubriqueUtiliseeDansRegles, this.tableNormageDataTemp, this.tableNormageOKTemp, this.paramBatch);
			    n.execute();
			    
			    this.structure=n.structure;

    }
    
    
 

	/**
	 * Remplace les UNION ALL par des inserts
	 * @param jointure
	 * @return
	 */

    /**
     * On sort les données des tables temporaires du module vers : - les tables définitives du normage (normage_ok et normage_ko) de
     * l'application - la vraie table de pilotage - la table buffer
     *
     * IMPORTANT : les ajouts ou mise à jours de données sur les tables de l'application doivent avoir lieu dans un même bloc de transaction
     * (ACID)
     * 
     * @throws ArcException
     *
     */
    private void insertionFinale() throws ArcException {
    	
    	ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
    	
    	// update the number of record ans structure in the pilotage table
    	query.append(ServicePilotageOperation.updateNbEnr(this.tableNormagePilTemp, this.tableNormageOKTemp, this.structure));
    
    	// promote the application user account to full right
    	query.append(switchToFullRightRole());
    	
    	String tableIdSourceOK=ServiceHashFileName.tableOfIdSource(this.tableNormageOK ,this.idSource);
    	query.append(ServiceTableOperation.createTableInherit(this.tableNormageOKTemp, tableIdSourceOK));
        String tableIdSourceKO=ServiceHashFileName.tableOfIdSource(this.tableNormageKO ,this.idSource);
        query.append(ServiceTableOperation.createTableInherit(this.tableNormageKOTemp, tableIdSourceKO));
		
        // mark file as done into global pilotage table
        arcThreadGenericDao.marquageFinalDefaultDao(query);
        
    }

    public ScalableConnection getConnexion() {
        return connexion;
    }

    public void setConnexion(ScalableConnection connexion) {
        this.connexion = connexion;
    }

    public Thread getT() {
        return t;
    }
    

}


