package fr.insee.arc.core.service.kubernetes;

import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.core.service.kubernetes.api.BuildRestQuery;
import fr.insee.arc.core.service.kubernetes.bo.RestQuery;
import fr.insee.arc.core.service.kubernetes.configuration.BuildJsonConfiguration;
import fr.insee.arc.core.service.kubernetes.security.BuildAccessToken;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.kubernetes.KubernetesApi;
import fr.insee.arc.utils.kubernetes.bo.KubernetesApiResult;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class ApiManageExecutorDatabase {
	
	private ApiManageExecutorDatabase() {
		throw new IllegalStateException("Utility class");
	}
	
	private static final int MAX_NUMBER_OF_EXECUTORS = 16; 
	
	public static List<KubernetesApiResult> create() throws ArcException
	{
		List<KubernetesApiResult> results = new ArrayList<>();
		
		// create statefuls
		
		for (String statefulJsonConfiguration : BuildJsonConfiguration.statefuls())
		{
			RestQuery restQuery = BuildRestQuery.createStateful();
			results.add(KubernetesApi.execute(
					restQuery.getUri(), //
					restQuery.getHttpMethod(), //
					BuildAccessToken.retrieve().getToken(), //
					statefulJsonConfiguration));
		}
		
		
		// create services
		
		for (String serviceJsonConfiguration : BuildJsonConfiguration.services())
		{
			RestQuery restQuery = BuildRestQuery.createService();
			results.add(KubernetesApi.execute(
					restQuery.getUri(), //
					restQuery.getHttpMethod(), //
					BuildAccessToken.retrieve().getToken(), //
					serviceJsonConfiguration));
		}
		
		return results;
	}
	
	public static List<KubernetesApiResult> delete() throws ArcException
	{
		List<KubernetesApiResult> results = new ArrayList<>();

		RestQuery restQuery;

		
		// try to delete more executors than declared because the number of executor could had been lowered 
		// and it is important to clear old created databases
		for (int i=0; i< MAX_NUMBER_OF_EXECUTORS; i++)
		{
			
			restQuery = BuildRestQuery.deleteStateful(i);
			results.add(KubernetesApi.execute(
					restQuery.getUri(), //
					restQuery.getHttpMethod(), //
					BuildAccessToken.retrieve().getToken(), //
					null));
			
			restQuery = BuildRestQuery.deleteService(i);
			results.add(KubernetesApi.execute(
					restQuery.getUri(), //
					restQuery.getHttpMethod(), //
					BuildAccessToken.retrieve().getToken(), //
					null));
		}
		
		return results;
		
	}

}
