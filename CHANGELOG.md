# Change Log

## TODO List
- enable tar compression for bigger files
- All notable changes to this project will be documented in this file.
- jacoco aggregate

## version-94.2.30b
- performance. Vertical scaling for the operation that maps data to model table. For each model data table, one data insertion thread is created to lower the issue of having many model data tables.

## version-94.2.29b
- ephemeral pvc for temporary tablespace

## version-94.2.28
- added jacoco aggregate for sonar accuracy
- non beta version

## version-94.2.26b
- postgres 18 support

## version-94.2.25b
- duckdb upgrade version 1.4
- minio upgrade version 1.6.0
- new parameter stop_on_backup to stop batch when an hot database backup has started. It checks the archive command to see if archive are disabled or redirected to bin. Production is stopped when archive mode is enabled ans not redirected to bin.

## version-94.2.20b
- spring security 6.5.4

## version-94.2.20b
- spring security 6.5.4

## version-94.2.19b
- export phase update
- when the export phase is activated, users may now set the target of mapping tables in the export data menu
  - default export targets of mapping tables are parquet files
  - user can also set target the the master nod
    - mapping table data are copied from executors nod to master nod during the export phase

## version-94.2.15b
- changed postgres docker image from bitnami to standart postgres

## version-94.2.10
- fix the non-copying of type_consolidation when importing family rules

## version-94.2.9
- minio client version
- security fix

## version-94.2.4b
- minimal csp policy
- changed uploads because of tomcat 10.1.43 multipart files changes

## version-94.2.1b
- security fix
- most of the process queries now don't use autocommit in order to avoid database connection locks

## version-94.1.117
- fix a crash bug when stopping and resuming batch pipeline
- quality
- added a configurable emptyDir usable to set a temporary tablespace for the kubernetes postgres executors
  - set KUBERNETES_EXECUTOR_TEMPTABLESPACE_MEDIUM to "Memory"for a memory drive
  - set KUBERNETES_EXECUTOR_TEMPTABLESPACE_MEDIUM to "" for a normal ephemeral drive

## version-94.1.112
- spring security critical upgrade
- webjar for js dependencies

## version-94.1.110b
- fix in multiphase api : temporary data are deleted when no longer used

## version-94.1.108b
- new multiphase api : file are now fully proceed in separate threads
- fix filesystem maintenance problem when files no longer exists

## version-94.1.105
- removed hotfix version-94.1.104
- spring security and zonky io maven dependencies upgrade

## version-94.1.104
- hotfix : check if an instance of arc batch is already running

## version-94.1.102
- sandbox list retrieved by data retrieval webservice are stored in mod_environnement_arc entity

## version-94.1.101b
- new library to read tar.gz as apache one throws crc32 errors randomly
- the data retrieval webservice now gives the sandbox meta table in metadata
- optimisation for pilotage dead tuples maintenance

## version-94.1.96
- optimization : keep intermediate columns in gui batch mode

## version-94.1.95b
- optimization : single batch connection and vacuum strategy

## version-94.1.95b
- optimization : single batch connection

## version-94.1.91
- fix bug in data retieval webservice

## version-94.1.91
- fix bug in data retieval webservice

## version-94.1.89
- fix java null pointer exception when crc error on reader

## version-94.1.87b
- fix parquet file export memory leak
- download data to s3 gui button works again
- if s3 is active, files which have triggered batch error are sent to s3
- fix executor ephemral pvc
- ignore independance rules when child tag doesn't exist

## version-94.1.79b
- add priority rules to sort incoming files

## version-94.1.77b
- common.io security version update

## version-94.1.76b
- postgres scanner version upgrade

## version-94.1.75b
- bug fix to catch when parquet export fail in order to interrupt batch correctly

## version-94.1.74b
- spring security update
- datawarehouse and archives inside datawarehouse can be loaded according to an order defined by user. Check "entrepot" gui.

## version-94.1.70
- add kubernetes ephemeral volume management for executor pods

## version-94.1.65
- fix web service not waiting for large data to be created

## version-94.1.64
- spring update

## version-94.1.65
- parquet support for data retrieval webservice
- enhanced logs for data retrieval webservice

## version-94.1.59b
- clear parquet export directory if s3 output bucket is defined

## version-94.1.58
- jwt authentification for data retrieval webservice if keycloak is set

## version-94.1.54b
- user_restricted right fix

## version-94.1.52b
- pvc is deleted in volatile mode after batch ends
- fix file action (delete, replay, archive replay)
- fix batch exit when production is set to off
- fix the total file size limiter used by reception phase in batch mode
- Add a new database parameter LanceurIHM.tailleMaxReceptionEnMb to limit the total file size to be received in gui mode.

## version-94.1.48b
- bug fix for deirectory creation in kubernetes pvc

## version-94.1.45b
- add parameters KUBERNETES_EXECUTOR_CPU, KUBERNETES_EXECUTOR_RAM

## version-94.1.44b
- file retention period can be in set fr.insee.arc.files.retention.days
- KO will now be deleted from database after data retention period

## version-94.1.43
- rework external table column red for file to be valid in database
- bug fix on parquet encryption key

## version-94.1.42b
- reduce privilege level for kubernetes executor databases
- sql constraints on the table parameter

## version-94.1.41b
- debug gui access can now be set in a distinct keycloak group (fr.insee.gui.debug.disable)
- exception handling for access denied
- docker images now have version. They are deployed through the "released on tag" workflow and handle same version number.

## version-94.1.33b
- rest webservice securisation for injection
- gui database securisation for injection (add gui constraints for all rules)
- new gui generic error message
- docker add : parquet encryption key for minio s3 export
- docker add : keycloak

## version-94.1.12b
- checkmarx fix
- databases ip caching to avoid dsn spam in batch mode
- deprecated spring security fix (rest webservice and web)
- action are now logged with idep

## version-94.1.11b
- ARC batch exports data to parquet (when fr.insee.arc.process.export property is not empty). Default is off
- ARC batch pops, uses and removes executor database (when fr.insee.arc.kubernetes.executor.volatile is not empty). Default is off

## version-94.1.10
- bug fix for gz archive entry name
- parquet export

## version-94.1.9
- bug fix with filter gui

## version-94.1.8
- gui bug fix : import rules and update comment in nomenclature screen
- tomcat fix as a library was missing in new version of tomcat 10.8

## version-94.1.7
- removed webservice entrypoint /execute/engine/{serviceName}/{serviceId}/{bas} as it looks unused

## version-94.1.3
- webservice data retrieval servlet access fix
- kubernetess support
- disablegui property now working

## version-94.1.1
- tomcat 10, java 17, spring 6

## version-93.1.36
- fix : ws import wait for large data tables
- last version in tomcat 9, java 11

## version-93.1.34b
- avoid overflow when large file data are set on a single line

## version-93.1.32b
- batch failed with functional warning

## version-93.1.31b
- upload mapping rules now the model declared variables

## version-93.1.30b
- scalable data retrieval webservice

## version-93.1.29
- csv_gzip format is available for the data retrieval webservice

## version-93.1.26
- dependencies security fix

## version-93.1.25
- fix performance issue in to_delete reception query
- bind variable in gererFamille

## version-93.1.22
- production version

## version-93.1.21b
- deployment for version qualification

## version-93.1.19b
- initialization optimization

## version-93.1.18b
- the webservice for data retrieval deletes the client pending data tables at the start of a new invoke
- the webservice for data retrieval acts creates data table asynchronously to avoid timeout problems

## version-93.1.7
- functionnal tests
- pg 14.8 bug fix
- bug fix in gui file report
- scalability connection management

## version-93.1.3
- passage en production

## version-93.1.1b
- version beta avec suppression de la phase de filtrage

## version-92.2.18
- github workflow java 11

## version-92.2.17 2023-11-03
- show sand box informations at the home page
- loader performance enhancement
- gui quality refactor
- wrong xsd location in webservice configuration
- unit test for technical and core class (part 1)

## version-92.2.14 2023-02-10
- upload data file via GUI is fixed to match with new security constraints

## version-92.2.12 2023-02-09
- web health-check is now public
- bug fix in normage view actions

## version-92.2.11 - 2023-01-30
- bug fix FileUtils.cleanDirectory with symbolic lock

## version-92.2.07 - 2023-01-12
- bug fix in duplicate detection

## version-92.2.06 - 2023-01-12
- gui refactor
- parallelism refactor

## version-92.2.04 - 2022-11-21
- healthcheck quality refactor and add-on for web module
- DDI model implementation
- bug fix in maintenance screen

## version-92.2.02 - 2022-10-14
- The batch logger default level was incorrect and fixed

## version-92.2.01 - 2022-10-03
- code quality
- secured the data retrieval service (https + whitelist)

## version-91.1.60 - 2022-06-02
- normage optimization for cartesian reduction with new independance rules

## version-91.1.55 - 2022-06-02
- model metadata can now be exported

## version-91.1.51 - 2022-06-02
- esane test for ci/cd

## version-91.1.50 - 2022-05-31
- deliverable name fix for internal ci/cd tested
- urls for hello, healthcheck and version pages are now public access

## version-91.1.45 - 2022-04-29
- batch optimization : better queuing for files processing to avoid downtime
- bug fix to download database output from the gui

## version-91.1.41 - 2022-04-29
- button to apply directly rules in production environments
- optimization of cartesian reduction algorithm
- spring and springboot security fix
- multiselection on export gui

## version-91.1.40 - 2022-04-29
- button to apply directly rules in production environments
- optimization of cartesian reduction algorithm
- spring and springboot security fix
- multiselection on export gui

## version-91.1.21 - 2022-03-14
- button to apply directly rules in production environments
- optimization of cartesian reduction algorithm

## version-91.1.20 - 2022-03-14
- add exclusion rules on structurize phase
- model tablename can now contain digits

## version-91.1.13 - 2022-02-22
- postgres driver security bump
- spring security bump

## version-91.1.12 - 2022-01-10
- h2 security fix
- git informations retrieval bug fix for batch

## version-91.1.10 - 2022-01-03
- fix version display message for batch

## version-91.1.9 - 2022-01-03
- revert phase ihm bugfix for reception phase

## version-91.1.8 - 2022-01-03
- files timestamp fix

## version-91.1.7 - 2022-01-03
- log4j 2.17.1

## version-91.1.6 - 2022-01-03
- log4j 2.17.0
- database h2 security fix

## version-91.1.4 - 2021-12-16
- fix #2 for log4j security jndi injection problem
- git-commit-id-plugin depandency update to get the tag number correctly

## version-91.1.3 - 2021-12-14
- sonar security fix
- for security purpose, ihm has now distinct public page index and a secured index
- keycloak.resource=keycloak.json settings is now default. ARC now detects if keycloak is activated by scanning the "realm" and "auth-server-url" value. No changes required for current installations
- log4j security fix
- the data retrieval webservice can now be used to retrieve specific data types ("mapping", "nomenclature", "metadata")
- maintenance parameters GUI to set the way ARC works is now available
- archive reader now works for tar.gz archives that contain several directories

## version-91.1.2 - 2021-10-04
- bug fix in user management interface due to a null attribute

## version-91.1.1 - 2021-09-03
- database and application version now directly picked from git metadata
- tabs in gui are now persitant
- new maintenance menu to set parameters and test loggers
- bugfixes in gui

## version-90.1.1 - 2021-08-02

- fixes bug preventing writing log to file (web-service)
- adds new property fr.insee.arc.log.level to configure ARC log level

## version-90.1 - 2021-06-29

- allows logging to file through property
- extends expressions to control rules
- fixes multithread bug on SimpleDateFormat causing NPE in mapping
- adds /healthcheck to webservice
- improves error message on key-value loading failing
- adds clarifying report to files marked KO because one other file in the archive could not be read

