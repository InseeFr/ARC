package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.operation.ChargeurXmlComplexe;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.PhaseInterface;

/**
 * execte chargement operation
 * @author FY2QEQ
 *
 */
public class ExecuteEngineChargementOperation {
	
	public ExecuteEngineChargementOperation(Sandbox sandbox, ExecuteParameterModel bodyPojo, PhaseInterface phaseInterface) {
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
		// register file

		try (InputStream inputStream = new ByteArrayInputStream(
				bodyPojo.fileContent.getBytes(StandardCharsets.UTF_8));) {
			
			FileIdCard fileIdCard = new FileIdCard(bodyPojo.fileName);
			fileIdCard.setFileIdCard(bodyPojo.norme, bodyPojo.validite, bodyPojo.periodicite);
			
			ChargeurXmlComplexe chargeur = new ChargeurXmlComplexe(sandbox.getConnection(), sandbox.getSchema(), fileIdCard, inputStream, phaseInterface.getOutputTable());
			chargeur.executeEngine();
			
			PhaseInterface returnInterface = new PhaseInterface();
			returnInterface.setInputTable(phaseInterface.getOutputTable());
			returnInterface.setStructure(FormatSQL.unquoteTextWithoutEnclosings(chargeur.getJointure()));
			return returnInterface;
			
		}
	}
	
}
