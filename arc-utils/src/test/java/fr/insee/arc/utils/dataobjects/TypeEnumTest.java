package fr.insee.arc.utils.dataobjects;

import static org.junit.Assert.*;

import org.junit.Test;

public class TypeEnumTest {

	@Test
	public void realNameOf() {
		assertEquals(TypeEnum.TEXT, TypeEnum.realNameOf("text"));
		assertEquals(TypeEnum.TEXT,TypeEnum.realNameOf("unkown type"));
		assertEquals(TypeEnum.INTEGER, TypeEnum.realNameOf("int4"));
		assertEquals(TypeEnum.DATE, TypeEnum.realNameOf("date"));
	}

}
