package fr.insee.arc.core.dataobjects;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class ArcDatabaseTest extends InitializeQueryTest {

	@Test
	public void numberOfExecutorNods_testMultiWithExecutor() {
		u.getProperties().setDatabaseUrl("{0=>\"db1\"},{1=>\"db2\"}");
		u.getProperties().setDatabaseUsername("{0=>\"db1\"},{1=>\"db2\"}");
		u.getProperties().setDatabasePassword("{0=>\"db1\"},{1=>\"db2\"}");
		u.getProperties().setDatabaseDriverClassName("{0=>\"db1\"},{1=>\"db2\"}");

		// must return 1 nods
		assertEquals(1,ArcDatabase.numberOfExecutorNods());
	}
	
	
	
	@Test
	public void numberOfExecutorNods_testMultiNoExecutor() {
		// settings 1 database
		// 1 is coordinator
		// 2+ are executor
		u.getProperties().setDatabaseUrl("arc");
		u.getProperties().setDatabaseUsername("usr");
		u.getProperties().setDatabasePassword("pwd");
		u.getProperties().setDatabaseDriverClassName("pg");
				
		// must return 0 executor nods
		assertEquals(0,ArcDatabase.numberOfExecutorNods());
	}

}
