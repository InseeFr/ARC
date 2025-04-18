package fr.insee.arc.core.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;

public class ApiServiceFactoryTest {

	@Test
	public void testMegaFactoryInstance() {
		
		ApiService p;
		
		// iterate over all ARC phases to check if the factory instantiation is correct
		
		int capacity=5001;
		
		for (TraitementPhase arcPhase:TraitementPhase.getListPhaseExecutableInBas())
		{
			p=ApiServiceFactory.getService(arcPhase, "arc_bas2", capacity, null);

			assertEquals(null, p.getParamBatch());
			assertEquals(capacity, p.getNbEnr().intValue());
			assertEquals(p.getCurrentPhase()[0].toString(), arcPhase.toString());
		}

	}

}
