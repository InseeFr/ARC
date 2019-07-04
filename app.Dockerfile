FROM maven:3.6-jdk-8-alpine AS build
COPY . /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests

FROM tomcat:8.5

RUN rm -rf $CATALINA_HOME/webapps/*

COPY --from=build usr/src/app/arc-web/target/*.war $CATALINA_HOME/webapps/ROOT.war
