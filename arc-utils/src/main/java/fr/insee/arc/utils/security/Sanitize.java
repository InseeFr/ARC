package fr.insee.arc.utils.security;

public class Sanitize {

	private Sanitize() {
	    throw new IllegalStateException("Utility class");
	}
	
	public static String htmlParameter(String parameter) 
	{
		return parameter==null? null: parameter.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
	
	
	
}
