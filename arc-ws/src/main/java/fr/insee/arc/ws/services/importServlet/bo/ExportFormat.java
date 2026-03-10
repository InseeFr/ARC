package fr.insee.arc.ws.services.importServlet.bo;

public enum ExportFormat {

	CSV_GZIP, BINARY, PARQUET;

	public boolean isParquet() {
		return this.equals(PARQUET);
	}
	
}
