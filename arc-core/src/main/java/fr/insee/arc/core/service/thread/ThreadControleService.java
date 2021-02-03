package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.ApiControleService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.engine.controle.ServiceJeuDeRegle;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Comme pour le normage et le filtrage, on parallélise en controlant chaque fichier dans des threads séparés.
 * @author S4LWO8
 *
 */
public class ThreadControleService extends ApiControleService implements Runnable {



    private static final Logger LOGGER = LogManager.getLogger(ThreadControleService.class);

    private int indice;

    public String tableControleDataTemp;
    protected String tableControlePilTemp;
    public String tableTempControleFoo;
    protected String tableOutOkTemp="tableOutOkTemp";
    protected String tableOutKoTemp="tableOutKoTemp";
    String tableOutOk;
    String tableOutKo;

    public ServiceJeuDeRegle sjdr;

    public JeuDeRegle jdr;
    
    public String structure;

    public ThreadControleService(Connection connexion, int currentIndice, ApiControleService theApi) {

        this.indice = currentIndice;
        this.setEnvExecution(theApi.getEnvExecution());
        this.idSource = theApi.getTabIdSource().get(ID_SOURCE).get(indice);
        this.connexion = connexion;
        try {
            this.connexion.setClientInfo("ApplicationName", "Controle fichier "+idSource);
        } catch (SQLClientInfoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.setTablePil(theApi.getTablePil());
        this.tablePilTemp = theApi.getTablePilTemp();

        this.setPreviousPhase(theApi.getPreviousPhase());
        this.setCurrentPhase(theApi.getCurrentPhase());

        this.setNbEnr(theApi.getNbEnr());

        this.setTablePrevious(theApi.getTablePrevious());
        this.setTabIdSource(theApi.getTabIdSource());

        this.setTableNorme(theApi.getTableNorme());
        this.setTableNormageRegle(theApi.getTableNormageRegle());

        this.setParamBatch(theApi.getParamBatch());

        this.setTableJeuDeRegle(theApi.getTableJeuDeRegle());
        this.setTableControleRegle(theApi.getTableControleRegle());

        
        this.sjdr = new ServiceJeuDeRegle(theApi.getTableControleRegle());
        this.jdr =new JeuDeRegle();

        
        this.setTableSeuil(theApi.getTableSeuil());

        // Nom des tables temporaires      
        this.tableControleDataTemp = FormatSQL.temporaryTableName("controle_data_temp");
        this.tableControlePilTemp= FormatSQL.temporaryTableName("controle_pil_temp");
        this.tableTempControleFoo = FormatSQL.temporaryTableName("controle_foo_temp");
        
        // tables finales
        this.tableOutOk = dbEnv(this.getEnvExecution()) + this.getCurrentPhase() + "_" + TraitementEtat.OK;
        this.tableOutKo = dbEnv(this.getEnvExecution()) + this.getCurrentPhase() + "_" + TraitementEtat.KO;

    }

    @Override
    public void run() {
        try {
            
            preparation();

            execute();          

            calculSeuilControle();
            
            insertionFinale();
            
        } catch (Exception e) {
			StaticLoggerDispatcher.error(e,LOGGER);
		    try {
				this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
					"aucuneTableADroper");
			    } catch (SQLException e2) {
					StaticLoggerDispatcher.error(e2,LOGGER);
			    }
		    Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }

    public void start() {
        StaticLoggerDispatcher.debug("Starting ThreadContrôleService", LOGGER);
        if (t == null) {
            t = new Thread(this, indice + "");
            t.start();
        }
    }

    /**
     * Préparation des données et implémentation des jeux de règles utiles
     *
     * @param this.connexion
     *
     * @param tableIn
     *            la table issue du chargement-normage
     *
     * @param env
     *            l'environnement d'execution
     * @param tableControle
     *            la table temporaire à controler
     * @param tablePilTemp
     *            la table temporaire listant les fichiers en cours de traitement
     * @param tableJeuDeRegle
     *            la table des jeux de règles
     * @param tableRegleC
     *            la table des règles de controles
     * @throws SQLException
     */
    public void preparation() throws SQLException {
        StaticLoggerDispatcher.info("** preparation **", LOGGER);

        StringBuilder blocPrep = new StringBuilder();

        // Marquage du jeux de règles appliqué
        StaticLoggerDispatcher.info("Récupération des rubrique de la table ", LOGGER);

        // fabrication de la table de pilotage controle lié au thread
        blocPrep.append("DISCARD TEMP;");
        blocPrep.append(createTablePilotageIdSource(this.tablePilTemp, this.tableControlePilTemp, this.idSource));
        blocPrep.append(resetTablePilotage(this.tableControlePilTemp));

        // Marquage du jeux de règles appliqué
        StaticLoggerDispatcher.info("Marquage du jeux de règles appliqué ", LOGGER);
        blocPrep.append(marqueJeuDeRegleApplique(this.tableControlePilTemp));
        
        // Fabrication de la table de controle temporaire
        StaticLoggerDispatcher.info("Fabrication de la table de controle temporaire ", LOGGER);
        blocPrep.append(createTableTravailIdSource(this.getTablePrevious(),this.tableControleDataTemp, this.idSource, "'0'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules"));

        UtilitaireDao.get("arc").executeBlock(this.connexion, blocPrep);

        // Récupération des Jeux de règles associés
        this.sjdr.fillRegleControle(this.connexion, jdr, this.getTableControleRegle(), this.tableControleDataTemp);
        this.structure=UtilitaireDao.get("arc").getString(this.connexion, new PreparedStatementBuilder("SELECT jointure FROM "+this.tableControlePilTemp));
    }

    
    protected String resetTablePilotage(String tableControlePilTemp) {
        StringBuilder requete = new StringBuilder();
        requete.append("\n UPDATE " + tableControlePilTemp + " set etat_traitement=NULL ");
        requete.append(";");
        return requete.toString();
    }

    
    
    /**
     * Méthode pour controler une table
     *
     * @param connexion
     *
     * @param tableControle
     *            la table à controler
     *
     * @throws SQLException
     */
    public void execute() throws Exception {
        StaticLoggerDispatcher.info("** execute CONTROLE sur la table : " + this.tableControleDataTemp + " **", LOGGER);

        this.sjdr.executeJeuDeRegle(this.connexion, jdr, this.tableControleDataTemp, this.structure);

    }

    /**
     * Méthode pour marquer à controle='1' les enregistrements de la table controlée
     *
     * @param tableControle
     * @return
     */
    public String marqueEnregistrementSansRegle(String tableControle) {
        StringBuilder requete = new StringBuilder();
        requete.append("UPDATE " + tableControle + " a ");
        requete.append("\n SET controle='1' ");
        requete.append("\n WHERE EXISTS(    SELECT 1 ");
        requete.append("\n      FROM " + this.tableControlePilTemp + " b ");
        requete.append("\n      WHERE phase_traitement='CONTROLE' ");
        requete.append("\n          AND 'KO'=ANY(etat_traitement) ");
        requete.append("\n          AND a.id_source=b.id_source); ");
        return requete.toString();
    }

    /**
     * Méthode pour marquer à KO les id_source de pilotage_fichier qui n'ont pas de règle associée
     *
     * @return
     */
    private String marqueFichierSansRegle() {
        StringBuilder requete = new StringBuilder();
        requete.append("\n WITH ");
        requete.append("prep AS (   SELECT id_norme, periodicite, validite ");
        requete.append("\n          FROM " + this.tableControlePilTemp + " ");
        requete.append("\n          WHERE phase_traitement='" + TraitementPhase.CONTROLE + "') ");
        // selection des jeux de règles applicables à la table
        requete.append("\n ,jdr AS (    SELECT DISTINCT a.id_norme, a.periodicite, a.validite_inf, a.validite_sup, a.version ");
        requete.append("\n      FROM " + this.getTableJeuDeRegle() + " a ");
        requete.append("\n          INNER JOIN prep ON a.id_norme=prep.id_norme AND a.periodicite = prep.periodicite ");
        requete.append("\n      WHERE a.id_norme=prep.id_norme ");
        requete.append("\n          AND a.periodicite = prep.periodicite ");
        requete.append("\n          AND a.validite_inf<=prep.validite::date ");
        requete.append("\n          AND a.validite_sup>=prep.validite::date) ");
        // Comptage du nombre de règles associées à chaque JdR
        requete.append("\n ,nb AS ( SELECT jdr.id_norme, jdr.periodicite, jdr.validite_inf, jdr.validite_sup, jdr.version, count(a.id_regle) as nb_regle ");
        requete.append("\n  FROM " + this.getTableControleRegle() + " a ");
        requete.append("\n  RIGHT JOIN jdr ON a.id_norme=jdr.id_norme AND a.periodicite=jdr.periodicite AND a.validite_inf=jdr.validite_inf AND a.validite_sup=jdr.validite_sup AND a.version=jdr.version ");
        requete.append("\n  GROUP BY jdr.id_norme, jdr.periodicite, jdr.validite_inf, jdr.validite_sup, jdr.version), ");
        requete.append("\n exclude AS (SELECT DISTINCT a.id_source ");
        requete.append("\n          FROM " + this.tableControleDataTemp + " a ");
        requete.append("\n          LEFT JOIN nb ON a.id_norme=nb.id_norme AND a.periodicite = nb.periodicite AND nb.validite_inf<=a.validite::date AND nb.validite_sup>=a.validite::date ");
        requete.append("\n          WHERE nb_regle IS NULL OR nb_regle = 0) ");
        requete.append("\n UPDATE " + this.tableControlePilTemp + " a \n \t SET etat_traitement=array['KO'],rapport='"
                + TraitementRapport.TOUTE_PHASE_AUCUNE_REGLE
                + "' \n \t WHERE phase_traitement='CONTROLE' AND EXISTS (SELECT 1 FROM exclude WHERE a.id_source=exclude.id_source); ");
        return requete.toString();
    }


    /**
     * Marquage d'un état pour un ensemble de fichier (on les prends tous pas de limitation) Méthode calibré pour notre table de pilotage
     * temporaire pas de vérification sur la phase car la table de pilotage temporaire ne contient que des CONTROLE de même, la table de
     * pilotage temporaire n'a pas d'état ENCOURS (il a été remis à NULL précédemment)
     *
     * @param tableControlePilTemp
     * @param etatNouveau
     * @param TableOut
     * @param
     * @return
     */
    public String passageEtat(String tablePilTemp, String etatNouveau, String TableOut) {
        StringBuilder requete = new StringBuilder();
        requete.append(" UPDATE " + tableControlePilTemp + " b ");
        requete.append(" SET  etat_traitement=etat_traitement||'{" + etatNouveau + "}' ");
        // requete.append(", date_traitement='"+ formatter.format(date) + "'::date");
        requete.append(" WHERE exists (select 1 from " + TableOut + " a where a.id_source=b.id_source); ");
        return requete.toString();
    }

    /**
     * Méthode à passer après les controles
     *
     * @param connexion
     *
     * @param tableIn
     *            la table temporaire avec les marquage du controle
     * @param tableOutOk
     *            la table permanente sur laquelle on ajoute les bons enregistrements de tableIn
     * @param tableOutKo
     *            la table permanente sur laquelle on ajoute les mauvais enregistrements de tableIn
     * @param tablePil
     *            la table de pilotage des fichiers
     * @param tableSeuil
     *            la table des seuils
     * @throws SQLException
     */
    public void calculSeuilControle() throws Exception {
        StaticLoggerDispatcher.info("finControle", LOGGER);


        StringBuilder blocFin = new StringBuilder();
        // Creation des tables temporaires ok et ko
        StaticLoggerDispatcher.info("Creation des tables temporaires ok et ko", LOGGER);
        blocFin.append(FormatSQL.dropTable(tableOutOkTemp).toString());
        blocFin.append(FormatSQL.dropTable(tableOutKoTemp).toString());

        // Execution à mi parcours du bloc de requete afin que les tables tempo soit bien créées
        // ensuite dans le java on s'appuie sur le dessin de ces tables pour ecrire du SQL
        blocFin.append(ApiService.creationTableResultat(this.tableControleDataTemp, tableOutOkTemp));
        blocFin.append(ApiService.creationTableResultat(this.tableControleDataTemp, tableOutKoTemp));

        // Calcul et maj du taux d'erreur
        StaticLoggerDispatcher.info("Calcul et maj du taux d'erreur", LOGGER);
        blocFin.append(calculTauxErreur(this.tableControleDataTemp, this.tableControlePilTemp));

        // Marquage etat controle_KO pour les seuils d'erreur trop elevé
        StaticLoggerDispatcher.info("Marquage etat controle_KO pour les seuils d'erreur trop elevé", LOGGER);
        blocFin.append(marquageControleKoSeuil(this.tableControlePilTemp, this.getTableSeuil()));
        UtilitaireDao.get("arc").executeBlock(this.connexion, blocFin);

        UtilitaireDao.get("arc").executeImmediate(this.connexion, "vacuum analyze " + this.tableControlePilTemp + ";");

        // Ajout des enregistrements dans la table finale
        StaticLoggerDispatcher.info("Ajout des enregistrements dans la table finale", LOGGER);
        String listColTableIn = this.listeColonne(this.connexion, tableOutOkTemp);


        // on insere dans la table OK que les fichiers pas déclarés complétement
        // KO à cause du seuil (etat_traitement is null)
        blocFin.setLength(0);
        StaticLoggerDispatcher.info("Insertion dans OK", LOGGER);
        blocFin.append(ajoutTableControle(listColTableIn, this.tableControleDataTemp, tableOutOkTemp, this.tableControlePilTemp, "etat_traitement is null",
                "controle='0' AND "));

        // on insere dans la table KO tous les enregistrements à controle!=0 et
        // tous ceux des fichiers déclarés en KO de seuil
        StaticLoggerDispatcher.info("Insertion dans KO", LOGGER);
        blocFin.append(ajoutTableControle(listColTableIn, this.tableControleDataTemp, tableOutKoTemp, this.tableControlePilTemp, "etat_traitement='{"
                + TraitementEtat.KO + "}'", "controle!='0' OR "));

        // Reset de la table de pilotage : on remet tout les états à null
        blocFin.append(resetTablePilotage(this.tableControlePilTemp));
        blocFin.append(passageEtat(this.tableControlePilTemp, TraitementEtat.OK.toString(), tableOutOkTemp));
        blocFin.append(passageEtat(this.tableControlePilTemp, TraitementEtat.KO.toString(), tableOutKoTemp));
        UtilitaireDao.get("arc").executeBlock(this.connexion, blocFin);

    }

    /**
     * Insertion dans les vraies tables
     * @throws Exception
     */
    private void insertionFinale() throws Exception {

	// promote the application user account to full right
	UtilitaireDao.get("arc").executeImmediate(connexion, FormatSQL.changeRole(properties.getDatabaseUsername()));
    	
    // Créer les tables héritées
    String tableIdSourceOK=tableOfIdSource(tableOutOk ,this.idSource);
    createTableInherit(connexion, tableOutOkTemp, tableIdSourceOK);
    String tableIdSourceKO=tableOfIdSource(tableOutKo ,this.idSource);
    createTableInherit(connexion, tableOutKoTemp, tableIdSourceKO);
    
    StringBuilder requete = new StringBuilder();
    
    if (paramBatch == null) {
    	requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ tableOutOk + "_todo;"));
        requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ tableOutOk +";"));
        requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceKO+" inherit "+ tableOutKo +";"));
    }
    else
    {
        requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ tableOutOk + "_todo;"));
        requete.append(FormatSQL.tryQuery("DROP TABLE IF EXISTS "+tableIdSourceKO+";"));
    }
    
    requete.append(this.marquageFinal(this.tablePil, this.tableControlePilTemp));
    
    requete.append(FormatSQL.dropTable(tableOutOkTemp).toString());
    requete.append(FormatSQL.dropTable(tableOutKoTemp).toString());
    requete.append(FormatSQL.dropTable(this.tableControleDataTemp).toString());
    requete.append("\n DISCARD SEQUENCES; DISCARD TEMP;");
    
    UtilitaireDao.get("arc").executeBlock(this.connexion, requete);
    }
    
    
    
    /**
     * Mise en CONTROLE_KO des fichiers avec trop d'erreur
     *
     * @param tablePilTemp
     *            , table de pilotage des fichiers
     * @param tableSeuil
     *            , table de seuil pour la comparaison
     * @return
     */
    public String marquageControleKoSeuil(String tableControlePilTemp, String tableSeuil) {
        StringBuilder requete = new StringBuilder();
        requete.append("WITH ");
        requete.append("seuil AS (  SELECT valeur ");
        requete.append("        FROM " + tableSeuil + " ");
        requete.append("        WHERE nom='s_taux_erreur'), ");
        requete.append("prep AS (   SELECT id_source,taux_ko,valeur ");
        requete.append("        FROM " + tableControlePilTemp + ", seuil ");
        requete.append("        WHERE taux_ko > valeur) ");
        requete.append("\n UPDATE " + tableControlePilTemp + " ");
        requete.append("\n SET etat_traitement='{" + TraitementEtat.KO.toString() + "}', ");
        requete.append("\n rapport='Fichier avec trop d''erreur' ");
        requete.append("\n WHERE id_source in (SELECT distinct id_source FROM prep); ");
        return requete.toString();
    }

    /**
     * Calcul du taux d'erreur (ligne dont le controle est différent de 0) et mise à jour dans pilotage
     *
     * @param tableIn
     *            , la table ayant subi des controles
     * @param tablePil
     *            , la table de pilotage des fichiers (à mettre à jour)
     * @return
     */
    public String calculTauxErreur(String tableIn, String tablePil) {
        StringBuilder requete = new StringBuilder();
        requete.append("\n DROP TABLE IF EXISTS " + this.tableTempControleFoo + " CASCADE; ");
        requete.append("\n CREATE ");
        if (!this.tableTempControleFoo.contains("."))
        {
            requete.append("\n TEMPORARY ");
        }
        requete.append("\n TABLE " + this.tableTempControleFoo
                + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) as select * FROM (");
        requete.append("\n \t with erreur AS(   SELECT id_source, count(id_source)::numeric as nb_erreur ");
        requete.append("\n          FROM " + tableIn + " ");
        requete.append("\n          WHERE controle != '0' ");
        requete.append("\n          GROUP BY id_source), ");
        requete.append("\n tot AS (SELECT id_source, count(id_source)::numeric AS nb_enr ");
        requete.append("\n      FROM " + tableIn + " ");
        requete.append("\n      GROUP BY id_source), ");
        requete.append("\n maj AS (SELECT   tot.id_source,");
        requete.append("\n              (CASE   WHEN erreur.nb_erreur is null ");
        requete.append("\n                  THEN 0 ");
        requete.append("\n                  ELSE erreur.nb_erreur/tot.nb_enr ");
        requete.append("\n              END)::numeric as taux_ko ");
        requete.append("\n      FROM tot LEFT JOIN (SELECT * FROM erreur) AS erreur ON tot.id_source=erreur.id_source) ");
        requete.append("\n select * from maj) foo; ");

        requete.append("\n UPDATE " + tablePil + " a SET taux_ko = b.taux_ko from " + this.tableTempControleFoo
                + " b WHERE a.id_source = b.id_source; ");
        requete.append("\n DROP TABLE IF EXISTS " + this.tableTempControleFoo + " CASCADE; ");
        return requete.toString();
    }

    /**
     * Insertion des données d'une table dans une autre avec un critère de sélection
     *
     * @param listColTableIn
     *
     * @param phase
     *
     * @param tableIn
     *            la table des données à insérer
     * @param tableOut
     *            la table réceptacle
     * @param tableControlePilTemp
     *            la table de pilotage des fichiers
     * @param etatNull
     *            pour sélectionner certains fichiers
     * @param condEnregistrement
     *            la condition pour filtrer la recopie
     * @return
     */
    public String ajoutTableControle(String listColTableIn, String tableIn, String tableOut, String tableControlePilTemp, String condFichier,
            String condEnregistrement) {

        StringBuilder requete = new StringBuilder();
        requete.append("\n INSERT INTO " + tableOut + "(" + listColTableIn + ")");
        requete.append("\n \t   SELECT " + listColTableIn + " ");
        requete.append("\n \t   FROM " + tableIn + " a ");
        requete.append("\n \t   WHERE " + condEnregistrement + " ");
        requete.append("\n \t   EXISTS (select 1 from  " + tableControlePilTemp + " b where a.id_source=b.id_source and "
                + condFichier + ");");
        return requete.toString();
    }

    // Getter et Setter
    public ServiceJeuDeRegle getSjdr() {
        return this.sjdr;
    }

    public void setSjdr(ServiceJeuDeRegle sjdr) {
        this.sjdr = sjdr;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }

    public Connection getConnexion() {
        return connexion;
    }

    public void setConnexion(Connection connexion) {
        this.connexion = connexion;
    }

}
