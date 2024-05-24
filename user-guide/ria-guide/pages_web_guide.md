# Construire une page dans arc-web

Ce document est une aide pour la création ou la modification de pages Web dans ARC.

## Vue d'ensemble des fichiers constituant une page Web dans ARC

### Nouveaux fichiers

- Composante java : `arc-web/src/main/java/fr/insee/arc/web/gui/nouvellepage`
	- Couche contrôleur : `/controller` avec 1 fichier Controller par vue
	- Couche DAO : `/dao` avec 1 fichier DAO
	- Couche modèle : `/model` avec 1 fichier Model + 1 fichier View par vue
	- Couche service : `/service` avec 1 fichier Interactor + 1 fichier Service par vue
- Page jsp : `arc-web/src/main/webapp/WEB-INF/jsp/nouvellepage.jsp`
- Fichier js : `arc-web/src/main/webapp/js/nouvellepage.js` (ne contient que la config)

### Fichiers à modifier

- `arc-web/src/main/resources/messages_[en&fr].properties` : libellés avec localisation
- `arc-web/src/main/webapp/WEB-INF/jsp/tiles/header.jsp` ou tout autre page pertinente, pour ajouter un lien vers la nouvelle page

## Les fichiers Java : le patron MVC

L'architecture modèle-vue-contrôleur (MVC) est une architecture logicielle qui consiste à séparer en modules distincts l'affichage, le métier et les données d'une page, pour rendre le code plus maintenable et compartimenté. Cette architecture peut être complétée par des objets d'accès aux données (DAO) qui regroupent les connexions et requêtes faites sur les données persistantes.

Dans **arc-web**, chaque page Web peut contenir plusieurs vues, et à chaque vue sont associés un contrôleur, un modèle et une méthode d'accès aux données. Dans `fr.insee.arc.web.gui`, une page Web XXX affichant des vues YYY est un package `XXX` contenant des packages correspondant aux différentes couches du modèle MVC : 
- la couche **modèle** (`model`), avec
    - une classe composant **ModelXXX**, contenant toutes les vues de la page XXX,
    - pour chaque vue une classe **ViewYYY** ;
- la couche **service** (`service`), avec
    - une classe service **InteractorXXX** ou **XXXAction** pour initialiser la page et les VObjects,
    - pour chaque vue une classe **ServiceViewYYY**, devant contenir si nécessaire les actions de base du VObject : selectYYY, sortYYY, addYYY, updateYYY, deleteYYY ;
- la couche **contrôleur** (`controller`), avec pour chaque vue une classe contrôleur **ControllerViewYYY** liant les requêtes Web aux actions du service.
- la couche **DAO** (`DAO`), avec une classe DAO **XXXDAO** gérant les requêtes à envoyer à la base de données.

Le framework Web MVC de Spring est utilisé.

### Le package `model`

Il se compose de deux types de classe : 
- une classe `Model[Nom de la page]`
- des classes `View[Nom de la vue]`

#### La classe `Model`

La classe modèle est annotée `@Component` dans Spring, et implémente `ArcModel`. C'est le modèle de la page, elle répertorie toutes les vues de la page : 
```java=
@Component
public class ModelPage implements ArcModel {
    private VObject viewUn;
    private VObject viewDeux;
    private VObject viewTrois;
    // ...
        
    public ModelPage() {
    	this.viewUn = new ViewUn();
    	this.viewDeux = new ViewDeux();
    	this.viewTrois = new ViewTrois();
    	// ...
    }
    
    // getters et setters
}
```

#### Les classes `View`

Dans **ARC**, les vues correspondent à des tableaux dynamiques de données des bases. Pour afficher ces tableaux, on utilise la classe `VObject`. Les classes vues héritent de `VObject`.

```java=
public class ViewUn extends VObject {
    public ViewUn() {
        super();
        
        this.setTitle("Nom de la vue"); // nom affiché sur la page Web
        this.setDefaultPaginationSize(5); // nombre de lignes par défaut
        this.setSessionName("viewUn");
		
        this.setConstantVObject(new ConstantVObject
            (new HashMap<String, ColumnRendering>()
        {
            private static final long serialVersionUID = ; // généré par l'IDE
            {
                put("nom_colonne", new ColumnRendering(...));
                // ...
            }
        }
        ));
    }
}
```

La méthode `ColumnRendering` permet de déterminer les colonnes à inclure dans le tableau et à faire le lien avec les variables des tables en base. Elle prend les paramètres suivants : 
- **visible** : booléen déterminant si la colonne est affichée
- **label** : nom de la colonne tel qu'affiché sur la page
- **size** : largeur de colonne en pixels ou pourcentage
- **type** : **text** pour entrée texte, **select** pour liste déroulante
- **query** : si **type = select**, requête SQL pour obtenir les items de la liste déroulante
- **isUpdatable** : booléen déterminant si la colonne peut être modifiée

### Le package `service`

Il se compose de deux types de classe : 
- une classe `Interactor[Nom de la page]`
- des classes `ServiceView[Nom de la vue]`

#### La classe `Interactor`

La classe interacteur est annotée `@Service` dans Spring (le `@Scope` request doit être également renseigné), et hérite de `ArcWebGenericService<Model[Nom de la page]>`. Elle permet d'initialiser la page Web avec tous les VObject.

La classe doit inclure un attribut de classe `Model[Nom de la page]` et annoté `@Autowired`. Cette annotation permet d'injecter une instance de `Model[Nom de la page]` et d'utiliser ses attributs et méthodes.

La méthode `putAllVObjects` permet d'initialiser toutes les vues de la page :

```java=
@Override
public void putAllVObjects(ModelGererFamille arcModel) {
    LoggerHelper.debug(LOGGER, "putAllVObjects()");

    views.setViewUn(vObjectService.preInitialize(arcModel.getViewUn()));
    views.setViewDeux(vObjectService.preInitialize(arcModel.getViewDeux()));
    views.setViewTrois(vObjectService.preInitialize(arcModel.getViewTrois()));
    // ...

    putVObject(views.getViewUn(), t -> initializeUn());
    putVObject(views.getViewDeux(), t -> initializeDeux());
    putVObject(views.getViewTrois(), t -> initializeTrois());
    // ...

    LoggerHelper.debug(LOGGER, "putAllVObjects() end");
}
```

Chaque vue a sa méthode d'initialisation `initialize[Nom de la vue]`, qui contient notamment l'appel au DAO permettant de retrouver les données du tableau, par exemple : 

```java=
private void initializeUn(VObject viewUn) {
    LoggerHelper.debug(LOGGER, "/* initializeUn */");
    dao.initializeViewUn(viewUn);
}
```

#### Les classes `ServiceView`

Les classes service sont annotées `@Service` dans Spring et héritent de la classe interacteur. Elles contiennent les fonctionnalités liées à leur VObject, notamment si nécessaire les fonctions des boutons de base : 
- **select[Nom de la vue]** pour rafraîchir le VObject
- **sort[Nom de la vue]** pour trier par une colonne
- **add[Nom de la vue]** pour ajouter une entrée
- **update[Nom de la vue]** pour modifier et mettre à jour une entrée
- **delete[Nom de la vue]** pour supprimer une entrée

Ces méthodes renvoient un display sous forme de String : 
```java=
public String doFonctionnalite(Model model) {
    // implémentation de la fonctionnalité
    return generateDisplay(model, RESULT_SUCCESS);
}
```

### Le package `controller`

Il se compose de tous les contrôleurs d'une même page, soit un ensemble de classes `ControllerView[Nom de la vue]`. Les classes contrôleur font le lien entre les requêtes HTTP et les actions du service en invoquant le modèle de données.

Une classe contrôleur est annotée `@Controller` dans Spring, et hérite de `ServiceView[Nom de la vue]`.

Les méthodes d'une classe contrôleur sont généralement de la forme : 
```java=
@RequestMapping("/fonctionnalite")
public String fonctionnaliteAction(Model model) {
    return fonctionnalite(model);
}
```
- `fonctionnalite` est une action de la vue, implémentée dans la classe service.
- l'annotation `@RequestMapping` permet d'associer une requête HTTP à cette action du contrôleur.

### Le package `dao`

Il se compose de la seule classe `[Nom de la page]Dao`. La classe DAO concentre toutes les requêtes SQL accédant à la base Postgres de ARC.

La classe DAO hérite de `VObjectHelperDao` qui regroupe quelques méthodes utiles. Elle inclut les attributs `VObjectService` pour le lien avec le VObject, et `DataObjectService` pour la connexion à Postgres.

Les méthodes de la classe DAO concernent généralement une seule requête SQL. Les méthodes d'initialisation des VObject, par exemple, sont de la forme : 

```java=
public void initializeViewUn(VObject viewUn) {
    ViewEnum dataModelUn = ViewEnum.TABLE;
    ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
    query.append(SQL.SELECT);
    // reste de la requête
    Map<String, String> defaultInputFields = new HashMap<>();
    vObjectService.initialize(viewUn, query,
        dataObjectService.getView(dataModelUn), defaultInputFields);
    }
```

- `ViewEnum` est une énumération des tables de ARC.
- Pour éviter les injections, il convient d'utiliser des `ArcPreparedStatementBuilder` pour formuler les requêtes SQL. L'énumération `SQL` contient les principaux mots-clés comme `SELECT` ou `FROM`.
- `defaultInputFields` sont les valeurs par défaut du VObject. Dans cet exemple, il n'y a pas de valeurs par défaut donc la `HashMap` est laissée vide.

## La page jsp : l'utilisation de VObject

Un **VObject** est un objet à inclure dans une jsp qui permet d'afficher sur une page web, un tableau de valeurs dynamique et éditable.

### Pré-requis du projet

Pour faire fonctionner VObject dans le projet, ARC a besoin de :
- `templateVObject.jsp` dans le répertoire jsp
- `component.js` dans le répertoire js
- le framework Bootstrap

### Inclure le VObject dans la page

Dans le fichier jsp, à l'endroit où inclure le VObject, écrire : 

```htmlembedded=
<c:set var="view" value="${viewYYY}" scope="request"/>
<c:import url="tiles/templateVObject.jsp">
    <c:param name="nom" value ="valeur" />
    ...
</c:import>
```
Autant de paramètres que nécessaire peuvent être ajoutés avec la syntaxe `<c:param name="nom" value ="valeur" />`. Les paramètres disponibles ont les noms et valeurs suivantes : 
- **taille** renseigne la largeur du VObject dans la page, en prenant des valeurs entre **col-md-1** et **col-md-12** (valeurs entières uniquement), par défaut le VObject prend toute la largeur disponible

Les paramètres suivants permettent d'afficher des boutons et fonctionnalités en renseignant la valeur **true**. Par défaut la valeur est **false** et ils ne sont pas affichés : 
- **btnSelect** pour afficher le bouton de rafraîchissement
- **btnSee** pour afficher le bouton de vue (pour le fonctionnement d'anciens écrans, plus nécessaire)
- **btnSort** pour permettre de trier les colonnes en cliquant sur leur nom
- **btnAdd** pour afficher le bouton d'ajout d'entrée (**ligneAdd** doit être activé pour remplir l'entrée à ajouter)
- **btnUpdate** pour afficher le bouton de mise à jour, qui permet de modifier le tableau directement et d'enregistrer les modifications
- **btnDelete** pour afficher le bouton de suppression d'entrée (**checkbox** doit être activé pour sélectionner l'entrée à supprimer)
- **ligneAdd** pour afficher la ligne d'ajout d'entrée en bas du tableau
- **ligneFilter** pour afficher la ligne de filtrage des entrées en haut du tableau
- **checkbox** pour afficher les cases à cocher à gauche de chaque entrée
- **multiSelection** pour autoriser la sélection de plusieurs cases à cocher (inutile si **checkbox** est désactivé)

### Fiches d'aide aux utilisateurs

En haut à droite de chaque vObject se trouve un bouton **(?)** : sur ARC, il renvoie vers [une page de documentation utilisateur](https://github.com/InseeFr/ARC/blob/master/user-guide/ihm_views_doc.md), en redirigeant automatiquement vers la partie de cette page portant le nom de la vue. Ces fiches de documentation décrivent le contenu des vObject avec éventuellement des explications, cas d'utilisation ou bonnes pratiques.

## Autres fichiers à (éventuellement) modifier

### Gestion du texte et sa localisation

ARC est disponible en français et en anglais. Les fichiers `messages_fr.properties` et `messages_en.properties` de `arc-web/src/main/resources/` permettent de gérer la localisation des différents textes de l'IHM.

Les messages localisés peuvent être intégrés à la page de différentes manières.

#### Directement sur la page jsp

On utilise alors spring message :
```jsp
<spring:message code="mon.message"/>
```
À utiliser notamment pour les headers de page.

#### Pour un nom de vObject

On précise dans le constructeur de ViewYYY.java :
```java
this.setTitle("view.yyy");
```
La vue affichera le nom correspondant à `view.yyy`.

#### Pour un nom de colonne

On précise dans la définition de la columnMap de ViewYYY.java :
```java
columnMap.put("maColonne", new ColumnRendering(true, "label.maColonne", "50%", "text", null, true));
```
La colonne affichera le nom correspondant à `label.maColonne`.

### Ajouter un lien vers la page dans le header

Dans le fichier `header.jsp`, dans la catégorie adéquate, ajouter
```jsp
<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="maPage"/><c:param name="linkId" value="maPage"/></c:import>
```
Le paramètre `linkRef` va chercher dans `action.maPage` le nom du endpoint à aller chercher. `action.maPage` doit être bien renseigné dans `messages_fr.properties` et `messages_en.properties`.

Le paramètre `linkId` va chercher dans `header.maPage` le titre de la page pour en faire l'intitulé du lien cliquable.