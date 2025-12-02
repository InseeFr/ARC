package fr.insee.arc.core.businesstest;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.DataWarehouse;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.service.p6export.dao.ExportDao;
import fr.insee.arc.core.service.p6export.provider.DirectoryPathExport;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class TestsFonctionnels extends InitializeQueryTest {

	@TempDir
	public File testFolder;

	@Test
	public void executeFunctionnalTests() throws IOException, SQLException, ArcException {
		BddPatcherTest.createDatabase();

		File root= new File(testFolder, "root");
		root.mkdir();
				
		String repertoire = root.getAbsolutePath();

		buildPropertiesWithoutScalability(repertoire);

		executeTestSirene("arc_bas1", repertoire);

		executeTestSiera("arc_bas2", repertoire);

		executeTestAnimal("arc_bas8", repertoire);

	}

	/**
	 * COVERAGE complex xml load test
	 * 
	 * @param sandbox
	 * @param repertoire
	 * @throws IOException
	 * @throws ArcException
	 * @throws SQLException
	 */
	private void executeTestSirene(String sandbox, String repertoire) throws IOException, ArcException, SQLException {
		BddPatcherTest.insertTestDataSirene();

		ApiServiceFactory.getService(TraitementPhase.INITIALISATION, sandbox, 10000000, null).invokeApi();

		String repertoireDeDepot = DirectoryPath.directoryReceptionEntrepot(repertoire, sandbox,
				DataWarehouse.DEFAULT.getName());

		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/Cas_test_V2008.11.zip"),
				new File(repertoireDeDepot, "Cas_test_V2008.11.zip").toPath());
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/Cas_test_V2016.02.zip"),
				new File(repertoireDeDepot, "Cas_test_V2016.02.zip").toPath());

		ApiServiceFactory.getService(TraitementPhase.RECEPTION, sandbox, 10000000, null).invokeApi();

		assertEquals(114, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT, sandbox, 10000000, null).invokeApi();
		assertEquals(114, nbFileInPhase(sandbox, TraitementPhase.CHARGEMENT, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.NORMAGE, sandbox, 10000000, null).invokeApi();
		assertEquals(114, nbFileInPhase(sandbox, TraitementPhase.NORMAGE, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.CONTROLE, sandbox, 10000000, null).invokeApi();
		assertEquals(47, nbFileInPhase(sandbox, TraitementPhase.CONTROLE, TraitementEtat.OK));

	}

	/**
	 * COVERAGE xml load test normage complexe rule test filtering controle rule
	 * test complex mapping rules test
	 * 
	 * @param sandbox
	 * @param repertoire
	 * @throws IOException
	 * @throws ArcException
	 * @throws SQLException
	 */
	private void executeTestSiera(String sandbox, String repertoire) throws IOException, ArcException, SQLException {
		BddPatcherTest.insertTestDataSiera();

		// to test batch mode
		String batchMode="1";
		
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION, sandbox, 10000000, batchMode).invokeApi();

		String repertoireDeDepot = DirectoryPath.directoryReceptionEntrepot(repertoire, sandbox,
				DataWarehouse.DEFAULT.getName());

		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/siera_ano.xml"),
				new File(repertoireDeDepot, "siera_ano.xml").toPath());

		ApiServiceFactory.getService(TraitementPhase.RECEPTION, sandbox, 10000000, batchMode).invokeApi();

		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT, sandbox, 10000000, batchMode).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.CHARGEMENT, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.NORMAGE, sandbox, 10000000, batchMode).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.NORMAGE, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.CONTROLE, sandbox, 10000000, batchMode).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.CONTROLE, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.MAPPING, sandbox, 10000000, batchMode).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.MAPPING, TraitementEtat.OK));

		// test on data
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "typrem", SQL.FROM, sandbox, ".mapping_dsn_remuneration_ok");
		Map<String, List<String>> dsnRemunerationRecords = new GenericBean(
				UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		List<String> expectedRecords = new ArrayList<String>();
		expectedRecords.add("{REM001,REM002,REM003,REM022,PRI027,PRI028,VFV,VPE,BAS02,BAS03,BAS03,BAS48}");
		expectedRecords.add("{REM001,REM003,REM010,PRI027,PRI028,VFV,VPE,BAS02,BAS03,BAS03,BAS48}");
		expectedRecords.add("{REM010,REM012,REM013,REM014,ARV04,ARV06,VFV,VPE,BAS02,BAS03,BAS04,BAS14}");
		expectedRecords.add("{REM001,REM002,ARV04,ARV06,VFV,VPE,BAS02,BAS03,BAS04,BAS14}");
		expectedRecords.add("{REM001,REM002,ARV04,ARV06,VFV,VPE,BAS12,BAS13}");
		expectedRecords.add("{REM002,REM003,REM010,REM012,REM001,ARV04,ARV06,VFV,VPE,BAS12,BAS13}");
		expectedRecords.add("{REM001,REM002,REM003,REM010,VFV,VPE,BAS02,BAS23}");
		expectedRecords.add("{VFV,VPE,BAS02,BAS23}");
		expectedRecords.add("{REM001,REM002,ARV06,VFV,VPE,BAS17,BAS18}");
		expectedRecords.add("{ARV06,VFV,VPE,BAS17,BAS18}");
		expectedRecords.add("{ARV04,ARV17,VFV,VPE,BAS02,BAS03,BAS14}");
		expectedRecords.add("{REM020,REM025,REM026,ARV04,ARV17,VFV,VPE,BAS02,BAS03,BAS14}");
		Collections.sort(expectedRecords);

		List<String> queryResults = dsnRemunerationRecords.get("typrem");
		Collections.sort(queryResults);

		assertArrayEquals(expectedRecords.toArray(), queryResults.toArray());
		
		// start export phase
		ApiServiceFactory.getService(TraitementPhase.EXPORT, sandbox, 10000000, batchMode).invokeApi();
		
		File fileOfParquetRootDirectory = new File(DirectoryPathExport.directoryExport(repertoire,"ARC_BAS2", ExportDao.EXPORT_CLIENT_NAME));
		
		// only one directory should had been created
		assertEquals(1,fileOfParquetRootDirectory.listFiles().length);
		
		// retrieve date of export
		String dateOfExport = fileOfParquetRootDirectory.listFiles()[0].getName();
		
		File fileOfParquetDirectory = new File(DirectoryPathExport.directoryExport(repertoire,"ARC_BAS2", ExportDao.EXPORT_CLIENT_NAME, dateOfExport));

		System.out.println(fileOfParquetDirectory.getAbsolutePath());
		
		// 4 tables should had been export to parquet
		assertEquals(4,fileOfParquetDirectory.listFiles().length);	

	}

	/**
	 * COVERAGE simple csv file load test tar.gz load test doublon detection test
	 * 
	 * @param sandbox
	 * @param repertoire
	 * @throws IOException
	 * @throws ArcException
	 * @throws SQLException
	 */
	private void executeTestAnimal(String sandbox, String repertoire) throws IOException, ArcException, SQLException {
		BddPatcherTest.insertTestDataAnimal();

		ApiServiceFactory.getService(TraitementPhase.INITIALISATION, sandbox, 10000000, null).invokeApi();

		String repertoireDeDepot = DirectoryPath.directoryReceptionEntrepot(repertoire, sandbox,
				DataWarehouse.DEFAULT.getName());

		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/animals.tar.gz"),
				new File(repertoireDeDepot, "animals.tar.gz").toPath());

		ApiServiceFactory.getService(TraitementPhase.RECEPTION, sandbox, 10000000, null).invokeApi();

		assertEquals(2, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT, sandbox, 10000000, null).invokeApi();
		assertEquals(2, nbFileInPhase(sandbox, TraitementPhase.CHARGEMENT, TraitementEtat.OK));

		// doublon detection test
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/animals-001.csv"),
				new File(repertoireDeDepot, "animals-001.csv").toPath());

		ApiServiceFactory.getService(TraitementPhase.RECEPTION, sandbox, 10000000, null).invokeApi();

		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.KO));

	}

	private int nbFileInPhase(String sandbox, TraitementPhase phase, TraitementEtat etat) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "count(*)", SQL.FROM, sandbox, ".", "pilotage_fichier", SQL.WHERE, "phase_traitement=",
				query.quoteText(phase.toString()), SQL.AND, "etat_traitement=",
				query.quoteText(etat.getSqlArrayExpression()), "::text[]");

		return UtilitaireDao.get(0).getInt(c, query);
	}

}
