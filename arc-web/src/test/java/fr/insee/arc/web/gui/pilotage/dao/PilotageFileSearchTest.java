package fr.insee.arc.web.gui.pilotage.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.pilotage.model.PilotageFileSearch;
import fr.insee.arc.web.gui.pilotage.model.ViewFichierBAS;
import fr.insee.arc.web.gui.pilotage.model.ViewPilotageBAS;
import fr.insee.arc.web.gui.pilotage.model.ViewRapportBAS;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.postgresql.PGConnection;

/** Own embedded database only: no application DB, S3 or engine invocation. */
class PilotageFileSearchTest {

	private static EmbeddedPostgres database;
	private static Connection connection;
	private PilotageDao dao;
	private CapturingVObjectService service;
	private VObject files;

	@BeforeAll
	static void createDatabase() throws Exception {
		database = EmbeddedPostgres.start();
		connection = database.getPostgresDatabase().getConnection();
		connection.unwrap(PGConnection.class).setPrepareThreshold(0);
		try (var statement = connection.createStatement()) {
			statement.execute("CREATE SCHEMA file_search_test");
			statement.execute("CREATE TABLE file_search_test.pilotage_fichier ("
					+ "container text, o_container text, id_source text, id_norme text, validite text, "
					+ "periodicite text, phase_traitement text, etat_traitement text[], "
					+ "date_traitement timestamp, date_entree text, rapport text, nb_enr integer, "
					+ "to_delete text, client text[], date_client timestamp[], jointure text)");
		}
	}

	@AfterAll
	static void closeDatabase() throws Exception {
		if (connection != null) {
			connection.close();
		}
		if (database != null) {
			database.close();
		}
	}

	@BeforeEach
	void prepareViews() throws Exception {
		try (var statement = connection.createStatement()) {
			statement.execute("TRUNCATE file_search_test.pilotage_fichier");
		}
		Session session = new Session();
		session.setHttpSession(new HttpSessionTemplate());
		service = new CapturingVObjectService(session);
		service.setConnection(connection);
		DataObjectService objects = new DataObjectService();
		objects.setSandboxSchema("file_search_test");
		dao = new PilotageDao();
		dao.initialize(service, objects);
		files = new ViewFichierBAS();
	}

	@Test
	void searchesEveryOkPhaseAcrossDatesAndOriginalArchives() throws Exception {
		for (TraitementPhase phase : TraitementPhase.getListPhaseReportedInBas()) {
			insert(phase, phase + "_FIP6_a.xml", "first", "original.zip", "2026-01-01", "{OK}");
			insert(phase, phase + "_fip6_b.xml", "second", "FIP6.zip", "2026-10-27", "{OK}");
			insert(phase, phase + "_other.xml", "renamed", "FIP6-original.zip", "2026-10-27", "{OK}");
			insert(phase, phase + "_FIP6_ko.xml", "ko", "original.zip", "2026-10-27", "{KO}");
			insert(phase, phase + "_FIP6_mixed.xml", "mixed", "original.zip", "2026-10-27", "{OK,KO}");
		}
		for (TraitementPhase phase : TraitementPhase.getListPhaseReportedInBas()) {
			search(phase.toString(), "%FIP6%");
			assertEquals(3, files.getContent().size(), phase.toString());
			assertEquals(List.of(phase.toString(), phase.toString(), phase.toString()),
					files.mapContent().get("phase_traitement"));
			assertEquals(3, rows(files.getMainQuery()).get("id_source").size());
		}
	}

	@Test
	void sourceSearchLimitAlsoRestrictsActions() throws Exception {
		for (int i = 0; i < 120; i++) {
			insert(TraitementPhase.MAPPING, "LIMIT_" + i + ".xml", "archive",
					"original.zip", "2026-01-01", "{OK}");
		}
		search("MAPPING", "LIMIT_");
		assertEquals(100, rows(files.getMainQuery()).get("id_source").size());
		UtilitaireDao.get(0).executeRequest(connection, dao.queryUpdateToDelete(files, "R"));
		assertEquals(100, markedCount("R"));
	}

	@Test
	void acceptsPlainTextWildcardsAndBoundApostrophes() throws Exception {
		insert(TraitementPhase.MAPPING, "FIP6_a.xml", "a", "archive.zip", "2026-01-01", "{OK}");
		insert(TraitementPhase.MAPPING, "l'archive_FIP7.xml", "b", "archive.zip", "2026-02-01", "{OK}");
		search("MAPPING", "fip6");
		assertEquals(0, files.getContent().size());
		search("MAPPING", "FIP6");
		assertEquals(List.of("FIP6_a.xml"), files.mapContent().get("id_source"));
		search("MAPPING", "%FIP_%.xml");
		assertEquals(2, files.getContent().size());
		search("MAPPING", "l'archive");
		assertEquals(List.of("l'archive_FIP7.xml"), files.mapContent().get("id_source"));
		search("MAPPING", "' OR true --");
		assertEquals(0, files.getContent().size());
		assertTrue(files.getIsInitialized());
	}

	@Test
	void paginationFiltersAndActionsKeepTheFullSearchScope() throws Exception {
		for (int i = 0; i < 12; i++) {
			insert(TraitementPhase.MAPPING, String.format("PAGINATE_%02d.xml", i),
					"archive", "original.zip", "2026-01-" + (i + 1), "{OK}");
		}
		insert(TraitementPhase.MAPPING, "unrelated.xml", "archive", "original.zip", "2026-01-01", "{OK}");
		search("MAPPING", "PAGINATE");
		assertEquals(10, files.getContent().size());
		assertEquals(2, files.getNbPages());
		files.setIdPage("2");
		refresh();
		assertEquals(2, files.getContent().size());
		UtilitaireDao.get(0).executeRequest(connection, dao.queryUpdateToDelete(files, "R"));
		assertEquals(12, markedCount("R"));

		files.setFilterFields(new ArrayList<>(List.of("", "%_11.xml")));
		files.setIdPage("1");
		refresh();
		assertEquals(List.of("PAGINATE_11.xml"), files.mapContent().get("id_source"));
		files.setSelectedLines(new ArrayList<>(List.of(true)));
		UtilitaireDao.get(0).executeRequest(connection, dao.queryUpdateToDelete(files, "1"));
		assertEquals(1, markedCount("1"));
		assertEquals(11, markedCount("R"));
	}

	@Test
	void checkedFilesRestrictActionsWithoutSecondaryFilters() throws Exception {
		insert(TraitementPhase.MAPPING, "FIP6_a.xml", "a", "archive.zip", "2026-01-01", "{OK}");
		insert(TraitementPhase.MAPPING, "FIP6_b.xml", "b", "archive.zip", "2026-02-01", "{OK}");
		search("MAPPING", "FIP6");
		files.setSelectedLines(new ArrayList<>(List.of(true, false)));
		UtilitaireDao.get(0).executeRequest(connection, dao.queryUpdateToDelete(files, "RA"));
		assertEquals(1, markedCount("RA"));
	}

	@Test
	void databaseDownloadUsesSearchAndDetailFiltersWithoutADateSelection() throws Exception {
		insert(TraitementPhase.MAPPING, "FIP6_a.xml", "a", "archive.zip", "2026-01-01", "{OK}");
		insert(TraitementPhase.MAPPING, "FIP6_b.xml", "b", "archive.zip", "2026-02-01", "{OK}");
		insert(TraitementPhase.MAPPING, "unrelated.xml", "c", "archive.zip", "2026-03-01", "{OK}");
		try (var statement = connection.createStatement()) {
			statement.execute("CREATE TEMP TABLE mapping_search_ok AS "
					+ "SELECT id_source FROM file_search_test.pilotage_fichier");
		}
		search("MAPPING", "FIP6");
		dao.downloadBdBAS(files, "unused", List.of("mapping_search_ok"), TraitementPhase.MAPPING);
		assertEquals(2, rows(service.downloadQueries.get(0)).get("id_source").size());
		files.setFilterFields(new ArrayList<>(List.of("", "%_b.xml")));
		refresh();
		dao.downloadBdBAS(files, "unused", List.of("mapping_search_ok"), TraitementPhase.MAPPING);
		assertEquals(List.of("FIP6_b.xml"), rows(service.downloadQueries.get(0)).get("id_source"));
		search("MAPPING", "missing");
		dao.downloadBdBAS(files, "unused", List.of("mapping_search_ok"), TraitementPhase.MAPPING);
		assertTrue(rows(service.downloadQueries.get(0)).isEmpty());
	}

	@Test
	void newSearchResetsSelectionFiltersAndPageAndSurvivesSessionCopy() throws Exception {
		insert(TraitementPhase.MAPPING, "FIP6_a.xml", "a", "archive.zip", "2026-01-01", "{OK}");
		search("MAPPING", "FIP6");
		files.setSelectedLines(new ArrayList<>(List.of(true)));
		files.setFilterFields(new ArrayList<>(List.of("unwanted")));
		files.setIdPage("8");
		assertTrue(PilotageFileSearch.start(files, "MAPPING", "FIP6", "file_search_test"));
		assertTrue(files.mapContentSelected().isEmpty());
		assertTrue(files.getFilterFields().isEmpty());
		assertEquals("1", files.getIdPage());
		refresh();
		VObject restored = service.preInitialize(new ViewFichierBAS());
		assertTrue(PilotageFileSearch.isActive(restored));
		assertEquals("FIP6", PilotageFileSearch.getPattern(restored));
		assertFalse(PilotageFileSearch.clearIfEnvironmentChanged(restored, "file_search_test"));
		assertTrue(PilotageFileSearch.clearIfEnvironmentChanged(restored, "another_environment"));
		assertFalse(PilotageFileSearch.isActive(restored));
		Session otherSession = new Session();
		otherSession.setHttpSession(new HttpSessionTemplate());
		assertFalse(PilotageFileSearch.isActive(new VObjectService(otherSession).preInitialize(new ViewFichierBAS())));
	}

	@Test
	void blankAndInvalidSearchesNeverOpenAllFiles() {
		assertFalse(PilotageFileSearch.start(files, "MAPPING", " ", "file_search_test"));
		refresh();
		assertFalse(files.getIsInitialized());
		assertFalse(PilotageFileSearch.start(files, "EXPORT", "%", "file_search_test"));
		assertFalse(PilotageFileSearch.start(files, "unknown", "FIP6", "file_search_test"));
		assertFalse(PilotageFileSearch.isActive(files));
	}

	private void search(String phase, String pattern) {
		assertTrue(PilotageFileSearch.start(files, phase, pattern, "file_search_test"));
		refresh();
		assertNotNull(files.getMainQuery());
	}

	private void refresh() {
		dao.initializeFichierBAS(files, new ViewPilotageBAS(), new ViewRapportBAS());
	}

	private Map<String, List<String>> rows(ArcPreparedStatementBuilder query) throws Exception {
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query)).mapContent();
	}

	private int markedCount(String code) throws Exception {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(
				"SELECT count(*) AS n FROM file_search_test.pilotage_fichier WHERE to_delete = ");
		query.appendText(code);
		return Integer.parseInt(rows(query).get("n").get(0));
	}

	private void insert(TraitementPhase phase, String source, String container, String original,
			String date, String state) throws Exception {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(
				"INSERT INTO file_search_test.pilotage_fichier "
				+ "(phase_traitement, id_source, container, o_container, date_entree, etat_traitement) VALUES (");
		query.appendText(phase.toString()).append(",").appendText(source).append(",").appendText(container)
				.append(",").appendText(original).append(",").appendText(date).append(",")
				.appendText(state).append("::text[])");
		UtilitaireDao.get(0).executeRequest(connection, query);
	}

	private static class CapturingVObjectService extends VObjectService {
		private List<ArcPreparedStatementBuilder> downloadQueries;

		CapturingVObjectService(Session session) {
			super(session);
		}

		@Override
		public File download(VObject view, String directory, List<String> names,
				List<ArcPreparedStatementBuilder> queries) {
			downloadQueries = queries;
			return new File("unused.zip");
		}
	}
}
