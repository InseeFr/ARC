package fr.insee.arc.utils.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class FormatSQLTest extends InitializeQueryTest {

	String tableIn = "tableIn";

	@Test
	public void hasRecord_TableWithRecords() throws ArcException {
		// create a non empty table
		u.executeImmediate(c, "CREATE TEMPORARY TABLE " + tableIn + " as SELECT i FROM generate_series(1,5) i");

		// execute the query "hasRecord"
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder(FormatSQL.hasRecord(tableIn));
		HashMap<String, ArrayList<String>> content = new GenericBean(u.executeRequest(c, query)).mapContent(true);

		// result must be true ('t' in postgres)
		assertEquals("t", content.get("has_record").get(0));
		u.dropTable(c, tableIn);

	}

	public void hasRecord_TableWithoutRecords() throws ArcException {

		// create an empty table
		u.executeImmediate(c,
				"CREATE TEMPORARY TABLE " + tableIn + " as SELECT i FROM generate_series(1,5) i WHERE false");

		// execute the query "hasRecord"
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder(FormatSQL.hasRecord(tableIn));
		HashMap<String, ArrayList<String>> content = new GenericBean(u.executeRequest(c, query)).mapContent(true);

		// result must be false ('f' in postgres)
		assertEquals("f", content.get("has_record").get(0));
		u.dropTable(c, tableIn);
	}

}
