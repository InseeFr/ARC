package fr.insee.arc.core.service.p5mapping.bo;

import java.util.List;

public class IdCardMapping {
	
	private List<RegleMapping> reglesMapping;
	
	public IdCardMapping(List<RegleMapping> reglesMapping) {
		super();
		this.reglesMapping = reglesMapping;
	}
	
	public List<RegleMapping> getReglesMapping() {
		return reglesMapping;
	}
	
	public void setReglesMapping(List<RegleMapping> reglesMapping) {
		this.reglesMapping = reglesMapping;
	}

}
