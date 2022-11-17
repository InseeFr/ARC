package fr.insee.arc.core.model.famille;

public class ModelVariable {

    private String idFamille;
    private String nomTableMetier;
    private String nomVariableMetier;
    private String typeVariableMetier;
    private String descriptionVariableMetier;
    private String typeConsolidation;

    public void setIdFamille(String idFamille) {
        this.idFamille = idFamille;
    }

    public void setNomTableMetier(String nomTableMetier) {
        this.nomTableMetier = nomTableMetier;
    }

    public void setNomVariableMetier(String nomVariableMetier) {
        this.nomVariableMetier = nomVariableMetier;
    }

    public void setTypeVariableMetier(String typeVariableMetier) {
        this.typeVariableMetier = typeVariableMetier;
    }

    public void setDescriptionVariableMetier(String descriptionVariableMetier) {
        this.descriptionVariableMetier = descriptionVariableMetier;
    }

    public void setTypeConsolidation(String typeConsolidation) {
        this.typeConsolidation = typeConsolidation;
    }

    @Override
    public String toString() {
        return "ModelVariable{" +
                "id_famille='" + idFamille + '\'' +
                ", nom_table_metier='" + nomTableMetier + '\'' +
                ", nom_variable_metier='" + nomVariableMetier + '\'' +
                ", type_variable_metier='" + typeVariableMetier + '\'' +
                ", description_variable_metier='" + descriptionVariableMetier + '\'' +
                ", type_consolidation='" + typeConsolidation + '\'' +
                '}';
    }

	public String getIdFamille() {
		return idFamille;
	}

	public String getNomTableMetier() {
		return nomTableMetier;
	}

	public String getNomVariableMetier() {
		return nomVariableMetier;
	}

	public String getTypeVariableMetier() {
		return typeVariableMetier;
	}

	public String getDescriptionVariableMetier() {
		return descriptionVariableMetier;
	}

	public String getTypeConsolidation() {
		return typeConsolidation;
	}
    
    
    
    
}