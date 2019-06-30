package fr.insee.arc.core.factory;

import java.sql.Connection;

import fr.insee.arc.core.service.AbstractPhaseService;

public interface IServiceFactory {
	
	public AbstractPhaseService get(String... args);

	public AbstractPhaseService get(Connection connexion, String... args2);

}
