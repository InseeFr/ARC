package fr.insee.arc.core.model;

public enum TraitementRapport {
    NORMAGE_MULTI_NORME("Plusieurs normes en correspondance. "), //
    NORMAGE_NO_NORME("Aucune norme trouvée. "), //
    NORMAGE_VALIDITE_NODATE("Validité <> yyyy-mm-dd. "), //
    NORMAGE_MULTI_DATE("PLusieurs validité dans le fichier. "), //
    NORMAGE_NO_DATE("Aucune validité trouvée. "), //
    CONTROLE_CALENDRIER_OBSOLETE("Calendrier disparu. "), //
    CONTROLE_VALIDITE_HORS_CALENDRIER("Validité hors du nouveau calendrier. "), //
    TOUTE_PHASE_AUCUNE_REGLE("Pas de règle associée. "), //
    INITIALISATION_CHGT_DEF_NORME("Nouvelle définition de norme. "), //
    INITIALISATION_NORME_OBSOLETE("Norme disparue. "), //
    INITIALISATION_HORS_CALENDRIER("Validité hors calendrier. "), //
    INITIALISATION_CORRUPTED_ENTRY("Fichier corrompu. "), //
    INITIALISATION_CORRUPTED_ARCHIVE("Archive corrompue. "), //
    INITIALISATION_FICHIER_OK_ARCHIVE_KO("Fichier OK dans archive KO."), //
    INITIALISATION_DUPLICATE("Doublon. "), //
    INITIALISATION_DUPLICATE_ARCHIVE("Archive contenant un doublon. "), //
    TOUTE_PHASE_ERREUR_SQL("Erreur SQL : "), TOUTE_PHASE_TAUX_ERREUR_SUPERIEUR_SEUIL(
            "Le fichier a été intégralement filtré"), TOUTE_PHASE_AUCUN_ENREGISTREMENT(
            "Le fichier ne possède aucun enregistrement pour cette phase. ");

    private TraitementRapport(String anExpression) {
        this.expression = anExpression;
    }

    private String expression;

    @Override
    public String toString() {
        return this.expression;
    }
}
