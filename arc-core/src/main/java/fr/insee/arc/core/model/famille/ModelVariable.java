package fr.insee.arc.core.model.famille;

/**
 * Représente une variable dans une table de ARC.
 * 
 * @author Z84H10
 *
 */
public class ModelVariable {

	public ModelVariable(String idFamille, String nomTableMetier, String nomVariableMetier, String typeVariableMetier,
			String descriptionVariableMetier) {
		super();
		this.idFamille = idFamille;
		this.nomTableMetier = nomTableMetier;
		this.nomVariableMetier = nomVariableMetier;
		this.typeVariableMetier = typeVariableMetier;
		this.descriptionVariableMetier = descriptionVariableMetier;
	}

	public ModelVariable() {
	}

	/**
	 * Identifiant nominal de la famille de norme à laquelle appartient la variable.
	 */
    private String idFamille;
    /**
     * Nom métier de la table à laquelle appartient la variable.
     */
    private String nomTableMetier;
    /**
     * Nom métier de la variable.
     */
    private String nomVariableMetier;
    /**
     * Type de la variable. Type {@code text} par défaut et si non renseigné.
     */
    private String typeVariableMetier;
    /**
     * Description et particularités de la variable.
     */
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