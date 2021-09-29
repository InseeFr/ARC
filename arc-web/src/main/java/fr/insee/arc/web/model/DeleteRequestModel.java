package fr.insee.arc.web.model;

public class DeleteRequestModel implements ArcModel {

    private String ihmClient;
    private String environment;
    private String lowDate;
    private String highDate;

    public String getIhmClient() {
        return this.ihmClient;
    }

    public void setIhmClient(String ihmClient) {
        this.ihmClient = ihmClient;
    }
    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getLowDate() {
        return this.lowDate;
    }

    public void setLowDate(String lowDate) {
        this.lowDate = lowDate;
    }

    public String getHighDate() {
        return this.highDate;
    }

    public void setHighDate(String highDate) {
        this.highDate = highDate;
    }

}
