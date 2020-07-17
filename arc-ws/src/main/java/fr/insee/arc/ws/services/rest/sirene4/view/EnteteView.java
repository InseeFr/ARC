package fr.insee.arc.ws.services.rest.sirene4.view;

public class EnteteView {

    private EnteteBilanView enteteBilan;

    private EnteteAnomalieView enteteAnomalie;

    public EnteteBilanView getEnteteBilan() {
        return enteteBilan;
    }

    public void setEnteteBilan(EnteteBilanView enteteBilan) {
        this.enteteBilan = enteteBilan;
    }

    public EnteteAnomalieView getEnteteAnomalie() {
        return enteteAnomalie;
    }

    public void setEnteteAnomalie(EnteteAnomalieView enteteAnomalie) {
        this.enteteAnomalie = enteteAnomalie;
        this.enteteBilan = new EnteteBilanView(enteteAnomalie.getAnomalies());
    }

}
