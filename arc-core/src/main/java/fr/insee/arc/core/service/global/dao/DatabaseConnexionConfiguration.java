package fr.insee.arc.core.service.global.dao;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.ModeRequeteImpl;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.FormatSQL;

public class DatabaseConnexionConfiguration {

	private DatabaseConnexionConfiguration() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Permet de configurer la connexion Mettre un timeout par exemple
	 */
	public static GenericPreparedStatementBuilder configConnection(String anEnvExecution) {
		return ModeRequeteImpl.arcModeRequeteEngine(anEnvExecution);
	}
	

	/**
	 * promote the application to the full right user role if required. required is
	 * true if the restrictedUserAccount exists
	 * 
	 * @throws ArcException
	 */
	public static GenericPreparedStatementBuilder switchToFullRightRole() {
		PropertiesHandler properties = PropertiesHandler.getInstance();
		if (!properties.getDatabaseRestrictedUsername().equals("")) {
			return FormatSQL.resetRole();
		}
		return new GenericPreparedStatementBuilder();
	}

	
	public static GenericPreparedStatementBuilder configAndRestrictConnexionQuery(String anEnvExecution, String restrictedUsername)
	{
		GenericPreparedStatementBuilder query = DatabaseConnexionConfiguration.configConnection(anEnvExecution);
		query.append((restrictedUsername.equals("") ? new GenericPreparedStatementBuilder() : FormatSQL.changeRole(restrictedUsername)));
		return query;
	}
	
}
