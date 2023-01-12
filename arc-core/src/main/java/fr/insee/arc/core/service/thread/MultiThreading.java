package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;

/**
 * class meant to start multithreading
 * @author FY2QEQ
 *
 * @param <U> : U is the thread model class
 * @param <T> : T is the thread class
 */
public class MultiThreading<U, T extends ArcThread<U>> {
	
	protected static final Logger LOGGER = LogManager.getLogger(MultiThreading.class);

	// thread model
	U threadModel;
	
	// thread template
	T threadTemplate;
	
	public MultiThreading(U threadModel, T threadTemplate) {
		super();
		this.threadModel = threadModel;
		this.threadTemplate = threadTemplate;
	}


	@SuppressWarnings("unchecked")
	public T getInstance()
	{
		if (threadTemplate instanceof ThreadChargementService)
		{
			return (T) new ThreadChargementService();
		}
		
		if (threadTemplate instanceof ThreadNormageService)
		{
			return (T) new ThreadNormageService();
		}
		
		if (threadTemplate instanceof ThreadControleService)
		{
			return (T) new ThreadControleService();
		}

		if (threadTemplate instanceof ThreadFiltrageService)
		{
			return (T) new ThreadFiltrageService();
		}
		
		if (threadTemplate instanceof ThreadMappingService)
		{
			return (T) new ThreadMappingService();
		}
		
		throw new ArcException("Illegal class call");
	}
	
	

	/**
	 * Iterate over thread to start them in parallel
	 * When the thread stack reaches {@param maxParallelWorkers}, the method will wait until a thread is released to start a new one
	 * @param maxParallelWorkers
	 * @param listIdSource
	 * @param envExecution
	 * @param restrictedUserName
	 */
	public void execute(int maxParallelWorkers, List<String> listIdSource, String envExecution, String restrictedUserName )
	{       
       
        long dateDebut = java.lang.System.currentTimeMillis() ;

        // récupère le nombre de fichier à traiter
        int nbFichier = listIdSource.size();
        
        Connection connexionThread = null;
        ArrayList<T> threadList = new ArrayList<>();
        ArrayList<Connection> connexionList = prepareThreads(maxParallelWorkers, envExecution, restrictedUserName);
        int currentIndice = 0;

        StaticLoggerDispatcher.info("/* Generation des threads pour "+threadTemplate.getClass()+" */", LOGGER);
    	
        for (currentIndice = 0; currentIndice < nbFichier; currentIndice++) {
        	
        	connexionThread = chooseConnection(connexionThread, threadList, connexionList);

        	T r = getInstance(); 
        	r.configThread(connexionThread, currentIndice, threadModel);

            threadList.add(r);
            r.start();
            waitForThreads2(maxParallelWorkers, threadList);

        }

        waitForThreads2(0, threadList);


        for (Connection connection : connexionList) {
            try {
				connection.close();
			} catch (SQLException e) {
				throw new ArcException("Error in closing thread connections",e);
			}
        }
        

        
        long dateFin= java.lang.System.currentTimeMillis() ;
        
        StaticLoggerDispatcher.info("Temp chargement des "+ nbFichier+" fichiers : " + (int)Math.round((dateFin-dateDebut)/1000F)+" sec", LOGGER);
        
	}
	
	/**
	 * @param connextionThread
	 * @param threadList
	 * @param connexionList
	 * @return
	 */
	public Connection chooseConnection(Connection connextionThread, List<T> threadList,
			List<Connection> connexionList) {
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
	 * when max parallel degree is reached, wait for threads to be release then remove it for thread stack
	 * if the number of thread becomes less than parallel degree, it no longer waits and exits
	 * @param parallel : parallel degree. when parallel set to 0, it means "wait until all is done"
	 * @param threadList
	 * @throws ArcException
	 */
	public void waitForThreads2(int parallel, List<T> threadList) throws ArcException {

		while (threadList.size() >= parallel && !threadList.isEmpty()) {
			Iterator<T> it = threadList.iterator();

			while (it.hasNext()) {
				T px = it.next();
				if (!px.getT().isAlive()) {
					it.remove();

					// close connexion when threading is done
					// (first one)
					if (parallel == 0) {
						try {
							px.getConnexion().close();
						} catch (SQLException e) {
							throw new ArcException("Error in closing thread connection",e);
						}
					}

				}
			}
		}
	}
	
	/**
	 * Build the connection pool for mutithreading
	 * returns a list of connections usable by the threads 
	 * @param parallel
	 * @param connexion
	 * @param anEnvExecution
	 * @param restrictedUsername
	 * @return
	 */
	public static ArrayList<Connection> prepareThreads(int parallel, String anEnvExecution,
			String restrictedUsername) {
		ArrayList<Connection> connexionList = new ArrayList<>();
		try {

			// add thread connexions
			for (int i = 0; i < parallel; i++) {

				Connection connexionTemp = UtilitaireDao.get("arc").getDriverConnexion();
				connexionList.add(connexionTemp);

				// demote application user account to temporary restricted operations and
				// readonly or non-temporary schema
				UtilitaireDao.get("arc").executeImmediate(connexionTemp, ApiService.configConnection(anEnvExecution)
						+ (restrictedUsername.equals("") ? "" : FormatSQL.changeRole(restrictedUsername)));
			}

		} catch (Exception ex) {
			StaticLoggerDispatcher.error("prepareThreads()", ex, LOGGER);
		}
		return connexionList;

	}
	
}
