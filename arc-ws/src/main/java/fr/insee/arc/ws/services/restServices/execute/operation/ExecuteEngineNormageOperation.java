package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p3normage.bo.IdCardNormage;
import fr.insee.arc.core.service.p3normage.operation.NormageOperation;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.PhaseInterface;

/**
 * execte chargement operation
 * @author FY2QEQ
 *
 */
public class ExecuteEngineNormageOperation {
	
	public ExecuteEngineNormageOperation(Sandbox sandbox, ExecuteParameterModel bodyPojo, PhaseInterface phaseInterface) {
		super();
		this.sandbox = sandbox;
		this.bodyPojo = bodyPojo;
		this.phaseInterface = phaseInterface;
	}

	private Sandbox sandbox;
	private ExecuteParameterModel bodyPojo;
	private PhaseInterface phaseInterface;
	

	public PhaseInterface execute() throws IOException, ArcException
	{

		FileIdCard fileIdCard = new FileIdCard(bodyPojo.fileName);
		fileIdCard.setFileIdCard(bodyPojo.norme, bodyPojo.validite, bodyPojo.periodicite, phaseInterface.getStructure());
		fileIdCard.setIdCardNormage(new IdCardNormage(new ArrayList<>()));

		Map<String, List<String>> rubriqueUtiliseeDansRegles = new HashMap<>();
		rubriqueUtiliseeDansRegles.put("var", new ArrayList<>());

		NormageOperation normage = new NormageOperation(sandbox.getConnection(), fileIdCard, rubriqueUtiliseeDansRegles, phaseInterface.getInputTable(), phaseInterface.getOutputTable(),
				null);
		normage.executeEngine();
		
		PhaseInterface returnInterface = new PhaseInterface();
		returnInterface.setInputTable(phaseInterface.getOutputTable());
		returnInterface.setStructure(FormatSQL.unquoteTextWithoutEnclosings(phaseInterface.getStructure()));
		return returnInterface;
		
	}
	
}
