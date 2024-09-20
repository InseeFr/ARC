package fr.insee.arc.core.service.kubernetes.configuration;

import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.core.service.kubernetes.bo.JsonFileParameter;
import fr.insee.arc.utils.consumer.ThrowingFunction;
import fr.insee.arc.utils.exception.ArcException;
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
	private static String replicaStatefulConfiguration(int executorReplicaIndex)
	{
		return applyKubernetesParameters(ExecutorDatabaseStatefulTemplate.configuration, kubernetesParameters(executorReplicaIndex));
	}
	
	/**
	 * Create the json configuration for the service matching the given executor replica index
	 * @param executorReplicaIndex
	 * @return
	 * @throws ArcException
	 */
	private static String replicaServiceConfiguration(int executorReplicaIndex)
	{
		return applyKubernetesParameters(ExecutorDatabaseServiceTemplate.configuration, kubernetesParameters(executorReplicaIndex));
	}
	
	private static String[] kubernetesParameters(int executorReplicaIndex)
	{
		return new String[] {JsonFileParameter.EXECUTOR_LABEL, KubernetesServiceLayer.getName(properties.getKubernetesExecutorLabel(), executorReplicaIndex) //
				, JsonFileParameter.EXECUTOR_USER, properties.getKubernetesExecutorUser() //
				, JsonFileParameter.EXECUTOR_PASSWORD, properties.getDatabasePassword() //
				, JsonFileParameter.EXECUTOR_DATABASE, properties.getKubernetesExecutorDatabase() //
				, JsonFileParameter.EXECUTOR_PORT, properties.getKubernetesExecutorPort()
				, JsonFileParameter.IMAGE, properties.getKubernetesExecutorImage()
				, JsonFileParameter.LIMITS_CPU, properties.getKubernetesLimitsExecutorCpu()
				, JsonFileParameter.LIMITS_MEMORY, properties.getKubernetesLimitsExecutorMemory()
				, JsonFileParameter.LIMITS_EPHEMERAL, properties.getKubernetesLimitsExecutorEphemeral()
				, JsonFileParameter.REQUESTS_CPU, properties.getKubernetesRequestsExecutorCpu()
				, JsonFileParameter.REQUESTS_MEMORY, properties.getKubernetesRequestsExecutorMemory()
				, JsonFileParameter.REQUESTS_EPHEMERAL, properties.getKubernetesRequestsExecutorEphemeral()
				, JsonFileParameter.GENERIC_EPHEMERAL_VOLUME_SIZE, properties.getKubernetesGenericEphemeralVolumeSize()
		};
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
