package fr.insee.arc.core.service.global.scalability;

import java.sql.Connection;
import java.sql.SQLException;

public class ScalableConnection {

	public static final int BOTH_COORDINATOR_AND_EXECUTOR_NOD_IDENTIFIER = 0;
	
	private Connection coordinatorConnection;
	private Connection executorConnection;
		
	private Integer nodIdentifier;

	public ScalableConnection(Connection coordinatorConnection) {
		super();
		this.coordinatorConnection = coordinatorConnection;
		this.executorConnection = coordinatorConnection;
		this.nodIdentifier=BOTH_COORDINATOR_AND_EXECUTOR_NOD_IDENTIFIER;
	}
	
	public ScalableConnection(int nodIdentifier, Connection coordinatorConnection, Connection executorConnection) {
		super();
		this.coordinatorConnection = coordinatorConnection;
		this.executorConnection = executorConnection;
		this.nodIdentifier=nodIdentifier;
	}
	
	public Connection getCoordinatorConnection() {
		return coordinatorConnection;
	}
	public void setCoordinatorConnection(Connection coordinatorConnection) {
		this.coordinatorConnection = coordinatorConnection;
	}

	public Connection getExecutorConnection() {
		return executorConnection;
	}

	public void setExecutorConnection(Connection executorConnection) {
		this.executorConnection = executorConnection;
	}

	public void closeAll() throws SQLException
	{
		if (coordinatorConnection!=null)
		{
			coordinatorConnection.close();
		}
		
		if (executorConnection!=null)
		{
			executorConnection.close();
		}
	}

	public Integer getNodIdentifier() {
		return nodIdentifier;
	}

	public void setNodIdentifier(Integer nodIdentifier) {
		this.nodIdentifier = nodIdentifier;
	}
	
	/**
	 * is the connexion scaled on nod ?
	 * @return
	 */
	public boolean isScaled()
	{
		return this.nodIdentifier!=BOTH_COORDINATOR_AND_EXECUTOR_NOD_IDENTIFIER;
	}
	
	
}
