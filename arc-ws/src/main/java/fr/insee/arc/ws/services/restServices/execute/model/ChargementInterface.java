package fr.insee.arc.ws.services.restServices.execute.model;

public class ChargementInterface {
	
	public ChargementInterface(String outputTable) {
		super();
		this.outputTable = outputTable;
	}

	private String outputTable;

	public String getOutputTable() {
		return outputTable;
	}
	
	
}
