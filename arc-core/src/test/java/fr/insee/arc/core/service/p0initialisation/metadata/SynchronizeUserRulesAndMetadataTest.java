package fr.insee.arc.core.service.p0initialisation.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class SynchronizeUserRulesAndMetadataTest  extends InitializeQueryTest  {
	
	private SynchronizeRulesAndMetadataOperation synchronizationInstance= new SynchronizeRulesAndMetadataOperation(new Sandbox(c, BddPatcherTest.testSandbox3));
	
	
	@Test
	public void copyMetadataToExecutorsTestNotScalable() throws SQLException, ArcException {
		
		buildPropertiesWithoutScalability(null);
		
		BddPatcherTest.initializeDatabaseForRetrieveTablesFromSchemaTest(u);
		
		synchronizationInstance.patchDatabaseToExecutorsAllNods();
		synchronizationInstance.copyMetadataToExecutorsAllNods();
		
		int nbRowOnCoordinator = UtilitaireDao.get(0).getInt(c, "SELECT count(*) FROM arc.nmcl_code_pays_etranger_2023");
		assertEquals(1, nbRowOnCoordinator);
	}

	@Test
	public void copyMetadataToExecutorsTestScalable() throws SQLException, ArcException {
		
		buildPropertiesWithOneExecutor(null);
		
		BddPatcherTest.initializeDatabaseForRetrieveTablesFromSchemaTest(u);
		
		synchronizationInstance.patchDatabaseToExecutorsAllNods();
		synchronizationInstance.copyMetadataToExecutorsAllNods();
		
		int nbRowOnCoordinator = UtilitaireDao.get(0).getInt(c, "SELECT count(*) FROM arc.nmcl_code_pays_etranger_2023");
		int nbRowOnExecutor = UtilitaireDao.get(0).getInt(e1, "SELECT count(*) FROM arc.nmcl_code_pays_etranger_2023");

		assertEquals(nbRowOnCoordinator, nbRowOnExecutor);
		
		u.executeRequest(c, "DROP SCHEMA IF EXISTS "+BddPatcherTest.testSandbox3+" CASCADE;");
	}
}
