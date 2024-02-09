package fr.insee.arc.core.service.kubernetes;

import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.core.service.kubernetes.api.BuildRestQuery;
import fr.insee.arc.core.service.kubernetes.configuration.BuildJsonConfiguration;
import fr.insee.arc.core.service.kubernetes.security.BuildAccessToken;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.kubernetes.KubernetesApiService;
import fr.insee.arc.utils.kubernetes.bo.KubernetesApiResult;

public class ApiManageExecutorDatabase {
	
	private ApiManageExecutorDatabase() {
		throw new IllegalStateException("Utility class");
	}

	public static List<KubernetesApiResult> create() throws ArcException
	{
		List<KubernetesApiResult> results = new ArrayList<>();
		
		// create stateful set of executor databases
		results.add(KubernetesApiService.execute(
				BuildRestQuery.stateful().getUri(), //
				BuildRestQuery.stateful().getHttpMethod(), //
				BuildAccessToken.retrieve().getToken(), //
				BuildJsonConfiguration.stateful()));
		
		// create services
		
		for (String serviceJsonConfiguration : BuildJsonConfiguration.services())
		{
			results.add(KubernetesApiService.execute(
					BuildRestQuery.service().getUri(), //
					BuildRestQuery.service().getHttpMethod(), //
					BuildAccessToken.retrieve().getToken(), //
					serviceJsonConfiguration));
		}
		
		return results;
	}
	
}
