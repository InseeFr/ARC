package fr.insee.arc.ws.services.importServlet.dao;

import java.sql.SQLException;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class InitializeTestDataNoScalability {

	/**
	 * initialize the data sample for wsimport test
	 * @param dataSampleOk : true mean that data sample will be valid, false that a table will be lacking
	 * @throws SQLException
	 * @throws ArcException
	 */
	public static void initializeTestData(boolean dataSampleOk) throws SQLException, ArcException {
		
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.append("CREATE SCHEMA arc;");
		query.append("CREATE SCHEMA arc_bas1;");

		query.append("CREATE TABLE arc.ext_etat_jeuderegle as select 'arc_bas1' as id, true as isenv;");
		query.append("CREATE TABLE arc_bas1.ext_etat_jeuderegle as select * from arc.ext_etat_jeuderegle;");

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
		
		
		if (dataSampleOk)
		{
			query.append("CREATE TABLE arc_bas1.mod_variable_metier AS SELECT 'DSN' as id_famille, 'mapping_dsn_test1_ok' as nom_table_metier, 'id_source' as nom_variable_metier");
			query.append(SQL.END_QUERY);
		}
		
		// pilotage tables
		query.append("CREATE TABLE arc_bas1.pilotage_fichier AS ");
		query.append("SELECT 'file1_to_retrieve.xml' as id_source, 'PHASE3V1' as id_norme, '2023-10-01' as validite,'M' as periodicite");
		query.append(", 'MAPPING' as phase_traitement, '{OK}'::text[] as etat_traitement, '2023-11-30 10:29:47.000'::timestamp as date_traitement");
		query.append(", null::text[] as client, null::timestamp[] as date_client");
		query.append(SQL.UNION_ALL);
		query.append("SELECT 'file2_to_retrieve.xml' as id_source, 'PHASE3V1' as id_norme, '2023-10-01' as validite,'M' as periodicite");
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
		query.append("SELECT 'file1_to_retrieve.xml' as id_source, 'data1_of_file_to_retrieve' as data");
		query.append(SQL.UNION_ALL);
		query.append("SELECT 'file2_to_retrieve.xml' as id_source, 'data2_of_file_to_retrieve' as data");
		query.append(SQL.UNION_ALL);
		query.append("SELECT 'file_not_to_retrieve_when_reprise_false.xml' as id_source, 'data_of_file_not_to_retrieve_when_reprise_false' as data");
		query.append(SQL.END_QUERY);
		
		// nomenclature tables
		query.append("CREATE TABLE arc_bas1.nmcl_table1 AS SELECT 'data1' as data");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc_bas1.nmcl_table2 AS SELECT 'data2' as data");
		query.append(SQL.END_QUERY);
		
		query.append("CREATE TABLE arc.ext_mod_periodicite AS SELECT 1 as id, 'A' as VAL");
		query.append(SQL.END_QUERY);

		UtilitaireDao.get(0).executeRequest(InitializeQueryTest.c, query);
	}

	

	/**
	 * destroy data for the tests
	 * @throws SQLException
	 * @throws ArcException
	 */
	public static void destroyTestData() throws SQLException, ArcException {

		InitializeQueryTest.buildPropertiesWithoutScalability(null);

		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();

		query.append("DROP SCHEMA IF EXISTS arc CASCADE;");
		query.append("DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		UtilitaireDao.get(0).executeRequest(InitializeQueryTest.c, query);
	}
	
	
	
	
}
