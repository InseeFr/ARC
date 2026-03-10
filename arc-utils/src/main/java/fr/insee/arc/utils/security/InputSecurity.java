package fr.insee.arc.utils.security;

import java.util.List;
import java.util.stream.Collectors;

public class InputSecurity {

	private InputSecurity() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * format an input field as a database identifier
	 * @param guiInput
	 * @return
	 */
	public static String formatAsDatabaseIdentifier(String guiInput) {
		
		if (guiInput==null)
		{
			return null;
		}
		
		return guiInput.replaceFirst("^[^a-zA-Z]+", "").replaceAll("[^[a-z][A-Z][0-9]_$]", "") // remove all but world and $ symbol. worlds are 0-9a-zA-Z and _
		;
	}
	
	public static List<String> formatAsDatabaseIdentifier(List<String> guiInputs) {
		if (guiInputs==null)
		{
			return null;
		}
		
		return guiInputs.stream().map(t->formatAsDatabaseIdentifier(t)).collect(Collectors.toList());
	}
	
	
	

}
