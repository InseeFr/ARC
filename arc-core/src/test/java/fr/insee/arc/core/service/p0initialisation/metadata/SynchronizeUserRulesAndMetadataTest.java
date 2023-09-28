package fr.insee.arc.core.service.p0initialisation.metadata;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class SynchronizeUserRulesAndMetadataTest  extends InitializeQueryTest  {
	
	private SynchronizeUserRulesAndMetadata synchronizationInstance= new SynchronizeUserRulesAndMetadata(new Sandbox(c, BddPatcherTest.testSandbox3));
	
	
	@Test
	public void copyMetadataToExecutorsTestNotScalable() throws SQLException, ArcException {
		buildPropertiesWithoutScalability(null);
		int result=synchronizationInstance.copyMetadataToExecutorsAllNods();
		assertEquals(0, result);
	}

	@Test
	public void copyMetadataToExecutorsTestScalable() throws SQLException, ArcException {
		
		buildPropertiesWithScalability(null);
		
		BddPatcherTest.initializeDatabaseForRetrieveTablesFromSchemaTest(u);
		
		int result=synchronizationInstance.copyMetadataToExecutorsAllNods();
		
		// copy should be a success
		assertEquals(1, result);
		
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS "+BddPatcherTest.testSandbox3+" CASCADE;");
	}
}
