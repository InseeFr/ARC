package fr.insee.arc.core.dataobjects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;

public class DataObjectServiceTest {

	DataObjectService dataObjectService = new DataObjectService();
	
	@Before
	public void setup() throws ArcException {
		dataObjectService.setSandboxSchema("arc_bas1");
	}
	
	@Test
	public void testGetViewInSandbox() {
		Assert.assertEquals("arc_bas1.pilotage_fichier", dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
	}

	@Test
	public void testGetViewInSandboxGenerated() {
		Assert.assertEquals("arc_bas1.norme", dataObjectService.getView(ViewEnum.NORME));
	}
	
	
	@Test
	public void testGetViewInMetadata() {
		Assert.assertEquals("arc.ihm_norme", dataObjectService.getView(ViewEnum.IHM_NORME));
	}
	
	@Test
	public void testGetViewInTemporary() {
		Assert.assertEquals(ViewEnum.TABLE_TEST_IN_TEMPORARY.getTableName(), dataObjectService.getView(ViewEnum.TABLE_TEST_IN_TEMPORARY));
	}

	@Test
	public void testGetViewInPublic() {
		Assert.assertEquals("public."+ViewEnum.TABLE_TEST_IN_PUBLIC.getTableName(), dataObjectService.getView(ViewEnum.TABLE_TEST_IN_PUBLIC));
	}
	
	@Test
	public void testGetFullTableNameInSchema() {
		Assert.assertEquals("arc.toto", DataObjectService.getFullTableNameInSchema(SchemaEnum.ARC_METADATA, "toto"));
		Assert.assertEquals("public.toto", DataObjectService.getFullTableNameInSchema(SchemaEnum.PUBLIC, "toto"));
		Assert.assertEquals("toto", DataObjectService.getFullTableNameInSchema(SchemaEnum.TEMPORARY, "toto"));
		Assert.assertEquals("arc_bas.toto", DataObjectService.getFullTableNameInSchema(SchemaEnum.SANDBOX, "toto"));
		Assert.assertEquals("pg_catalog.pg_tables", DataObjectService.getFullTableNameInSchema(SchemaEnum.CATALOG, "pg_tables"));
		Assert.assertEquals("information_schema.columns", DataObjectService.getFullTableNameInSchema(SchemaEnum.INFORMATION_SCHEMA, "columns"));
	}

}
