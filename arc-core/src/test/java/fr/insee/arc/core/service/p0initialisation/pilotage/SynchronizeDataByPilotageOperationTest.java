package fr.insee.arc.core.service.p0initialisation.pilotage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class SynchronizeDataByPilotageOperationTest extends InitializeQueryTest {

	@Test
	public void deleteUnusedDataRecordsAllNods() throws SQLException, ArcException {
		buildPropertiesWithTwoExecutors("/tmp");

		// test case 1 : the files data are kept according to pilotage table
		// the service keeps the data of mapping tables only for the files rightly referenced in the pilotage table 
		clearTest(c);
		clearTest(e1);
		clearTest(e2);
		
		pilotageTableTest();
		coodinatorDataTest();
		executor1DataTest();
		executor2DataTest();

		// count data before sync
		assertEquals(1,countMappingTable1(c));
		assertEquals(0,countMappingTable2(c));
		assertEquals(0,countMappingTableKo(c));
		
		assertEquals(4,countMappingTable1(e1));
		assertEquals(3,countMappingTable2(e1));

		assertEquals(2,countMappingTable1(e2));
		assertEquals(2,countMappingTable2(e2));
		assertEquals(1,countMappingTableKo(e2));
		
		// sync
		Sandbox sandbox = new Sandbox(c, "arc_bas1");
		SynchronizeDataByPilotageOperation sync = new SynchronizeDataByPilotageOperation(sandbox);
		sync.deleteUnusedDataRecordsAllNods(null);
		
		
		// test the mapping table with records
		List<String> mappingTableWithRecords;
		
		mappingTableWithRecords = retrieveMappingTableWithRecords(c);
		assertTrue(mappingTableWithRecords.contains("arc_bas1.mapping_test_table1_ok"));
		assertEquals(1, mappingTableWithRecords.size());
		

		mappingTableWithRecords = retrieveMappingTableWithRecords(e1);
		assertTrue(mappingTableWithRecords.contains("arc_bas1.mapping_test_table1_ok"));
		assertTrue(mappingTableWithRecords.contains("arc_bas1.mapping_test_table2_ok"));
		assertEquals(2, mappingTableWithRecords.size());

		mappingTableWithRecords = retrieveMappingTableWithRecords(e2);
		assertTrue(mappingTableWithRecords.contains("arc_bas1.mapping_test_table1_ok"));
		assertTrue(mappingTableWithRecords.contains("arc_bas1.mapping_test_table2_ok"));
		assertTrue(mappingTableWithRecords.contains("arc_bas1.mapping_ko"));
		assertEquals(3, mappingTableWithRecords.size());
		
		// test the content of mapping tables
		
		assertEquals(0,countMappingTable1(c));
		assertEquals(0,countMappingTable2(c));
		assertEquals(0,countMappingTableKo(c));
		
		assertEquals(3,countMappingTable1(e1));
		assertEquals(2,countMappingTable2(e1));
		
		assertEquals(0,countMappingTable1(e2));
		assertEquals(0,countMappingTable2(e2));
		assertEquals(1,countMappingTableKo(e2));
		
		clearTest(c);
		clearTest(e1);
		clearTest(e2);
		
		// test case 2 : a list of of file (id_source) to delete is provided to the service.
		// the service delete their data in mapping tables
		pilotageTableTest();
		coodinatorDataTest();
		executor1DataTest();
		executor2DataTest();
				
		sync.deleteUnusedDataRecordsAllNods(Arrays.asList("f01", "f11", "f12", "f23"));
		assertEquals(0,countMappingTable1(c));
		assertEquals(0,countMappingTable2(c));
		
		assertEquals(1,countMappingTable1(e1));
		assertEquals(1,countMappingTable2(e1));

		assertEquals(2,countMappingTable1(e2));
		assertEquals(2,countMappingTable2(e2));
		assertEquals(0,countMappingTableKo(e2));

		clearTest(c);
		clearTest(e1);
		clearTest(e2);
		
	}

	private int countMappingTable1(Connection connection) throws ArcException
	{
		return u.getInt(connection, "SELECT count(*) from arc_bas1.mapping_test_table1_ok"); 
	}

	private int countMappingTable2(Connection connection) throws ArcException
	{
		return u.getInt(connection, "SELECT count(*) from arc_bas1.mapping_test_table2_ok"); 
	}
	
	private int countMappingTableKo(Connection connection) throws ArcException
	{
		return u.getInt(connection, "SELECT count(*) from arc_bas1.mapping_ko"); 
	}
	
	private List<String> retrieveMappingTableWithRecords(Connection connection) throws ArcException
	{
		return new GenericBean(
				u.executeRequest(connection, "SELECT nom_table_metier from arc_bas1.tables_with_records"))
				.getColumnValues("nom_table_metier"); 
	}

	
	private void clearTest(Connection connection) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeRequest(connection, query);
	}
	
	private void pilotageTableTest() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("CREATE SCHEMA arc_bas1;");

		query.append("DROP TABLE IF EXISTS arc_bas1.pilotage_fichier;");
		query.append(
				"CREATE TABLE arc_bas1.pilotage_fichier (id_source text, phase_traitement text, etat_traitement _text, etape int4);");

		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f11','CHARGEMENT', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f11','NORMAGE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f11','CONTROLE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f11','MAPPING', '{OK}', 2);");

		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f12','CHARGEMENT', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f12','NORMAGE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f12','CONTROLE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f12','MAPPING', '{OK}', 2);");

		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f19','CHARGEMENT', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f19','NORMAGE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f19','CONTROLE', '{OK}', 2);");

		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f22','CHARGEMENT', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f22','NORMAGE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f22','CONTROLE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f22','MAPPING', '{KO}', 2);");

		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f23','CHARGEMENT', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f23','NORMAGE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f23','CONTROLE', '{OK}', 1);");
		query.append("INSERT INTO arc_bas1.pilotage_fichier values ('f23','MAPPING', '{KO}', 2);");
		u.executeRequest(c, query);
	}

	private void coodinatorDataTest() throws ArcException {
		// test data on coordinator
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_test_table1_ok; CREATE TABLE arc_bas1.mapping_test_table1_ok (id_source text, id int4, data_1 text);");
		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_test_table2_ok; CREATE TABLE arc_bas1.mapping_test_table2_ok (id_source text, id int4, data_2 text);");
		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_ko; CREATE TABLE arc_bas1.mapping_ko (id_source text, id int4, data_ko text);");

		// drop (f01 NOT EXISTS IN PILOTAGE)
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f01',1,'f01_rec11');");
		u.executeRequest(c, query);
	}

	private void executor1DataTest() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("CREATE SCHEMA arc_bas1;");

		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_test_table1_ok; CREATE TABLE arc_bas1.mapping_test_table1_ok (id_source text, id int4, data_1 text);");
		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_test_table2_ok; CREATE TABLE arc_bas1.mapping_test_table2_ok (id_source text, id int4, data_2 text);");

		// keep (f11 IN MAPPING OK)
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f11',1, 'f11_rec11');");
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f11',2, 'f11_rec12');");
		query.append("INSERT INTO arc_bas1.mapping_test_table2_ok values('f11',1, 'f11_rec21');");

		// keep (f12 IN MAPPING OK)
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f12',1,'f12_rec12');");
		query.append("INSERT INTO arc_bas1.mapping_test_table2_ok values('f12',1,'f12_rec21');");

		// drop (f19 IN CONTROLE OK)
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f19',1,'f19_rec12');");
		query.append("INSERT INTO arc_bas1.mapping_test_table2_ok values('f19',1,'f19_rec21');");

		u.executeRequest(e1, query);
	}

	private void executor2DataTest() throws ArcException {
		// test data on executor 1
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("CREATE SCHEMA arc_bas1;");

		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_test_table1_ok; CREATE TABLE arc_bas1.mapping_test_table1_ok (id_source text, id int4, data_1 text);");
		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_test_table2_ok; CREATE TABLE arc_bas1.mapping_test_table2_ok (id_source text, id int4, data_2 text);");
		query.append(
				"DROP TABLE IF EXISTS arc_bas1.mapping_ko; CREATE TABLE arc_bas1.mapping_ko (id_source text, id int4, data_ko text);");

		// delete (f21 NOT EXISTS IN PILOTAGE)
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f21',1,'f21_rec11');");
		query.append("INSERT INTO arc_bas1.mapping_test_table2_ok values('f21',1,'f21_rec21');");

		// delete (f22 IN MAPPING KO)
		query.append("INSERT INTO arc_bas1.mapping_test_table1_ok values('f22',1,'f22_rec11');");
		query.append("INSERT INTO arc_bas1.mapping_test_table2_ok values('f22',1,'f22_rec21');");

		// keep (f23 IN MAPPING KO)
		query.append("INSERT INTO arc_bas1.mapping_ko values('f23',1,'f23_ko_1');");
		u.executeRequest(e2, query);
	}

}
