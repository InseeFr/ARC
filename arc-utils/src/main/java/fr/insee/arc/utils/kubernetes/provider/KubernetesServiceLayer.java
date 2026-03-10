package fr.insee.arc.utils.kubernetes.provider;

public class KubernetesServiceLayer {

	public static final String KUBERNETES_TOKEN_DELIMITER = "-";

	/**
	 * return service name for a given statefuleSet label and given replica index
	 * 
	 * @param statefuleLabel
	 * @param executorReplicaIndex
	 * @return
	 */
	public static String getName(String statefulLabel, int executorReplicaIndex) {
		return String.join(KUBERNETES_TOKEN_DELIMITER, statefulLabel, String.valueOf(executorReplicaIndex));
	}

	public static String getUri(String statefulLabel, int executorReplicaIndex, String database, String port) {
		return "jdbc:postgresql://" + getName(statefulLabel, executorReplicaIndex) + ":" + port + "/" + database;
	}

}
