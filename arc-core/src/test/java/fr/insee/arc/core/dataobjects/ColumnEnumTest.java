package fr.insee.arc.core.dataobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ColumnEnumTest {

	@Test
	public void listOfColumnEnumTest() {
		List<ColumnEnum> listOfColumnEnum = Arrays.asList(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME);
		List<String> listOfColumnEnumName = Arrays.asList("id_famille", "id_norme");

		assertEquals(ColumnEnum.listColumnEnumByName(listOfColumnEnum), listOfColumnEnumName);
	}

}
