package fr.insee.arc.utils.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class FormatSQLTest extends InitializeQueryTest {

	String tableIn = "tableIn";
	String tableInPublic = "public.tableIn";

	@Test
	public void tableExists_true() throws ArcException {

		// test for table not exists
		assertFalse(UtilitaireDao.get("arc").isTableExiste(c, tableIn));

		// test for temporary table
		UtilitaireDao.get("arc").executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableIn + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");
		assertTrue(UtilitaireDao.get("arc").isTableExiste(c, tableIn));
		UtilitaireDao.get("arc").dropTable(c, tableIn);

		// test for schema table
		UtilitaireDao.get("arc").executeImmediate(c,
				"CREATE TABLE " + tableInPublic + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");
		assertTrue(UtilitaireDao.get("arc").isTableExiste(c, tableInPublic));
		UtilitaireDao.get("arc").dropTable(c, tableInPublic);
	}

	@Test
	public void changeRole_Test() throws ArcException {

		String myTestRole = "arc";

		// create a role
		UtilitaireDao.get("arc").executeImmediate(c, "CREATE ROLE " + myTestRole + " with NOINHERIT;");

		// change role test
		UtilitaireDao.get("arc").executeImmediate(c, FormatSQL.changeRole(myTestRole));

		// check the current role used
		GenericPreparedStatementBuilder testQuery = new GenericPreparedStatementBuilder(
				"SELECT current_user as current_role");
		HashMap<String, ArrayList<String>> content = new GenericBean(UtilitaireDao.get("arc").executeRequest(c, testQuery)).mapContent(true);

		assertEquals(myTestRole, content.get("current_role").get(0));
	}

	@Test
	public void listeColonneByHeaders_Test() throws ArcException {
		// create a test table
		UtilitaireDao.get("arc").executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableIn + " as SELECT i as col_1, i as col_2 FROM generate_series(1,5) i");

		// execute query
		List<String> columns = UtilitaireDao.get("arc").getColumns(c, tableIn);

		// check if the headers are found and ok
		assertEquals(2, columns.size());
		assertEquals("col_1", columns.get(0));
		assertEquals("col_2", columns.get(1));

		UtilitaireDao.get("arc").dropTable(c, tableIn);
	}

	@Test
	public void hasRecord_TableWithRecords() throws ArcException {
		// create a non empty table
		UtilitaireDao.get("arc").executeImmediate(c, "CREATE TEMPORARY TABLE " + tableIn + " as SELECT i FROM generate_series(1,5) i");

		// execute the query "hasRecord"
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder(FormatSQL.hasRecord(tableIn));
		HashMap<String, ArrayList<String>> content = new GenericBean(UtilitaireDao.get("arc").executeRequest(c, query)).mapContent(true);

		// result must be true ('t' in postgres)
		assertEquals("t", content.get("has_record").get(0));
		UtilitaireDao.get("arc").dropTable(c, tableIn);

	}

	@Test
	public void hasRecord_TableWithoutRecords() throws ArcException {

		// create an empty table
		UtilitaireDao.get("arc").executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableIn + " as SELECT i FROM generate_series(1,5) i WHERE false");

		// execute the query "hasRecord"
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder(FormatSQL.hasRecord(tableIn));
		HashMap<String, ArrayList<String>> content = new GenericBean(UtilitaireDao.get("arc").executeRequest(c, query)).mapContent(true);

		// result must be false ('f' in postgres)
		assertEquals("f", content.get("has_record").get(0));
		UtilitaireDao.get("arc").dropTable(c, tableIn);
	}

	@Test
	public void rebuildTableAsSelectWhere() throws ArcException {

		String indexCreationQuery = "CREATE index idx1_test_index on " + tableInPublic + " (i);";

		// create a table with an index
		UtilitaireDao.get("arc").executeImmediate(c,
				"CREATE TABLE " + tableInPublic + " as SELECT i FROM generate_series(1,20) i");
		UtilitaireDao.get("arc").executeImmediate(c, indexCreationQuery);

		// execute the rebuild with a where condition
		UtilitaireDao.get("arc").executeImmediate(c, FormatSQL.rebuildTableAsSelectWhere(tableInPublic, "i<=15", indexCreationQuery));

		// test
		// the table must exists and should have only 15 records left
		testMetadataAndNumberOfRecords(tableInPublic, 15, new String[] { "i" });

	}

}
