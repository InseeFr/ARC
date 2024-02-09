package fr.insee.arc.core.service.kubernetes.api;

import org.springframework.http.HttpMethod;

import fr.insee.arc.core.service.kubernetes.bo.RestQuery;
import fr.insee.arc.utils.kubernetes.KubernetesApi;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildRestQuery {

	private BuildRestQuery() {
		throw new IllegalStateException("Utility class");
	}

	private static PropertiesHandler properties = PropertiesHandler.getInstance();

	public static RestQuery stateful() {
		return new RestQuery() //
				.setUri(properties.getKubernetesApiUri() + "/apis/apps/v1/namespaces/"
						+ properties.getKubernetesApiNamespace() + "/statefulsets")
				.setHttpMethod(HttpMethod.POST);
	}

	public static RestQuery service() {
		return new RestQuery() //
				.setUri(properties.getKubernetesApiUri() + "/api/v1/namespaces/"
						+ properties.getKubernetesApiNamespace() + "/services")
				.setHttpMethod(HttpMethod.POST);
	}

}
