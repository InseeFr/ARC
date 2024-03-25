package fr.insee.arc.utils.parquet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class ParquetDaoTest extends ParquetDao {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void attachmentNameTest() {
		assertEquals("pg_0", attachmentName(0));
		assertEquals("pg_1", attachmentName(1));
	}

	@Test
	public void attachmentTableNameTest() {
		assertEquals("pg_0.arc_bas1.ma_table", attachedTableName(0, "arc_bas1.ma_table"));
	}

	@Test
	public void exportParquetTestOnExecutor() throws SQLException, IOException, ArcException {
		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();

		InitializeQueryTest.buildPropertiesWithTwoExecutors(repertoire);

		String testTable1 = "public.test_table1";
		// create a test table on coordinator
		createTestTable(InitializeQueryTest.c, testTable1);

		// create a test table on executor 1
		String testTable2 = "public.test_table2";
		createTestTable(InitializeQueryTest.e1, testTable2);
		// create a test table on executor 1
		createTestTable(InitializeQueryTest.e2, testTable2);

		
		// directory is empty
		assertEquals(0,root.listFiles().length);
		
		new ParquetDao().exportToParquet(Arrays.asList(new TableToRetrieve(ArcDatabase.COORDINATOR, testTable1),
				new TableToRetrieve(ArcDatabase.EXECUTOR, testTable2)), repertoire, null);

		
		List<String> f = Arrays.asList(root.listFiles()).stream().map(t->t.getName()).toList();
		assertEquals(2, f.size());
		assertTrue(f.contains("test_table1.parquet"));
		assertTrue(f.contains("test_table2.parquet"));

	}

	/**
	 * Create a test table on target connection with several common types of data
	 * 
	 * @param connection
	 * @param testTableName
	 * @throws ArcException
	 */
	private void createTestTable(Connection connection, String testTableName) throws ArcException {

		// test query on
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.build(SQL.CREATE, SQL.TABLE, testTableName, SQL.AS, SQL.SELECT);
		query.append("'string'::text as column_string");
		query.append(",12::int as column_int4");
		query.append(",123::bigint as column_int8");
		query.append(",current_timestamp as column_timestamp");
		query.append(",current_date as column_date");
		query.append(",array['c11','c12']::text[] as column_array_text");
		query.append(",array[1, 2, 3]::int[] as column_array_int4");
		query.append(",array[8, 9, 10]::int[] as column_array_int8");
		query.append(",array[current_timestamp, current_timestamp] as column_array_timestamp");
		query.append(",array[current_date, current_date] as column_array_date");

		UtilitaireDao.get(0).executeImmediate(connection, query);
	}

}