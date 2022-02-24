package fr.insee.arc.web.action;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GererFamilleNormeActionTest {

	@Test
	public void isNomTableMetierOkTableName() {
		assertTrue(GererFamilleNormeAction.isNomTableMetierValide("mapping_majic_bati_ok","mapping","majic"));
	}
	
	@Test
	public void isNomTableMetierTableNameMustStartWithALetter() {
		assertTrue(!GererFamilleNormeAction.isNomTableMetierValide("mapping_majic__bati_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadTableName() {
		assertTrue(!GererFamilleNormeAction.isNomTableMetierValide("mapping_majic_bati*_ok","mapping","majic"));
	}

	@Test
	public void isNomTableMetierValideBadFamilly() {
		assertTrue(!GererFamilleNormeAction.isNomTableMetierValide("mapping_majic_bati_01_ok","mapping","notTheRightFamilly"));
	}

	@Test
	public void isNomTableMetierValideBadPhase() {
		assertTrue(!GererFamilleNormeAction.isNomTableMetierValide("mapping_majic_bati_01_ok","wrongphase","majic"));
	}
	
	@Test
	public void isNomTableMetierCaseIncensitive() {
		assertTrue(GererFamilleNormeAction.isNomTableMetierValide("maPPing_majic_BATI_ok","MApping","maJic"));
	}
	
	@Test
	public void isNomTableMetierMustFinishWithOK() {
		assertTrue(!GererFamilleNormeAction.isNomTableMetierValide("maPPing_majic_BATI","MApping","maJic"));
	}
	
	
}
