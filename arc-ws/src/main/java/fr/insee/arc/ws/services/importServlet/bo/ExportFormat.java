package fr.insee.arc.ws.services.importServlet.bo;

public enum ExportFormat {

	CSV_GZIP, BINARY, PARQUET;
	
	public static boolean isCsv(ExportFormat clientFormat) {
		return clientFormat.equals(CSV_GZIP);
	}
	
	public static boolean isParquet(ExportFormat clientFormat) {
		return clientFormat.equals(PARQUET);
	}
	
}
