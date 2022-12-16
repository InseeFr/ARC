package fr.insee.arc.web.webusecases.gererfamillenorme.service;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.web.webusecases.gererfamillenorme.service.HubServiceGererFamilleNorme;

public class GererFamilleNormeServiceTest {

	@Test
	public void isNomTableMetierOkTableName() {
		assertTrue(HubServiceGererFamilleNorme.isNomTableMetierValide("mapping_majic_bati_ok","mapping","majic"));
	}
	
	@Test
	public void isNomTableMetierTableNameMustStartWithALetter() {
		assertTrue(!HubServiceGererFamilleNorme.isNomTableMetierValide("mapping_majic__bati_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadTableName() {
		assertTrue(!HubServiceGererFamilleNorme.isNomTableMetierValide("mapping_majic_bati*_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadFamilly() {
		assertTrue(!HubServiceGererFamilleNorme.isNomTableMetierValide("mapping_majic_bati_01_ok","mapping","notTheRightFamilly"));
	}

	@Test
	public void isNomTableMetierValideBadPhase() {
		assertTrue(!HubServiceGererFamilleNorme.isNomTableMetierValide("mapping_majic_bati_01_ok","wrongphase","majic"));
	}
	
	@Test
	public void isNomTableMetierCaseIncensitive() {
		assertTrue(HubServiceGererFamilleNorme.isNomTableMetierValide("maPPing_majic_BATI_ok","MApping","maJic"));
	}
	
	@Test
	public void isNomTableMetierMustFinishWithOK() {
		assertTrue(!HubServiceGererFamilleNorme.isNomTableMetierValide("maPPing_majic_BATI","MApping","maJic"));
	}

}
