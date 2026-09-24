package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class CleanPilotageDaoTest extends InitializeQueryTest {

	@Test
	public void execQueryMaterializeFilesToDelete10DaysTest() throws ArcException {
	
		ArcPreparedStatementBuilder query;
		
		// build test case
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");

		query.build("CREATE schema arc;");
		query.build("CREATE schema arc_bas1;");
		
		
		query.build("CREATE TABLE arc.ihm_client AS ");
		query.build("SELECT 'app_1' as id_application, 'DSN' as id_famille ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_2' as id_application, 'DSN' as id_famille ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_1' as id_application, 'RESIL' as id_famille ");
		query.build(SQL.END_QUERY);
		
		query.build("CREATE TABLE arc.ihm_norme AS ");
		query.build("SELECT 'DSN_2024' as id_norme, 'M' as periodicite, 'DSN' as id_famille ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'RESIL_2024' as id_norme, 'M' as periodicite, 'RESIL' as id_famille ");
		query.build(SQL.END_QUERY);

		
		query.build("CREATE TABLE arc_bas1.pilotage_fichier (id_source text, container text, id_norme text, periodicite text, date_traitement timestamp, etape int, phase_traitement text, etat_traitement _text, client _text, date_client _timestamp);");
		query.build("INSERT INTO arc_bas1.pilotage_fichier VALUES ");
		// fichier récupéré par les 2 clients DSN déclarés depuis plus de 10j -> à effacer
		query.build("('fichier_1', 'archive_1', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2, app_1}', '{2023-11-30 10:29:25.000, 2023-11-15 11:15:14.000}')");
		query.appendNewLine(",");
		// fichier récupéré par 1 seul client DSN déclaré depuis plus de 10j -> ne pas effacer car pas encore récupéré par l'autre client
		query.build("('fichier_2', 'archive_2', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2}', '{2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");
		// fichier récupéré par les 2 clients DSN déclarés depuis moins de 10j -> ne pas effacer car durée de rétention non écoulée
		query.build("('fichier_3', 'archive_3', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2, app_1}', array['2023-11-30 10:29:25.000', current_timestamp])");
		query.appendNewLine(",");
		// fichier récupéré par l'unique client RESIL depuis plus de 10j et par un client qui n'existe plus -> à effacer
		query.build("('fichier_4', 'archive_4', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_1}', '{2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");
		// fichier récupéré par l'unique client RESIL depuis plus de 10j et par un client qui n'existe plus depuis plus de 10j -> à effacer
		query.build("('fichier_41', 'archive_4', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_1, app_3}', '{2023-11-30 10:29:25.000, 2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");
		// fichier RESIL KO depuis plus de 10j -> à effacer
		query.build("('fichier_5', 'archive_5', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'CHARGEMENT', '{KO}', null, null)");
		query.appendNewLine(",");
		// fichier RESIL KO depuis moins de 10j -> ne pas effacer car durée de rétention non écoulée
		query.build("('fichier_6', 'archive_6', 'RESIL_2024', 'M', current_timestamp, 2, 'MAPPING', '{KO}', null, null)");
		query.appendNewLine(",");
		// fichier pas encore récupéré -> ne pas effacer car pas récupéré
		query.build("('fichier_7', 'archive_7', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', null, null)");
		
		
		query.build(SQL.END_QUERY);
		
		u.executeRequest(c, query);
		
		// execute test : delay is 10 days
		Sandbox sandbox = new Sandbox(c, "arc_bas1");
		CleanPilotageDao dao = new CleanPilotageDao(sandbox);
		dao.execQueryMaterializeFilesToDelete(10);
		
		// retrieve files to delete
		List<String> filesToDelete = new GenericBean(u.executeRequest(c, new ArcPreparedStatementBuilder("SELECT * FROM fichier_to_delete"))).getColumnValues("id_source");
		
		// compare to expected files to delete
		List<String> expectedFilesToDelete = new ArrayList<String>();
		expectedFilesToDelete.add("fichier_1"); // fichier récupéré par tous les clients depuis plus de 10j
		expectedFilesToDelete.add("fichier_4"); // fichier récupéré par client unique depuis plus de 10j
		expectedFilesToDelete.add("fichier_41"); // fichier récupéré par client unique depuis plus de 10j et par un client qui n'existe plus depuis plus de 10j
		expectedFilesToDelete.add("fichier_5"); // fichier KO depuis plus de 10j
		assertTrue(expectedFilesToDelete.size() == filesToDelete.size() && filesToDelete.containsAll(expectedFilesToDelete));
		
		// clean
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");
		u.executeRequest(c, query);

	}

	@Test
	public void execQueryMaterializeFilesToDelete10DaysTestNonRegression() throws ArcException {

		ArcPreparedStatementBuilder query;

		// build test case
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");

		query.build("CREATE schema arc;");
		query.build("CREATE schema arc_bas1;");


		query.build("CREATE TABLE arc.ihm_client AS ");
		query.build("SELECT 'app_1' as id_application, 'DSN' as id_famille, NULL::integer as jours_retention ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_2' as id_application, 'DSN' as id_famille, NULL::integer as jours_retention ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_1' as id_application, 'RESIL' as id_famille, NULL::integer as jours_retention ");
		query.build(SQL.END_QUERY);

		query.build("CREATE TABLE arc.ihm_norme AS ");
		query.build("SELECT 'DSN_2024' as id_norme, 'M' as periodicite, 'DSN' as id_famille ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'RESIL_2024' as id_norme, 'M' as periodicite, 'RESIL' as id_famille ");
		query.build(SQL.END_QUERY);


		query.build("CREATE TABLE arc_bas1.pilotage_fichier (id_source text, container text, id_norme text, periodicite text, date_traitement timestamp, etape int, phase_traitement text, etat_traitement _text, client _text, date_client _timestamp);");
		query.build("INSERT INTO arc_bas1.pilotage_fichier VALUES ");
		// fichier récupéré par les 2 clients DSN déclarés depuis plus de 10j -> à effacer
		query.build("('fichier_1', 'archive_1', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2, app_1}', '{2023-11-30 10:29:25.000, 2023-11-15 11:15:14.000}')");
		query.appendNewLine(",");
		// fichier récupéré par 1 seul client DSN déclaré depuis plus de 10j -> ne pas effacer car pas encore récupéré par l'autre client
		query.build("('fichier_2', 'archive_2', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2}', '{2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");
		// fichier récupéré par les 2 clients DSN déclarés depuis moins de 10j -> ne pas effacer car durée de rétention non écoulée
		query.build("('fichier_3', 'archive_3', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2, app_1}', array['2023-11-30 10:29:25.000', current_timestamp])");
		query.appendNewLine(",");
		// fichier récupéré par l'unique client RESIL depuis plus de 10j et par un client qui n'existe plus -> à effacer
		query.build("('fichier_4', 'archive_4', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_1}', '{2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");
		// fichier récupéré par l'unique client RESIL depuis plus de 10j et par un client qui n'existe plus depuis plus de 10j -> à effacer
		query.build("('fichier_41', 'archive_4', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_1, app_3}', '{2023-11-30 10:29:25.000, 2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");
		// fichier RESIL KO depuis plus de 10j -> à effacer
		query.build("('fichier_5', 'archive_5', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'CHARGEMENT', '{KO}', null, null)");
		query.appendNewLine(",");
		// fichier RESIL KO depuis moins de 10j -> ne pas effacer car durée de rétention non écoulée
		query.build("('fichier_6', 'archive_6', 'RESIL_2024', 'M', current_timestamp, 2, 'MAPPING', '{KO}', null, null)");
		query.appendNewLine(",");
		// fichier pas encore récupéré -> ne pas effacer car pas récupéré
		query.build("('fichier_7', 'archive_7', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', null, null)");


		query.build(SQL.END_QUERY);

		u.executeRequest(c, query);

		// execute test : delay is 10 days
		Sandbox sandbox = new Sandbox(c, "arc_bas1");
		CleanPilotageDao dao = new CleanPilotageDao(sandbox);
		dao.execQueryMaterializeFilesToDeleteNew(10);

		// retrieve files to delete
		List<String> filesToDelete = new GenericBean(u.executeRequest(c, new ArcPreparedStatementBuilder("SELECT * FROM fichier_to_delete"))).getColumnValues("id_source");

		// compare to expected files to delete
		List<String> expectedFilesToDelete = new ArrayList<String>();
		expectedFilesToDelete.add("fichier_1"); // fichier récupéré par tous les clients depuis plus de 10j
		expectedFilesToDelete.add("fichier_4"); // fichier récupéré par client unique depuis plus de 10j
		expectedFilesToDelete.add("fichier_41"); // fichier récupéré par client unique depuis plus de 10j et par un client qui n'existe plus depuis plus de 10j
		expectedFilesToDelete.add("fichier_5"); // fichier KO depuis plus de 10j
		assertTrue(expectedFilesToDelete.size() == filesToDelete.size() && filesToDelete.containsAll(expectedFilesToDelete));

		// clean
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");
		u.executeRequest(c, query);

	}

	@Test
	public void execQueryMaterializeFilesToDeleteNonRegressionTest() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");

		query.build("CREATE schema arc;");
		query.build("CREATE schema arc_bas1;");

		query.build("CREATE TABLE arc.ihm_client AS ");
		query.build("SELECT 'app_1' as id_application, 'DSN' as id_famille, NULL::integer as jours_retention ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_2' as id_application, 'DSN' as id_famille, NULL::integer as jours_retention ");
		query.build(SQL.END_QUERY);

		query.build("CREATE TABLE arc.ihm_norme AS ");
		query.build("SELECT 'DSN_2024' as id_norme, 'M' as periodicite, 'DSN' as id_famille ");
		query.build(SQL.END_QUERY);

		query.build("""
        CREATE TABLE arc_bas1.pilotage_fichier (
            id_source text,
            container text,
            id_norme text,
            periodicite text,
            date_traitement timestamp,
            etape int,
            phase_traitement text,
            etat_traitement _text,
            client _text,
            date_client _timestamp
        );
        """);

		query.build("INSERT INTO arc_bas1.pilotage_fichier VALUES ");

		// Exactement 10 jours pour les deux clients
		// => à supprimer car délai >= 10
		query.build("""
        ('fichier_exactement_10j', 'archive_1', 'DSN_2024', 'M',
         current_timestamp - interval '10 days',
         2, 'MAPPING', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '10 days',
               current_timestamp - interval '10 days'])
        """);

		query.appendNewLine(",");

		// Un client à J-20 et le dernier à J-9
		// => à conserver : la dernière récupération n'a que 9 jours
		query.build("""
        ('fichier_dernier_client_9j', 'archive_2', 'DSN_2024', 'M',
         current_timestamp - interval '20 days',
         2, 'MAPPING', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '20 days',
               current_timestamp - interval '9 days'])
        """);

		query.appendNewLine(",");

		// Un client à J-20 et le dernier à J-10
		// => à supprimer
		query.build("""
        ('fichier_dernier_client_10j', 'archive_3', 'DSN_2024', 'M',
         current_timestamp - interval '20 days',
         2, 'MAPPING', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '20 days',
               current_timestamp - interval '10 days'])
        """);

		query.appendNewLine(",");

		// Client historique app_3 qui n'existe plus dans ihm_client.
		// Les deux clients actuellement déclarés ont récupéré le fichier.
		// => à supprimer
		query.build("""
        ('fichier_ancien_client', 'archive_4', 'DSN_2024', 'M',
         current_timestamp - interval '20 days',
         2, 'MAPPING', '{OK}',
         '{app_3, app_2, app_1}',
         array[current_timestamp - interval '20 days',
               current_timestamp - interval '20 days',
               current_timestamp - interval '20 days'])
        """);

		query.appendNewLine(",");

		// app_2 est déclaré mais n'a jamais récupéré le fichier
		// => à conserver
		query.build("""
        ('fichier_client_manquant', 'archive_5', 'DSN_2024', 'M',
         current_timestamp - interval '20 days',
         2, 'MAPPING', '{OK}',
         '{app_1}',
         array[current_timestamp - interval '20 days'])
        """);

		query.appendNewLine(",");

		// KO exactement à 10 jours
		// => à supprimer
		query.build("""
        ('fichier_ko_10j', 'archive_6', 'DSN_2024', 'M',
         current_timestamp - interval '10 days',
         2, 'CHARGEMENT', '{KO}',
         null, null)
        """);

		query.appendNewLine(",");

		// KO à 9 jours
		// => à conserver
		query.build("""
        ('fichier_ko_9j', 'archive_7', 'DSN_2024', 'M',
         current_timestamp - interval '9 days',
         2, 'CHARGEMENT', '{KO}',
         null, null)
        """);

		query.appendNewLine(",");

		// Tous les clients ont récupéré le fichier depuis longtemps,
		// mais le fichier OK n'est pas en phase MAPPING
		// => à conserver
		query.build("""
        ('fichier_ok_hors_mapping', 'archive_8', 'DSN_2024', 'M',
         current_timestamp - interval '20 days',
         2, 'CHARGEMENT', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '20 days',
               current_timestamp - interval '20 days'])
        """);

		query.build(SQL.END_QUERY);

		u.executeRequest(c, query);

		Sandbox sandbox = new Sandbox(c, "arc_bas1");
		CleanPilotageDao dao = new CleanPilotageDao(sandbox);

		dao.execQueryMaterializeFilesToDeleteNew(10);

		List<String> filesToDelete = new GenericBean(
				u.executeRequest(
						c,
						new ArcPreparedStatementBuilder(
								"SELECT * FROM fichier_to_delete"
						)
				)
		).getColumnValues("id_source");

		List<String> expectedFilesToDelete = List.of(
				"fichier_exactement_10j",
				"fichier_dernier_client_10j",
				"fichier_ancien_client",
				"fichier_ko_10j"
		);

		assertTrue(
				expectedFilesToDelete.size() == filesToDelete.size()
						&& filesToDelete.containsAll(expectedFilesToDelete)
		);

		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");
		u.executeRequest(c, query);
	}

	@Test
	public void execQueryMaterializeFilesToDeleteWithClientRetentionTest() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		// build test case
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");

		query.build("CREATE schema arc;");
		query.build("CREATE schema arc_bas1;");

		/*
		 * app_1 : rétention de 90 jours
		 * app_2 : rétention de 0 jour
		 * RESIL : pas de rétention spécifique => fallback sur les 10 jours globaux
		 */
		query.build("CREATE TABLE arc.ihm_client AS ");
		query.build("SELECT 'app_1' as id_application, 'DSN' as id_famille, 90::integer as jours_retention ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_2' as id_application, 'DSN' as id_famille, 0::integer as jours_retention ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'app_1' as id_application, 'RESIL' as id_famille, NULL::integer as jours_retention ");
		query.build(SQL.END_QUERY);

		query.build("CREATE TABLE arc.ihm_norme AS ");
		query.build("SELECT 'DSN_2024' as id_norme, 'M' as periodicite, 'DSN' as id_famille ");
		query.build(SQL.UNION_ALL);
		query.build("SELECT 'RESIL_2024' as id_norme, 'M' as periodicite, 'RESIL' as id_famille ");
		query.build(SQL.END_QUERY);

		query.build("""
        CREATE TABLE arc_bas1.pilotage_fichier (
            id_source text,
            container text,
            id_norme text,
            periodicite text,
            date_traitement timestamp,
            etape int,
            phase_traitement text,
            etat_traitement _text,
            client _text,
            date_client _timestamp
        );
        """);

		query.build("INSERT INTO arc_bas1.pilotage_fichier VALUES ");

		// app_1 : export depuis plus de 90j
		// app_2 : export récent mais rétention 0j
		// => à effacer : les deux rétentions sont écoulées
		query.build("""
        ('fichier_1', 'archive_1', 'DSN_2024', 'M',
         current_timestamp - interval '100 days',
         2, 'MAPPING', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '100 days',
               current_timestamp - interval '1 day'])
        """);

		query.appendNewLine(",");

		// app_1 : export depuis seulement 80j avec rétention 90j
		// app_2 : rétention 0j déjà écoulée
		// => ne pas effacer : app_1 bloque encore pendant 10j
		query.build("""
        ('fichier_2', 'archive_2', 'DSN_2024', 'M',
         current_timestamp - interval '80 days',
         2, 'MAPPING', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '80 days',
               current_timestamp - interval '20 days'])
        """);

		query.appendNewLine(",");

		// Cas important :
		// app_1 (90j) a été exporté il y a 100j => échéance dépassée
		// app_2 (0j) est exporté aujourd'hui => son échéance tombe après celle d'app_1
		// => à effacer
		query.build("""
        ('fichier_3', 'archive_3', 'DSN_2024', 'M',
         current_timestamp - interval '100 days',
         2, 'MAPPING', '{OK}',
         '{app_1, app_2}',
         array[current_timestamp - interval '100 days',
               current_timestamp])
        """);

		query.appendNewLine(",");

		// app_1 n'a pas encore récupéré le fichier
		// => ne pas effacer, même si app_2 a une rétention de 0j
		query.build("""
        ('fichier_4', 'archive_4', 'DSN_2024', 'M',
         current_timestamp - interval '100 days',
         2, 'MAPPING', '{OK}',
         '{app_2}',
         array[current_timestamp - interval '100 days'])
        """);

		query.appendNewLine(",");

		// RESIL n'a pas de rétention spécifique :
		// fallback sur les 10 jours globaux
		// export depuis 11j => à effacer
		query.build("""
        ('fichier_5', 'archive_5', 'RESIL_2024', 'M',
         current_timestamp - interval '11 days',
         2, 'MAPPING', '{OK}',
         '{app_1}',
         array[current_timestamp - interval '11 days'])
        """);

		query.appendNewLine(",");

		// Même fallback global, mais seulement 9j
		// => ne pas effacer
		query.build("""
        ('fichier_6', 'archive_6', 'RESIL_2024', 'M',
         current_timestamp - interval '9 days',
         2, 'MAPPING', '{OK}',
         '{app_1}',
         array[current_timestamp - interval '9 days'])
        """);

		query.appendNewLine(",");

		// KO depuis 11j :
		// la rétention client ne s'applique pas, délai global de 10j
		// => à effacer
		query.build("""
        ('fichier_7', 'archive_7', 'DSN_2024', 'M',
         current_timestamp - interval '11 days',
         2, 'CHARGEMENT', '{KO}', null, null)
        """);

		query.appendNewLine(",");

		// KO depuis 9j :
		// => ne pas effacer
		query.build("""
        ('fichier_8', 'archive_8', 'DSN_2024', 'M',
         current_timestamp - interval '9 days',
         2, 'CHARGEMENT', '{KO}', null, null)
        """);

		query.build(SQL.END_QUERY);

		u.executeRequest(c, query);

		// execute test : global/default delay is 10 days
		Sandbox sandbox = new Sandbox(c, "arc_bas1");
		CleanPilotageDao dao = new CleanPilotageDao(sandbox);
		dao.execQueryMaterializeFilesToDeleteNew(10);

		// retrieve files to delete
		List<String> filesToDelete = new GenericBean(
				u.executeRequest(
						c,
						new ArcPreparedStatementBuilder(
								"SELECT * FROM fichier_to_delete"
						)
				)
		).getColumnValues("id_source");

		// compare to expected files to delete
		List<String> expectedFilesToDelete = new ArrayList<>();
		expectedFilesToDelete.add("fichier_1");
		expectedFilesToDelete.add("fichier_3");
		expectedFilesToDelete.add("fichier_5");
		expectedFilesToDelete.add("fichier_7");

		assertTrue(
				expectedFilesToDelete.size() == filesToDelete.size()
						&& filesToDelete.containsAll(expectedFilesToDelete)
		);

		// clean
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");
		u.executeRequest(c, query);
	}

}
