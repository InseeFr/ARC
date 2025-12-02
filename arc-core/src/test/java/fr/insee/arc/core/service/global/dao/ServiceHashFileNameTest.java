package fr.insee.arc.core.service.global.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class ServiceHashFileNameTest {

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(HashFileNameConversion.class);
	}
	
	@Test
	public void HashOfIdSourceWithWrongAlgorithm() throws ArcException
	{
		assertThrows(ArcException.class, () -> {
			HashFileNameConversion.hashOfIdSource("my_file.xml", "UNKNOWN_ALGORITHM");
		});
	}

}
