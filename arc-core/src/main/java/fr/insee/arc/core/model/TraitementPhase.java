package fr.insee.arc.core.model;

import java.util.ArrayList;

public enum TraitementPhase {
    DUMMY(-1, 1), INITIALISATION(0, 1000000), RECEPTION(1, 1000000), CHARGEMENT(2, 1000000), NORMAGE(3, 1000000)
    , CONTROLE(4, 1000000), FILTRAGE(5, 1000000), MAPPING(6, 1000000), TRANSFORMATION(0,1);
    private TraitementPhase(int anOrdre, int aNbLigneATraiter) {
        this.ordre = anOrdre;
        this.nbLigneATraiter = aNbLigneATraiter;
    }

    private int ordre;
    private int nbLigneATraiter;

    public int getOrdre() {
        return this.ordre;
    }

    public int getNbLigneATraiter() {
        return this.nbLigneATraiter;
    }

    /**
     * Renvoie la phase en fonction de l'ordre
     * 
     * @param ordre
     *            , numéro d'ordre dans l'énumération (premier=1)
     * @return
     */
    public TraitementPhase getPhase(int ordre) {
        for (TraitementPhase phase : TraitementPhase.values()) {
            if (phase.getOrdre() == ordre) {
                return phase;
            }
        }
        return null;
    }

    /**
     * Renvoie une liste des phases qui sont postérieures à une phase donnée
     * 
     * @return
     */
    public ArrayList<TraitementPhase> nextPhases() {
        ArrayList<TraitementPhase> listPhase = new ArrayList<>();
        for (TraitementPhase phase : TraitementPhase.values()) {
            if (phase.getOrdre() > ordre) {
                listPhase.add(phase);
            }
        }
        return listPhase;
    }

    /**
     * Renvoie la phase précédente ou null
     * 
     * @return
     */
    public TraitementPhase previousPhase() {
        TraitementPhase phase = null;
        int i = this.getOrdre();
        phase = getPhase(i - 1);
        return phase;
    }
    
    /**
     * Renvoie la phase précédente ou null
     * 
     * @return
     */
    public TraitementPhase nextPhase() {
        TraitementPhase phase = null;
        int i = this.getOrdre();
        phase = getPhase(i + 1);
        return phase;
    }
    
}
