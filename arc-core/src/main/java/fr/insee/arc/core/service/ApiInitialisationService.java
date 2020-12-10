package fr.insee.arc.core.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * ApiNormageService
 *
 * 1- Implémenter des maintenances sur la base de donnée </br> 2- Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
 * l'environnement d'excécution courant</br> 3- Gestion des fichiers en doublon</br> 4- Assurer la cohérence entre les table de données et
 * la table de pilotage de l'environnement qui fait foi</br> 5- Maintenance base de données</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiInitialisationService extends ApiService {
    public ApiInitialisationService() {
        super();
    }

    // nombre de fichier à traiter lors à chaque itération d'archivage
    private int NB_FICHIER_PER_ARCHIVE;
    
    // indique combien de jour doivent etre conservé les fichiers apres avoir été récupérés par le web service
    private int Nb_Jour_A_Conserver;

    private static final Logger LOGGER = LogManager.getLogger(ApiInitialisationService.class);
    public String tablePilTemp2;

    public ApiInitialisationService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot,
            Integer aNbEnr, String... paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
        this.tablePilTemp2 = FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + TraitementTableExecution.PILOTAGE_FICHIER, "2");
    }

    @Override
    public void executer() throws Exception {

        // Supprime les lignes devenues inutiles récupérées par le webservice de la table pilotage_fichier
        nettoyerTablePilotage(this.connexion, this.envExecution);

        // Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans l'environnement d'excécution courant
        copyTablesToExecutionThrow(connexion, envParameters, envExecution);
        
        // mettre à jour les tables métier avec les paramêtres de la famille de norme
        mettreAJourSchemaTableMetierThrow(connexion, envParameters, envExecution);

        // marque les fichiers ou les archives à rejouer
        reinstate(this.connexion, this.tablePil);

        // efface des fichiers de la table de pilotage
        cleanToDelete(this.connexion, this.tablePil);

        // Met en cohérence les table de données avec la table de pilotage de l'environnement
        // La table de pilotage fait foi
        synchroniserEnvironmentByPilotage(this.connexion, this.envExecution);

        // remettre les archives ou elle doivent etre en cas de restauration de la base
        rebuildFileSystem();

    }

    /**
     * remet le filesystem en etat en cas de restauration de la base
     *
     * @throws Exception
     */
    public void rebuildFileSystem() throws Exception {
        loggerDispatcher.info("rebuildFileSystem", LOGGER);

        // parcourir toutes les archives dans le répertoire d'archive
    	String repertoire = properties.getBatchParametersDirectory();
        String envDir = this.envExecution.replace(".", "_").toUpperCase();
        String nomTableArchive = dbEnv(envExecution) + "pilotage_archive";

        // pour chaque en trepot de données,
        // Comparer les archives du répertoire aux archives enregistrées dans la table d'archive :
        // comme la table d'archive serait dans l'ancien état de données
        // on peut remettre dans le repertoire de reception les archives qu'on ne retouvent pas dans la table

        if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot"))) {
            ArrayList<String> entrepotList = new GenericBean(UtilitaireDao.get("arc")
                    .executeRequest(null, new PreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent().get("id_entrepot");

            if (entrepotList!=null)
            {
            for (String s : entrepotList) {
            	String fullEnvDir = Paths.get(
                		repertoire, 
                		envDir).toString();
            	File envDirFile = new File(fullEnvDir);
            	makeDir(envDirFile);
            	
                String dirIn = Paths.get(
                		fullEnvDir,
                		TraitementPhase.RECEPTION + "_" + s + "_ARCHIVE").toString();
                String dirOut = Paths.get(
                		fullEnvDir,
                		TraitementPhase.RECEPTION + "_" + s).toString();

                // on itère sur les fichiers trouvé dans le répertoire d'archive
                File f = new File(dirIn);
                makeDir(f);
                
                File[] fichiers = f.listFiles();

                // on les insere dans une table temporaires t_files
                StringBuilder requete=new StringBuilder();
                requete.append("DROP TABLE IF EXISTS t_files; CREATE TEMPORARY TABLE t_files (fname text); ");
                
                boolean first=true;
                                
                for (File fichier : fichiers) {
                    if (!fichier.isDirectory()) {
	                	if (first || requete.length()>FormatSQL.TAILLE_MAXIMAL_BLOC_SQL)
	                	{
	                		UtilitaireDao.get("arc").executeImmediate(this.connexion, requete + ";");
	                        requete=new StringBuilder();
	                		requete.append("INSERT INTO t_files values ('"+fichier.getName().replace("'", "''")+"')");
	                		first=false;
	                	}
	                	else
	                	{
	                		requete.append(",('"+fichier.getName().replace("'", "''")+"')");
	                	}
                    }
                }
        		UtilitaireDao.get("arc").executeImmediate(this.connexion, requete + ";");

        		// On cherche les fichiers du répertoire d'archive qui ne sont pas dans la table archive
        		// Si on en trouve ce n'est pas cohérent et on doit remettre ces fichiers dans le répertoire de reception
        		// pour être rechargé
                PreparedStatementBuilder requete2=new PreparedStatementBuilder();
                requete2.append(" SELECT fname FROM t_files a ");
                requete2.append(" WHERE NOT EXISTS (SELECT * FROM " + nomTableArchive + " b WHERE b.nom_archive=a.fname) ");

                ArrayList<String> fileToBeMoved=new GenericBean(UtilitaireDao.get("arc").executeRequest(this.connexion,requete2)).mapContent().get("fname");
                
                if (fileToBeMoved!=null)
                {
	        		for (String fname:fileToBeMoved)
	        		{
	        			ApiReceptionService.deplacerFichier(dirIn, dirOut, fname, fname);
	        		}
                }
                
                
                // Traitement des # dans le repertoire de reception
                // on efface les # dont le fichier existe déjà avec un autre nom sans # ou un numéro # inférieur
                f = new File(dirOut);
                makeDir(f);
                
                fichiers = f.listFiles();
                
                for (File fichier : fichiers) {
                	String name_no_ext=ManipString.substringBeforeFirst(fichier.getName(),".");
                	String ext = "."+ ManipString.substringAfterFirst(fichier.getName(),".");

                	
                	if (name_no_ext.contains("#"))
                	{
                    	Integer number=ManipString.parseNumber(ManipString.substringAfterLast(name_no_ext,"#"));
                    	
                    	// c'est un fichier valide
                    	if (number!=null)
                    	{
                    		
                        	String name_source=ManipString.substringBeforeLast(name_no_ext,"#");

                    		// tester ce qu'on doit en faire
                        	
                        	// comparer au fichier sans index
                    		File autreFichier=new File (dirOut+ File.separator + name_source + ext);
                    		if (autreFichier.exists() && FileUtils.contentEquals(autreFichier, fichier))
                    		{
                    			fichier.delete();
                    		}
                    		
                    		// comparer aux fichier avec un index précédent
                    		for (int i=2;i<number;i++)
                    		{
                    			autreFichier=new File (dirOut+ File.separator + name_source + "#" + i + ext);
                        		
                        		if (autreFichier.exists() && FileUtils.contentEquals(autreFichier, fichier))
                        		{
                        			fichier.delete();
                        		}

                    		}
                    		
                    		
                    	}
                	}
                	
                	
                }
                
                
            }
            }
            
        }
    }

	private void makeDir(File f) {
		if (!f.exists())
		{
			f.mkdir();
		}
	}

    /**
     * Méthode pour implémenter des maintenances sur la base de donnée
     *
     * @param connexion
     * @throws Exception
     */
    public static void bddScript(String envExecution, Connection connexion) {

        String user = "arc";
        try {
            user = UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("select user "));
        } catch (SQLException ex) {
            LoggerHelper.errorGenTextAsComment(ApiInitialisationService.class, "bddScript()", LOGGER, ex);
        }

        StringBuilder requete = new StringBuilder();

        // création des tables si elles n'xistent pas
        requete.append("\n CREATE SCHEMA IF NOT EXISTS arc; ");
        
        // Création de la table de parametres
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.parameter ");
        requete.append("\n ( ");
        requete.append("\n key text, ");
        requete.append("\n val text, ");
        requete.append("\n CONSTRAINT parameter_pkey PRIMARY KEY (key) ");
        requete.append("\n ); ");
        
        // création des table de modalités IHM
        int n=UtilitaireDao.get("arc").getInt(null, new PreparedStatementBuilder("select count(*) from arc.ext_etat"));
        if (n>2)
        {
        	requete.append("\n DROP TABLE IF EXISTS arc.ext_etat; ");
        }
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_etat ");
        requete.append("\n ( ");
        requete.append("\n id text, ");
        requete.append("\n val text, ");
        requete.append("\n CONSTRAINT ext_etat_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_etat values ('0','INACTIF'),('1','ACTIF'); "));
        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_etat_jeuderegle ");
        requete.append("\n ( ");
        requete.append("\n id text NOT NULL, ");
        requete.append("\n val text, ");
        requete.append("\n isenv boolean, ");
        requete.append("\n mise_a_jour_immediate boolean, ");
        requete.append("\n CONSTRAINT ext_etat_jeuderegle_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");
    	requete.append(FormatSQL.tryQuery("\n DELETE FROM arc.ext_etat_jeuderegle where id='arc.bas';"));
    	
    	int nbSandboxes=BDParameters.getInt(null, "ApiInitialisationService.nbSandboxes",8);
    	
    	for (int i=1;i<=nbSandboxes;i++)
        {
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_etat_jeuderegle values ('arc.bas"+i+"','BAC A SABLE "+i+"','TRUE','TRUE');"));
        }
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_etat_jeuderegle values ('arc.prod','PRODUCTION','TRUE','FALSE');"));
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_etat_jeuderegle values ('inactif','INACTIF','FALSE','FALSE');"));


        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_mod_periodicite ");
        requete.append("\n ( ");
        requete.append("\n id text, ");
        requete.append("\n val text, ");
        requete.append("\n CONSTRAINT ext_mod_periodicite_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_mod_periodicite values ('M','MENSUEL'),('A','ANNUEL'); "));
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_mod_type_autorise ");
        requete.append("\n ( ");
        requete.append("\n  nom_type name NOT NULL, ");
        requete.append("\n  description_type text NOT NULL, ");
        requete.append("\n  CONSTRAINT pk_ext_mod_type_autorise PRIMARY KEY (nom_type) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_mod_type_autorise values ('bigint','Entier'),('bigint[]','Tableau d''entier long'),('boolean','Vrai (t ou true) ou faux (f ou false)'),('date','Date'),('date[]','Tableau de date'),('float','Nombre décimal virgule flottante'),('float[]','Tableau de nombre décimaux'),('interval','Durée (différence de deux dates)'),('text','Texte sans taille limite'),('text[]','Tableau de texte sans limite'),('timestamp without time zone','Date et heure'); "));

        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_type_controle ");
        requete.append("\n ( ");
        requete.append("\n   id text NOT NULL, ");
        requete.append("\n   ordre integer, ");
        requete.append("\n   CONSTRAINT ext_type_controle_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_controle values ('ALPHANUM','2'),('CARDINALITE','1'),('CONDITION','5'),('DATE','4'),('NUM','3'); "));
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_controle values ('REGEXP', '6');"));
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_controle values ('ENUM_BRUTE', '7');"));
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_controle values ('ENUM_TABLE', '8');"));
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_controle values ('STRUCTURE', '8');"));
        
        requete.append("\n DELETE FROM arc.ext_type_controle WHERE id='ALIAS';");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_type_fichier_chargement ");
        requete.append("\n ( ");
        requete.append("\n   id text NOT NULL, ");
        requete.append("\n   ordre integer, ");
        requete.append("\n   CONSTRAINT ext_type_fichier_chargement_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_fichier_chargement values ('xml','1'),('clef-valeur','2'),('plat','3'); "));
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_fichier_chargement values ('xml-complexe','4'); "));
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_type_normage ");
        requete.append("\n ( ");
        requete.append("\n   id text NOT NULL, ");
        requete.append("\n   ordre integer, ");
        requete.append("\n   CONSTRAINT ext_type_normage_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ext_type_normage values ('relation','1'),('cartesian','2'),('suppression','3'),('unicité','4'); "));

        
        // fonctions
        requete.append("\n CREATE OR REPLACE FUNCTION arc.update_list_param( ");
        requete.append("\n 	    n text, ");
        requete.append("\n dt text) ");
        requete.append("\n RETURNS boolean AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE p text; ");
        requete.append("\n BEGIN ");
        requete.append("\n begin ");
        requete.append("\n p:=current_setting(n); ");
        requete.append("\n exception when others then ");
        requete.append("\n perform set_config(n,';'||dt||';',true ); ");
        requete.append("\n return false; ");
        requete.append("\n end; ");
        requete.append("\n if (p='') then ");
        requete.append("\n perform set_config(n,';'||dt||';',true ); ");
        requete.append("\n return false; ");
        requete.append("\n end if; ");
        requete.append("\n if (p not like '%;'||dt||';%') then ");
        requete.append("\n perform set_config(n,p||dt||';',true ); ");
        requete.append("\n end if; ");
        requete.append("\n return true; ");
        requete.append("\n END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_famille ");
        requete.append("\n ( ");
        requete.append("\n   id_famille text NOT NULL, ");
        requete.append("\n   CONSTRAINT ihm_famille_pkey PRIMARY KEY (id_famille) ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_client ");
        requete.append("\n (id_famille text NOT NULL, ");
        requete.append("\n id_application text NOT NULL, ");
        requete.append("\n CONSTRAINT pk_ihm_client PRIMARY KEY (id_famille, id_application), ");
        requete.append("\n CONSTRAINT fk_client_famille FOREIGN KEY (id_famille) ");
        requete.append("\n REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ");
        requete.append("\n ON UPDATE NO ACTION ON DELETE NO ACTION ");
        requete.append("\n ); ");
        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_norme ");
        requete.append("\n ( ");
        requete.append("\n   id_norme text NOT NULL, ");
        requete.append("\n   periodicite text  NOT NULL, ");
        requete.append("\n   def_norme text  NOT NULL, ");
        requete.append("\n   def_validite text  NOT NULL, ");
        requete.append("\n   id serial NOT NULL, ");
        requete.append("\n   etat text , ");
        requete.append("\n   id_famille text , ");
        requete.append("\n   CONSTRAINT ihm_norme_pkey PRIMARY KEY (id_norme, periodicite), ");
        requete.append("\n   CONSTRAINT ihm_norme_id_famille_fkey FOREIGN KEY (id_famille) ");
        requete.append("\n       REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ");
        requete.append("\n       ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_calendrier ");
        requete.append("\n ( ");
        requete.append("\n   id_norme text NOT NULL, ");
        requete.append("\n   periodicite text NOT NULL, ");
        requete.append("\n   validite_inf date NOT NULL, ");
        requete.append("\n   validite_sup date NOT NULL, ");
        requete.append("\n   id serial NOT NULL, ");
        requete.append("\n   etat text, ");
        requete.append("\n   CONSTRAINT ihm_calendrier_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup), ");
        requete.append("\n   CONSTRAINT ihm_calendrier_norme_fkey FOREIGN KEY (id_norme, periodicite) ");
        requete.append("\n       REFERENCES arc.ihm_norme (id_norme, periodicite) MATCH SIMPLE ");
        requete.append("\n       ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_jeuderegle ");
        requete.append("\n ( ");
        requete.append("\n   id_norme text NOT NULL, ");
        requete.append("\n   periodicite text NOT NULL, ");
        requete.append("\n   validite_inf date NOT NULL, ");
        requete.append("\n   validite_sup date NOT NULL, ");
        requete.append("\n   version text NOT NULL, ");
        requete.append("\n   etat text, ");
        requete.append("\n   date_production date, ");
        requete.append("\n   date_inactif date, ");
        requete.append("\n   CONSTRAINT ihm_jeuderegle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version), ");
        requete.append("\n   CONSTRAINT ihm_jeuderegle_calendrier_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup) ");
        requete.append("\n       REFERENCES arc.ihm_calendrier (id_norme, periodicite, validite_inf, validite_sup) MATCH SIMPLE ");
        requete.append("\n       ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        requete.append(FormatSQL.tryQuery("\n UPDATE arc.ihm_jeuderegle set etat='arc.bas1' where etat='arc.bas'; "));

        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_chargement_regle ");
        requete.append("\n ( ");
        requete.append("\n id_regle bigint NOT NULL, ");
        requete.append("\n id_norme text NOT NULL, ");
        requete.append("\n validite_inf date NOT NULL, ");
        requete.append("\n validite_sup date NOT NULL, ");
        requete.append("\n version text NOT NULL, ");
        requete.append("\n periodicite text NOT NULL, ");
        requete.append("\n type_fichier text, ");
        requete.append("\n delimiter text, ");
        requete.append("\n format text, ");
        requete.append("\n commentaire text, ");
        requete.append("\n CONSTRAINT pk_ihm_chargement_regle PRIMARY KEY (id_regle, id_norme, validite_inf, validite_sup, version, periodicite), ");
        requete.append("\n CONSTRAINT ihm_chargement_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) ");
        requete.append("\n REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE ");
        requete.append("\n ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_normage_regle ");
        requete.append("\n ( ");
        requete.append("\n  id_norme text NOT NULL, ");
        requete.append("\n periodicite text NOT NULL, ");
        requete.append("\n validite_inf date NOT NULL, ");
        requete.append("\n validite_sup date NOT NULL, ");
        requete.append("\n version text NOT NULL, ");
        requete.append("\n  id_classe text NOT NULL, ");
        requete.append("\n rubrique text, ");
        requete.append("\n rubrique_nmcl text, ");
        requete.append("\n id_regle integer NOT NULL, ");
        requete.append("\n todo text, ");
        requete.append("\n commentaire text, ");
        requete.append("\n CONSTRAINT ihm_normage_regle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle), ");
        requete.append("\n CONSTRAINT ihm_normage_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) ");
        requete.append("\n       REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE ");
        requete.append("\n      ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_controle_regle ");
        requete.append("\n ( ");
        requete.append("\n   id_norme text NOT NULL, ");
        requete.append("\n   periodicite text NOT NULL, ");
        requete.append("\n   validite_inf date NOT NULL, ");
        requete.append("\n   validite_sup date NOT NULL, ");
        requete.append("\n   version text NOT NULL, ");
        requete.append("\n   id_classe text, ");
        requete.append("\n   rubrique_pere text, ");
        requete.append("\n   rubrique_fils text, ");
        requete.append("\n   borne_inf text, ");
        requete.append("\n   borne_sup text, ");
        requete.append("\n   condition text, ");
        requete.append("\n   pre_action text, ");
        requete.append("\n   id_regle integer NOT NULL, ");
        requete.append("\n   todo text, ");
        requete.append("\n   commentaire text, ");
        requete.append("\n   CONSTRAINT ihm_controle_regle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle), ");
        requete.append("\n   CONSTRAINT ihm_controle_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) ");
        requete.append("\n       REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE ");
        requete.append("\n       ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        requete.append(FormatSQL.tryQuery("\n ALTER TABLE arc.ihm_controle_regle ADD xsd_ordre int; "));
        requete.append(FormatSQL.tryQuery("\n ALTER TABLE arc.ihm_controle_regle ADD xsd_label_fils text; "));
        requete.append(FormatSQL.tryQuery("\n ALTER TABLE arc.ihm_controle_regle ADD xsd_role text; "));
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_filtrage_regle ");
        requete.append("\n ( ");
        requete.append("\n   id_regle bigint NOT NULL, ");
        requete.append("\n   id_norme text NOT NULL, ");
        requete.append("\n   validite_inf date NOT NULL, ");
        requete.append("\n   validite_sup date NOT NULL, ");
        requete.append("\n   version text NOT NULL, ");
        requete.append("\n   periodicite text NOT NULL, ");
        requete.append("\n   expr_regle_filtre text, ");
        requete.append("\n   commentaire text, ");
        requete.append("\n   CONSTRAINT pk_ihm_mapping_filtrage_regle PRIMARY KEY (id_regle, id_norme, validite_inf, validite_sup, version, periodicite), ");
        requete.append("\n CONSTRAINT fk_ihm_mapping_filtrage_regle_jeuderegle FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) ");
        requete.append("\n REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE ");
        requete.append("\n ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_mapping_regle ");
        requete.append("\n ( ");
        requete.append("\n id_regle bigint, ");
        requete.append("\n id_norme text NOT NULL, ");
        requete.append("\n validite_inf date NOT NULL, ");
        requete.append("\n validite_sup date NOT NULL, ");
        requete.append("\n version text NOT NULL, ");
        requete.append("\n periodicite text NOT NULL, ");
        requete.append("\n variable_sortie character varying(63) NOT NULL, ");
        requete.append("\n expr_regle_col text, ");
        requete.append("\n commentaire text, ");
        requete.append("\n CONSTRAINT pk_ihm_mapping_regle PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, variable_sortie), ");
        requete.append("\n CONSTRAINT fk_ihm_mapping_regle_jeuderegle FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) ");
        requete.append("\n REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE ");
        requete.append("\n ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");
        
        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_mod_table_metier ");
        requete.append("\n ( ");
        requete.append("\n id_famille text NOT NULL, ");
        requete.append("\n nom_table_metier text NOT NULL, ");
        requete.append("\n description_table_metier text, ");
        requete.append("\n CONSTRAINT pk_ihm_mod_table_metier PRIMARY KEY (id_famille, nom_table_metier), ");
        requete.append("\n CONSTRAINT fk_ihm_table_metier_famille FOREIGN KEY (id_famille) ");
        requete.append("\n REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ");
        requete.append("\n ON UPDATE NO ACTION ON DELETE NO ACTION ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_mod_variable_metier ");
        requete.append("\n ( ");
        requete.append("\n id_famille text NOT NULL, ");
        requete.append("\n nom_table_metier text NOT NULL, ");
        requete.append("\n nom_variable_metier text NOT NULL, ");
        requete.append("\n type_variable_metier name NOT NULL, ");
        requete.append("\n description_variable_metier text, ");
        requete.append("\n type_consolidation text, ");
        requete.append("\n CONSTRAINT pk_ihm_mod_variable_metier PRIMARY KEY (id_famille, nom_table_metier, nom_variable_metier), ");
        requete.append("\n CONSTRAINT fk_ihm_mod_variable_table_metier FOREIGN KEY (id_famille, nom_table_metier) ");
        requete.append("\n REFERENCES arc.ihm_mod_table_metier (id_famille, nom_table_metier) MATCH SIMPLE ");
        requete.append("\n ON UPDATE NO ACTION ON DELETE NO ACTION ");
        requete.append("\n ); ");

        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_nmcl ");
        requete.append("\n ( ");
        requete.append("\n nom_table text NOT NULL, ");
        requete.append("\n description text, ");
        requete.append("\n CONSTRAINT ihm_nmcl_pkey PRIMARY KEY (nom_table) ");
        requete.append("\n ); ");
        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_schema_nmcl ");
        requete.append("\n ( ");
        requete.append("\n type_nmcl text, ");
        requete.append("\n nom_colonne text, ");
        requete.append("\n type_colonne text ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_seuil ");
        requete.append("\n ( ");
        requete.append("\n nom text, ");
        requete.append("\n valeur numeric, ");
        requete.append("\n CONSTRAINT ihm_seuil_pkey PRIMARY KEY (nom) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ihm_seuil values ('filtrage_taux_exclusion_accepte',1.0) ,('s_taux_erreur',0.5); "));

        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_user ");
        requete.append("\n ( ");
        requete.append("\n idep text NOT NULL, ");
        requete.append("\n profil text, ");
        requete.append("\n CONSTRAINT ihm_user_pkey PRIMARY KEY (idep) ");
        requete.append("\n ); ");
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_entrepot ");
        requete.append("\n ( ");
        requete.append("\n   id_entrepot text NOT NULL, ");
        requete.append("\n   id_loader text, ");
        requete.append("\n   CONSTRAINT ihm_entrepot_pkey PRIMARY KEY (id_entrepot) ");
        requete.append("\n ); ");
        requete.append(FormatSQL.tryQuery("\n INSERT INTO arc.ihm_entrepot values ('DEFAULT','DEFAULT'); "));
        
        if (!envExecution.contains(".")) {
            requete.append("\n CREATE SCHEMA IF NOT EXISTS " + envExecution + " AUTHORIZATION " + user + "; ");
            requete.append("\n GRANT ALL ON SCHEMA " + envExecution + " TO " + user + "; ");
            requete.append("\n GRANT ALL ON SCHEMA " + envExecution + " TO public; ");
        }

        
        requete.append("\n CREATE TABLE IF NOT EXISTS "
                + dbEnv(envExecution)
                + "pilotage_fichier_t (date_entree text COLLATE pg_catalog.\"C\") "+FormatSQL.WITH_NO_VACUUM+"; ");
        requete.append("\n CREATE TABLE IF NOT EXISTS "
                + dbEnv(envExecution)
                + "pilotage_fichier (id_source text COLLATE pg_catalog.\"C\",  id_norme text COLLATE pg_catalog.\"C\",  validite text COLLATE pg_catalog.\"C\",  periodicite text COLLATE pg_catalog.\"C\",  phase_traitement text COLLATE pg_catalog.\"C\",  etat_traitement text[] COLLATE pg_catalog.\"C\",  date_traitement timestamp without time zone,  rapport text COLLATE pg_catalog.\"C\",  taux_ko numeric,  nb_enr integer,  nb_essais integer,  etape integer,  validite_inf date,  validite_sup date,  version text COLLATE pg_catalog.\"C\",  date_entree text,  container text COLLATE pg_catalog.\"C\",  v_container text COLLATE pg_catalog.\"C\",  o_container text COLLATE pg_catalog.\"C\",  to_delete text COLLATE pg_catalog.\"C\",  client text[],  date_client timestamp without time zone[],  jointure text, generation_composite text COLLATE pg_catalog.\"C\") "+FormatSQL.WITH_NO_VACUUM+"; ");
        requete.append(FormatSQL.tryQuery("\n ALTER TABLE "+ dbEnv(envExecution) + "pilotage_fichier ADD generation_composite text COLLATE pg_catalog.\"C\"; "));

        
        requete.append("\n CREATE TABLE IF NOT EXISTS "
                + dbEnv(envExecution)
                + "pilotage_archive (  entrepot text COLLATE pg_catalog.\"C\",  nom_archive text COLLATE pg_catalog.\"C\") "+FormatSQL.WITH_NO_VACUUM+"; ");

        requete.append("\n alter table " + dbEnv(envExecution)
                + "pilotage_fichier_t set (autovacuum_enabled = false, toast.autovacuum_enabled = false); ");
        requete.append("\n ");
        
        
        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_ws_context ");
        requete.append("\n ( ");
        requete.append("\n   service_name text NOT NULL, ");
        requete.append("\n   service_type integer, ");
        requete.append("\n call_id integer NOT NULL, ");
        requete.append("\n environment text, ");
        requete.append("\n target_phase text, ");
        requete.append("\n norme text, ");
        requete.append("\n validite text, ");
        requete.append("\n periodicite text, ");
        requete.append("\n CONSTRAINT ws_engine_context_pkey PRIMARY KEY (service_name, call_id) ");
        requete.append("\n ); ");


        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ihm_ws_query ");
        requete.append("\n ( ");
        requete.append("\n query_id integer NOT NULL, ");
        requete.append("\n query_name text NOT NULL, ");
        requete.append("\n expression text, ");
        requete.append("\n query_view integer, ");
        requete.append("\n service_name text NOT NULL, ");
        requete.append("\n call_id integer NOT NULL, ");
        requete.append("\n CONSTRAINT ws_engine_queries_pkey PRIMARY KEY (service_name, call_id, query_id), ");
        requete.append("\n CONSTRAINT ws_engine_queries_fkey FOREIGN KEY (service_name, call_id) ");
        requete.append("\n REFERENCES arc.ihm_ws_context (service_name, call_id) MATCH SIMPLE ");
        requete.append("\n ON UPDATE CASCADE ON DELETE CASCADE ");
        requete.append("\n ); ");


        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_webservice_type ");
        requete.append("\n ( ");
        requete.append("\n id text NOT NULL, ");
        requete.append("\n val text, ");
        requete.append("\n CONSTRAINT ext_webservice_type_pkey PRIMARY KEY (id) ");
        requete.append("\n ); ");

        requete.append("\n INSERT INTO arc.ext_webservice_type VALUES ('1','ENGINE'), ('2','SERVICE') ON CONFLICT DO NOTHING; ");

        requete.append("\n CREATE TABLE IF NOT EXISTS arc.ext_webservice_queryview ");
        requete.append("\n ( ");
        requete.append("\n id text NOT NULL, ");
        requete.append("\n val text, ");
        requete.append("\n CONSTRAINT ext_webservice_queryview_pkey PRIMARY KEY (id) ");
        requete.append("\n );");

        requete.append("\n INSERT INTO arc.ext_webservice_queryview VALUES ('1','COLUMN'), ('2','LINE') ON CONFLICT DO NOTHING; ");
        
        // script fonctions
        
        // fonction pour pouvoir exporter simplement des tables sans préciser le schéma de façon annexe
        requete.append("\n CREATE OR REPLACE FUNCTION arc.export_model (tname text) "); 
        requete.append("\n RETURNS TABLE ( ");
        requete.append("\n varbdd text ");
        requete.append("\n ,pos int ");
        requete.append("\n ) ");
        requete.append("\n AS $$ ");
        requete.append("\n BEGIN ");
        requete.append("\n RETURN QUERY select distinct column_name::text as varbdd, ordinal_position::int as pos from information_schema.columns where case when tname like '%.%' then table_schema||'.'||table_name else table_name end=tname; ");
        requete.append("\n END; $$ ");
        requete.append("\n LANGUAGE 'plpgsql'; ");
        
        
        requete.append("\n DROP FUNCTION IF EXISTS arc.finalize_todo(); ");
        requete.append("\n ");

        requete.append("\n CREATE OR REPLACE FUNCTION arc.transpose_pilotage_calcul() ");
        requete.append("\n RETURNS trigger AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE dt text; ");
        requete.append("\n DECLARE b boolean; ");
        requete.append("\n DECLARE c integer; ");
        requete.append("\n BEGIN ");
        requete.append("\n ");
        requete.append("\n if (TG_OP='UPDATE') then ");
        requete.append("\n if (old.phase_traitement=new.phase_traitement and old.etat_traitement=new.etat_traitement) then ");
        requete.append("\n return null; ");
        requete.append("\n end if; ");
        requete.append("\n end if; ");
        requete.append("\n ");
        requete.append("\n if (TG_OP in ('UPDATE','DELETE')) then ");
        requete.append("\n dt:='p.'||old.date_entree||'_'||old.phase_traitement||'_'||array_to_string(old.etat_traitement,'$'); ");
        requete.append("\n b:=arc.update_list_param('p.pilotage',dt); ");
        requete.append("\n begin ");
        requete.append("\n if (current_setting(dt)='') then c=-1; else c:=current_setting(dt)::integer-1; end if; ");
        requete.append("\n exception when others then  ");
        requete.append("\n c:=-1; ");
        requete.append("\n end; ");
        requete.append("\n perform set_config(dt,c::text,true ); ");
        requete.append("\n end if; ");
        requete.append("\n ");
        requete.append("\n if (TG_OP in ('UPDATE','INSERT')) then ");
        requete.append("\n dt:='p.'||new.date_entree||'_'||new.phase_traitement||'_'||array_to_string(new.etat_traitement,'$'); ");
        requete.append("\n b:=arc.update_list_param('p.pilotage',dt); ");
        requete.append("\n begin ");
        requete.append("\n if (current_setting(dt)='') then c=1; else c:=current_setting(dt)::integer+1; end if; ");
        requete.append("\n exception when others then  ");
        requete.append("\n c:=1; ");
        requete.append("\n end; ");
        requete.append("\n perform set_config(dt,c::text,true); ");
        requete.append("\n end if; ");
        requete.append("\n ");
        requete.append("\n return null; ");
        requete.append("\n ");
        requete.append("\n END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");
        
        requete.append("\n CREATE OR REPLACE FUNCTION arc.transpose_pilotage_fin() ");
        requete.append("\n RETURNS trigger AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE bac text; ");
        requete.append("\n DECLARE env text; ");
        requete.append("\n DECLARE query text; ");
        requete.append("\n DECLARE cols text[]; ");
        requete.append("\n DECLARE cols_t text[]; ");
        requete.append("\n DECLARE date_entree text; ");
        requete.append("\n DECLARE dt text; ");
        requete.append("\n declare b boolean:=false; ");
        requete.append("\n BEGIN ");
        requete.append("\n query:=''; ");
        requete.append("\n ");
        requete.append("\n -- récupération des données d'environnement ");
        requete.append("\n if (strpos(TG_TABLE_SCHEMA,'_')=0) then ");
        requete.append("\n bac:=substr(TG_TABLE_NAME,1,strpos(TG_TABLE_NAME,'_')); ");
        requete.append("\n env:=TG_TABLE_SCHEMA||'.'||bac; ");
        requete.append("\n else ");
        requete.append("\n bac:=''; ");
        requete.append("\n env:=TG_TABLE_SCHEMA||'.'; ");
        requete.append("\n end if; ");
        requete.append("\n ");
        requete.append("\n ");
        requete.append("\n begin ");
        requete.append("\n cols:=string_to_array(trim(current_setting('p.pilotage'),';'),';'); ");
        requete.append("\n exception when others then ");
        requete.append("\n return null; ");
        requete.append("\n end; ");
        requete.append("\n ");
        requete.append("\n if (current_setting('p.pilotage')='') then return null; end if; ");
        requete.append("\n ");
        requete.append("\n ");
        requete.append("\n -- récupération des colonnes de la table de pilotage_t  ");
        requete.append("\n SELECT array_agg(column_name::text) into cols_t ");
        requete.append("\n FROM   information_schema.columns ");
        requete.append("\n WHERE  table_schema = TG_TABLE_SCHEMA ");
        requete.append("\n AND    table_name = bac||'pilotage_fichier_t'; ");
        requete.append("\n ");
        requete.append("\n --raise notice '%',cols_t; ");
        requete.append("\n -- créer la table de pilotage si elle n'existe pas ");
        requete.append("\n if (cols_t is null) then ");
        requete.append("\n query:=query||' ");
        requete.append("\n create table '||env||'pilotage_fichier_t(date_entree text) "+FormatSQL.WITH_NO_VACUUM+";'; ");
        requete.append("\n cols_t=array_append(cols_t, 'date_entree'); ");
        requete.append("\n end if; ");
        requete.append("\n ");
        requete.append("\n -- création de la requete qui va mettre à jour la table pilotage_t  ");
        requete.append("\n for i in 1..array_length(cols, 1) loop  ");
        requete.append("\n --raise notice '%',cols[i]; ");
        requete.append("\n date_entree:=substr(cols[i],3,13); ");
        requete.append("\n dt:=substr(cols[i],17); ");
        requete.append("\n ");
        requete.append("\n -- si la colonne n'existe pas dans la table pilotage_t, on la crée  ");
        requete.append("\n if (not(lower(dt)= ANY (cols_t))) then ");
        requete.append("\n query:=query||' ");
        requete.append("\n alter table '||env||'pilotage_fichier_t add column '||dt||' integer default 0;'; ");
        requete.append("\n cols_t=array_append(cols_t, lower(dt)); end if; ");
        requete.append("\n ");
        requete.append("\n if (current_setting(cols[i])::integer!=0) then  ");
        requete.append("\n query:=query||'WITH upsert AS (update '||env||'pilotage_fichier_t set '||dt||'='||dt||'+'||current_setting(cols[i])||'  ");
        requete.append("\n where date_entree='''||date_entree||''' returning *)  ");
        requete.append("\n INSERT INTO '||env||'pilotage_fichier_t (date_entree, '||dt||') SELECT '''||date_entree||''', '||current_setting(cols[i])||'  ");
        requete.append("\n WHERE NOT EXISTS (SELECT * FROM upsert); ';  ");
        requete.append("\n b:=true;  ");
        requete.append("\n perform set_config(cols[i],'0',true );  ");
        requete.append("\n end if; ");
        requete.append("\n ");
        requete.append("\n end loop; ");
        requete.append("\n ");
        requete.append("\n if (b) then ");
        requete.append("\n query:=query||'delete from '||env||'pilotage_fichier_t where 0'; for i in 1..array_length(cols_t,1) loop ");
        requete.append("\n if (cols_t[i]!='date_entree') then query:=query||'+coalesce('||cols_t[i]||',0)'; end if; end loop; query:=query||'=0;'; end if; ");
        requete.append("\n ");
        requete.append("\n --raise notice '%',query; ");
        requete.append("\n execute query; ");
        requete.append("\n ");
        requete.append("\n return null; ");
        requete.append("\n END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");
        
        requete.append("\n do $$ begin create type public.cle_valeur as (i bigint, v text collate \"C\"); exception when others then end; $$; ");
        requete.append("\n CREATE OR REPLACE FUNCTION public.distinct_on_array(public.cle_valeur[]) ");
        requete.append("\n   RETURNS text[] AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n select array_agg(v) from (select distinct m.i, m.v from unnest($1) m where m.i is not null order by m.i, m.v ) t0 ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE sql IMMUTABLE STRICT ");
        requete.append("\n COST 100; ");

        requete.append("\n do $$ begin CREATE SEQUENCE arc.number_generator cycle; exception when others then end; $$; ");
        requete.append("\n CREATE OR REPLACE FUNCTION public.curr_val(text) ");
        requete.append("\n   RETURNS bigint AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n BEGIN ");
        requete.append("\n return currval($1); ");
        requete.append("\n exception when others then ");
        requete.append("\n return nextval($1); ");
        requete.append("\n END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");
        
        requete.append("\n CREATE OR REPLACE FUNCTION arc.fn_check_calendrier() ");
        requete.append("\n RETURNS boolean AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE ");
        requete.append("\n n integer; ");
        requete.append("\n BEGIN  ");
        requete.append("\n select count(1) into n from arc.ihm_calendrier a ");
        requete.append("\n where exists (select 1 from arc.ihm_calendrier b ");
        requete.append("\n where a.validite_inf>=b.validite_inf  ");
        requete.append("\n and a.validite_inf<=b.validite_sup  ");
        requete.append("\n and a.id_norme=b.id_norme  ");
        requete.append("\n and a.periodicite=b.periodicite ");
        requete.append("\n and a.etat='1' ");
        requete.append("\n and a.etat=b.etat ");
        requete.append("\n and a.id<>b.id); ");
        requete.append("\n       	if n>0 then  ");
        requete.append("\n RAISE EXCEPTION 'Chevauchement de calendrier'; ");
        requete.append("\n end if; ");
        requete.append("\n select count(1) into n from arc.ihm_calendrier a ");
        requete.append("\n where a.validite_inf>a.validite_sup; ");
        requete.append("\n       	if n>0 then  ");
        requete.append("\n RAISE EXCEPTION 'Intervalle non valide. Date inf >= Date sup'; ");
        requete.append("\n end if; ");
        requete.append("\n return true; ");
        requete.append("\n END;  ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");
        
        
        requete.append("\n CREATE OR REPLACE FUNCTION arc.fn_check_jeuderegle() ");
        requete.append("\n RETURNS boolean AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE ");
        requete.append("\n   n integer; ");
        requete.append("\n BEGIN  ");
        requete.append("\n SELECT count(1) into n ");
        requete.append("\n FROM 	( ");
        requete.append("\n SELECT id_norme, periodicite, validite_inf, validite_sup, etat, count(etat) ");
        requete.append("\n FROM arc.ihm_jeuderegle  b ");
        requete.append("\n WHERE b.etat != 'inactif' ");
        requete.append("\n GROUP BY id_norme, periodicite, validite_inf, validite_sup, etat ");
        requete.append("\n HAVING count(etat)>1 ");
        requete.append("\n ) AS foo; ");
        requete.append("\n if n>0 then  ");
        requete.append("\n RAISE EXCEPTION 'Un seul jeu de règle en production ou par bac à sable pour un calendrier'; ");
        requete.append("\n end if; ");
        requete.append("\n return true; ");
        requete.append("\n END;  ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");
        
        
        requete.append("\n CREATE OR REPLACE FUNCTION arc.isdate( ");
        requete.append("\n text, ");
        requete.append("\n text) ");
        requete.append("\n RETURNS boolean AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n BEGIN ");
        requete.append("\n IF TO_CHAR(TO_TIMESTAMP($1,$2), $2) != $1 THEN ");
        requete.append("\n RETURN FALSE; ");
        requete.append("\n END IF; ");
        requete.append("\n RETURN TRUE; ");
        requete.append("\n EXCEPTION WHEN others THEN ");
        requete.append("\n RETURN FALSE; ");
        requete.append("\n END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql IMMUTABLE ");
        requete.append("\n COST 100; ");

        
        requete.append("\n CREATE OR REPLACE FUNCTION arc.verif_doublon() ");
        requete.append("\n RETURNS trigger AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE ");
        requete.append("\n n integer; ");
        requete.append("\n k integer; ");
        requete.append("\n nom_table text := quote_ident(TG_TABLE_SCHEMA) || '.'|| quote_ident(TG_TABLE_NAME); ");
        requete.append("\n query text; ");
        requete.append("\n BEGIN  ");
        requete.append("\n --RAISE NOTICE 'Nom de la table : %', nom_table; ");
        requete.append("\n query := 'SELECT count(1) FROM (SELECT count(1)	FROM '|| nom_table ||' b WHERE id_classe=''CARDINALITE'' ");
        requete.append("\n GROUP BY id_norme, periodicite, validite_inf, validite_sup, version, lower(rubrique_pere), lower(rubrique_fils), condition ");
        requete.append("\n HAVING count(1)>1) AS foo'; ");
        requete.append("\n --RAISE NOTICE 'Query vaut : %', query; ");
        requete.append("\n EXECUTE query INTO n; ");
        requete.append("\n query := 'SELECT count(1) FROM 	(SELECT count(1) FROM '|| nom_table ||' b WHERE id_classe IN(''NUM'',''DATE'',''ALPHANUM'',''REGEXP'') ");
        requete.append("\n GROUP BY id_norme, periodicite, validite_inf, validite_sup, version, lower(rubrique_pere), condition ");
        requete.append("\n HAVING count(1)>1) AS foo'; ");
        requete.append("\n --RAISE NOTICE 'Query vaut : %', query; ");
        requete.append("\n EXECUTE query INTO k; ");
        requete.append("\n RAISE NOTICE 'Variable de comptage n : %', n; ");
        requete.append("\n RAISE NOTICE 'Variable de comptage k : %', k; ");
        requete.append("\n if n>0 then  ");
        requete.append("\n RAISE EXCEPTION 'Règles de CARDINALITE en doublon'; ");
        requete.append("\n end if; ");
        requete.append("\n if k>0 then  ");
        requete.append("\n RAISE EXCEPTION 'Règles de TYPE en doublon'; ");
        requete.append("\n end if; ");
        requete.append("\n RETURN NEW; ");
        requete.append("\n END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");

        requete.append("\n CREATE OR REPLACE FUNCTION arc.insert_controle() ");
        requete.append("\n RETURNS trigger AS ");
        requete.append("\n $BODY$ ");
        requete.append("\n DECLARE i integer; ");
        requete.append("\n nom_table text := quote_ident(TG_TABLE_SCHEMA) || '.'|| quote_ident(TG_TABLE_NAME); ");
        requete.append("\n query text; ");
        requete.append("\n   BEGIN ");
        requete.append("\n if (new.id_regle is null) then ");
        requete.append("\n query:='select coalesce(max(id_regle),0)+1 from '||nom_table||' ");
        requete.append("\n where id_norme='''||NEW.id_norme||'''  ");
        requete.append("\n and periodicite='''||NEW.periodicite||'''  ");
        requete.append("\n and validite_inf='''||NEW.validite_inf||'''  ");
        requete.append("\n and validite_sup='''||NEW.validite_sup||'''   ");
        requete.append("\n and version='''||NEW.version||''' '; ");
        requete.append("\n EXECUTE query INTO i; ");
        requete.append("\n NEW.id_regle:=i; ");
        requete.append("\n end if; ");
        requete.append("\n RETURN NEW; ");
        requete.append("\n   END; ");
        requete.append("\n $BODY$ ");
        requete.append("\n LANGUAGE plpgsql VOLATILE ");
        requete.append("\n COST 100; ");

        
        requete.append(FormatSQL.tryQuery("CREATE TRIGGER tg_insert_chargement BEFORE INSERT ON arc.ihm_chargement_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle();"));
        requete.append(FormatSQL.tryQuery("CREATE TRIGGER tg_insert_controle BEFORE INSERT ON arc.ihm_controle_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle();"));
        requete.append(FormatSQL.tryQuery("CREATE TRIGGER tg_insert_filtrage BEFORE INSERT ON arc.ihm_filtrage_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle();"));
        
        
        
        
        try {
            UtilitaireDao.get(poolName).executeBlock(connexion, requete);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        
        ArrayList<String> lTable = new ArrayList<String>(Arrays.asList(
        		 dbEnv(envExecution) + TraitementPhase.CHARGEMENT + "_" + TraitementEtat.OK
        		 ,dbEnv(envExecution) + TraitementPhase.NORMAGE + "_" + TraitementEtat.OK
        		 ,dbEnv(envExecution) + TraitementPhase.NORMAGE + "_" + TraitementEtat.KO
        		 ,dbEnv(envExecution) + TraitementPhase.CONTROLE + "_" + TraitementEtat.OK
        		 ,dbEnv(envExecution) + TraitementPhase.CONTROLE + "_" + TraitementEtat.KO
        		 ,dbEnv(envExecution) + TraitementPhase.FILTRAGE + "_" + TraitementEtat.OK
        		 ,dbEnv(envExecution) + TraitementPhase.FILTRAGE + "_" + TraitementEtat.KO
        		 ,dbEnv(envExecution) + TraitementPhase.MAPPING + "_" + TraitementEtat.KO
        		 ,dbEnv(envExecution) + TraitementPhase.CHARGEMENT + "_" + TraitementEtat.OK + "_TODO"
        		 ,dbEnv(envExecution) + TraitementPhase.NORMAGE + "_" + TraitementEtat.OK + "_TODO"
        		 ,dbEnv(envExecution) + TraitementPhase.CONTROLE + "_" + TraitementEtat.OK + "_TODO"
        		 ,dbEnv(envExecution) + TraitementPhase.FILTRAGE + "_" + TraitementEtat.OK + "_TODO"
        		))
        		;
        
        String dataDef="(id_source text COLLATE pg_catalog.\"C\", id integer, date_integration text COLLATE pg_catalog.\"C\", id_norme text COLLATE pg_catalog.\"C\",  periodicite text COLLATE pg_catalog.\"C\", validite text COLLATE pg_catalog.\"C\")"+FormatSQL.WITH_NO_VACUUM+";";
        
		 	HashMap<String, ArrayList<String>> m;
			try {

				// si les tables sont vide, on les recrée
				for (String t:lTable)
				{
					UtilitaireDao.get(poolName).executeImmediate(connexion, "CREATE TABLE IF NOT EXISTS "+ t + dataDef);
					
					if (!UtilitaireDao.get(poolName).hasResults(connexion, new PreparedStatementBuilder("select 1 FROM "+t+" limit 1")))
					{
						UtilitaireDao.get(poolName).executeImmediate(connexion, "DROP TABLE IF EXISTS "+ t +" CASCADE; CREATE TABLE IF NOT EXISTS "+ t + dataDef);
					}
				}

      		 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    /**
     * Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans l'environnement d'excécution courant
     * @throws Exception
     */
    public static void synchroniserSchemaExecution(Connection connexion, String envParameters, String envExecution) {
        copyTablesToExecution(connexion, envParameters, envExecution);
        mettreAJourSchemaTableMetier(connexion, envParameters, envExecution);
    }
    
    /**
     * Méthode pour rejouer des fichiers
     *
     * @param connexion
     * @param tablePil
     * @throws SQLException
     */
    public void reinstate(Connection connexion, String tablePil) throws Exception {
        loggerDispatcher.info("reinstateWithRename", LOGGER);

        // on cherche tous les containers contenant un fichier à rejouer
        // on remet l'archive à la racine

        PreparedStatementBuilder requete = new PreparedStatementBuilder();

        ArrayList<String> containerList = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
        		new PreparedStatementBuilder("select distinct container from " + tablePil + " where to_delete in ('R','RA')"))).mapContent().get("container");

        if (containerList != null) {
        	String repertoire = properties.getBatchParametersDirectory();
            String envDir = this.envExecution.replace(".", "_").toUpperCase();

            for (String s : containerList) {

                String entrepot = ManipString.substringBeforeFirst(s, "_");
                String archive = ManipString.substringAfterFirst(s, "_");

                String dirIn = repertoire + envDir + File.separator + TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE";
                String dirOut = repertoire + envDir + File.separator + TraitementPhase.RECEPTION + "_" + entrepot;

                ApiReceptionService.deplacerFichier(dirIn, dirOut, archive, archive);

            }

        }

        // effacer les archives marquées en RA
        UtilitaireDao.get("arc").executeImmediate(
                connexion,
                "DELETE FROM " + this.tablePil + " a where exists (select 1 from " + this.tablePil
                        + " b where a.container=b.container and b.to_delete='RA')");

    }

    /**
     * Créer ou detruire les colonnes ou les tables métiers en comparant ce qu'il y a en base à ce qu'il y a de déclaré dans la table des
     * familles de norme
     *
     * @param connexion
     * @throws Exception
     */
    
    public static void mettreAJourSchemaTableMetier(Connection connexion, String envParameters, String envExecution) {
    	try{
            StaticLoggerDispatcher.info("Mettre à jour le schéma des tables métiers avec la famille", LOGGER);
            mettreAJourSchemaTableMetierThrow(connexion, envParameters, envExecution);
    	} catch (Exception e)
    	{
    		StaticLoggerDispatcher.info("Erreur mettreAJourSchemaTableMetier", LOGGER);
            e.printStackTrace();
    	}
    	
    }

    
    public static void mettreAJourSchemaTableMetierThrow(Connection connexion, String envParameters, String envExecution) throws Exception {
        try {
        	StaticLoggerDispatcher.info("mettreAJourSchemaTableMetier", LOGGER);
            /*
             * Récupérer la table qui mappe : famille / table métier / variable métier et type de la variable
             */
        	PreparedStatementBuilder requeteRef = new PreparedStatementBuilder();
        	requeteRef.append("SELECT lower(id_famille), lower('" + ApiService.dbEnv(envExecution)
                    + "'||nom_table_metier), lower(nom_variable_metier), lower(type_variable_metier) FROM " + envParameters + "_mod_variable_metier");

            List<List<String>> relationalViewRef = Format.patch(UtilitaireDao.get(poolName).executeRequestWithoutMetadata(connexion, requeteRef));
            HierarchicalView familleToTableToVariableToTypeRef = HierarchicalView.asRelationalToHierarchical(
                    "(Réf) Famille -> Table -> Variable -> Type",
                    Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"), relationalViewRef);
            /*
             * Récupérer dans le méta-modèle de la base les tables métiers correspondant à la famille chargée
             */
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("SELECT lower(id_famille), lower(table_schema||'.'||table_name) nom_table_metier, lower(column_name) nom_variable_metier");
            
            // les types dans postgres sont horribles :(
            // udt_name : float8 = float, int8=bigint, int4=int
            // data_type :  double precision = float, integer=int
            requete.append(", case when lower(data_type)='array' then replace(replace(replace(ltrim(udt_name,'_'),'int4','int'),'int8','bigint'),'float8','float')||'[]' ");
            requete.append("	else replace(replace(lower(data_type),'double precision','float'),'integer','int') end type_variable_metier ");
            requete.append("\n FROM information_schema.columns, " + envParameters + "_famille ");
            requete.append("\n WHERE table_schema='" + ManipString.substringBeforeFirst(ApiService.dbEnv(envExecution), ".").toLowerCase() + "' ");
            requete.append("\n and table_name LIKE '" + ManipString.substringAfterFirst(ApiService.dbEnv(envExecution), ".").toLowerCase()
                    + "mapping\\_%' ");
            requete.append("\n and table_name LIKE '" + ManipString.substringAfterFirst(ApiService.dbEnv(envExecution), ".").toLowerCase()
                    + "mapping\\_'||lower(id_famille)||'\\_%';");

            List<List<String>> relationalView = Format.patch(UtilitaireDao.get(poolName).executeRequestWithoutMetadata(connexion, requete));

            HierarchicalView familleToTableToVariableToType = HierarchicalView.asRelationalToHierarchical(
                    "(Phy) Famille -> Table -> Variable -> Type",
                    Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"), relationalView);
            StringBuilder requeteMAJSchema = new StringBuilder("BEGIN;\n");
            /*
             * AJOUT/MODIFICATION DES COLONNES DE REFERENCE
             */
            for (HierarchicalView famille : familleToTableToVariableToTypeRef.children()) {
                /**
                 * Pour chaque table de référence
                 */
                for (HierarchicalView table : famille.children()) {
                    /**
                     * Est-ce que la table existe physiquement ?
                     */
                    if (familleToTableToVariableToType.hasPath(famille, table)) {
                        /**
                         * Pour chaque variable de référence
                         */
                        for (HierarchicalView variable : table.children()) {
                            /*
                             * Si la variable*type n'existe pas
                             */
                            if (!familleToTableToVariableToType.hasPath(famille, table, variable, variable.getUniqueChild())) {
                            	
                            	// BUG POSTGRES : pb drop et add column : recréer la table sinon ca peut excéder la limite postgres de 1500
                            	requeteMAJSchema.append("DROP TABLE IF EXISTS "+table.getLocalRoot()+"_IMG ;");
                            	requeteMAJSchema.append("CREATE TABLE "+table.getLocalRoot()+"_IMG "+FormatSQL.WITH_NO_VACUUM+" AS SELECT * FROM "+table.getLocalRoot()+";");
                            	requeteMAJSchema.append("DROP TABLE IF EXISTS "+table.getLocalRoot()+" ;");
                            	requeteMAJSchema.append("ALTER TABLE "+table.getLocalRoot()+"_IMG RENAME TO "+ManipString.substringAfterFirst(table.getLocalRoot(),".")+";");
                            	
                                /*
                                 * Si la variable existe
                                 */
                                if (familleToTableToVariableToType.hasPath(famille, table, variable)) {
                                    /*
                                     * Drop de la variable
                                     */
                                    requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " DROP COLUMN " + variable.getLocalRoot() + ";");
                                }
                                /*
                                 * Ajout de la variable
                                 */
                                requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " ADD COLUMN " + variable.getLocalRoot() + " "
                                        + variable.getUniqueChild().getLocalRoot() + " ");
                                if (variable.getUniqueChild().getLocalRoot().equals("text")) {
                                    requeteMAJSchema.append(" collate \"C\" ");
                                }
                                requeteMAJSchema.append(";");
                                
                            }
                        }
                    } else {
                        AttributeValue[] attr = new AttributeValue[table.children().size()];
                        int i = 0;
                        for (HierarchicalView variable : table.children()) {
                            attr[i++] = new AttributeValue(variable.getLocalRoot(), variable.getUniqueChild().getLocalRoot());
                        }
                        requeteMAJSchema.append("CREATE TABLE " + table.getLocalRoot() + " (");
                        for (int j = 0; j < attr.length; j++) {
                            if (j > 0) {
                                requeteMAJSchema.append(", ");
                            }
                            requeteMAJSchema.append(attr[j].getFirst() + " " + attr[j].getSecond());
                            if (attr[j].getSecond().equals("text")) {
                                requeteMAJSchema.append(" collate \"C\" ");
                            }
                        }
                        requeteMAJSchema.append(") "+FormatSQL.WITH_NO_VACUUM+";\n");
                    }

                }
            }
            /*
             * SUPPRESSION DES COLONNES QUI NE SONT PAS CENSEES EXISTER
             */
            for (HierarchicalView famille : familleToTableToVariableToType.children()) {
                /**
                 * Pour chaque table physique
                 */
                for (HierarchicalView table : familleToTableToVariableToType.get(famille).children()) {
                    /**
                     * Est-ce que la table devrait exister ?
                     */
                    if (!familleToTableToVariableToTypeRef.hasPath(famille, table)) {
                        requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + ";\n");
                    } else {
                        /**
                         * Pour chaque variable de cette table
                         */
                        for (HierarchicalView variable : table.children()) {
                            /**
                             * Est-ce que la variable devrait exister ?
                             */
                            if (!familleToTableToVariableToTypeRef.hasPath(famille, table, variable)) {
								requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " DROP COLUMN " + variable.getLocalRoot() + ";\n");
							}
                        }
                    }
                }
            }
            requeteMAJSchema.append("END;");
            UtilitaireDao.get("arc").executeBlock(connexion, requeteMAJSchema);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Suppression dans la table de pilotage des fichiers qui ont été marqué par la MOA (via la colonne to_delete de la table de pilotage);
     *
     * @param connexion
     * @param tablePil
     * @throws Exception
     */
    public void cleanToDelete(Connection connexion, String tablePil) throws Exception {
        loggerDispatcher.info("cleanToDelete", LOGGER);

        StringBuilder requete = new StringBuilder();
        requete.append("DELETE FROM " + tablePil + " a WHERE exists (select 1 from " + tablePil + " b where b.to_delete='1' and a.id_source=b.id_source and a.container=b.container); ");
        UtilitaireDao.get("arc").executeBlock(connexion, requete);
    }

    /**
     * Suppression dans la table de pilotage des fichiers inutils. r.g :- une copie des données du fichier doit être envoyée à chacun de ses
     * clients RG1; - pour un fichier donné, l'ancienneté de son dernier transfert doit dépasser 7 jours RG2.
     *
     * @param connexion
     * @param tablePil
     * @param tablePil
     * @throws SQLException
     */
    public void nettoyerTablePilotage(Connection connexion, String envExecution) throws Exception {

        loggerDispatcher.info("nettoyerTablePilotage", LOGGER);
        
        Nb_Jour_A_Conserver = BDParameters.getInt(this.connexion, "ApiInitialisationService.Nb_Jour_A_Conserver",365);
        
        NB_FICHIER_PER_ARCHIVE = BDParameters.getInt(this.connexion, "ApiInitialisationService.NB_FICHIER_PER_ARCHIVE",10000);

        String nomTablePilotage = dbEnv(envExecution) + "pilotage_fichier";
        String nomTableArchive = dbEnv(envExecution) + "pilotage_archive";

        PreparedStatementBuilder requete;
        
        requete = new PreparedStatementBuilder();
        
        requete.append("DROP TABLE IF EXISTS fichier_to_delete; ");
        requete.append("CREATE TEMPORARY TABLE fichier_to_delete AS ");
        requete.append("WITH ")

                // 1. on récupère sous forme de tableau les clients de chaque famille
                .append("clientsParFamille AS ( ")
                .append("SELECT array_agg(id_application) as client, id_famille ")
                .append("FROM arc.ihm_client ")
                .append("GROUP BY id_famille ")
                .append(") ")

                // 2. on fait une première selection des fichiers candidats au Delete
                .append(",isFichierToDelete AS (	 ")
                .append("SELECT id_source, container, date_client ")
                .append("FROM ")
                .append(nomTablePilotage)
                .append(" a ")
                .append(", arc.ihm_norme b ")
                .append(", clientsParFamille c ")
                .append("WHERE a.phase_traitement='" + TraitementPhase.MAPPING + "' ")
                .append("AND a.etat_traitement='{" + TraitementEtat.OK + "}' ")
                .append("AND a.client is not null ")
                .append("AND a.id_norme=b.id_norme ")
                .append("AND a.periodicite=b.periodicite ")
                .append("AND b.id_famille=c.id_famille ")
                // on filtre selon RG1
                .append("AND (a.client <@ c.client AND c.client <@ a.client) ")
                // test d'égalité des 2 tableaux (a.client,c.client)
                .append(") ")
                // par double inclusion (A dans B & B dans A)

                // 3. on selectionne les fichiers éligibles
                .append("SELECT id_source, container FROM (SELECT unnest(date_client) as t, id_source, container FROM isFichierToDelete) ww ")
                .append("GROUP BY id_source, container ")
                // on filtre selon RG2
                .append("HAVING (current_date - max(t) ::date ) >=" + Nb_Jour_A_Conserver + " ")
                .append("; ");

                UtilitaireDao.get("arc").executeRequest(connexion, requete);
                
                
                // requete sur laquelle on va itérer : on selectionne un certain nombre de fichier et on itere
                requete = new PreparedStatementBuilder();
                
                // 3b. on selectionne les fichiers éligibles et on limite le nombre de retour pour que l'update ne soit pas trop massif (perf)
                requete.append("WITH fichier_to_delete_limit AS ( ")
                .append(" SELECT * FROM fichier_to_delete LIMIT "+NB_FICHIER_PER_ARCHIVE+" ")
                .append(") ")
                
                // 4. suppression des archive de la table d'archive (bien retirer le nom de l'entrepot du début du container)
                .append(",delete_archive AS (").append("DELETE FROM ").append(nomTableArchive).append(" a ").append("USING fichier_to_delete_limit b ")
                .append("WHERE a.nom_archive=substring(b.container,strpos(b.container,'_')+1) ").append("returning *) ")

                // 5. suppression des fichier de la table de pilotage
                .append(",delete_idsource AS (").append("DELETE FROM ").append(nomTablePilotage).append(" a ").append("USING fichier_to_delete_limit b ")
                .append("WHERE a.id_source=b.id_source ").append(") ")
                
                //5b. suppression de la tgable des fichiers eligibles
                .append(",delete_source as (DELETE FROM fichier_to_delete a using fichier_to_delete_limit b where row(a.id_source,a.container)::text=row(b.id_source,b.container)::text) ")
                // 6. récuperer la liste des archives
                .append("SELECT entrepot, nom_archive FROM delete_archive ");

        
        // initialisation de la liste contenant les archives à déplacer
        HashMap<String, ArrayList<String>> m = new HashMap<String,ArrayList<String>>();
        m.put("entrepot", new ArrayList<String>());
        m.put("nom_archive", new ArrayList<String>());
        
        HashMap<String, ArrayList<String>> n = new HashMap<String,ArrayList<String>>();
        
        
        // on continue jusqu'a ce qu'on ne trouve plus rien à effacer
        do {
         // récupérer le résultat de la requete
         loggerDispatcher.info("Archivage de "+NB_FICHIER_PER_ARCHIVE+" fichiers - Début", LOGGER);
         n = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requete)).mapContent();
         
         
         // ajouter à la liste m les enregistrements qu'ils n'existent pas déjà dans m
         
         // on parcours n
         if (!n.isEmpty())
         {
	         for (int k=0; k<n.get("entrepot").size();k++)
	         {
	        	 boolean toInsert=true;
	        	 
	        	 //vérifier en parcourant m si on doit réaliser l'insertion
	             for (int l=0; l<m.get("entrepot").size();l++)
	             {
	            	 if (n.get("entrepot").get(k).equals(m.get("entrepot").get(l))
	            			&& n.get("nom_archive").get(k).equals(m.get("nom_archive").get(l))
	            			 )
	            	 {
	            		 toInsert=false;
	            		 break;
	            	 }
	             }
	             
	             // si aprés avoir parcouru tout m, l'enreigstrement de n n'est pas trouvé on l'insere
	             if (toInsert)
	             {
	            	 m.get("entrepot").add(n.get("entrepot").get(k));
	            	 m.get("nom_archive").add(n.get("nom_archive").get(k));
	             }
	             
	         }
        	}
         loggerDispatcher.info("Archivage Fin", LOGGER);
         
        } while (UtilitaireDao.get("arc").hasResults(connexion, new PreparedStatementBuilder("select 1 from fichier_to_delete limit 1")))
        ;
        
        // y'a-til des choses à faire ?
        if (m.get("entrepot").size()>0) {

            // 7. Déplacer les archives effacées dans le répertoire de sauvegarde "OLD"
        	String repertoire = properties.getBatchParametersDirectory();
            String envDir = this.envExecution.replace(".", "_").toUpperCase();

            String entrepotSav = "";
            for (int i = 0; i < m.get("entrepot").size(); i++) {
                String entrepot = m.get("entrepot").get(i);
                String archive = m.get("nom_archive").get(i);
                String dirIn = repertoire + envDir + File.separator + TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE";
                String dirOut = repertoire + envDir + File.separator + TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE" + File.separator
                        + "OLD";

                // création du répertoire "OLD" s'il n'existe pas
                if (!entrepotSav.equals(entrepot)) {
                    File f = new File(dirOut);
                    makeDir(f);
                    entrepotSav = entrepot;
                }

                // déplacement de l'archive de dirIn vers dirOut
                ApiReceptionService.deplacerFichier(dirIn, dirOut, archive, archive);

            }

            StringBuilder requeteMaintenance = new StringBuilder();
            requete.append("vacuum analyze " + nomTablePilotage + "; ");
            requete.append("vacuum analyze " + nomTableArchive + "; ");
            UtilitaireDao.get("arc").executeImmediate(connexion, requeteMaintenance);
        }

    }

    /**
     * Recopier les tables de l'environnement de parametres (IHM) vers l'environnement d'execution (batch, bas, ...)
     *
     * @param connexion
     * @param anParametersEnvironment
     * @param anExecutionEnvironment
     * @throws Exception
     */
    
    public static void copyTablesToExecution(Connection connexion, String anParametersEnvironment, String anExecutionEnvironment) {
    	try{
    		StaticLoggerDispatcher.info("Recopie des regles dans l'environnement", LOGGER);
        	copyTablesToExecutionThrow(connexion, anParametersEnvironment, anExecutionEnvironment);
    	} catch (Exception e)
    	{
    		StaticLoggerDispatcher.info("Erreur copyTablesToExecution", LOGGER);
            e.printStackTrace();
    	}
    }
    
    public static void copyTablesToExecutionThrow(Connection connexion, String anParametersEnvironment, String anExecutionEnvironment) throws Exception {
    	StaticLoggerDispatcher.info("copyTablesToExecution", LOGGER);
        try {
            StringBuilder requete = new StringBuilder();
            TraitementTableParametre[] r = TraitementTableParametre.values();
            StringBuilder condition = new StringBuilder();
            String modaliteEtat = anExecutionEnvironment.replace("_", ".");
            String tablePil = ApiService.dbEnv(anExecutionEnvironment) + TraitementTableExecution.PILOTAGE_FICHIER;
            String tableImage;
            String tableCurrent;
            for (int i = 0; i < r.length; i++) {
                // on créé une table image de la table venant de l'ihm
                // (environnement de parametre)
                tableCurrent = ApiService.dbEnv(anExecutionEnvironment) + r[i];
                tableImage = FormatSQL.temporaryTableName(ApiService.dbEnv(anExecutionEnvironment) + r[i]);

                // recopie partielle (en fonction de l'environnement
                // d'exécution)
                // pour les tables JEUDEREGLE, CONTROLE_REGLE et MAPPING_REGLE
                condition.setLength(0);
                if (r[i] == TraitementTableParametre.NORME) {
                    condition.append(" WHERE etat='1'");
                }
                if (r[i] == TraitementTableParametre.CALENDRIER) {
                    condition.append(" WHERE etat='1' ");
                    condition
                            .append(" and exists (select 1 from " + anParametersEnvironment + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
                }
                if (r[i] == TraitementTableParametre.JEUDEREGLE) {
                    condition.append(" WHERE etat=lower('" + modaliteEtat + "')");
                    condition
                            .append(" and exists (select 1 from " + anParametersEnvironment + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
                    condition
                            .append(" and exists (select 1 from "
                                    + anParametersEnvironment
                                    + "_calendrier b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
                }
                if (r[i] == TraitementTableParametre.CHARGEMENT_REGLE || r[i] == TraitementTableParametre.NORMAGE_REGLE || r[i] == TraitementTableParametre.CONTROLE_REGLE
                        || r[i] == TraitementTableParametre.MAPPING_REGLE || r[i] == TraitementTableParametre.FILTRAGE_REGLE) {
                    condition.append(" WHERE exists (select 1 from " + anParametersEnvironment
                            + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
                    condition
                            .append(" and exists (select 1 from "
                                    + anParametersEnvironment
                                    + "_calendrier b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
                    condition
                            .append(" and exists (select 1 from "
                                    + anParametersEnvironment
                                    + "_jeuderegle b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup AND a.version=b.version and b.etat=lower('"
                                    + modaliteEtat + "'))");
                }
                requete.append(FormatSQL.dropTable(tableImage).toString());
                requete.append("CREATE TABLE " + tableImage
                        + " "+FormatSQL.WITH_NO_VACUUM+" AS SELECT a.* FROM " + anParametersEnvironment + "_"
                        + r[i] + " AS a " + condition + ";\n");
                requete.append(FormatSQL.dropTable(tableCurrent).toString());
                requete.append("ALTER TABLE " + tableImage + " rename to " + ManipString.substringAfterLast(tableCurrent, ".") + "; \n");
            }
            UtilitaireDao.get("arc").executeBlock(connexion, requete);

            // Dernière étape : recopie des tables de nomenclature et des tables prefixées par ext_ du schéma arc vers schéma courant
            
            requete.setLength(0);
           
            //1.Préparation des requêtes de suppression des tables nmcl_ et ext_ du schéma courant
            
            PreparedStatementBuilder requeteSelectDrop = new PreparedStatementBuilder();
            requeteSelectDrop.append(" SELECT 'DROP TABLE IF EXISTS '||schemaname||'.'||tablename||';'  AS requete_drop");
            requeteSelectDrop.append(" FROM pg_tables where schemaname = "+ requeteSelectDrop.quoteText(anExecutionEnvironment.toLowerCase())+" ");
            requeteSelectDrop.append(" AND tablename SIMILAR TO '%nmcl%|%ext%'");
            							
            
            ArrayList<String> requetesDeSuppressionTablesNmcl = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requeteSelectDrop)).mapContent().get("requete_drop");
            
            if (requetesDeSuppressionTablesNmcl!=null)
            {
	            for (String requeteDeSuppression : requetesDeSuppressionTablesNmcl) {
					requete.append("\n ")
							.append(requeteDeSuppression);
				}
            }
            
            //2.Préparation des requêtes de création des tables
            ArrayList<String> requetesDeCreationTablesNmcl = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion,
            		new PreparedStatementBuilder("select tablename from pg_tables where (tablename like 'nmcl\\_%' OR tablename like 'ext\\_%') and schemaname='arc'"))).mapContent().get("tablename");
            
            if (requetesDeCreationTablesNmcl!=null){
	            for (String tableName : requetesDeCreationTablesNmcl) {
	                requete.append("\n CREATE TABLE " + ApiService.dbEnv(anExecutionEnvironment) + tableName
	                        + " "+FormatSQL.WITH_NO_VACUUM+" AS SELECT * FROM arc." + tableName + ";");
	            }
            }

            //3.Execution du script Sql de suppression/création
            UtilitaireDao.get("arc").executeBlock(connexion, requete);

        } catch (Exception e) {
        	StaticLoggerDispatcher.info("Problème lors de la copie des tables vers l'environnement : " + anExecutionEnvironment, LOGGER);
        	StaticLoggerDispatcher.info(e.getMessage().toString(), LOGGER);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Méthode pour remettre le système d'information dans la phase précédente Nettoyage des tables _ok et _ko ainsi que mise à jour de la
     * table de pilotage de fichier
     *
     * @param phase
     * @param querySelection
     * @param listEtat
     */
    public void retourPhasePrecedente(TraitementPhase phase, PreparedStatementBuilder querySelection, ArrayList<TraitementEtat> listEtat) {
        LOGGER.info("Retour arrière pour la phase :" + phase);
        PreparedStatementBuilder requete;
        // MAJ de la table de pilotage
        Integer nbLignes = 0;

        
        // Delete the selected file entries from the pilotage table from all the phases after the undo phase
        for (TraitementPhase phaseNext : phase.nextPhases()) {
        	requete = new PreparedStatementBuilder();
            requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.tablePil + " WHERE phase_traitement = " + requete.quoteText(phaseNext.toString()) + " ");
            if (querySelection.length()>0) {
                requete.append("AND id_source IN (SELECT distinct id_source FROM (");
                requete.append(querySelection);
                requete.append(") q1 ) ");               
            }
            requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
            nbLignes = nbLignes + UtilitaireDao.get("arc").getInt(this.connexion, requete);
        }

        
        // Mark the selected file entries to be reload then rebuild the file system for the reception phase
        if (phase.equals(TraitementPhase.RECEPTION))
        {
        	requete = new PreparedStatementBuilder();
            requete.append("UPDATE  " + this.tablePil + " set to_delete='R' WHERE phase_traitement = '" + requete.quoteText(phase.toString()) + "' ");
            if (querySelection != null) {
            	 requete.append("AND id_source IN (SELECT distinct id_source FROM (");
                 requete.append(querySelection);
                 requete.append(") q1 ) ");
            }
            try {
				UtilitaireDao.get("arc").executeRequest(connexion, requete);
			} catch (SQLException e) {
				loggerDispatcher.error(e, LOGGER);
			}
            
            try {
                reinstate(this.connexion, this.tablePil);
            } catch (Exception e) {
            	loggerDispatcher.error(e, LOGGER);
            }
            
            nbLignes++;
        }

        // Delete the selected file entries from the pilotage table from the undo phase
    	requete = new PreparedStatementBuilder();
        requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.tablePil + " WHERE phase_traitement = '" + phase + "' ");
        if (querySelection.length()>0) {
       	 	requete.append("AND id_source IN (SELECT distinct id_source FROM (");
            requete.append(querySelection);
            requete.append(") q1 ) ");
       }
        requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
        nbLignes = nbLignes + UtilitaireDao.get("arc").getInt(this.connexion, requete);

        // Run a database synchronization with the pilotage table
        try {
	        synchroniserEnvironmentByPilotage(this.connexion, this.envExecution);
        } catch (Exception e) {
        	loggerDispatcher.error(e, LOGGER);
        }

        if (nbLignes > 0) {
            maintenancePilotage(this.connexion, this.envExecution, "");
        }

        // Penser à tuer la connexion
    }

    public void resetEnvironnement() {
        try {
	        synchroniserEnvironmentByPilotage(this.connexion, this.envExecution);
	        maintenancePilotage(this.connexion, this.envExecution, "");
        } catch (Exception e) {
        	loggerDispatcher.error(e, LOGGER);
        }
    }

    /**
     * Récupere toutes les tables temporaires d'un environnement
     *
     * @param env
     * @return
     */
    public PreparedStatementBuilder requeteListAllTablesEnvTmp(String env) {
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        TraitementPhase[] phase = TraitementPhase.values();
        // on commence après la phase "initialisation". i=2
        for (int i = 2; i < phase.length; i++) {
            if (i > 2) {
                requete.append(" UNION ALL ");
            }
            requete.append(FormatSQL.tableExists(ApiService.dbEnv(this.envExecution) + phase[i] + "$%$tmp$%", " "));
            requete.append(" UNION ALL ");
            requete.append(FormatSQL.tableExists(ApiService.dbEnv(this.envExecution) + phase[i] + "\\_%$tmp$%", " "));
        }
        return requete;
    }

    /**
     * recupere toutes les tables d'état d'un envrionnement
     *
     * @param env
     * @return
     */
    public static PreparedStatementBuilder requeteListAllTablesEnv(String env) {
    	PreparedStatementBuilder requete = new PreparedStatementBuilder();
        TraitementPhase[] phase = TraitementPhase.values();
        boolean insert = false;

        for (int i = 0; i < phase.length; i++) {
            if (insert) {
                requete.append(" UNION ALL ");
            }
            PreparedStatementBuilder r = requeteListTableEnv(env, phase[i].toString());
            insert = (r.length() > 0);
            requete.append(r);
        }
        return requete;
    }

    public static PreparedStatementBuilder requeteListTableEnv(String env, String phase) {
        // Les tables dans l'environnement sont de la forme
        TraitementEtat[] etat = TraitementEtat.values();
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        for (int j = 0; j < etat.length; j++) {
            if (!etat[j].equals(TraitementEtat.ENCOURS)) {
                if (j > 0) {
                    requete.append(" UNION ALL ");
                }
                requete.append(FormatSQL.tableExists(ApiService.dbEnv(env) + "%" + phase + "%\\_" + etat[j], " "));
            }
        }
        return requete;
    }

    /**
     * Remise en coherence des tables de données avec la table de pilotage
     *
     * @param connexion
     * @param envExecution
     * @throws Exception
     */
    public void synchroniserEnvironmentByPilotage(Connection connexion, String envExecution) throws Exception {
        loggerDispatcher.info("synchronisationEnvironmentByPilotage", LOGGER);
        try {
            // maintenance de la table de pilotage
            // retirer les "encours" de la table de pilotage
            loggerDispatcher.info("** Maintenance table de pilotage **", LOGGER);
            UtilitaireDao.get("arc").executeBlock(connexion,
                    "alter table " + this.tablePil + " alter column date_entree type text COLLATE pg_catalog.\"C\"; ");
            UtilitaireDao.get("arc").executeBlock(connexion, "delete from " + this.tablePil + " where etat_traitement='{ENCOURS}';");

            // pour chaque fichier de la phase de pilotage, remet à etape='1' pour sa derniere phase valide
            remettreEtapePilotage();

            // recrée la table de pilotage, ses index, son trigger
            rebuildPilotage(connexion, this.tablePil);

            // drop des tables temporaires
            GenericBean g = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requeteListAllTablesEnvTmp(envExecution)));
            if (!g.mapContent().isEmpty()) {
                ArrayList<String> envTables = g.mapContent().get("table_name");
                for (String nomTable : envTables) {
                    UtilitaireDao.get("arc").executeBlock(connexion, FormatSQL.dropTable(nomTable).toString());
                }
            }

            // pour chaque table de l'environnement d'execution courant
            g = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requeteListAllTablesEnv(envExecution)));
            if (!g.mapContent().isEmpty()) {
                ArrayList<String> envTables = g.mapContent().get("table_name");
                for (String nomTable : envTables) {

                    String phase = ManipString.substringBeforeFirst(nomTable.substring(envExecution.length() + 1), "_").toUpperCase();
                    String etat = ManipString.substringAfterLast(nomTable, "_").toUpperCase();

                    // Cas des tables non MAPPING
                    if (!nomTable.contains(TraitementPhase.MAPPING.toString().toLowerCase())){
                    	
                    	
        		    // temporary table to store inherit table already checked
   		 			UtilitaireDao.get(poolName).executeImmediate(connexion, "DROP TABLE IF EXISTS TMP_INHERITED_TABLES_TO_CHECK; CREATE TEMPORARY TABLE TMP_INHERITED_TABLES_TO_CHECK (tablename text);" );
                	
   				    // Does the table have some inherited table left to check ?
   		 			// Only MAX_LOCK_PER_TRANSACTION tables can be proceed at the same time so iteration is required
                    HashMap<String, ArrayList<String>> m;
                    do {
                    	m=new GenericBean(UtilitaireDao.get(poolName).executeRequest(connexion,
                    			new PreparedStatementBuilder("\n WITH TMP_SELECT AS (SELECT schemaname||'.'||tablename as tablename FROM pg_tables WHERE schemaname||'.'||tablename like '"+nomTable+"\\_"+CHILD_TABLE_TOKEN+"\\_%' AND schemaname||'.'||tablename NOT IN (select tablename from TMP_INHERITED_TABLES_TO_CHECK) LIMIT "+FormatSQL.MAX_LOCK_PER_TRANSACTION+" ) "
           		 					+"\n , TMP_INSERT AS (INSERT INTO TMP_INHERITED_TABLES_TO_CHECK SELECT * FROM TMP_SELECT) "
           		 					+"\n SELECT tablename from TMP_SELECT ")
           		 					)).mapContent();
           		 	
	           		 	StringBuilder query=new StringBuilder();
	           		 	// Oui elle a des héritages
	           		 	if (!m.isEmpty())
	           		 	{
	           		 		
	           		 		// on parcourt les tables héritées
		           		 	for (String t:m.get("tablename"))
		           		 	{
		           		 		// on récupère la variable etape dans la phase
		           		 		// si on ne trouve la source de la table dans la phase, on drop !
		           		 		String etape=UtilitaireDao.get(poolName).getString(connexion, new PreparedStatementBuilder("SELECT etape FROM "+tablePil+" WHERE phase_traitement='" + phase + "' AND '" + etat + "'=ANY(etat_traitement) AND id_source=(select id_source from "+t+" limit 1)"));
		           		 		
		           		 		if (etape==null)
		           		 		{
		           		 			query.append("\n DROP TABLE IF EXISTS "+t+"; COMMIT;");
		           		 		}
		           		 		else
		           		 		{
			           		 			// si on ne trouve pas la table dans la phase en etape=1, on détruit le lien avec to do
				           		 		if (!etape.equals("1"))
				           		 		{
				           		 			query.append(FormatSQL.tryQuery("\n ALTER TABLE "+t+" NO INHERIT "+ManipString.substringBeforeFirst(t,"_"+CHILD_TABLE_TOKEN+"_")+"_todo;"));
				           		 		}
				           		 		else
				           		 		// sinon on pose le lien (etape 1 ou 2)
				           		 		{
				           		 			query.append(FormatSQL.tryQuery("\n ALTER TABLE "+t+" INHERIT "+ManipString.substringBeforeFirst(t,"_"+CHILD_TABLE_TOKEN+"_")+"_todo;"));
				           		 		}
		           		 		}
		           		 	}
		           		 	
	       		 			UtilitaireDao.get(poolName).executeImmediate(connexion, query);
	
	                    }
           		 	
                    } while (!m.isEmpty());
           		 	
                    }
                    else
                    {
                    	UtilitaireDao.get("arc").executeBlock(this.connexion, deleteTableByPilotage(nomTable, nomTable, this.tablePil, phase, etat, ""));
                        UtilitaireDao.get("arc").executeImmediate(connexion,
                                "set default_statistics_target=1; vacuum analyze " + nomTable + "(id_source); set default_statistics_target=100;");
                    }
           		 	
                }

            }

        } catch (Exception ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "synchroniserEnvironnementByPilotage()", LOGGER, ex);
            throw ex;
        }

        // maintenance des tables de catalogue car postgres ne semble pas être foutu de le faire soit même... 12G de catalogue au bout de 6
        // mois o_O
        UtilitaireDao.get(poolName).maintenancePgCatalog(this.connexion, "full");

    }

    
    public static void rebuildPilotage(Connection connexion, String tablePilotage) throws SQLException
    {
        UtilitaireDao.get("arc").executeBlock(
                connexion,
                FormatSQL.rebuildTableAsSelectWhere(tablePilotage, "true",
                        "create index idx1_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (id_source);",
                        "create index idx2_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (phase_traitement, etape);",
                        "create index idx3_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (date_entree);",
                        "create index idx4_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (rapport) where rapport is not null;",
                        "create index idx5_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (o_container,v_container);",
                        "create index idx6_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (to_delete);",
                        "CREATE TRIGGER tg_pilotage_fichier_calcul AFTER INSERT OR UPDATE OR DELETE ON " + tablePilotage    + " FOR EACH ROW EXECUTE PROCEDURE arc.transpose_pilotage_calcul();",
                        "CREATE TRIGGER tg_pilotage_fichier_fin AFTER INSERT OR UPDATE OR DELETE ON " + tablePilotage+ " FOR EACH STATEMENT EXECUTE PROCEDURE arc.transpose_pilotage_fin();"));
        UtilitaireDao.get("arc").executeBlock(connexion, "analyze " + tablePilotage + ";");
    }
    
    
    /**
     * la variable etape indique si c'est bien l'etape à considerer pour traitement ou pas etape='1' : phase à considerer, sinon etape='0'
     *
     * @return
     * @throws SQLException
     */
    public boolean remettreEtapePilotage() throws SQLException {

        StringBuilder requete = new StringBuilder();
        requete.append("WITH tmp_1 as (select id_source, max(");

        new StringBuilder();
        requete.append("case ");

        for (TraitementPhase p : TraitementPhase.values()) {
            requete.append("when phase_traitement='" + p.toString() + "' then " + p.ordinal() + " ");
        }
        requete.append("end ) as p ");
        requete.append("FROM " + this.tablePil + " ");
        requete.append("GROUP BY id_source ");
        requete.append("having max(etape)=0 ) ");
        requete.append("update " + this.tablePil + " a ");
        requete.append("set etape=1 ");
        requete.append("from tmp_1 b ");
        requete.append("where a.id_source=b.id_source ");
        requete.append("and a.phase_traitement= case ");
        for (TraitementPhase p : TraitementPhase.values()) {
            requete.append("when p=" + p.ordinal() + " then '" + p.toString() + "' ");
        }
        requete.append("end ; ");

        UtilitaireDao.get("arc").executeBlock(this.connexion, requete);

        return true;
    }


    public static void clearPilotageAndDirectories(String repertoire, String env) throws Exception {
        try {
        	 UtilitaireDao.get("arc").executeBlock(null, "truncate " + dbEnv(env) + "pilotage_fichier; ");
        	 UtilitaireDao.get("arc").executeBlock(null, "truncate " + dbEnv(env) + "pilotage_fichier_t; ");
             UtilitaireDao.get("arc").executeBlock(null, "truncate " + dbEnv(env) + "pilotage_archive; ");

            String envDir = env.replace(".", "_").toUpperCase();

            if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot"))) {
                ArrayList<String> entrepotList = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
                		new PreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent().get("id_entrepot");
                if (entrepotList!=null)
                {
	                for (String s : entrepotList) {
	                    FileUtils.cleanDirectory(Paths.get(repertoire, envDir, TraitementPhase.RECEPTION + "_" + s).toFile());
	                    FileUtils.cleanDirectory(Paths.get(repertoire, envDir, TraitementPhase.RECEPTION + "_" + s + "_ARCHIVE").toFile());
	                }
                }
            }
            FileUtils.cleanDirectory(Paths.get(repertoire, envDir, TraitementPhase.RECEPTION + "_" + TraitementEtat.ENCOURS).toFile());
            FileUtils.cleanDirectory(Paths.get(repertoire, envDir, TraitementPhase.RECEPTION + "_" + TraitementEtat.OK).toFile());
            FileUtils.cleanDirectory(Paths.get(repertoire, envDir, TraitementPhase.RECEPTION + "_" + TraitementEtat.KO).toFile());
            try {
                FileUtils.cleanDirectory(Paths.get(repertoire, envDir, "EXPORT").toFile());
            } catch (Exception e) {}

        } catch (IOException ex) {
            LoggerHelper.errorGenTextAsComment(ApiInitialisationService.class, "clearPilotageAndDirectories()", LOGGER, ex);
        } catch (SQLException ex) {
            LoggerHelper.errorGenTextAsComment(ApiInitialisationService.class, "clearPilotageAndDirectories()", LOGGER, ex);
            throw ex;
        }
    }

     
    /**
     * Rebuild des grosses tables
     * attention si on touche parameteres de requetes ou à la clause exists; forte volumétrie !
     */
    public static String deleteTableByPilotage(String nomTable, String nomTableSource, String tablePil, String phase, String etat, String extraCond) {
        StringBuilder requete = new StringBuilder();

        String tableDestroy = FormatSQL.temporaryTableName(nomTable, "D");
        requete.append("\n SET enable_nestloop=off; ");
        
        requete.append("\n DROP TABLE IF EXISTS " + tableDestroy + " CASCADE; ");
        requete.append("\n DROP TABLE IF EXISTS TMP_SOURCE_SELECTED CASCADE; ");
        
        // PERF : selection des id_source dans une table temporaire pour que postgres puisse partir en semi-hash join
        requete.append("\n CREATE TEMPORARY TABLE TMP_SOURCE_SELECTED AS ");
        requete.append("\n SELECT id_source from " + tablePil + " ");
        requete.append("\n WHERE phase_traitement='" + phase + "' ");
        requete.append("\n AND '" + etat + "'=ANY(etat_traitement) ");
        requete.append("\n "+extraCond+" ");
        requete.append("\n ; ");
        
        requete.append("\n ANALYZE TMP_SOURCE_SELECTED; ");
        
        requete.append("\n CREATE  TABLE " + tableDestroy + " "+FormatSQL.WITH_NO_VACUUM+" ");
        requete.append("\n AS select * from " + nomTableSource + " a ");
        requete.append("\n WHERE exists (select 1 from TMP_SOURCE_SELECTED b WHERE a.id_source=b.id_source) ");
        requete.append("\n ; ");

        requete.append("\n DROP TABLE IF EXISTS " + nomTable + " CASCADE; ");
        requete.append("\n ALTER TABLE " + tableDestroy + " rename to " + ManipString.substringAfterFirst(nomTable, ".") + ";\n");
        
        requete.append("\n DROP TABLE IF EXISTS TMP_SOURCE_SELECTED; ");

        requete.append("\n SET enable_nestloop=on; ");
        
        return requete.toString();

    }

}
