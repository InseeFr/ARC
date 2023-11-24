package fr.insee.arc.ws.services.importServlet.bo;

public enum ExportFormat {

	CSV_GZIP("csv_gzip"), BINARY("binary");
	
	private String format;
	
	private ExportFormat(String format)
	{
		this.format = format;
	}

	public String getFormat() {
		return format;
	}
	
}
