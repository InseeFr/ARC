package fr.insee.arc.core.service.p2chargement.bo;

import java.util.Date;

import fr.insee.arc.core.service.global.dao.DateConversion;

public class FileAttributes {


	private String[] headers;
	private String[] headersV;
	private String[] headersI;
	private String fileName;
	private String validite;
	private final String integrationDate = DateConversion.queryDateConversion(new Date());
	
	
	public FileAttributes(String fileName, String validite) {
		super();
		this.fileName = fileName;
		this.validite = validite;
	}


	public String[] getHeaders() {
		return headers;
	}


	public void setHeaders(String[] headers) {
		this.headers = headers;
	}


	public String[] getHeadersV() {
		return headersV;
	}


	public void setHeadersV(String[] headersV) {
		this.headersV = headersV;
	}


	public String[] getHeadersI() {
		return headersI;
	}


	public void setHeadersI(String[] headersI) {
		this.headersI = headersI;
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
	
	
	
	
}
