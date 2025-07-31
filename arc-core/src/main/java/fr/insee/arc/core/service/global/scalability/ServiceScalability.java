package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ServiceScalability {
	
	  private ServiceScalability() {
		    throw new IllegalStateException("Utility class to dispatch a treatment over connections");
		  }
	
	/**
	 * Execute actions on coordinator and on all the executors
	 * it is handy to propagate an action to the coordinator and all of the executors
	 * @param coordinatorConnexion
	 * @param actionOnCoordinator
	 * @param actionOnExecutor
	 * @return
	 * @throws ArcException
	 */
	public static int dispatchOnNods(Connection coordinatorConnexion, ThrowingConsumer<Connection> actionOnCoordinator, ThrowingConsumer<Connection> actionOnExecutor) throws ArcException
	{
		
		List<Thread> threadPool = new ArrayList<Thread>();	
		threadPool.add(new ThreadDispatchOnCoordinator(actionOnCoordinator, coordinatorConnexion));

		// dispatch when scaled
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		if (numberOfExecutorNods>0)
		{
			for (int executorConnectionIndex=ArcDatabase.EXECUTOR.getIndex(); executorConnectionIndex<ArcDatabase.EXECUTOR.getIndex()+numberOfExecutorNods; executorConnectionIndex++ )
			{
				threadPool.add(new ThreadDispatchOnExecutor(actionOnExecutor, executorConnectionIndex));
			}
		}
		
		// start threads
		for (Thread t : threadPool)
		{
			t.start();
		}
		
		// join threads
		boolean errorInThread = false;
		for (Thread t : threadPool)
		{
			try {
				t.join();
			} catch (InterruptedException e) {
				t.interrupt();
				errorInThread = true;
			}
		}
		
		if (errorInThread)
		{
			throw new ArcException(ArcExceptionMessage.MULTITHREADING_DISPATCH_FAILED);
		}
		
		return numberOfExecutorNods;
		
	}
	
}
