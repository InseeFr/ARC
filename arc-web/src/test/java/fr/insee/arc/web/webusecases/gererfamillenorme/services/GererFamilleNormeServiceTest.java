package fr.insee.arc.web.webusecases.gererfamillenorme.services;

import static org.junit.Assert.*;

import org.junit.Test;

public class GererFamilleNormeServiceTest {

	@Test
	public void isNomTableMetierOkTableName() {
		assertTrue(GererFamilleNormeService.isNomTableMetierValide("mapping_majic_bati_ok","mapping","majic"));
	}
	
	@Test
	public void isNomTableMetierTableNameMustStartWithALetter() {
		assertTrue(!GererFamilleNormeService.isNomTableMetierValide("mapping_majic__bati_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadTableName() {
		assertTrue(!GererFamilleNormeService.isNomTableMetierValide("mapping_majic_bati*_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadFamilly() {
		assertTrue(!GererFamilleNormeService.isNomTableMetierValide("mapping_majic_bati_01_ok","mapping","notTheRightFamilly"));
	}

	@Test
	public void isNomTableMetierValideBadPhase() {
		assertTrue(!GererFamilleNormeService.isNomTableMetierValide("mapping_majic_bati_01_ok","wrongphase","majic"));
	}
	
	@Test
	public void isNomTableMetierCaseIncensitive() {
		assertTrue(GererFamilleNormeService.isNomTableMetierValide("maPPing_majic_BATI_ok","MApping","maJic"));
	}
	
	@Test
	public void isNomTableMetierMustFinishWithOK() {
		assertTrue(!GererFamilleNormeService.isNomTableMetierValide("maPPing_majic_BATI","MApping","maJic"));
	}

}
