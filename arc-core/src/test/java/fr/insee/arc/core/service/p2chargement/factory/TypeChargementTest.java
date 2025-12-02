package fr.insee.arc.core.service.p2chargement.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;

public class TypeChargementTest {

	@Test
	public void getEnum_test_ok() throws ArcException {

		assertEquals(TypeChargement.XML_COMPLEXE, TypeChargement.getEnum("xml-complexe"));
	}

	@Test
	public void getEnum_test_ko() throws ArcException {
		assertThrows(ArcException.class, () -> {
			TypeChargement.getEnum("type de chargement inexistant");
		});
	}

}
