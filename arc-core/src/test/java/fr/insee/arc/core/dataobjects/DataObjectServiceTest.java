package fr.insee.arc.core.dataobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;

public class DataObjectServiceTest {

	DataObjectService dataObjectService = new DataObjectService();
	
	@BeforeEach
	public void setup() throws ArcException {
		dataObjectService.setSandboxSchema("arc_bas1");
	}
	
	@Test
	public void testGetViewInSandbox() {
		assertEquals("arc_bas1.pilotage_fichier", dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
	}

	@Test
	public void testGetViewInSandboxGenerated() {
		assertEquals("arc_bas1.norme", dataObjectService.getView(ViewEnum.NORME));
	}
	
	
	@Test
	public void testGetViewInMetadata() {
		assertEquals("arc.ihm_norme", dataObjectService.getView(ViewEnum.IHM_NORME));
	}
	
	@Test
	public void testGetViewInTemporary() {
		assertEquals(ViewEnum.TABLE_TEST_IN_TEMPORARY.getTableName(), dataObjectService.getView(ViewEnum.TABLE_TEST_IN_TEMPORARY));
	}

	@Test
	public void testGetViewInPublic() {
		assertEquals("public."+ViewEnum.TABLE_TEST_IN_PUBLIC.getTableName(), dataObjectService.getView(ViewEnum.TABLE_TEST_IN_PUBLIC));
	}
	
	@Test
	public void testGetFullTableNameInSchema() {
		assertEquals("arc.toto", ViewEnum.getFullTableNameInSchema(SchemaEnum.ARC_METADATA, "toto"));
		assertEquals("public.toto", ViewEnum.getFullTableNameInSchema(SchemaEnum.PUBLIC, "toto"));
		assertEquals("toto", ViewEnum.getFullTableNameInSchema(SchemaEnum.TEMPORARY, "toto"));
		assertEquals("arc_bas.toto", ViewEnum.getFullTableNameInSchema(SchemaEnum.SANDBOX, "toto"));
		assertEquals("pg_catalog.pg_tables", ViewEnum.getFullTableNameInSchema(SchemaEnum.CATALOG, "pg_tables"));
		assertEquals("information_schema.columns", ViewEnum.getFullTableNameInSchema(SchemaEnum.INFORMATION_SCHEMA, "columns"));
	}

}
