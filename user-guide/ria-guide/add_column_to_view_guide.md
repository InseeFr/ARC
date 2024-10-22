# Ajouter une nouvelle colonne à une vue dans ARC

## Classes à modifier

- ajouter les colonnes à la table dans la BDD (sous forme de ALTER TABLE) via le script global `arc-core/src/main/resources/BdD/script_global.sql`
- ajouter des contraintes SQL sur la nouvelle colonne si besoin via le script function constraint `arc-core/src/main/resources/BdD/script_function_constraint.sql`
- modifier le DAO de sa page pour inclure la nouvelle colonne dans la méthode pour initialize
- ajouter les colonnes au modèle de sa vue (classe View[nom de vue]) en rebalançant les largeurs de colonne
- ajouter les labels de colonne dans `arc-web/src/main/resources/messages_en.properties` et `messages_fr.properties`
- ajouter les nouvelles colonnes dans ColumnEnum `arc-core/src/main/java/fr/insee/arc/core/dataobjects/ColumnEnum.java`
- ajouter les colonnes à la liste des colonnes de la vue dans ViewEnum `arc-core/src/main/java/fr/insee/arc/core/dataobjects/ViewEnum.java`
