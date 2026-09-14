package fr.insee.arc.core.jeuderegle.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import fr.insee.arc.core.service.global.bo.JeuDeRegle;

public class ExpressionRegle
        implements RegleComparable<
        ExpressionRegle.CleComparaisonRegleExpression> {

    private JeuDeRegle jeuDeRegle;

    private Long idRegle;
    private String exprNom;
    private String exprValeur;
    private String commentaire;

    @Override
    public CleComparaisonRegleExpression getCleComparaison() {
        return new CleComparaisonRegleExpression(
                exprNom,
                exprValeur
        );
    }

    @Override
    public ContenuComparaisonRegleExpression getContenuComparaison() {
        return new ContenuComparaisonRegleExpression();
    }

    public record CleComparaisonRegleExpression(
            String exprNom,
            String exprValeur
    ) {
    }

    public record ContenuComparaisonRegleExpression() {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("idRegle", idRegle)
                .append("exprNom", exprNom)
                .append("exprValeur", exprValeur)
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

    public String getExprNom() {
        return exprNom;
    }

    public void setExprNom(String exprNom) {
        this.exprNom = exprNom;
    }

    public String getExprValeur() {
        return exprValeur;
    }

    public void setExprValeur(String exprValeur) {
        this.exprValeur = exprValeur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}