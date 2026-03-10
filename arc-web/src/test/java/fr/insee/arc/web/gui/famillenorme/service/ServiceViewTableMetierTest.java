package fr.insee.arc.web.gui.famillenorme.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServiceViewTableMetierTest {

	@Test
	public void isNomTableMetierOkTableName() {
		assertTrue(ServiceViewTableMetier.isNomTableMetierValide("mapping_majic_bati_ok","mapping","majic"));
	}
	
	@Test
	public void isNomTableMetierTableNameMustStartWithALetter() {
		assertTrue(!ServiceViewTableMetier.isNomTableMetierValide("mapping_majic__bati_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadTableName() {
		assertTrue(!ServiceViewTableMetier.isNomTableMetierValide("mapping_majic_bati*_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadFamilly() {
		assertTrue(!ServiceViewTableMetier.isNomTableMetierValide("mapping_majic_bati_01_ok","mapping","notTheRightFamilly"));
	}

	@Test
	public void isNomTableMetierValideBadPhase() {
		assertTrue(!ServiceViewTableMetier.isNomTableMetierValide("mapping_majic_bati_01_ok","wrongphase","majic"));
	}
	
	@Test
	public void isNomTableMetierCaseIncensitive() {
		assertTrue(ServiceViewTableMetier.isNomTableMetierValide("maPPing_majic_BATI_ok","MApping","maJic"));
	}
	
	@Test
	public void isNomTableMetierMustFinishWithOK() {
		assertTrue(!ServiceViewTableMetier.isNomTableMetierValide("maPPing_majic_BATI","MApping","maJic"));
	}

}
