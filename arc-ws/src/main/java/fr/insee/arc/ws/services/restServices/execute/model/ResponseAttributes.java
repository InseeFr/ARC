package fr.insee.arc.ws.services.restServices.execute.model;

import java.util.Date;

import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

/**
 * les attributs de la réponse en mode engine
 * @author FY2QEQ
 *
 */
public class ResponseAttributes {

	public ResponseAttributes(String serviceName, int serviceId) {
		super();	
		
		this.serviceName=serviceName;
		this.serviceId=serviceId;
		
		this.firstContactDate=new Date();
		this.returnView=new ReturnView();
		this.identifiantLog = "(" + serviceName + ", " + serviceId + ")";
		
	}
	
	
	private String serviceName;
	private int serviceId;
	private Date firstContactDate;
	private ReturnView returnView;
	private String identifiantLog;
	
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public int getServiceId() {
		return serviceId;
	}
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	public Date getFirstContactDate() {
		return firstContactDate;
	}
	public void setFirstContactDate(Date firstContactDate) {
		this.firstContactDate = firstContactDate;
	}
	public ReturnView getReturnView() {
		return returnView;
	}
	public void setReturnView(ReturnView returnView) {
		this.returnView = returnView;
	}
	public String getIdentifiantLog() {
		return identifiantLog;
	}
	public void setIdentifiantLog(String identifiantLog) {
		this.identifiantLog = identifiantLog;
	}
	
	
	
	
}
