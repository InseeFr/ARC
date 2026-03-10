# Le workflow de ARC

Ce document décrit le workflow de ARC, c'est à dire le circuit de validation de nouvelles versions de ARC.

## Politique de versionnement

### Nommage des commits

Il n'y a pas de convention stricte pour le nommage des commits de ARC. Par habitude, les messages de commit sont écrits en anglais et précédés de "fix :" si c'est une correction, ou "feat :" si c'est une évolution.

### Releases

Les tags de version sont de la forme **version-XX.YY.ZZ(b)** avec :
- **XX** : n° de version majeure, dont le changement est impactant pour les clients
- **YY** : n° de version majeure, mais dont le changement ne requiert pas d'opération client
- **ZZ** : n° de version mineure
- **b** : lettre à ajouter pour indiquer une version bêta

Par exemple : **version-94.1.42b**

À chaque nouvelle version générée, il est important de mettre à jour le [CHANGELOG.md](https://github.com/InseeFr/ARC/blob/master/CHANGELOG.md) pour journaliser cette nouvelle release avec les changements qu'elle apporte. La changelog renseigne également les clients sur les maintenances en cours dans la partie TODO List. La changelog est écrite en anglais.

Pour être mieux accompagnés en cas de problème sur leur instance d'ARC, les clients doivent déployer la dernière version non-bêta de l'application.

Les versions bêta doivent faire l'objet de **tirs de performance** systématiques (~1 par semaine), afin de régulièrement valider les développements en cours. Ces versions bêta, une fois validées, peuvent alors optionnellement devenir des versions non-bêta.
- **Siera** : la planification de ces tirs est abordée lors de points hebdomadaires.
- **Résil** : leur planification devra être mis en place, notamment pour ARC scalable, et dans l'univers Kubernetes.

## Que se passe-t-il lors de la création d'une nouvelle release ?

### Création des livrables

Les pipelines exécutés pour une nouvelle version du code sont décrits dans les fichiers du [workflow](https://github.com/InseeFr/ARC/tree/master/.github/workflows). (si le répertoire n'est pas visible dans Eclipse, aller dans Project Explorer > Filters (icône entonnoir), et décocher ".* resources")
- **build.yml** : mise à jour de Sonar et exécution des tests
- **deploy-on-docker.yml** : image latest sur commit
- **release-on-tag.yml** : générer la release et déposer sur dockerhub les images versionnées

Lors d'un commit sur la branche `master` sans tag :
- Sonar est mis à jour et les tests sont lancés
- Trois nouveaux tags "latest" sont déposés sur Dockerhub : [inseefr/arc:latest](https://hub.docker.com/r/inseefr/arc/tags), [inseefr/arc-batch:latest](https://hub.docker.com/r/inseefr/arc-batch/tags) et [inseefr/arc-ws:latest](https://hub.docker.com/r/inseefr/arc-ws/tags)
    - Cette image peut être utile pour faire des tests. On s'y réfère alors avec son sha : inseefr/arc@sha256:xxxx...

Lors de la création d'un tag "version-XX.YY.ZZ" :
- Sonar est mis à jour et les tests sont lancés
- Une release "version-XX.YY.ZZ" est créée
- Trois nouveaux tags "XX.YY.ZZ" sont déposés sur Dockerhub : [inseefr/arc:XX.YY.ZZ](https://hub.docker.com/r/inseefr/arc/tags), [inseefr/arc-batch:XX.YY.ZZ](https://hub.docker.com/r/inseefr/arc-batch/tags) et [inseefr/arc-ws:XX.YY.ZZ](https://hub.docker.com/r/inseefr/arc-ws/tags)

L'analyse Sonar de ARC est accessible [ici](https://sonarcloud.io/project/overview?id=fr.insee:arc), ou depuis le [dépôt Github](https://github.com/InseeFr/ARC), sur l'icône "quality gate" du README.

Pour récupérer les livrables sur Github et générer les livrables en interne, on passe par le projet [ARC Gitlab](https://gitlab.insee.fr/sndi-orleans/groupe-sdi/arc). Automatiquement, les livrables sont récupérés 2 fois par jour. Mais si besoin, on peut effectuer un lancement manuel via Pipelines > Run pipeline sur la branche run_with_gitlab.

Les livrables Insee reconstitués sont alors déposés sur le [Nexus](https://nexus.insee.fr/#browse/browse:depot-local), dans le répertoire arc.

### Déploiement

Une fois les livrables déposés sur le Nexus, il faut communiquer aux clients le nouveau numéro de version, les principaux changements et le lien vers le changelog. L'annonce se fait via les canaux Tchap :
- Insee - arc-sirene4
- Insee - arc-resil
- Insee - arc-esane
- Insee - arc-siera 

Chaque client dispose de son propre pipeline d'intégration. Il leur appartient alors le choix ou non de déployer la nouvelle version.
