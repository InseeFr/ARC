package fr.insee.arc.utils.ressourceUtils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class ConnectionAttribute {

    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private String databaseDriverClassName;
    
    public ConnectionAttribute(String databaseUrl, String databaseUsername, String databasePassword,
			String databaseDriverClassName) {
		super();
		this.databaseUrl = databaseUrl;
		this.databaseUsername = databaseUsername;
		this.databasePassword = databasePassword;
		this.databaseDriverClassName = databaseDriverClassName;
	}

    
    
    /**
     * Ruby map from ci/cd : {0=>"zzz"},{1=>"xxxx"},{2=>"pass\"ji\""}
     * No ruby parser in java :(
     * Change string to json {0:"zzz",1:"xxxx",2:"pass\"ji\""} to be parsable in java
     * replace },{ by ,
     * replace => by :
     * then cast to json
     * @param rubyMapToken
     * @return
     */
    
    public static String[] unserialize(String rubyMapToken)
    {
    	// the rubyMapToken must begin with starString or it is not an expected rubyMapToken
    	String startString = "{0=>";
    	if (!rubyMapToken.startsWith(startString))
    	{
    		// it is not a ruby map key, no parsing required, return the provided string
    		return new String[] {rubyMapToken};
    	}
    	
    	String inputToken = "{0:" + rubyMapToken.substring(startString.length());
    	
    	// transform ruby in json to parse that correctly
        // replace },{ by ,
        // replace => by :
    	int numberOfToken=1;
    	for (int tokenId=1; tokenId < Integer.MAX_VALUE; tokenId++)
    	{
    		String toFind="\"},{"+tokenId+"=>\"";
    		if (inputToken.contains(toFind))
    		{
    			inputToken = inputToken.replace(toFind, "\","+tokenId+":\"");
    		}
    		else
    		{
    			numberOfToken= numberOfToken + tokenId -1;
    			break;
    		}
    	}
    	    	
    	// cast to json
    	JSONObject parsedMapTokens = new JSONObject(inputToken);    	
    	
    	// extract tokens
    	List<String> resultToken = new ArrayList<>();
    	for (int tokenId=0; tokenId < numberOfToken; tokenId++)
    	{
    		resultToken.add(parsedMapTokens.getString(tokenId+""));
    	}

    	return resultToken.toArray(new String[0]);
    	
    }
    
    
    
    
	public String getDatabaseUrl() {
		return databaseUrl;
	}
	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}
	public String getDatabaseUsername() {
		return databaseUsername;
	}
	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
	}
	public String getDatabasePassword() {
		return databasePassword;
	}
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}
	public String getDatabaseDriverClassName() {
		return databaseDriverClassName;
	}
	public void setDatabaseDriverClassName(String databaseDriverClassName) {
		this.databaseDriverClassName = databaseDriverClassName;
	}
    
    
    
	
}
