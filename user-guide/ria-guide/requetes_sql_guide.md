# Lancer une requête sur ARC

Ce document est une aide pour le requêtage en SQL de la base de données de ARC.

## Où faire la requête ? Les classes DAO

Il faut, de préférence, requêter la base de données dans une classe prévue à cet effet : une classe DAO (Data Access Object). Bien séparer données et code "métier" est en effet plus maintenable.

Toutes les pages Web et le code core des phases de traitement de ARC ont une ou plusieurs classes DAO. Mais d'autres services de ARC peuvent aussi avoir une classe DAO dédiée. Ne pas hésiter à en créer de nouvelles au besoin.

## Comment construire la requête SQL ? Les classes utilitaires

Pour éviter d'écrire la requête à la main dans une grande chaîne de caractères (plus difficile à maintenir, possibilité d'injection, etc.), ARC dispose d'une série de classes utilitaires permettant de construire des requêtes SQL.

### ArcPreparedStatementBuilder

Cette classe permet de construire notre requête par mots-clés et en utilisant certaines méthodes toutes faites. Par exemple :

```java
ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
query.build("SELECT * FROM arc.table WHERE id='", monId, "'");
```

ArcPreparedStatementBuilder permet notamment d'utiliser les méthodes suivantes :
- **quoteText** : permet d'encapsuler les entrées utilisateur d'une manière qui prévient l'injection SQL. À utiliser lorsqu'une chaîne de caractères de la requête peut être entrée par un utilisateur de l'IHM. Notre exemple devient :
```java
query.build("SELECT * FROM arc.table WHERE id=", query.quoteText(monId));
```
- **sqlListeOfColumnsFromModel** : permet de lister toutes les colonnes d'une vue ViewEnum. Cela a valeur de `*` dans une requête select. Notre exemple devient :
```java
ViewEnum dataModelTable = ViewEnum.TABLE;
query.append("SELECT");
query.append(query.sqlListeOfColumnsFromModel(dataModelTable));
query.append("FROM");
query.append(dataObjectService.getView(dataModelTable));
query.append("WHERE id=");
query.append(query.quoteText(monId));
```

Attention, ArcPreparedStatementBuilder est une classe de arc-core, elle n'est donc pas utilisable dans arc-utils. On utilisera alors GenericPreparedStatementBuilder qui est la classe parent.

### SQL

La classe SQL de ARC est une énumération de mots-clés et symboles SQL (par exemple : SELECT, FROM, GROUP BY, point-virgule, opérateur cast, etc.). Ces mots-clés peuvent être enchaînés dans les statement builder car le toString est fait tel qu'il ajoute des espaces avant et après le mot-clé. Notre exemple devient :
```java
ViewEnum dataModelTable = ViewEnum.TABLE;
query.append(SQL.SELECT);
query.append(query.sqlListeOfColumnsFromModel(dataModelTable));
query.append(SQL.FROM);
query.append(dataObjectService.getView(dataModelTable));
query.append(SQL.WHERE);
query.append("id=" + query.quoteText(monId));
```

### ViewEnum, ColumnEnum, SchemaEnum

Ces énumérations permettent de retrouver les noms des différentes composantes de la base de données de ARC : tables, colonnes et schémas. Ces éléments peuvent directement être référencés dans un ArcPreparedStatementBuilder. Attention pour les vues, on utilisera getFullName pour retourner le nom de la table avec son schéma. Par exemple :
```java
query.build(SQL.SELECT, ColumnEnum.COLONNE, SQL.FROM);
query.build(ViewEnum.TABLE.getFullName(envExecution));
```

Note : getFullName() suffit pour une table du schéma des métadonnées (arc). Pour une table du bac à sable, il est nécessaire de préciser de quel bac à sable il s'agit.

Les tables de ViewEnum sont associées à leurs colonnes et au schéma dans lequel elles se trouvent. On peut utiliser getColumns() et getTableLocation() pour retrouver ces éléments.

Ces trois énumérations contiennent aussi des alias, noms génériques et tables de métadonnées Postgres : ColumnEnum.NB, ColumnEnum.TABLE_NAME, TableEnum.ALIAS_A, TableEnum.PG_TABLES, SchemaEnum.INFORMATION_SCHEMA, etc.

## Comment exécuter la requête ? UtilitaireDao

UtilitaireDao est la classe de arc-utils à utiliser pour exécuter une requête SQL dans la base de données. Cette classe gère les propriétés et le pool de connexions.

Pour exécuter une requête, il faut d'abord faire UtilitaireDao.get(pool) pour récupérer un pool de connexions. pool est un entier. pool = 0 peut généralement suffire, notamment pour les tests, mais attention à toujours utiliser le même pool de connexion si les requêtes doivent se suivre. Certaines classes ont déjà en attribut un connexionIndex ou similaire.

Pour exécuter la requête, on utilise executeRequest(connexion, requete) ou executeImmediate(connexion, requete). executeRequest est à utiliser pour les requêtes SELECT dont on souhaite exploiter la réponse : cette réponse sera encapsulée dans un GenericBean qu'on ira bien souvent parcourir sous forme de map. executeImmediate ne renvoie pas de réponse, mais est plus efficace pour les requêtes sans réponse : CREATE, INSERT, etc.

## Exemple complet

Imaginons que la table TABLE associe un ID unique à sa VALEUR. On souhaite écrire une méthode renvoyant cette valeur pour un id donné en interrogeant la base. On pourra alors écrire :

```java
public String getValeur(String monId) throws ArcException {
    
    ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
    
    query.build(SQL.SELECT, ColumnEnum.VALEUR);
    query.build(SQL.FROM, ViewEnum.TABLE.getFullName(envExecution));
    query.build(SQL.WHERE, ColumnEnum.ID, "=", query.quoteText(monId));
    
    Map<String, List<String>> result = new GenericBean(
        UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex())
        .executeQuery(null, query)).mapContent();
    
    return result.get(ColumnEnum.VALEUR.getColumnName()).get(0);
    
}
```
