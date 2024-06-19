# ARC parameters

## Index

[1- Database configuration](#1--Database-configuration)

[2- Loggers configuration](#2--Loggers-configuration)

[3- Process configuration](#3--Process-configuration)

[4- Kubernetes configuration](#4--Kubernetes-configuration)

[5- S3 configuration](#5--S3-configuration)


## 1- Database configuration

|                |                                                                                                                                           |
| -------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.database.poolName**                                                                                                            |
| **env**        | **-**                                                                                                                                     |
| java           | databasePoolName                                                                                                                          |
| describe       | Set the name of the connections identifier. This identifier is used for the database properties names such as fr.insee.**identifier**.url |
| default value  | arc                                                                                                                                       |
| example value  | my_custom_connections_identifier                                                                                                          |

___

|                |                                                                                                                          |
| -------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **properties** | **fr.insee.database.arc.url**                                                                                            |
| **env**        | **DATABASE_URL**                                                                                                         |
| java           | databaseUrl                                                                                                              |
| describe       | Urls of the databases used by arc. Accept ruby map format with key as database index. 0 is coordinator, 1+ are executors |
| default value  | -                                                                                                                        |
| example value  | jdbc:postgresql://localhost:5432/arc_single_database_mode                                                                |
| example value  | {0=>"jdbc:postgresql://localhost:5432/arc_coordinator},{1=>"jdbc:postgresql://localhost:5432/arc_executor_1"}            |
|                |                                                                                                                          |


___

|               |                                                                                                                           |
| ------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **properties**    | **fr.insee.database.arc.username**                                                                                        |
| **env**           | **DATABASE_USER**                                                                                                         |
| java          | databaseUsername                                                                                                          |
| describe      | Users of the databases used by arc. Accept ruby map format with key as database index. 0 is coordinator, 1+ are executors |
| default value | -                                                                                                                         |
| example value | database_user                                                                                                             |
| example value | {0=>"user_coordinator"},{1=>"user_executor_1"},{2=>"user_executor_2"}                                                     |


___

|                |                                                                                                                                   |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.database.arc.restrictedUsername**                                                                                      |
| **env**        | **DATABASE_RESTRICTED_USER**                                                                                                      |
| java           | databaseRestrictedUsername                                                                                                        |
| describe       | Restricted users of databases used by arc. Accept ruby map format with key as database index. 0 is coordinator, 1+ are executors  |
| describe       | Restricted users have read access only except on temporary table schema. They are used by engine to prevent harmful sql injection |
| default value  | -                                                                                                                                 |
| example value  | database_restricted_user                                                                                                          |
| example value  | {0=>"restricted_user_coordinator"},{1=>"restricted_user_executor_1"},{2=>"restricted_user_executor_2"}                            |

___

|                |                                                                                                                           |
| -------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.database.arc.password**                                                                                        |
| **env**        | **DATABASE_PASSWORD**                                                                                                     |
| java           | databasePassword                                                                                                          |
| describe       | Passwords of databases used by arc. Accept ruby map format with key as database index. 0 is coordinator, 1+ are executors |
| default value  | -                                                                                                                         |
| example value  | database_password                                                                                                         |
| example value  | {0=>"database_password_coordinator"},{1=>"database_password_executor_1"},{2=>"database_password_executor_2"}              |

___

|               |                                                                                                                                  |
| ------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| **properties**    | **fr.insee.database.arc.driverClassName**                                                                                        |
| **env**       | **-**                                                                                                                            |
| java          | databaseDriverClassName                                                                                                          |
| describe      | Java driver to connect database. Either declared once for all databases but may be also declared for each database with ruby map |
| default value | -                                                                                                                                |
| example value | org.postgresql.Driver                                                                                                            |
|               |                                                                                                                                  |

## 2- Loggers configuration

|                |                                                                                                 |
| -------------- | ----------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.arc.log.directory**                                                                  |
| **env**        | **-**                                                                                           |
| java           | logDirectory                                                                                    |
| describe       | Directory wheres application log files are put into. If blank, logs are put in console appender |
| default value  | -                                                                                               |
| example value  | C:/logs                                                                                         |

___

|                |                                                                                                 |
| -------------- | ----------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.arc.log.directory**                                                                  |
| **env**        | **-**                                                                                           |
| java           | logLevel                                                                                        |
| describe       | The expected log level for application. Custom loggers are always logged whatever this parameter is. |
| default value  | ERROR                                                                                           |
| example value  | INFO                                                                                            |

___

|                |                                                                 |
| -------------- | --------------------------------------------------------------- |
| **properties** | **fr.insee.arc.log.XMLconfiguration**                           |
| **env**        | **-**                                                           |
| java           | logConfiguration                                                |
| describe       | A path to a custom log4j2.xml configuration path |
| default value  | fr/insee/config/log4j2.xml                                      |
| example value  | C:/config/log4j2.xml                                            |
___

## 3- Process configuration

|                |                                                                                               |
| -------------- | --------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.arc.batch.parametre.repertoire**                                                   |
| **env**        | **APPLICATION_DIRECTORY**                                                                     |
| java           | batchParametersDirectory                                                                      |
| describe       | The root directory where the sandbox folders are. "/" at the end of the path is mandatory     |
| default value  |                                                                                               |
| example value  | C:/arc_root/                                                                                  |

|                |                                                                                               |
| -------------- | --------------------------------------------------------------------------------------------- |
| **properties** | **files.retention.days**                                                                      |
| **env**        | **FILES_RETENTION_DAYS**                                                                      |
| java           | logConfiguration                                                                              |
| describe       | The number of days after which to delete archived files. -1 means archived files musn't be deleted |
| default value  | -1                                                                                            |
| example value  | 45                                                                                            |

___

|                |                                                                                                                                     |
| -------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| **properties** | **fr.insee.arc.batch.parametre.envExecution**                                                                                       |
| **env**        | **ENV_EXECUTION**                                                                                                                   |
| java           | batchExecutionEnvironment                                                                                                           |
| describe       | Target sandbox for batch. Overriden by LanceurARC.envExecution GUI parameter when the GUI parameter LanceurARC.envFromDatabase=true |
| default value  | arc_prod                                                                                                                            |
| example value  | arc_bas2                                                                                                                            |
                                                                                                                                    |
___

|                |                                                                           |
| -------------- | ------------------------------------------------------------------------- |
| **properties** | **fr.insee.arc.process.export**                                           |
| **env**        | **PROCESS_EXPORT**                                                        |
| java           | processExport                                                             |
| describe       | Data must be exported at the end of the batch pipeline if not empty value |
| default value  |                                                                           |
| example value  | Y                                                                         |


## 4- Kubernetes configuration

|                            |                              |                                           |
| -------------------------- | ---------------------------- | ----------------------------------------- |
| kubernetesApiUri           | KUBERNETES_API_URI           | fr.insee.arc.kubernetes.api.uri           |
| kubernetesApiNamespace     | KUBERNETES_API_NAMESPACE     | fr.insee.arc.kubernetes.api.namespace     |
| kubernetesApiTokenPath     | KUBERNETES_API_TOKEN_PATH    | fr.insee.arc.kubernetes.api.token.path    |
| kubernetesApiTokenValue    | KUBERNETES_API_TOKEN_VALUE   | fr.insee.arc.kubernetes.api.token.value   |
| kubernetesExecutorNumber   | KUBERNETES_EXECUTOR_NUMBER   | fr.insee.arc.kubernetes.executor.number   |
| kubernetesExecutorLabel    | KUBERNETES_EXECUTOR_LABEL    | fr.insee.arc.kubernetes.executor.label    |
| kubernetesExecutorUser     | KUBERNETES_EXECUTOR_USER     | fr.insee.arc.kubernetes.executor.user     |
| kubernetesExecutorDatabase | KUBERNETES_EXECUTOR_DATABASE | fr.insee.arc.kubernetes.executor.database |
| kubernetesExecutorPort     | KUBERNETES_EXECUTOR_PORT     | fr.insee.arc.kubernetes.executor.port     |
| kubernetesExecutorVolatile | KUBERNETES_EXECUTOR_VOLATILE | fr.insee.arc.kubernetes.executor.volatile |


## 5- S3 configuration

|                |                   |                                |
| -------------- | ----------------- | ------------------------------ |
| s3InputApiUri  | S3_INPUT_API_URI  | fr.insee.arc.s3.input.api.uri  |
| s3InputBucket  | S3_INPUT_BUCKET   | fr.insee.arc.s3.input.bucket   |
| s3InputAccess  | S3_INPUT_ACCESS   | fr.insee.arc.s3.input.access   |
| s3InputSecret  | S3_INPUT_SECRET   | fr.insee.arc.s3.input.secret   |
| s3OutputApiUri | S3_OUTPUT_API_URI | fr.insee.arc.s3.output.api.uri |
| s3OutputBucket | S3_OUTPUT_BUCKET  | fr.insee.arc.s3.output.bucket  |
| s3OutputAccess | S3_OUTPUT_ACCESS  | fr.insee.arc.s3.output.access  |
| s3OutputSecret | S3_OUTPUT_SECRET  | fr.insee.arc.s3.output.secret  |
| s3OutputParquetKey | S3_OUTPUT_PARQUET_KEY  | fr.insee.arc.s3.output.parquet.key  |
| | KEYCLOAK_AUTHORIZED_ROLES | fr.insee.arc.roles.admin |
| | KEYCLOAK_REALM  | fr.insee.keycloak.realm  |
| | KEYCLOAK_SERVER  | fr.insee.keycloak.server  |
| | KEYCLOAK_RESOURCE  | fr.insee.keycloak.resource  |
| | KEYCLOAK_CREDENTIALS  | fr.insee.keycloak.credentials.secret  |

