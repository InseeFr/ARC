package fr.insee.arc.core.service.p2chargement.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.thread.ThreadTemporaryTable;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class ChargeurCsvDaoTest extends InitializeQueryTest {

	@Test
	public void execQuerySelectColumnsFromLoadTableTest() throws ArcException {
		
		u.executeRequest(c, "DISCARD TEMP;");

		
		Sandbox s = new Sandbox(c, null);
		ChargeurCsvDao chargeurCsv = new ChargeurCsvDao(s, null, null, null);
		
		u.executeRequest(c, new GenericPreparedStatementBuilder("CREATE TEMPORARY TABLE "+ThreadTemporaryTable.TABLE_TEMP_CHARGEMENT_A+" (a1 text, a2 int);"));

		List<String> cols = chargeurCsv.execQuerySelectColumnsFromLoadTable();
		
		assertTrue(cols.contains("a1"));
		assertTrue(cols.contains("a2"));
		assertEquals(2, cols.size());
		
		u.executeRequest(c, "DISCARD TEMP;");
		
	}

}
