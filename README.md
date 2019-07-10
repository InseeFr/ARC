# ARC - Acquire - Register - Control : Workbench for acquisition and normalization of data sets

General présentation of the application
The ARC (from the French: Acquisition - Réception - Contrôles) software allows receiving (administrative) data supplied by the providers (several formats are supported, particularly XML), to control the compliance of the received files, and to transform administrative data to elementary statistical data. The software enables the statistician to define and apply controls and mappings, to test them in a sandbox environment (linked to the software), and to put them into production without frequently calling on a developer.

These functionnalities/services aim the statistician’s independence and ability to adapt to the data evolutions, thereby avoiding impacts on the statistical chain.

![workflow](user-guide/img/workflow.png)

## Running the application

The ARC application is a java 8 application, working with a PostgreSQL > 9.6 database. To run the app you will need this PostgreSQL DB and configure the connection in the [arc.properties](arc-web/src/main/resources/fr/insee/config/arc.properties) file. Once this is done

### Running the web application with Docker

The easiest way to run the ARC application is with docker. Because you could want to build the app with some custom maven settings or behind a proxy, there is 3 way to build the web app.

- The easy peasy way. If you don't need proxy or custom settings just run
  
  ```shell
  docker build -f app.Dockerfile -t arc .
  ```

- Behind a proxy. To run the maven phase behind a proxy you have to specify it. We created two args to pass the proxy settings to maven. If your proxy environnement variables are set, run 

  ```shell
  docker build -f app.Dockerfile \
    --build-arg HTTP_PROXY=${HTTP_PROXY}  \
    --build-arg HTTPS_PROXY=${HTTPS_PROXY} \
    -t arc \
    .
  ```

  otherwise replace ${HTTP_PROXY} and ${HTTPS_PROXY} by your proxies.

- with maven settings. Finaly if you what to fully configure maven, add a maven settings.xml file in the ARC directory and run :

  ```shell
    docker build -f app.Dockerfile \
    --build-arg MAVEN_SETTINGS=path/to/maven/settings.xml  \
    -t arc \
    .
  ```

After the image build,

  ```shell
    docker run -p 8080:8080 arc
  ```

### Running the app with tomcat

The ARC web-user application component uses an apache/tomcat server with version 8.5 or higher.

#### Set the tomcat and the database connection

Add to the tomcat service or tomcat runner the parameter -Dproperties.path= to set up the directory location of properties files

For example in catalina.bat, the JAVA_OPTS parameters may be changed as followed

```bash
set "JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dproperties.file=D:\apache-tomcat-8.5.38\webapps\"
```

Change the file resources-prod.properties to configure the database connections, the root directory of the filesystem used by ARC and eventually the path to log4j configuration files See "Java configuration parameters" for more informations

#### Deploy or update the application

1. Stop tomcat server
2. Delete the content of the temporary tomcat directories namely "temp" and "work" directories
3. Copy arc-web.war into the "webapps" tomcat directory
4. Copy the resources-prod.properties to the properties directory
5. Start tomcat server

### Test the deployment

In a web browser go to localhost:8080/status.action. The status action returns :

- 0 - No error detected
- 201 - Database error connection

> Change the host ip adress and port number according to the tomcat server and tomcat ARC application context configuration.

> For more information about the installation go check the [Install guide](user-guide/Install_guide.md)
