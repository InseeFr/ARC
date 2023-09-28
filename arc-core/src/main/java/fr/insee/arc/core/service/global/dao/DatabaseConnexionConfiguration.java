package fr.insee.arc.core.service.global.dao;

import fr.insee.arc.utils.dao.ModeRequeteImpl;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class DatabaseConnexionConfiguration {

	private DatabaseConnexionConfiguration() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Permet de configurer la connexion Mettre un timeout par exemple
	 */
	public static StringBuilder configConnection(String anEnvExecution) {
		StringBuilder requete = new StringBuilder();
		requete.append(ModeRequeteImpl.arcModeRequeteEngine(ManipString.substringBeforeFirst(TableNaming.dbEnv(anEnvExecution), ".")));
		return requete;
	}
	

	/**
	 * promote the application to the full right user role if required. required is
	 * true if the restrictedUserAccount exists
	 * 
	 * @throws ArcException
	 */
	public static String switchToFullRightRole() {
		PropertiesHandler properties = PropertiesHandler.getInstance();
		if (!properties.getDatabaseRestrictedUsername().equals("")) {
			return FormatSQL.changeRole(properties.getDatabaseUsername());
		}
		return "";
	}

	
}
