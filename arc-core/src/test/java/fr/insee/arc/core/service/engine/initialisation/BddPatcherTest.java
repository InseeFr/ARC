package fr.insee.arc.core.service.engine.initialisation;

import static org.junit.Assert.assertEquals;
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

	private static String newVersion = "v2";
	private static String userWithRestrictedRights = "";
	public static String testSandbox1 = "arc_bas1";
	public static String testSandbox2 = "arc_bas2";
	public static String testSandbox3 = "arc_bas3";
	public static String testSandbox8 = "arc_bas8";

	
	/**
	 * test the database initialization
	 * 
	 * @throws ArcException
	 */
	@Test
	public void bddScriptTest() throws ArcException {
		
		// test an arc database
		createDatabase(userWithRestrictedRights);
		testDatabaseCreation();

		createDatabase("");
		testDatabaseCreation();
		
		createDatabase(null);
		testDatabaseCreation();
	}
	
	public static void testDatabaseCreation() throws ArcException
	{
		GenericPreparedStatementBuilder query;

		// test the meta data schema creation
		query = new GenericPreparedStatementBuilder();
		query.append("select tablename from pg_tables where schemaname=")
				.append(query.quoteText(DataObjectService.ARC_METADATA_SCHEMA));

		HashMap<String, ArrayList<String>> content;

		content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query))
				.mapContent();

		// check if all metadata view had been created
		for (ViewEnum v : ViewEnum.values()) {
			if (v.getTableLocation().equals(SchemaEnum.METADATA)) {
				assertTrue(content.get("tablename").contains(v.getTableName()));
			}
		}

		// test a sandbox schema creation
		query = new GenericPreparedStatementBuilder();
		query.append("select tablename from pg_tables where schemaname=").append(query.quoteText(testSandbox1));

		content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query))
				.mapContent();
	
		// check if the sandbox views had been created
		for (ViewEnum v : ViewEnum.values()) {
			if (v.getTableLocation().equals(SchemaEnum.SANDBOX)) {
				assertTrue(content.get("tablename").contains(v.getTableName()));
			}
		}
	}

	/**
	 * create a blank arc database based on bddScript method
	 * @throws ArcException
	 */
	private static void createDatabase(String restrictedUser) throws ArcException {
		GenericPreparedStatementBuilder query;

		// clean database
		query = new GenericPreparedStatementBuilder();
		query.append("DROP SCHEMA IF EXISTS " + DataObjectService.ARC_METADATA_SCHEMA + " CASCADE;");
		query.append("DROP SCHEMA IF EXISTS " + testSandbox1 + " CASCADE;");
		query.append("DROP SCHEMA IF EXISTS public CASCADE;");
		UtilitaireDao.get(0).executeRequest(c, query);
		
		BddPatcher patcher=new BddPatcher();
		patcher.getProperties().setGitCommitId(newVersion);
		patcher.getProperties().setDatabaseRestrictedUsername(userWithRestrictedRights);

		// metadata schema creation
		patcher.bddScript(c);
		// sandbox schema creation
		patcher.bddScript(c, testSandbox1, testSandbox2, testSandbox8);
		
	}
	
	/**
	 * create a blank arc database based on bddScript method
	 * @throws ArcException
	 */
	public static void createDatabase() throws ArcException {
		createDatabase(userWithRestrictedRights);		
	}

	/**
	 * insert data for functional tests sirene
	 * @throws ArcException
	 */
	public static void insertTestDataSirene() throws ArcException {
		insertTestData("BdDTest/script_test_fonctionnel_sirene.sql");
	}
	
	/**
	 * insert data for functional tests siera
	 * @throws ArcException
	 */
	public static void insertTestDataSiera() throws ArcException {
		insertTestData("BdDTest/script_test_fonctionnel_siera.sql");
	}
	
	/**
	 * insert data for functional tests siera
	 * @throws ArcException
	 */
	public static void insertTestDataAnimal() throws ArcException {
		insertTestData("BdDTest/script_test_fonctionnel_animal.sql");
	}
	
	/**
	 * insert data for functional tests
	 * @throws ArcException
	 */
	public static void insertTestDataLight() throws ArcException {
		insertTestData("BdDTest/script_test_fonctionnel_sample.sql");
	}
	
	/**
	 * insert data for export unit tests
	 * @throws ArcException
	 */
	public static void insertTestDataExport() throws ArcException {
		insertTestData("BdDTest/script_test_export.sql");
	}
	
	/**
	 * insert data for norm family unit tests
	 * @throws ArcException
	 */
	public static void insertTestDataFamilleNorme() throws ArcException {
		insertTestData("BdDTest/script_test_famille_norme.sql");
	}
	
	/**
	 * insert data for functional tests
	 * @throws ArcException
	 */
	private static void insertTestData(String sqlFileResource) throws ArcException {		
		String scriptDataTest;
		try {
			scriptDataTest = IOUtils.toString(ApiInitialisationService.class.getClassLoader().getResourceAsStream(sqlFileResource), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED);
		}
		UtilitaireDao.get(0).executeImmediate(c, scriptDataTest);
		
	}
	
	
	@Test
	public void retrieveRulesTablesFromSchemaTest() throws ArcException {
		UtilitaireDao.get(0).executeImmediate(c, "DROP SCHEMA IF EXISTS "+testSandbox3+" CASCADE;");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE SCHEMA IF NOT EXISTS "+testSandbox3+";");

		// tables que la fonction testée ne doit pas retenir
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".pilotage_fichier (a text);");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".reception_regle (a text);");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".fake_regle (a text);");
		
		// tables que la fonction testée doit retenir
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".chargement_regle (regle text);");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".mapping_regle (id_norme text, regle text, commentaire text);");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".mod_variable_metier (a text);");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE "+testSandbox3+".mod_table_metier (a text);");

		// invocation de la fonction à tester
		ArrayList<String> result;
		
		result = BddPatcher.retrieveRulesTablesFromSchema(c, testSandbox3);

		// test : on enleve tous les éléments à retenir et l'array list devra être au final vide
		assertTrue(result.contains(testSandbox3+".chargement_regle"));
		assertTrue(result.contains(testSandbox3+".mapping_regle"));
		assertTrue(result.contains(testSandbox3+".mod_variable_metier"));
		assertTrue(result.contains(testSandbox3+".mod_table_metier"));
		assertEquals(4, result.size());

		
		UtilitaireDao.get(0).executeImmediate(c, "INSERT INTO arc.ihm_nmcl (nom_table) values ('nmcl_code_pays_etranger_2023'), ('nmcl_sicore_2014'), ('nmcl_vs3');");
		UtilitaireDao.get(0).executeImmediate(c, "INSERT INTO "+testSandbox3+".mapping_regle (id_norme, regle, commentaire) values ('PHASE1V3', '{v0012}','relation avec la colonne nmcl_sicore_2014'), ('PHASE1V3', 'select codegeo from nmcl_code_pays_etranger_2023 where pays={v008}','relation avec la colonne nmcl_code_pays_etranger_2021');");
		UtilitaireDao.get(0).executeImmediate(c, "INSERT INTO "+testSandbox3+".chargement_regle (regle) values ('select * from arc.nmcl_vs3 where pcs={v001');");

		result = BddPatcher.retrieveExternalTablesUsedInRules(c, testSandbox3);
		
		UtilitaireDao.get(0).executeImmediate(c, "DROP SCHEMA IF EXISTS "+testSandbox3+" CASCADE;");
		assertTrue(result.contains("arc.nmcl_code_pays_etranger_2023"));
		assertTrue(result.contains("arc.nmcl_vs3"));
		assertTrue(result.contains(testSandbox3+".nmcl_vs3"));
		assertTrue(result.contains(testSandbox3+".nmcl_vs3"));
		assertEquals(4, result.size());

	}
	
//	@Test
//	public void retrieveExternalTablesUsedInRulesTest()
//	{
//		UtilitaireDao.get(0).executeImmediate(c, "DROP SCHEMA arc CASCADE;");
//		UtilitaireDao.get(0).executeImmediate(c, "DROP SCHEMA arc_bas2 CASCADE;");
//		UtilitaireDao.get(0).executeImmediate(c, "CREATE SCHEMA IF NOT EXISTS arc_bas2;");
//		
//		
//		ArrayList<String> result = BddPatcher.retrieveRulesTablesFromSchema(c, "arc_bas2");
//	}
	
}
