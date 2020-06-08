package fr.insee.arc.core.service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.NormeFichier;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.LoggerDispatcher;


@Component
public abstract class ApiService implements IDbConstant, IConstanteNumerique {
    public ApiService() {
        super();
    }

    protected static final Logger LOGGER = Logger.getLogger(ApiService.class);

    public static final String SUFFIXE_TEMP_FILE_ORIADE = "_W";
    public static final String FICHIER_MISE_EN_PRODUCTION = "production.dummy";
    public int MAX_PARALLEL_WORKERS;
    public static final String CHILD_TABLE_TOKEN="child";

    // Nom du fichier
    public static final String ID_SOURCE = "id_source";
    // racine xml
    public static final String ROOT="root";

	protected PropertiesHandler properties;

    
    protected Connection connexion;
    protected String envExecution;
    protected String envParameters;
    protected String tablePrevious;
    protected String previousPhase;
    protected String currentPhase;
    protected String tablePil;
    protected String tablePilTemp;
    protected String tableNorme;
    protected String tableJeuDeRegle;
    protected String tableChargementRegle;
    protected String tableNormageRegle;
    protected String tableFiltrageRegle;
    protected String tableMappingRegle;
    protected String tableControleRegle;
    protected String tableSeuil;
    protected Integer nbEnr;
    protected String tableCalendrier;
    protected String directoryRoot;
    protected String nullString="[[[#NULL VALUE#]]]";
    protected String paramBatch=null;
    protected String currentIdSource;
    public int reporting=0;

    
    protected String idSource;

    public final static String SEPARATEUR_JOINTURE_XML = "\n";
    protected Boolean todo = false;
    
    protected HashMap<String, ArrayList<String>> tabIdSource;
    
    public Exception error = null;
    public Thread t = null;
    
    public static ArrayList<Connection> prepareThreads(int parallel, Connection connexion, String anEnvExecution) {
        ArrayList<Connection> connexionList = new ArrayList<Connection>();
		try {
			if (connexion != null) {
				connexionList.add(connexion);
				UtilitaireDao.get("arc").executeImmediate(connexion, configConnection(anEnvExecution));

			}

			for (int i = connexionList.size(); i < parallel; i++) {

				Connection connexionTemp = UtilitaireDao.get(poolName).getDriverConnexion();
				connexionList.add(connexionTemp);

				UtilitaireDao.get("arc").executeImmediate(connexionTemp, configConnection(anEnvExecution));
			}

		} catch (Exception ex) {
			 LoggerHelper.error(LOGGER,ApiService.class, "prepareThreads()", ex);
        }
        return connexionList;

    }



    public  void waitForThreads2(int parallel, ArrayList<? extends ApiService> threadList, ArrayList<Connection> connexionList)
            throws Exception {
        
//        System.out.println("threadList.size() "+threadList.size());
        while (threadList.size() >= parallel && threadList.size() > 0) {
            Iterator<? extends ApiService> it = threadList.iterator();
            
            while (it.hasNext()) {
                ApiService px = it.next();
                if (!px.getT().isAlive()) {

                    if (!(px.getError()==null)) {
                        error = px.error;
                    }
                    px = null;
                    it.remove();

                }
            }
        };

        if (parallel == 0 || error != null) {
            for (int i = 1; i < connexionList.size(); i++) {
                try {
                    connexionList.get(i).close();
                } catch (SQLException ex) {}
            }
        }

        if (error != null) {
            
//            throw error;
        }

    }
    
    /**
     * @param connextionThread
     * @param threadList
     * @param connexionList
     * @return
     */
    public Connection chooseConnection(Connection connextionThread, ArrayList<? extends ApiService> threadList, ArrayList<Connection> connexionList) {
        // on parcourt l'array list de this.connexion disponible
        for (int i = 0; i < connexionList.size(); i++) {
            boolean choosen = true;

            for (int j = 0; j < threadList.size(); j++) {
                if (connexionList.get(i).equals(threadList.get(j).getConnexion())) {
                    choosen = false;
                }
            }

            if (choosen) {
                connextionThread = connexionList.get(i);
                break;
            }
        }
        return connextionThread;
    }


    /**
     * Permet la rétro compatibilité pour la migration vers 1 schéma par envirionnement d'execution
     * 
     * @param anEnv
     * @return
     */
    public static String dbEnv(String env) {
        return env.replace(".", "_")+".";
    }

    public ApiService(String aCurrentPhase, String aParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        LoggerDispatcher.info("** initialiserVariable **", LOGGER);
        // System.out.println(new java.util.Date());
        try {
            this.connexion = UtilitaireDao.get(poolName).getDriverConnexion();
            // this.connexion = InseeConfig.getPool(poolName).getConnection();
            // this.connexion=null;
        } catch (Exception ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "ApiService()",ex);
        }

        if (paramBatch != null && paramBatch.length > 0) {
            this.setParamBatch(paramBatch[0]);
        }

        // Initialisation de la phase;
        this.setCurrentPhase(aCurrentPhase);
        this.setPreviousPhase(TraitementPhase.valueOf(this.getCurrentPhase()).previousPhase().toString());
        // Table en entrée
        this.setEnvExecution(aEnvExecution);
        this.envParameters = aParametersEnvironment;
        this.setDirectoryRoot(aDirectoryRoot);

        this.setTablePrevious((dbEnv(aEnvExecution) + this.getPreviousPhase() + "_" + TraitementEtat.OK).toLowerCase());

        // Tables de pilotage et pilotage temporaire
        this.setTablePil(dbEnv(aEnvExecution) + TraitementTableExecution.PILOTAGE_FICHIER);
        this.tablePilTemp = temporaryTableName(aEnvExecution, aCurrentPhase, TraitementTableExecution.PILOTAGE_FICHIER.toString(), "0");
        this.setTableNorme(dbEnv(aEnvExecution) + TraitementTableParametre.NORME);
        this.tableCalendrier = dbEnv(aEnvExecution) + TraitementTableParametre.CALENDRIER;
        // Tables venant de l'initialisation globale
        this.setTableJeuDeRegle(dbEnv(aEnvExecution) + TraitementTableParametre.JEUDEREGLE);
        this.setTableChargementRegle(dbEnv(aEnvExecution) + TraitementTableParametre.CHARGEMENT_REGLE);
        this.setTableNormageRegle(dbEnv(aEnvExecution) + TraitementTableParametre.NORMAGE_REGLE);
        this.setTableControleRegle(dbEnv(aEnvExecution) + TraitementTableParametre.CONTROLE_REGLE);
        this.setTableFiltrageRegle(dbEnv(aEnvExecution) + TraitementTableParametre.FILTRAGE_REGLE);
        this.setTableMappingRegle(dbEnv(aEnvExecution) + TraitementTableParametre.MAPPING_REGLE);
        this.setTableSeuil(dbEnv(aEnvExecution) + TraitementTableParametre.SEUIL);
        this.setTableOutKo((dbEnv(aEnvExecution) + this.getCurrentPhase() + "_" + TraitementEtat.KO).toLowerCase());
        this.setNbEnr(aNbEnr);

        LoggerDispatcher.info("** Fin constructeur ApiService **", LOGGER);
    }

    /**
     * Compteur simple pour tester la boucle d'execution
     */
    protected Integer cptBoucle = 0;
    private AbstractXmlApplicationContext context;
    private String tableOutKo;





    /**
     * Initialisation des variable et des noms de table
     *
     * @param aEnvExecution
     * @param aPreviousPhase
     * @param aCurrentPhase
     * @param aNbEnr
     */
    public boolean initialiser() {
        LoggerDispatcher.info("** initialiser **", LOGGER);
        // Vérifie si y'a des sources à traiter
        if (this.todo) {
            try {
                UtilitaireDao.get(poolName).executeBlock(this.connexion, configConnection());
            } catch (SQLException ex) {
                LoggerHelper.error(LOGGER,ApiService.class, "initialiser()", ex);
            }
            register(this.connexion, this.getPreviousPhase(), this.getCurrentPhase(), this.getTablePil(), this.tablePilTemp, this.getNbEnr());
        }
        // on est obligé de lancer le context d'autowire manuellement a cause de
        // la factory
        
        // fix a cause de spring mvc : le context peut venir de springmvc quand on fait du rest avec spring et qu'on invoke les api
        // y'a surement mieux comme fix mais pour faire vite
        try {
		    this.context = new ClassPathXmlApplicationContext("classpath*:/applicationContext.xml");
		    AutowireCapableBeanFactory acbFactory = this.context.getAutowireCapableBeanFactory();
		    acbFactory.autowireBean(this);
        }
        catch(Exception e)
        {
        	
        }
        return this.todo;
    }

    /**
     * Permet de configurer la connexion Mettre un timeout par exemple
     */
    public StringBuilder configConnection() {
        return configConnection(this.getEnvExecution());
    }

    
    public static StringBuilder configConnection(String anEnvExecution) {
        StringBuilder requete = new StringBuilder();
        requete.append(FormatSQL.modeParallel(ManipString.substringBeforeFirst(ApiService.dbEnv(anEnvExecution), ".")));
        return requete;

    }
    
    /**
     * Vérifier si y'a des fichiers à traiter avant toutes choses
     *
     * @param tablePil
     * @param phaseAncien
     * @return
     */
    public boolean checkTodo(String tablePil, String phaseAncien, String phaseNouveau) {
        StringBuilder requete = new StringBuilder();
        boolean todo = false;
        requete.append("SELECT 1 FROM " + tablePil + " a ");
        requete.append("WHERE phase_traitement='" + phaseAncien + "' AND '" + TraitementEtat.OK + "'=ANY(etat_traitement) ");
        requete.append("and etape=1 ");
        requete.append("UNION ALL ");
        requete.append("SELECT 1 FROM " + tablePil + " a ");
        requete.append("WHERE phase_traitement='" + phaseNouveau + "' AND '" + TraitementEtat.ENCOURS + "'=ANY(etat_traitement) ");
        requete.append("and etape=1 ");
        requete.append("limit 1 ");
        try {
            todo = UtilitaireDao.get(poolName).hasResults(this.connexion, requete);
        } catch (Exception ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "checkTodo()", ex);
        }
        return todo;
    }

    /**
     * Marque dans la table de pilotage les id_source qui vont être traités dans la phase
     * Si des id_source sont déjà en traitement, la méthode n'en selectionne pas de nouveaux
     * Copie la table de pilotage pour les id_source selectionnés. Cette table sera mis à jour pendant l'éxécution de la phase.
     * @param connexion
     * @param phaseIn
     * @param phase
     * @param tablePil
     * @param tablePilTemp
     * @param nbEnr
     * @throws SQLException
     */
    public void register(Connection connexion, String phaseIn, String phase, String tablePil, String tablePilTemp, Integer nbEnr) {
        LoggerDispatcher.info("** register **", LOGGER);
        try {
            // System.out.println(new java.util.Date());
            StringBuilder blocInit = new StringBuilder();

            // si il n'y a pas de sources déjà marquées en cours, on procède à la selection
            if (!UtilitaireDao.get(poolName).hasResults(
                    connexion,
                    "select 1 from " + tablePil + " where phase_traitement='" + phase + "' AND '" + TraitementEtat.ENCOURS
                            + "'=ANY(etat_traitement) and etape=1 limit 1")) {
                blocInit.append(selectionSource(tablePil, phaseIn, phase, nbEnr));
            }
            blocInit.append(copieTablePilotage(phase, tablePil, tablePilTemp));

            // LoggerDispatcher.info("Selection et copie de la table de pilotage : "
            // + blocInit.toString(),logger);

            UtilitaireDao.get(poolName).executeBlock(connexion, blocInit);
        } catch (Exception ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "register()", ex);
        }
    }

    /**
     * Méthode pour marquer la table de pilotage temporaire avec le jeu de règle appliqué
     *
     * @return
     */
    protected String marqueJeuDeRegleApplique(String pilTemp) {
        StringBuilder requete = new StringBuilder();
        requete.append("WITH ");
        requete.append("prep AS (SELECT a.id_source, a.id_norme, a.periodicite, b.validite_inf, b.validite_sup, b.version ");
        requete.append("	FROM " + pilTemp + " a  ");
        requete.append("	INNER JOIN "
                + this.getTableJeuDeRegle()
                + " b ON a.id_norme=b.id_norme AND a.periodicite=b.periodicite AND b.validite_inf <=a.validite::date AND b.validite_sup>=a.validite::date ");
        requete.append("	WHERE phase_traitement='" + this.getCurrentPhase() + "') ");
        requete.append("UPDATE " + pilTemp + " AS a ");
        requete.append("SET validite_inf=prep.validite_inf, validite_sup=prep.validite_sup, version=prep.version ");
        requete.append("FROM prep ");
        requete.append("WHERE a.id_source=prep.id_source AND a.phase_traitement='" + this.getCurrentPhase() + "'; ");
        return requete.toString();
    }

    /**
     * Requete permettant de récupérer les règles pour un id_source donnée et une table de regle
     * @param idSource : identifiant du fichier
     * @param tableRegle : table de regle
     * @param tablePilotage : table de pilotage
     * @return
     */
    public static String getRegles(String tableRegle, String tablePilotage) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT * FROM "+tableRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	return requete.toString();
    }

    public static String getRegles(String tableRegle, NormeFichier normeFichier ) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT * FROM "+tableRegle+" a WHERE ");
    	requete.append(conditionRegle(normeFichier));
    	return requete.toString();
    }
    
    
    /**
     * Récupère toutes les rubriques utilisées dans les regles relatives au fichier
     * @param idSource
     * @param tablePilotage
     * @param tableNormageRegle
     * @param tableControleRegle
     * @param tableFiltrageRegle
     * @param tableMappingRegle
     * @return
     */
    public static String getAllRubriquesInRegles(String tablePilotage, String tableNormageRegle, String tableControleRegle, String tableFiltrageRegle, String tableMappingRegle) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT * FROM ( ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_col),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableMappingRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_filtre),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableFiltrageRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_pere) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_fils) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(condition),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(pre_action),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique) as var from "+tableNormageRegle+" a where id_classe!='suppression' AND ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_nmcl) as var from "+tableNormageRegle+" a where id_classe!='suppression' AND ");
    	requete.append(conditionRegle(tablePilotage));
    	requete.append("\n ) ww where var is NOT NULL; ");
    	return requete.toString();
    }
    
    /**
     * Retourne la clause WHERE SQL qui permet de selectionne les bonne regles pour un fichier
     * @param idSource
     * @param tablePilotage
     * @return
     */
    private static String conditionRegle (String tablePilotage)
    {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n ");
    	requete.append("EXISTS ( SELECT * FROM "+tablePilotage+" b ");
    	requete.append("WHERE a.id_norme=b.id_norme ");
    	requete.append("AND a.periodicite=b.periodicite ");
    	requete.append("AND a.validite_inf<=to_date(b.validite,'YYYY-MM-DD') ");
    	requete.append("AND a.validite_sup>=to_date(b.validite,'YYYY-MM-DD') ");
    	requete.append(") ");
    	return requete.toString();
    }
    
    private static String conditionRegle (NormeFichier normeFichier)
    {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n ");
    	requete.append("a.id_norme='"+normeFichier.getIdNorme()+"' ");
    	requete.append("AND a.periodicite='"+normeFichier.getPeriodicite()+"' ");
    	requete.append("AND a.validite_inf<=to_date('"+normeFichier.getValidite()+"','YYYY-MM-DD') ");
    	requete.append("AND a.validite_sup>=to_date('"+normeFichier.getValidite()+"','YYYY-MM-DD') ");
    	requete.append(";");
    	return requete.toString();
    }
    
    /**
     * Requete permettant de récupérer les règles pour un id_source donnée et une table de regle
     * @param id_source
     * @param tableRegle
     * @return SQL pil.id_source, pil.jointure, pil.id_norme, pil.validite, pil.periodicite, pil.validite
     */
    public static String getNormeAttributes(String idSource, String tablePilotage) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT pil.id_source, pil.jointure, pil.id_norme, pil.validite, pil.periodicite, pil.validite " + "FROM " + tablePilotage + " pil " + " WHERE id_source='" + idSource + "' ");
    	return requete.toString();
    }
    
    /**
     * récupere le contenu d'une requete; iniitalise si le résultat est null
     * @param c
     * @param req
     * @return
     * @throws SQLException
     */
    public static HashMap<String, ArrayList<String>> getBean(Connection c, String req) throws SQLException
    {
    	GenericBean gb=  new GenericBean(UtilitaireDao.get("arc").executeRequest(c, req));
    	HashMap<String, ArrayList<String>> m = gb.mapContent();

    	if (gb.headers.size()>0 && m.get(gb.headers.get(0))==null)
    	{
    		for (int i=0; i<gb.headers.size(); i++)
    		{
    			m.put(gb.headers.get(i), new ArrayList<String>());
    		}
    	}
    	
    	return m;
    }
    
    
    /**
     * Selection d'un lot d'id_source pour appliquer le traitement
     * Les id_sources sont selectionnés parmi les id_source présent dans la phase précédentes avec etape =1
     * Ces id_source sont alors mis à jour dans la phase précédente à étape =0 et 
     * une nouvelle ligne est créee pour la phase courante et pour chaque id_source avec etape=1
     * @param tablePil
     *            , la table de pilotage des fichiers
     * @param phaseAncien
     *            , la phase précédente
     * @param phaseNouveau
     *            , la phase courante
     * @param nbEnr
     *            , le nombre maximal d'enregistrement que l'on souhaite voir traités
     * @return
     */
    private String selectionSource(String tablePil, String phaseAncien, String phaseNouveau, Integer nbEnr) {
        StringBuilder requete = new StringBuilder();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//      requete.append("WITH prep as (SELECT a.*, sum(nb_enr) OVER (ORDER BY date_traitement, nb_essais, id_source) as cum_enr ");
        requete.append("WITH prep as (SELECT a.*, count(1) OVER (ORDER BY date_traitement, nb_essais, id_source) as cum_enr ");
        requete.append("FROM " + tablePil + " a ");
        requete.append("WHERE phase_traitement='"+phaseAncien+"'  AND '" + TraitementEtat.OK + "'=ANY(etat_traitement) and etape=1 ) ");
        requete.append(",mark AS (SELECT a.*    FROM prep a WHERE cum_enr<" + nbEnr + " ");
        requete.append("UNION   (SELECT a.* FROM prep a LIMIT 1)) ");
        requete.append(", update as ( UPDATE " + tablePil + " a set etape=0 from mark b where a.id_source=b.id_source and a.etape=1) ");
        requete.append("INSERT INTO " + tablePil + " ");
        requete.append("(container, id_source, date_entree, id_norme, validite, periodicite, phase_traitement, etat_traitement, date_traitement, rapport, taux_ko, nb_enr, nb_essais, etape, generation_composite ");
        requete.append(",jointure");
        requete.append(") ");
        requete.append("select container, id_source, date_entree, id_norme, validite, periodicite, '" + phaseNouveau + "' as phase_traitement, '{" + TraitementEtat.ENCOURS + "}' as etat_traitement ");
        requete.append(", '" + formatter.format(date) + "', rapport, taux_ko, nb_enr, nb_essais, 1 as etape, generation_composite ");
        requete.append(",jointure ");
        requete.append("from mark; ");

        return requete.toString();
    }

    /**
     * Fabrique une copie de la table de pilotage avec uniquement les informations et les enregistrements qui concernent le batch
     *
     * @param tablePil
     * @param tablePilTemp
     * @return
     */
    private String copieTablePilotage(String phase, String tablePil, String tablePilTemp) {
        StringBuilder requete = new StringBuilder();
        requete.append("\n DROP TABLE IF EXISTS " + tablePilTemp + "; ");
        requete.append("\n CREATE ");
        if (!tablePilTemp.contains(".")) {
            requete.append("TEMPORARY ");
        } else {
            requete.append(" ");
        }
        requete.append("TABLE " + tablePilTemp + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ( ");
        requete.append("\n SELECT *  ");
        requete.append("\n FROM " + tablePil + " ");
        requete.append("\n WHERE etape=1 and phase_traitement='" + phase + "' AND etat_traitement='{" + TraitementEtat.ENCOURS + "}'); ");
        requete.append("\n analyze " + tablePilTemp + ";");
        return requete.toString();
    }

    public abstract void executer() throws Exception;

    
    /**
     * Finalise l'appel d'une phase
     * Marque dans la table de pilotage globale les id_source qui ont été traités
     * (recopie des état de la table de pilotage temporaire de la phase vers la table de pilotage globale)
     * Efface les objets temporaires (tables, type, ...)
     */
    public void finaliser() {
        LoggerDispatcher.info("finaliser", LOGGER);

        try {
        if (this.todo) {

            if (!(this.getTablePrevious().contains(TraitementPhase.DUMMY.toString().toLowerCase())
                    || this.getTablePrevious().contains(TraitementPhase.INITIALISATION.toString().toLowerCase())
                    || this.getTablePrevious()
                        .contains(TraitementPhase.RECEPTION.toString().toLowerCase()))) {
                deleteTodo(this.connexion, this.tablePilTemp, this.getTablePrevious(), this.paramBatch);
            }
            StringBuilder requete = new StringBuilder();
            requete.append(FormatSQL.dropTable(this.tablePilTemp).toString());
            try {
                UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);
            } catch (Exception ex) {
                LoggerHelper.error(LOGGER,ApiService.class, "finaliser()", ex);
            }
			
          if (this.currentPhase.equals(TraitementPhase.CHARGEMENT.toString()) && this.paramBatch!=null)
          {
  				ApiService.maintenancePgCatalog(this.connexion, "freeze analyze");
  				ApiService.maintenancePilotageT(this.connexion, this.envExecution, "freeze analyze");
          }
		}
        }
        finally {
        	try {
	            if (this.connexion != null) {
	                this.connexion.close();
	                this.connexion = null;
	            }
	        } catch (Exception ex) {
	            LoggerHelper.error(LOGGER,ApiService.class, "finaliser()", ex);
	        }
        
	        if (this.context != null) {
	            this.context.close();
	        }
        }
    }

    /**
     * Effacer les fichiers traités de la table todo de la phase précédente
     * 
     * @param connexion
     * @param tablePilTemp
     * @param tablePrevious
     */
    public static void deleteTodo(Connection connexion, String tablePilTemp, String tablePrevious, String paramBatch) {
        try {
        	
            // LOCK OBLIGATOIRE POUR NE PAS PERDRE DE DONNER LORS DES EXECUTIONS EN PARALLELE
//            LoggerDispatcher.info("** nettoyage todo **", LOGGER);
//            UtilitaireDao.get(poolName).executeBlock(
//                    connexion,
//                    "LOCK TABLE "
//                            + tablePrevious
//                            + " IN ACCESS EXCLUSIVE MODE;"
//                            + FormatSQL.rebuildTableAsSelectWhere(tablePrevious, " id_source not in (SELECT distinct id_source from "
//                                    + tablePilTemp + " b) ", "analyze " + tablePrevious + "(id_source); "
//                                    ));
            // Si on est en batch, on drop les tables source
        	// sinon on retire le lien avec la table héritée
        	StringBuilder query=new StringBuilder();
   		 	HashMap<String, ArrayList<String>> m=new GenericBean(UtilitaireDao.get(poolName).executeRequest(connexion, "select id_source from "+tablePilTemp+"")).mapContent();
       		int count=0; 
   		 	for (String z:m.get("id_source")) {	
   		 		
   		 		count++;
       			 if (paramBatch==null)
       			 {
        			 query.append("ALTER TABLE "+tableOfIdSource(tablePrevious, z)+" NO INHERIT "+tablePrevious+"_todo ;");
       			 }
        		 else
        		 {
        			 query.append("DROP TABLE IF EXISTS "+tableOfIdSource(tablePrevious, z)+";");
	 
        		 }
       			 if (count>FormatSQL.MAX_LOCK_PER_TRANSACTION)
       			 {
       	       		UtilitaireDao.get(poolName).executeBlock(connexion, query);
       	       		query.setLength(0);
       	       		count=0;
       			 }
        	}
       		UtilitaireDao.get(poolName).executeBlock(connexion, query);
                    
        } catch (Exception ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "deleteTodo()", ex);
        }

    }
   

    public static void maintenancePilotage(Connection connexion, String envExecution, String type) {
        String tablePil = dbEnv(envExecution) + TraitementTableExecution.PILOTAGE_FICHIER;
        LoggerDispatcher.info("** Maintenance Pilotage **", LOGGER);

        try {
            UtilitaireDao.get(poolName).executeImmediate(connexion,"vacuum " +type + " "+ tablePil + ";");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Maintenance sur la transposée de la table de pilotage (vue par date d'entrée dans l'écran IHM)
     * @param connexion
     * @param envExecution
     * @param type
     */
    public static void maintenancePilotageT(Connection connexion, String envExecution, String type) {
        
    	// table de pilotage transposée par date d'entrée
    	String tablePilT = dbEnv(envExecution) + TraitementTableExecution.PILOTAGE_FICHIER+"_t";
        
        LoggerDispatcher.info("** Maintenance Pilotage T **", LOGGER);

        try {
            UtilitaireDao.get(poolName).executeImmediate(connexion,FormatSQL.vacuumSecured(tablePilT, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * vaccum table du méta modele
	 * @param connexion
	 */
	public static void maintenancePgCatalog(Connection connexion, String type)	{
        // postgres libere mal l'espace sur ces tables qaund on fait trop d'opération sur les colonnes
		// vaccum full sinon ca fait quasiment rien ...
            LoggerDispatcher.info("** Maintenance Catalogue **", LOGGER);
            UtilitaireDao.get(poolName).maintenancePgCatalog(connexion, type);
	}

    /**
     * vacuum analyze et index sur les tables * vacuum analyze et index sur les tables
     * 
     * @param connexion
     * @param envExecution
     * @param typeMaintenance
     *            : full ou ""
     */         
    public static void maintenance(Connection connexion, String envExecution, String typeMaintenance)       {
//        maintenancePilotage(connexion, envExecution, typeMaintenance);
//		maintenancePgCatalog(connexion, typeMaintenance);
        LoggerDispatcher.info("** Fin de maintenance **", LOGGER);

    }

    public static String temporaryTableName(String aEnvExecution, String aCurrentPhase, String tableName, String... suffix) {

        if (suffix != null && suffix.length > 0) {
        	String suffixJoin = String.join("$", suffix);
        	return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName, suffixJoin);
        } else {
            return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName);
        }
    }

    public static String globalTableName(String aEnvExecution, String aCurrentPhase, String tableName) {
        return dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName;
    }

    public static String temporaryTableName(String aEnvExecution, String tableName) {
        return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + tableName);
    }

    public static String globalTableName(String aEnvExecution, String tableName) {
        return dbEnv(aEnvExecution) + tableName;
    }

    /**
     * liste les id_source pour une phase et un etat donnée dans une table de pilotage
     *
     * @param tablePilotage
     * @param aCurrentPhase
     * @param etat
     * @return
     */
    public HashMap<String, ArrayList<String>> pilotageListIdsource(String tablePilotage, String aCurrentPhase, String etat) {
        LoggerDispatcher.info("pilotageListIdsource", LOGGER);
        StringBuilder requete = new StringBuilder();
        requete.append("SELECT container, id_source FROM " + tablePilotage + " ");
        requete.append("WHERE phase_traitement='" + aCurrentPhase + "' ");
        requete.append("AND '" + etat + "'=ANY(etat_traitement); ");
        try {
            return new GenericBean(UtilitaireDao.get(poolName).executeRequest(this.connexion, requete)).mapContent();
        } catch (SQLException ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "pilotageListIdSource()", ex);
        }
        return new HashMap<String, ArrayList<String>>();
    }

    /**
     * Marque la phase et l'état d'un idsource dans une table de pilotage
     *
     * @param tablePilotage
     * @param idSource
     * @param phaseNew
     * @param etatNew
     * @return
     * 
     *         modif 05/04/2016 Pépin Rémi Ajout du paramètre typeComopsite pour garder en base la requete de génération et l'exécuter si
     *         besoin.
     * 
     */
    public static StringBuilder pilotageMarkIdsource(String tablePilotage, String idSource, String phaseNew, String etatNew, String rapport, String... jointure) {
        StringBuilder requete = new StringBuilder();
        requete.append("UPDATE " + tablePilotage + " ");
        requete.append("SET phase_traitement= '" + phaseNew + "' ");
        requete.append(", etat_traitement= '{" + etatNew + "}' ");
        if (rapport == null) {
            requete.append(", rapport= null ");
        } else {
            requete.append(", rapport= '" + rapport + "' ");
        }

        if (jointure.length > 0) {
            requete.append(", jointure= '" + jointure[0] + "'");
        }

        requete.append("WHERE id_source='" + idSource + "';\n");
        return requete;
    }

    /**
     * Remplace les enregistrement en cours d'une phase d'une table de pilotage avec ceux d'une table de pilotage temporaire
     *
     * @param tablePil
     * @param tablePilTemp
     * @return
     */
    public String marquageFinal(String tablePil, String tablePilTemp) {
        return marquageFinal(tablePil, tablePilTemp, "");
    }

    public String marquageFinal(String tablePil, String tablePilTemp, String id_source) {
        StringBuilder requete = new StringBuilder();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        requete.append("\n set enable_hashjoin=off; ");
        requete.append("\n UPDATE " + tablePil + " AS a ");
        requete.append("\n \t SET etat_traitement =  b.etat_traitement, ");
        requete.append("\n \t   id_norme = b.id_norme, ");
        requete.append("\n \t   validite = b.validite, ");
        requete.append("\n \t   periodicite = b.periodicite, ");
        requete.append("\n \t   taux_ko = b.taux_ko, ");
        requete.append("\n \t   date_traitement = '" + formatter.format(date) + "', ");
        requete.append("\n \t   nb_enr = b.nb_enr, ");
        requete.append("\n \t   rapport = b.rapport, ");
        requete.append("\n \t   validite_inf = b.validite_inf, ");
        requete.append("\n \t   validite_sup = b.validite_sup, ");
        requete.append("\n \t   version = b.version, ");
        requete.append("\n \t   etape = case when b.etat_traitement='{" + TraitementEtat.KO + "}' then 2 else b.etape end, ");
        requete.append("\n \t   jointure = b.jointure ");


        // Si on dispose d'un id source on met à jour seulement celui ci
        requete.append("\n \t FROM " + tablePilTemp + " as b ");
        if (id_source.isEmpty()) {
            requete.append("\n \t WHERE b.id_source =  a.id_source");
        } else {
            requete.append("\n \t WHERE b.id_source = '" + id_source + "' ");
            requete.append("\n \t AND a.id_source = '" + id_source + "' ");
        }
        requete.append("\n \t AND a.etape = 1 ; ");
        requete.append("\n set enable_hashjoin = on; ");
        return requete.toString();

    }

    public String getMiseANiveauSchemaTable(String aTableReference, String... aTableCible) throws SQLException {

        StringBuilder returned = new StringBuilder();
        
        for (int i = 0; i < aTableCible.length; i++) {
    	ArrayList<ArrayList<String>> listeColonne = UtilitaireDao.get(poolName).executeRequest(this.connexion,
                FormatSQL.listAjoutColonne(aTableReference, aTableCible[i]));
        
        ArrayList<ArrayList<String>> listeColonneKeeped = new ArrayList<ArrayList<String>>();

        for (int j =2; j<listeColonne.size(); j++) {
//            String debutNomColonne = listeColonne.get(j).get(0).substring(0, 2);
//            if (!(debutNomColonne.equalsIgnoreCase("i_") || debutNomColonne.equalsIgnoreCase("v_"))) {
                listeColonneKeeped.add(listeColonne.get(j));
//            }
        }

            returned.append(FormatSQL.addColonnePourGenericBeanData(aTableCible[i], listeColonneKeeped) + "\n");
        }
        return returned.toString();
        
    }


    /**
     * Renvoie la liste des colonnes d'une table, avec comme séparateur une virgule
     *
     * @param connexion
     * @param tableIn
     * @return
     */
    public String listeColonne(Connection connexion, String tableIn) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try {
            result = UtilitaireDao.get(poolName).executeRequest(connexion, FormatSQL.listeColonne(tableIn));

        } catch (SQLException ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "listeColonne()", ex);
        }
        StringBuilder listCol = new StringBuilder();
        if (result.size() >= 2) {// les données ne sont qu'à partir du 3e
            // élement (1er noms, 2e types)
            for (int i = 2; i < result.size(); i++) {
                if (i == 2) {// initialisation de la liste (pas de virgule)
                    listCol.append(result.get(i).get(0));
                } else {
                    listCol.append("," + result.get(i).get(0));
                }
            }
        }
        return listCol.toString();
    }
    
    /**
     * Renvoie un hashset des colonnes
     *
     * @param connexion
     * @param tableIn
     * @return
     */
    public HashSet<String> listeColonneHashSet(Connection connexion, String tableIn) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try {
            result = UtilitaireDao.get(poolName).executeRequest(connexion, FormatSQL.listeColonne(tableIn));
        } catch (SQLException ex) {
            LoggerHelper.error(LOGGER,ApiService.class, "listeColonne()", ex);
        }
        HashSet<String> listCol = new HashSet<String>();
        if (result.size() >= 2) {// les données ne sont qu'à partir du 3e
            // élement (1er noms, 2e types)
            for (int i = 2; i < result.size(); i++) {
                    listCol.add(result.get(i).get(0));
            }
        }
        return listCol;
    }

    /**
     * Créer une table image vide d'une autre table Si le schema est spécifié, la table est créée dans le schema; sinon elle est crée en
     * temporary
     *
     * @param tableIn
     * @param tableToBeCreated
     * @return
     */
    public static String creationTableResultat(String tableIn, String tableToBeCreated, Boolean... image) {
        StringBuilder requete = new StringBuilder();
        requete.append("\n CREATE ");
        if (!tableToBeCreated.contains(".")) {
            requete.append("TEMPORARY ");
        } else {
            requete.append(" ");
        }
        requete.append("TABLE " + tableToBeCreated + " ");
        requete.append(""+FormatSQL.WITH_NO_VACUUM+" ");
        requete.append("as SELECT * FROM " + tableIn + " ");
        if (image.length == 0 || image[0] == false) {
            requete.append("where 1=0 ");
        }
        requete.append("; ");
        return requete.toString();
    }

    /**
     * Insertion dans une table resultat; les id_source de la table resultat présent dans la table pilotage sont préalablement effacés Si le
     * schema est spécifié, la table est créée dans le schema; sinon elle est crée en temporary
     *
     * @param tableIn
     * @param tableToBeCreated
     * @return
     */
    public String insertTableResultat(String tableTemp, String tableFinale, String tableFinaleBuffer, String tablePil, String... col) {
        StringBuilder requete = new StringBuilder();
        // sécurité inutile à condition qu'on fasse bien une transaction donnée + pilotage
        // requete.append("DELETE FROM " + tableFinale + " A USING (select id_source from " + tablePil +
        // ") B where a.id_source=B.id_source; ");
        // requete.append("do $$ begin DROP INDEX "+ManipString.substringBeforeFirst(tableFinale,
        // ".")+"idx1_"+ManipString.substringAfterFirst(tableFinale, ".")+"; exception when others then end; $$;");

        // if (tableFinaleBuffer != null) {
        // requete.append("LOCK TABLE "+tableFinaleBuffer+" IN ACCESS EXCLUSIVE MODE;");
        // }

        String cols;
        if (col.length == 1) {
            cols = col[0];
        } else {
            cols = listeColonne(this.connexion, tableTemp);
        }
        // System.out.println(cols);

        // LoggerDispatcher.info("Liste des colonnes de la table " + tableTemp +
        // " : " + cols,logger);
        if (tableFinale != null) {
            requete.append("\n INSERT INTO " + tableFinale + " (" + cols + ") select " + cols + " from " + tableTemp + " ; ");
        }
        if (tableFinaleBuffer != null) {
            requete.append("\n INSERT INTO " + tableFinaleBuffer + " (" + cols + ") select " + cols + " from " + tableTemp + " ; ");
        }

        return requete.toString();
    }
    
    /**
     * Permet de déclencher l'intialisation en production à une certaine heure
     */
    public static void declencherInitialisationEnProduction() {
    	
    	int HEURE_INITIALISATION_PRODUCTION=BDParameters.getInt(null, "ApiService.HEURE_INITIALISATION_PRODUCTION",22);
    	
        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
        Date dNow = DateUtils.setHours(new Date(), HEURE_INITIALISATION_PRODUCTION);

        System.out.println("declencherInitialisationEnProduction : " + dateFormat.format(dNow));

        try {
            UtilitaireDao.get("arc").executeRequest(
                    null,
                    "update arc.pilotage_batch set last_init='" + dateFormat.format(dNow)
                            + "', operation=case when operation='R' then 'O' else operation end;");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
        }
    }
    
    
    
    /**
     * Insertion dans une table resultat; les id_source de la table resultat présent dans la table pilotage sont préalablement effacés Si le
     * schema est spécifié, la table est créée dans le schema; sinon elle est crée en temporary
     *
     * @param tableIn
     * @param tableToBeCreated
     * @return
     */
    public String insertTableResultat2(String tableTemp, String tableFinale, String tableFinaleBuffer, String tablePil, String... col) {
        StringBuilder requete = new StringBuilder();
        
        // récupération des colonnes de la table
        String cols;
        if (col.length >0) {
            cols = col[0];
        } else {
            cols = listeColonne(this.connexion, tableTemp);
        }
        
        // on parse cols pour trier les colonnes. Certaines vont dans le type composite, d'autre pas
        // Celles qui commencent pas i_ ou v_ sont les noms des attributs contenuent dans le fichier source et vont donc
        // dans le type composite mais pas ailleurs.
        
        String[] arrayColonne = cols.split(",");
        String colonneNonAttribut = "";
        String colonneAttribut="";
        
        for (String colonne : arrayColonne) {
            String debutNomCol = colonne.trim().substring(0, 2);
            if (!(debutNomCol.equalsIgnoreCase("i_") || debutNomCol.equalsIgnoreCase("v_"))) {
                colonneNonAttribut = colonneNonAttribut + colonne+",";
            }
            else
            {
            	colonneAttribut = colonneAttribut + colonne+","; 
            }
        }
        
        //on supprime les dernières virgules
        colonneNonAttribut=ManipString.substringBeforeLast(colonneNonAttribut, ",");
        colonneAttribut=ManipString.substringBeforeLast(colonneAttribut, ",");

        // on construit la chaine d'insertion
        String colsInserted = colonneNonAttribut + ", data";
        String colsValues = colonneNonAttribut + ", ROW("+colonneAttribut+") ";

        
        if (tableFinale != null) {
            requete.append("\n INSERT INTO " + tableFinale + " (" + colsInserted + ") select " + colsValues + " from " + tableTemp + " ; ");
        }
        if (tableFinaleBuffer != null) {
            requete.append("\n INSERT INTO " + tableFinaleBuffer + " (" + colsInserted + ") select " + colsValues + " from " + tableTemp + " ; ");
        }

        return requete.toString();
    }

    /**
     * Création filtre sur les id_source marqué en état KO
     *
     * @param tableIn
     * @param tableOut
     * @param tablePilTemp
     * @return
     */
    public String createTableTravail(String extraColumns, String tableIn, String tableOut, String tablePilTemp, String... etat_traitement) {
        return (createTableTravail(extraColumns, tableIn, tableOut, tablePilTemp, "", false, etat_traitement));
    }

    public String createTableTravail(String extraColumns, String tableIn, String tableOut, String tablePilTemp, String idSource, Boolean isIdSource,
            String... etat_traitement) {
        StringBuilder requete = new StringBuilder();

        if (tableIn.toLowerCase().contains("_todo")) {
            requete.append(FormatSQL.lock(tableIn));
        }
        requete.append("\n DROP TABLE IF EXISTS " + tableOut + " CASCADE; \n");

        requete.append("\n CREATE ");
        if (!tableOut.contains(".")) {
            requete.append("TEMPORARY ");
        } else {
            requete.append("UNLOGGED ");
        }

        requete.append("TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
        requete.append("( ");
        requete.append("\n    SELECT * " + extraColumns);
        requete.append("\n    FROM " + tableIn + " stk ");
        requete.append("\n    WHERE exists ( SELECT 1  ");
        requete.append("\n            FROM " + tablePilTemp + " pil  ");
        requete.append("\n  where pil.id_source=stk.id_source ");
        if (etat_traitement.length > 0) {
            requete.append(" AND '" + etat_traitement[0] + "'=ANY(pil.etat_traitement) ");
        }
        if (isIdSource) {
            requete.append(" AND stk.id_source ='" + idSource + "' ");
        }
        requete.append(" ) ");
        requete.append(");\n");

        return requete.toString();
    }
    
    
    

    public void createTableInherit(Connection connexion, String tableIn, String tableIdSource) throws Exception
    {

    	// on créé la table héritée que si la table a des enregistrements
    	if (UtilitaireDao.get(poolName).hasResults(connexion, "SELECT 1 FROM "+tableIn+" LIMIT 1"))
    	{
    	
        StringBuilder query = new StringBuilder();
        
    	LoggerDispatcher.info("** createTableOK ** : "+tableIdSource, LOGGER);
    	java.util.Date beginDate = new java.util.Date();
    	        
        query.append("DROP TABLE IF EXISTS " + tableIdSource + ";");
        query.append("CREATE TABLE " + tableIdSource +" "+FormatSQL.WITH_NO_VACUUM+" AS SELECT * FROM "+tableIn+";");


        UtilitaireDao.get("arc").executeBlock(connexion, query);
        
        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** createTableOK ** temps : " + (endDate.getTime()-beginDate.getTime()) + " ms", LOGGER);
    	}
    	
    }
    
    
    
    public static String tableOfIdSource(String tableName, String idSource)
    {
    	String hashText="";
        MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA1");
			m.update(idSource.getBytes(),0,idSource.length());
			hashText=String.format("%1$032x",new BigInteger(1,m.digest()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return tableName + "_"+CHILD_TABLE_TOKEN+"_" + hashText;
    }
    
    /**
     * Créer la copie d'une table selectionnée sur un id_source particulier
     * @param TableIn
     * @param TableOut
     * @param idSource
     * @return
     */
    public String createTablePilotageIdSource (String tableIn, String tableOut, String idSource)
    {
        StringBuilder requete = new StringBuilder();
        requete.append("\n CREATE ");
        if (!tableOut.contains(".")) {
            requete.append("TEMPORARY ");
        } else {
            requete.append("UNLOGGED ");
        }
        requete.append("TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
        requete.append("\n SELECT * FROM "+tableIn+" ");
        requete.append("\n WHERE id_source ='" + idSource + "' ");
        requete.append("\n AND etape = 1 ");
        requete.append("\n ; ");
        return requete.toString();
    }

    
    /**
     * Met à jour le comptage du nombre d'enregistrement par fichier; nos fichiers de blocs XML sont devenus tous plats :) 
     * 
     * @throws SQLException
     */
    public void updateNbEnr(String tablePilTemp, String tableTravailTemp, String...jointure) throws SQLException {
        StringBuilder query = new StringBuilder();

        // mise à jour du nombre d'enregistrement et du type composite
        LoggerDispatcher.info("** updateNbEnr **", LOGGER);
        query.append("\n UPDATE " + tablePilTemp + " a ");
        query.append("\n \t SET nb_enr=(select count(*) from " + tableTravailTemp + ") ");
        
        if (jointure.length > 0) {
        	query.append(", jointure= " + FormatSQL.textToSql(jointure[0]) + "");
        }
        query.append(";");
        
        UtilitaireDao.get(poolName).executeBlock(this.getConnexion(), query);

    }
    
    
    public String createTableTravailIdSource(String tableIn, String tableOut, String idSource, String... extraCols)
    {
    	 StringBuilder requete = new StringBuilder();
         requete.append("\n CREATE ");
         if (!tableOut.contains(".")) {
             requete.append("TEMPORARY ");
         } else {
             requete.append("UNLOGGED ");
         }
         requete.append("TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
         
         requete.append("\n SELECT * ");
         
         if (extraCols.length>0)
         {
        	 requete.append(", "+extraCols[0]);
         }
         
         requete.append("\n FROM "+tableOfIdSource(tableIn,idSource)+"; ");
         
         return requete.toString();
    }

    

    /**
     *
     * @return le temps d'execution
     */
    public ServiceReporting invokeApi() {
        double start = System.currentTimeMillis();
        int nbLignes = 0;

        LoggerDispatcher.info("****** Execution " + this.getCurrentPhase() + " *******", LOGGER);
        try {

            // set schema
            // Méthode pour implémenter des maintenances sur la base de donnée
            if (this.getCurrentPhase().equals(TraitementPhase.INITIALISATION.toString())) {
                ApiInitialisationService.bddScript(this.getEnvExecution(), this.connexion);
            }

            if (this.getCurrentPhase().equals(TraitementPhase.INITIALISATION.toString()) || this.getCurrentPhase().equals(TraitementPhase.RECEPTION.toString())) {
                this.todo = true;
            } else {
                this.todo = checkTodo(this.getTablePil(), this.getPreviousPhase(), this.getCurrentPhase());
            }
            LoggerDispatcher.info("A faire - " + this.getCurrentPhase() + " : " + this.todo, LOGGER);

            if (this.initialiser()) {
                try {
                    this.executer();
                } catch (Exception ex) {
                    LoggerDispatcher.error("Erreur dans " + this.getCurrentPhase() + ". ", ex, LOGGER);
                    try {
                        ex.printStackTrace();

                        this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.getTablePil(), ex, "aucuneTableADroper");
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }
            }
        } finally {
            if (this.todo && !this.getCurrentPhase().equals(TraitementPhase.INITIALISATION.toString())) {
                
            	if (this.reporting>0)
            	{
            		nbLignes=this.reporting;
            	}
            	else
            	{
	            	try {
	                    UtilitaireDao.get(poolName).executeRequest(this.connexion, "CREATE TABLE IF NOT EXISTS " + this.tablePilTemp + " (nb_enr int); ");
	                } catch (SQLException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	                nbLignes = UtilitaireDao.get(poolName).getInt(this.connexion, "select coalesce(sum(nb_enr),0) from " + this.tablePilTemp);
            	}
            }
            this.finaliser();
        }

        LoggerDispatcher.info("****** Fin " + this.getCurrentPhase() + " *******", LOGGER);

        return new ServiceReporting(nbLignes, System.currentTimeMillis() - start);

    }

    public String getTablePilTemp() {
        return this.tablePilTemp;
    }

    public void setTablePilTemp(String tablePilTemp) {
        this.tablePilTemp = tablePilTemp;
    }

    /**
     * Remise dans l'état juste avant le lancement des controles et insertion dans une table d'erreur
     *
     * @param connexion
     * @param phase
     * @param tablePil
     * @param exception
     * @param tableDrop
     * @throws SQLException
     */
    public void repriseSurErreur(Connection connexion, String phase, String tablePil, Exception exception, String... tableDrop) throws SQLException {
        // nettoyage de la connexion
        // comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de la transaction
        // si on lui renvoie une requete SQL, il la refuse avec le message
        // ERROR: current transaction is aborted, commands ignored until end of transaction block
        this.connexion.setAutoCommit(false);
        this.connexion.rollback();
        StringBuilder requete = new StringBuilder();
        // Date date = new Date();
        // SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        for (int i = 0; i < tableDrop.length; i++) {
            requete.append("DROP TABLE IF EXISTS " + tableDrop[i] + ";");
        }

        // requete.append("UPDATE "+tablePil+"  SET phase_traitement='"+phaseAvant+"', etat_traitement='{"+TraitementEtat.OK+"}' ");
        requete.append("UPDATE " + tablePil + " set etape=2, etat_traitement= '{" + TraitementEtat.KO + "}', rapport='"
                + exception.toString().replace("'", "''").replaceAll("\r", "") + "' ");
        requete.append("WHERE phase_traitement='" + phase + "' AND etat_traitement='{" + TraitementEtat.ENCOURS + "}' ;");

        
        // requete.append("DELETE FROM " + tablePil + " ");
        // requete.append("	WHERE phase_traitement='" + phase + "' AND etat_traitement='{" + TraitementEtat.ENCOURS
        // + "}' ;");
        //
        //
        //
        // requete.append("INSERT INTO " + this.tableSuiviErreur + " (phase_traitement, date_evenement, message) VALUES ");
        // requete.append("('" + phase + "','" + formatter.format(date) + "'::timestamp,'"
        // + exception.toString().replace("'", "''").replaceAll("\r", "") + "');");
        // System.out.println("Exception : " + exception);
        UtilitaireDao.get(poolName).executeBlock(connexion, requete);
    }
    
    
    /**
     * Remise dans l'état juste avant le lancement des controles et insertion dans une table d'erreur
     *
     * @param connexion
     * @param phase
     * @param tablePil
     * @param exception
     * @param tableDrop
     * @throws SQLException
     */
    public void repriseSurErreur(Connection connexion, String phase, String tablePil,String idSource, Exception exception, String... tableDrop) throws SQLException {
        // nettoyage de la connexion
        // comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de la transaction
        // si on lui renvoie une requete SQL, il la refuse avec le message
        // ERROR: current transaction is aborted, commands ignored until end of transaction block
        this.connexion.setAutoCommit(false);
        this.connexion.rollback();
        StringBuilder requete = new StringBuilder();
        // Date date = new Date();
        // SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        for (int i = 0; i < tableDrop.length; i++) {
            requete.append("DROP TABLE IF EXISTS " + tableDrop[i] + ";");
        }

        // requete.append("UPDATE "+tablePil+"  SET phase_traitement='"+phaseAvant+"', etat_traitement='{"+TraitementEtat.OK+"}' ");
        requete.append("UPDATE " + tablePil + " set etape=2, etat_traitement= '{" + TraitementEtat.KO + "}', rapport='"
                + exception.toString().replace("'", "''").replaceAll("\r", "") + "' ");
        requete.append("WHERE phase_traitement='" + phase + "' AND etat_traitement='{" + TraitementEtat.ENCOURS + "}'"
        	+ "AND id_source = '"+idSource+"' ;");

        
        UtilitaireDao.get(poolName).executeBlock(connexion, requete);
    }

    /**
     * permet de récupérer un tableau de la forme id_source | id1 , id2, id3 ... type_comp | comp1,comp2, comp3 ...
     * 
     * @return
     * @throws SQLException
     */
    protected HashMap<String, ArrayList<String>> recuperationIdSource(String phaseTraiement) throws SQLException {
        HashMap<String, ArrayList<String>> pil = new GenericBean(UtilitaireDao.get(poolName).executeRequest(
                this.connexion,
                "SELECT p.id_source "
                + "\n \t FROM " + this.getTablePilTemp() + " p "
                + "\n \t order by id_source ;")).mapContent();

        return (pil);

    }   

    public String getEnvExecution() {
        return envExecution;
    }
    public void setEnvExecution(String envExecution) {
        this.envExecution = envExecution;
    }

    public HashMap<String, ArrayList<String>> getTabIdSource() {
        return tabIdSource;
    }

    protected void setTabIdSource(HashMap<String, ArrayList<String>> tabIdSource) {
        this.tabIdSource = tabIdSource;
    }

    public String getTablePil() {
        return tablePil;
    }

    public void setTablePil(String tablePil) {
        this.tablePil = tablePil;
    }

    public String getPreviousPhase() {
        return previousPhase;
    }

    public void setPreviousPhase(String previousPhase) {
        this.previousPhase = previousPhase;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }

    public String getTablePrevious() {
        return tablePrevious;
    }

    public void setTablePrevious(String tablePrevious) {
        this.tablePrevious = tablePrevious;
    }

    public String getParamBatch() {
        return paramBatch;
    }

    protected void setParamBatch(String paramBatch) {
        this.paramBatch = paramBatch;
    }

    public String getTableJeuDeRegle() {
        return tableJeuDeRegle;
    }

    public void setTableJeuDeRegle(String tableJeuDeRegle) {
        this.tableJeuDeRegle = tableJeuDeRegle;
    }

    public String getTableNorme() {
        return tableNorme;
    }

    public void setTableNorme(String tableNorme) {
        this.tableNorme = tableNorme;
    }

    public String getTableOutKo() {
        return tableOutKo;
    }

    public void setTableOutKo(String tableOutKo) {
        this.tableOutKo = tableOutKo;
    }

    public Exception getError() {
        return error;
    }

    public Thread getT() {
        return t;
    }

    public Connection getConnexion() {
        return connexion;
    }

    public String getTableControleRegle() {
        return tableControleRegle;
    }

    public String getTableChargementRegle() {
        return tableChargementRegle;
    }

    public void setTableChargementRegle(String tableChargementRegle) {
        this.tableChargementRegle = tableChargementRegle;
    }

    public void setTableControleRegle(String tableControleRegle) {
        this.tableControleRegle = tableControleRegle;
    }

    public String getTableMappingRegle() {
        return tableMappingRegle;
    }

    public void setTableMappingRegle(String tableMappingRegle) {
        this.tableMappingRegle = tableMappingRegle;
    }

    public Integer getNbEnr() {
        return nbEnr;
    }

    public void setNbEnr(Integer nbEnr) {
        this.nbEnr = nbEnr;
    }

    public String getTableNormageRegle() {
        return tableNormageRegle;
    }

    public void setTableNormageRegle(String tableNormageRegle) {
        this.tableNormageRegle = tableNormageRegle;
    }

    public String getTableSeuil() {
        return tableSeuil;
    }

    public void setTableSeuil(String tableSeuil) {
        this.tableSeuil = tableSeuil;
    }

    public String getDirectoryRoot() {
        return directoryRoot;
    }

    public void setDirectoryRoot(String directoryRoot) {
        this.directoryRoot = directoryRoot;
    }

    public String getTableFiltrageRegle() {
        return tableFiltrageRegle;
    }

    public void setTableFiltrageRegle(String tableFiltrageRegle) {
        this.tableFiltrageRegle = tableFiltrageRegle;
    }

    /**
     * @return the idSource
     */
    public String getIdSource() {
        return idSource;
    }

    /**
     * @param idSource the idSource to set
     */
    public void setIdSource(String idSource) {
        this.idSource = idSource;
    }


}
