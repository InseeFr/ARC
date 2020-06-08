package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiMappingService;
import fr.insee.arc.core.service.engine.ServiceCommunFiltrageMapping;
import fr.insee.arc.core.service.engine.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.engine.mapping.RequeteMapping;
import fr.insee.arc.core.service.engine.mapping.RequeteMappingCalibree;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends ApiMappingService implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ThreadMappingService.class);

    int indice;
    private Thread t;
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
        
        this.tableTempFiltrageOk = temporaryTableName(this.getEnvExecution(), this.getCurrentPhase(), "temp_filtrage_ok", Integer.toString(indice));
        this.tableMappingPilTemp = temporaryTableName(this.getEnvExecution(), this.getCurrentPhase(), "pil_temp", Integer.toString(indice));
        
        this.requeteSQLCalibree = new RequeteMappingCalibree(this.connexion, FormatSQL.TAILLE_MAXIMAL_BLOC_SQL, this.tableMappingPilTemp);
        this.jdrDAO = new JeuDeRegleDao();
        this.setTableJeuDeRegle(anApi.getTableJeuDeRegle());
        this.setTableNorme(anApi.getTableNorme());
        this.setTableOutKo(anApi.getTableOutKo());
        this.tablePil = anApi.getTablePil();
    }

    public void start() {
        LoggerDispatcher.debug("Starting ThreadmappingService", LOGGER);
        if (t == null) {
            t = new Thread(this, indice + "");
            t.start();
        }

    }

    public void run() {
        try {
            this.preparerExecution();
            /*
             * Construire l'ensemble des jeux de règles
             */
            List<JeuDeRegle> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(this.connexion, this.tableTempFiltrageOk, this.getTableJeuDeRegle());

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
                RequeteMapping requeteMapping = new RequeteMapping(this.connexion, this.regleMappingFactory, idFamille, listeJeuxDeRegles.get(i),
                        this.getEnvExecution(), this.tableTempFiltrageOk, this.indice);
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
                UtilitaireDao.get(poolName).executeBlock(this.connexion, requeteMapping.requeteCreationTablesTemporaires());

//            requeteMapping.showAllrubrique();
//
                /*
                 * Multithread - Initialisation
                 */

                // try{
                // initializeMultiThread(2);
                StringBuilder req = new StringBuilder();
//                LoggerDispatcher.info("Connexions OK", LOGGER);
                /*
                 * Exécution de la requête de mapping pour chaque fichier
                 */
//                LoggerDispatcher.info("Exécution du mapping", LOGGER);

                int j = 0;
                    // req.append(this.requeteSQLCalibree.buildMainQuery(requeteMapping.getRequete(listeFichier.get(j)),
                    // Arrays.asList(listeFichier.get(j))));
                    req.append(requeteMapping.getRequete(listeFichier.get(j)));
                    LoggerDispatcher.info("Mapping : " + listeFichier.get(j), LOGGER);

                     UtilitaireDao.get(poolName).executeBlock(this.connexion,"set enable_nestloop=off;"+req.toString()+"set enable_nestloop=on;");
                    req.setLength(0);


                /**
                 * Marquer les OK
                 */
                UtilitaireDao.get(poolName).executeBlock(
                        this.connexion,
                        "UPDATE " + this.tableMappingPilTemp + " SET etape=2, etat_traitement = '{" + TraitementEtat.OK + "}' WHERE etat_traitement='{"
                                + TraitementEtat.ENCOURS + "}' AND id_source = '" + idSource + "' ;");

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
//                requeteMAJFinale.append(this.getMiseANiveauSchemaTable(this.tableTempFiltrageOk, this.getTableOutKo()));
//                requeteMAJFinale.append(requeteInsertMappingKo(this.getTableOutKo()));
                
//                requeteMAJFinale.append(requeteInsertMappingKo(this.getTableOutKo()));

                /*
                 * Transfert de la table mapping_ko temporaire vers la table mapping_ko définitive
                 */
                requeteMAJFinale.append(marquageFinal(this.tablePil, this.tableMappingPilTemp));
                
                UtilitaireDao.get(poolName).executeBlock(this.connexion, requeteMAJFinale);

                /*
                 * DROP des tables utilisées
                 */
                UtilitaireDao.get(poolName).dropTable(this.connexion, requeteMapping.tableauNomsTablesTemporaires());
                UtilitaireDao.get(poolName).dropTable(this.connexion, this.tableMappingPilTemp);
            }
            /*
             * DROP des tables utilisées
             */
            UtilitaireDao.get(poolName).dropTable(this.connexion, this.tableTempFiltrageOk);
        } catch (Exception e) {
            e.printStackTrace();
	    try {
		this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
			"aucuneTableADroper");
	    } catch (SQLException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	    }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
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
        requete = new StringBuilder();
        requete.append(createTablePilotageIdSource(this.tablePilTemp, this.tableMappingPilTemp, this.idSource));
        UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);

        /*
         * Marquer le jeu de règles
         */
        requete = new StringBuilder();
        requete.append(this.marqueJeuDeRegleApplique(this.tableMappingPilTemp));
        UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);
        
        requete = new StringBuilder();
        requete.append(createTableTravailIdSource(this.getTablePrevious(),this.tableTempFiltrageOk, this.idSource));
        UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);

        /*
         * Indexation de la table temporaire
         */
//        requete = new StringBuilder();
//        requete.append("create unique index idx_" + ManipString.substringAfterFirst(this.tableTempFiltrageOk, ".") + " on "
//                + this.tableTempFiltrageOk + "(id_source, id);");
//        requete.append("\n analyze " + this.tableTempFiltrageOk + ";");
//        UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);

    }

    /**
     *
     * @param aJeuDeRegle
     * @return Le bon id_famille
     * @throws SQLException
     */
    private String fetchIdFamille(JeuDeRegle aJeuDeRegle) throws SQLException {
        StringBuilder requete = new StringBuilder("SELECT id_famille FROM " + this.getTableNorme())//
                .append("\n WHERE id_norme    = '" + aJeuDeRegle.getIdNorme() + "'")//
                .append("\n AND periodicite = '" + aJeuDeRegle.getPeriodicite() + "';");
        return UtilitaireDao.get(poolName).getString(this.connexion, requete);
    }


    /**
     * Récupère l'ensemble des colonnes de la table de la phase précédente, et les répartit dans deux containers :<br/>
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
        for (String nomColonne : ServiceCommunFiltrageMapping.calculerListeColonnes(this.connexion, this.tableTempFiltrageOk)) {
            if (nomColonne.startsWith(this.getPrefixidentifiantrubrique())) {
                ensembleIdentifiantRubriqueExistante.add(nomColonne);
            } else {
                ensembleNomRubriqueExistante.add(nomColonne);
            }
        }
        return new RegleMappingFactory(this.connexion, this.getEnvExecution(), ensembleIdentifiantRubriqueExistante, ensembleNomRubriqueExistante);
    }

  
    public Connection getConnexion() {
        return connexion;
    }

    public Thread getT() {
        return t;
    }
}
