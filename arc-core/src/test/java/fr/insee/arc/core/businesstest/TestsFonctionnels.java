package fr.insee.arc.core.businesstest;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

		// TEST ON DATA
		ArcPreparedStatementBuilder query;
		List<String> queryRecords;
		List<List<String>> expectedRawRecords;
		List<String> expectedRecords;
		
		// test on data : remuneration
		query= new ArcPreparedStatementBuilder();
		
		query.build("WITH ttt as(");
		query.build("SELECT id_remuneration, unnest(typrem) as typrem from ",sandbox,".mapping_dsn_remuneration_ok order by typrem)");
		query.build("SELECT id_remuneration, array_agg(typrem order by typrem) as typrem from ttt group by id_remuneration order by typrem");
		queryRecords = new GenericBean(
				UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("typrem");
		Collections.sort(queryRecords);	
		
		expectedRecords = new ArrayList<>();
		expectedRawRecords = new ArrayList<>();
		
		expectedRawRecords.add(Arrays.asList("ARV04","ARV17","VFV","VPE","BAS02","BAS03","BAS14"));
		expectedRawRecords.add(Arrays.asList("ARV06","VFV","VPE","BAS17","BAS18"));
		expectedRawRecords.add(Arrays.asList("REM001","REM002","ARV04","ARV06","VFV","VPE","BAS02","BAS03","BAS04","BAS14"));
		expectedRawRecords.add(Arrays.asList("REM001","REM002","ARV04","ARV06","VFV","VPE","BAS12","BAS13"));
		expectedRawRecords.add(Arrays.asList("REM001","REM002","ARV06","VFV","VPE","BAS17","BAS18"));
		expectedRawRecords.add(Arrays.asList("REM001","REM002","REM003","REM010","VFV","VPE","BAS02","BAS23"));
		expectedRawRecords.add(Arrays.asList("REM001","REM002","REM003","REM022","PRI027","PRI028","VFV","VPE","BAS02","BAS03","BAS03","BAS48"));
		expectedRawRecords.add(Arrays.asList("REM001","REM003","REM010","PRI027","PRI028","VFV","VPE","BAS02","BAS03","BAS03","BAS48"));
		expectedRawRecords.add(Arrays.asList("REM002","REM003","REM010","REM012","REM001","ARV04","ARV06","VFV","VPE","BAS12","BAS13"));
		expectedRawRecords.add(Arrays.asList("REM010","REM012","REM013","REM014","ARV04","ARV06","VFV","VPE","BAS02","BAS03","BAS04","BAS14"));
		expectedRawRecords.add(Arrays.asList("REM020","REM025","REM026","ARV04","ARV17","VFV","VPE","BAS02","BAS03","BAS14"));
		expectedRawRecords.add(Arrays.asList("VFV","VPE","BAS02","BAS23"));

		expectedRawRecords.stream().forEach(r -> {
			List<String> values = r;
			Collections.sort(values);
			expectedRecords.add(values.toString().replace(" ","").replace("[", "{").replace("]", "}"));
		});
		Collections.sort(expectedRecords);

		assertArrayEquals(expectedRecords.toArray(), queryRecords.toArray());
		
		// test on data : poste
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "coalesce(cpost_lieu_trav,'') as cpost_lieu_trav", SQL.FROM, sandbox, ".mapping_dsn_poste_ok");
		queryRecords = new GenericBean(
				UtilitaireDao.get(0).executeRequest(c, query)).getColumnValues("cpost_lieu_trav");
		Collections.sort(queryRecords);

		List<String> expectedRecords2 = Arrays.asList("","" , "", "", "", "", "51100", "78200");
		Collections.sort(expectedRecords2);
		assertArrayEquals(expectedRecords2.toArray(), queryRecords.toArray());
		
		// test on data : employeur
		query = new ArcPreparedStatementBuilder();
		int nbEnrInEmployeur = UtilitaireDao.get(0).getInt(c, "SELECT count(*) FROM "+sandbox+".mapping_dsn_employeur_ok" );
		assertEquals(1, nbEnrInEmployeur);
		
		// start export phase
		ApiServiceFactory.getService(TraitementPhase.EXPORT, sandbox, 10000000, batchMode).invokeApi();
		
		File fileOfParquetRootDirectory = new File(DirectoryPathExport.directoryExport(repertoire,"ARC_BAS2", ExportDao.EXPORT_CLIENT_NAME));
		
		// only one directory should had been created
		assertEquals(1,fileOfParquetRootDirectory.listFiles().length);
		
		// retrieve date of export
		String dateOfExport = fileOfParquetRootDirectory.listFiles()[0].getName();
		
		File fileOfParquetDirectory = new File(DirectoryPathExport.directoryExport(repertoire,"ARC_BAS2", ExportDao.EXPORT_CLIENT_NAME, dateOfExport));

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
