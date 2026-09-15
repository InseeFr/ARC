package fr.insee.arc.core.famille.model;

public class Norme
        implements RegleComparable<
        Norme.CleComparaisonNorme> {

    private String idNorme;
    private String periodicite;

    private String defNorme;
    private String defValidite;
    private String etat;
    private String idFamille;

    private Integer id;

    @Override
    public CleComparaisonNorme getCleComparaison() {
        return CleComparaisonNorme.PAS_DE_CRITERE_APPARIEMENT;
    }

    @Override
    public ContenuComparaisonNorme getContenuComparaison() {
        return new ContenuComparaisonNorme(
                defNorme,
                defValidite,
                etat,
                idFamille
        );
    }

    public enum CleComparaisonNorme {
        PAS_DE_CRITERE_APPARIEMENT
    }

    public record ContenuComparaisonNorme(
            String defNorme,
            String defValidite,
            String etat,
            String idFamille
    ) {
    }

    public String getIdNorme() {
        return idNorme;
    }

    public void setIdNorme(String idNorme) {
        this.idNorme = idNorme;
    }

    public String getPeriodicite() {
        return periodicite;
    }

    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }

    public String getDefNorme() {
        return defNorme;
    }

    public void setDefNorme(String defNorme) {
        this.defNorme = defNorme;
    }

    public String getDefValidite() {
        return defValidite;
    }

    public void setDefValidite(String defValidite) {
        this.defValidite = defValidite;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getIdFamille() {
        return idFamille;
    }

    public void setIdFamille(String idFamille) {
        this.idFamille = idFamille;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}