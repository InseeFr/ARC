package fr.insee.arc.core.famille.model;

public class TableMetier
        implements RegleComparable<TableMetier.CleComparaisonTableMetier> {

    private String idFamille;
    private String nomTableMetier;
    private String descriptionTableMetier;

    @Override
    public CleComparaisonTableMetier getCleComparaison() {
        return new CleComparaisonTableMetier(nomTableMetier);
    }

    @Override
    public Object getContenuComparaison() {
        return new ContenuComparaisonTableMetier();
    }

    public record CleComparaisonTableMetier(
            String nomTableMetier) {
    }

    public record ContenuComparaisonTableMetier() {
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

    public String getDescriptionTableMetier() {
        return descriptionTableMetier;
    }

    public void setDescriptionTableMetier(String descriptionTableMetier) {
        this.descriptionTableMetier = descriptionTableMetier;
    }
}