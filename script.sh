#!/bin/bash

VAR_HTTP_PROXY=$HTTP_PROXY;

MAVEN_CONF=;

echo $VAR_HTTP_PROXY;

# Check if var is null
if [ -z $VAR_HTTP_PROXY ] && [ ! -z $MAVEN_SETTINGS ]; then
echo "no http proxy";
else 
echo "http proxy set";
Dhttp_proxyHost=$(cut -d':' -f2 <<<$VAR_HTTP_PROXY);
Dhttp_proxyPort=$(cut -d':' -f3 <<<$VAR_HTTP_PROXY);
MAVEN_CONF="$MAVEN_CONF -Dhttp.proxyPort=${Dhttp_proxyHost%/*}";
MAVEN_CONF="$MAVEN_CONF -Dhttp.proxyHost=${Dhttp_proxyPort#//}";
fi


VAR_HTTPS_PROXY=$HTTPS_PROXY

echo $VAR_HTTPS_PROXY;

# Check if var is null
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

echo "mvn -f /usr/src/app/pom.xml clean package -DskipTests $MAVEN_CONF;"

mvn -f /usr/src/app/pom.xml clean package -DskipTests $MAVEN_CONF;
