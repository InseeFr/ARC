package fr.insee.arc_essnet.core.factory;

import java.sql.Connection;

import fr.insee.arc_essnet.core.service.AbstractPhaseService;

public interface IServiceFactory {
	
	public AbstractPhaseService get(String... args);

	public AbstractPhaseService get(Connection connexion, String... args2);

}
