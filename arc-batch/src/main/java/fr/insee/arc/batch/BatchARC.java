package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.batch.unitaryLauncher.ChargerBatch;
import fr.insee.arc.batch.unitaryLauncher.ControlerBatch;
import fr.insee.arc.batch.unitaryLauncher.FiltrerBatch;
import fr.insee.arc.batch.unitaryLauncher.InitialiserBatch;
import fr.insee.arc.batch.unitaryLauncher.MapperBatch;
import fr.insee.arc.batch.unitaryLauncher.NormerBatch;
import fr.insee.arc.batch.unitaryLauncher.RecevoirBatch;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiReceptionService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Classe lanceur de l'application Accueil Reception Contrôle
 * 07/08/2015
 * Version pour les tests de performance et pré-production
 * @author Manu
 * 
 */
public class BatchARC {
	private static final Logger LOGGER = LogManager.getLogger(BatchARC.class);
	static HashMap<String, String> mapParam = new HashMap<>();

	/**
	 * variable dateInitialisation
	 * si vide (ou si date du jour+1 depassé à 20h),  je lance initialisation et j'initialise dateInitialisation à la nouvelle date du jour
	 * puis une fois terminé, je lancent la boucle des batchs
	 * si date du jour+1 depassé a 20h,
	 * - j'indique aux autre batchs de s'arreter
	 * - une fois arretés, je met tempo à la date du jour
	 * - je lance initialisation
	 * etc.
	 */
	
	@Autowired
	PropertiesHandler properties;
	
	// mode production (boucle sur les paquets d'enveloppes)
	private static boolean production=true;
	
	// keepInDatabase = est-ce qu'on garde les données des phases intérmédiaires en base ?
	// false en production 
	private static boolean keepInDatabase;
	
	// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas excéder une certaine taille
	protected static int tailleMaxReceptionEnMb;

	// Maximum number of files to load
	protected static int maxFilesToLoad;

	// Maximum number of files processed in each phase iteration
	protected static int maxFilesPerPhase;

	// fréquence à laquelle les phases sont démarrées
	private static int poolingDelay;

	// heure d'initalisation en production
	private static int hourToTriggerInitializationInProduction;
	
	// interval entre chaque initialisation en nb de jours
	private static Integer intervalForInitializationInDay;

	// keys name for the hashmap mapParam containing the batch parameters
	private static final String KEY_FOR_METADATA_ENVIRONMENT="env";
	private static final String KEY_FOR_EXECUTION_ENVIRONMENT="envExecution";
	private static final String KEY_FOR_DIRECTORY_LOCATION="repertoire";
	private static final String KEY_FOR_BATCH_CHUNK_ID="numlot";

	
	public static class InitialiserThread {
		private ServiceReporting report=new ServiceReporting();

		  public void run() {
			  InitialiserBatch c=new InitialiserBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , tailleMaxReceptionEnMb+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			  c.execute();
			  this.report=c.report;
		  }
		}

	public static class RecevoirThread {
		private ServiceReporting report=new ServiceReporting();
		  public void run() {
			RecevoirBatch c=new RecevoirBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , tailleMaxReceptionEnMb+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			c.execute();
			this.report=c.report;
		  }
		}

	public static class ChargerThread extends Thread {
		@Override
		  public void run() {
			ChargerBatch c=new ChargerBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , maxFilesToLoad+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			c.execute();
		  }
		}

	public static class NormerThread extends Thread {
		  @Override
		  public void run() {
			  NormerBatch c=new NormerBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			  c.execute();
		  }
		}

	public static class ControlerThread extends Thread {
		  @Override
		  public void run() {
			  ControlerBatch c=new ControlerBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			  c.execute();
		  }
		}

	public static class FiltrerThread extends Thread {
		  @Override
		  public void run() {
			  FiltrerBatch c=new FiltrerBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			  c.execute();
		  }
		}


	public static class MapperThread extends Thread {
		  @Override
		  public void run() {
			  MapperBatch c= new MapperBatch(mapParam.get(KEY_FOR_METADATA_ENVIRONMENT), mapParam.get(KEY_FOR_EXECUTION_ENVIRONMENT), mapParam.get(KEY_FOR_DIRECTORY_LOCATION) , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get(KEY_FOR_BATCH_CHUNK_ID));
			  c.execute();
		  }
		}
	
	public static void message(String msg)
	{
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"  "+msg);
	}

	
	private static void initParameters()
	{

		keepInDatabase= Boolean.parseBoolean(BDParameters.getString(null, "LanceurARC.keepInDatabase","false"));
		
		// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas excéder une certaine taille
		tailleMaxReceptionEnMb=BDParameters.getInt(null, "LanceurARC.tailleMaxReceptionEnMb",10);

		// Maximum number of files to load
		maxFilesToLoad=BDParameters.getInt(null, "LanceurARC.maxFilesToLoad",101);

		// Maximum number of files processed in each phase iteration
		maxFilesPerPhase=BDParameters.getInt(null, "LanceurARC.maxFilesPerPhase",1000000);

		// fréquence à laquelle les phases sont démarrées
		poolingDelay=BDParameters.getInt(null, "LanceurARC.poolingDelay",1000);

		// heure d'initalisation en production
		hourToTriggerInitializationInProduction=BDParameters.getInt(null, "ApiService.HEURE_INITIALISATION_PRODUCTION",22);
		
		// interval entre chaque initialisation en nb de jours
		intervalForInitializationInDay = BDParameters.getInt(null, "LanceurARC.INTERVAL_JOUR_INITIALISATION",7);
	}
	
	
	/**
	 * Lanceur MAIN arc
	 * @param args
	 */
	public void execute(String[] args) {

		// fill the parameters
		initParameters();
				
		boolean fichierRestant=false;

		message ("Main");
		
		do {
		
		message("Batch ARC " + properties.fullVersionInformation().toString());
		
		try{
		
		String env =null;
		String envExecution = null;
		
		// either we take env and envExecution from database or properties
		// default is from properties
		if (Boolean.parseBoolean(BDParameters.getString(null, "LanceurARC.envFromDatabase","false")))
		{
			env=BDParameters.getString(null, "LanceurARC.env",ApiService.IHM_SCHEMA);
			envExecution=BDParameters.getString(null, "LanceurARC.envExecution","arc_prod");
		}	
		else
		{
			env= properties.getBatchArcEnvironment();
			envExecution = properties.getBatchExecutionEnvironment();
		}
		
		envExecution=envExecution.replace(".", "_");
		
		mapParam.put(KEY_FOR_METADATA_ENVIRONMENT, env);
		mapParam.put(KEY_FOR_EXECUTION_ENVIRONMENT, envExecution);
		
		String repertoire = properties.getBatchParametersDirectory();
		mapParam.put(KEY_FOR_DIRECTORY_LOCATION, repertoire);

		message(mapParam.toString());


		creerTablePilotageBatch();

		InitialiserThread initialiser=new InitialiserThread();
		RecevoirThread recevoir=new RecevoirThread();
		ChargerThread charger=new ChargerThread();
		NormerThread normer=new NormerThread();
		ControlerThread controler=new ControlerThread();
		FiltrerThread filtrer=new FiltrerThread();
		MapperThread mapper=new MapperThread();

		
		// opération de maintenance
		message("Maintenance pilotage");
		ApiService.maintenancePilotage(null, envExecution, "freeze");
		message("Fin de Maintenance pilotage");
		
		message("Déplacements de fichiers");

		boolean productionOn=productionOn();

		if (productionOn)
		{
		// on vide les repertoires de chargement OK, KO, ENCOURS
		effacerRepertoireChargement(repertoire, envExecution);
		
		
		// des archives n'ont elles pas été traitées jusqu'au bout ?
		ArrayList<String> aBouger= new GenericBean(UtilitaireDao.get("arc").executeRequest(null,new PreparedStatementBuilder("select distinct container from "+envExecution+".pilotage_fichier where etape=1"))).mapContent().get("container");
		
		boolean dejaEnCours=(aBouger!=null);

		// si oui, on essaie de recopier les archives dans chargement OK
		if (aBouger!=null)
		{
			
			for (String container:aBouger)
			{
				String entrepotContainer=ManipString.substringBeforeFirst(container, "_");
				String originalContainer=ManipString.substringAfterFirst(container, "_");
				
				File fIn= Paths.get(
						ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepotContainer)
						, originalContainer
						).toFile();
				
				File fOut= Paths.get(
						ApiReceptionService.directoryReceptionEtatOK(repertoire, envExecution)
						, container
						).toFile();

				Files.copy(fIn.toPath(), fOut.toPath());
			}
			
		}
		
		message("Fin des déplacements de fichiers");

		
		do
		{
			
		message("Traitement Début");

		// plage d'initialisation

		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		 DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHH");

		 String lastInitialize=null;
         lastInitialize=UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("select last_init from arc.pilotage_batch "));


		 Date dNow = new Date();
		 Date dLastInitialize;

		 mapParam.put(KEY_FOR_BATCH_CHUNK_ID, dateFormat2.format(dNow));

	     dLastInitialize = dateFormat.parse(lastInitialize);
	     
	     // la nouvelle initialisation se lance directe : pas de plage horaire. Mais juste en cas de modification de regles.
	     // on lance toujours l'initialisation en mode "non production"
	     // on ne la lance que s'il n'y a rien en cours (pas essentiel mais plus sécurisé)
	     if ((!dejaEnCours && dLastInitialize.compareTo(dNow)<0) || !production)
	     {
	    	 message("Initialisation en cours");
	    	 initialiser.run();
			 message("Initialisation terminée : "+(int)initialiser.report.nbLines+" e : "+initialiser.report.duree+" ms");
			
			UtilitaireDao.get("arc").executeRequest(null, new PreparedStatementBuilder("update arc.pilotage_batch set last_init=to_char(current_date+interval '"+intervalForInitializationInDay+" days','yyyy-mm-dd')||':"+hourToTriggerInitializationInProduction+"' , operation=case when operation='R' then 'O' else operation end;"));
		}
		 productionOn=productionOn();


		 // Vérifier si la production est activée
		 if (productionOn)
		 {
		
		// Reception : ne faire que si il n'y a rien déjà en cours au début du batch
			 if (!dejaEnCours)
			 {
				 recevoir.run();
				 dejaEnCours=false;
			 }

			 if (production)
			 {
				 message("Reception : "+recevoir.report.nbLines+" e : "+recevoir.report.duree+" ms");
			 }
			 
			 fichierRestant=(int)recevoir.report.nbLines>0;

		// on lance tout systematiquement pour la reprise sur erreur

			int delay=poolingDelay/6;
			boolean exit=false;
		
			message("Début boucle Chargement->Mapping");
			do {
				
			if (!charger.isAlive())
			{
				charger=new ChargerThread();
				charger.start();
			}

			Sleep.sleep(delay);
			
			if (!normer.isAlive())
			{
				normer=new NormerThread();
				normer.start();
			}
			Sleep.sleep(delay);

			if (!controler.isAlive())
			{
				controler=new ControlerThread();
				controler.start();
			}

			Sleep.sleep(delay);
			
			if (!filtrer.isAlive())
			{
				filtrer=new FiltrerThread();
				filtrer.start();
			}

			Sleep.sleep(delay);
			
			if (!mapper.isAlive())
			{
				mapper=new MapperThread();
				mapper.start();
			}
			
			Sleep.sleep(delay);
			
			if (
					UtilitaireDao.get("arc").getInt(null,new PreparedStatementBuilder("select count(*) from (select 1 from "+envExecution+".pilotage_fichier where etape=1 limit 1) ww"))==0
					)
			{
					exit=true;
			}

			
			productionOn=productionOn();

			if (!productionOn)
			{
				break;
			}
			
			Sleep.sleep(delay);
		}
			while (!exit);


			if (productionOn)
			{
				// Effacer les fichiers du répertoire OK
				effacerRepertoireChargement(repertoire, envExecution);
			}

		}

		 // Maintenance du catalog
		ApiService.maintenancePgCatalog(null, "full");
			

		message("Traitement Fin");

		// si on n'est pas en production, on itere tant qu'il y a des fichiers dans le repertoire.
		}
		while (!production && recevoir.report.nbLines>0 && productionOn);
        
        if (args!=null && args.length>0 && args[0].equals("noExit"))
        {
        	message("No Exit");
        }
        else
        {
        	System.exit(0);
        }

	}
		

    } catch (Exception ex) {
         LoggerHelper.errorGenTextAsComment(BatchARC.class, "main()", LOGGER, ex);
		System.exit(202);
    }
	
	} while (fichierRestant);

	message("Fin du batch");

}


	/**
	 * Créer la table de pilotage batch si elle n'existe pas déjà
	 * @throws SQLException 
	 * @throws Exception
	 */
	public static void creerTablePilotageBatch() throws SQLException
	{
		PreparedStatementBuilder requete=new PreparedStatementBuilder();
		requete.append("\n CREATE TABLE IF NOT EXISTS arc.pilotage_batch (last_init text, operation text); ");
		requete.append("\n insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select 1 from arc.pilotage_batch); ");
        UtilitaireDao.get("arc").executeRequest(null, requete);
	}


	/**
	 * test si la chaine batch est arrétée
	 * @return
	 * @throws Exception
	 */
	public static boolean productionOn() throws Exception {
		if (production) {
			return UtilitaireDao.get("arc").hasResults(null, new PreparedStatementBuilder("select 1 from arc.pilotage_batch where operation='O'"));
		}
		else
		{
			return true;
		}
	}
	
	
	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * @param directory
	 * @param envExecution
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void effacerRepertoireChargement(String directory, String envExecution) throws IOException
	{
		
		// Effacer les fichiers des répertoires OK et KO
		String envDirectory = envExecution.replace(".", "_").toUpperCase();

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.OK);
		
		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.KO);
		
		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.ENCOURS);
		
	}

	private static void cleanDirectory(String directory, String envExecution, String envDirectory, TraitementEtat etat) throws IOException {
		File f= Paths.get(ApiReceptionService.directoryReceptionEtat(directory, envDirectory, etat)).toFile();
		if (!f.exists()) {
			return;
		}
		File[] fs= f.listFiles();
		for (File z:fs) {
			if (z.isDirectory()) {
				FileUtils.deleteDirectory(z);
			} else {
				deleteIfArchived(directory, envExecution, z);
			}
		}
	}

	/**
	 * If the file has already been moved in the archive directory by ARC
	 * it is safe to delete it
	 * else save it to the archive directory 
	 * @param repertoire
	 * @param envExecution
	 * @param z
	 * @return
	 */
	private static boolean deleteIfArchived(String repertoire, String envExecution, File z) {
		
		String entrepot =  ManipString.substringBeforeFirst(z.getName(),"_");
		String filename = ManipString.substringAfterFirst(z.getName(),"_");
		
		// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
		File fCheck = Paths.get(ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepot)
				, filename
				).toFile();
		
		if (fCheck.exists())
		{
			return z.delete();
		}
		else
		{
			return z.renameTo(fCheck);
		}
	}
	

}
