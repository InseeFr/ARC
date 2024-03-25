package fr.insee.arc.utils.parquet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParquetDaoTest extends ParquetDao {

	@Test
	public void attachmentNameTest() {
		assertEquals("pg_0", attachmentName(0));
		assertEquals("pg_1", attachmentName(1));
	}

	@Test
	public void attachmentTableNameTest() {
		assertEquals("pg_0.arc_bas1.ma_table", attachedTableName(0, "arc_bas1.ma_table"));
	}

}