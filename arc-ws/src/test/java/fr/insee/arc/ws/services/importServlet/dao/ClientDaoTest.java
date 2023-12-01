package fr.insee.arc.ws.services.importServlet.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ExportTrackingType;
import fr.insee.arc.ws.services.importServlet.bo.TableToRetrieve;

public class ClientDaoTest extends InitializeQueryTest {

	// request for DSN family, ARTEMIS client and reprise = false
	JSONObject jsonDsnStep1 = new JSONObject(
			"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
	ArcClientIdentifier queryParametersDsnStep1 = new ArcClientIdentifier(jsonDsnStep1, true);
	ClientDao clientDaoDsnStep1 = new ClientDao(queryParametersDsnStep1);

	
	@Test
	public void clientDaoTest() throws ArcException, SQLException {

		InitializeQueryTest.buildPropertiesWithoutScalability(null);

		destroyTestData();
		initializeTestData();
		
		// test tracking table creation and registration
		testCreateTableTrackRetrievedTables();
		
		//test return of client table when nothing found
		testGetAClientTableByNameNotFound();
		
		// test family check
		testVerificationFamilleOK();
		testVerificationFamilleKO();
		
		// test data tables retrieved according to query
		List<String> selectedDataTables = testSelectBusinessDataTables();
		
		// test id_source selection table
		testCreateTableOfIdSourceRepriseFalse();
		testCreateTableOfIdSourceRepriseTrue();

		// test data table image creation
		// table must had been registered in track table
		List<String> dataTableImages = testCreateImages(selectedDataTables);
		
		// test return table from track table
		// the dataTable in dataTableImages must be found the the track data table with type ExportTrackingType.DATA
		testGetAClientTableByType(dataTableImages);
		// the dataTable in dataTableImages must be found the the track data table by its name
		testGetAClientTableByName(dataTableImages);
		
		// test tables creation for metadata tables
		testCreateTableNmcl();
		testCreateTableVarMetier();
		testCreateTableTableMetier();
		testCreateTableTableFamille();
		testCreateTableTablePeriodicite();
		
		testDropPendingClientTables();
		
		
		destroyTestData();
	}

	private void testDropPendingClientTables() throws ArcException {
		clientDaoDsnStep1.dropPendingClientTables(ArcDatabase.COORDINATOR.getIndex());
		// all client tables should had been deleted
		assertFalse(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS%"));
	}

	private void testCreateTableNmcl() throws ArcException {
		// TODO Auto-generated method stub
		clientDaoDsnStep1.createTableNmcl();
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS_%_nmcl_table1"));
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS_%_nmcl_table2"));
	}

	private void testCreateTableVarMetier() throws ArcException {
		// TODO Auto-generated method stub
		clientDaoDsnStep1.createTableVarMetier();
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS_%_mod_variable_metier"));
	}
	
	private void testCreateTableTableMetier() throws ArcException {
		// TODO Auto-generated method stub
		clientDaoDsnStep1.createTableMetier();
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS_%_mod_table_metier"));
	}
	
	private void testCreateTableTableFamille() throws ArcException {
		// TODO Auto-generated method stub
		clientDaoDsnStep1.createTableFamille();
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS_%_ext_mod_famille"));
	}
	
	private void testCreateTableTablePeriodicite() throws ArcException {
		// TODO Auto-generated method stub
		clientDaoDsnStep1.createTablePeriodicite();
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(c, "arc_bas1.ARTEMIS_%_ext_mod_periodicite"));
	}
	
	private void testGetAClientTableByNameNotFound() throws ArcException {
		TableToRetrieve registeredTable = clientDaoDsnStep1.getAClientTableByName("not_existing_table");
		assertNull(registeredTable.getTableName());
		assertNull(registeredTable.getNod());
	}

	private void testGetAClientTableByType(List<String> dataTableImages) throws ArcException {
		TableToRetrieve registeredTable = clientDaoDsnStep1.getAClientTableByType(ExportTrackingType.DATA);

		// now that image had been created we should find it in tracking table
		// check the name
		assertEquals(dataTableImages.get(0),registeredTable.getTableName());
		// data table are found on executor nod
		assertEquals(ArcDatabase.EXECUTOR,registeredTable.getNod());
	}

	private void testGetAClientTableByName(List<String> dataTableImages) throws ArcException {
		
		TableToRetrieve registeredTable = clientDaoDsnStep1.getAClientTableByName(dataTableImages.get(0));
		
		// now that image had been created we should find it in tracking table
		// check the name
		assertEquals(dataTableImages.get(0),registeredTable.getTableName());
		// the test is in non scalable nod so the data table must be on coordinator
		assertEquals(ArcDatabase.EXECUTOR,registeredTable.getNod());
	}
	
	private List<String> testCreateImages(List<String> selectedDataTables) throws ArcException {
		List<String> dataTableImages = clientDaoDsnStep1.createImages(selectedDataTables, 0);
		
		// only 1 table in model and 1 table should had been created
		assertEquals(1, dataTableImages.size());
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT distinct id_source FROM "+dataTableImages.get(0)+";");
		List<String> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("id_source");
		
		// only table with 1 id_source must had been retrieved
		assertEquals(1, content.size());

		return dataTableImages;
		
	}

	/**
	 * test on retrieving idSource
	 * request on DSN family, ARTEMIS client and reprise = false 
	 * as reprise = false, only files not already retrieved by client must be selected
	 * @throws ArcException
	 */
	private void testCreateTableOfIdSourceRepriseFalse() throws ArcException {
		
		clientDaoDsnStep1.createTableOfIdSource(jsonDsnStep1);
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT id_source FROM "+clientDaoDsnStep1.getTableOfIdSource()+";");
		List<String> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("id_source");
		
		// only 1 file must be selected as reprise = false 
		// file_not_to_retrieve_when_reprise_false has already been marked as retrieved by 'ARTEMIS' client
		assertEquals(1, content.size());

	}
	
	/**
	 * test to select id_source to be retrieved when reprise=true
	 * @throws ArcException
	 */
	private void testCreateTableOfIdSourceRepriseTrue() throws ArcException {

		JSONObject jsonDsnStep1RepriseTrue = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":true,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
		ArcClientIdentifier queryParametersDsnStep1RepriseTrue = new ArcClientIdentifier(jsonDsnStep1RepriseTrue, true);
		ClientDao clientDaoDsnStep1RepriseTrue = new ClientDao(queryParametersDsnStep1RepriseTrue);

		// create tracking table
		clientDaoDsnStep1RepriseTrue.createTableTrackRetrievedTables();
		
		clientDaoDsnStep1RepriseTrue.createTableOfIdSource(jsonDsnStep1RepriseTrue);
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT id_source FROM "+clientDaoDsnStep1RepriseTrue.getTableOfIdSource()+";");
		
		List<String> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("id_source");
		// only 1 file must be selected as reprise = false 
		// file_not_to_retrieve_when_reprise_false has already been marked as retrieved by 'ARTEMIS' client
		assertEquals(2, content.size());
	}

	private void testCreateTableTrackRetrievedTables() throws ArcException {
		clientDaoDsnStep1.createTableTrackRetrievedTables();
		
		// test
		// retrieve table content
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT tracking_type FROM "+clientDaoDsnStep1.getTableWsTracking()+";");
		List<String> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("tracking_type");
		
		// test that the table had been created and that it had been registered in itself
		assertEquals(1, content.size());
		assertEquals(ExportTrackingType.TRACK.toString(), content.get(0));

	}

	private List<String> testSelectBusinessDataTables() throws ArcException {

		List<String> clientTables = clientDaoDsnStep1.selectBusinessDataTables();

		assertTrue(clientTables.contains("mapping_dsn_test1_ok"));
		assertEquals(1,clientTables.size());
		return clientTables;
	}
	
	public void testVerificationFamilleOK() throws ArcException {
		assertTrue(clientDaoDsnStep1.verificationClientFamille());
	}
	
	public void testVerificationFamilleKO() throws ArcException {
		// request on BATI family, RESIL client and reprise = true
		// BATI family doesn't exists in the test data set
		JSONObject jsonBatiStep1 = new JSONObject(
				"{\"familleNorme\":\"BATI\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":true,\"client\":\"RESIL\",\"environnement\":\"arc_bas1\"}");
		ArcClientIdentifier queryParametersBatiStep1 = new ArcClientIdentifier(jsonBatiStep1, true);
		ClientDao clientDaoBatiStep1 = new ClientDao(queryParametersBatiStep1);
		
		assertFalse(clientDaoBatiStep1.verificationClientFamille());
	}
	
	
	
	/**
	 * initialize data for the tests
	 * @throws SQLException
	 * @throws ArcException
	 */
	private void initializeTestData() throws SQLException, ArcException {

		ArcPreparedStatementBuilder query;

		query = new ArcPreparedStatementBuilder();

		query.append("CREATE SCHEMA arc;");
		query.append("CREATE SCHEMA arc_bas1;");

		
		// family and client tables
		query.append("CREATE TABLE arc.ihm_client AS ");
		query.append("SELECT 'DSN' as id_famille,'ARTEMIS' as id_application UNION ALL ");
		query.append("SELECT 'DSN' as id_famille,'DSNFLASH' as id_application");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc.ihm_famille AS SELECT 'DSN' as id_famille");
		query.append(SQL.END_QUERY);

		query.append("CREATE TABLE arc_bas1.mod_table_metier AS ");
		query.append("SELECT 'DSN' as id_famille,'mapping_dsn_test1_ok' as nom_table_metier UNION ALL ");
		query.append("SELECT 'PASRAU' as id_famille,'mapping_pasrau_test_ok' as nom_table_metier");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc_bas1.mod_variable_metier AS SELECT 'DSN' as id_famille, 'mapping_dsn_test1_ok' as nom_table_metier, 'id_source' as nom_variable_metier");
		query.append(SQL.END_QUERY);

		// pilotage tables
		query.append("CREATE TABLE arc_bas1.pilotage_fichier AS ");
		query.append("SELECT 'file_to_retrieve.xml' as id_source, 'PHASE3V1' as id_norme, '2023-10-01' as validite,'M' as periodicite");
		query.append(", 'MAPPING' as phase_traitement, '{OK}'::text[] as etat_traitement, '2023-11-30 10:29:47.000'::timestamp as date_traitement");
		query.append(", null::text[] as client, null::timestamp[] as date_client");
		query.append(" UNION ALL ");
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

		UtilitaireDao.get(0).executeImmediate(c, query);
	}

	/**
	 * destroy data for the tests
	 * @throws SQLException
	 * @throws ArcException
	 */
	private void destroyTestData() throws SQLException, ArcException {

		ArcPreparedStatementBuilder query;

		query = new ArcPreparedStatementBuilder();

		query.append("DROP SCHEMA IF EXISTS arc CASCADE;");
		query.append("DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		UtilitaireDao.get(0).executeImmediate(c, query);
	}

}
