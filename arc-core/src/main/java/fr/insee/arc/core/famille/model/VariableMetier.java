package fr.insee.arc.core.famille.model;

public class VariableMetier
        implements RegleComparable<VariableMetier.CleComparaisonVariableMetier> {

    private String idFamille;
    private String nomTableMetier;
    private String nomVariableMetier;
    private String typeVariableMetier;
    private String descriptionVariableMetier;
    private String typeConsolidation;

    @Override
    public CleComparaisonVariableMetier getCleComparaison() {
        return new CleComparaisonVariableMetier(
                nomTableMetier,
                nomVariableMetier
        );
    }

    @Override
    public Object getContenuComparaison() {
        return new ContenuComparaisonVariableMetier(
                typeVariableMetier,
                typeConsolidation
        );
    }

    public record CleComparaisonVariableMetier(
            String nomTableMetier,
            String nomVariableMetier) {
    }

    public record ContenuComparaisonVariableMetier(
            String typeVariableMetier,
            String typeConsolidation) {
    }

    public String getIdFamille() {
        return idFamille;
    }

    public void setIdFamille(String idFamille) {
        this.idFamille = idFamille;
    }

    public String getNomTableMetier() {
        return nomTableMetier;
    }

    public void setNomTableMetier(String nomTableMetier) {
        this.nomTableMetier = nomTableMetier;
    }

    public String getNomVariableMetier() {
        return nomVariableMetier;
    }

    public void setNomVariableMetier(String nomVariableMetier) {
        this.nomVariableMetier = nomVariableMetier;
    }

    public String getTypeVariableMetier() {
        return typeVariableMetier;
    }

    public void setTypeVariableMetier(String typeVariableMetier) {
        this.typeVariableMetier = typeVariableMetier;
    }

    public String getDescriptionVariableMetier() {
        return descriptionVariableMetier;
    }

    public void setDescriptionVariableMetier(String descriptionVariableMetier) {
        this.descriptionVariableMetier = descriptionVariableMetier;
    }

    public String getTypeConsolidation() {
        return typeConsolidation;
    }

    public void setTypeConsolidation(String typeConsolidation) {
        this.typeConsolidation = typeConsolidation;
    }
}