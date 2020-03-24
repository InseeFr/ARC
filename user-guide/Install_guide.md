# ARC Installation guide

- [Build ARC java executables from release tags](#Build-ARC-java-executables-from-realease-tags)
- [ARC database building](#ARC-database-building)
- [Choose your application components](#Choose-your-application-components)
  - [Install the ARC web-user interface component](#Install-the-ARC-web-user-interface-component)
    - [Set the tomcat and the database connection](#Set-the-tomcat-and-the-database-connection)
    - [Deploy or update the application](#Deploy-or-update-the-application)
    - [Test the deployment](#Test-the-deployment)
  - [Install the ARC data retrieval web-service component](#Install-the-ARC-data-retrieval-web-service-component)
    - [Set the tomcat and the database connection for the webservice](#Set-the-tomcat-and-the-database-connection-for-the-webservice)
    - [Deploy or update the webservice](#Deploy-or-update-the-webservice)
    - [Test the deployment of the webservice](#Test-the-deployment-of-the-webservice)
 - [Batch launcher component](#Batch-launcher-component)
    - [Installation steps](#Installation-steps)
- [Configuration parameters](#Configuration-parameters)
  - [Java configuration parameters](#Java-configuration-parameters)
    - [External java properties](#External-java-properties)
    - [log4j logger configuration](#log4j-logger-configuration)
  - [Database configuration parameters](#Database-configuration-parameters)
    - [Enchanced configuration for massive load in production environment](#Enchanced-configuration-for-massive-load-in-production-environment)
- [How to use ARC data retrieval web-service](#How-to-use-ARC-data-retrieval-web-service)
  - [POST url](#POST-url)
  - [Structure of queries](#Structure-of-queries)
    - [WS0 : Calculate data tables and provide a client token](#WS0--Calculate-data-tables-and-provide-a-client-token)
    - [WS0 request structure](#WS0-request-structure)
      - [Example of data retrieval token initialisation, from sandbox bas2, for testing purpose that's say with no mark for deletion, from the data model called "LIASSE", concerning the files for the client called "ESANE", monthly marked and with date reference under 2019-01-01](#Example-of-data-retrieval-token-initialisation-from-sandbox-bas2-for-testing-purpose-thats-say-with-no-mark-for-deletion-from-the-data-model-called-LIASSE-concerning-the-files-for-the-client-called-ESANE-monthly-marked-and-with-date-reference-under-2019-01-01)
    - [WS0 response structure](#WS0-response-structure)
  - [WS1 : Get one of the generated table name and structure](#WS1--Get-one-of-the-generated-table-name-and-structure)
    - [WS1 request structure](#WS1-request-structure)
    - [WS1 response structure](#WS1-response-structure)
  - [WS2 : Stream a table data content](#WS2--Stream-a-table-data-content)
    - [WS2 request structure](#WS2-request-structure)
    - [WS2 response structure](#WS2-response-structure)
  - [How to retrieve data](#How-to-retrieve-data)

## Build ARC java executables from realease tags
ARC web application may be deployed by docker as described in the readme file.

[Maven](https://maven.apache.org/index.html) software may also be used to generate easily the 2 war files of the web application and the data retrieval web-service application and the runnable jar file of the batch application.
1. Get the selected realease source code
2. Execute "maven clean install" command in the ARC directory
3. The web application tomcat file arc-web.war will be found in the ARC/arc-web/target/ directory
4. The data-retrieval web-service application tomcat file arc-ws.war will be found in the ARC/arc-ws/target/ directory 
5. The runnable jar file arc-batch.jar used to start the ARC batch application will be found in the ARC/arc-batch/target/ directory

## ARC database building

1. Build a postgres database with version 9.6 or higher
2. Arc application will build the database at the first run of the initialize module.
    * Initialize module deploys the global database structure and the database structure for the running sandbox.
    * This module may be executed from the web application inside the sandbox control screen or from the first run of the batch application.
    * The running sandbox for the batch is defined by the java configuration parameter "fr.insee.arc.batch.parametre.envExecution". [See "Java configuration parameters"](#Java-configuration-parameters) for more informations

## Choose your application components

ARC application is composed by three application components :

* the web-user interface
* the data retrieval web-service
* the batch launcher
It isn't required to install all the application components to use ARC. Typically, the web-interface may be enough for users in testing environnements whereas the batch launcher component will be likely be used by the scheduler of production servers.

### Install the ARC web-user interface component

The web-user application allows ARC users to use the software through a web browser. It provides an interface to define target data models, write user defined pocessing rules and test the processing in a sandbox environnement.

The ARC web-user application component uses an apache/tomcat server with version 8.5 or higher.

#### Set the tomcat and the database connection

1. Download the archive an extract the archive arc-web.zip
2. Add to the tomcat service or tomcat runner the parameter -Dproperties.path= to set up the directory location of properties files
    * For example in catalina.bat, the JAVA_OPTS parameters may be changed as followed

    ```cmd
    set "JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dproperties.file=D:\apache-tomcat-8.5.38\webapps\"
    ```

3. Change the file resources-prod.properties to configure the database connections, the root directory of the filesystem used by ARC and eventually the path to log4j configuration files
    [See "Java configuration parameters"](#Java-configuration-parameters) for more informations
  
#### Deploy or update the application

1. Stop tomcat server
2. Delete the content of the temporary tomcat directories namely "temp" and "work" directories
3. Copy arc-web.war into the "webapps" tomcat directory
4. Copy the resources-prod.properties to the properties directory
5. Start tomcat server

#### Test the deployment

http://locahost:8080/arc-web/status.action
The status action returns :

* 0 - No error detected
* 201 - Database error connection

> *Change the host ip adress and port number according to the tomcat server and tomcat ARC application context configuration.*

### Install the ARC data retrieval web-service component

The ARC data retrieval web-service application is use by the client softwares of ARC to retrieve their data from the ARC database. This web-service application uses an apache/tomcat server with version 8.5 or higher.

#### Set the tomcat and the database connection for the webservice

1. Download the archive an extract the archive arc-ws.zip
2. Add to the tomcat service or tomcat runner the parameter -Dproperties.path= to set up the directory location of properties files
    * For example in catalina.bat, the JAVA_OPTS parameters may be changed as followed

    ```cmd
    set "JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dproperties.file=D:\apache-tomcat-8.5.38\webapps\"
    ```

3. Change the file resources-prod.properties to configure the database connections, the root directory of the filesystem used by ARC and eventually the path to log4j configuration files
    [See "Java configuration parameters"](#Java-configuration-parameters) for more informations
  
#### Deploy or update the webservice

1. Stop tomcat server
2. Delete the content of the temporary tomcat directories namely "temp" and "work" directories
3. Copy arc-ws.war into the "webapps" tomcat directory
4. Copy the resources-prod.properties to the properties directory
5. Start tomcat server

#### Test the deployment of the webservice

http://locahost:8080/arc-ws/status.action
> *Change the host ip adress and port number according to the tomcat server and tomcat ARC application context configuration.*

The status action returns :

* 0 - No error detected
* 201 - Database error connection

## Batch launcher component

The batch launcher component batch the files to be processed by ARC and parallelize the module executions between files. Once a batch is processed, it exits. The number or volume of files batched may be configured.

### Installation steps

1. Download the last zip batch installation file arc-batch.zip
2. Extract the zip file arc_batch.zip into your target installation directory

    * The directory structure of the arc_batch.zip archive

      <img alt="" src='https://g.gravizo.com/svg?digraph%20simple_example%20%7B%0A%20%20%20%20scriptsh%5Blabel%3D%22scriptsh%5Cn%5Cnapplication%20launcher%22%2Cshape%3D%22box%22%5D%0A%20%20%20%20properties%5Blabel%3D%22properties%5Cn%5Cnconfiguration%20files%22%2Cshape%3D%22box%22%5D%0A%20%20%20%20lib%5Blabel%3D%22lib%5Cn%5Cnjava%20binaries%22%2Cshape%3D%22box%22%5D%0A%20%20%20%20files%5Blabel%3D%22files%5Cn%5Cninput%20files%20storage%22%2Cshape%3D%22box%22%5D%0A%20%20%20%20log%5Blabel%3D%22log%5Cn%5Cnlogger%20directory%22%2Cshape%3D%22box%22%5D%0A%20%20%20arc_batch%20-%3E%20scriptsh%0A%20%20%20arc_batch%20-%3E%20properties%0A%20%20%20arc_batch%20-%3E%20lib%0A%20%20%20arc_batch%20-%3E%20files%0A%20%20%20arc_batch%20-%3E%20log%0A%7D'/>

3. Set the laucher file found in the scripth archive directory
    * The laucher file is either run.bat for windows cmd or run.sh for cross-plateform bash
    * Edit the launcher file to fix the command line that will run the ARC jar file
        * Set the path to the java 8 version installed on the system
        * Keep the -jar option as the ARC batch application is an runnable jar file
        * Set the -Dproperties= parameter to the path locating the directory that contain the configuration properties files. By default, set the path to the "properties" directory extracted from the installation archive.
        * Set the path to the application runnable jar file. By default, this file called ArcMain.jar and is located in the "lib" directory

  Example on windows (file run.bat)

  ``` cmd
  "C:\Program Files (x86)\insee\atelier-dev-2\applications\jdk18_64\jdk-1.8.0_40\bin\java" -jar -Dproperties.path=".\..\properties" .\..\lib\ArcMain.jar
  pause
  ```

4. set configuration parameters in the ressources-prod.properties file

## Configuration parameters

### Java configuration parameters

#### External java properties

The java configuration parameters are declared in a .properties extension file. By default, the ARC deployment archives contains a .properties file which can be modified during the installation process.

``` bash
# database connexion
fr.insee.database.poolName=arc
# database jdbc connexion uri
# example :jdbc:postgresql://dvarcldb01.ad.insee.intra:1983/di_pg_arc_dv01
fr.insee.database.arc.url=
# database username
fr.insee.database.arc.username=
# database password
fr.insee.database.arc.password=
# database jdbc connexion driver
fr.insee.database.arc.driverClassName=org.postgresql.Driver

# path to input file root directory
fr.insee.arc.batch.parametre.repertoire=/opt/insee/arc/recette/files/

# path to logger configuration file log4j.xml
fr.insee.dsn.log.configuration=/opt/insee/arc/recette/properties/log4j.xml

# sandbox used by the batch process
# this parameter is not used by the web application
fr.insee.arc.batch.parametre.envExecution=arc.prod
```

#### log4j logger configuration

ARC use appenders log4j configurations. Here are the list of the predefined ARC appenders that may be used to log the whole software or specific java class.

``` xml
<appender-ref ref="console_trace" />
<appender-ref ref="console_debug" />
<appender-ref ref="console_info" />
<appender-ref ref="console_warn" />
<appender-ref ref="console_error" />
<appender-ref ref="query_trace" />
```

Commenting an appender will disable it.

The appender ```<appender-ref ref="query_trace" />``` triggers the logging of SQL queries runned by the application.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

<log4j:configuration>
<!-- APPENDERS LIST -->

<!-- Pour un affichage dans la console -->
  <!-- Traces -->
  <appender name="console_trace" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%5p %d{DATE}- %c{1}:%-4L - %m%n" />
    </layout>
    <filter class="org.apache.log4j.varia.LevelRangeFilter">
      <param name="LevelMin" value="TRACE" />
      <param name="LevelMax" value="TRACE" />
    </filter>
  </appender>
  <appender name="query_trace" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%m%n" />
    </layout>
    <filter class="org.apache.log4j.varia.LevelRangeFilter">
      <param name="LevelMin" value="TRACE" />
      <param name="LevelMax" value="TRACE" />
    </filter>
  </appender>

  <!-- Debug -->
  <appender name="console_debug" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%5p %d{DATE}- %c{1}:%-4L - %m%n" />
    </layout>
    <filter class="org.apache.log4j.varia.LevelRangeFilter">
      <param name="LevelMin" value="DEBUG" />
      <param name="LevelMax" value="DEBUG" />
    </filter>
  </appender>

<!-- Info -->
  <appender name="console_info" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%5p %d{DATE}- %c{1}:%-4L - %m%n" />
    </layout>
    <filter class="org.apache.log4j.varia.LevelRangeFilter">
      <param name="LevelMin" value="INFO" />
      <param name="LevelMax" value="INFO" />
    </filter>
  </appender>

<!-- Warn -->
  <appender name="console_warn" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%5p %d{DATE}- %c{1}:%-4L - %m%n" />
    </layout>
    <filter class="org.apache.log4j.varia.LevelRangeFilter">
      <param name="LevelMin" value="WARN" />
      <param name="LevelMax" value="WARN" />
    </filter>
  </appender>

  <!-- Error -->
  <appender name="console_error" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%5p %d{DATE}- %c{1}:%-4L - %m%n" />
    </layout>
    <filter class="org.apache.log4j.varia.LevelRangeFilter">
      <param name="LevelMin" value="ERROR" />
      <param name="LevelMax" value="ERROR" />
    </filter>
  </appender>

<!-- LOGGER LIST -->
  <logger name="fr.insee.arc_composite" additivity="false">
    <level value="INFO" />
    <appender-ref ref="console_info" />
    <appender-ref ref="console_debug" />
  </logger>

  <logger name="fr.insee.siera.core.dao" additivity="false">
    <level value="TRACE" />
    <appender-ref ref="console_error" />
    <appender-ref ref="query_trace" />
  </logger>

<!-- Logger specifique au service d'extraction de données -->
  <logger name="fr.insee.arc_composite.core.service.extraction" additivity="false">
    <level value="INFO" />
    <appender-ref ref="console_info" />
  </logger>

<!-- base logger -->
  <root>
    <priority value="ERROR"></priority>
    <appender-ref ref="console_trace" />
    <appender-ref ref="console_debug" />
    <appender-ref ref="console_info" />
    <appender-ref ref="console_warn" />
    <appender-ref ref="console_error" />
  </root>

</log4j:configuration>
```

### Database configuration parameters

1. Database version must be Postgres 9.6 or higher

2. The following database parameters are set by the application :

  * synchronous_commit = 'off'
    * synchonous_commit is disabled for performance purpose as the ARC process innerly prevents from loosing data
  * enable_bitmapscan = 'off'
    * Bitmapscan index are disabled for better optimizer plans
  * vacuum_cost_limit = '10000'
    * The application takes care of the database maintenance especially the pg_catalog schema which is heavily used by arc. The standart maintenance process implemented by postgres is not efficient enough under massive data load.

#### Enchanced configuration for massive load in production environment

1. Disable archive_mode
    * The database server will likely not support the heavy load induced by archive wal storage with massive parallelism
2. monitor pg_xlog usage to set max_wal_size
    3. The wal log buffers (pg_xlog) must be configured high enough to support heavy data manipulation. It is set to 50Go on our biggest application instance.

## How to use ARC data retrieval web-service

### POST url

The queries to ARC data retrieval web-service must be sent to the folowing url : http://locahost:8080/arc-ws/webservice/
> *Change the host ip adress and port number according to the tomcat server and tomcat ARC application context configuration.*

### Structure of queries

#### WS0 : Calculate data tables and provide a client token

ARC will generate the temporary tables containing the data to be retrieved and will provide a client token.

#### WS0 request structure

* Type : JSON

``` js
{
  "environnement" : "the name of the sandbox database schema where the data are located"
  , "reprise" : "true or false. true = no mark, false = mark the data retrieved for further data managements such as data deletion after retrieval"
  , "familleNorme" : "the name of the target database user model"
  , "periodicite" : "the periodicity of data (M for monthly, A for yearly)"
  , "setValiditeInf" : "(opts) the minimal validity data for the data to be retrieved"
  , "setValiditeSup" : "the maximal validity data for the data to be retrieved"
  , "arcClient" : "the name of the client defined by the user in ARC"
}
```

##### Example of data retrieval token initialisation, from sandbox bas2, for testing purpose that's say with no mark for deletion, from the data model called "LIASSE", concerning the files for the client called "ESANE", monthly marked and with date reference under 2019-01-01

``` js
{
      "environnement" : "arc_bas2"
    , "reprise" : "true"
    , "familleNorme" : "LIASSE"
    , "periodicite" : "M"
    , "setValiditeSup" : "2019-01-01"
    , "arcClient" : "ESANE"
}
```

#### WS0 response structure

* Type : stream
* Format : plain/text
* Content :

    * the token id provided by ARC to client
    *> arc_bas2.artemis_128546456456456*

### WS1 : Get one of the generated table name and structure

ARC will return the name and the definition of the first temporary table associated with the token

#### WS1 request structure

* Type : JSON

```json=
{
    "tableName" : "the token id provided by ARC to client"
}
```

#### WS1 response structure

* Type : stream
* Format : plain/text
* Content :

  * the name of the table
    *> arc_bas2.artemis_128546456456456_mapping_esane_entete_ok*
  * the data definition. 
    *> siren text, id_entete text, id_source text, depot_entete text*

### WS2 : Stream a table data content

ARC will copy the data of the table to response stream. When the response is consumed, ARC delete the temporary table.

#### WS2 request structure

* Type : JSON

``` js
{
  "tableContent" : "the name of the table"
  ,"csv" : "(opts) require csv data format"
}
```

#### WS2 response structure

* Type : stream
* Format : inputStream
* Content : by default, postgres copy binary stream format. If the csv parameter had been set in the json request, the returning content will be streamed in csv format

### How to retrieve data

``` java
1. Use WS0 to generate temporary table and get a token
do {
  1. Use WS1 to get the table name and structure of the data to be retrieved
  2. Use WS2 to copy the data to the client
} (while WS1 finds a table to proceed)
```
