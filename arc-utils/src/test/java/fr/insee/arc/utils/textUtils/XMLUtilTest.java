package fr.insee.arc.utils.textUtils;

import static org.junit.Assert.*;

import org.junit.Test;

public class XMLUtilTest extends XMLUtil {
	
	@Test
	public void parseXML1() {
		assertEquals("2<9 is true",parseXML("<my_tag>2<9 is true</my_tag>","my_tag"));
	}

	@Test
	public void parseXML2() {
		assertEquals("2<9 is true",parseXML("<my_tag>2<9 is true","my_tag"));
	}
	
}
