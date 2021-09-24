package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewClient;
import fr.insee.arc.web.model.viewobjects.ViewOperations;
import fr.insee.arc.web.util.VObject;

public class MaintenanceOperationsModel implements ArcModel {

    private VObject viewOperations;
//    private VObject viewIhmClient;
    private String ihmClient;

    public MaintenanceOperationsModel() {
        this.viewOperations = new ViewOperations();
//        this.viewIhmClient = new ViewClient();
    }

    public VObject getViewOperations() {
        return this.viewOperations;
    }

    public void setViewOperations(VObject viewOperations) {
        this.viewOperations = viewOperations;
    }

    
    /**
     * @return the ihmClient
     */
    public String getIhmClient() {
        return ihmClient;
    }

    
    /**
     * @param ihmClient the ihmClient to set
     */
    public void setIhmClient(String ihmClient) {
        this.ihmClient = ihmClient;
    }

//    public VObject getViewIhmClient() {
//        return this.viewIhmClient;
//    }
//
//    public void setViewIhmClient(VObject viewIhmClient) {
//        this.viewIhmClient = viewIhmClient;
//    }

}
