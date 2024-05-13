package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
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
		if (coordinatorConnexion==null)
		{
			try (Connection newCoordinatorConnexion = UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getDriverConnexion())
			{
				actionOnCoordinator.accept(newCoordinatorConnexion);
				
			} catch (SQLException | ArcException e) {
				 ArcException customException = new ArcException(e, ArcExceptionMessage.DATABASE_CONNECTION_COORDINATOR_FAILED);
				 customException.logFullException();
				 throw customException;
			}
		}
		else
		{
			actionOnCoordinator.accept(coordinatorConnexion);
		}
		
		// dispatch when scaled
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		if (numberOfExecutorNods==0)
		{
			return numberOfExecutorNods;
		}
		
		for (int executorConnectionIndex=ArcDatabase.EXECUTOR.getIndex(); executorConnectionIndex<ArcDatabase.EXECUTOR.getIndex()+numberOfExecutorNods; executorConnectionIndex++ )
		{
			
			// instanciate connexion
			try (Connection executorConnection = UtilitaireDao.get(executorConnectionIndex).getDriverConnexion())
			{
				actionOnExecutor.accept(executorConnection);

			} catch (SQLException | ArcException e) {
				 ArcException customException = new ArcException(e, ArcExceptionMessage.DATABASE_CONNECTION_EXECUTOR_FAILED);
				 customException.logFullException();
				 throw customException;
			}
		}
		
		return numberOfExecutorNods;
		
	}
	
}
