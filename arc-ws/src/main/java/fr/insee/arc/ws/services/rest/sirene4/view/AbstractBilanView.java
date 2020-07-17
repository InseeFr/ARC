package fr.insee.arc.ws.services.rest.sirene4.view;

import java.util.List;

public abstract class AbstractBilanView {

    private String conformite;

    private String etat;

    private int nbAnomalieTotal;

    private int nbAnomalieBloquant;

    public AbstractBilanView(List<AnomalieView> anomalies) {
        super();

        // 1 / 4
        nbAnomalieTotal = anomalies.size();

        // 2 / 4
        conformite = anomalies.isEmpty() ? "OK" : "KO";

        // 3 / 4
        nbAnomalieBloquant = 0;
        for (final AnomalieView anomalie : anomalies) {
            if (anomalie.getCategorie() == EnumCategorie.BLOQUANT) {
                nbAnomalieBloquant++;
            }
        }

        // 4 / 4
        etat = nbAnomalieBloquant == 0 ? "accepte" : "refuse";

    }

    public String getConformite() {
        return conformite;
    }

    public void setConformite(String conformite) {
        this.conformite = conformite;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public int getNbAnomalieTotal() {
        return nbAnomalieTotal;
    }

    public void setNbAnomalieTotal(int nbAnomalieTotal) {
        this.nbAnomalieTotal = nbAnomalieTotal;
    }

    public int getNbAnomalieBloquant() {
        return nbAnomalieBloquant;
    }

    public void setNbAnomalieBloquant(int nbAnomalieBloquant) {
        this.nbAnomalieBloquant = nbAnomalieBloquant;
    }

}
