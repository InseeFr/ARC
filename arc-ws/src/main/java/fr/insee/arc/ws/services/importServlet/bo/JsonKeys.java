package fr.insee.arc.ws.services.importServlet.bo;

public enum JsonKeys {
	//Requête
	CLIENT( "client" )
	,SERVICE( "service" )

	//Service "arcClient"
	,REPRISE( "reprise" )
	,ENVIRONNEMENT( "environnement" )
	,FAMILLE( "familleNorme" )
	,VALINF( "validiteInf" )
	,VALSUP( "validiteSup" )
	,PERIODICITE( "periodicite" )
    ,NBFICHIERS("nbfichiers")

	//Réponse
	,ID( "id" ) //Aussi utilisé dans les réponses quelque soit le service
	,TABLE( "table" )
	,FILEDATA("filedata")
	,SOURCE("source")

	;
	
	private String key;

	private JsonKeys( String name ){
		this.key = name;
	}



	public String getKey(){
		return this.key;
	}

}
