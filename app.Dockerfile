FROM maven:3.6-jdk-8-alpine AS build
COPY . /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests -Dhttp.proxyHost=proxy-rie.http.insee.fr -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy-rie.http.insee.fr -Dhttps.proxyPort=8080 

FROM tomcat:8.0.48-jre8

RUN rm -rf $CATALINA_HOME/webapps/*

COPY --from=build usr/src/app/arc-web/target/*.war $CATALINA_HOME/webapps/ROOT.war