package fr.insee.arc.batch.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class BatchArcDaoTest extends InitializeQueryTest {

	private String sandbox = "arc_bas1";
	private String tablePilotageBatch=ViewEnum.PILOTAGE_BATCH.getFullName(sandbox);
	
	@Before
	public void initDatabaseBeforeTest() throws SQLException, ArcException {
		buildPropertiesWithoutScalability("tmp");

		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE");
		u.executeRequest(c, "CREATE SCHEMA arc_bas1");

		
		u.executeRequest(c, "DROP TABLE IF EXISTS " + tablePilotageBatch);
		u.executeRequest(c, "CREATE TABLE " + tablePilotageBatch + " (last_init text, operation text)");
		u.executeRequest(c, "INSERT INTO " + tablePilotageBatch + " select '2024-01-01:22','O'");
		
	}

	

	@After
	public void cleanDatabaseAfterTest() throws SQLException, ArcException {
		// clear
		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeRequest(c, "DISCARD TEMP;");
	}
	
	
	@Test
	/**
	 * Test the time of the next initialisation phase in batch
	 * @throws ArcException
	 * @throws ParseException
	 */
	public void BatchActiveTest() throws ArcException, ParseException {
		BatchArcDao dao = new BatchArcDao(c);
		assertTrue(dao.execQueryIsProductionOn(sandbox, true));

		u.executeRequest(c, "UPDATE "+ tablePilotageBatch + " set operation = 'N'");
		assertFalse(dao.execQueryIsProductionOn(sandbox, true));
		assertFalse(dao.execQueryIsResetRequired(sandbox));

		u.executeRequest(c, "UPDATE "+ tablePilotageBatch + " set operation = 'OR'");
		assertTrue(dao.execQueryIsProductionOn(sandbox, true));
		assertTrue(dao.execQueryIsResetRequired(sandbox));

		u.executeRequest(c, "UPDATE "+ tablePilotageBatch + " set operation = 'O-'");
		assertTrue(dao.execQueryIsProductionOn(sandbox, true));
		assertFalse(dao.execQueryIsResetRequired(sandbox));
		
		u.executeRequest(c, "UPDATE "+ tablePilotageBatch + " set operation = 'NR'");
		assertFalse(dao.execQueryIsProductionOn(sandbox, true));
		assertTrue(dao.execQueryIsResetRequired(sandbox));

	}
	
	
	@Test
	/**
	 * Test the time of the next initialisation phase in batch
	 * @throws ArcException
	 * @throws ParseException
	 */
	public void initialisationTimerTest() throws ArcException, ParseException {
		
		
		BatchArcDao dao = new BatchArcDao(c);

		// retrieve the next initialisation timestamp
		String nextInitialisationTimestamp = dao.execQueryNextInitialisationTimestamp(sandbox);
		assertEquals("2024-01-01:22", nextInitialisationTimestamp);
		
		// adding 7 days the current_date and setting the hour of initialisation to 15
		dao.execUpdateLastInitialisationTimestamp(sandbox, 7 , 15);
		// getting new timestamp
		nextInitialisationTimestamp = dao.execQueryNextInitialisationTimestamp(sandbox);
		Date dnextInitialize = new SimpleDateFormat(ArcDateFormat.DATE_HOUR_FORMAT_CONVERSION.getApplicationFormat())
				.parse(nextInitialisationTimestamp);
		// must be higher than current Date
		assertEquals(1, dnextInitialize.compareTo(new Date()));
		// hour must be set to 15
		assertEquals("15", nextInitialisationTimestamp.substring(nextInitialisationTimestamp.indexOf(":")+1));
		
		assertFalse(dao.isInitializationMustTrigger(sandbox));
		
		// test if batch if on
		assertTrue(dao.execQueryIsProductionOn(sandbox,true));
		
	}

}
