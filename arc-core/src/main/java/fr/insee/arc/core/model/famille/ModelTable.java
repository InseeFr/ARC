package fr.insee.arc.core.model.famille;

public class ModelTable {

    private String idFamille;
    private String nomTableMetier;
    private String descriptionTable;

    public void setIdFamille(String idFamille) {
        this.idFamille = idFamille;
    }

    public void setNomTableMetier(String nomTableMetier) {
        this.nomTableMetier = nomTableMetier;
    }

    public void setDescriptionTable(String descriptionTable) {
        this.descriptionTable = descriptionTable;
    }

    @Override
    public String toString() {
        return "ModelTable{" +
                "id_famille='" + idFamille + '\'' +
                ", nom_table_metier='" + nomTableMetier + '\'' +
                ", description_table='" + descriptionTable + '\'' +
                '}';
    }

	public String getIdFamille() {
		return idFamille;
	}

	public String getNomTableMetier() {
		return nomTableMetier;
	}

	public String getDescriptionTable() {
		return descriptionTable;
	}

    
    
    
}