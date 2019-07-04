if [ "${path_to_settings}" = false ]; then
mvn -f /usr/src/app/pom.xml clean package -DskipTests;
else
mvn -s /usr/src/app/$path_to_settings -f /usr/src/app/pom.xml clean package -DskipTests;
fi