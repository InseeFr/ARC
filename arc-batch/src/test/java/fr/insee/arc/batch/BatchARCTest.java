package fr.insee.arc.batch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.query.TestDatabase;

class BatchARCTest extends InitializeQueryTest {

	@Test
	public void waitDatabaseNodsToPopUpTestTimeOut() throws ArcException, SQLException
	{
		// BatchArc will wait for 1 second max that all database are connectable
		BatchARC b = new BatchARC();
		b.setWaitExecutorTimerInMS(2000);

		e1 = new TestDatabase().testConnection;
		e2 = new TestDatabase().testConnection;
		buildProperties("tmp", new Connection[] {c, e1, e2});

		// it should succeed
		assertDoesNotThrow(() -> b.waitDatabaseNodsToPopUp());
		
		// computing a false uri for the nod 2
		String uri=u.getProperties().getDatabaseUrl();
		String[] tokens = uri.split(",");
		String falseUri =  tokens[0] +"," + tokens[1] + "," + tokens[2].replace("/postgres","/unknown");

		
		// falseuri is used as database uri of nod 2, it should timeout
		u.getProperties().setDatabaseUrl(falseUri);

		
		// database on nod 2 should not be connectable, so an exception is raised 
		assertThrows(ArcException.class,() -> {b.waitDatabaseNodsToPopUp();});

		
	}
	
	
}
