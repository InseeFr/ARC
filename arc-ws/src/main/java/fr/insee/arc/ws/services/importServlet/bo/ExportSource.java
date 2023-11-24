package fr.insee.arc.ws.services.importServlet.bo;

public enum ExportSource {

	NOMENCLATURE("nomenclature"), METADATA("metadata"), MAPPING("mapping");

	private String source;

	private ExportSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

}
