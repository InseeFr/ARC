package fr.insee.arc.core.famille.model;

public class Client implements RegleComparable<Client.CleComparaisonClient> {

    private String idFamille;
    private String idApplication;

    @Override
    public CleComparaisonClient getCleComparaison() {
        return new CleComparaisonClient(idApplication);
    }

    @Override
    public Object getContenuComparaison() {
        return ContenuComparaisonClient.PAS_DE_CONTENU_A_COMPARER;
    }

    public record CleComparaisonClient(
            String idApplication) {
    }

    public enum ContenuComparaisonClient {
        PAS_DE_CONTENU_A_COMPARER
    }

    public String getIdFamille() {
        return idFamille;
    }

    public void setIdFamille(String idFamille) {
        this.idFamille = idFamille;
    }

    public String getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(String idApplication) {
        this.idApplication = idApplication;
    }
}
