package fr.insee.arc.ws.services.restServices.execute.model;

public class PhaseInterface {


	public PhaseInterface() {
		super();
	}


	private String inputTable;
	private String outputTable;
	
	// the structure of xml file
	private String structure;
	
	public String getStructure() {
		return structure;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public String getInputTable() {
		return inputTable;
	}

	public void setInputTable(String inputTable) {
		this.inputTable = inputTable;
	}

	public String getOutputTable() {
		return outputTable;
	}

	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}
	
	
	
}
