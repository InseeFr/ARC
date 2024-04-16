package fr.insee.arc.ws.services.importServlet.bo;

public enum ExportFormat {

	CSV_GZIP, BINARY;
	
	public static boolean isCsv(ExportFormat clientFormat) {
		return clientFormat.equals(CSV_GZIP);
	}
	
}
