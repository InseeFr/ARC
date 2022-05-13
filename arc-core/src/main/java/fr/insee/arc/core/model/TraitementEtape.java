package fr.insee.arc.core.model;

public enum TraitementEtape {
	E0_PHASE_INTERMEDIAIRE_TERMINE(0)
	, E1_PHASE_EN_COURS(1)
	, E2_PIPELINE_TERMINE(2)
	, E3_PHASE_SUIVANTE_EN_COURS(3)
	;
	
	private TraitementEtape(int anEtape) {
		this.etape = anEtape;
	}

	private int etape;

	public int getEtape() {
		return this.etape;
	}

	
}
