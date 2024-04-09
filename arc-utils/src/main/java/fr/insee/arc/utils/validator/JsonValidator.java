package fr.insee.arc.utils.validator;

public class JsonValidator {

	private String input;


	public JsonValidator(String input)
	{
		this.input = input;
	}
	
	public String validate()
	{
		input = input.replace("&", "&amp;");
		input = input.replace("<", "&lt;");
		input = input.replace(">", "&gt;");
	    return input;	    
		
	}
	
	
}
