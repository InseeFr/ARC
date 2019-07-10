FROM maven:3.6-jdk-8-alpine AS build

# Proxies
ARG HTTP_PROXY
ARG HTTPS_PROXY

# path of settings.xml 
ARG MAVEN_SETTINGS

# DB properties
ARG DATABASE_URL
ARG DATABASE_USER
ARG DATABASE_PASSWORD

# Log properties
ARG LOG_PATH=logs/log-arc.log
ARG LOG_LEVEL=ERROR
ARG LOG_SETTINGS=fr/insee/config/log4j.xml

COPY . /usr/src/app/

# Run a conditional script for the maven build
RUN chmod +x usr/src/app/script.sh && usr/src/app/script.sh
 
# Get a tomcat 8.5
FROM tomcat:8.5

# Clean it
RUN rm -rf $CATALINA_HOME/webapps/*

# Copy the war file
COPY --from=build usr/src/app/arc-web/target/*.war $CATALINA_HOME/webapps/ROOT.war
