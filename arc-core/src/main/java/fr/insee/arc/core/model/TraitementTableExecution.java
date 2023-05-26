package fr.insee.arc.core.model;

public enum TraitementTableExecution {
	PILOTAGE_FICHIER("PILOTAGE_FICHIER"), NORME("NORME"), CALENDRIER("CALENDRIER"), JEUDEREGLE("JEUDEREGLE"), CONTROLE_REGLE(
			"CONTROLE_REGLE"), MAPPING_REGLE("MAPPING_REGLE"), SEUIL("SEUIL");

	private TraitementTableExecution(String anExpression) {
		this.expression = anExpression;
	}

	private String expression;

	@Override
	public String toString() {
		return this.expression;
	}
}
