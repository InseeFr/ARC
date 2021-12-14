# Change Log

All notable changes to this project will be documented in this file.

## [Unreleased]

## version-91.1.3 - 2021-12-14
- sonar security fix
- for security purpose, ihm has now distinct public page index and a secured index
- keycloak.resource=keycloak.json settings is now default. ARC now detects if keycloak is activated by scanning the "realm" and "auth-server-url" value. No changes required for current installations
- log4j security fix
- the retrieved data webservice wand now be used to retrieve specific data types ("mapping", "nomenclature", "metadata")
- maintenance parameters GUI to set the way ARC works is now available
- archive reader now works for archive containing several directories

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

