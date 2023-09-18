package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
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
	public static int dispatchOnNods(Connection coordinatorConnexion, ThrowingConsumer<Connection, ArcException> actionOnCoordinator, ThrowingConsumer<Connection, ArcException> actionOnExecutor) throws ArcException
	{
		actionOnCoordinator.accept(coordinatorConnexion);

		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		// meta data copy is only necessary when scaled
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
				 ArcException customException = new ArcException(e, ArcExceptionMessage.DATABASE_INITIALISATION_SCRIPT_FAILED);
				 customException.logFullException();
				 throw customException;
			}
		}
		
		return numberOfExecutorNods;
		
	}
	
}
