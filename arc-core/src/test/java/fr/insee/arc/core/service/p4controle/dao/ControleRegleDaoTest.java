package fr.insee.arc.core.service.p4controle.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.thread.ThreadTemporaryTable;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class ControleRegleDaoTest extends InitializeQueryTest  {

	@Test
	public void test() throws ArcException {
		
		u.executeImmediate(c, "DISCARD TEMP;");

		u.executeImmediate(c, "CREATE TEMPORARY TABLE tmp_table_controle AS SELECT 1 as id, null as brokenrules, null as controle ");

		// execute dao initTemporaryTable from tmp_table_controle
		ControleRegleDao dao = new ControleRegleDao();
		u.executeImmediate(c, dao.initTemporaryTable("tmp_table_controle"));

		int numberOfRowInTcMark = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) FROM "+ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP));
		int numberOfRowInTcMeta = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) FROM "+ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP));
		int numberOfRowInTcCount = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) FROM "+ThreadTemporaryTable.TABLE_CONTROLE_ROW_TOTAL_COUNT_TEMP));
		
		assertEquals(0, numberOfRowInTcMark);
		assertEquals(0, numberOfRowInTcMeta);
		assertEquals(1, numberOfRowInTcCount);
		
		// clean connection
		u.executeImmediate(c, "DISCARD TEMP;");
	}

}
