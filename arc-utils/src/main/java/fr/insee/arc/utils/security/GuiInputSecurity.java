package fr.insee.arc.utils.security;

import java.util.List;
import java.util.stream.Collectors;

public class GuiInputSecurity {

	private GuiInputSecurity() {
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
		
		return guiInput.replaceAll("[^\\w$]", "") // remove all but world and $ symbol. worlds are 0-9a-zA-Z and _
				.replaceFirst("^[_$]*", "") // remove begin trailings $ and _
				.replaceFirst("[_$]*$", "") // remove end trailings $ and _
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
