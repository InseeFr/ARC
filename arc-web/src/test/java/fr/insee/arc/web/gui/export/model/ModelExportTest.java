package fr.insee.arc.web.gui.export.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ModelExportTest {

	@Test
	public void modelExportGSTest() {
		
		ModelExport modelExport = new ModelExport();
		
		assertEquals("view.export",modelExport.getViewExport().getTitle());
		assertEquals("view.exportOption",modelExport.getViewExportOption().getTitle());
		assertEquals("view.exportFile",modelExport.getViewFileExport().getTitle());
		
		ViewExport v0 = new ViewExport();
		v0.setTitle("t0");
		modelExport.setViewExport(v0);
		assertEquals("t0",modelExport.getViewExport().getTitle());

		ViewExportOption v1 = new ViewExportOption();
		v1.setTitle("t1");
		modelExport.setViewExportOption(v1);
		assertEquals("t1",modelExport.getViewExportOption().getTitle());
		

		ViewFileExport v2 = new ViewFileExport();
		v2.setTitle("t2");
		modelExport.setViewFileExport(v2);
		assertEquals("t2",modelExport.getViewFileExport().getTitle());
		
	}

}
