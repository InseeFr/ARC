package fr.insee.arc_essnet.core.model;

public enum TraitementState {
	OK(2,2), KO(2,4), OK_KO(2,3), ENCOURS(1,1);
	
	private TraitementState(int anOrdre, int anOrdreAffichage) {
		this.ordre = anOrdre;
		this.ordreAffichage = anOrdreAffichage;
	}

	private int ordre;
	private int ordreAffichage;

	public int getOrdre() {
		return this.ordre;
	}
	
	
	public int getOrdreAffichage() {
		return ordreAffichage;
	}

	
}
