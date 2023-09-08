package fr.insee.arc.core.dataobjects;

import static org.junit.Assert.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

import java.util.List;

import org.junit.Test;

public class ViewEnumTest {

	@Test
	public void testConstructorFromUtil() {
		
		ViewEnum pgTables =	ViewEnum.PG_TABLES;
		
		List<String> cols= ColumnEnum.listColumnEnumByName(pgTables.getColumns().keySet());
		
		assertEquals("schemaname", cols.get(0));
		assertEquals("tablename", cols.get(1));
		assertEquals("pg_catalog", pgTables.getTableLocation().toString());
		
	}

}
