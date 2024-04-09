package fr.insee.arc.utils.validator;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonValidator {

	private String input;
	
	private int maxSize = Integer.MAX_VALUE;
	private int minSize = 1;

	public JsonValidator(String input)
	{
		this.input = input;
	}
	
	public boolean validate()
	{
		// length validation
		if (this.input.length()>maxSize || this.input.length()<minSize)
		{
			return false;
		}
		
		// format validation
		try {		
			new JSONObject(input);
			return true;
		}
		catch (JSONException e)
		{
			return false;
		}
		
	}
	
	
}
