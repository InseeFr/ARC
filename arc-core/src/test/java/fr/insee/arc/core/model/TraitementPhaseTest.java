package fr.insee.arc.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TraitementPhaseTest {
	
	@Test
	public void getListPhaseCShouldNotBeNull() {
		assertNotNull(TraitementPhase.getListPhaseC());
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

	@Test
	public void getPhase() {
		// returns the normage phase as it is number 3
		assertEquals(TraitementPhase.NORMAGE, TraitementPhase.getPhase("3"));
		
		// out of range return null
		assertNull(TraitementPhase.getPhase("99"));

		// returns the normage phase by its name
		assertEquals(TraitementPhase.NORMAGE, TraitementPhase.getPhase("NORMAGE"));
	}
	
	
}
