# Interfaces fonctionnelles de ARC

Ce document est une aide pour l'utilisation des interfaces fonctionnelles de ARC.

## Les classes *Throwing*

Une interface fonctionnelle est une interface ne contenant qu'une méthode abstraite. Ces interfaces permettent l'écriture d'expressions lambda. On peut donc faire de la programmation fonctionnelle en manipulant des fonctions comme arguments.

Dans ARC, ces interfaces sont appelées *Throwing* et se trouvent dans arc-utils. Il en existe quatre selon le type d'expression lambda à écrire :
- **ThrowingConsumer** pour un consumer : une entrée T, pas de sortie (méthode **accept**)
- **ThrowingFunction** pour une fonction : une entrée T, une sortie R (méthode **apply**)
- **ThrowingRunnable** pour un lanceur : ni entrée, ni sortie (méthode **run**)
- **ThrowingSupplier** pour un fournisseur : pas d'entrée, une sortie R (méthode **get**)

## Utilisation

Prenons par exemple ThrowingFunction :
```java
@FunctionalInterface
public interface ThrowingFunction<T, R> {
   R apply(T t) throws ArcException;
}
```
ThrowingFunction permet d'implémenter des fonctions "jetables", définies par la transformation qu'elles appliquent à un objet T vers un objet R. On peut alors écrire une simple fonction comme un objet :
```java
private ThrowingFunction<String, String> toMail = s -> s + "@insee.fr";
```
Une utilisation classique des lambdas est d'appliquer une transformation aux éléments d'une liste :
```java
for (Agent a : agents) {
    a.setMail(toMail.apply(a.getName()));
}
```

Une autre utilisation des fonctions lambda est de mutualiser des conditions : c'est ce qui est notamment fait sur BatchARC avec les différents lanceurs.

Au lieu d'avoir
```java
// set batch parameters
if (isProductionOn()) {	
    batchParametersGet();
} else {
    message("La production est arretée !");
}

// prepare batch
if (isProductionOn()) {	
    batchEnvironmentPrepare();
} else {
    message("La production est arretée !");
}

[...]
```

on a
```java
// set batch parameters
executeIfProductionActive(this::batchParametersGet);

// prepare batch
executeIfProductionActive(this::batchEnvironmentPrepare);

[...]
```

avec
```java
private void executeIfProductionActive(ThrowingRunnable method) throws ArcException {
    if (!isProductionOn()) {
		message("La production est arretée !");
		return;
	}
	method.run();
}
```

ce qui permet de répliquer une même condition sur plusieurs actions de manière plus lisible.
