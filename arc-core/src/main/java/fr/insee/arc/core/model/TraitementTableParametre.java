package fr.insee.arc.core.model;

import fr.insee.arc.core.dataobjects.ViewEnum;

public enum TraitementTableParametre {
    NORME(ViewEnum.IHM_NORME, ViewEnum.NORME), //
    CALENDRIER(ViewEnum.IHM_CALENDRIER, ViewEnum.CALENDRIER), //
    JEUDEREGLE(ViewEnum.IHM_JEUDEREGLE, ViewEnum.JEUDEREGLE), //
    CHARGEMENT_REGLE(ViewEnum.IHM_CHARGEMENT_REGLE, ViewEnum.CHARGEMENT_REGLE), //
    NORMAGE_REGLE(ViewEnum.IHM_NORMAGE_REGLE, ViewEnum.NORMAGE_REGLE), //
    CONTROLE_REGLE(ViewEnum.IHM_CONTROLE_REGLE, ViewEnum.CONTROLE_REGLE), //
    MAPPING_REGLE(ViewEnum.IHM_MAPPING_REGLE, ViewEnum.MAPPING_REGLE), //
    EXPRESSION(ViewEnum.IHM_EXPRESSION, ViewEnum.EXPRESSION), //
    MOD_TABLE_METIER(ViewEnum.IHM_MOD_TABLE_METIER, ViewEnum.MOD_TABLE_METIER), //
    MOD_VARIABLE_METIER(ViewEnum.IHM_MOD_VARIABLE_METIER, ViewEnum.MOD_VARIABLE_METIER);
    
	private TraitementTableParametre(ViewEnum tablenameInMetadata, ViewEnum tablenameInSandbox) {
        this.tablenameInMetadata = tablenameInMetadata;
        this.tablenameInSandbox = tablenameInSandbox;

    }

    private ViewEnum tablenameInMetadata;
    private ViewEnum tablenameInSandbox;
    
    public boolean isPartOfRuleset() {
    	return this == TraitementTableParametre.CHARGEMENT_REGLE 
        		|| this == TraitementTableParametre.NORMAGE_REGLE 
        		|| this == TraitementTableParametre.CONTROLE_REGLE
                || this == TraitementTableParametre.MAPPING_REGLE 
                || this == TraitementTableParametre.EXPRESSION;
    }

	public ViewEnum getTablenameInMetadata() {
		return tablenameInMetadata;
	}

	public ViewEnum getTablenameInSandbox() {
		return tablenameInSandbox;
	}

    
}
