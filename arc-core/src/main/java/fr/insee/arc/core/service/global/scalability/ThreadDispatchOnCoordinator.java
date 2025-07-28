package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ThreadDispatchOnCoordinator extends Thread {

	private ThrowingConsumer<Connection> actionOnCoordinator;
	
	private Connection coordinatorConnexion;
	
	public ThreadDispatchOnCoordinator(ThrowingConsumer<Connection> actionOnCoordinator, Connection coordinatorConnexion) {
		super();
		this.actionOnCoordinator = actionOnCoordinator;
		this.coordinatorConnexion = coordinatorConnexion;
	}

	public void actionOnCoordinator() throws ArcException, SQLException
	{
		if (coordinatorConnexion==null)
		{
			try (Connection newCoordinatorConnexion = UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).getDriverConnexion())
			{
				actionOnCoordinator.accept(newCoordinatorConnexion);
				
			}
		}
		else
		{
			actionOnCoordinator.accept(coordinatorConnexion);
		}
	}

	@Override
	public void run()
	{
		try {
			actionOnCoordinator();
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
