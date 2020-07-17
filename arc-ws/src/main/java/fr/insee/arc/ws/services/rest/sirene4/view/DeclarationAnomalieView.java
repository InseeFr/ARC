package fr.insee.arc.ws.services.rest.sirene4.view;

import java.util.List;

public class DeclarationAnomalieView {

    private List<AnomalieView> anomalies;

    public List<AnomalieView> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<AnomalieView> anomalies) {
        this.anomalies = anomalies;
    }

}
