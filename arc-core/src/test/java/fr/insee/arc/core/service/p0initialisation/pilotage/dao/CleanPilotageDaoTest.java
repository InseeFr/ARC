package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
		// fichier récupéré par l'unique client RESIL depuis plus de 10j -> à effacer
		query.build("('fichier_4', 'archive_4', 'RESIL_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_1}', '{2023-11-30 10:29:25.000}')");
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
		expectedFilesToDelete.add("fichier_5"); // fichier KO depuis plus de 10j
		assertTrue(expectedFilesToDelete.size() == filesToDelete.size() && filesToDelete.containsAll(expectedFilesToDelete));
		
		// clean
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");
		u.executeRequest(c, query);

	}

}
