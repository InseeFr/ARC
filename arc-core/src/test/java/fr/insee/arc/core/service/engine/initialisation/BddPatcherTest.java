package fr.insee.arc.core.service.engine.initialisation;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.api.ApiInitialisationService;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class BddPatcherTest extends InitializeQueryTest {

	private static String oldVersion = "v1";
	private static String newVersion = "v2";
	private static String userWithRestrictedRights = "arc_restricted";
	public static String testSandbox = "arc_bas1";

	/**
	 * test the database initialization
	 * 
	 * @throws ArcException
	 */
	@Test
	public void bddScriptTest() throws ArcException {
		
		// test an arc database
		createDatabase();

		GenericPreparedStatementBuilder query;

		// test the meta data schema creation
		query = new GenericPreparedStatementBuilder();
		query.append("select tablename from pg_tables where schemaname=")
				.append(query.quoteText(DataObjectService.ARC_METADATA_SCHEMA));

		HashMap<String, ArrayList<String>> content;

		content = new GenericBean(UtilitaireDao.get(UtilitaireDao.DEFAULT_CONNECTION_POOL).executeRequest(c, query))
				.mapContent();

		// check if all metadata view had been created
		for (ViewEnum v : ViewEnum.values()) {
			if (v.getTableLocation().equals(SchemaEnum.METADATA)) {
				assertTrue(content.get("tablename").contains(v.getTableName()));
			}
		}

		// test a sandbox schema creation
		query = new GenericPreparedStatementBuilder();
		query.append("select tablename from pg_tables where schemaname=").append(query.quoteText(testSandbox));

		content = new GenericBean(UtilitaireDao.get(UtilitaireDao.DEFAULT_CONNECTION_POOL).executeRequest(c, query))
				.mapContent();

		// check if the sandbox views had been created
		for (ViewEnum v : ViewEnum.values()) {
			if (v.getTableLocation().equals(SchemaEnum.SANDBOX)) {
				assertTrue(content.get("tablename").contains(v.getTableName()));
			}
		}
	}

	public static void createDatabase() throws ArcException {
		GenericPreparedStatementBuilder query;

		// clean database
		query = new GenericPreparedStatementBuilder();
		query.append("DROP SCHEMA IF EXISTS " + DataObjectService.ARC_METADATA_SCHEMA + " CASCADE;");
		query.append("DROP SCHEMA IF EXISTS " + testSandbox + " CASCADE;");
		UtilitaireDao.get(UtilitaireDao.DEFAULT_CONNECTION_POOL).executeRequest(c, query);

		// metadata schema creation
		BddPatcher.bddScript(oldVersion, newVersion, userWithRestrictedRights, c);
		// sandbox schema creation
		BddPatcher.bddScript(oldVersion, newVersion, userWithRestrictedRights, c, testSandbox);
		
		// insert test data
		insertTestData();
	}

	public static void insertTestData() throws ArcException {
		//
		
		String scriptDataTest;
		try {
			scriptDataTest = IOUtils.toString(ApiInitialisationService.class.getClassLoader().getResourceAsStream("BdDTest/script_sirene4.sql"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED);
		}
		UtilitaireDao.get(UtilitaireDao.DEFAULT_CONNECTION_POOL).executeImmediate(c, scriptDataTest);
		
	}
	
	
}