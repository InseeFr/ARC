#!/bin/bash

VAR_HTTP_PROXY=$HTTP_PROXY;

MAVEN_CONF=;

echo $VAR_HTTP_PROXY;

# Check if VAR_HTTP_PROXY is null
if [ -z $VAR_HTTP_PROXY ] && [ ! -z $MAVEN_SETTINGS ]; then
echo "no http proxy";
else 
echo "http proxy set";
Dhttp_proxyHost=$(cut -d':' -f2 <<<$VAR_HTTP_PROXY);
Dhttp_proxyPort=$(cut -d':' -f3 <<<$VAR_HTTP_PROXY);
MAVEN_CONF="$MAVEN_CONF -Dhttp.proxyPort=${Dhttp_proxyPort%/*}";
MAVEN_CONF="$MAVEN_CONF -Dhttp.proxyHost=${Dhttp_proxyHost#//}";
fi

VAR_HTTPS_PROXY=$HTTPS_PROXY

echo $VAR_HTTPS_PROXY;

# Check if VAR_HTTPS_PROXY is null
if [ -z $VAR_HTTPS_PROXY ] && [ ! -z $MAVEN_SETTINGS ]; then
echo "no https proxy";
else 
echo "https proxy set";
Dhttps_proxyHost=$(cut -d':' -f2 <<<$VAR_HTTPS_PROXY);
Dhttps_proxyPort=$(cut -d':' -f3 <<<$VAR_HTTPS_PROXY);
MAVEN_CONF="$MAVEN_CONF -Dhttps.proxyPort=${Dhttps_proxyPort%/*}";
MAVEN_CONF="$MAVEN_CONF -Dhttps.proxyHost=${Dhttps_proxyHost#//}";
fi

if [ ! -z $MAVEN_SETTINGS ]; then
echo "maven settings.xml set to $MAVEN_SETTINGS";
MAVEN_CONF="-s usr/src/app/$MAVEN_SETTINGS";
fi

mvn -f /usr/src/app/pom.xml clean package -DskipTests $MAVEN_CONF -Pdocker -Denv.logSettings=$LOG_SETTINGS -Denv.urlDatabase=$DATABASE_URL -Denv.usernameDatabase=$DATABASE_USER -Denv.passwordDatabase=$DATABASE_PASSWORD -Denv.restrictedUserDatabase=$DATABASE_RESTRICTED_USER -Denv.applicationDirectory=$APPLICATION_DIRECTORY -Denv.disableDebugGui=$DISABLE_DEBUG_GUI -Denv.kubernetesApiUri=$KUBERNETES_API_URI -Denv.kubernetesApiNamespace=KUBERNETES_API_NAMESPACE -Denv.kubernetesApiTokenPath=$KUBERNETES_API_TOKEN_PATH -Denv.kubernetesApiTokenValue=$KUBERNETES_API_TOKEN_VALUE -Denv.kubernetesExecutorImage=$KUBERNETES_EXECUTOR_IMAGE -Denv.kubernetesExecutorNumber=$KUBERNETES_EXECUTOR_NUMBER -Denv.kubernetesExecutorLabel=$KUBERNETES_EXECUTOR_LABEL -Denv.kubernetesExecutorUser=$KUBERNETES_EXECUTOR_USER -Denv.kubernetesExecutorDatabase=$KUBERNETES_EXECUTOR_DATABASE -Denv.kubernetesExecutorPort=$KUBERNETES_EXECUTOR_PORT -Denv.kubernetesExecutorVolatile=$KUBERNETES_EXECUTOR_VOLATILE -Denv.processExport=$PROCESS_EXPORT -Denv.s3InputApiUri=$S3_INPUT_API_URI -Denv.s3InputBucket=$S3_INPUT_BUCKET -Denv.s3InputAccess=$S3_INPUT_ACCESS -Denv.s3InputSecret=$S3_INPUT_SECRET -Denv.s3OutputApiUri=$S3_OUTPUT_API_URI -Denv.s3OutputBucket=$S3_OUTPUT_BUCKET -Denv.s3OutputAccess=$S3_OUTPUT_ACCESS -Denv.s3OutputSecret=$S3_OUTPUT_SECRET -Denv.s3OutputParquetKey=$S3_OUTPUT_PARQUET_KEY -Denv.envExecution=$ENV_EXECUTION -Dfr.insee.arc.roles.admin=$KEYCLOAK_AUTHORIZED_ROLES -Dfr.insee.keycloak.realm=$KEYCLOAK_REALM -Dfr.insee.keycloak.server=$KEYCLOAK_SERVER -Dfr.insee.keycloak.resource=$KEYCLOAK_RESOURCE -Dfr.insee.keycloak.credentials.secret=$KEYCLOAK_CREDENTIALS -Dfr.insee.arc.files.retention.days=FILES_RETENTION_DAYS;


