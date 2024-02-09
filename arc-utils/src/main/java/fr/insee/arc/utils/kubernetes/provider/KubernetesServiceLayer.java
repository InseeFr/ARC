package fr.insee.arc.utils.kubernetes.provider;

public class KubernetesServiceLayer {

	public static final String KUBERNETES_TOKEN_DELIMITER="-";
	public static final String USER_NAME="postgres";
	

	/**
	 * return service name for a given statefuleSet label and given replica index
	 * @param statefuleLabel
	 * @param executorReplicaIndex
	 * @return
	 */
	public static String getName(String statefulLabel, int executorReplicaIndex)
	{
		return String.join(KUBERNETES_TOKEN_DELIMITER, statefulLabel, String.valueOf(executorReplicaIndex));
	}
	
	
	public static String getUri(String statefulLabel, int executorReplicaIndex)
	{
		return "jdbc:postgresql://"+getName(statefulLabel, executorReplicaIndex)+":5432/defaultdb";
	}
	
}
