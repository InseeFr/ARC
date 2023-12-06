package fr.insee.arc.ws.services.importServlet;

import java.util.Arrays;
import java.util.List;

import fr.insee.arc.ws.services.importServlet.bo.RetrievedTable;

public class ExpectedResults {


	public static final List<RetrievedTable> EXPECTED_RETRIEVED_TABLES = Arrays.asList(
			new RetrievedTable("ws_info", 1, "ARTEMIS")
			,new RetrievedTable("mapping_dsn_test1_ok", 2, "file1_to_retrieve.xml")
			,new RetrievedTable("mod_table_metier", 1, "DSN")
			,new RetrievedTable("mod_variable_metier", 1, "DSN")
			,new RetrievedTable("nmcl_table1", 1, "data1")
			,new RetrievedTable("nmcl_table2", 1, "data2")
			,new RetrievedTable("ext_mod_famille", 1, "DSN")
			,new RetrievedTable("ext_mod_periodicite", 1, "1")
	);
	
}
