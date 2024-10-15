# Aide des vues de l'appication Web d'ARC

Ce document détaille l'utilisation des différents tableaux (appelés *view* ou vues) de l'application Web d'ARC. Les tableaux sont regroupés par écran.

## Accueil

### `viewIndex`

***"Utilisateurs des bacs à sable"*** : ce tableau est un aperçu de l'utilisation de tous les bacs à sable.

L'encart en haut à droite de l'écran de pilotage permet de renseigner à quoi sert l'environnement. En pratique, on y renseignera soit son nom, soit la famille de norme que l'on teste, etc. Cela ne change rien au traitement et permet simplement de s'y retrouver. Le tableau de la page d'accueil reflète tous ces champs commentaire.

## Gérer les familles

### `viewFamilleNorme`

***"Liste des familles de normes"*** : ce tableau liste les familles de norme renseignées dans ARC.

La famille de norme représente le métamodèle de la sortie, c'est à dire le format que doivent prendre les données statistiques en sortie d'ARC. Définir une famille de norme, cela permet d'établir un modèle stable et comparable dans le temps, là où le format des données en entrée peut légèrement évoluer d'un millésime à l'autre.

Une famille de norme définit les tables, les liens entre tables et les variables des données statistiques en sortie. Cliquer sur la coche d'une famille fera donc apparaître ses tables, ses variables et ses clients (les applications pouvant décharger les données de cette famille).

Pour ajouter une nouvelle famille de norme, il faut soit la créer à la main, soit importer une famille précédemment téléchargée, soit charger un modèle DDI issu de Collectica. Par sécurité, une famille de norme ne peut pas être supprimée si elle a au moins une application cliente.

---

### `viewClient`

***"Applications clientes"*** : ce tableau liste les applications clientes de la famille de norme sélectionnée.

Le client doit être défini dans ARC pour que l'application cliente aie le droit de récupérer les données. Un même client ne peut récupérer qu'une seule fois une sortie d'ARC.

---

### `viewTableMetier`

***"Tables métier"*** : ce tableau liste les tables qui composent la famille de norme sélectionnée.

Ajouter une nouvelle table dans ce tableau met à jour le tableau des variables métier avec une nouvelle colonne correspondant à la table ajoutée.

#### Convention de nommage

Le nom des tables créées dans la famille de norme doivent suivre les règles suivantes :

- le nom doit avoir la forme suivante : `mapping_<nom de la famille de norme>_<nom de la table>_ok`
- le nom doit être en minuscule, même si la famille de norme est en majuscule
- le nom de table ne doit pas comporter d'accuentuation ni d'espace

Exemple : je souhaite créer une famille de norme INSEE avec une table établissement. Le nom de ma table dans ARC sera :

- **mapping_insee_etablissement_ok**
- ~~mapping_INSEE_etablissement_ok~~
- ~~mapping_insee_établissement_ok~~
- ~~mapping_insee etablissement_ok~~

---

### `viewVariableMetier`

***"Variables métier"*** : ce tableau liste les variables qui composent la famille de norme sélectionnée.

À chaque variable est associée son type, un commentaire et la ou les tables auxquelles elle appartient (le type de consolidation n'est utilisé que par la DSN).

Il y a une colonne par table dans la famille de norme. Pour indiquer qu'une variable appartient à une certaine table, il faut marquer d'un "x" la colonne correspondante (en pratique, n'importe quel symbole fonctionne). Par exemple :

|  Nom de variable  | ... | etablissement | agent |
|:------------------|:---:|:--------------|:------|
| id_agent          |     |               | x     |
| id_etablissement  |     | x             | x     |
| id_source         |     | x             | x     |
| nom_agent         |     |               | x     |
| nom_etablissement |     | x             |       |
| prenom_agent      |     |               | x     |

#### Variables techniques nécessaires à ARC

Pour qu'ARC puisse fonctionner, chacune des tables doit comporter a minima les 2 variables suivantes :

- **id_source** : cette variable reprend le nom du fichier en entrée d'ARC.
- **id_\<nom de la table>** : cette variable sert de clé primaire à la table.

Si le modèle est hiérarchique, ces 2 variables servent de clé de jointure entre la table père et table fille. Il faudra donc que la clé primaire de la table mère soit dans la table fille (par exemple, dans l'exemple précédent, id_etablissement est dans la table agent).

## Gérer les normes

### `viewNorme`

***"Liste des normes"*** : ce tableau liste les normes renseignées dans ARC.

La norme représente le métamodèle de l'entrée, ou plutôt consiste en un ensemble de règles permettant de reconnaître le modèle du fichier en entrée : c'est en quelque sorte sa "carte d'identité". La norme ne contient pas les règles de traitement du fichier, mais permet de lier un fichier au jeu de règles qui doit lui être appliqué.

Une norme dans ARC est définie par sa **famille de norme**, son **nom** et sa **périodicité**, peut être activée ou désactivée, et contient ses règles d'identification : une pour la norme et une pour la validité.

- La règle de **calcul de la norme** permet de définir à quels fichiers cette norme doit être attribuée.
- La règle de **calcul de la validité** permet d'extraire d'un fichier sa date de validité, qui est ainsi comparée à un calendrier de la norme pour déterminer quelle version de la norme utiliser.

Pour qu'une norme soit créée, il est nécessaire que l'ensemble de ces informations soient remplies, sinon ARC n'accepte pas la norme.

#### Famille de norme

Dans la zone de saisie, la colonne Famille de norme propose un menu déroulant permettant de choisir parmi les familles de normes créées celle sur laquelle on souhaite faire une norme. Il suffit de sélectionner celle qui nous intéresse.

#### Norme

Il s'agit ici de donner un nom à la norme.
Une norme est associée à une instance du fichier. Si le fichier évolue, il sera nécessaire de créer une nouvelle norme. Aussi, on nommera la norme avec un nom explicite, comme [nom de la famille]\_[millésime] mais, en pratique, ARC accepte n'importe quel nom.

Attention, il est important de tout de même mettre quelque chose de distinctif, comme le nom de la famille, dans le nom de la norme. Le doublet norme et périodicité sert de clé primaire à cette table, et donc on ne peut pas donner le même nom et périodicité à plusieurs normes, même si elles sont rattachées à des familles différentes.

#### Périodicité

La zone de saisie propose ici un menu déroulant avec 2 choix : **MENSUEL** et **ANNUEL**. En pratique, cela ne modifie pas le comportement d'ARC, le paramètre est purement informatif. Il est toutefois préférable de renseigner si le fichier est reçu mensuellement ou annuellement.

#### Calcul de la norme

Ici, il s'agit de donner à ARC les éléments lui permettant d'affecter la norme au fichier reçu. Cela passe par une requête SQL. ARC met en place une table temporaire à la réception d'un fichier qui se nomme **alias_table** et qui permet de créer la variable id_source, variable contenant le nom du fichier chargé. **alias_table** comporte 3 colonnes :

- **id_source** : le nom du fichier
- **id** : le numéro de ligne
- **ligne** : la ligne du fichier lu par ARC

Par exemple, pour un fichier XML `insee.xml` :
| id_source         |id | ligne                                             |
|-------------------|---|---------------------------------------------------|
| default_insee.xml | 1 | <?xml version="1.0" encoding="UTF-8"?>            |
| default_insee.xml | 2 |\<Insee>                                           |
| default_insee.xml | 3 |\<Etablissement>                                   |
| default_insee.xml | 4 |\<Nom_Etablissement>DR Bretagne</Nom_Etablissement>|
| ...               |...| ...                                               |

La requête doit donc prendre la forme :

`select 1 from alias_table where [condition]`

Cette condition doit être suffisante pour repérer le fichier mais pas trop permissive si l'instance d'ARC est conçue pour charger plusieurs types de fichiers. En effet, si un fichier peut être repéré par plus d'une norme, le chargement du fichier plantera.

Le cas le plus simple et le plus courant est une condition sur le nom du fichier. Par exemple, tous les fichiers contenant "insee" dans le nom doivent être associés à la norme insee. La requête est alors :

`select 1 from alias_table where id_source like '%insee%'`

L'attribution de la norme s'effectue au début de la phase de chargement, c'est à ce moment-là que toutes les requêtes de calcul de la norme sont mobilisées.

#### Calcul de la validité

ARC utilise un système de calendrier pour savoir si la norme est actuelle ou non. Cela permet à ARC d'utiliser ou non cette norme. Cela offre donc une historicisation des normes, le but étant de rejouer des fichiers passés.

Comme pour le calcul de la norme, on définira une requête SQL qui permet de récupérer une date de fichier. La récupération de cette date peut être faite dans le nom du fichier ou autrement, en fonction du fichier reçu. La table **alias_table** peut encore une fois être mobilisée.

Si le système de calendrier n'est pas utilisé, une date fixe suffit (`select 2023-01-01` par exemple).

#### État

L'état permet de dire à ARC s'il peut utiliser la norme ou non. C'est utile lorsque l'on veut faire des tests. On peut vouloir désactiver une norme pour en créer une seconde sans supprimer la première.

La zone de saisie propose un menu déroulant avec donc 2 choix : **ACTIF** ou **INACTIF**.

---

### `viewCalendrier`

***"Calendrier des normes"*** : ce tableau liste les périodes de validité créées pour la norme sélectionnée.

Le calendrier de la norme correspond aux dates sur lesquels nous souhaitons voir la norme en question s'exécuter. Sur le bloc calendrier de la norme, 3 zones de saisie apparaissent : **début de validité**, **fin de validité** et **état**.

Ici, ARC va mettre en regard les dates de début de validité et de fin de validité avec le champ Calcul de la validité renseigné précédemment. Si la date fournie par ce dernier est compris dans l'intervalle de validité du calendrier, alors ARC considérera que c'est cette norme qu'il faut appliquer.

Attention, pour un même fichier, si plusieurs normes existent, il est nécessaire que les calendriers ne se chevauchent pas, sinon ARC ne pourra pas attribuer de norme au fichier.

Noter que les dates renseignées sont au format DATE de PostgreSQL, à savoir AAAA-MM-JJ.

---

### `viewJeuxDeRegles`

***"Jeux de règle"*** : ce tableau liste les différentes versions du jeu de règle créées pour la norme et la période de validité sélectionnées.

Un jeu de règle est associé à une norme et une période de validité. Cet ensemble de règles écrites par le statisticien détermine comment le fichier va être traité par ARC. Il est possible de définir plusieurs versions d'un jeu de règle, notamment pour garder des versions stables sur lesquelles revenir en cas d'erreur dans une version future.

Une fois le jeu de règle défini, il est nécessaire d'indiquer sur quel bac à sable le charger pour qu'il puisse être appliqué. Il ne peut y avoir qu'une version du jeu de règle activée par bac à sable. Comme les normes et les périodes de validité, les versions peuvent être désactivées en sélectionnant l'état **INACTIF** à la place d'un bac à sable.

---

### `viewChargement`

***"Règles de chargement"*** : ce tableau liste les règles de chargement pour le jeu de règle sélectionné.

La phase de chargement est la phase d'intégration des fichiers à la "machine" ARC. Le fichier est lu selon le format indiqué : fichier XML, plat, etc. En sortie de chargement, les tables obtenues après lecture gagnent des champs nécessaires aux traitements suivants :

- **id_source** : nom du fichier source
- **id** : id global, numéro de ligne
- **date_integration** : date du chargement
- **id_norme**, **periodicite**, **validite** : champs liés à la norme

Pour définir une règle de chargement, différents champs sont disponibles :

- **id** : ici, on peut ne rien saisir, ARC attribuera l'ID du chargement automatiquement.
- **Type de fichier** : ici s'ouvre un menu déroulant permettant de choisir le type de fichier que l'on reçoit. 4 valeurs sont possibles : xml, clé-valeur, plat et xml complexe.
- **Délimiteur** : permet de déclarer quel délimiteur est utilisé pour les fichiers plats.
- **Règles de formatage** : on saisira ici l'ensemble des règles permettant la lecture des données. Cet élément sera détaillé ci-dessous.
- **Commentaire** : on pourra saisir ici des éléments permettant de comprendre les règles de formatage par exemple.

#### Règles de formatage

Pour que ARC puisse lire et mettre en forme les données en entrée de processus, on lui indiquera un ensemble d'éléments qui lui est nécessaire. En fonction du fichier, on pourra :

- Déclarer l'encoding du fichier
- Créer des variables
- Faire la jointure entre les données et des nomenclatures
- Générer de nouvelles variables
- Indexer les données

Par exemple :

```
<encoding>UTF8</encoding>
<quote>E'\2'</quote>
<headers>col</headers>
<join-table>mapping_esane_entete_ok
<join-type>inner join
<join-select>*
<join-clause>substring(v_col,1,24)=v_depot_entete and substr(substr(id_source,strpos(id_source,'_')+1),1,5)=substr(substr(v_id_source,strpos(v_id_source,'_')+1),1,5) and substr(substr(id_source,strpos(id_source,'_')+1),7)=substr(substr(v_id_source,strpos(v_id_source,'_')+1),7)
<join-table>arc.nmcl_corresp_v004
<join-type>inner join
<join-select>id_insee,regime,nref,formulaire,format_dgfip,ordre_doublon
<join-clause>v_regime_entete=v_regime and substring(v_col,58,6)=v_nref and trim(substring(v_col,25,20))=v_formulaire
<partition-expression>v_cle_entete
i_col=#pn#+dense_rank() over (order by v_cle_entete)
v_col=rtrim(v_col)
i_choice_fiscal=null::int
v_choice_fiscal=row_number() over (partition by v_cle_entete,v_id_insee order by v_ordre_doublon)
<where>v_choice_fiscal$new$=1
```

#### Description du processus de chargement

À l'étape de chargement, ARC structure l'information contenue dans les fichiers de façon hiérarchique, c'est à dire que pour chaque variable, il va définir d'une part sa position hiérarchique, et d'autre part sa valeur.

##### Exemple

Prenons un extrait d'un fichier exemple.
  
```
<Etablissement>  
    <Nom Etablissement>DR Bretagne<\Nom Etablissement>  
    <Individu>  
        <Nom>Tortosa<\Nom>  
        <Prénom>Thomas<\Prénom>  
    <\Individu>  
<\Etablissement>  
```

Ici, nous avons 3 variables : Nom Etablissement, Nom et Prénom

Toutefois, ces 3 variables ne concernent pas le même noeud. Nom Etablissement concerne Etablissement, tandis que Nom et Prénom concerne Individu. Dans l'arborescence XML, on dira que Etablissement est parent de Individu, avec une relation 1-* puisque qu'un établissement peut être composé de un à plusieurs individus.

**Comment ARC stocke-t-il cette information?** ARC fonctionne sur un système de clé-valeur. Concrètement, si l'on reprend l'exemple ci-dessus, ARC chargera les données ainsi :
| i_nom_etablissement | v_nom_etablissement | i_nom | v_nom | i_pr_nom | v_pr_nom |
| ------- | ------- | ------- | ------- | ------- | ------- |
|    1    | DR Bretagne |     1      |    Tortosa     |    1     |    Thomas     |
|    2    |    DR Centre Val de Loire     |    2    | Manelphe |   2     |  Léo  |
|    2    |           |    3    | Soulier |   3     |  Manuel  |

Que constate-t-on?

- ARC crée une ligne par enregistrement.
- ARC crée 2 types de variables, une préfixée i (clé) qui indice le noeud et une préfixée v qui donne la valeur.
- Les noeuds enfants récupèrent la valeur d'indice des objets parents afin de pouvoir les rattacher.
- Lorsque qu'ARC récupère les noms de balise pour créer les variables, il les transforme en minuscule et remplace les accents par des \_. Cela a une importance lorsque l'on sera à l'étape du mapping.

#### Spécificités du chargement des fichiers délimités

- Il faut bien définir le délimiteur dans la colonne dédiée.
- En cas de souci au chargement, définir l'encoding du fichier via les balises `<encoding> </encoding>`.
- Si le fichier ne contient pas de nom de colonne sur la première ligne, les définir via la balise `<headers> </headers>`.

#### Spécificités du chargement des fichiers positionnels

- La lecture du fichier se fait ligne à ligne. Le délimiteur pour ces fichiers est `E'\1'`, ce qui permet de récupérer la ligne dans une variable.
- Dans la partie Règles de formatage, définir un header pour la variable constituée via les balises `<headers> </headers>`.
- Définir aussi les quotes via `E'\2'` afin d'éviter la perte des apostrophes présentes dans le fichier.
- Pour les fichiers positionnels complexes ou hiérarchiques, définir une variable qui permet de récupérer l'information 'hiérarchique' de la ligne

---

### `viewNormage`

***"Règles de structuration XML"*** : ce tableau liste les règles de normage XML pour le jeu de règle sélectionné.

La phase de normage est la phase de structuration XML du fichier, c'est à dire de mise en forme des données en sortie du chargement. Cette phase n'est pertinente que si vous chargez des fichiers XML et ne concerne donc pas les autres types de chargement.

En fonction du fichier XML traité, il peut y avoir des relations entre noeuds frères et il est donc nécessaire de déclarer ces relations à ARC, ceci afin que les informations soient correctement reliées entre elles. Cela passe par la définition d'un ensemble de règles de structuration. Il en existe plusieurs types :

- **Relation** : spécifier que deux colonnes ont un lien
- **Cartésien** : spécifier que deux colonnes (ou blocs) ne sont pas indépendants
- **Unicité** : ne garder que les valeurs uniques d'une colonne
- **Partition** : partitionner selon les valeurs d'une colonne pour limiter la taille des requêtes
- **Indépendance** : spécifier l'indépendance de deux colonnes (ou blocs)

---

### `viewControle`

***"Règles de contrôle"*** : ce tableau liste les règles de contrôle pour le jeu de règle sélectionné.

La phase de contrôle permet de filtrer et/ou de rejeter certains fichiers ou lignes selon les informations contenues dedans. En effet, on peut vouloir n'intégrer que tout ou partie de l'information. Par exemple, si le fichier contient des lignes non conformes à l'attendu, on peut vouloir ne pas les intégrer dans la table finale. De même, si on s'attend à recevoir un fichier contenant certaines informations mais qu'elles n'y sont pas, on peut vouloir ne pas intégrer le fichier.

#### Type de contrôle

Il existe plusieurs types de contrôle :

- **Cardinalité** : contrôle sur les relations entre objets
- **Alphanumérique** : contrôle sur la longueur
- **Numérique** : contrôle sur si les valeurs sont numériques et leur longueur
- **Date** : contrôle sur le format de date
- **Condition** : contrôle suivant une condition SQL définie
- **Regexp** : contrôle suivant une expression régulière
- **Énumération (brute/de table)** : contrôle suivant une liste de valeurs possibles
- **Structure** : contrôle sur la structure XML globale du fichier

#### Rubrique et Rubrique enfant

La ou les colonnes concernées par le contrôle. N'oubliez pas le préfixe i\_ pour un index ou v\_ pour une valeur.

#### Seuil bloquant

Cette option permet d'exclure tout le fichier, lignes valides comprises, si le nombre de lignes en erreur dépasse le seuil défini. Les lignes en erreur sont tout de même exclues si le seuil n'est pas dépassé. Le seuil peut être défini par nombre d'unités (**u**) ou pourcentage (**%**). Par exemple :

- champ vide : le fichier n'est jamais exclu
- **>0u** : le fichier entier est exclu à la moindre ligne en erreur
- **>=10%** : le fichier entier est exclu si 10% ou plus de lignes sont en erreur

Un fichier avec certaines lignes exclues sera noté dans un état **OK / KO** dans un bac à sable de test.

#### Traitement des lignes d'erreur

Une règle de contrôle peut soit **exclure** ou **conserver** les lignes qu'elle détecte en erreur. Dans les deux cas, les lignes en erreur sont notées dans le rapport de traitement.

#### Prétraitement SQL

Cette requête SQL est jouée avant le contrôle, ce qui peut servir dans de nombreux scénarios, notamment de reformatage : changement de format de date, ajout de 0 devant un numéro de département, modification d'une énumération, etc.

#### Exemples d'utilisation des règles de contrôle

##### Contrôle des formats de date

Un problème très fréquent dans la gestion de différents fichiers de données est la conciliation de différents formats de date. Il est possible dans ARC de reformater des dates en un format commun en utilisant la règle de prétraitement d'une règle de contrôle :

- Type de contrôle : **ALPHANUM**
- Rubrique : nom de la colonne contenant la date (ne pas oublier v_)
- Traitement des lignes d'erreur : **exclure**
- Prétraitement SQL : une requête permettant de reformater la date, un exemple est donné ci-dessous
- Autres champs laissés vides

Par exemple, si l'on souhaite que toutes nos dates soient au format YYYY-MM-DD et que l'on sait que certaines dates sont au format DDMMYYYY, on écrira cette requête de prétraitement :

```sql
case
    when arc.isdate({v_colonne_date},'DDMMYYYY') is true then
        substr({v_colonne_date},5,4)||'-'||
        substr({v_colonne_date},3,2)||'-'||
        substr({v_colonne_date},1,2)
end
```

On utilise la fonction **arc.isdate** qui est une fonction de ARC permettant de reconnaître le format de date à modifier, pour ensuite réarranger les dates vers le format souhaité.

##### Règles de filtrage

Avant la version 93 de ARC, il existait une phase distincte de filtrage, qui permettait d'exclure des lignes ou des tables suivant un certain critère sur les données. Pour simplifier le pipeline, cette phase a été supprimée, mais il reste possible d'écrire des règles équivalentes à un filtrage en phase de contrôle :

- Type de contrôle : **CONDITION**. Le filtre sera une condition écrite en SQL.
- Traitement des lignes d'erreur : **exclure**
- Seuil bloquant : au choix 
- Condition SQL : une condition SQL (donc uniquement ce qui viendrait après une clause WHERE) portant sur les lignes à **garder** dans vos données. Par exemple, pour un simple filtre sur la valeur d'une colonne : `{v_nom_etablissement}='DR Bretagne'`.
- Laissez les autres champs vides.

---

### `viewMapping`

***"Règles de mapping"*** : ce tableau liste les règles de mapping pour le jeu de règle sélectionné.

La phase de mapping fait le lien entre les données en sortie du contrôle et le métamodèle de sortie. Elle donne à ARC les éléments de lecture du fichier pour le remplissage des variables définies dans la famille de norme. Chaque variable définie dans la famille de norme est ainsi créée par une expression SQL sur les données après contrôle, en précisant son type et sa table de sortie.

À la création de la norme, l'onglet mapping est vide. Pour initialiser le mapping, il est nécessaire de cliquer sur le bouton "Pré-générer les règles". Une fois cela fait, l'ensemble des variables définies dans la famille de norme rattachée à la norme apparaît. Il ne reste plus qu'à saisir dans la colonne "Expression SQL" les requêtes SQL qui permettront de récupérer l'information.

#### Mapping des fichiers XML

Pour les fichiers XML, ARC récupère le nom des balises pour nommer leur contenu dans une variable v_[nom de la balise]. Attention, les balises XML peuvent être associées à un namespace. Dans ce cas, ARC ne retient pas le namespace et il faudra donc ne pas le saisir.

Une syntaxe particulière à ARC doit être utilisée. Le nom de la variable v_[nom de la balise] doit être incluse entre {} ou entre ##. Par exemple, la variable v_nom doit être saisie dans la colonne "expression SQL" ainsi : {v_nom}, #v_nom# ou v_nom.

Quelles différences entre ces 3 syntaxes :

- si la variable est comprise entre {}, alors v_nom est utilisé pour remplir le champ et est null si la variable n'existe pas, et i_nom devient une clé de la table ;
- si la variable est comprise entre ##, alors v_nom est utilisé pour remplir le champ et est null si la variable n'existe pas, et i_nom ne devient pas une clé de la table, c'est à dire que les autres variables définissent la clé de table;
- si la variable n'est pas comprise entre # ou {, alors elle est obligatoire dans le fichier et i_nom ne devient pas une clé de table.

Si dans une table du mapping, toutes les variables comprises entre {} ou ## sont nulles, alors la ligne n'est pas créée.

Ici, on récupère la valeur tel qu'elle est dans le fichier. On a bien la variable nom en sortie de processus qui vaut la valeur de nom dans le fichier en entrée de processus.

On peut vouloir créer de nouvelles variables à partir des variables en entrée de processus. Par exemple, si dans mon fichier initial, j'ai une variable civilite qui informe sur la civilité des individus (codé 'M' pour Monsieur et 'MME' pour Madame). Je souhaite disposer dans ma table en sortie d'une variable sexe (codé 1 pour Homme et 2 pour Femme).

Dans ce cas, on pourra écrire l'expression SQL suivant pour "mapper" la variable sexe :
`CASE {v_civilite} WHEN 'M' THEN '1' WHEN 'MME' THEN '2' END`

#### Spécificité du mapping des fichiers délimités

Pour les fichiers délimités, il faut gérer les formats de variable, notamment pour les dates, car les variables des fichiers CSV sont souvent non formatées.

#### Spécificité du mapping des fichiers positionnels

Pour les fichiers positionnels, l'ensemble des variables est contenu dans une chaine de caractère. Il faut donc utiliser des fonctions SQL pour extraire les données.

Quelques fonctions utiles:
- **substr** : permet d'extraire de la chaine de caractère la valeur d'une variable
- **trim** : permet de supprimer les blancs en début ou fin de chaine
- **nullif** : permet de mettre à null une valeur en fonction de la valeur d'une chaine
- **to_date** : permet de formater une valeur en date

##### Pour les fichiers positionnels complexes ou hiérarchiques

En complément de l'information hiérarchique, il faut utiliser la fonction interne d'ARC **ArcProcessingTable** qui permet de pointer la table ARC utile, ceci afin d'éviter la création de lignes vides.

**ArcProcessingTable** est le nom de la table sur laquelle travaille le mapping. Cela permet de créer des règles permettant de calculer une variable différemment selon la table contenant la variable. Par exemple :

```sql
case
    when ArcProcessingTable='mapping_majic_bati_00_ok'
        and #v_article#='00' then #i_00#
    when ArcProcessingTable='mapping_majic_bati_10_ok'
        and #v_article#='10' then #i_00#
end
```

---

### `viewExpression`

***"Expressions"*** : ce tableau liste les expressions définies pour le jeu de règle sélectionné.

Les expressions permettent de réutiliser dans le mapping des chaînes de caractères prédéfinies (par exemple, un format de date récurrent).

Cette fonctionnalité n'est pas utilisée dans les différentes applications qui utilisent ARC.

---

### `viewJeuxDeReglesCopie`

***"Jeu de règle à copier"*** : ce tableau liste tous les jeux de règles existants, avec leur norme, périodicité, validités, version et bac à sable.

En cochant un jeu de règle puis en cliquant sur "Copier", ce jeu de règle vient remplacer le jeu de règle sélectionné au-dessus, uniquement pour la phase sélectionnée.

## Gérer les nomenclatures

### `viewListNomenclatures`

***"Liste des tables externes"*** : ce tableau liste les nomenclatures renseignées dans ARC.

Le nom de la table doit être obligatoirement précédé de `nmcl_`. Pour importer une nomenclature, il faut au préalable ajouter et sélectionner son nom dans ce tableau. Le nom du fichier importé n'est pas utilisé.

---

### `viewSchemaNmcl`

***"Schéma de la table externe"*** : ce tableau détaille les colonnes et leur types pour la nomenclature sélectionée.

Le schéma est imputé de la table de nomenclature consultée, il ne peut pas être modifié dans ARC.

---

### `viewNomenclature`

***"Contenu de la table externe"*** : ce tableau détaille les éléments de la nomenclature sélectionnée.

Le contenu des nomenclatures est seulement consulté, il ne peut pas être modifié dans ARC.

## Gérer les webservices

### `viewWebserviceContext`

***"Définition des webservices"*** : ce tableau liste les webservices renseignés dans ARC.

---

### `viewWebserviceQuery`

***"Requête des webservices"*** : ce tableau détaille les requêtes du webservice sélectionné et l'expression SQL permettant d'y répondre.

## Gérer les entrepôts

### `viewEntrepot`

***"Liste des entrepôts"*** : ce tableau liste les entrepôts renseignés dans ARC.

Dans le système de fichiers sur lequel il pointe, ARC crée un dossier par bac à sable utilisé. Dans chacun de ces dossiers, un ensemble de dossiers préfixés "RECEPTION_" est généré. Ce sont ces dossiers-là que ARC utilise pour la phase de réception.

C'est ici que le choix d'entrepôt entre en jeu. Lorsque la phase de réception est lancée, ARC lit dans les dossiers RECEPTION_[ENTREPÔT] pour les fichiers à réceptionner. Les fichiers chargés sont alors préfixés du nom de leur entrepôt pour la suite du traitement. L'entrepôt par défaut s'appelle DEFAULT.

À la réception, les fichiers sont copiés dans [ENTREPÔT]_ARCHIVE pour archivage, et déplacés dans ENCOURS le temps du traitement, puis OK ou KO selon l'état final.

Il y a deux moyens de prioriser les traitements de ARC, et cela passe par l'écran des entrepôts :

- La colonne **Priorité de l'entrepôt** permet d'ordonner les entrepôts pour choisir l'ordre dans lequel ils sont traités. Les entrepôts seront traités par ordre croissant des nombres renseignés.
- La colonne **Règle de priorité des fichiers** permet d'ordonner les archives au sein même d'un entrepôt pour choisir l'ordre dans lequel elles sont traitées. Cette règle s'écrit comme la clause suivant un ORDER BY dans une requête SQL. On pourra utiliser les variables suivantes :
  - `archive_name` pour le nom des archives
  - `archive_date` pour la date d'arrivée des archives
  - `archive_size` pour la taille des archives
Par défaut, les archives sont traitées par ordre alphabétique des noms (`archive_name ASC`).

## Piloter l'environnement

### `viewPilotageBAS`

***"État des fichiers dans le traitement"*** : ce tableau détaille le nombre de fichiers par phase de traitement pour chaque réception de fichiers dans ARC.

Les cases correspondant au nombre de fichiers par phase sont cliquables et permettent d'ouvrir le détail des fichiers dans cette phase pour ce chargement.

---

### `viewRapportBAS`

***"Rapport de traitement"*** : ce tableau détaille tous les rapports d'erreur des fichiers du bac à sable.

Les rapports d'erreur sont sous forme d'exceptions Java, et concernent aussi bien les fichiers en échec après une phase (état KO) que les lignes en erreur d'une règle de contrôle.

---

### `viewFichierBAS`

***"Détail des fichiers traités"*** : ce tableau détaille, pour un paquet de fichiers sélectionné, la liste de tous les fichiers ayant passé par la phase et l'état donné.

Il est possible d'exécuter un module de traitement (ou son retour arrière) sur une sélection de fichiers uniquement, en les sélectionnant depuis ce tableau.

---

### `viewArchiveBAS`

***"Contenu du répertoire d'archive"*** : ce tableau montre les archives contenues dans le dépôt donné.

## Exporter des données

### `viewExport`

***"Définition des exports"*** : ce tableau liste les exports définis pour les tables du bac à sable sélectionné.

---

### `viewFileExport`

***"Fichiers exportés"*** : ce tableau liste les exports effectués et prêts à être téléchargés pour le bac à sable sélectionné.

## Paramétrer l'application

### `viewParameters`

***"Paramétrage du fonctionnement de l'application"*** : ce tableau liste les différents paramètres de l'application.

#### Exemples de modifications de paramètres

Dans ce qui suit, pour toute modification de paramètre, n'oubliez pas de cliquer sur "Mettre à jour" en bas du tableau pour que le changement soit pris en compte.

##### Ajouter des bacs à sable

Le paramètre **ApiInitialisationService.nbSandboxes** détermine le nombre de bacs à sable disponibles. Il est possible d'en rajouter en modifiant ce nombre. Pour que ce changement prenne effet, la manipulation est la suivante :
- Modifiez le paramètre **ApiInitialisationService.nbSandboxes** avec le nombre de votre choix
- Laissez à vide le paramètre **git.commit&period;id**
- N'oubliez pas de mettre à jour la table des paramètres
- Allez sur la page d'accueil

Les paramètres **git.commit&period;id** sont les identifiants de version des bases de données de ARC. Supprimer l'identifiant de la base de données globale force ARC à le retrouver et à au passage regénérer cette base de données, donc avec les nouveaux bacs à sable. La regénération est déclenchée en allant sur la page d'accueil.

##### Convertir un environnement de test en prod (et inversement)

Le paramètre **ArcAction.productionEnvironments** détermine quels environnements sont à afficher avec une interface de production. Pour convertir le bac à sable X de test en prod, ajoutez "arc_basX" (entre guillemets) à la liste.

Le paramètre devient par exemple `["arc_prod", "arc_bas1"]`

Pour reconvertir un bac à sable de prod en test, supprimez-le de la liste.
