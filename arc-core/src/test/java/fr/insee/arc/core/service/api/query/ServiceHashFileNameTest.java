package fr.insee.arc.core.service.api.query;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class ServiceHashFileNameTest {

	@Test
	public void testUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(ServiceHashFileName.class);
	}

}
