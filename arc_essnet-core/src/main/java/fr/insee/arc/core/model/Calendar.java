package fr.insee.arc.core.model;

public class Calendar {
    private String idNorme;
    private String periodicity;
    private String validiteInf;
    private String validiteSup;
    private String id;
    private String state;
    
    
    
    public String getIdNorme() {
        return idNorme;
    }
    public void setIdNorme(String idNorme) {
        this.idNorme = idNorme;
    }
    public String getPeriodicity() {
        return periodicity;
    }
    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }
    public String getValiditeInf() {
        return validiteInf;
    }
    public void setValiditeInf(String validiteInf) {
        this.validiteInf = validiteInf;
    }
    public String getValiditeSup() {
        return validiteSup;
    }
    public void setValiditeSup(String validiteSup) {
        this.validiteSup = validiteSup;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getState() {
        return state;
    }
    public void setState(String etatt) {
        this.state = etatt;
    }
    

}
