package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ThreadDispatchOnExecutor extends Thread {
			
	private ThrowingConsumer<Connection> actionOnExecutor;
	private int executorConnectionIndex;
	
	
	public ThreadDispatchOnExecutor(ThrowingConsumer<Connection> actionOnExecutor, int executorConnectionIndex) {
		super();
		this.actionOnExecutor = actionOnExecutor;
		this.executorConnectionIndex = executorConnectionIndex;
	}

	public void actionOnExecutor() throws ArcException, SQLException
	{
		// instanciate connexion
		try (Connection executorConnection = UtilitaireDao.get(executorConnectionIndex).getDriverConnexion())
		{
			actionOnExecutor.accept(executorConnection);

		}
	}

	public void run()
	{
		try {
			actionOnExecutor();
		}
		 catch (SQLException e) {
			 ArcException customException = new ArcException(e, ArcExceptionMessage.DATABASE_CONNECTION_COORDINATOR_FAILED);
			 customException.logFullException();
			this.interrupt();
		}
		 catch (ArcException e) {
			e.logFullException();
			this.interrupt();
		}
	}
	
}
