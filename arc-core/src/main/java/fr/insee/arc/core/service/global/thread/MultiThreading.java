package fr.insee.arc.core.service.global.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.mutiphase.ApiMultiphaseService;
import fr.insee.arc.core.service.mutiphase.thread.ThreadMultiphaseService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.FormatSQL;

/**
 * class meant to start multithreading
 * 
 * @author FY2QEQ
 *
 * @param <U> : U is the thread model class
 * @param <T> : T is the thread class
 */
public class MultiThreading {

	protected static final Logger LOGGER = LogManager.getLogger(MultiThreading.class);

	// thread model
	ApiMultiphaseService threadModel;


	public MultiThreading(ApiMultiphaseService threadModel) {
		super();
		this.threadModel = threadModel;
	}

	public ThreadMultiphaseService getInstance() {
		return new ThreadMultiphaseService();
	}

	/**
	 * Iterate over thread to start them in parallel When the thread stack reaches
	 * {@param maxParallelWorkers}, the method will wait until a thread is released
	 * to start a new one
	 * 
	 * @param maxParallelWorkers
	 * @param listIdSource
	 * @param envExecution
	 * @param restrictedUserName
	 * @throws ArcException
	 */
	public void execute(int maxParallelWorkers, List<String> listIdSource, String envExecution,
			String restrictedUserName) {

		StaticLoggerDispatcher.info(LOGGER, "/* Generation des threads multiphase */");

		long dateDebut = java.lang.System.currentTimeMillis();

		// récupère le nombre de fichier à traiter
		int nbFichier = listIdSource.size();

		List<ScalableConnection> connexionList = new ArrayList<>();

		try {
			// prepare the connections
			// get the number of declared executor nodes
			int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();

			// if 0 executor nodes declared, index of the stack of the executors nod
			// connections is 0
			// if more than 0 executor nodes declared, index of the stack of the executors
			// nod connections is 1, 2, ...
			int startIndexOfExecutorNods = (numberOfExecutorNods == 0 ? 0 : 1);

			// set the pool of connections
			// dispatch file to nod id
			for (int i = startIndexOfExecutorNods; i <= numberOfExecutorNods; i++) {
				connexionList.addAll(prepareThreads(i, maxParallelWorkers, envExecution, restrictedUserName));
			}

			// dispatch files to a target nod
			Map<Integer, List<Integer>> filesByNods = dispatchFilesByNodId(listIdSource, startIndexOfExecutorNods,
					numberOfExecutorNods);

			// thread iteration
			iterateOverThreadConnections(filesByNods, connexionList);

			// close connections
		} finally {
			closeThreadConnections(connexionList);
		}

		long dateFin = java.lang.System.currentTimeMillis();

		StaticLoggerDispatcher.info(LOGGER, "Temp traitement des " + nbFichier + " fichiers : "
				+ Math.round((dateFin - dateDebut) / 1000F) + " sec");

	}

	/**
	 * Close the connections granted to threads
	 * 
	 * @param connexionList
	 * @throws ArcException
	 */
	private void closeThreadConnections(List<ScalableConnection> connexionList) {
		for (ScalableConnection connection : connexionList) {
			try {
				connection.closeAll();
			} catch (SQLException e) {
				new ArcException(e, ArcExceptionMessage.MULTITHREADING_CONNECTIONS_CLOSE_FAILED).logFullException();
			}
		}
	}

	/**
	 * Iterate thru connexion/thread Choose the file to be processed in the thread
	 * and start the thread Exit when all thread are dead and no more file to be
	 * proceed
	 * 
	 * @param filesByNods
	 * @param connexionList
	 * @throws ArcException
	 */
	private void iterateOverThreadConnections(Map<Integer, List<Integer>> filesByNods,
			List<ScalableConnection> connexionList) {
		int currentIndice;

		// register thread by connection (1-1 relationship)
		Map<ScalableConnection, ThreadMultiphaseService> threadByConnection = new HashMap<>();

		// iterate thru connexionList

		boolean exit = true;
		do {
			// exit condition
			exit = true;
			for (ScalableConnection connection : connexionList) {
				if (threadByConnection.get(connection) != null && threadByConnection.get(connection).getT().isAlive()) {
					exit = false;
				}

				// check if no thread registered for connection or if thread is dead
				if ((threadByConnection.get(connection) == null || !threadByConnection.get(connection).getT().isAlive())
						&& !filesByNods.get(connection.getNodIdentifier()).isEmpty()) {
					currentIndice = filesByNods.get(connection.getNodIdentifier()).remove(0);

					ThreadMultiphaseService r = getInstance();
					r.configThread(connection, currentIndice, threadModel);
					r.start();

					threadByConnection.put(connection, r);
					exit = false;
				}

			}
		} while (!exit);
	}

	/**
	 * According to its name, dispatch the file to a target nod id The file data
	 * must stay on the same nod id to be processed so hashcode is use to get a
	 * consistent bucket value
	 * 
	 * @param listIdSource
	 * @param startIndexOfExecutorNods
	 * @param numberOfExecutorNods
	 * @return
	 */
	protected static Map<Integer, List<Integer>> dispatchFilesByNodId(List<String> listIdSource,
			int startIndexOfExecutorNods, int numberOfExecutorNods) {
		Map<Integer, List<Integer>> filesByNodId = new HashMap<>();

		for (int i = startIndexOfExecutorNods; i <= numberOfExecutorNods; i++) {
			filesByNodId.put(i, new ArrayList<>());
		}

		for (int fileIndex = 0; fileIndex < listIdSource.size(); fileIndex++) {
			if (numberOfExecutorNods == 0) {
				filesByNodId.get(0).add(fileIndex);
			} else {
				int hashCode = listIdSource.get(fileIndex).hashCode();
				hashCode = (hashCode == Integer.MIN_VALUE) ? (Integer.MIN_VALUE + 1) : hashCode;
				int targetNodId = 1 + Math.abs(hashCode) % numberOfExecutorNods;
				filesByNodId.get(targetNodId).add(fileIndex);
			}
		}

		return filesByNodId;
	}

	/**
	 * Build the connection pool for mutithreading returns a list of connections
	 * usable by the threads
	 * 
	 * @param parallel
	 * @param connexion
	 * @param anEnvExecution
	 * @param restrictedUsername
	 * @return
	 */
	private static List<ScalableConnection> prepareThreads(int executorNodTarget, int parallelDegree,
			String anEnvExecution, String restrictedUsername) {
		List<ScalableConnection> connexionList = new ArrayList<>();
		try {

			// add thread connexions
			for (int i = 0; i < parallelDegree; i++) {

				Connection coordinatorConnexionTemp = UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex())
						.getDriverConnexion();
				// demote application user account to temporary restricted operations and
				// readonly or non-temporary schema
				configAndRestrictConnexion(ArcDatabase.COORDINATOR.getIndex(), anEnvExecution, restrictedUsername,
						coordinatorConnexionTemp);

				// prepare the thread connections for a BOTH COORDINATOR AND EXECUTOR NOD thread
				// only one connection on coordinator is required for such a thread
				if (executorNodTarget == ScalableConnection.BOTH_COORDINATOR_AND_EXECUTOR_NOD_IDENTIFIER) {
					connexionList.add(new ScalableConnection(coordinatorConnexionTemp));
				}
				// prepare thread connections for a specific EXCUTOR NOD thread
				// for this type of thread, it will require 2 connections
				// one for coordinator and one for executor
				else {
					Connection executorConnexionTemp = UtilitaireDao
							.get(ArcDatabase.EXECUTOR.getIndex() - 1 + executorNodTarget).getDriverConnexion();
					connexionList.add(
							new ScalableConnection(executorNodTarget, coordinatorConnexionTemp, executorConnexionTemp));
					configAndRestrictConnexion(executorNodTarget, anEnvExecution, restrictedUsername,
							executorConnexionTemp);
				}

			}

		} catch (Exception ex) {
			StaticLoggerDispatcher.error(LOGGER, "prepareThreads()", ex);
		}
		return connexionList;

	}

	/**
	 * configure a connection for arc process and restrict its database rights
	 * 
	 * @param poolId
	 * @param anEnvExecution
	 * @param restrictedUsername
	 * @param connection
	 * @throws ArcException
	 */
	private static void configAndRestrictConnexion(int poolId, String anEnvExecution, String restrictedUsername,
			Connection connection) throws ArcException {
		UtilitaireDao.get(poolId).executeRequest(connection,
					DatabaseConnexionConfiguration.configAndRestrictConnexionQuery(anEnvExecution, restrictedUsername)
				);
	}


}
