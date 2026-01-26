package fr.insee.arc.core.model;

public enum ExportOption {

	ACTIVE("1");

	
	private String status;
	
	private ExportOption(String status)
	{
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
