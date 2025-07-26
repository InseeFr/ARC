package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.IOException;
import java.util.ArrayList;

import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p3normage.bo.IdCardNormage;
import fr.insee.arc.core.service.p4controle.operation.ControleRulesOperation;
import fr.insee.arc.core.service.p4controle.operation.ServiceJeuDeRegleOperation;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.restServices.execute.dao.ExecuteEngineControleDao;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.PhaseInterface;

/**
 * execte chargement operation
 * @author FY2QEQ
 *
 */
public class ExecuteEngineControleOperation {
	
	public ExecuteEngineControleOperation(Sandbox sandbox, ExecuteParameterModel bodyPojo, PhaseInterface phaseInterface) {
		super();
		this.sandbox = sandbox;
		this.bodyPojo = bodyPojo;
		this.phaseInterface = phaseInterface;
	}

	private Sandbox sandbox;
	@SuppressWarnings("unused")
	private ExecuteParameterModel bodyPojo;
	private PhaseInterface phaseInterface;
	

	public PhaseInterface execute() throws IOException, ArcException
	{
		
		ExecuteEngineControleDao.execQueryCreateControleTable(this.sandbox.getConnection(), phaseInterface.getInputTable(), phaseInterface.getOutputTable());
		
		ServiceJeuDeRegleOperation sjdr = new ServiceJeuDeRegleOperation();
		
		FileIdCard fileIdCard = new FileIdCard(bodyPojo.fileName);
		fileIdCard.setFileIdCard(bodyPojo.norme, bodyPojo.validite, bodyPojo.periodicite, phaseInterface.getStructure());
		fileIdCard.setIdCardNormage(new IdCardNormage(new ArrayList<>()));
		
		ControleRulesOperation.fillControleRules(this.sandbox.getConnection(), sandbox.getSchema(), fileIdCard);

		sjdr.executeJeuDeRegle(this.sandbox.getConnection(), fileIdCard, phaseInterface.getOutputTable());
		
		
		PhaseInterface returnInterface = new PhaseInterface();
		returnInterface.setInputTable(phaseInterface.getOutputTable());
		returnInterface.setStructure(phaseInterface.getStructure());
		return returnInterface;
		
	}
	
}
