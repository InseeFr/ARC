package fr.insee.arc.core.model;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class BatchModeTest {

	@Test
	public void testBatchModeIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(BatchMode.class);
	}
	
	@Test
	public void computeBatchModeTest() throws ArcException
	{
		assertEquals(BatchMode.UNSET, BatchMode.computeBatchMode(false,true));
		assertEquals(BatchMode.UNSET, BatchMode.computeBatchMode(false,false));
		assertEquals(BatchMode.NORMAL, BatchMode.computeBatchMode(true,false));
		assertEquals(BatchMode.KEEP_INTERMEDIATE_DATA, BatchMode.computeBatchMode(true,true));
	}


}
