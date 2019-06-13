package fr.insee.arc_essnet.core.model;

import java.util.ArrayList;

public enum TypeTraitementPhase {
    DUMMY(-1, 1) //
    , INITIALIZE(0, 1000000)//
    , REGISTER(1, 1000000)//
    , IDENTIFY(2, 1000000)//
    , LOAD(3, 1000000)//
    , STRUCTURIZE_XML(4, 1000000)//
    , CONTROL(5, 1000000)//
    , FILTER(6, 1000000)//
    , FORMAT_TO_MODEL(7, 1000000);//

    private TypeTraitementPhase(int anOrdre, int aNbLigneATraiter) {
	this.order = anOrdre;
	this.nbLinesToProcess = aNbLigneATraiter;
    }

    private int order;
    private int nbLinesToProcess;

    public int getOrder() {
	return this.order;
    }

    public int getNbLinesToProcess() {
	return this.nbLinesToProcess;
    }

    /**
     * Return phase with given order
     *       
     * @param order
     *            , order in the enum
     * @return the phase with the given order
     */
    public TypeTraitementPhase getPhaseByOrder(int order) {
	for (TypeTraitementPhase phase : TypeTraitementPhase.values()) {
	    if (phase.getOrder() == order) {
		return phase;
	    }
	}
	return null;
    }

    /**
     * Return the later phases list of a given phase
     * 
     * @return the later phase list
     */
    public ArrayList<TypeTraitementPhase> nextPhases() {
	ArrayList<TypeTraitementPhase> listPhase = new ArrayList<>();
	for (TypeTraitementPhase phase : TypeTraitementPhase.values()) {
	    if (phase.getOrder() > order) {
		listPhase.add(phase);
	    }
	}
	return listPhase;
    }

    /**
     * Return previous phase or null 
     * 
     * @return Return previous phase or null 
     */
    public TypeTraitementPhase previousPhase() {
	TypeTraitementPhase phase = null;
	int i = this.getOrder();
	phase = getPhaseByOrder(i - 1);
	return phase;
    }

    /**
     * Return nex phase or null 
     * 
     * @return Return nex phase or null 
     */
    public TypeTraitementPhase nextPhase() {
	TypeTraitementPhase phase = null;
	int i = this.getOrder();
	phase = getPhaseByOrder(i + 1);
	return phase;
    }

}
