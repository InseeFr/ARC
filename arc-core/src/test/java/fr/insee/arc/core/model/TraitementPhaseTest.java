package fr.insee.arc.core.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TraitementPhaseTest {
	
	@Test
	public void getListPhaseCShouldNotBeNull() {
		assertTrue(TraitementPhase.getListPhaseC() != null);
	}

	@Test
	public void getListPhaseCShoulNotBeEmpty() {
		assertTrue(!TraitementPhase.getListPhaseC().isEmpty());
	}

	@Test
	public void getListPhaseCShouldNotContainsUnordoredElements() {
		for (TraitementPhase phase : TraitementPhase.getListPhaseC()) {
			assertTrue(phase.toString(), phase.getOrdre() >= 0);
		}
	}

}
