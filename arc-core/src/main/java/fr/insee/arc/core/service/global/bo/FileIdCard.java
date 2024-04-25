package fr.insee.arc.core.service.global.bo;

import java.util.Date;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.dao.DateConversion;
import fr.insee.arc.core.service.p2chargement.bo.IdCardChargement;
import fr.insee.arc.core.service.p3normage.bo.IdCardNormage;
import fr.insee.arc.core.service.p4controle.bo.IdCardControle;
import fr.insee.arc.core.service.p5mapping.bo.IdCardMapping;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;

public class FileIdCard {

	private final GenericPreparedStatementBuilder integrationDate = DateConversion.queryDateConversion(new Date());

	private String idSource;
	private String idNorme;
	private String validite;
	private String periodicite;
	private String jointure;
	
    private IdCardChargement idCardChargement;
    
    public IdCardChargement getIdCardChargement() {
        return idCardChargement;
    }
    
    public void setIdCardChargement(IdCardChargement regleChargement) {
        this.idCardChargement = regleChargement;
    }
    
    private IdCardNormage idCardNormage;
    
	public IdCardNormage getIdCardNormage() {
		return idCardNormage;
	}

	public void setIdCardNormage(IdCardNormage idCardNormage) {
		this.idCardNormage = idCardNormage;
	}
    
    private IdCardControle idCardControle;
    
	public IdCardControle getIdCardControle() {
		return idCardControle;
	}

	public void setIdCardControle(IdCardControle idCardControle) {
		this.idCardControle = idCardControle;
	}
	
	private IdCardMapping idCardMapping;
    
	public IdCardMapping getIdCardMapping() {
		return idCardMapping;
	}

	public void setIdCardMapping(IdCardMapping idCardMapping) {
		this.idCardMapping = idCardMapping;
	}
	
	public FileIdCard(String idSource) {
		this.idSource = idSource;
	}

	/**
	 * Set the pilotage values for a file
	 * @param idNorme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 */
	public void setFileIdCard(String idNorme, String validite, String periodicite, String jointure) {
		this.idNorme = idNorme;
		this.validite = validite;
		this.periodicite = periodicite;
		this.jointure = jointure;
	}


	public String getIdSource() {
		return idSource;
	}


	public void setIdSource(String idSource) {
		this.idSource = idSource;
	}


	public String getValidite() {
		return validite;
	}


	public void setValidite(String validite) {
		this.validite = validite;
	}


	public GenericPreparedStatementBuilder getIntegrationDate() {
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
	
	

	public String getJointure() {
		return jointure;
	}

	public void setJointure(String jointure) {
		this.jointure = jointure;
	}
	
}
