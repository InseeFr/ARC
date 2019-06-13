package fr.insee.arc_essnet.core.model;

public enum TraitementTableExecution {
	PILOTAGE_FICHIER("PILOTAGE_FICHIER"), NORME("NORME"), CALENDRIER("CALENDRIER"), JEUDEREGLE("JEUDEREGLE"), CONTROLE_REGLE(
			"CONTROLE_REGLE"), FILTRAGE_REGLE("FILTRAGE_REGLE"), MAPPING_REGLE("MAPPING_REGLE"), SEUIL("SEUIL");

	private TraitementTableExecution(String anExpression) {
		this.expression = anExpression;
	}

	private String expression;

	public String toString() {
		return this.expression;
	}
}
