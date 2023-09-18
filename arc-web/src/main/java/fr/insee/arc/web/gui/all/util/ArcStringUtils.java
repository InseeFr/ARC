package fr.insee.arc.web.gui.all.util;

import java.util.Locale;

public class ArcStringUtils {

	private ArcStringUtils() {
		// Classe statique non instantiable
	}

	/** Cleans up a business variable for ARC : lowercase and trim.
	 * Returns null in the variable is null.*/
	public static String cleanUpVariable(String variable) {
		if (variable == null) {
			return null;
		}
		return variable.trim().toLowerCase(Locale.FRANCE);
	}

}
