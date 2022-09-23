package fr.insee.arc.core.model;

public class ServiceReporting {

	public ServiceReporting(Integer init, double d) {
		this.nbObject=init;
		this.duree=d;
	}

	public ServiceReporting() {
		this.nbObject=-1;
		this.duree=-1;
	}
	
	private Integer nbObject;
	private double duree;
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
	
	
	
	
}
