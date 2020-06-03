package fr.insee.arc.core.model;

public class ServiceReporting {

	public ServiceReporting(Integer init, double d) {
		this.nbLines=init;
		this.duree=d;
	}

	public ServiceReporting() {
		this.nbLines=-1;
		this.duree=-1;
	}
	
	public Integer nbLines;
	public double duree;
	
	
}
