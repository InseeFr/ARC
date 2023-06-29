package fr.insee.arc.core.dataobjects;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.utils.query.InitializeQueryTest;

public class ArcDatabaseTest extends InitializeQueryTest {

	@Test
	public void numberOfExecutorNods_testMultiWithExecutor() {
		// settings 4 database
		// 1 is meta data
		// 2 is coordinator
		// 3+ are executor
		u.getProperties().setDatabaseUsername("arc|||arc|||arc|||arc");
		
		// must return 2 executor nods
		assertEquals(2,ArcDatabase.numberOfExecutorNods());
	}
	
	@Test
	public void numberOfExecutorNods_testMultiJustCoodinator() {
		// settings 4 database
		// 1 is meta data
		// 2 is coordinator
		// 3+ are executor
		u.getProperties().setDatabaseUsername("arc|||arc");
		
		// must return 0 executor nods
		assertEquals(0,ArcDatabase.numberOfExecutorNods());
	}
	
	@Test
	public void numberOfExecutorNods_testMultiJustExecutor() {
		// settings 4 database
		// 1 is meta data
		// 2 is coordinator
		// 3+ are executor
		u.getProperties().setDatabaseUsername("arc");
		
		// must return 0 executor nods
		assertEquals(0,ArcDatabase.numberOfExecutorNods());
	}

}
