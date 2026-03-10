package fr.insee.arc.core.service.global.bo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class SandboxTest extends InitializeQueryTest {

	@Test
	public void isEnvSetForProductionTest() throws IOException, SQLException, ArcException {

		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc CASCADE;");
		u.executeRequest(c, "CREATE schema arc;");
		u.executeRequest(c, "CREATE TABLE arc.parameter (key text, val text, description text);");
		u.executeRequest(c,
				"INSERT INTO arc.parameter SELECT 'ArcAction.productionEnvironments', '[\"arc_prod\",\"arc_bas2\"]', 'environnement déclaré en production en ihm';");

		assertTrue(new Sandbox(c,"arc_prod").isEnvSetForProduction());
		assertTrue(new Sandbox(c,"arc_bas2").isEnvSetForProduction());
		assertFalse(new Sandbox(c,"arc_bas1").isEnvSetForProduction());
		
		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc CASCADE;");
	}

}
