package fr.insee.arc.core.dataobjects;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.utils.query.InitializeQueryTest;

public class ArcDatabaseTest extends InitializeQueryTest {

	@Test
	public void numberOfExecutorNods_testMultiWithExecutor() {
		// settings 4 database
		// 1 is coordinator
		// 2+ are executor
		u.getProperties().setDatabaseUsername("arc|||arc|||arc|||arc");
		
		// must return 3 executor nods
		assertEquals(3,ArcDatabase.numberOfExecutorNods());
	}
	
	@Test
	public void numberOfExecutorNods_testMultiJustExecutor() {
		// settings 1 database
		// 1 is coordinator
		// 2+ are executor
		u.getProperties().setDatabaseUsername("arc");
		
		// must return 0 executor nods
		assertEquals(0,ArcDatabase.numberOfExecutorNods());
	}

}
