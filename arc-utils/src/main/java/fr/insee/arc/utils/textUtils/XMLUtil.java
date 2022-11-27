package fr.insee.arc.utils.textUtils;

import fr.insee.arc.utils.utils.ManipString;

public class XMLUtil {

	XMLUtil() {
	}

	/**
	 * retrouve la valeur d'une balise dans un string
	 * 
	 * @param inputString
	 * @param tag
	 * @return
	 */
	public static String parseXML(String inputString, String tag) {
		if (inputString != null && inputString.contains("<" + tag + ">")) {
			return ManipString.substringBeforeFirst(ManipString.substringAfterFirst(inputString, "<" + tag + ">"),
					"</" + tag + ">");
		}
		return null;
	}

}
