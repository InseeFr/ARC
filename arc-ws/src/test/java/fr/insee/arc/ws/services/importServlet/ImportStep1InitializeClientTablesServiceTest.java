package fr.insee.arc.ws.services.importServlet;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;

public class ImportStep1InitializeClientTablesServiceTest extends ServletArc {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7832574224892526397L;


	@BeforeClass
    public static void setup() throws SQLException, ArcException {

		InitializeQueryTest.buildPropertiesWithoutScalability(null);
		
		destroyTestData();
		initializeTestData();
	}
	
	@AfterClass
    public static void tearDown() throws SQLException, ArcException {
		destroyTestData();
    }

	private String executeImportStep1(JSONObject clientJsonInput) throws ArcException
	{
		JSONObject clientJsonInputValidated= validateRequest(clientJsonInput);
		ImportStep1InitializeClientTablesService imp = new ImportStep1InitializeClientTablesService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return sentResponse.getWr().toString();
	}
	
	
	@Test(expected = ArcException.class)
	public void testExecuteFamilyNotValid() throws ArcException {
		JSONObject clientJsonInput = new JSONObject(
				"{\"familleNorme\":\"RESIL\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
		executeImportStep1(clientJsonInput);
	}
	
	
	@Test
	public void testExecute() throws ArcException {

		JSONObject clientJsonInput = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");

		executeImportStep1(clientJsonInput);

		testCreateAndDropWsPending();
		testCreateTableNmcl();
		testCreateTableVarMetier();
		testCreateTableTableMetier();
		testCreateTableTableFamille();
		testCreateTableTablePeriodicite();
	}
	
	private void testCreateAndDropWsPending() throws ArcException {
		
		// check that the parallel thread that create tables drop the table ws_pending

		// it should be done in less than 50 iteration, test data is very little
		int maxIteration = 50;
		int i=0;
		
		while (i<maxIteration && UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ws_pending"))
		{
			i++;
			UtilitaireDao.get(0).executeImmediate(InitializeQueryTest.c, "SELECT pg_sleep(1);");
		}
		
		assertTrue(i>0);
		assertTrue(i<maxIteration);
	}
	
	private void testCreateTableNmcl() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_nmcl_table1"));
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_nmcl_table2"));
	}

	private void testCreateTableVarMetier() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_mod_variable_metier"));
	}
	
	private void testCreateTableTableMetier() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_mod_table_metier"));
	}
	
	private void testCreateTableTableFamille() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ext_mod_famille"));
	}
	
	private void testCreateTableTablePeriodicite() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ext_mod_periodicite"));
	}
	
	
	/**
	 * initialize data for the tests
	 * @throws SQLException
	 * @throws ArcException
	 */
	private static void initializeTestData() throws SQLException, ArcException {
		
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();

		query.append("CREATE SCHEMA arc;");
		query.append("CREATE SCHEMA arc_bas1;");

		
		// family and client tables
		query.append("CREATE TABLE arc.ihm_client AS ");
		query.append("SELECT 'DSN' as id_famille,'ARTEMIS' as id_application");
		query.append(SQL.UNION_ALL);
		query.append("SELECT 'DSN' as id_famille,'DSNFLASH' as id_application");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc.ihm_famille AS SELECT 'DSN' as id_famille");
		query.append(SQL.END_QUERY);

		query.append("CREATE TABLE arc_bas1.mod_table_metier AS ");
		query.append("SELECT 'DSN' as id_famille,'mapping_dsn_test1_ok' as nom_table_metier");
		query.append(SQL.UNION_ALL);
		query.append("SELECT 'PASRAU' as id_famille,'mapping_pasrau_test_ok' as nom_table_metier");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc_bas1.mod_variable_metier AS SELECT 'DSN' as id_famille, 'mapping_dsn_test1_ok' as nom_table_metier, 'id_source' as nom_variable_metier");
		query.append(SQL.END_QUERY);

		// pilotage tables
		query.append("CREATE TABLE arc_bas1.pilotage_fichier AS ");
		query.append("SELECT 'file_to_retrieve.xml' as id_source, 'PHASE3V1' as id_norme, '2023-10-01' as validite,'M' as periodicite");
		query.append(", 'MAPPING' as phase_traitement, '{OK}'::text[] as etat_traitement, '2023-11-30 10:29:47.000'::timestamp as date_traitement");
		query.append(", null::text[] as client, null::timestamp[] as date_client");
		query.append(SQL.UNION_ALL);
		// file that mustn't be retrieved when reprise is false and family is DSN
		query.append("SELECT 'file_not_to_retrieve_when_reprise_false.xml' as id_source, 'PHASE3V1' as id_norme, '2023-10-01' as validite,'M' as periodicite");
		query.append(", 'MAPPING' as phase_traitement, '{OK}'::text[] as etat_traitement, '2023-11-30 10:29:47.000'::timestamp as date_traitement");
		query.append(", '{ARTEMIS}'::text[] as client, '{2023-11-30 10:29:47.000}'::timestamp[] as date_client");;
		query.append(SQL.END_QUERY);

		// norme table used to retrieve family of data
		query.append("CREATE TABLE arc_bas1.norme AS ");
		query.append("SELECT 'PHASE3V1' as id_norme, 'DSN' as id_famille UNION ALL ");
		query.append("SELECT 'PASRAU' as id_norme, 'PASRAU' as id_famille");
		query.append(SQL.END_QUERY);
		
		// data tables containing two files
		// one had already been retrieved by client 'ARTEMIS', the other hadn't been retrieved yet
		query.append("CREATE TABLE arc_bas1.mapping_dsn_test1_ok AS ");
		query.append("SELECT 'file_to_retrieve.xml' as id_source, 'data_of_file_to_retrieve' as data UNION ALL ");
		query.append("SELECT 'file_not_to_retrieve_when_reprise_false.xml' as id_source, 'data_of_file_not_to_retrieve_when_reprise_false' as data");
		query.append(SQL.END_QUERY);
		
		// nomenclature tables
		query.append("CREATE TABLE arc_bas1.nmcl_table1 AS SELECT 1 as data");
		query.append(SQL.END_QUERY);
		query.append("CREATE TABLE arc_bas1.nmcl_table2 AS SELECT 1 as data");
		query.append(SQL.END_QUERY);
		query.append("CREATE TABLE arc.ext_mod_periodicite AS SELECT 1 as id, 'A' as VAL");
		query.append(SQL.END_QUERY);

		UtilitaireDao.get(0).executeImmediate(InitializeQueryTest.c, query);
	}

	

	/**
	 * destroy data for the tests
	 * @throws SQLException
	 * @throws ArcException
	 */
	private static void destroyTestData() throws SQLException, ArcException {

		ArcPreparedStatementBuilder query;

		query = new ArcPreparedStatementBuilder();

		query.append("DROP SCHEMA IF EXISTS arc CASCADE;");
		query.append("DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		UtilitaireDao.get(0).executeImmediate(InitializeQueryTest.c, query);
	}
	
	
	

}
