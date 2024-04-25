package fr.insee.arc.core.service.kubernetes.configuration;

public class ExecutorDatabaseServiceTemplate {

	private ExecutorDatabaseServiceTemplate() {
		throw new IllegalStateException("Service Template for kubernetes database executors");
	}

	
	protected static String configuration =
"""
{
  "apiVersion": "v1",
  "kind": "Service",
  "metadata": {
    "name": "{pg-arc-executor-label}",
    "labels": {
      "app": "{pg-arc-executor-label}"
    }
  },
  "spec": {
    "type": "ClusterIP",
    "ports": [
      {
        "port": {port}
      }
    ],
    "selector": {
      "app": "{pg-arc-executor-label}"
    }
  }
}
""";
	
}
