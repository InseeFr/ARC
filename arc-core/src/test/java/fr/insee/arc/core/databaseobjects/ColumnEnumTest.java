package fr.insee.arc.core.databaseobjects;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ColumnEnumTest {

	@Test
	public void listOfColumnEnumTest() {
		List<ColumnEnum> listOfColumnEnum= Arrays.asList(ColumnEnum.ID_FAMILLE,ColumnEnum.ID_NORME);
		List<String> listOfColumnEnumName= Arrays.asList("id_famille","id_norme");
		
		Assert.assertEquals(ColumnEnum.listColumnEnumByName(listOfColumnEnum), listOfColumnEnumName );
	}

}
