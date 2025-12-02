package fr.insee.arc.core.service.p3normage.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;

public class TypeNormageTest {

	@Test
	public void getEnum_test_ok() throws ArcException {

		assertEquals(TypeNormage.RELATION, TypeNormage.getEnum("relation"));
	}

	@Test
	public void getEnum_test_ko() throws ArcException {
		assertThrows(ArcException.class, () -> {
			TypeNormage.getEnum("type de norme inexistant");
		});
	}

}
