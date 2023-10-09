package fr.insee.arc.core.model;

/**
 * Liste des entrepots de donnés Pour l'instant cela ne représente juste qu'un
 * répertoire ou arrive les données
 */
public enum DataWarehouse {

	// nom de l'entrepot par défaut
	DEFAULT("DEFAULT");

	private DataWarehouse(String name) {
		this.name = name;
	}

	private String name;

	public String getName() {
		return name;
	}

}
