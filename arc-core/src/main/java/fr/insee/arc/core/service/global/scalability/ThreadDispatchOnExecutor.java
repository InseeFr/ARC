package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ThreadDispatchOnExecutor extends ThreadDispatchOn {
			
	private ThrowingConsumer<Connection> actionOnExecutor;
	private int executorConnectionIndex;
	
	
	public ThreadDispatchOnExecutor(int executorConnectionIndex, ThrowingConsumer<Connection> actionOnExecutor) {
		super();
		this.actionOnExecutor = actionOnExecutor;
		this.executorConnectionIndex = executorConnectionIndex;
	}

	public void actionOnExecutor() throws ArcException
	{
		// instanciate connexion
		try (Connection executorConnection = UtilitaireDao.get(executorConnectionIndex).getDriverConnexion())
		{
			actionOnExecutor.accept(executorConnection);

		} catch (SQLException e) {
			throw new ArcException(e, ArcExceptionMessage.DATABASE_CONNECTION_COORDINATOR_FAILED);
		}
	}

	@Override
	public void run()
	{
		try {
			actionOnExecutor();
		}
		 catch (ArcException e) {
			e.logFullException();
			this.setErrorInThread(true);
		}
	}
	
}
