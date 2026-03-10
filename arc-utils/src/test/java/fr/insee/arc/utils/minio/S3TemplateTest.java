package fr.insee.arc.utils.minio;

import static org.junit.Assert.*;

import org.junit.Test;

public class S3TemplateTest {

	@Test
	public void normalizePathTest() {
		String path;
		
		path = S3Template.normalizePath("");
		assertEquals("", path);
		
		path = S3Template.normalizePath("  ");
		assertEquals("", path);
		
		path = S3Template.normalizePath("arc\\arc_prod//");
		assertEquals("arc/arc_prod/", path);
	}

}
