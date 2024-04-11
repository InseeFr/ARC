package fr.insee.arc.core.service.kubernetes.configuration;

public class ExecutorDatabaseStatefulTemplate {
	
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
        "containers": [
          {
            "env": [
              {
                "name": "BITNAMI_DEBUG",
                "value": "false"
              },
              {
                "name": "POSTGRESQL_PORT_NUMBER",
                "value": "{port}"
              },
              {
                "name": "POSTGRESQL_VOLUME_DIR",
                "value": "/bitnami/postgresql"
              },
              {
                "name": "PGDATA",
                "value": "/bitnami/postgresql/data"
              },
              {
                "name": "POSTGRES_USER",
                "value": "{user}"
              },
              {
                "name": "POSTGRES_POSTGRES_PASSWORD",
                "value": "{password}"
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
                "name": "POSTGRESQL_ENABLE_LDAP",
                "value": "no"
              },
              {
                "name": "POSTGRESQL_ENABLE_TLS",
                "value": "no"
              },
              {
                "name": "POSTGRESQL_LOG_HOSTNAME",
                "value": "false"
              },
              {
                "name": "POSTGRESQL_LOG_CONNECTIONS",
                "value": "false"
              },
              {
                "name": "POSTGRESQL_LOG_DISCONNECTIONS",
                "value": "false"
              },
              {
                "name": "POSTGRESQL_PGAUDIT_LOG_CATALOG",
                "value": "off"
              },
              {
                "name": "POSTGRESQL_CLIENT_MIN_MESSAGES",
                "value": "error"
              },
              {
                "name": "POSTGRESQL_SHARED_PRELOAD_LIBRARIES",
                "value": "pgaudit"
              },
              {
                "name": "POSTGRES_INITDB_ARGS",
                "value": "--encoding=UTF-8 --lc-collate=C --lc-ctype=C"
              }
            ],
            "image": "docker.io/bitnami/postgresql:11",
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
                  "exec pg_isready -U \\"{user}\\" -d \\"dbname={database}\\" -h 127.0.0.1 -p {port}\\n[ -f /opt/bitnami/postgresql/tmp/.initialized ] || [ -f /bitnami/postgresql/.initialized ]\\n"
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
                "memory": "16Mi"
              },
              "limits": {
                "cpu": 1,
                "memory": "1Gi"
              }
            },
            "securityContext": {
              "runAsUser": 0
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
          }
        ]
      }
    }
  }
}

""";
}
