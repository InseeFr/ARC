package fr.insee.arc.utils.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ModeRequeteImplTest {


	@Test
	public void arcModeRequeteIHMTest() {
		String query = "set enable_seqscan=off;set enable_material=off;\n";
		assertEquals(query, ModeRequete.untokenize(ModeRequeteImpl.arcModeRequeteIHM()).getQueryWithParameters());
	}

	@Test
	public void arcModeRequeteEngineTest() {
		
		String PARALLEL_WORK_MEM = "24MB";

		int TIME_OUT_SQL_EN_HEURE = 100;

		String defaultSchema = "arc_bas1";

		StringBuilder query = new StringBuilder();

		query.append("set statement_timeout=" + (3600000 * TIME_OUT_SQL_EN_HEURE) + ";")
				.append("SELECT set_config('search_path', '"+defaultSchema+",public', false);")
				.append("COMMIT;");

		assertEquals(query.toString(), ModeRequeteImpl.arcModeRequeteEngine(defaultSchema).getQueryWithParameters());

	}
}
