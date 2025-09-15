package fr.insee.arc.core.service.global.scalability;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class CopyFromCoordinatorToExecutors {

	private int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
	private List<ThreadConsumer> tcs;
	private List<ThreadDispatchOn> threadsToExecute;
	
	// tee outputStream
	// it copy data to several outputstreams


	public void copyWithTee(String table) throws ArcException {
		this.copyWithTee(table, table);
	}

	public void copyWithTee(String tableIn, String tableOut) throws ArcException {

		// if no executors, return, no copy to do
		if (numberOfExecutorNods == 0) {
			return;
		}

		createOutputTables(tableIn, tableOut);

		initializeStreamPipes(tableIn, tableOut);

		streamData();


	}

	/**
	 * Stream the data from tableIn to outputstream
	 * @param tableIn
	 * @throws ArcException 
	 */
	private void streamData() throws ArcException {

		ThreadDispatchOn.execute(threadsToExecute);
		
	}

	
	/**
	 * Connect the ouput stream that recieve data to the multiple inputstreams that
	 * will read data ans write them to the distant database
	 * 
	 * @param tableOut
	 * @return
	 * @throws ArcException
	 */
	private void initializeStreamPipes(String tableIn, String tableOut) throws ArcException {
		
		@SuppressWarnings("resource")
		ArcTeeOutputStream tos = new ArcTeeOutputStream();
		this.tcs = new ArrayList<ThreadConsumer>();

		for (int executorConnectionIndex = ArcDatabase.EXECUTOR
				.getIndex(); executorConnectionIndex < ArcDatabase.EXECUTOR.getIndex()
						+ numberOfExecutorNods; executorConnectionIndex++) {
			try {
				ArcPipedInputStream pin = new ArcPipedInputStream();
				tos.add(new ArcPipedOutputStream(pin));
				tcs.add(new ThreadConsumer(pin, executorConnectionIndex, tableOut));
			} catch (IOException e) {
				throw new ArcException(ArcExceptionMessage.STREAM_WRITE_FAILED);
			}
		}
		
		ThreadProducer p = new ThreadProducer(tos, 0, tableIn);
		
		// add threads to the list of thread to execute
		threadsToExecute = new ArrayList<>();
		threadsToExecute.add(p);
		threadsToExecute.addAll(tcs);


	}

	private void createOutputTables(String tableIn, String tableOut) throws ArcException {
		try (Connection connectionIn = UtilitaireDao.get(0).getDriverConnexion()) {

			for (int executorConnectionIndex = ArcDatabase.EXECUTOR
					.getIndex(); executorConnectionIndex < ArcDatabase.EXECUTOR.getIndex()
							+ numberOfExecutorNods; executorConnectionIndex++) {
				try (Connection connectionOut = UtilitaireDao.get(executorConnectionIndex).getDriverConnexion();) {
					CopyObjectsToDatabase.createOutputTableIfRequired(connectionOut, tableOut,
							UtilitaireDao.get(0).retrieveColumnAttributes(connectionIn, tableIn), true);

				} catch (SQLException e) {
					throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_EXECUTOR_FAILED);
				}
			}
		} catch (SQLException e1) {
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_EXECUTOR_FAILED);
		}
	}

}
