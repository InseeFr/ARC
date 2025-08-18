package fr.insee.arc.core.service.kubernetes.configuration;

public class ExecutorDatabaseStatefulTemplate {

	private ExecutorDatabaseStatefulTemplate() {
		throw new IllegalStateException("Database template for kubernetes database executors");
	}
	
	protected static String configuration =
"""
{
  "apiVersion": "apps/v1",
  "kind": "StatefulSet",
  "metadata": {
    "name": "{pg-arc-executor-label}"
  },
  "spec": {
    "replicas": 1,
    "selector": {
      "matchLabels": {
        "app": "{pg-arc-executor-label}"
      }
    },
    "template": {
      "metadata": {
        "labels": {
          "app": "{pg-arc-executor-label}"
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
            "image": "{image}",
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
                "name": "POSTGRESQL_PORT_NUMBER",
                "value": "{port}"
              },
              {
                "name": "PGDATA",
                "value": "/postgresql/data"
              },
              {
                "name": "POSTGRES_USER",
                "value": "{user}"
              },
              {
                "name": "POSTGRES_PASSWORD",
                "value": "{password}"
              },
              {
                "name": "POSTGRES_DB",
                "value": "{database}"
              },
              {
                "name": "POSTGRES_INITDB_ARGS",
                "value": "--encoding=UTF-8 --lc-collate=C --lc-ctype=C"
              }
            ],
            "image": "{image}",
            "imagePullPolicy": "IfNotPresent",
            "livenessProbe": {
              "exec": {
                "command": [
                  "/bin/sh",
                  "-c",
                  "exec pg_isready -U \\"{user}\\" -d \\"dbname={database}\\" -h 127.0.0.1 -p {port}"
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
                "containerPort": {port},
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
                  "exec pg_isready -U \\"{user}\\" -d \\"dbname={database}\\" -h 127.0.0.1 -p {port}"
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
                "memory": "{ram}",
                "ephemeral-storage": "40Mi"
              },
              "limits": {
                "cpu": "{cpu}",
                "memory": "{ram}",
                "ephemeral-storage": "{ephemeral}"
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
            "emptyDir": {
              "medium": "{temporary_tablespace_medium}"
            },
            "name": "tbstmp"
          },
          {
            "name": "tmp-volume",
            "ephemeral": {
              "volumeClaimTemplate": {
                "spec": {
                  "accessModes": ["ReadWriteOnce"],
                  "resources": {
                    "requests": {
                      "storage": "{generic_ephemeral_volume_size}"
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
""";
}
