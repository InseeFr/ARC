package fr.insee.arc.utils.exception;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.junit.Rule;
import org.junit.Test;

import fr.insee.arc.utils.utils.LogAppenderResource;

public class ArcExceptionTest {

	@Rule
	public LogAppenderResource appender = new LogAppenderResource(LogManager.getLogger(ArcException.class));

	private final static File UNKNOWN_FILE = new File("f.test");
	private final static String EXPECTED_MESSAGE_JAVA_EXCEPTION = String
			.format("%s Le fichier %s n'a pas pu être effacé", Level.ERROR.toString(), UNKNOWN_FILE);
	private final static String EXPECTED_STACK_JAVA_EXCEPTION = String
			.format("java.nio.file.NoSuchFileException: %s", UNKNOWN_FILE);

	private final static String RUBRIQUE_NAME = "ma_rubrique";
	private final static String EXPECTED_MESSAGE_BUSINESS_EXCEPTION = String
			.format("%s La rubrique fille %s n'existe pas les règles de formatage", Level.ERROR.toString(), RUBRIQUE_NAME);
	private final static String EXPECTED_STACK_BUSINESS_EXCEPTION = String
			.format("java.lang.Throwable: La rubrique fille %s n'existe pas les règles de formatage", RUBRIQUE_NAME);
	
	
	@Test
	public void testExceptionLoggerShort() {

		try {
			simulateJavaException();
		} catch (ArcException e) {
			e.logMessageException();
		}

		// first line in logger must be message
		assertEquals(EXPECTED_MESSAGE_JAVA_EXCEPTION, appender.getOutput()[0]);
		// there musn't be other messages. No stack
		assertEquals(1, appender.getOutput().length);
	}

	@Test
	public void testExceptionLoggerFull() {

		try {
			simulateJavaException();
		} catch (ArcException e) {
			e.logFullException();
		}

		// first line in logger must be message
		assertEquals(EXPECTED_MESSAGE_JAVA_EXCEPTION, appender.getOutput()[0]);
		// second line in logger must be the stack trace
		assertEquals(EXPECTED_STACK_JAVA_EXCEPTION, appender.getOutput()[1]);
	}

	/**
	 * simulate a java exception by trying to delete a non existing file
	 * 
	 * @throws ArcException
	 */
	public void simulateJavaException() throws ArcException {

		try {
			Files.delete(UNKNOWN_FILE.toPath());
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_DELETE_FAILED, UNKNOWN_FILE.getName());
		}
	}

	/**
	 * simulate a business exception by trying to delete a non existing file
	 * 
	 * @throws ArcException
	 */
	public void simulateBusinessException() throws ArcException {
		throw new ArcException(ArcExceptionMessage.LOAD_KEYVALUE_VAR_NOT_EXISTS_IN_FORMAT_RULES, "ma_rubrique");
	}

	@Test
	public void testBusinessExceptionLoggerShort() {
		try {
			simulateBusinessException();
		} catch (ArcException e) {
			e.logMessageException();
		}

		// first line in logger must be message
		assertEquals(EXPECTED_MESSAGE_BUSINESS_EXCEPTION, appender.getOutput()[0]);
		// there musn't be other messages. No stack
		assertEquals(1, appender.getOutput().length);
	}

	@Test
	public void testBusinessExceptionLoggerFull() {
		try {
			simulateBusinessException();
		} catch (ArcException e) {
			e.logFullException();
		}

		System.out.println(appender.getOutputAsString());
		
		// first line in logger must be message
		assertEquals(EXPECTED_MESSAGE_BUSINESS_EXCEPTION, appender.getOutput()[0]);
		// second line in logger must be the stack trace
		assertEquals(EXPECTED_STACK_BUSINESS_EXCEPTION, appender.getOutput()[1]);
	}
	
	
}
