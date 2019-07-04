FROM maven:3.6-jdk-8-alpine AS build

ARG HTTP_PROXY
ARG HTTPS_PROXY

COPY . /usr/src/app/

# Run a conditional script for the maven build
RUN chmod +x usr/src/app/script.sh && usr/src/app/script.sh
 
# Get a tomcat 8.5
FROM tomcat:8.5

# Clean it
RUN rm -rf $CATALINA_HOME/webapps/*

# Copy the war file
COPY --from=build usr/src/app/arc-web/target/*.war $CATALINA_HOME/webapps/ROOT.war