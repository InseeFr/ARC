package fr.insee.arc.utils.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArcExceptionMessageTest {

	@Test
	public void testEnum() {
		assertEquals("PARALLEL_INSERT_THREAD_FAILED",ArcExceptionMessage.PARALLEL_INSERT_THREAD_FAILED.name());
	}

}
