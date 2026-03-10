package fr.insee.arc.core.service.p6export.provider;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class DirectoryPathExportTest {

	@Test
	public void directoryExportTest() {
		
		String pathTest = DirectoryPathExport.directoryExport("root","arc_bas1");
		assertEquals("root" + File.separator + "ARC_BAS1" +File.separator +"EXPORT", pathTest);
		
	}

}
