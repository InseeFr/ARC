package fr.insee.arc_essnet.utils.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class FormatSQLTest {

    @Test
    public void testSimpleSelectRequestNominal() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";
	Boolean isAsc = true;
	Integer aLimit = 10;
	Integer anOffset = 0;
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.simpleSelectRequest(someCols, atableName, anOrderByColumn, isAsc,
		aLimit, anOffset, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"LIMIT 10");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"OFFSET 0");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testSimpleSelectRequestNominalDesc() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";
	Boolean isAsc = false;
	Integer aLimit = 10;
	Integer anOffset = 0;
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.simpleSelectRequest(someCols, atableName, anOrderByColumn, isAsc,
		aLimit, anOffset, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo DESC");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"LIMIT 10");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"OFFSET 0");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testSimpleSelectRequestAllNull() {
	// GIVEN

	// WHEN
	StringBuilder actualRequest = FormatSQL.simpleSelectRequest(null, null, null, null, null, null, null);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM null");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetAllReccordsFromATableWithOnlyTableName() throws Exception {
	// GIVEN
	String atableName = "test";

	// WHEN
	StringBuilder actualRequest = FormatSQL.getAllReccordsFromATable(atableName);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetAllReccordsFromATableAscOrderWithTableNameAndOrderColumn() throws Exception {
	// GIVEN
	String atableName = "test";
	String anOrderByColumn = "foo";

	// WHEN
	StringBuilder actualRequest = FormatSQL.getAllReccordsFromATableAscOrder(atableName, anOrderByColumn);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetAllReccordsFromATableDescOrderWithTableNameAndOrderColumn() throws Exception {
	// GIVEN
	String atableName = "test";
	String anOrderByColumn = "foo";

	// WHEN
	StringBuilder actualRequest = FormatSQL.getAllReccordsFromATableDescOrder(atableName, anOrderByColumn);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo DESC");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATableWithTableNameAndConditions() throws Exception {
	// GIVEN
	String atableName = "test";

	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATable(atableName, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATableAscOrderedWithTableNameOrderColumnAndConditions() throws Exception {
	// GIVEN
	String atableName = "test";
	String anOrderByColumn = "foo";
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATableAscOrdered(atableName, anOrderByColumn,
		someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATableDescOrderedStringWithTableNameOrderColumnAndConditions() throws Exception {
	// GIVEN
	String atableName = "test";
	String anOrderByColumn = "foo";
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATableDescOrdered(atableName, anOrderByColumn,
		someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo DESC");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATableWithCondiion() throws Exception {
	// GIVEN
	String atableName = "test";
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATable(atableName, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT *");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testSimpleSelectRequest() throws Exception {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";
	Boolean isAsc = true;
	Integer aLimit = 10;
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.simpleSelectRequest(someCols, atableName, anOrderByColumn, isAsc,
		aLimit, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"LIMIT 10");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetAllReccordsFromATable() throws Exception {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";

	// WHEN
	StringBuilder actualRequest = FormatSQL.getAllReccordsFromATable(someCols, atableName);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetAllReccordsFromATableAscOrder() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";

	// WHEN
	StringBuilder actualRequest = FormatSQL.getAllReccordsFromATableAscOrder(someCols, atableName, anOrderByColumn);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetAllReccordsFromATableDescOrder() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";

	// WHEN
	StringBuilder actualRequest = FormatSQL.getAllReccordsFromATableDescOrder(someCols, atableName,
		anOrderByColumn);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo DESC");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATable() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";

	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATable(someCols, atableName, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATableAscOrdered() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATableAscOrdered(someCols, atableName,
		anOrderByColumn, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testGetSomeReccordFromATableDescOrdered() {
	// GIVEN
	Collection<String> someCols = Arrays.asList("foo", "bar", "baz");
	String atableName = "test";
	String anOrderByColumn = "foo";
	String[] someCondition = { "foo =1", "baz like %toto%" };

	// WHEN
	StringBuilder actualRequest = FormatSQL.getSomeReccordFromATableDescOrdered(someCols, atableName,
		anOrderByColumn, someCondition);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("\n SELECT foo , bar , baz");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"FROM test");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"WHERE foo =1 AND baz like %toto%");
	expectedRequest.append(FormatSQL.NEWLINE_TABULATION+"ORDER BY foo DESC");

	assertEquals(expectedRequest.toString(), actualRequest.toString());
    }

    @Test
    public void testAddCommentaryHeaderToQuery() throws Exception {
	// GIVEN
	String query = "query";
	String header = "header";

	// WHEN
	String actualRequest = FormatSQL.addCommentaryHeaderToQuery(query, header);

	// THEN
	StringBuilder expectedRequest = new StringBuilder();
	expectedRequest.append("/*header*/");
	expectedRequest.append("\nquery");


	assertEquals(expectedRequest.toString(), actualRequest);
    }

}
