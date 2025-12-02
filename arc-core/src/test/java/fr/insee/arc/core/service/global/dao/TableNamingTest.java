package fr.insee.arc.core.service.global.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;

public class TableNamingTest {

	@Test
	public void buildTableNameTokensSuffix() {
		
		String client = "ARTEMIS";
		long timestamp = System.currentTimeMillis();
				
		assertEquals("arc_bas2.ARTEMIS_"+timestamp+"_pilotage_fichier", TableNaming.buildTableNameWithTokens("arc_bas2", ViewEnum.PILOTAGE_FICHIER, client, timestamp));
		assertEquals("arc_bas2.ARTEMIS_"+timestamp+"_id_source", TableNaming.buildTableNameWithTokens("arc_bas2", ColumnEnum.ID_SOURCE, client, timestamp));
		assertEquals(null, TableNaming.buildTableNameWithTokens("arc_bas2", ColumnEnum.ID_SOURCE, null, timestamp));
		assertEquals("arc_bas2.ARTEMIS_"+timestamp+"_test", TableNaming.buildTableNameWithTokens("arc_bas2", "test", client, timestamp));
		assertEquals("arc_bas2.test", TableNaming.buildTableNameWithTokens("arc_bas2", "TEST"));
		
	}

}
