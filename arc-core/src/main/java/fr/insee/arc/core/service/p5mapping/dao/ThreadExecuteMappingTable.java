package fr.insee.arc.core.service.p5mapping.dao;

import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.scalability.ThreadWithException;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class ThreadExecuteMappingTable extends ThreadWithException {

	private String envExecution;
	private int executorNod;
	private StringBuilder queryToExecute;

	public ThreadExecuteMappingTable(ScalableConnection connection, String envExecution, StringBuilder queryToExecute) {
		super();
		this.executorNod = ArcDatabase.EXECUTOR.getIndex() - 1 + connection.getNodIdentifier();
		this.envExecution = envExecution;
		this.queryToExecute = queryToExecute;
	}

	@Override
	public void run() {
		try (Connection executorConnexionTemp = UtilitaireDao.get(this.executorNod).getDriverConnexion();) {
			
			PropertiesHandler properties = PropertiesHandler.getInstance();
			String restrictedUsername = properties.getDatabaseRestrictedUsername();
			
			GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
			query.append(DatabaseConnexionConfiguration.configAndRestrictConnexionQuery(this.envExecution, restrictedUsername));
			query.append(queryToExecute);
			
			UtilitaireDao.get(0).executeRequest(executorConnexionTemp, query);
			
		} catch (ArcException e) {
			e.logFullException();
			this.setErrorInThread(true);
		} catch (SQLException e1) {
			new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_EXECUTOR_FAILED,e1).logFullException();
			this.setErrorInThread(true);
		}

	}
	

}
