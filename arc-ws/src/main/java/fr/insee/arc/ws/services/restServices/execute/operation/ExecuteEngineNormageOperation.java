package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.operation.ChargeurXmlComplexe;
import fr.insee.arc.core.service.p3normage.operation.NormageOperation;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.ws.services.restServices.execute.model.ChargementInterface;
import fr.insee.arc.ws.services.restServices.execute.model.PhaseInterface;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;

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


		Map<String, List<String>> pil = new HashMap<>();
		pil.put(ColumnEnum.ID_SOURCE.getColumnName(), new ArrayList<>(Arrays.asList(bodyPojo.fileName)));
		pil.put("id_norme", new ArrayList<>(Arrays.asList(bodyPojo.norme)));
		pil.put("validite", new ArrayList<>(Arrays.asList(bodyPojo.validite)));
		pil.put("periodicite", new ArrayList<>(Arrays.asList(bodyPojo.periodicite)));
		pil.put("jointure", new ArrayList<>(Arrays.asList(phaseInterface.getStructure())));

		Map<String, List<String>> regle = new HashMap<>();
		regle.put("id_regle", new ArrayList<>());
		regle.put("id_norme", new ArrayList<>());
		regle.put("periodicite", new ArrayList<>());
		regle.put("validite_inf", new ArrayList<>());
		regle.put("validite_sup", new ArrayList<>());
		regle.put("id_classe", new ArrayList<>());
		regle.put("rubrique", new ArrayList<>());
		regle.put("rubrique_nmcl", new ArrayList<>());

		Map<String, List<String>> rubriqueUtiliseeDansRegles = new HashMap<>();
		rubriqueUtiliseeDansRegles.put("var", new ArrayList<>());

		NormageOperation normage = new NormageOperation(sandbox.getConnection(), pil, regle, rubriqueUtiliseeDansRegles, phaseInterface.getInputTable(), phaseInterface.getOutputTable(),
				null);
		normage.executeEngine();
		
		PhaseInterface returnInterface = new PhaseInterface();
		returnInterface.setInputTable(phaseInterface.getOutputTable());
		returnInterface.setStructure(FormatSQL.unquoteTextWithoutEnclosings(phaseInterface.getStructure()));
		return returnInterface;
		
	}
	
}
