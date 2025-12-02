package fr.insee.arc.core.service.p1reception.registerfiles.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class FileRegistrationDaoTest extends InitializeQueryTest {

	private String tablePil = "arc_bas1.pilotage_fichier";
	private String tablePilTemp = "arc_bas1.pilotage_fichier_tmp";
	private FileRegistrationDao dao;

	@BeforeEach
	public void initDatabaseBeforeTest() throws SQLException, ArcException {
		buildPropertiesWithoutScalability("tmp");

		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE");
		u.executeRequest(c, "CREATE SCHEMA arc_bas1");

		u.executeRequest(c, "CREATE TABLE " + tablePilTemp + " (container text, id_source text)");
		u.executeRequest(c, "CREATE TABLE " + tablePil
				+ " (container text, id_source text, phase_traitement text, etat_traitement text[], to_delete text)");

		// f1 is a duplicate
		u.executeRequest(c, "INSERT INTO " + tablePilTemp + " select 'c1', 'f1'");
		u.executeRequest(c, "INSERT INTO " + tablePilTemp + " select 'c1', 'f1'");

		// f2 not a duplicate
		u.executeRequest(c, "INSERT INTO " + tablePilTemp + " select 'c1', 'f2'");

		// f3 is a duplicate as it is found in files received (tablePilTemp) and also in
		// pilotage table (tablePil)
		// and it is not set as to be replayed
		u.executeRequest(c, "INSERT INTO " + tablePilTemp + " select 'c1', 'f3'");
		u.executeRequest(c, "INSERT INTO " + tablePil + " select 'c2', 'f3', 'RECEPTION', '{OK}', null");
		u.executeRequest(c, "INSERT INTO " + tablePil + " select 'c2', 'f3', 'CHARGEMENT', '{OK}', null");

		// f4 is not a duplicate as it is found in files received (tablePilTemp) and
		// also in pilotage table (tablePil)
		// but as it is set to be replayed, it doesn't count as duplicate
		u.executeRequest(c, "INSERT INTO " + tablePilTemp + " select 'c1', 'f4'");
		u.executeRequest(c, "INSERT INTO " + tablePil + " select 'c3', 'f4', 'RECEPTION', '{OK}', null");
		u.executeRequest(c, "INSERT INTO " + tablePil + " select 'c3', 'f4', 'CHARGEMENT', '{OK}', 'R'");

		// f5 is set as replayed but not found among the new files received
		u.executeRequest(c, "INSERT INTO " + tablePil + " select 'c4', 'f5', 'RECEPTION', '{OK}', 'R'");

		this.dao = new FileRegistrationDao(new Sandbox(c, "arc_bas1"), tablePilTemp);
	}

	@AfterEach
	public void cleanDatabaseAfterTest() throws SQLException, ArcException {
		// clear
		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeRequest(c, "DISCARD TEMP;");
	}

	@Test
	public void querySelectFilesMarkedToReplayTest() throws ArcException, SQLException {

		// get files marked as to be replayed (R tag) in pilotage and also found among
		// the new received files
		// f4 in that case
		List<String> filesToTest = dao.execQuerySelectFilesMarkedToReplay();
		assertTrue(filesToTest.contains("f4"));
		assertEquals(1, filesToTest.size());

		// delete the files from the pilotage table marked ad R and found in the
		// received pool
		dao.execQueryDeleteFilesMarkedToReplay(filesToTest);

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT count(*) as c from " + tablePil);
		int numberOfLine = UtilitaireDao.get(0).getInt(c, query);

		// only 3 lines should be left in pilotage table (pilotage lines of the f3 and
		// f5 files).
		// Other should had been be deleted.
		assertEquals(3, numberOfLine);

	}

	@Test
	public void execQueryFindDuplicateFilesTest() throws ArcException, SQLException {
		// find duplicates
		List<String> filesToTest = dao.execQueryFindDuplicateFiles();
		assertTrue(filesToTest.contains("f1"));
		assertTrue(filesToTest.contains("f3"));
		assertEquals(2, filesToTest.size());
	}

	@Test
	public void execQueryFindFilesMarkedAsReplayTest() throws ArcException, SQLException {

		List<String> listContainerARejouer = new ArrayList<>();
		List<String> listIdsourceARejouer = new ArrayList<>();

		// In the received files (tablePilTemp), found the ones that are marked to be
		// replayed (in tablePil)
		dao.execQueryFindFilesMarkedAsReplay(listContainerARejouer, listIdsourceARejouer);
		assertTrue(listContainerARejouer.contains("c1"));
		assertTrue(listIdsourceARejouer.contains("c1" + File.separator + "f4"));
		assertEquals(1, listContainerARejouer.size());
		assertEquals(1, listIdsourceARejouer.size());

	}

}
