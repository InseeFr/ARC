package fr.insee.arc.core.service.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.rulesobjects.JeuDeRegleDao;
import fr.insee.arc.core.service.api.query.ServiceDatabaseMaintenance;
import fr.insee.arc.core.service.api.query.ServiceFileSystemManagement;
import fr.insee.arc.core.service.api.query.ServiceHashFileName;
import fr.insee.arc.core.service.api.query.ServiceTableNaming;
import fr.insee.arc.core.service.engine.initialisation.BddPatcher;
import fr.insee.arc.core.service.engine.mapping.ExpressionService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
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

    public ApiInitialisationService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot,
            Integer aNbEnr, String paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
    }

    @Override
    public void executer() throws ArcException {

        // Supprime les lignes devenues inutiles récupérées par le webservice de la table pilotage_fichier
        nettoyerTablePilotage(this.connexion.getCoordinatorConnection(), this.envExecution);

        // Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans l'environnement d'excécution courant
        copyTablesToExecutionThrow(connexion.getCoordinatorConnection(), envParameters, envExecution);
        
        // mettre à jour les tables métier avec les paramêtres de la famille de norme
        mettreAJourSchemaTableMetierThrow(connexion.getCoordinatorConnection(), envParameters, envExecution);

        // marque les fichiers ou les archives à rejouer
        reinstate(this.connexion.getCoordinatorConnection(), this.tablePil);

        // efface des fichiers de la table de pilotage
        cleanToDelete(this.connexion.getCoordinatorConnection(), this.tablePil);

        // Met en cohérence les table de données avec la table de pilotage de l'environnement
        // La table de pilotage fait foi
        synchroniserEnvironmentByPilotage(this.connexion.getCoordinatorConnection(), this.envExecution);

        // remettre les archives ou elle doivent etre en cas de restauration de la base
        rebuildFileSystem();

    }

    /**
     * remet le filesystem en etat en cas de restauration de la base
     *
     * @throws ArcException
     */
    private void rebuildFileSystem() throws ArcException {
        loggerDispatcher.info("rebuildFileSystem", LOGGER);

        // parcourir toutes les archives dans le répertoire d'archive
    	String repertoire = properties.getBatchParametersDirectory();

    	
    	String nomTableArchive = ServiceTableNaming.dbEnv(envExecution) + "pilotage_archive";

        // pour chaque en trepot de données,
        // Comparer les archives du répertoire aux archives enregistrées dans la table d'archive :
        // comme la table d'archive serait dans l'ancien état de données
        // on peut remettre dans le repertoire de reception les archives qu'on ne retouvent pas dans la table

        if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot"))) {
            ArrayList<String> entrepotList = new GenericBean(UtilitaireDao.get("arc")
                    .executeRequest(null, new ArcPreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent().get("id_entrepot");

            if (entrepotList!=null)
            {
            for (String entrepot : entrepotList) {
            	
            	
            	String fullEnvDir = ServiceFileSystemManagement.directoryEnvRoot(repertoire, this.envExecution);
            	File envDirFile = new File(fullEnvDir);
            	makeDir(envDirFile);
 
            	
                String dirIn = ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, this.envExecution, entrepot);
                String dirOut =  ApiReceptionService.directoryReceptionEntrepot(repertoire, this.envExecution, entrepot);

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
	                		UtilitaireDao.get("arc").executeImmediate(this.connexion.getCoordinatorConnection(), requete + ";");
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
        		UtilitaireDao.get("arc").executeImmediate(this.connexion.getCoordinatorConnection(), requete + ";");

        		// On cherche les fichiers du répertoire d'archive qui ne sont pas dans la table archive
        		// Si on en trouve ce n'est pas cohérent et on doit remettre ces fichiers dans le répertoire de reception
        		// pour être rechargé
                ArcPreparedStatementBuilder requete2=new ArcPreparedStatementBuilder();
                requete2.append(" SELECT fname FROM t_files a ");
                requete2.append(" WHERE NOT EXISTS (SELECT * FROM " + nomTableArchive + " b WHERE b.nom_archive=a.fname) ");

                ArrayList<String> fileToBeMoved=new GenericBean(UtilitaireDao.get("arc").executeRequest(this.connexion.getCoordinatorConnection(),requete2)).mapContent().get("fname");
                
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
                	String filenameWithoutExtension=ManipString.substringBeforeFirst(fichier.getName(),".");
                	String ext = "."+ ManipString.substringAfterFirst(fichier.getName(),".");

                	
                	if (filenameWithoutExtension.contains("#"))
                	{
                    	Integer number=ManipString.parseInteger(ManipString.substringAfterLast(filenameWithoutExtension,"#"));
                    	
                    	// c'est un fichier valide
                    	if (number!=null)
                    	{
                    		
                        	String originalIdSource=ManipString.substringBeforeLast(filenameWithoutExtension,"#");

                    		// tester ce qu'on doit en faire
                        	
                        	// comparer au fichier sans index
                    		File autreFichier=new File (dirOut + File.separator + originalIdSource + ext);
                    		try {
								if (autreFichier.exists() && FileUtils.contentEquals(autreFichier, fichier))
								{
									FileUtilsArc.delete(fichier);
								}
							} catch (IOException exception) {
								throw new ArcException(exception, ArcExceptionMessage.FILE_DELETE_FAILED,fichier);
							}
                    		
                    		// comparer aux fichier avec un index précédent
                    		for (int i=2;i<number;i++)
                    		{
                    			autreFichier=new File (dirOut + File.separator + originalIdSource + "#" + i + ext);
                        		
                        		try {
									if (autreFichier.exists() && FileUtils.contentEquals(autreFichier, fichier))
									{
										FileUtilsArc.delete(fichier);
									}
								} catch (IOException exception) {
									throw new ArcException(exception, ArcExceptionMessage.FILE_DELETE_FAILED,fichier);
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
			f.mkdirs();
		}
	}

    /**
     * Méthode pour initialiser ou patcher la base de données la base de donnée. 
     */
	public static void bddScript(Connection connexion, String...envExecutions) {
		BddPatcher.bddScript(connexion, envExecutions);
	}


    
    /**
     * Build directories for the sandbox
     * @param envExecutions
     */
    public static void buildFileSystem(Connection connexion, String[] envExecutions)
    {
    	PropertiesHandler properties = PropertiesHandler.getInstance();

		HashMap<String, ArrayList<String>> entrepotList = new HashMap<>();
		
    	try {
			entrepotList = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion,
					new ArcPreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent();

		} catch (ArcException ex) {
            LoggerHelper.errorGenTextAsComment(ApiInitialisationService.class, "buildFileSystem(envExecutions)", LOGGER, ex);
		}
    	
    	
    	for (String envExecution:Arrays.asList(envExecutions))
    	{
			
    		if (!entrepotList.isEmpty())
			{
    			for (String d : entrepotList.get("id_entrepot")) {
    				UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEntrepot(properties.getBatchParametersDirectory(), envExecution, d));
    				UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEntrepotArchive(properties.getBatchParametersDirectory(), envExecution, d));
    			}
			}
    		
			UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEtatEnCours(properties.getBatchParametersDirectory(), envExecution));
			UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEtatOK(properties.getBatchParametersDirectory(), envExecution));
			UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEtatKO(properties.getBatchParametersDirectory(), envExecution));
    	}

    }
    
    /**
     * Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans l'environnement d'excécution courant
     * @throws ArcException
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
     * @throws ArcException
     */
    private void reinstate(Connection connexion, String tablePil) throws ArcException {
        loggerDispatcher.info("reinstateWithRename", LOGGER);

        // on cherche tous les containers contenant un fichier à rejouer
        // on remet l'archive à la racine

        ArrayList<String> containerList = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
        		new ArcPreparedStatementBuilder("select distinct container from " + tablePil + " where to_delete in ('R','RA')"))).mapContent().get("container");

        if (containerList != null) {
        	String repertoire = properties.getBatchParametersDirectory();
            String envDir = this.envExecution.replace(".", "_").toUpperCase();

            for (String s : containerList) {

                String entrepot = ManipString.substringBeforeFirst(s, "_");
                String archive = ManipString.substringAfterFirst(s, "_");
                
                String dirIn= ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envDir, entrepot);
                String dirOut= ApiReceptionService.directoryReceptionEntrepot(repertoire, envDir, entrepot);

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
     * @throws ArcException
     */
    
    private static void mettreAJourSchemaTableMetier(Connection connexion, String envParameters, String envExecution) {
    	try{
            StaticLoggerDispatcher.info("Mettre à jour le schéma des tables métiers avec la famille", LOGGER);
            mettreAJourSchemaTableMetierThrow(connexion, envParameters, envExecution);
    	} catch (Exception e)
    	{
    		StaticLoggerDispatcher.error("Error in ApiInitialisation.mettreAJourSchemaTableMetier" , LOGGER);    	}
    	
    }

    
    private static void mettreAJourSchemaTableMetierThrow(Connection connexion, String envParameters, String envExecution) throws ArcException {
    		StaticLoggerDispatcher.info("mettreAJourSchemaTableMetier", LOGGER);
            /*
             * Récupérer la table qui mappe : famille / table métier / variable métier et type de la variable
             */
        	ArcPreparedStatementBuilder requeteRef = new ArcPreparedStatementBuilder();
        	requeteRef.append("SELECT lower(id_famille), lower('" + ServiceTableNaming.dbEnv(envExecution)
                    + "'||nom_table_metier), lower(nom_variable_metier), lower(type_variable_metier) FROM " + envParameters + "_mod_variable_metier");

            List<List<String>> relationalViewRef = Format.patch(UtilitaireDao.get(poolName).executeRequestWithoutMetadata(connexion, requeteRef));
            HierarchicalView familleToTableToVariableToTypeRef = HierarchicalView.asRelationalToHierarchical(
                    "(Réf) Famille -> Table -> Variable -> Type",
                    Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"), relationalViewRef);
            /*
             * Récupérer dans le méta-modèle de la base les tables métiers correspondant à la famille chargée
             */
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
            requete.append("SELECT lower(id_famille), lower(table_schema||'.'||table_name) nom_table_metier, lower(column_name) nom_variable_metier");
            
            // les types dans postgres sont horribles :(
            // udt_name : float8 = float, int8=bigint, int4=int
            // data_type :  double precision = float, integer=int
            requete.append(", case when lower(data_type)='array' then replace(replace(replace(ltrim(udt_name,'_'),'int4','int'),'int8','bigint'),'float8','float')||'[]' ");
            requete.append("	else replace(replace(lower(data_type),'double precision','float'),'integer','int') end type_variable_metier ");
            requete.append("\n FROM information_schema.columns, " + envParameters + "_famille ");
            requete.append("\n WHERE table_schema='" + ManipString.substringBeforeFirst(ServiceTableNaming.dbEnv(envExecution), ".").toLowerCase() + "' ");
            requete.append("\n and table_name LIKE '" + ManipString.substringAfterFirst(ServiceTableNaming.dbEnv(envExecution), ".").toLowerCase()
                    + "mapping\\_%' ");
            requete.append("\n and table_name LIKE '" + ManipString.substringAfterFirst(ServiceTableNaming.dbEnv(envExecution), ".").toLowerCase()
                    + "mapping\\_'||lower(id_famille)||'\\_%';");

            List<List<String>> relationalView = Format.patch(UtilitaireDao.get(poolName).executeRequestWithoutMetadata(connexion, requete));

            HierarchicalView familleToTableToVariableToType = HierarchicalView.asRelationalToHierarchical(
                    "(Phy) Famille -> Table -> Variable -> Type",
                    Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"), relationalView);
            StringBuilder requeteMAJSchema = new StringBuilder();
                        
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
            UtilitaireDao.get("arc").executeBlock(connexion, requeteMAJSchema);
    }

    /**
     * Suppression dans la table de pilotage des fichiers qui ont été marqué par la MOA (via la colonne to_delete de la table de pilotage);
     *
     * @param connexion
     * @param tablePil
     * @throws ArcException
     */
    private void cleanToDelete(Connection connexion, String tablePil) throws ArcException {
        loggerDispatcher.info("cleanToDelete", LOGGER);

        StringBuilder requete = new StringBuilder();
        requete.append("DELETE FROM " + tablePil + " a WHERE exists (select 1 from " + tablePil + " b where b.to_delete='1' and a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+" and a.container=b.container); ");
        UtilitaireDao.get("arc").executeBlock(connexion, requete);
    }

    /**
     * Suppression dans la table de pilotage des fichiers inutils. r.g :- une copie des données du fichier doit être envoyée à chacun de ses
     * clients RG1; - pour un fichier donné, l'ancienneté de son dernier transfert doit dépasser 7 jours RG2.
     *
     * @param connexion
     * @param tablePil
     * @param tablePil
     * @throws ArcException
     */
    private void nettoyerTablePilotage(Connection connexion, String envExecution) throws ArcException {

        loggerDispatcher.info("nettoyerTablePilotage", LOGGER);
        
        Nb_Jour_A_Conserver = BDParameters.getInt(this.connexion.getCoordinatorConnection(), "ApiInitialisationService.Nb_Jour_A_Conserver",365);
        
        NB_FICHIER_PER_ARCHIVE = BDParameters.getInt(this.connexion.getCoordinatorConnection(), "ApiInitialisationService.NB_FICHIER_PER_ARCHIVE",10000);

        String nomTablePilotage = ServiceTableNaming.dbEnv(envExecution) + "pilotage_fichier";
        String nomTableArchive = ServiceTableNaming.dbEnv(envExecution) + "pilotage_archive";

        ArcPreparedStatementBuilder requete;
        
        requete = new ArcPreparedStatementBuilder();
        
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
                .append("SELECT "+ColumnEnum.ID_SOURCE.getColumnName()+", container, date_client ")
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
                .append("SELECT "+ColumnEnum.ID_SOURCE.getColumnName()+", container FROM (SELECT unnest(date_client) as t, "+ColumnEnum.ID_SOURCE.getColumnName()+", container FROM isFichierToDelete) ww ")
                .append("GROUP BY "+ColumnEnum.ID_SOURCE.getColumnName()+", container ")
                // on filtre selon RG2
                .append("HAVING (current_date - max(t) ::date ) >=" + Nb_Jour_A_Conserver + " ")
                .append("; ");

                UtilitaireDao.get("arc").executeRequest(connexion, requete);
                
                
                // requete sur laquelle on va itérer : on selectionne un certain nombre de fichier et on itere
                requete = new ArcPreparedStatementBuilder();
                
                // 3b. on selectionne les fichiers éligibles et on limite le nombre de retour pour que l'update ne soit pas trop massif (perf)
                requete.append("WITH fichier_to_delete_limit AS ( ")
                .append(" SELECT * FROM fichier_to_delete LIMIT "+NB_FICHIER_PER_ARCHIVE+" ")
                .append(") ")
                
                // 4. suppression des archive de la table d'archive (bien retirer le nom de l'entrepot du début du container)
                .append(",delete_archive AS (").append("DELETE FROM ").append(nomTableArchive).append(" a ").append("USING fichier_to_delete_limit b ")
                .append("WHERE a.nom_archive=substring(b.container,strpos(b.container,'_')+1) ").append("returning *) ")

                // 5. suppression des fichier de la table de pilotage
                .append(",delete_idsource AS (").append("DELETE FROM ").append(nomTablePilotage).append(" a ").append("USING fichier_to_delete_limit b ")
                .append("WHERE a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+" ").append(") ")
                
                //5b. suppression de la tgable des fichiers eligibles
                .append(",delete_source as (DELETE FROM fichier_to_delete a using fichier_to_delete_limit b where row(a."+ColumnEnum.ID_SOURCE.getColumnName()+",a.container)::text=row(b."+ColumnEnum.ID_SOURCE.getColumnName()+",b.container)::text) ")
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
         
        } while (UtilitaireDao.get("arc").hasResults(connexion, new ArcPreparedStatementBuilder("select 1 from fichier_to_delete limit 1")))
        ;
        
        // y'a-til des choses à faire ?
        if (m.get("entrepot").size()>0) {

            // 7. Déplacer les archives effacées dans le répertoire de sauvegarde "OLD"
        	String repertoire = properties.getBatchParametersDirectory();

            String entrepotSav = "";
            for (int i = 0; i < m.get("entrepot").size(); i++) {
                String entrepot = m.get("entrepot").get(i);
                String archive = m.get("nom_archive").get(i);
                String dirIn = ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, this.envExecution, entrepot);
                String dirOut = ApiReceptionService.directoryReceptionEntrepotArchiveOld(repertoire, this.envExecution, entrepot);

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
     * @throws ArcException
     */
    
    public static void copyTablesToExecution(Connection connexion, String anParametersEnvironment, String anExecutionEnvironment) {
    	try{
    		StaticLoggerDispatcher.info("Recopie des regles dans l'environnement", LOGGER);
        	copyTablesToExecutionThrow(connexion, anParametersEnvironment, anExecutionEnvironment);
    	} catch (Exception e)
    	{
    		StaticLoggerDispatcher.error("Error in ApiInitialisation.copyTablesToExecution" , LOGGER);
    	}
    }
    
    
    
    private static void copyTablesToExecutionThrow(Connection connexion, String anParametersEnvironment, String anExecutionEnvironment) throws ArcException {
    	copyRulesTablesToExecution(connexion, anParametersEnvironment, anExecutionEnvironment);
    	applyExpressions(connexion, anExecutionEnvironment);
    }
    
    /**
     * Copy the table containing user rules to the sandbox so they will be used by the sandbox process
     * @param connexion
     * @param anParametersEnvironment
     * @param anExecutionEnvironment
     * @throws ArcException
     */
    private static void copyRulesTablesToExecution(Connection connexion, String anParametersEnvironment, String anExecutionEnvironment) throws ArcException {
    	StaticLoggerDispatcher.info("copyTablesToExecution", LOGGER);
        try {
        	
        	anExecutionEnvironment=anExecutionEnvironment.replace(".", "_");
        	
        	StringBuilder requete = new StringBuilder();
            TraitementTableParametre[] r = TraitementTableParametre.values();
            StringBuilder condition = new StringBuilder();
            String modaliteEtat = anExecutionEnvironment.replace("_", ".");
            String tableImage;
            String tableCurrent;
            for (int i = 0; i < r.length; i++) {
                // on créé une table image de la table venant de l'ihm
                // (environnement de parametre)
                TraitementTableParametre parameterTable = r[i];
				tableCurrent = ServiceTableNaming.dbEnv(anExecutionEnvironment) + parameterTable;
                tableImage = FormatSQL.temporaryTableName(ServiceTableNaming.dbEnv(anExecutionEnvironment) + parameterTable);

                // recopie partielle (en fonction de l'environnement
                // d'exécution)
                // pour les tables JEUDEREGLE, CONTROLE_REGLE et MAPPING_REGLE
                condition.setLength(0);
                if (parameterTable == TraitementTableParametre.NORME) {
                    condition.append(" WHERE etat='1'");
                } else if (parameterTable == TraitementTableParametre.CALENDRIER) {
                    condition.append(" WHERE etat='1' ");
                    condition
                            .append(" and exists (select 1 from " + anParametersEnvironment + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
                } else if (parameterTable == TraitementTableParametre.JEUDEREGLE) {
                    condition.append(" WHERE etat=lower('" + modaliteEtat + "')");
                    condition
                            .append(" and exists (select 1 from " + anParametersEnvironment + "_norme b where a.id_norme=b.id_norme and b.etat='1')");
                    condition
                            .append(" and exists (select 1 from "
                                    + anParametersEnvironment
                                    + "_calendrier b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
                } else if (parameterTable.isPartOfRuleset()) {
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
                requete.append(FormatSQL.dropTableCascade(tableImage));
                
                requete.append("CREATE TABLE " + tableImage
                        + " "+FormatSQL.WITH_NO_VACUUM+" AS SELECT a.* FROM " + anParametersEnvironment + "_"
                        + r[i] + " AS a " + condition + ";\n");
                
                requete.append(FormatSQL.dropTableCascade(tableCurrent));
                requete.append("ALTER TABLE " + tableImage + " rename to " + ManipString.substringAfterLast(tableCurrent, ".") + "; \n");
            }
            UtilitaireDao.get("arc").executeBlock(connexion, requete);

            // Dernière étape : recopie des tables de nomenclature et des tables prefixées par ext_ du schéma arc vers schéma courant
            
            requete.setLength(0);
           
            //1.Préparation des requêtes de suppression des tables nmcl_ et ext_ du schéma courant
            
            ArcPreparedStatementBuilder requeteSelectDrop = new ArcPreparedStatementBuilder();
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
            		new ArcPreparedStatementBuilder("select tablename from pg_tables where (tablename like 'nmcl\\_%' OR tablename like 'ext\\_%') and schemaname='arc'"))).mapContent().get("tablename");
            
            if (requetesDeCreationTablesNmcl!=null){
	            for (String tableName : requetesDeCreationTablesNmcl) {
	                requete.append("\n CREATE TABLE " + ServiceTableNaming.dbEnv(anExecutionEnvironment) + tableName
	                        + " "+FormatSQL.WITH_NO_VACUUM+" AS SELECT * FROM arc." + tableName + ";");
	            }
            }

            //3.Execution du script Sql de suppression/création
            UtilitaireDao.get("arc").executeBlock(connexion, requete);

        } catch (Exception e) {
        	StaticLoggerDispatcher.trace("Problème lors de la copie des tables vers l'environnement : " + anExecutionEnvironment, LOGGER);
        	StaticLoggerDispatcher.error("Error in ApiInitialisation.copyRulesTablesToExecution" , LOGGER);
            throw e;
        }
    }

    private static void applyExpressions(Connection connexion, String anExecutionEnvironment) throws ArcException {
		// Checks expression validity
    	ExpressionService expressionService = new ExpressionService();
    	ArrayList<JeuDeRegle> allRuleSets = JeuDeRegleDao.recupJeuDeRegle(connexion, anExecutionEnvironment + ".jeuderegle");
		for (JeuDeRegle ruleSet : allRuleSets) {
			// Check
			GenericBean expressions = expressionService.fetchExpressions(connexion, anExecutionEnvironment, ruleSet);
			if (expressions.isEmpty()) {
				continue;
			}

	    	Optional<String> loopInExpressionSet = expressionService.loopInExpressionSet(expressions);
	    	if (loopInExpressionSet.isPresent()) {
	    		StaticLoggerDispatcher.info("A loop is present in the expression set : " + loopInExpressionSet.get(), LOGGER);
	    		StaticLoggerDispatcher.info("The expression set is not applied", LOGGER);
	    		continue;
	    	}

	    	// Apply
	    	expressions = expressionService.fetchOrderedExpressions(connexion, anExecutionEnvironment, ruleSet);
	    	if(expressionService.isExpressionSyntaxPresentInControl(connexion, anExecutionEnvironment, ruleSet)) {
	    		UtilitaireDao.get(poolName).executeRequest(connexion, expressionService.applyExpressionsToControl(ruleSet, expressions, anExecutionEnvironment));
	    	}
	    	if(expressionService.isExpressionSyntaxPresentInMapping(connexion, anExecutionEnvironment, ruleSet)) {
	    		UtilitaireDao.get(poolName).executeRequest(connexion, expressionService.applyExpressionsToMapping(ruleSet, expressions, anExecutionEnvironment));
	    	}
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
    public void retourPhasePrecedente(TraitementPhase phase, ArcPreparedStatementBuilder querySelection, ArrayList<TraitementEtat> listEtat) {
        LOGGER.info("Retour arrière pour la phase : {}", phase);
        ArcPreparedStatementBuilder requete;
        // MAJ de la table de pilotage
        Integer nbLignes = 0;

        // reset etape=3 file to etape=0
        try {
            UtilitaireDao.get("arc").executeRequest(this.connexion.getCoordinatorConnection(), new ArcPreparedStatementBuilder(resetPreviousPhaseMark(this.tablePil, null, null)));
        } catch (Exception e) {
        	loggerDispatcher.error(e, LOGGER);
        }

        
        // Delete the selected file entries from the pilotage table from all the phases after the undo phase
        for (TraitementPhase phaseNext : phase.nextPhases()) {
        	requete = new ArcPreparedStatementBuilder();
            requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.tablePil + " WHERE phase_traitement = " + requete.quoteText(phaseNext.toString()) + " ");
            if (querySelection.length()>0) {
                requete.append("AND "+ColumnEnum.ID_SOURCE.getColumnName()+" IN (SELECT distinct "+ColumnEnum.ID_SOURCE.getColumnName()+" FROM (");
                requete.append(querySelection);
                requete.append(") q1 ) ");               
            }
            requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
            nbLignes = nbLignes + UtilitaireDao.get("arc").getInt(this.connexion.getCoordinatorConnection(), requete);
        }

        
        // Mark the selected file entries to be reload then rebuild the file system for the reception phase
        if (phase.equals(TraitementPhase.RECEPTION))
        {
        	requete = new ArcPreparedStatementBuilder();
            requete.append("UPDATE  " + this.tablePil + " set to_delete='R' WHERE phase_traitement = " + requete.quoteText(phase.toString()) + " ");
            if (querySelection.length()>0) {
            	 requete.append("AND "+ColumnEnum.ID_SOURCE.getColumnName()+" IN (SELECT distinct "+ColumnEnum.ID_SOURCE.getColumnName()+" FROM (");
                 requete.append(querySelection);
                 requete.append(") q1 ) ");
            }
            try {
				UtilitaireDao.get("arc").executeRequest(connexion.getCoordinatorConnection(), requete);
			} catch (ArcException e) {
				loggerDispatcher.error(e, LOGGER);
			}
            
            try {
                reinstate(this.connexion.getCoordinatorConnection(), this.tablePil);
            } catch (Exception e) {
            	loggerDispatcher.error(e, LOGGER);
            }
            
            nbLignes++;
        }

        // Delete the selected file entries from the pilotage table from the undo phase
    	requete = new ArcPreparedStatementBuilder();
        requete.append("WITH TMP_DELETE AS (DELETE FROM " + this.tablePil + " WHERE phase_traitement = " + requete.quoteText(phase.toString()) + " ");
        if (querySelection.length()>0) {
       	 	requete.append("AND "+ColumnEnum.ID_SOURCE.getColumnName()+" IN (SELECT distinct "+ColumnEnum.ID_SOURCE.getColumnName()+" FROM (");
            requete.append(querySelection);
            requete.append(") q1 ) ");
       }
        requete.append("RETURNING 1) select count(1) from TMP_DELETE;");
        nbLignes = nbLignes + UtilitaireDao.get("arc").getInt(this.connexion.getCoordinatorConnection(), requete);

        // Run a database synchronization with the pilotage table
        try {
	        synchroniserEnvironmentByPilotage(this.connexion.getCoordinatorConnection(), this.envExecution);
        } catch (Exception e) {
        	loggerDispatcher.error(e, LOGGER);
        }

        if (nbLignes > 0) {
        	ServiceDatabaseMaintenance.maintenanceDatabaseClassic(connexion.getCoordinatorConnection(), envExecution);
        }

        // Penser à tuer la connexion
    }

    public void resetEnvironnement() {
        try {
	        synchroniserEnvironmentByPilotage(this.connexion.getCoordinatorConnection(), this.envExecution);
	        ServiceDatabaseMaintenance.maintenanceDatabaseClassic(connexion.getCoordinatorConnection(), envExecution);
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
    private ArcPreparedStatementBuilder requeteListAllTablesEnvTmp(String env) {
        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        TraitementPhase[] phase = TraitementPhase.values();
        // on commence après la phase "initialisation". i=2
        for (int i = 2; i < phase.length; i++) {
            if (i > 2) {
                requete.append(" UNION ALL ");
            }
            requete.append(FormatSQL.tableExists(ServiceTableNaming.dbEnv(this.envExecution) + phase[i] + "$%$tmp$%"));
            requete.append(" UNION ALL ");
            requete.append(FormatSQL.tableExists(ServiceTableNaming.dbEnv(this.envExecution) + phase[i] + "\\_%$tmp$%"));
        }
        return requete;
    }

    /**
     * recupere toutes les tables d'état d'un envrionnement
     *
     * @param env
     * @return
     */
    public static ArcPreparedStatementBuilder requeteListAllTablesEnv(String env) {
    	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        TraitementPhase[] phase = TraitementPhase.values();
        boolean insert = false;

        for (int i = 0; i < phase.length; i++) {
            if (insert) {
                requete.append(" UNION ALL ");
            }
            ArcPreparedStatementBuilder r = requeteListTableEnv(env, phase[i].toString());
            insert = (r.length() > 0);
            requete.append(r);
        }
        return requete;
    }

    private static ArcPreparedStatementBuilder requeteListTableEnv(String env, String phase) {
        // Les tables dans l'environnement sont de la forme
        TraitementEtat[] etat = TraitementEtat.values();
        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        for (int j = 0; j < etat.length; j++) {
            if (!etat[j].equals(TraitementEtat.ENCOURS)) {
                if (j > 0) {
                    requete.append(" UNION ALL ");
                }
                requete.append(FormatSQL.tableExists(ServiceTableNaming.dbEnv(env) + "%" + phase + "%\\_" + etat[j]));
            }
        }
        return requete;
    }

    /**
     * Remise en coherence des tables de données avec la table de pilotage
     *
     * @param connexion
     * @param envExecution
     * @throws ArcException
     */
    private void synchroniserEnvironmentByPilotage(Connection connexion, String envExecution) throws ArcException {
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
                    UtilitaireDao.get("arc").executeBlock(connexion, FormatSQL.dropTableCascade(nomTable));
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
                    			new ArcPreparedStatementBuilder("\n WITH TMP_SELECT AS (SELECT schemaname||'.'||tablename as tablename FROM pg_tables WHERE schemaname||'.'||tablename like '"+nomTable+"\\_"+ServiceHashFileName.CHILD_TABLE_TOKEN+"\\_%' AND schemaname||'.'||tablename NOT IN (select tablename from TMP_INHERITED_TABLES_TO_CHECK) LIMIT "+FormatSQL.MAX_LOCK_PER_TRANSACTION+" ) "
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
		           		 		String etape=UtilitaireDao.get(poolName).getString(connexion, new ArcPreparedStatementBuilder("SELECT etape FROM "+tablePil+" WHERE phase_traitement='" + phase + "' AND '" + etat + "'=ANY(etat_traitement) AND "+ColumnEnum.ID_SOURCE.getColumnName()+"=(select "+ColumnEnum.ID_SOURCE.getColumnName()+" from "+t+" limit 1)"));
		           		 		
		           		 		if (etape==null)
		           		 		{
		           		 			query.append("\n BEGIN; DROP TABLE IF EXISTS "+t+"; COMMIT;");
		           		 		}
		           		 	}
		           		 	
	       		 			UtilitaireDao.get(poolName).executeImmediate(connexion, query);
	
	                    }
           		 	
                    } while (!m.isEmpty());
           		 	
                    }
                    else
                    {
                    	UtilitaireDao.get("arc").executeBlock(this.connexion.getCoordinatorConnection(), deleteTableByPilotage(nomTable, nomTable, this.tablePil, phase, etat, ""));
                        UtilitaireDao.get("arc").executeImmediate(connexion,
                                "set default_statistics_target=1; vacuum analyze " + nomTable + "("+ColumnEnum.ID_SOURCE.getColumnName()+"); set default_statistics_target=100;");
                    }
           		 	
                }

            }

        } catch (Exception ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "synchroniserEnvironnementByPilotage()", LOGGER, ex);
            throw ex;
        }

        // maintenance des tables de catalogue car postgres ne le réalise pas correctement sans mettre en oeuvre
        // une stratégie de vacuum hyper agressive et donc ajouter une spécificité pour les DBAs
        ServiceDatabaseMaintenance.maintenanceDatabaseClassic(connexion, envExecution);
		
    }

    
    
    private static void rebuildPilotage(Connection connexion, String tablePilotage) throws ArcException
    {
        UtilitaireDao.get("arc").executeBlock(
                connexion,
                FormatSQL.rebuildTableAsSelectWhere(tablePilotage, "true",
                        "create index idx1_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " ("+ColumnEnum.ID_SOURCE.getColumnName()+");",
                        "create index idx2_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (phase_traitement, etape);",
                        "create index idx4_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (rapport) where rapport is not null;",
                        "create index idx5_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (o_container,v_container);",
                        "create index idx6_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (to_delete);",
                        "create index idx7_" + ManipString.substringAfterFirst(tablePilotage, ".") + " on " + tablePilotage + " (date_entree, phase_traitement, etat_traitement);"
        				));

        UtilitaireDao.get("arc").executeBlock(connexion, "analyze " + tablePilotage + ";");
    }
    
    
    /**
     * la variable etape indique si c'est bien l'etape à considerer pour traitement ou pas etape='1' : phase à considerer, sinon etape='0'
     *
     * @return
     * @throws ArcException
     */
    private boolean remettreEtapePilotage() throws ArcException {

        StringBuilder requete = new StringBuilder();
        
        requete.append(ApiService.resetPreviousPhaseMark(this.tablePil, null, null));
        
        requete.append("WITH tmp_1 as (select "+ColumnEnum.ID_SOURCE.getColumnName()+", max(");
        new StringBuilder();
        requete.append("case ");
        for (TraitementPhase p : TraitementPhase.values()) {
            requete.append("when phase_traitement='" + p.toString() + "' then " + p.ordinal() + " ");
        }
        requete.append("end ) as p ");
        requete.append("FROM " + this.tablePil + " ");
        requete.append("GROUP BY "+ColumnEnum.ID_SOURCE.getColumnName()+" ");
        requete.append("having max(etape)=0 ) ");
        requete.append("update " + this.tablePil + " a ");
        requete.append("set etape=1 ");
        requete.append("from tmp_1 b ");
        requete.append("where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+" ");
        requete.append("and a.phase_traitement= case ");
        for (TraitementPhase p : TraitementPhase.values()) {
            requete.append("when p=" + p.ordinal() + " then '" + p.toString() + "' ");
        }
        requete.append("end ; ");

        UtilitaireDao.get("arc").executeBlock(this.connexion.getCoordinatorConnection(), requete);

        return true;
    }


    public static void clearPilotageAndDirectories(String repertoire, String env) throws ArcException {
        	 UtilitaireDao.get("arc").executeBlock(null, "truncate " + ServiceTableNaming.dbEnv(env) + "pilotage_fichier; ");
             UtilitaireDao.get("arc").executeBlock(null, "truncate " + ServiceTableNaming.dbEnv(env) + "pilotage_archive; ");


            if (Boolean.TRUE.equals(UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot")))) {
                ArrayList<String> entrepotList = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
                		new ArcPreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent().get("id_entrepot");
                if (entrepotList!=null)
                {
	                for (String s : entrepotList) {
	                	FileUtilsArc.deleteAndRecreateDirectory(Paths.get(ApiReceptionService.directoryReceptionEntrepot(repertoire, env, s)).toFile());
	                	FileUtilsArc.deleteAndRecreateDirectory(Paths.get(ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, env, s)).toFile());
	                }
                }
            }
            FileUtilsArc.deleteAndRecreateDirectory(Paths.get(ApiReceptionService.directoryReceptionEtatEnCours(repertoire, env)).toFile());
            FileUtilsArc.deleteAndRecreateDirectory(Paths.get(ApiReceptionService.directoryReceptionEtatOK(repertoire, env)).toFile());
            FileUtilsArc.deleteAndRecreateDirectory(Paths.get(ApiReceptionService.directoryReceptionEtatKO(repertoire, env)).toFile());
            FileUtilsArc.deleteAndRecreateDirectory(Paths.get(ServiceFileSystemManagement.directoryEnvExport(repertoire, env)).toFile());
    }

     
    /**
     * Rebuild des grosses tables
     * attention si on touche parameteres de requetes ou à la clause exists; forte volumétrie !
     */
    private static String deleteTableByPilotage(String nomTable, String nomTableSource, String tablePil, String phase, String etat, String extraCond) {
        StringBuilder requete = new StringBuilder();

        String tableDestroy = FormatSQL.temporaryTableName(nomTable, "D");
        requete.append("\n SET enable_nestloop=off; ");
        
        requete.append("\n DROP TABLE IF EXISTS " + tableDestroy + " CASCADE; ");
        requete.append("\n DROP TABLE IF EXISTS TMP_SOURCE_SELECTED CASCADE; ");
        
        // PERF : selection des id_source dans une table temporaire pour que postgres puisse partir en semi-hash join
        requete.append("\n CREATE TEMPORARY TABLE TMP_SOURCE_SELECTED AS ");
        requete.append("\n SELECT "+ColumnEnum.ID_SOURCE.getColumnName()+" from " + tablePil + " ");
        requete.append("\n WHERE phase_traitement='" + phase + "' ");
        requete.append("\n AND '" + etat + "'=ANY(etat_traitement) ");
        requete.append("\n "+extraCond+" ");
        requete.append("\n ; ");
        
        requete.append("\n ANALYZE TMP_SOURCE_SELECTED; ");
        
        requete.append("\n CREATE  TABLE " + tableDestroy + " "+FormatSQL.WITH_NO_VACUUM+" ");
        requete.append("\n AS select * from " + nomTableSource + " a ");
        requete.append("\n WHERE exists (select 1 from TMP_SOURCE_SELECTED b WHERE a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ");
        requete.append("\n ; ");

        requete.append("\n DROP TABLE IF EXISTS " + nomTable + " CASCADE; ");
        requete.append("\n ALTER TABLE " + tableDestroy + " rename to " + ManipString.substringAfterFirst(nomTable, ".") + ";\n");
        
        requete.append("\n DROP TABLE IF EXISTS TMP_SOURCE_SELECTED; ");

        requete.append("\n SET enable_nestloop=on; ");
        
        return requete.toString();

    }

}
