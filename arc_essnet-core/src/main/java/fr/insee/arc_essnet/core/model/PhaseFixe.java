package fr.insee.arc_essnet.core.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * All the fixed phases
 * 
 * @author S4lwo8
 *
 */
public enum PhaseFixe {
    DUMMY(new TraitementPhaseEntity("DUMMY", TypeTraitementPhase.DUMMY, 0, 1, false, false, ""))//
    , INITIALISATION(new TraitementPhaseEntity("INITIALISATION", TypeTraitementPhase.INITIALIZE, 0, 1000000, true, true,
	    "DUMMY"))//
    , RECEPTION(new TraitementPhaseEntity("RECEPTION", TypeTraitementPhase.REGISTER, 0, 1000000, true, true,
	    "INITIALISATION"))//
    , IDENTIFICATION(new TraitementPhaseEntity("IDENTIFICATION", TypeTraitementPhase.IDENTIFY, 0, 1000000, true, true,
	    "RECEPTION"))//
    , CHARGEMENT(
	    new TraitementPhaseEntity("CHARGEMENT", TypeTraitementPhase.LOAD, 0, 1000000, true, true, "IDENTIFICATION"))//
    ;

    private TraitementPhaseEntity traitementPhaseEntity;

    private PhaseFixe(TraitementPhaseEntity aTraitementPhaseEntity) {
	this.setTraitementPhaseEntity(aTraitementPhaseEntity);
    }

    public TraitementPhaseEntity getTraitementPhaseEntity() {
	return traitementPhaseEntity;
    }

    private void setTraitementPhaseEntity(TraitementPhaseEntity traitementPhaseEntity) {
	this.traitementPhaseEntity = traitementPhaseEntity;
    }

    public static List<TraitementPhaseEntity> getAllFixedPhases() {
	return Arrays.asList(PhaseFixe.values()).stream().map(el -> el.getTraitementPhaseEntity())
		.collect(Collectors.toList());
    }
}
