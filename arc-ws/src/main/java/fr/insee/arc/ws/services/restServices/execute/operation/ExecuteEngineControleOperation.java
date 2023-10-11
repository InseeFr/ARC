package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.IOException;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p4controle.operation.ServiceJeuDeRegleOperation;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
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

		// Récupération des règles de controles associées aux jeux de règle
		JeuDeRegle jdr = new JeuDeRegle();

		sjdr.fillRegleControle(this.sandbox.getConnection(), jdr, ViewEnum.CONTROLE_REGLE.getFullName(this.sandbox.getSchema()), phaseInterface.getOutputTable());
		sjdr.executeJeuDeRegle(this.sandbox.getConnection(), jdr, phaseInterface.getOutputTable());
		
		
		PhaseInterface returnInterface = new PhaseInterface();
		returnInterface.setInputTable(phaseInterface.getOutputTable());
		returnInterface.setStructure(FormatSQL.unquoteTextWithoutEnclosings(phaseInterface.getStructure()));
		return returnInterface;
		
	}
	
}
