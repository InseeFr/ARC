package fr.insee.arc.core.service.kubernetes.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.service.kubernetes.bo.JsonFileParameter;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.kubernetes.provider.KubernetesServiceLayer;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildJsonConfiguration {
	
	private BuildJsonConfiguration() {
		throw new IllegalStateException("Utility class");
	}
	
	private static PropertiesHandler properties = PropertiesHandler.getInstance();


	/**
	 * Create the json configuration to create <ExecutorNumber> replica of stateful databases
	 * @return
	 * @throws ArcException
	 */
	public static String stateful() throws ArcException
	{
		return readConfiguration("kubernetes/executorDatabaseStatefulTemplate.json"
				, JsonFileParameter.EXECUTOR_LABEL, properties.getKubernetesExecutorLabel() //
				, JsonFileParameter.EXECUTOR_NUMBER, String.valueOf(properties.getKubernetesExecutorNumber()) //
				, JsonFileParameter.EXECUTOR_PASSWORD, properties.getDatabasePassword() //
				)
				;
	}
	
	/**
	 * Create a set of json configuration for the set of services
	 * One service per replica
	 * @return
	 * @throws ArcException
	 */
	public static List<String> services() throws ArcException
	{
		List<String> servicesLayers = new ArrayList<>();
		
		for (int i=0; i< properties.getKubernetesExecutorNumber(); i++)
		{
			servicesLayers.add(service(i));
		}
		return servicesLayers;
	}
	
	
	/**
	 * Create the json configuration for the service matching the given executor replica index
	 * @param executorReplicaIndex
	 * @return
	 * @throws ArcException
	 */
	private static String service(int executorReplicaIndex) throws ArcException
	{
		PropertiesHandler properties = PropertiesHandler.getInstance();
		return readConfiguration("kubernetes/executorDatabaseServiceTemplate.json"
				, JsonFileParameter.EXECUTOR_LABEL, 
				KubernetesServiceLayer.getName(properties.getKubernetesExecutorLabel(), executorReplicaIndex)
				)
				;
	}
	
	
	private static String readConfiguration(String kubernetesConfigurationFileName, String...keyvalue) throws ArcException
	{
		String resource;
		try {
			resource = IOUtils.toString(
					ApiInitialisationService.class.getClassLoader().getResourceAsStream(kubernetesConfigurationFileName),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, kubernetesConfigurationFileName);
		}

		for (int i=0;i<keyvalue.length;i=i+2)
		{
			resource = resource.replace(keyvalue[i], keyvalue[i+1]);
		}
		
		return resource;
	}
	
}
