package fr.insee.arc.core.service.global;

import fr.insee.arc.utils.exception.ArcException;

public class ServiceReporting {

	public ServiceReporting(Integer init, double d, ArcException e) {
		this.nbObject=init;
		this.duree=d;
		this.exception = e;
	}

	public ServiceReporting() {
		this.nbObject=-1;
		this.duree=-1;
	}
	
	private Integer nbObject;
	private double duree;
	private ArcException exception;
	
	public Integer getNbObject() {
		return nbObject;
	}

	public void setNbObject(Integer nbObject) {
		this.nbObject = nbObject;
	}

	public double getDuree() {
		return duree;
	}

	public void setDuree(double duree) {
		this.duree = duree;
	}

	public ArcException getException() {
		return exception;
	}

	public void setException(ArcException exception) {
		this.exception = exception;
	}
	
	
	
	
}
