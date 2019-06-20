package fr.insee.arc_essnet.core.service;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.dao.PilotageDAO;
import fr.insee.arc_essnet.core.model.BddTable;
import fr.insee.arc_essnet.core.model.DbConstant;
import fr.insee.arc_essnet.core.model.TraitementState;
import fr.insee.arc_essnet.core.service.thread.AbstractThreadService;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.queryhandler.BatchQueryHandler;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class that all the phaseService class need to extends. Provide a
 * generic way to create and run all the needed thread
 * 
 * @author S4lwo8
 *
 * @param <T>
 *            : the thread service that this class will create and launch
 */
public abstract class AbstractThreadRunnerService<T extends AbstractThreadService> extends AbstractPhaseService {
    protected static final Logger LOGGER = Logger.getLogger(AbstractThreadRunnerService.class);

    protected Class<T> classThread;

    public AbstractThreadRunnerService(Class<T> aClassThread, String aCurrentPhase, String anParametersEnvironment,
	    String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String[] paramBatch) {
	super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	LoggerDispatcher.info(String.format("** Constructor %s**", aCurrentPhase), LOGGER);

	this.classThread = aClassThread;
    }

    public AbstractThreadRunnerService(Connection connexion, Class<T> aClassThread, String aCurrentPhase,
	    String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
	    String[] paramBatch) {
	super(connexion, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	LoggerDispatcher.info(String.format("** Constructor %s**", aCurrentPhase), LOGGER);

	this.classThread = aClassThread;
    }

    public AbstractThreadRunnerService() {
	super();
    }

    @Override
    public void process() throws Exception {
	threadRunner();

    }

    /**
     * Method that create and run all the thread of a phase.
     * 
     * @throws Exception
     */
    public void threadRunner() throws Exception {

	LoggerDispatcher.info("** threadRunner **", LOGGER);
	int currentIndice;

	long startTime = java.lang.System.currentTimeMillis();

	// Get the number of file to process
	@SuppressWarnings("resource")
	PilotageDAO pilotageDao = new PilotageDAO(new BatchQueryHandler(this.getConnection()),
		this.getBddTable().getContextName(BddTable.ID_TABLE_PILOTAGE_TEMP));

	this.filesToProcess = pilotageDao.getFilesToProcess(this.phaseName, TraitementState.ENCOURS.toString());

	int nbFichier = filesToProcess.size();
	LoggerDispatcher.info(String.format("There are %s files to process", nbFichier), LOGGER);

	Connection connectionThread = null;

	// Thread pool
	List<AbstractThreadService> threadList = new ArrayList<>();

	// Connection pool
	List<Connection> connexionList = prepareThreads(nbThread, null);
	currentIndice = 0;

	LoggerDispatcher.info(String.format("** %s thread generation**", this.phaseName), LOGGER);
	for (currentIndice = 0; currentIndice < nbFichier; currentIndice++) {

	    if (currentIndice % 10 == 0) {
		LoggerDispatcher.info(String.format("%s file %s/%s", this.phaseName, currentIndice, nbFichier), LOGGER);
	    }

	    connectionThread = chooseConnection(connectionThread, threadList, connexionList);

	    AbstractThreadService thread = createThread(currentIndice, connectionThread);

	    threadList.add(thread);
	    thread.start();

	    waitForAbstractServiceThreads(nbThread, threadList, connexionList);

	}

	LoggerDispatcher.info("**Wait for threads**", LOGGER);
	waitForAbstractServiceThreads(0, threadList, connexionList);

	LoggerDispatcher.info("** Connection closure **", LOGGER);
	for (Connection connection : connexionList) {
	    connection.close();
	}

	long dateFin = java.lang.System.currentTimeMillis();
	LoggerDispatcher.info(
		"Process time : " + nbFichier + " Files : " + Math.round((dateFin - startTime) / 1000F) + " sec",
		LOGGER);

    }

    /**
     * Create a thread using reflexion
     * 
     * @param an
     *            indice to identify the thread
     * @param a
     *            connection
     * @return an AbstractThreadService
     * @throws Exception
     */
    private AbstractThreadService createThread(int currentIndice, Connection connectionThread) throws Exception {
	Constructor<T> constructeur = classThread.getDeclaredConstructor(int.class, this.getClass(), Connection.class);

	constructeur.setAccessible(true);
	return constructeur.newInstance(currentIndice, this, connectionThread);
    }

    /**
     * Wait for the end of a thread
     * 
     * @param parallel
     * @param threadList
     * @param connexionList
     * @throws Exception
     */
    public void waitForAbstractServiceThreads(int parallel, List<? extends AbstractThreadService> threadList,
	    List<Connection> connexionList) throws Exception {

	while (threadList.size() >= parallel && threadList.size() > 0) {
	    Iterator<? extends AbstractThreadService> it = threadList.iterator();

	    while (it.hasNext()) {
		AbstractThreadService px = it.next();
		if (!px.getActualThread().isAlive()) {
		    px = null;
		    it.remove();

		}
	    }
	}
	;

	if (parallel == 0) {
	    for (int i = 1; i < connexionList.size(); i++) {
		try {
		    connexionList.get(i).close();
		} catch (SQLException ex) {
		}
	    }
	}

    }

    public static List<Connection> prepareThreads(int parallel, Connection connexion) {
	ArrayList<Connection> connexionList = new ArrayList<>();

	if (connexion != null) {
	    connexionList.add(connexion);
	}

	for (int i = connexionList.size(); i < parallel; i++) {
	    try {
		Connection connexionTemp = UtilitaireDao.get(DbConstant.POOL_NAME).getDriverConnexion();

		connexionList.add(connexionTemp);

	    } catch (Exception ex) {
		LoggerHelper.error(LOGGER, AbstractPhaseService.class, "prepareThreads()", ex);
	    }
	}

	return connexionList;

    }

    /**
     * Get a free connection for the thread
     * 
     * FIXME
     * 
     * @param connectionThread
     * @param threadList
     * @param connexionList
     * @return a free connection
     */
    public Connection chooseConnection(Connection connectionThread, List<? extends AbstractThreadService> threadList,
	    List<Connection> connexionList) {
	// loop on the connection pool to fine a free one
	for (int i = 0; i < connexionList.size(); i++) {
	    boolean choosen = true;

	    for (int j = 0; j < threadList.size(); j++) {
		if (connexionList.get(i).equals(threadList.get(j).getConnection())) {
		    choosen = false;
		}
	    }

	    if (choosen) {
		connectionThread = connexionList.get(i);
		break;
	    }
	}
	return connectionThread;
    }


}
