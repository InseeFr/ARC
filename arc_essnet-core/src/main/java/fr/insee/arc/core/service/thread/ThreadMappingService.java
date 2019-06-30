package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.RuleSets;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.service.ServiceCommunFiltrageMapping;
import fr.insee.arc.core.service.mapping.MappingService;
import fr.insee.arc.core.service.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.mapping.RequeteMapping;
import fr.insee.arc.core.service.mapping.RequeteMappingCalibree;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.SQLExecutor;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends AbstractThreadService {

    private static final Logger LOGGER = Logger.getLogger(ThreadMappingService.class);

    protected String tableTempFiltrageOk;
    private static final String PREFIX_IDENTIFIANT_RUBRIQUE = "i_";

    protected RequeteMappingCalibree requeteSQLCalibree  = new RequeteMappingCalibree(this.connection, FormatSQL.TAILLE_MAXIMAL_BLOC_SQL, this.getTablePilTemp());

    protected JeuDeRegleDao jdrDAO;
    protected RegleMappingFactory regleMappingFactory ;
    

    public ThreadMappingService(int currentIndice, MappingService anApi, Connection zeConnexion) {
	super(currentIndice, anApi, zeConnexion);


	this.tableTempFiltrageOk = temporaryTableName(this.getExecutionEnv(), this.tokenInputPhaseName,
		"temp_filtrage_ok");

	this.requeteSQLCalibree = new RequeteMappingCalibree(this.connection, FormatSQL.TAILLE_MAXIMAL_BLOC_SQL,
		this.getTablePilTempThread());
	this.setTableOutKo(anApi.getTableOutKo());

    }

    @Override
    @SQLExecutor
    public void initialisationTodo() throws Exception {
	   preparerExecution();

    }

    @Override
    @SQLExecutor
    public void process() throws Exception {
	/*
	 * Construire l'ensemble des jeux de règles
	 */
	List<RuleSets> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(this.connection, this.tableTempFiltrageOk,
		this.bddTable.getQualifedName(BddTable.ID_TABLE_RULESETS_BAS));
	

        /*
         * Construction de la factory pour les règles de mapping
         */
        this.regleMappingFactory = this.construireRegleMappingFactory();
        /*
         * Pour chaque jeu de règles
         */
        for (int i = 0; i < listeJeuxDeRegles.size(); i++) {
            /*
             * Récupération de l'id_famille
             */
            String idFamille = this.fetchIdFamille(listeJeuxDeRegles.get(i));

            /*
             * Instancier une requête de mapping générique pour ce jeu de règles.
             */
            RequeteMapping requeteMapping = new RequeteMapping(this.connection, this.regleMappingFactory, idFamille, listeJeuxDeRegles.get(i),
                    this.getExecutionEnv(), this.tableTempFiltrageOk);
            /*
             * Construire la requête de mapping (dérivation des règles)
             */
            requeteMapping.construire();

            /*
             * Récupérer la liste des fichiers concernés
             */

            // List<String> listeFichier = construireListeFichiers(listeJeuxDeRegles.get(i));

            List<String> listeFichier = new ArrayList<String>();
            listeFichier.add(idSource);

            /*
             * Créer les tables temporaires métier
             */
            UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, requeteMapping.requeteCreationTablesTemporaires());

//        requeteMapping.showAllrubrique();
//
            /*
             * Multithread - Initialisation
             */

            // try{
            // initializeMultiThread(2);
            StringBuilder req = new StringBuilder();
//            LoggerDispatcher.info("Connexions OK", LOGGER);
            /*
             * Exécution de la requête de mapping pour chaque fichier
             */
//            LoggerDispatcher.info("Exécution du mapping", LOGGER);

            int j = 0;
                // req.append(this.requeteSQLCalibree.buildMainQuery(requeteMapping.getRequete(listeFichier.get(j)),
                // Arrays.asList(listeFichier.get(j))));
                req.append(requeteMapping.getRequete(listeFichier.get(j)));
                LoggerDispatcher.info("Mapping : " + listeFichier.get(j), LOGGER);

                 UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection,"set enable_nestloop=off;"+req.toString()+"set enable_nestloop=on;");
                req.setLength(0);


            /**
             * Marquer les OK
             */
            UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(
                    this.connection,
                    "UPDATE " + this.getTablePilTempThread() + " SET etape=2, etat_traitement = '{" + TraitementState.OK + "}' WHERE etat_traitement='{"
                            + TraitementState.ENCOURS + "}' AND id_source = '" + idSource + "' ;");

            /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
             * Opérations de fin d'opération
             */

            StringBuilder requeteMAJFinale = new StringBuilder();
            /*
             * Transfert des tables métier temporaires vers les tables définitives
             */
            requeteMAJFinale.append(requeteMapping.requeteTransfertVersTablesMetierDefinitives());

            /*
             * Mettre à jour le dessin de la table mapping_ko Insertion dans la table de mapping_ko des éléments en erreur.
             */


            /*
             * Transfert de la table mapping_ko temporaire vers la table mapping_ko définitive
             */
            requeteMAJFinale.append(marquageFinal(this.getTablePil(), this.getTablePilTempThread()));
            
            UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, requeteMAJFinale);

            /*
             * DROP des tables utilisées
             */
            UtilitaireDao.get(DbConstant.POOL_NAME).dropTable(this.connection, requeteMapping.tableauNomsTablesTemporaires());
        }
	
	
    }

    @Override
    @SQLExecutor
    public void finalizePhase() throws Exception {

	  /*
         * DROP des tables utilisées
         */
        UtilitaireDao.get(DbConstant.POOL_NAME).dropTable(this.connection, this.tableTempFiltrageOk);
        

    }

    /**
     * @throws SQLException
     */
    private void preparerExecution() throws SQLException {

	StringBuilder requete = new StringBuilder();

	/*
	 * Insertion dans la table temporaire des fichiers marqués dans la table de
	 * pilotage
	 */
	requete = new StringBuilder();
	requete.append(getRequestTocreateTablePilotageIdSource(this.getTablePilTemp(), this.getTablePilTempThread(), this.idSource));
	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, requete);

	/*
	 * Marquer le jeu de règles
	 */
	requete = new StringBuilder();
	requete.append(this.marqueJeuDeRegleApplique(this.getTablePilTempThread()));
	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, requete);

	requete = new StringBuilder();
	requete.append(getRequestToCreateWorkingTable(this.getTablePrevious(), this.tableTempFiltrageOk, this.idSource));
	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, requete);

	/*
	 * Indexation de la table temporaire
	 */
	// requete = new StringBuilder();
	// requete.append("create unique index idx_" +
	// ManipString.substringAfterFirst(this.tableTempFiltrageOk, ".") + " on "
	// + this.tableTempFiltrageOk + "(id_source, id);");
	// requete.append("\n analyze " + this.tableTempFiltrageOk + ";");
	// UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);

    }

    /**
     *
     * @param aJeuDeRegle
     * @return Le bon id_famille
     * @throws SQLException
     */
    private String fetchIdFamille(RuleSets aJeuDeRegle) throws SQLException {
	StringBuilder requete = new StringBuilder("SELECT id_famille FROM " + this.bddTable.getQualifedName(BddTable.ID_TABLE_NORME_SPECIFIC))//
		.append("\n WHERE id_norme    = '" + aJeuDeRegle.getIdNorme() + "'")//
		.append("\n AND periodicite = '" + aJeuDeRegle.getPeriodicite() + "';");
	return UtilitaireDao.get(DbConstant.POOL_NAME).getString(this.connection, requete);
    }

    /**
     * Récupère l'ensemble des colonnes de la table de la phase précédente, et les
     * répartit dans deux containers :<br/>
     * 1. Un pour les identifiants de rubriques<br/>
     * 2. Un pour les autres types de colonnes<br/>
     *
     * @return
     *
     * @return
     *
     * @throws SQLException
     */
    public RegleMappingFactory construireRegleMappingFactory() throws SQLException {
	Set<String> ensembleIdentifiantRubriqueExistante = new HashSet<>();
	Set<String> ensembleNomRubriqueExistante = new HashSet<>();
	for (String nomColonne : ServiceCommunFiltrageMapping.calculerListeColonnes(this.connection,
		this.tableTempFiltrageOk)) {
	    if (nomColonne.startsWith(PREFIX_IDENTIFIANT_RUBRIQUE)) {
		ensembleIdentifiantRubriqueExistante.add(nomColonne);
	    } else {
		ensembleNomRubriqueExistante.add(nomColonne);
	    }
	}
	return new RegleMappingFactory(this.connection, this.getExecutionEnv(), ensembleIdentifiantRubriqueExistante,
		ensembleNomRubriqueExistante);
    }

    

    @Override
    public boolean initialize() {
	return false;
    }




}
