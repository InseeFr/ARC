FROM maven:3.6-jdk-8-alpine AS build

# the path+file name to a maven settings.xml file
ARG path_to_settings=false

# little "trick" to make the setting file optionnal. If
# the file is not define, the second argument of copy
# will fail, but as the first work it's ok
COPY .  $path_to_settings* /usr/src/app/

# Run a conditional script for the maven build
RUN chmod +x usr/src/app/maven-settings.sh && usr/src/app/maven-settings.sh
 
# Get a tomcat 8.5
FROM tomcat:8.5

# Clean it
RUN rm -rf $CATALINA_HOME/webapps/*

# Copy the war file
COPY --from=build usr/src/app/arc-web/target/*.war $CATALINA_HOME/webapps/ROOT.war

