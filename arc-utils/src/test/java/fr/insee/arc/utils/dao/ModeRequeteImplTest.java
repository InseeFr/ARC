package fr.insee.arc.utils.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ModeRequeteImplTest {


	@Test
	public void arcModeRequeteIHMTest() {
		String query = "set enable_hashjoin=on;set enable_mergejoin=off;set enable_hashagg=on;set enable_seqscan=off;set enable_material=off;\n";
		assertEquals(query, ModeRequete.untokenize(ModeRequeteImpl.arcModeRequeteIHM()).getQueryWithParameters());
	}

	@Test
	public void arcModeRequeteEngineTest() {
		
		String PARALLEL_WORK_MEM = "24MB";

		int TIME_OUT_SQL_EN_HEURE = 100;

		String defaultSchema = "arc_bas1";

		StringBuilder query = new StringBuilder();

		query.append("set enable_nestloop=on;").append("set enable_mergejoin=off;").append("set enable_hashjoin=on;")
				.append("set enable_material=off;").append("set enable_seqscan=off;").append("set enable_hashagg=on;")
				.append("set work_mem='" + PARALLEL_WORK_MEM + "';")
				.append("set maintenance_work_mem='" + PARALLEL_WORK_MEM + "';")
				.append("set temp_buffers='" + PARALLEL_WORK_MEM + "';")
				.append("set statement_timeout=" + (3600000 * TIME_OUT_SQL_EN_HEURE) + ";")
				.append("set from_collapse_limit=10000;").append("set join_collapse_limit=10000;")
				.append("SELECT set_config('search_path', '"+defaultSchema+",public', false);")
				.append(ModeRequete.EXTRA_FLOAT_DIGIT.expr()).append("COMMIT;");

		assertEquals(query.toString(), ModeRequeteImpl.arcModeRequeteEngine(defaultSchema).getQueryWithParameters());

	}
}
