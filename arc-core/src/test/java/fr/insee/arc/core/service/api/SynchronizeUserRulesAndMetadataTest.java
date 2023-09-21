package fr.insee.arc.core.service.api;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.p0initialisation.userdata.SynchronizeUserRulesAndMetadata;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class SynchronizeUserRulesAndMetadataTest extends InitializeQueryTest {
	
	@Test
	public void copyMetadataToExecutorsTestNotScalable() throws SQLException, ArcException {
		buildPropertiesWithNoScalability(null);
		int result=SynchronizeUserRulesAndMetadata.copyMetadataToExecutorsAllNods(c, BddPatcherTest.testSandbox3);
		assertEquals(0, result);
	}

	@Test
	public void copyMetadataToExecutorsTestScalable() throws SQLException, ArcException {
		
		
		buildPropertiesWithScalability(null);
		
		BddPatcherTest.initializeDatabaseForRetrieveTablesFromSchemaTest(u);
		
		int result=SynchronizeUserRulesAndMetadata.copyMetadataToExecutorsAllNods(c, BddPatcherTest.testSandbox3);
		
		// copy should be a success
		assertEquals(1, result);
		
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS "+BddPatcherTest.testSandbox3+" CASCADE;");
	}

//	@Test 
//	public void requeteListTableEnvTest()
//	{
//		System.out.println(ApiInitialisationService.requeteTablesFoundInPhaseAndEnv("arc_bas2", TraitementPhase.CHARGEMENT.toString()));
//		assertTrue(true);
//	}
	
}
