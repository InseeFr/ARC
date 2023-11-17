package fr.insee.arc.core.service.p3normage.bo;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;

public class TypeNormageTest {

	@Test
	public void getEnum_test_ok() throws ArcException {
		
		assertEquals(TypeNormage.RELATION, TypeNormage.getEnum("relation"));
	}

	@Test(expected = ArcException.class)
	public void getEnum_test_ko() throws ArcException {
		TypeNormage.getEnum("type de norme inexistant");
	}
		
}
