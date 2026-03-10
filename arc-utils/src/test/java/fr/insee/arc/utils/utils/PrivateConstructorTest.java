package fr.insee.arc.utils.utils;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class PrivateConstructorTest {

	public static void testConstructorIsPrivate(final Class<?> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
	    Constructor<?> constructor = clazz.getDeclaredConstructor();
	    assertTrue(Modifier.isPrivate(constructor.getModifiers())); //this tests that the constructor is private
	    constructor.setAccessible(true);
	    assertThrows(InvocationTargetException.class, () -> {
	        constructor.newInstance();
	    }); //this add the full coverage on private constructor
	}
	
	@Test(expected = AssertionError.class)
	public void testConstructorIsPrivateTest() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		testConstructorIsPrivate(PrivateConstructorTest.class);
	}
	
}
