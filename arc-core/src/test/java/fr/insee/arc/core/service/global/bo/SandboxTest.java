package fr.insee.arc.core.service.global.bo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class SandboxTest extends InitializeQueryTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testSandboxUtilityClass()
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(Sandbox.class);
	}

	@Test
	public void isEnvSetForProductionTest() throws IOException, SQLException, ArcException {
		BddPatcherTest.createDatabase();

		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();

		buildPropertiesWithoutScalability(repertoire);

		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc CASCADE;");
		u.executeImmediate(c, "CREATE schema arc;");
		u.executeImmediate(c, "CREATE TABLE arc.parameter (key text, val text, description text);");
		u.executeImmediate(c,
				"INSERT INTO arc.parameter SELECT 'ArcAction.productionEnvironments', '[\"arc_prod\",\"arc_bas2\"]', 'environnement déclaré en production en ihm';");

		assertTrue(Sandbox.isEnvSetForProduction("arc_prod"));
		assertTrue(Sandbox.isEnvSetForProduction("arc_bas2"));
		assertFalse(Sandbox.isEnvSetForProduction("arc_bas1"));
		
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc CASCADE;");
	}

}
