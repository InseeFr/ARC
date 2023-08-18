package fr.insee.arc.core.dataobjects;

import org.junit.Assert;
import org.junit.Test;

public class DataObjectServiceTest {

	@Test
	public void testGetViewInSandbox() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("arc_bas1.pilotage_fichier", dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
	}

	@Test
	public void testGetViewInSandboxGenerated() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("arc_bas1.norme", dataObjectService.getView(ViewEnum.NORME));
	}
	
	
	@Test
	public void testGetViewInMetadata() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("arc.ihm_norme", dataObjectService.getView(ViewEnum.IHM_NORME));
	}
	
	@Test
	public void testGetViewInTemporary() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals(ViewEnum.TABLE_TEST_IN_TEMPORARY.getTableName(), dataObjectService.getView(ViewEnum.TABLE_TEST_IN_TEMPORARY));
	}

	@Test
	public void testGetViewInPublic() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("public."+ViewEnum.TABLE_TEST_IN_PUBLIC.getTableName(), dataObjectService.getView(ViewEnum.TABLE_TEST_IN_PUBLIC));
	}
	
	@Test
	public void testGetFullTableNameInMetadata() {
		Assert.assertEquals("arc.toto", DataObjectService.getFullTableNameInMetadata("toto"));
	}

}
