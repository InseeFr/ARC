package fr.insee.arc.web.gui.famillenorme.service;

import static org.junit.Assert.assertEquals;

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
		DDIModeler modeler = uploadFamilleNormeDansBase(is);

		assertEquals("FAMILLE", modeler.getModelTables().get(0).getIdFamille());
		assertEquals("table", modeler.getModelTables().get(0).getNomTableMetier());
		assertEquals("", modeler.getModelTables().get(0).getDescriptionTable());
		
		assertEquals("FAMILLE", modeler.getModelVariables().get(0).getIdFamille());
		assertEquals("table", modeler.getModelVariables().get(0).getNomTableMetier());
		assertEquals("text2", modeler.getModelVariables().get(0).getNomVariableMetier());
		assertEquals("text", modeler.getModelVariables().get(0).getTypeVariableMetier());
		assertEquals("", modeler.getModelVariables().get(0).getDescriptionVariableMetier());
		assertEquals(null, modeler.getModelVariables().get(0).getTypeConsolidation()); // on ne prend pas en compte TypeConsolidation qui reste donc null

	}
	
	@Test
	public void keepTableNameTUWithTokens() {
		
		String nomTableMetier = "mapping_famille_table_ok";
		String idFamille = "FAMILLE";
		
		assertEquals("table", keepTableName(nomTableMetier, idFamille));
	}

	@Test
	public void keepTableNameTUWithoutTokens() {
		
		String nomTableMetier = "table";
		String idFamille = "FAMILLE";
		
		assertEquals("table", keepTableName(nomTableMetier, idFamille));
	}

}
