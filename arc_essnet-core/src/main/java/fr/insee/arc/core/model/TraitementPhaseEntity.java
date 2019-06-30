package fr.insee.arc.core.model;

public class TraitementPhaseEntity {

    private String nomPhase;
    private String previousPhase;
    private TypeTraitementPhase typePhase;
    private int ordre;
    private int nbLigneTraitee;
    private boolean isInIhm;
    private boolean isRAIhm;

    public TraitementPhaseEntity(String nomPhase, TypeTraitementPhase typePhase, int ordre, int nbLigneTraitee,
	    boolean isInIhm, boolean isRAIhm, String previousPhase) {
	super();
	this.nomPhase = nomPhase;
	this.typePhase = typePhase;
	this.ordre = ordre;
	this.nbLigneTraitee = nbLigneTraitee;
	this.isInIhm = isInIhm;
	this.isRAIhm = isRAIhm;
	this.previousPhase= previousPhase;

    }

    public TraitementPhaseEntity() {
    }

    public String getNomPhase() {
	return nomPhase;
    }

    public void setNomPhase(String nomPhase) {
	this.nomPhase = nomPhase;
    }

    public TypeTraitementPhase getTypePhase() {
	return typePhase;
    }

    public void setTypePhase(TypeTraitementPhase typePhase) {
	this.typePhase = typePhase;
    }

    public int getOrdre() {
	return ordre;
    }

    public void setOrdre(int ordre) {
	this.ordre = ordre;
    }

    public int getNbLigneTraitee() {
	return nbLigneTraitee;
    }

    public void setNbLigneTraitee(int nbLigneTraitee) {
	this.nbLigneTraitee = nbLigneTraitee;
    }

    public String getPreviousPhase() {
	return previousPhase;
    }

    public void setPreviousPhase(String previousPhase) {
	this.previousPhase = previousPhase;
    }

    public boolean getIsInIhm() {
	return isInIhm;
    }

    public void setIsInIhm(boolean isInIhhm) {
	this.isInIhm = isInIhhm;
    }

    public boolean getIsRAIhm() {
	return isRAIhm;
    }

    public void setIsRAIhm(boolean isRAIhm) {
	this.isRAIhm = isRAIhm;
    }

}
