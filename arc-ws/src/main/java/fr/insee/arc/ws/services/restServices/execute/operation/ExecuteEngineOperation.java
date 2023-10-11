package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p4controle.operation.ServiceJeuDeRegleOperation;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
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

						
						StringBuilder requete;
						
						requete = new StringBuilder();
						requete.append(
								"CREATE TEMPORARY TABLE "+currentTemporaryTable(i)+" as select *, '0'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules from "+previousTemporaryTable(i)+";");
						UtilitaireDao.get(0).executeImmediate(connection, requete);

						ServiceJeuDeRegleOperation sjdr = new ServiceJeuDeRegleOperation();

						// Récupération des règles de controles associées aux jeux de règle
						JeuDeRegle jdr = new JeuDeRegle();

						sjdr.fillRegleControle(connection, jdr, ViewEnum.CONTROLE_REGLE.getFullName(envExecution), currentTemporaryTable(i));
						sjdr.executeJeuDeRegle(connection, jdr, currentTemporaryTable(i));
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
				throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
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
