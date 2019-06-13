package fr.insee.arc_essnet.utils.utils;

public enum Services {
	QUERY( "query" ),
	CLIENT( "arcClient" ),
	TABLE_NAME( "tableName" ),
	TABLE_CONTENT( "tableContent" );

	
	private String service;
	
	private Services( String name ){
		this.service = name;
	}
	
	
	
	public String getService(){
		return this.service;
	}
}
