package fr.insee.arc.core.jeuderegle.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import fr.insee.arc.core.service.global.bo.JeuDeRegle;

public class ControleRegle
        implements RegleComparable<
        ControleRegle.CleComparaisonRegleControle> {

    private JeuDeRegle jeuDeRegle;

    private Integer idRegle;
    private String idClasse;
    private String rubriquePere;
    private String rubriqueFils;
    private String borneInf;
    private String borneSup;
    private String condition;
    private String preAction;
    private String todo;
    private String commentaire;
    private Integer xsdOrdre;
    private String xsdLabelFils;
    private String xsdRole;
    private String blockingThreshold;
    private String errorRowProcessing;

    @Override
    public CleComparaisonRegleControle getCleComparaison() {
        return new CleComparaisonRegleControle(
                rubriquePere,
                rubriqueFils
        );
    }

    @Override
    public ContenuComparaisonRegleControle getContenuComparaison() {
        return new ContenuComparaisonRegleControle(
                idClasse,
                borneInf,
                borneSup,
                condition,
                preAction,
                blockingThreshold,
                errorRowProcessing
        );
    }

    public record CleComparaisonRegleControle(
            String rubriquePere,
            String rubriqueFils
    ) {
    }

    public record ContenuComparaisonRegleControle(
            String idClasse,
            String borneInf,
            String borneSup,
            String condition,
            String preAction,
            String blockingThreshold,
            String errorRowProcessing
    ) {
    }

    public JeuDeRegle getJeuDeRegle() {
        return jeuDeRegle;
    }

    public void setJeuDeRegle(JeuDeRegle jeuDeRegle) {
        this.jeuDeRegle = jeuDeRegle;
    }

    public Integer getIdRegle() {
        return idRegle;
    }

    public void setIdRegle(Integer idRegle) {
        this.idRegle = idRegle;
    }

    public String getIdClasse() {
        return idClasse;
    }

    public void setIdClasse(String idClasse) {
        this.idClasse = idClasse;
    }

    public String getRubriquePere() {
        return rubriquePere;
    }

    public void setRubriquePere(String rubriquePere) {
        this.rubriquePere = rubriquePere;
    }

    public String getRubriqueFils() {
        return rubriqueFils;
    }

    public void setRubriqueFils(String rubriqueFils) {
        this.rubriqueFils = rubriqueFils;
    }

    public String getBorneInf() {
        return borneInf;
    }

    public void setBorneInf(String borneInf) {
        this.borneInf = borneInf;
    }

    public String getBorneSup() {
        return borneSup;
    }

    public void setBorneSup(String borneSup) {
        this.borneSup = borneSup;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPreAction() {
        return preAction;
    }

    public void setPreAction(String preAction) {
        this.preAction = preAction;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Integer getXsdOrdre() {
        return xsdOrdre;
    }

    public void setXsdOrdre(Integer xsdOrdre) {
        this.xsdOrdre = xsdOrdre;
    }

    public String getXsdLabelFils() {
        return xsdLabelFils;
    }

    public void setXsdLabelFils(String xsdLabelFils) {
        this.xsdLabelFils = xsdLabelFils;
    }

    public String getXsdRole() {
        return xsdRole;
    }

    public void setXsdRole(String xsdRole) {
        this.xsdRole = xsdRole;
    }

    public String getBlockingThreshold() {
        return blockingThreshold;
    }

    public void setBlockingThreshold(String blockingThreshold) {
        this.blockingThreshold = blockingThreshold;
    }

    public String getErrorRowProcessing() {
        return errorRowProcessing;
    }

    public void setErrorRowProcessing(String errorRowProcessing) {
        this.errorRowProcessing = errorRowProcessing;
    }
}