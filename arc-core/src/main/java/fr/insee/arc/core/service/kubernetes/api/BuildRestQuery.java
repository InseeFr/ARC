package fr.insee.arc.core.service.kubernetes.api;

import org.springframework.http.HttpMethod;

import fr.insee.arc.core.service.kubernetes.bo.RestQuery;
import fr.insee.arc.utils.kubernetes.provider.KubernetesServiceLayer;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildRestQuery {

	private BuildRestQuery() {
		throw new IllegalStateException("Utility class");
	}

	private static PropertiesHandler properties = PropertiesHandler.getInstance();

	public static RestQuery deleteStateful(int executorReplicaIndex) {
		return new RestQuery() //
				.setUri(properties.getKubernetesApiUri() + "/apis/apps/v1/namespaces/"
						+ properties.getKubernetesApiNamespace() + "/statefulsets"
						+"/"+KubernetesServiceLayer.getName(properties.getKubernetesExecutorLabel(), executorReplicaIndex)
				)
				.setHttpMethod(HttpMethod.DELETE);
	}
	
	public static RestQuery deleteService(int executorReplicaIndex) {
		return new RestQuery() //
				.setUri(properties.getKubernetesApiUri() + "/api/v1/namespaces/"
						+ properties.getKubernetesApiNamespace() + "/services"
						+"/"+KubernetesServiceLayer.getName(properties.getKubernetesExecutorLabel(), executorReplicaIndex)
				)
				.setHttpMethod(HttpMethod.DELETE);
	}
	
	public static RestQuery createStateful() {
		return new RestQuery() //
				.setUri(properties.getKubernetesApiUri() + "/apis/apps/v1/namespaces/"
						+ properties.getKubernetesApiNamespace() + "/statefulsets")
				.setHttpMethod(HttpMethod.POST);
	}

	public static RestQuery createService() {
		return new RestQuery() //
				.setUri(properties.getKubernetesApiUri() + "/api/v1/namespaces/"
						+ properties.getKubernetesApiNamespace() + "/services")
				.setHttpMethod(HttpMethod.POST);
	}

}
