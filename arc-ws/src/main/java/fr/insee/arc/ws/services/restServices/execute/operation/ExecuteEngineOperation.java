package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.SecurityDao;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.PhaseInterface;
import fr.insee.arc.ws.services.restServices.execute.model.ResponseAttributes;
import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

public class ExecuteEngineOperation {
	
	private ReturnView returnView;
	private ResponseAttributes responseAttributes;
	private ExecuteParameterModel bodyPojo;
	
	public ExecuteEngineOperation(ReturnView returnView, ResponseAttributes responseAttributes,
			ExecuteParameterModel bodyPojo) {
		super();
		this.returnView = returnView;
		this.responseAttributes = responseAttributes;
		this.bodyPojo = bodyPojo;
	}

	public void executeEngineClient() throws ArcException {
			
		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {
			
			ExecuteRulesDao.fillRules(connection, bodyPojo, responseAttributes.getServiceName(), responseAttributes.getServiceId());
			
			String envExecution = bodyPojo.sandbox;
			
			Sandbox serviceSandbox = new Sandbox(connection, envExecution);

			// interface to exchange data between phases
			PhaseInterface phaseInterface = new PhaseInterface();

				for (int i = 2; i <= TraitementPhase.getPhase(bodyPojo.targetPhase).getOrdre(); i++) {

					switch (TraitementPhase.getPhase(i)) {
					case CHARGEMENT:

						phaseInterface.setOutputTable(currentTemporaryTable(i));
						ExecuteEngineChargementOperation chargementOperation = new ExecuteEngineChargementOperation(serviceSandbox, bodyPojo, phaseInterface);
						phaseInterface = chargementOperation.execute();
						
						break;

					case NORMAGE:
						
						phaseInterface.setOutputTable(currentTemporaryTable(i));
						ExecuteEngineNormageOperation normageOperation = new ExecuteEngineNormageOperation(serviceSandbox, bodyPojo, phaseInterface);
						phaseInterface = normageOperation.execute();

						
						break;
					case CONTROLE:

						phaseInterface.setOutputTable(currentTemporaryTable(i));
						ExecuteEngineControleOperation controleOperation = new ExecuteEngineControleOperation(serviceSandbox, bodyPojo, phaseInterface);
						phaseInterface = controleOperation.execute();
						break;
					default:
						break;
					}
				}
				
				ExecuteRulesDao.buildResponse(connection, bodyPojo, returnView, responseAttributes.getFirstContactDate());
			}
			catch (IOException e) {
				throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED);
			}
			catch (SQLException e) {
				throw new ArcException(ArcExceptionMessage.SQL_EXECUTE_FAILED, e.getMessage());
			}
		
	}
	
	
	// les tables temporaires des phases respectives valent a,b,c,d ...
	private final static int TEMPORARY_TABLE_ASCII_BASE_NAME=97;

	
	private String generateTemporaryTableName(int i)
	{
		return String.valueOf(((char) (TEMPORARY_TABLE_ASCII_BASE_NAME + i)));
	}
	
	private String currentTemporaryTable(int i)
	{
		return generateTemporaryTableName(i);
	}
	
	
	private String previousTemporaryTable(int i)
	{
		return generateTemporaryTableName(i-1);
	}
	
	
}
