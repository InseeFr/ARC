package fr.insee.arc.core.service.global.bo;

import java.util.Date;

import fr.insee.arc.core.service.global.dao.DateConversion;
import fr.insee.arc.core.service.p2chargement.bo.IdCardChargement;

public class FileIdCard {


	private String fileName;
	private String idNorme;
	private String validite;
	private String periodicite;
	private final String integrationDate = DateConversion.queryDateConversion(new Date());
	
    private IdCardChargement idCardChargement;
    public IdCardChargement getIdCardChargement() {
        return idCardChargement;
    }
    public void setIdCardChargement(IdCardChargement regleChargement) {
        this.idCardChargement = regleChargement;
    }
	
	public FileIdCard(String idSource) {
		this.fileName = idSource;
	}

	public void setFileIdCard(String idNorme, String validite, String periodicite) {
		this.idNorme = idNorme;
		this.validite = validite;
		this.periodicite = periodicite;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getValidite() {
		return validite;
	}


	public void setValidite(String validite) {
		this.validite = validite;
	}


	public String getIntegrationDate() {
		return integrationDate;
	}


	public String getIdNorme() {
		return idNorme;
	}


	public void setIdNorme(String idNorme) {
		this.idNorme = idNorme;
	}


	public String getPeriodicite() {
		return periodicite;
	}


	public void setPeriodicite(String periodicite) {
		this.periodicite = periodicite;
	}
	
	
	
	
}
