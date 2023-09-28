package fr.insee.arc.core.service.global.dao;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class ServiceHashFileNameTest {

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(HashFileNameConversion.class);
	}
	
	@Test(expected = ArcException.class)
	public void HashOfIdSourceWithWrongAlgorithm() throws ArcException
	{
		HashFileNameConversion.hashOfIdSource("my_file.xml", "UNKNOWN_ALGORITHM");
	}

}
