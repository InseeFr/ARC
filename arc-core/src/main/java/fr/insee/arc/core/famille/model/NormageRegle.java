package fr.insee.arc.core.famille.model;

import fr.insee.arc.core.service.global.bo.JeuDeRegle;

public class NormageRegle implements RegleComparable <NormageRegle.CleComparaisonRegleNormage>{

    private JeuDeRegle jeuDeRegle;

    private Integer idRegle;
    private String idClasse;
    private String rubrique;
    private String rubriqueNmcl;
    private String todo;
    private String commentaire;

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

    public String getRubrique() {
        return rubrique;
    }

    public void setRubrique(String rubrique) {
        this.rubrique = rubrique;
    }

    public String getRubriqueNmcl() {
        return rubriqueNmcl;
    }

    public void setRubriqueNmcl(String rubriqueNmcl) {
        this.rubriqueNmcl = rubriqueNmcl;
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

    public record CleComparaisonRegleNormage(
            String rubrique,
            String rubriqueNmcl
    ) {
    }

    public record ContenuComparaisonRegleNormage(
            String idClasse
    ) {
    }

    @Override
    public CleComparaisonRegleNormage getCleComparaison() {
        return new CleComparaisonRegleNormage(
                rubrique,
                rubriqueNmcl
        );
    }

    @Override
    public ContenuComparaisonRegleNormage getContenuComparaison() {
        return new ContenuComparaisonRegleNormage(
                idClasse
        );
    }
}
