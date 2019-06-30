package fr.insee.arc.core.model.rules;


/**
 * Abstract class that all rules entity need to extends
 * @author Pépin Rémi
 *
 */
public abstract class AbstractRuleEntity {

    private String idSource ;
    private String idNorme  ;
    private String idRule;
    private String periodicite ;
    private String validiteInf;
    private String validiteSup ;
    private String version ;
    private String commentaire ;
    
    public String getIdSource() {
        return idSource;
    }
    public void setIdSource(String idSource) {
        this.idSource = idSource;
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
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getCommentaire() {
        return commentaire;
    }
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    public String getIdRule() {
	return idRule;
    }
    public void setIdRule(String idRule) {
	this.idRule = idRule;
    }

    
}
