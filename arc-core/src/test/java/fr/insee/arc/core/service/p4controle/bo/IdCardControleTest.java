package fr.insee.arc.core.service.p4controle.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class IdCardControleTest {

	@Test
	public void getReglesControleByControleTypeCode() {
		
		List<RegleControle> reglesControle = new ArrayList<>();
		reglesControle.add(new RegleControle(ControleTypeCode.ALPHANUM, null, null, null, null, null, null, 0, null, null));
		reglesControle.add(new RegleControle(ControleTypeCode.ALPHANUM, null, null, null, null, null, null, 1, null, null));
		reglesControle.add(new RegleControle(ControleTypeCode.CARDINALITE, null, null, null, null, null, null, 2, null, null));
		reglesControle.add(new RegleControle(ControleTypeCode.ALPHANUM, null, null, null, null, null, null, 3, null, null));

		IdCardControle idCardControle = new IdCardControle(reglesControle);
		
		List<RegleControle> reglesControleALPHANUM = idCardControle.getReglesControle(ControleTypeCode.ALPHANUM);
		assertEquals(3, reglesControleALPHANUM.size());

	}

}
