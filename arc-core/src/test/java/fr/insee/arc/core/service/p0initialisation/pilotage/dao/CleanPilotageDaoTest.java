package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import static org.junit.Assert.*;

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
		
		// fichier récupéré par les 1 seul client DSN depuis plus de 10j -> il ne doit pas être effacé car pas encore récupéré par l'autre client
		query.build("('fichier_2', 'archive_2', 'DSN_2024', 'M', '2023-11-30 10:29:25.000', 2, 'MAPPING', '{OK}', '{app_2}', '{2023-11-30 10:29:25.000}')");
		query.appendNewLine(",");

		// fichier récupéré par les 2 clients DSN déclarés depuis moins de 10j -> à effacer
		query.build("('fichier_3', 'archive_3', 'DSN_2024', 'M', current_timestamp, 2, 'MAPPING', '{OK}', '{app_2, app_1}', array[current_timestamp, current_timestamp])");
		
		query.build(SQL.END_QUERY);
		u.executeRequest(c, query);
		
		// execute test : delay is 10 days
		Sandbox sandbox = new Sandbox(c, "arc_bas1");
		CleanPilotageDao dao = new CleanPilotageDao(sandbox);
		dao.execQueryMaterializeFilesToDelete(10);
		
		// retrieve files to delete
		List<String> filesToDelete = new GenericBean(u.executeRequest(c, new ArcPreparedStatementBuilder("SELECT * FROM fichier_to_delete"))).getColumnValues("id_source");
		assertEquals(1, filesToDelete.size());
		
		
		// clean
		query = new ArcPreparedStatementBuilder();
		query.build("DROP schema if exists arc CASCADE;");
		query.build("DROP schema if exists arc_bas1 CASCADE;");
		u.executeRequest(c, query);

	}

}
