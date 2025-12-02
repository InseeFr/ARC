package fr.insee.arc.utils.textUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class XMLUtilTest extends XMLUtil {
	
	@Test
	/*
	 * commn usecase; return value between tags
	 */
	public void parseXML1() {
		assertEquals("2<9 is true",parseXML("<my_tag>2<9 is true</my_tag>","my_tag"));
	}

	@Test
	/*
	 * refer only to the first tag; closing tag is optional
	 */
	public void parseXML2() {
		assertEquals("2<9 is true",parseXML("<my_tag>2<9 is true","my_tag"));
	}
	
	
	@Test
	/**
	 * if input null, return null
	 */
	public void parseXMLnull() {
		assertEquals(null,parseXML(null,"my_tag"));
	}
	
}
