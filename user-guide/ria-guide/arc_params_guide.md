# Ajouter un paramètre de configuration de ARC

Ce document est une aide pour l'ajout de paramètres de configuration de l'application ARC.

## Ajouter au code

### PropertiesHandler

- Ajouter un attribut dans la classe `arc-utils/src/main/java/fr/insee/arc/utils/ressourceUtils/PropertiesHandler.java` :
```
private Type nomDuParametre;
```
- Lui créer un getter et un setter

### Kubernetes

Pour un paramètre Kubernetes :

- Modifier `arc-core/src/main/java/fr/insee/arc/core/service/kubernetes/configuration/ExecutorDatabaseStatefulTemplate.java`
pour inclure le paramètre à l'endroit adéquat, sous la forme `{nom_du_parametre}`
- Ajouter dans `arc-core/src/main/java/fr/insee/arc/core/service/kubernetes/bo/JsonFileParameter.java` :
```
public static final String NOM_DU_PARAMETRE = "{nom_du_parametre}";
```
- Ajouter dans `arc-core/src/main/java/fr/insee/arc/core/service/kubernetes/configuration/BuildJsonConfiguration.java` :
```
, JsonFileParameter.NOM_DU_PARAMETRE, properties.getNomDuParametre()
```
pour faire le lien avec PropertiesHandler

## Ajouter aux applications

### Properties

- Ajouter dans `arc-batch/src/main/resources/applicationContext.xml`,
`arc-web/src/main/resources/applicationContext.xml` et/ou `arc-ws/src/main/webapp/WEB-INF/applicationContext.xml` :
```
<property name="nomDuParametre" value="#{systemEnvironment['NOM_DU_PARAMETRE'] ?: '${fr.insee.arc.nom.du.parametre}' }"></property>
```
- Ajouter dans `arc-batch/src/main/resources/fr/insee/config/arc.properties`,
`arc-ws/src/main/resources/fr/insee/config/arc.properties` et/ou `arc-ws/src/main/resources/fr/insee/config/arc.properties` :
```
fr.insee.arc.nom.du.parametre=${env.nomDuParametre:}
```

### Docker

- Ajouter dans `docker/app-batch.Dockerfile`, `docker/app-web.Dockerfile` et/ou `docker/app-ws.Dockerfile` :
```
ARG NOM_DU_PARAMETRE
```
- Modifier le `docker/script.sh` pour inclure dans les options de la dernière ligne :
`-Denv.nomDuParametre=$NOM_DU_PARAMETRE`

## Documenter le nouveau paramètre

- Compléter `user-guide/arc_parameters.md` avec le paramètre nouvellement créé
