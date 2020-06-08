package fr.insee.arc.utils.utils;

public enum Services {
	QUERY( "query" ),
	CLIENT( "arcClient" ),
	TABLE_NAME( "tableName" ),
	TABLE_CONTENT( "tableContent" ),
	RUN( "run" );

	private String service;
	
	private Services( String name ){
		this.service = name;
	}
	
	
	
	public String getService(){
		return this.service;
	}
}
