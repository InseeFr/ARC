package fr.insee.arc.web.gui.famillenorme.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import fr.insee.arc.core.serviceinteractif.ddi.DDIModeler;

public class ServiceViewFamilleNormeTest extends ServiceViewFamilleNorme {

	@Test
	public void uploadFamilleNormeDansBaseNominalTU() throws FileNotFoundException, IOException {

		String path = "src/test/resources/fr/insee/testfiles/regles_famille_test.zip";
		File file = new File(path);

		FileInputStream is = new FileInputStream(file);

		System.out.print(is);

		DDIModeler modeler = uploadFamilleNormeDansBase(is);
		System.out.println(modeler);

	}

}
