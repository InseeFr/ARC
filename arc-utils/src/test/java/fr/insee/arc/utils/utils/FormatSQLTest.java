package fr.insee.arc.utils.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class FormatSQLTest extends InitializeQueryTest {

	String tableInTemporary = "tableIn";
	String tableInPublic = "public.tableIn";

	/**
	 * Test the query that drop table cascade
	 * @throws ArcException
	 */
	@Test
	public void dropTable() throws ArcException
	{
		// create table
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TABLE " + tableInPublic + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, tableInPublic));
		
		
		// test the query
		UtilitaireDao.get(0).executeImmediate(c, FormatSQL.dropTable(tableInPublic));
		// the table must have been drop
		assertFalse(UtilitaireDao.get(0).isTableExiste(c, tableInPublic));
	}
	
	/**
	 * Test the method that check if a table or a temporary exists in database
	 * @throws ArcException
	 */
	@Test
	public void tableExists_true() throws ArcException {

		// test for table not exists
		assertFalse(UtilitaireDao.get(0).isTableExiste(c, tableInTemporary));

		// test for temporary table
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableInTemporary + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, tableInTemporary));
		UtilitaireDao.get(0).dropTable(c, tableInTemporary);

		// test for schema table
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TABLE " + tableInPublic + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, tableInPublic));
		UtilitaireDao.get(0).dropTable(c, tableInPublic);
	}

	@Test
	public void changeRole_Test() throws ArcException {

		String myTestRole = "arc";

		// create a role
		UtilitaireDao.get(0).executeImmediate(c, "CREATE ROLE " + myTestRole + " with NOINHERIT;");

		// change role test
		UtilitaireDao.get(0).executeImmediate(c, FormatSQL.changeRole(myTestRole));

		// check the current role used
		GenericPreparedStatementBuilder testQuery = new GenericPreparedStatementBuilder(
				"SELECT current_user as current_role");
		Map<String, List<String>> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, testQuery)).mapContent(true);

		assertEquals(myTestRole, content.get("current_role").get(0));
	}

	@Test
	public void listeColonneByHeaders_Test() throws ArcException {
		// create a test table
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableInTemporary + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");

		// execute query
		List<String> columns = UtilitaireDao.get(0).getColumns(c, tableInTemporary);

		// check if the headers are found and ok
		assertEquals(2, columns.size());
		assertEquals("col_1", columns.get(0));
		assertEquals("col_2", columns.get(1));

		UtilitaireDao.get(0).dropTable(c, tableInTemporary);
	}

	@Test
	public void hasRecord_TableWithRecords() throws ArcException {
		// create a non empty table
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TEMPORARY TABLE " + tableInTemporary + " as SELECT i FROM generate_series(1,5) i");

		// execute the query "hasRecord"
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder(FormatSQL.hasRecord(tableInTemporary));
		Map<String, List<String>> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent(true);

		// result must be true ('t' in postgres)
		assertEquals("t", content.get("has_record").get(0));
		UtilitaireDao.get(0).dropTable(c, tableInTemporary);

	}

	@Test
	public void hasRecord_TableWithoutRecords() throws ArcException {

		// create an empty table
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableInTemporary + " as SELECT i FROM generate_series(1,5) i WHERE false");

		// execute the query "hasRecord"
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder(FormatSQL.hasRecord(tableInTemporary));
		Map<String, List<String>> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent(true);

		// result must be false ('f' in postgres)
		assertEquals("f", content.get("has_record").get(0));
		UtilitaireDao.get(0).dropTable(c, tableInTemporary);
	}

	@Test
	public void rebuildTableAsSelectWhere() throws ArcException {
		rebuildTableAsSelectWhere(tableInPublic);
		rebuildTableAsSelectWhere(tableInTemporary);
	}
	
	/**
	 * test that rebuildTable give a good copy of the table
	 * @param aTable
	 * @throws ArcException
	 */
	private static void rebuildTableAsSelectWhere(String aTable) throws ArcException
	{
		// create a table with an index
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TABLE " + aTable + " as SELECT i FROM generate_series(1,20) i");

		// execute the rebuild with a where condition
		UtilitaireDao.get(0).executeImmediate(c, FormatSQL.rebuildTableAsSelectWhere(aTable, "i<=15"));

		// test
		// the table must exists and should have only 15 records left
		testMetadataAndNumberOfRecords(aTable, 15, new String[] { "i" });
		
		UtilitaireDao.get(0).dropTable(c, aTable);
	}

	@Test
	public void analyzeAndVacuumSecuredTest() throws ArcException
	{
		UtilitaireDao.get(0).executeImmediate(c,
				"CREATE TABLE " + tableInPublic + " as SELECT i FROM generate_series(1,5) i WHERE false");

		boolean maintenanceSucess=true;
		
		// test if maintenance is a success
		try {
			UtilitaireDao.get(0).executeImmediate(c,FormatSQL.vacuumSecured(tableInPublic,"full"));
			UtilitaireDao.get(0).executeImmediate(c,FormatSQL.analyzeSecured(tableInPublic));
		} catch (ArcException e) {
			maintenanceSucess=false;
		}
		
		assertTrue(maintenanceSucess);
		
		UtilitaireDao.get(0).dropTable(c, tableInPublic);

	}
	
	@Test
	 public void extractSchemaNameTokenTest()
    {
		assertEquals("arc", FormatSQL.extractSchemaNameToken("arc.zzz"));
		assertNull(FormatSQL.extractSchemaNameToken("zzz"));
    }
	
	@Test
	 public void extractTableNameTokenTest()
	 {
		assertEquals("zzz", FormatSQL.extractTableNameToken("arc.zzz"));
		assertEquals("zzz", FormatSQL.extractTableNameToken("zzz"));	
	 }
	
	
}
