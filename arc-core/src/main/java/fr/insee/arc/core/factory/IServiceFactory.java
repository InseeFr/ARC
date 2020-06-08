package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiService;

public interface IServiceFactory {
	
	public ApiService get(String... args);

}
