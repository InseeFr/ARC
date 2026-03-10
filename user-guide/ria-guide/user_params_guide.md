# Utiliser les paramètres utilisateur de ARC

Ce document est une aide pour l'ajout et l'appel de paramètres utilisateur dans le code de l'application ARC.

## Où sont les paramètres utilisateur ? La page "Paramétrer l'application"

Les utilisateurs de ARC peuvent, depuis l'IHM, modifier de nombreux paramètres qui viennent modifier le comportement de l'application. C'est la page Maintenance > Paramétrer l'application. Quelques exemples sont détaillés dans la [documentation utilisateur](https://github.com/InseeFr/ARC/blob/master/user-guide/ihm_views_doc.md#viewParameters).

## Comment ajouter un paramètre utilisateur ?

Avant d'ajouter un nouveau paramètre, il faut s'assurer qu'il est bien pertinent d'en laisser le contrôle aux utilisateurs. Il est en effet parfois plus judicieux, notamment pour des paramètres techniques, de donner le contrôle aux équipes informatiques plutôt qu'aux statisticiens.

Pour créer un paramètre, il faut l'ajouter à la table des paramètres de la base de données de ARC. On écrit donc une requête INSERT dans le script_global.sql :
```sql
INSERT INTO arc.parameter VALUES ('Categorie.monParametre','valeurParDefaut');
UPDATE arc.parameter set
    description='parameter.categorie.descriptionDeMonParametre'
    where key='Categorie.monParametre';
```
Le paramètre sera créé et affiché sur la page IHM des paramètres après une regénération de la base de données. Les trois champs nécessaires à la création du paramètre sont :
- **key** : le nom technique du paramètre. C'est par ce nom que le paramètre sera invoqué dans le code par la suite. Dans l'exemple, c'est `Categorie.monParametre`.
- **value** : la valeur du paramètre, que l'utilisateur peut modifier dans l'IHM. À la création du paramètre, on lui donne donc une valeur par défaut.
- **description** : la description du paramètre pour l'utilisateur. Le système de locale permet d'afficher dans l'IHM une phrase simple expliquant ce que le paramètre modifie. Dans l'exemple, c'est `parameter.categorie.descriptionDeMonParametre`, on ajoutera donc la valeur de ce champ dans messages_en.properties et messages_fr.properties.

Pour être sûr de ne pas provoquer d'erreur avec une valeur de paramètre invalide, il faut ajouter une contrainte SQL sur cette valeur. Cela évitera que le paramètre soit enregistré avec une valeur invalide. Dans le fichier script_function_constraint.sql, dans la section `-- parameters rules`, on ajoutera alors une contrainte sur notre nouveau paramètre :
```sql
when key= 'Categorie.monParametre' then public.check_maMethode(val)
```
Il faudra alors soit écrire une nouvelle méthode de vérification (plus haut dans ce même fichier), soit utiliser l'une de celles déjà écrites, comme par exemple :
- **public.check_sandbox(val)** si le paramètre est un bac à sable de ARC
- **public.check_sandboxes(val)** si le paramètre est une liste de bacs à sable
- **public.check_integer(val)** si le paramètre est un nombre entier
- **val::boolean in (true,false)** si le paramètre est un booléen
- **public.check_word(val)** si le paramètre est un mot (caractères spéciaux autorisés $ _ -)
- **public.check_sql(val)** si le paramètre est une requête SQL

## Comment invoquer un paramètre utilisateur dans le code ? La classe BDParameters

La classe BDParameters de arc-core permet d'invoquer la valeur d'un paramètre utilisateur.
```java
BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);
String monParametre =
    bdParameters.getString(c, "Categorie.monParametre", "valeurParDefaut");
```
avec `c` une connexion à la base de données. Si le paramètre est un entier, on utilisera getInt(...).
