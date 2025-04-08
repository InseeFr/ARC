package fr.insee.arc.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.junit.Test;

public class TraitementPhaseTest {

	
	@Test
	public void getListPhaseExecutableInBasShouldNotBeNull() {
		assertNotNull(TraitementPhase.getListPhaseExecutableInBas());
	}

	@Test
	public void getListPhaseExecutableInBasShoulNotBeEmpty() {
		assertTrue(!TraitementPhase.getListPhaseExecutableInBas().isEmpty());
	}

	@Test
	public void getListPhaseExecutableInBasShouldNotContainsUnordoredElements() {
		for (TraitementPhase phase : TraitementPhase.getListPhaseExecutableInBas()) {
			assertTrue(phase.toString(), phase.getOrdre() >= 0);
		}
	}
	
	@Test
	public void nextPhases() {
		
		System.out.println(TraitementPhase.EXPORT.nextPhases());
		assertTrue(TraitementPhase.EXPORT.nextPhases().isEmpty());
		assertEquals(TraitementPhase.MAPPING.nextPhases(),  Stream.of(TraitementPhase.EXPORT).toList());
		assertEquals(TraitementPhase.DUMMY.nextPhases().size(), 7 );
		
	}
	
	
	@Test
	public void previousPhase()
	{
		assertNull(TraitementPhase.DUMMY.previousPhase());
		assertEquals(TraitementPhase.CONTROLE, TraitementPhase.MAPPING.previousPhase());
	}
	

	@Test
	public void getPhase() {
		
		// returns the normage phase as it is number 3
		assertEquals(TraitementPhase.NORMAGE, TraitementPhase.getPhase(3));
		
		
		// returns the normage phase as it is number 3
		assertEquals(TraitementPhase.NORMAGE, TraitementPhase.getPhase("3"));
		
		// out of range return null
		assertNull(TraitementPhase.getPhase("99"));

		// returns the normage phase by its name
		assertEquals(TraitementPhase.NORMAGE, TraitementPhase.getPhase("NORMAGE"));
	}
	
	
}
