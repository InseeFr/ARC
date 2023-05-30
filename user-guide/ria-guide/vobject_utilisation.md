# Utilisation de VObject

Un **VObject** est un objet à inclure dans une jsp qui permet d'afficher sur une page web, un tableau de valeurs dynamique et éditable.

### Pré-requis du projet

- Copier `templateVObject.jsp` dans le répertoire jsp du projet
- Copier `component.js` dans le répertoire js du projet
- Disposer de Bootstrap dans le projet

### Préparer la vue

En se conformant au patron MVC, la page XXX, pour afficher la vue YYY, doit disposer des couches suivantes : 
- la couche **modèle** (model), avec
    - une classe composant **ModelXXX**, contenant toutes les vues de la page XXX,
    - pour chaque vue une classe **ViewYYY** ;
- la couche **service** (service), avec
    - une classe service **InteractorXXX** ou **XXXAction** pour initialiser la page et les VObjects,
    - pour chaque vue une classe **ServiceViewYYY**, devant contenir si nécessaire les actions de base du VObject : selectYYY, sortYYY, addYYY, updateYYY, deleteYYY ;
- la couche **contrôleur** (controller), avec pour chaque vue une classe contrôleur **ControllerViewYYY** liant les requêtes Web aux actions du service.

### Inclure le VObject dans la page

Dans le fichier jsp, à l'endroit où inclure le VObject, écrire : 

```htmlembedded=
<c:set var="view" value="${viewXXX}" scope="request"/>
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
- **btnAdd** pour afficher le bouton d'ajout d'entrée
- **btnUpdate** pour afficher le bouton de mise à jour, qui permet de modifier le tableau directement et d'enregistrer les modifications
- **btnDelete** pour afficher le bouton de suppression d'entrée (**checkbox** doit être activé pour sélectionner l'entrée à supprimer)
- **ligneAdd** pour afficher la ligne d'ajout d'entrée en bas du tableau
- **ligneFilter** pour afficher la ligne de filtrage des entrées en haut du tableau
- **checkbox** pour afficher les cases à cocher à gauche de chaque entrée
- **multiSelection** pour autoriser la sélection de plusieurs cases à cocher (inutile si **checkbox** est désactivé)
