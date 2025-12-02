package fr.insee.arc.core.service.p4controle.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;

public class ControleTypeCodeTest {

	@Test
	public void getEnum_test_ok() throws ArcException {

		assertEquals(ControleTypeCode.NUM, ControleTypeCode.getEnum("NUM"));
	}

	@Test
	public void getEnum_test_ko() throws ArcException {
		assertThrows(ArcException.class, () -> {
			ControleTypeCode.getEnum("type de chargement inexistant");
		});
	}

}
