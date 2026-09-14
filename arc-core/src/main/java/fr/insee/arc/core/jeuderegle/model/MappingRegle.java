package fr.insee.arc.core.jeuderegle.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import fr.insee.arc.core.service.global.bo.JeuDeRegle;


public class MappingRegle
        implements RegleComparable<
        MappingRegle.CleComparaisonRegleMapping> {

    private JeuDeRegle jeuDeRegle;

    private Long idRegle;
    private String variableSortie;
    private String exprRegleCol;
    private String commentaire;

    @Override
    public CleComparaisonRegleMapping getCleComparaison() {
        return new CleComparaisonRegleMapping(variableSortie);
    }

    @Override
    public ContenuComparaisonRegleMapping getContenuComparaison() {
        return new ContenuComparaisonRegleMapping(exprRegleCol);
    }

    public record CleComparaisonRegleMapping(
            String variableSortie
    ) {
    }

    public record ContenuComparaisonRegleMapping(
            String exprRegleCol
    ) {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("idRegle", idRegle)
                .append("variableSortie", variableSortie)
                .append("exprRegleCol", exprRegleCol)
                .append("commentaire", commentaire)
                .toString();
    }

    public JeuDeRegle getJeuDeRegle() {
        return jeuDeRegle;
    }

    public void setJeuDeRegle(JeuDeRegle jeuDeRegle) {
        this.jeuDeRegle = jeuDeRegle;
    }

    public Long getIdRegle() {
        return idRegle;
    }

    public void setIdRegle(Long idRegle) {
        this.idRegle = idRegle;
    }

    public String getVariableSortie() {
        return variableSortie;
    }

    public void setVariableSortie(String variableSortie) {
        this.variableSortie = variableSortie;
    }

    public String getExprRegleCol() {
        return exprRegleCol;
    }

    public void setExprRegleCol(String exprRegleCol) {
        this.exprRegleCol = exprRegleCol;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}