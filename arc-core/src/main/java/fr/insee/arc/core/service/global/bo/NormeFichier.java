package fr.insee.arc.core.service.global.bo;

public class NormeFichier {
	private String idNorme;
	private String validite;
	private String periodicite;
	
	
		
	public NormeFichier(String idNorme, String validite, String periodicite) {
		super();
		this.idNorme = idNorme;
		this.validite = validite;
		this.periodicite = periodicite;
	}
	
	public String getIdNorme() {
		return idNorme;
	}
	public void setIdNorme(String idNorme) {
		this.idNorme = idNorme;
	}
	public String getValidite() {
		return validite;
	}
	public void setValidite(String validite) {
		this.validite = validite;
	}
	public String getPeriodicite() {
		return periodicite;
	}
	public void setPeriodicite(String periodicite) {
		this.periodicite = periodicite;
	}	
	
	
}    

