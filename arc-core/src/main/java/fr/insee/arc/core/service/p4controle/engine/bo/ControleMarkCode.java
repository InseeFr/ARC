package fr.insee.arc.core.service.p4controle.engine.bo;


/**
 * Modality codes used to mark records whether they succeed or fail a control rule
 * @author FY2QEQ
 *
 */
public enum ControleMarkCode {

	// Codes to mark record with error or not
	RECORD_WITH_NOERROR("0")
	,RECORD_WITH_ERROR_TO_EXCLUDE("1")
	,RECORD_WITH_ERROR_TO_KEEP("2")
	
	// Codes to know what to do when a rule fail
	,ERROR_ROW_PROCESSING_KEEP("k")
	,ERROR_ROW_PROCESSING_EXCLUDE("e")
	;
	
	private String code;

	private ControleMarkCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
	
}
