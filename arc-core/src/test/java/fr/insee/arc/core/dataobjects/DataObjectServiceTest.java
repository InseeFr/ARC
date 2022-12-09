package fr.insee.arc.core.dataobjects;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class DataObjectServiceTest {

	@Test
	public void testGetViewInSandbox() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("arc_bas1.pilotage_fichier", dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
	}

	@Test
	public void testGetViewInMetadata() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("arc.ihm_norme", dataObjectService.getView(ViewEnum.IHM_NORME));
	}
	
	@Test
	public void testGetFullTableNameInMetadata() {
		DataObjectService dataObjectService = new DataObjectService("arc_bas1");
		Assert.assertEquals("arc.toto", dataObjectService.getFullTableNameInMetadata("toto"));
	}

}
