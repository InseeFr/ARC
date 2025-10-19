package fr.insee.arc.web.gui.pilotage.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.BatchEtat;
import fr.insee.arc.core.model.DataWarehouse;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.pilotage.model.ViewArchiveBAS;
import fr.insee.arc.web.gui.pilotage.model.ViewEntrepotBAS;
import fr.insee.arc.web.gui.pilotage.model.ViewFichierBAS;
import fr.insee.arc.web.gui.pilotage.model.ViewPilotageBAS;
import fr.insee.arc.web.gui.pilotage.model.ViewRapportBAS;

public class PilotageDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static PilotageDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		
		BddPatcherTest.createDatabase();
		
		Session session = new Session();
		HttpSessionTemplate z = new HttpSessionTemplate();
		session.setHttpSession(z);
		vObjectService = new VObjectService(session);
		vObjectService.setConnection(c);

		dao = new DataObjectService();
		dao.setSandboxSchema(BddPatcherTest.testSandbox1);
		pdao = new PilotageDao();
		pdao.initialize(vObjectService, dao);
	}
	
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void initializePilotageBAS() {

		VObject viewPilotageBAS = new ViewPilotageBAS();
		
		// execute query
		pdao.initializePilotageBAS(viewPilotageBAS);
		
		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.VIEW_PILOTAGE_FICHIER.getColumns().keySet());

		assertTrue(viewPilotageBAS.getHeadersDLabel().containsAll(viewColumns));

	}
	
	@Test
	public void initializeRapportBAS() {

		VObject viewRapportBAS = new ViewRapportBAS();
		
		// execute query
		pdao.initializeRapportBAS(viewRapportBAS);
		
		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.VIEW_RAPPORT_FICHIER.getColumns().keySet());

		assertTrue(viewRapportBAS.getHeadersDLabel().containsAll(viewColumns));

	}
	
	@Test
	public void initializeArchiveBAS() throws ArcException {

		VObject viewArchiveBAS = new ViewArchiveBAS();
		VObject viewEntrepotBAS = new ViewEntrepotBAS();
		
		// select entrepot
		viewEntrepotBAS.setCustomValue("entrepotLecture", "DEFAULT");
		
		// execute query
		pdao.initializeArchiveBAS(viewArchiveBAS, viewEntrepotBAS);
		
		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.PILOTAGE_ARCHIVE.getColumns().keySet());

		assertTrue(viewArchiveBAS.getHeadersDLabel().containsAll(viewColumns));

	}
	
	@Test
	public void downloadFichierBAS() throws ArcException, IOException, SQLException {

		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();

		String testSandbox1 = "arc_bas1";
		VObject viewFichierBAS = new ViewFichierBAS();
		Map<String, String> defaultInputFields = new HashMap<>();
		vObjectService.initialize(viewFichierBAS, new ArcPreparedStatementBuilder("SELECT 'DEFAULT_insee.xml' as id_source, 'insee' as container"), null, defaultInputFields);		
		vObjectService.getSession().put(viewFichierBAS.getSessionName(), viewFichierBAS);

		// créer le répertoire source
		String repertoireDeDepot = DirectoryPath.directoryReceptionEntrepot(repertoire, testSandbox1,
				DataWarehouse.DEFAULT.getName());
		FileUtilsArc.createDirIfNotexist(repertoireDeDepot);
				
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("fr/insee/testfiles/insee.xml"),
				new File(repertoireDeDepot, "insee.xml").toPath());
		
		// select file
		Map<String, List<String>> viewFichierBASSelectedRecords = new HashMap<String, List<String>>();
		viewFichierBASSelectedRecords.put("id_source", List.of("DEFAULT_insee.xml"));
		pdao.setSelectedRecords(viewFichierBASSelectedRecords);

		// créer le répertoire cible du download
		Path dirOut = Paths.get(FileSystemManagement.directoryEnvDownload(repertoire, testSandbox1));
		FileUtilsArc.createDirIfNotexist(dirOut.toFile());
		
		// 0 file before download in target directory
		assertEquals(0,dirOut.toFile().listFiles().length);
		
		// download
		pdao.downloadFichierBAS(viewFichierBAS, dirOut.toString(), repertoire, testSandbox1);
		
		// 1 file before download in target directory
		assertEquals(1,dirOut.toFile().listFiles().length);
	}
	
	@Test
	public void downloadEnvelopeBAS() throws ArcException, IOException, SQLException {

		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();

		String testSandbox1 = "arc_bas1";
		VObject viewFichierBAS = new ViewFichierBAS();
		Map<String, String> defaultInputFields = new HashMap<>();
		vObjectService.initialize(viewFichierBAS, new ArcPreparedStatementBuilder("SELECT 'DEFAULT_insee.xml' as id_source, 'insee' as container"), null, defaultInputFields);		
		vObjectService.getSession().put(viewFichierBAS.getSessionName(), viewFichierBAS);

		// créer le répertoire source
		String repertoireDeDepot = DirectoryPath.directoryReceptionEntrepot(repertoire, testSandbox1,
				DataWarehouse.DEFAULT.getName());
		FileUtilsArc.createDirIfNotexist(repertoireDeDepot);
				
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("fr/insee/testfiles/insee.xml"),
				new File(repertoireDeDepot, "insee.xml").toPath());
		
		List<String> listRepertoire = new ArrayList<>();
		
		// select file
		Map<String, List<String>> viewFichierBASSelectedRecords = new HashMap<String, List<String>>();
		viewFichierBASSelectedRecords.put("id_source", List.of("DEFAULT_insee.xml"));
		pdao.setSelectedRecords(viewFichierBASSelectedRecords);

		// créer le répertoire cible du download
		Path dirOut = Paths.get(FileSystemManagement.directoryEnvDownload(repertoire, testSandbox1));
		FileUtilsArc.createDirIfNotexist(dirOut.toFile());
		
		// 0 file before download in target directory
		assertEquals(0,dirOut.toFile().listFiles().length);
		
		// download
		pdao.downloadEnvelopeBAS(viewFichierBAS, dirOut.toString(), repertoire, listRepertoire);
		
		// 1 file before download in target directory
		assertEquals(1,dirOut.toFile().listFiles().length);
	}
	
	@Test
	public void execQueryStateTest() throws ArcException, SQLException
	{
		buildPropertiesWithoutScalability("tmp");

		// production batch is active by default after database creation
		assertEquals("active",pdao.execQueryState());
		
		// toggle production batch to off
		pdao.execQueryToggleOff();
		// check if inactive
		assertEquals("inactive",pdao.execQueryState());

		// toggle production batch to on
		pdao.execQueryToggleOn();
		// check if active again		
		assertEquals("active",pdao.execQueryState());
		
	}
	

}
