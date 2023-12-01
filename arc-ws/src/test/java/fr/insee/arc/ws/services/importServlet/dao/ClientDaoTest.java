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
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;

public class ClientDaoTest extends InitializeQueryTest {

	@Test
	public void clientDaoTest() throws ArcException, SQLException {

		InitializeQueryTest.buildPropertiesWithoutScalability(null);
		
		initializeTestData();
		
		// test family check
		testVerificationFamilleOK();
		testVerificationFamilleKO();
		
		// test data tables retrieved according to query
		testSelectBusinessDataTables();
		
//		testCreateTableOfIdSource();

		destroyTestData();
	}

	private void testSelectBusinessDataTables() throws ArcException {
		JSONObject json = new JSONObject(
				"{\"client\":\"ARTEMIS\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"DSN\",\"format\":\"csv_gzip\"}");
		ArcClientIdentifier queryParameters = new ArcClientIdentifier(json, true);
		ClientDao clientDao = new ClientDao(queryParameters);
		List<String> clientTables = clientDao.selectBusinessDataTables();
		
		assertTrue(clientTables.contains("mapping_dsn_test1_ok"));
		assertTrue(clientTables.contains("mapping_dsn_test2_ok"));
		assertEquals(2,clientTables.size());
	}
	
	public void testVerificationFamilleOK() throws ArcException {
		JSONObject json = new JSONObject(
				"{\"client\":\"ARTEMIS\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"DSN\",\"format\":\"csv_gzip\"}");
		ArcClientIdentifier queryParameters = new ArcClientIdentifier(json, true);
		ClientDao clientDao = new ClientDao(queryParameters);
		assertTrue(clientDao.verificationClientFamille());
	}
	
	public void testVerificationFamilleKO() throws ArcException {
		JSONObject json = new JSONObject(
				"{\"client\":\"ARTEMIS\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"BATI\",\"format\":\"csv_gzip\"}");
		ArcClientIdentifier queryParameters = new ArcClientIdentifier(json, true);
		ClientDao clientDao = new ClientDao(queryParameters);
		assertFalse(clientDao.verificationClientFamille());
	}
	

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

		UtilitaireDao.get(0).executeImmediate(c, query);
	}

	private void destroyTestData() throws SQLException, ArcException {

		ArcPreparedStatementBuilder query;

		query = new ArcPreparedStatementBuilder();

		query.append("DROP SCHEMA arc CASCADE;");
		query.append("DROP SCHEMA arc_bas1 CASCADE;");
		UtilitaireDao.get(0).executeImmediate(c, query);
	}

}
