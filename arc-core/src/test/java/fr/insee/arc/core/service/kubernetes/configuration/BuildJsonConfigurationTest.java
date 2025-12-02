package fr.insee.arc.core.service.kubernetes.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildJsonConfigurationTest {

	@Test
	public void kubernetesParametersTest() {
		
		PropertiesHandler properties=PropertiesHandler.getInstance();
		
		properties.setKubernetesExecutorLabel("pg-arc-executor-batch-w");
		properties.setKubernetesExecutorUser("arc");
		properties.setDatabasePassword("");
		properties.setKubernetesExecutorDatabase("arc_db");
		properties.setKubernetesExecutorPort("5432");
		properties.setKubernetesExecutorImage("postgres:17.5.0");
		properties.setKubernetesExecutorCpu("8");
		properties.setKubernetesExecutorRam("8Gi");
		properties.setKubernetesExecutorEphemeral("50Gi");
		properties.setKubernetesExecutorEphemeralVolumeSize("400Gi");
		properties.setKubernetesExecutorTemptablespaceMedium("100Gi");
		
		String expectedTemplate = 
"""
{
  "apiVersion": "apps/v1",
  "kind": "StatefulSet",
  "metadata": {
    "name": "pg-arc-executor-batch-w-1"
  },
  "spec": {
    "replicas": 1,
    "selector": {
      "matchLabels": {
        "app": "pg-arc-executor-batch-w-1"
      }
    },
    "template": {
      "metadata": {
        "labels": {
          "app": "pg-arc-executor-batch-w-1"
        }
      },
      "spec": {
        "securityContext": {
          "fsGroupChangePolicy": "Always",
          "fsGroup": 1001,
          "supplementalGroups": [
            1001
          ],
          "runAsUser": 1001
		},
        "initContainers": [
          {
            "name": "tbstmp-init",
            "image": "postgres:17.5.0",
            "imagePullPolicy": "IfNotPresent",
            "command": [
              "/bin/sh",
              "-c"
            ],
            "args": [
              "echo create directory for temporary tablespace && mkdir /tbstmp/tmp"
            ],
            "volumeMounts": [
              {
                "name": "tbstmp",
                "mountPath": "/tbstmp"
              }
            ],
            "resources": {
              "requests": {
                "cpu": "10m",
                "memory": "16Mi",
                "ephemeral-storage": "1Mi"
              },
              "limits": {
                "cpu": "100m",
                "memory": "32Mi",
                "ephemeral-storage": "500Mi"
              }
            },
            "securityContext": {
              "runAsNonRoot": true,
              "allowPrivilegeEscalation": false,
              "capabilities": {
                "drop": [
                  "ALL"
                ]
              },
              "seccompProfile": {
                "type": "RuntimeDefault"
              }
            }
          }
        ],
        "containers": [
          {
            "env": [
              {
                "name": "PGPORT",
                "value": "5432"
              },
              {
                "name": "PGDATA",
                "value": "/postgresql/data"
              },
              {
                "name": "POSTGRES_USER",
                "value": "arc"
              },
              {
                "name": "PGUSER",
                "value": "arc"
              },
              {
                "name": "POSTGRES_PASSWORD",
                "value": ""
              },
              {
                "name": "PGPASSWORD",
                "value": ""
              },
              {
                "name": "POSTGRES_DB",
                "value": "arc_db"
              },
              {
                "name": "POSTGRES_INITDB_ARGS",
                "value": "--encoding=UTF-8 --lc-collate=C --lc-ctype=C"
              }
            ],
            "image": "postgres:17.5.0",
            "imagePullPolicy": "IfNotPresent",
            "livenessProbe": {
              "exec": {
                "command": [
                  "/bin/sh",
                  "-c",
                  "exec pg_isready -U \\"arc\\" -d \\"dbname=arc_db\\" -h 127.0.0.1 -p 5432"
                ]
              },
              "failureThreshold": 6,
              "initialDelaySeconds": 30,
              "periodSeconds": 10,
              "successThreshold": 1,
              "timeoutSeconds": 5
            },
            "name": "postgresql",
            "ports": [
              {
                "containerPort": 5432,
                "name": "tcp-postgresql",
                "protocol": "TCP"
              }
            ],
            "readinessProbe": {
              "exec": {
                "command": [
                  "/bin/sh",
                  "-c",
                  "-e",
                  "exec pg_isready -U \\"arc\\" -d \\"dbname=arc_db\\" -h 127.0.0.1 -p 5432"
                ]
              },
              "failureThreshold": 6,
              "initialDelaySeconds": 5,
              "periodSeconds": 10,
              "successThreshold": 1,
              "timeoutSeconds": 5
            },
            "resources": {
              "requests": {
                "cpu": "10m",
                "memory": "8Gi",
                "ephemeral-storage": "40Mi"
              },
              "limits": {
                "cpu": "8",
                "memory": "8Gi",
                "ephemeral-storage": "50Gi"
              }
            },
            "securityContext": {
              "runAsNonRoot": true,
              "allowPrivilegeEscalation": false,
              "capabilities": {
                "drop": [
                  "ALL"
                ]
              },
              "seccompProfile": {
                "type": "RuntimeDefault"
              }
            },
            "terminationMessagePath": "/dev/termination-log",
            "terminationMessagePolicy": "File",
            "volumeMounts": [
              {
                "mountPath": "/dev/shm",
                "name": "dshm"
              },
              {
                "mountPath": "/docker-entrypoint-initdb.d/",
                "name": "custom-init-scripts"
              },
              {
                "mountPath": "/postgresql",
                "name": "tmp-volume"
              },
              {
                "mountPath": "/tbstmp",
                "name": "tbstmp"
              }
            ]
          }
        ],
        "volumes": [
          {
            "configMap": {
              "name": "custom-init-scripts-pg-arc"
            },
            "name": "custom-init-scripts"
          },
          {
            "emptyDir": {
              "medium": "Memory"
            },
            "name": "dshm"
          },
          {
            "name": "tbstmp",
            "ephemeral": {
              "volumeClaimTemplate": {
                "spec": {
                  "accessModes": ["ReadWriteOnce"],
                  "resources": {
                    "requests": {
                      "storage": "100Gi"
                    }
                  }
                }
              }
            }
          },
          {
            "name": "tmp-volume",
            "ephemeral": {
              "volumeClaimTemplate": {
                "spec": {
                  "accessModes": ["ReadWriteOnce"],
                  "resources": {
                    "requests": {
                      "storage": "400Gi"
                    }
                  }
                }
              }
            }
          }
        ]
      }
    }
  }
}
"""
;	
		
		assertEquals(expectedTemplate, BuildJsonConfiguration.replicaStatefulConfiguration(1));
		
expectedTemplate =
"""
{
  "apiVersion": "v1",
  "kind": "Service",
  "metadata": {
    "name": "pg-arc-executor-batch-w-1",
    "labels": {
      "app": "pg-arc-executor-batch-w-1"
    }
  },
  "spec": {
    "type": "ClusterIP",
    "ports": [
      {
        "port": 5432
      }
    ],
    "selector": {
      "app": "pg-arc-executor-batch-w-1"
    }
  }
}		
"""
;
		assertEquals(expectedTemplate, BuildJsonConfiguration.replicaServiceConfiguration(1));
	
		
	}

}
