package fr.insee.arc.ws.services.importServlet.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ExportTrackingType;

public class ClientDaoTest extends InitializeQueryTest {

	// request for DSN family, ARTEMIS client and reprise = true
	JSONObject jsonDsnStep1 = new JSONObject(
			"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":true,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
	ArcClientIdentifier queryParametersDsnStep1 = new ArcClientIdentifier(jsonDsnStep1, true);
	ClientDao clientDaoDsnStep1 = new ClientDao(queryParametersDsnStep1);

	
	@Test
	public void clientDaoTest() throws ArcException, SQLException {

		InitializeQueryTest.buildPropertiesWithoutScalability(null);

		destroyTestData();
		initializeTestData();
		
		// test tracking table creation and registration
		testCreateTableTrackRetrievedTables();
		
		// test family check
		testVerificationFamilleOK();
		testVerificationFamilleKO();
		
		// test data tables retrieved according to query
		testSelectBusinessDataTables();
		
		testCreateTableOfIdSourceRepriseFalse();
		testCreateTableOfIdSourceRepriseTrue();

		destroyTestData();
	}

	private void testCreateTableOfIdSourceRepriseTrue() throws ArcException {
		clientDaoDsnStep1.createTableOfIdSource(jsonDsnStep1);
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT id_source FROM "+clientDaoDsnStep1.getTableOfIdSource()+";");
		
		List<String> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("id_source");
		assertEquals(2, content.size());
	}
	
	private void testCreateTableOfIdSourceRepriseFalse() throws ArcException {

		// request on DSN family, ARTEMIS client and reprise = false
		// as reprise = false, only files not already retrieved by client must be selected
		JSONObject jsonDsnStep1RepriseFalse = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
		ArcClientIdentifier queryParametersDsnStep1RepriseFalse = new ArcClientIdentifier(jsonDsnStep1RepriseFalse, true);
		ClientDao clientDaoDsnStep1RepriseFalse = new ClientDao(queryParametersDsnStep1RepriseFalse);

		// create tracking table
		clientDaoDsnStep1RepriseFalse.createTableTrackRetrievedTables();
		
		clientDaoDsnStep1RepriseFalse.createTableOfIdSource(jsonDsnStep1RepriseFalse);
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT id_source FROM "+clientDaoDsnStep1RepriseFalse.getTableOfIdSource()+";");
		
		List<String> content = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("id_source");
		// only 1 file must be selected as reprise = false 
		// file_not_to_retrieve_when_reprise_false has already been marked as retrieved by 'ARTEMIS' client
		assertEquals(1, content.size());
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

	private void testSelectBusinessDataTables() throws ArcException {

		List<String> clientTables = clientDaoDsnStep1.selectBusinessDataTables();

		assertTrue(clientTables.contains("mapping_dsn_test1_ok"));
		assertTrue(clientTables.contains("mapping_dsn_test2_ok"));
		assertEquals(2,clientTables.size());
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

		query.append("CREATE TABLE arc.ihm_client AS ");
		query.append("SELECT 'DSN' as id_famille,'ARTEMIS' as id_application UNION ALL ");
		query.append("SELECT 'DSN' as id_famille,'DSNFLASH' as id_application");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc_bas1.mod_table_metier AS ");
		query.append("SELECT 'DSN' as id_famille,'mapping_dsn_test1_ok' as nom_table_metier UNION ALL ");
		query.append("SELECT 'DSN' as id_famille,'mapping_dsn_test2_ok' as nom_table_metier UNION ALL ");
		query.append("SELECT 'PASRAU' as id_famille,'mapping_pasrau_test_ok' as nom_table_metier");
		query.append(SQL.END_QUERY);
		
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

		query.append("CREATE TABLE arc_bas1.norme AS ");
		query.append("SELECT 'PHASE3V1' as id_norme, 'DSN' as id_famille UNION ALL ");
		query.append("SELECT 'PASRAU' as id_norme, 'PASRAU' as id_famille");
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
