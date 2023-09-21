package fr.insee.arc.core.model;

import fr.insee.arc.core.dataobjects.ViewEnum;

public enum TraitementTableParametre {
    CALENDRIER(ViewEnum.IHM_CALENDRIER), //
    NORME(ViewEnum.IHM_NORME), //
    JEUDEREGLE(ViewEnum.IHM_JEUDEREGLE), //
    CHARGEMENT_REGLE(ViewEnum.IHM_CHARGEMENT_REGLE), //
    NORMAGE_REGLE(ViewEnum.IHM_NORMAGE_REGLE), //
    CONTROLE_REGLE(ViewEnum.IHM_CONTROLE_REGLE), //
    MAPPING_REGLE(ViewEnum.IHM_MAPPING_REGLE), //
    EXPRESSION(ViewEnum.IHM_EXPRESSION), //
    MOD_TABLE_METIER(ViewEnum.MOD_TABLE_METIER), //
    MOD_VARIABLE_METIER(ViewEnum.MOD_VARIABLE_METIER);
    
	private TraitementTableParametre(ViewEnum tablename) {
        this.tablename = tablename;
    }

    private ViewEnum tablename;
    
    public boolean isPartOfRuleset() {
    	return this == TraitementTableParametre.CHARGEMENT_REGLE 
        		|| this == TraitementTableParametre.NORMAGE_REGLE 
        		|| this == TraitementTableParametre.CONTROLE_REGLE
                || this == TraitementTableParametre.MAPPING_REGLE 
                || this == TraitementTableParametre.EXPRESSION;
    }

    @Override
    public String toString() {
        return this.tablename.getFullName();
    }
}
