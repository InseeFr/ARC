#!/bin/bash

VAR_HTTP_PROXY=$HTTP_PROXY

echo $VAR_HTTP_PROXY;

# Check if var is null
if [ -z $VAR_HTTP_PROXY ]; then
echo "vide";
else 
Dhttp_proxyHost=$(cut -d':' -f2 <<<$VAR_HTTP_PROXY);
Dhttp_proxyPort=$(cut -d':' -f3 <<<$VAR_HTTP_PROXY);
HTTP_PROXY_PORT="${Dhttp_proxyPort%/*}";
HTTP_PROXY_HOST="${Dhttp_proxyHost#//}";
fi


VAR_HTTPS_PROXY=$HTTPS_PROXY

echo $VAR_HTTPS_PROXY;

# Check if var is null
if [ -z $VAR_HTTPS_PROXY ]; then
echo "vide";
else 
Dhttps_proxyHost=$(cut -d':' -f2 <<<$VAR_HTTPS_PROXY);
Dhttps_proxyPort=$(cut -d':' -f3 <<<$VAR_HTTPS_PROXY);
HTTPS_PROXY_PORT="${Dhttps_proxyPort%/*}";
HTTPS_PROXY_HOST="${Dhttps_proxyHost#//}";
fi

if [ -z $VAR_HTTPS_PROXY -a  -z $VAR_HTTP_PROXY ]; then
mvn -f /usr/src/app/pom.xml clean package -DskipTests ;
else
mvn -f /usr/src/app/pom.xml clean package -DskipTests -Dhttp.proxyHost=$HTTP_PROXY_HOST -Dhttp.proxyPort=$HTTP_PROXY_PORT -Dhttps.proxyHost=$HTTPS_PROXY_HOST -Dhttps.proxyPort=$HTTPS_PROXY_PORT ;
fi