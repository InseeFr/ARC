package fr.insee.arc.core.famille.model;

import fr.insee.arc.core.service.global.bo.JeuDeRegle;

public class ChargementRegle
        implements RegleComparable<
        ChargementRegle.CleComparaisonRegleChargement> {

    private JeuDeRegle jeuDeRegle;

    private Long idRegle;
    private String typeFichier;
    private String delimiter;
    private String format;
    private String commentaire;

    @Override
    public CleComparaisonRegleChargement getCleComparaison() {
        return CleComparaisonRegleChargement.PAS_DE_CRITERE_APPARIEMENT;
    }

    @Override
    public ContenuComparaisonRegleChargement getContenuComparaison() {
        return new ContenuComparaisonRegleChargement(
                typeFichier,
                delimiter,
                format
        );
    }

    public enum CleComparaisonRegleChargement {
        PAS_DE_CRITERE_APPARIEMENT
    }

    public record ContenuComparaisonRegleChargement(
            String typeFichier,
            String delimiter,
            String format
    ) {
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

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
