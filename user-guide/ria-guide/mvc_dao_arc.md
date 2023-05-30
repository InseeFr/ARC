# MVC et DAO dans ARC

L'architecture modèle-vue-contrôleur (MVC) est une architecture logicielle qui consiste à séparer en modules distincts l'affichage, le métier et les données d'une page, pour rendre le code plus maintenable et compartimenté. Cette architecture peut être complétée par des objets d'accès aux données (DAO) qui regroupent les connexions et requêtes faites sur les données persistantes.

Dans **arc-web**, chaque page Web peut contenir plusieurs vues, et à chaque vue sont associés un contrôleur, un modèle et une méthode d'accès aux données. Une page Web dans `fr.insee.arc.web.gui` est un package contenant les packages `controller` (contrôleurs de la page), `dao` (objet d'accès à Postgres), `model` (modèles de la page) et `service` (vues de la page). Le framework Web MVC de Spring est utilisé.

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
    HashMap<String, String> defaultInputFields = new HashMap<>();
    vObjectService.initialize(viewUn, query,
        dataObjectService.getView(dataModelUn), defaultInputFields);
    }
```

- `ViewEnum` est une énumération des tables de ARC.
- Pour éviter les injections, il convient d'utiliser des `ArcPreparedStatementBuilder` pour formuler les requêtes SQL. L'énumération `SQL` contient les principaux mots-clés comme `SELECT` ou `FROM`.
- `defaultInputFields` sont les valeurs par défaut du VObject. Dans cet exemple, il n'y a pas de valeurs par défaut donc la `HashMap` est laissée vide.


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
    loggerDispatcher.debug("putAllVObjects()", LOGGER);

    views.setViewUn(vObjectService.preInitialize(arcModel.getViewUn()));
    views.setViewDeux(vObjectService.preInitialize(arcModel.getViewDeux()));
    views.setViewTrois(vObjectService.preInitialize(arcModel.getViewTrois()));
    // ...

    putVObject(views.getViewUn(), t -> initializeUn());
    putVObject(views.getViewDeux(), t -> initializeDeux());
    putVObject(views.getViewTrois(), t -> initializeTrois());
    // ...

    loggerDispatcher.debug("putAllVObjects() end", LOGGER);
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
