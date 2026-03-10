package fr.insee.arc.core.service.p2chargement.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;

public class TypeChargementTest {


	@Test
	public void getEnum_test_ok() throws ArcException {
		
		assertEquals(TypeChargement.XML_COMPLEXE, TypeChargement.getEnum("xml-complexe"));
	}

	@Test(expected = ArcException.class)
	public void getEnum_test_ko() throws ArcException {
		TypeChargement.getEnum("type de chargement inexistant");
	}
		
}
