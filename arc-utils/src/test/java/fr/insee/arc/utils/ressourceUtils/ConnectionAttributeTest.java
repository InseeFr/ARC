package fr.insee.arc.utils.ressourceUtils;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ConnectionAttributeTest {

	@Test
	public void testUnserialize() {
    	List<String> zz;
    	
    	zz = Arrays.asList(ConnectionAttribute.unserialize("{0=>\"zzz\"},{1=>\"xxxx\"}"));
    	assertTrue(zz.contains("zzz"));
    	assertTrue(zz.contains("xxxx"));
		assertEquals(2, zz.size());

    	zz = Arrays.asList(ConnectionAttribute.unserialize("{0=>\"zzz\"}"));
    	assertTrue(zz.contains("zzz"));
		assertEquals(1, zz.size());
		
    	zz = Arrays.asList(ConnectionAttribute.unserialize("zzz=>"));
    	assertTrue(zz.contains("zzz=>"));
		assertEquals(1, zz.size());
    	
	}

}
