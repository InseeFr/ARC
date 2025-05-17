package fr.insee.arc.core.service.global.thread;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class ThreadConstantTest {

	@Test
	public void testThreadConstantIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(ThreadConstant.class);
	}

}
