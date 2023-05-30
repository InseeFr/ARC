package fr.insee.arc.batch.threadrunners;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.core.model.TraitementPhase;

public class ArcThreadFactoryTest {

	@Test
	public void capacityParameterName() {
		
		// The INITIALISATION and RECEPTION phase must return ParameterKey.KEY_FOR_MAX_SIZE_RECEPTION
		// The CHARGEMENT phase must return ParameterKey.KEY_FOR_MAX_FILES_TO_LOAD
		// The others phases such as NORMAGE, CONTROLE, FILTRAGE, MAPPING must return ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE
		
		assertEquals(PhaseParameterKeys.KEY_FOR_MAX_SIZE_RECEPTION, new PhaseThreadFactory(null, TraitementPhase.INITIALISATION).capacityParameterName());
		assertEquals(PhaseParameterKeys.KEY_FOR_MAX_SIZE_RECEPTION, new PhaseThreadFactory(null, TraitementPhase.RECEPTION).capacityParameterName());
		assertEquals(PhaseParameterKeys.KEY_FOR_MAX_FILES_TO_LOAD, new PhaseThreadFactory(null, TraitementPhase.CHARGEMENT).capacityParameterName());
		assertEquals(PhaseParameterKeys.KEY_FOR_MAX_FILES_PER_PHASE, new PhaseThreadFactory(null, TraitementPhase.NORMAGE).capacityParameterName());
		assertEquals(PhaseParameterKeys.KEY_FOR_MAX_FILES_PER_PHASE, new PhaseThreadFactory(null, TraitementPhase.CONTROLE).capacityParameterName());
		assertEquals(PhaseParameterKeys.KEY_FOR_MAX_FILES_PER_PHASE, new PhaseThreadFactory(null, TraitementPhase.MAPPING).capacityParameterName());
		
	}

}
