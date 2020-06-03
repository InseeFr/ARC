package fr.insee.arc.core.dao;

import java.util.Map;

public class SqlToJavaMapper {
	
	private Map<String, String> bdToJavaType;

	public Map<String, String> getBdToJavaType() {
		return bdToJavaType;
	}

	public void setBdToJavaType(Map<String, String> bdToJavaType) {
		this.bdToJavaType = bdToJavaType;
	}

}
