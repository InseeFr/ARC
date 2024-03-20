package fr.insee.arc.core.service.kubernetes.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.apache.commons.io.IOUtils;

import fr.insee.arc.core.service.kubernetes.bo.JsonFileParameter;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;
import fr.insee.arc.utils.consumer.ThrowingFunction;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.kubernetes.provider.KubernetesServiceLayer;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildJsonConfiguration {
	
	private BuildJsonConfiguration() {
		throw new IllegalStateException("Utility class");
	}
	
	private static PropertiesHandler properties = PropertiesHandler.getInstance();

	
	public static List<String> listOfReplicaConfiguration(ThrowingFunction<Integer, String> getSingleReplicaConfiguration) throws ArcException
	{
		List<String> statefulLayers = new ArrayList<>();
		
		for (int i=0; i< properties.getKubernetesExecutorNumber(); i++)
		{
			statefulLayers.add(getSingleReplicaConfiguration.apply(i));
		}
		return statefulLayers;
	}
	
	
	
	public static List<String> statefuls() throws ArcException
	{
		return listOfReplicaConfiguration(BuildJsonConfiguration::replicaStatefulConfiguration);
	}

	
	/**
	 * Create a set of json configuration for the set of services
	 * One service per replica
	 * @return
	 * @throws ArcException
	 */
	public static List<String> services() throws ArcException
	{
		return listOfReplicaConfiguration(BuildJsonConfiguration::replicaServiceConfiguration);
	}
	
	
	/**
	 * Create the json configuration to create <ExecutorNumber> replica of stateful databases
	 * @return
	 * @throws ArcException
	 */
	public static String replicaStatefulConfiguration(int executorReplicaIndex) throws ArcException
	{
		String kubernetesConfiguration;
		try {
			kubernetesConfiguration = IOUtils.toString(
					ApiInitialisationService.class.getClassLoader().getResourceAsStream("kubernetes/executorDatabaseStatefulTemplate.jsonnet"),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, "executorDatabaseStatefulTemplate.jsonnet").logFullException();
		}		
		return applyKubernetesParameters(kubernetesConfiguration, kubernetesParameters(executorReplicaIndex));
	}
	
	/**
	 * Create the json configuration for the service matching the given executor replica index
	 * @param executorReplicaIndex
	 * @return
	 * @throws ArcException
	 */
	private static String replicaServiceConfiguration(int executorReplicaIndex) throws ArcException
	{
		String kubernetesConfiguration;
		try {
			kubernetesConfiguration = IOUtils.toString(
					ApiInitialisationService.class.getClassLoader().getResourceAsStream("kubernetes/executorDatabaseServiceTemplate.jsonnet"),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, "executorDatabaseServiceTemplate.jsonnet");
		}		
		return applyKubernetesParameters(kubernetesConfiguration, kubernetesParameters(executorReplicaIndex));
	}
	
	private static String[] kubernetesParameters(int executorReplicaIndex)
	{
		return new String[] {JsonFileParameter.EXECUTOR_LABEL, KubernetesServiceLayer.getName(properties.getKubernetesExecutorLabel(), executorReplicaIndex) //
				, JsonFileParameter.EXECUTOR_USER, properties.getKubernetesExecutorUser() //
				, JsonFileParameter.EXECUTOR_PASSWORD, properties.getDatabasePassword() //
				, JsonFileParameter.EXECUTOR_DATABASE, properties.getKubernetesExecutorDatabase() //
				, JsonFileParameter.EXECUTOR_PORT, properties.getKubernetesExecutorPort()};
	}

	
	private static String applyKubernetesParameters(String kubernetesConfiguration, String[] kubernetesParameters)
	{
		for (int i=0;i<kubernetesParameters.length;i=i+2)
		{
			kubernetesConfiguration = kubernetesConfiguration.replace(kubernetesParameters[i], kubernetesParameters[i+1]);
		}
		
		return kubernetesConfiguration;
	}
	
}
