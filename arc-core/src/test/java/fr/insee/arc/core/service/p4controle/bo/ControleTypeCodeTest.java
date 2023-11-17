package fr.insee.arc.core.service.p4controle.bo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;

public class ControleTypeCodeTest {

	
	@Test
	public void getEnum_test_ok() throws ArcException {
		
		assertEquals(ControleTypeCode.NUM, ControleTypeCode.getEnum("NUM"));
	}

	@Test(expected = ArcException.class)
	public void getEnum_test_ko() throws ArcException {
		ControleTypeCode.getEnum("type de chargement inexistant");
	}

}
