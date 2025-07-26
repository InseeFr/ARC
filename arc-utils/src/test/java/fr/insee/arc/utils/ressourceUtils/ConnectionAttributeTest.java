package fr.insee.arc.utils.ressourceUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void testHostPortDatabaseFromUri() {
		ConnectionAttribute c;
		
		c = new ConnectionAttribute("jdbc:postgresql://localhost:5555/arc_test", null, null, null);
		assertEquals("jdbc:postgresql", c.getPrefix());
		assertEquals("localhost", c.getHost());
		assertEquals("5555", c.getPort());
		assertEquals("arc_test", c.getDatabase());
		assertEquals("jdbc:postgresql://localhost:5555/arc_test",c.getDatabaseUrl());

		c = new ConnectionAttribute("jdbc:postgresql://localhost/arc_test", null, null, null);
		assertEquals("jdbc:postgresql", c.getPrefix());
		assertEquals("localhost", c.getHost());
		assertEquals("5432", c.getPort());
		assertEquals("arc_test", c.getDatabase());
		
		c.setHost("127.0.0.1");
		assertEquals("jdbc:postgresql://127.0.0.1:5432/arc_test",c.getDatabaseUrl());
	}
	
}
