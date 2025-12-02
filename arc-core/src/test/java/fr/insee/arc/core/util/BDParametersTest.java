package fr.insee.arc.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LogAppenderResource;

public class BDParametersTest {

	public static Connection c;
	public static Connection nullConnexion;
	public static UtilitaireDao u;

	@RegisterExtension
	public LogAppenderResource appender = new LogAppenderResource(LogManager.getLogger(BDParameters.class));

	@BeforeAll
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		c = BddPatcherTest.c;
		u = BddPatcherTest.u;
	}

	@Test
	public void insertDefaultValue_Exception() {
        BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);
		
        bdParameters.insertDefaultValue(c, "'a", "10");
		assertTrue(appender.getOutputAsString().startsWith("ERROR"));
	}
	
	@Test
	public void getString_TestDefaultValue() throws ArcException {
		
		String testKey="test.key";
		String testValue="test.value";
		
		// insert the key value into the parameter table
        BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);
        bdParameters.getString(c, testKey, testValue);
		
		// check if the parameter had been well registered in the parameter table
		ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "val", SQL.FROM, new DataObjectService().getView(ViewEnum.PARAMETER), SQL.WHERE, "key=", query.quoteText(testKey));
		String expectedValue = u.getString(c, query);
		
		assertEquals(expectedValue, testValue);
	}

	
}


