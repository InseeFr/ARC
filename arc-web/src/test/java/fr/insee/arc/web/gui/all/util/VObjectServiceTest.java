package fr.insee.arc.web.gui.all.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.web.gui.pilotage.dao.HttpSessionTemplate;
import fr.insee.arc.web.gui.pilotage.model.ViewFichierBAS;

public class VObjectServiceTest extends InitializeQueryTest {

	private static VObjectService vObjectService;

	@BeforeClass
	public static void setup() throws ArcException {
		
		BddPatcherTest.createDatabase();
		
		Session session = new Session();
		HttpSessionTemplate z = new HttpSessionTemplate();
		session.setHttpSession(z);
	
		vObjectService = new VObjectService(session);
		vObjectService.setConnection(c);

	}
	
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void download() throws ArcException, IOException, SQLException {

		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();
		
		buildPropertiesWithoutScalability(repertoire);

		String testSandbox1 = "arc_bas1";
		VObject viewFichierBAS = new ViewFichierBAS();
		Map<String, String> defaultInputFields = new HashMap<>();
		vObjectService.initialize(viewFichierBAS, new ArcPreparedStatementBuilder("SELECT 'DEFAULT_insee.xml' as id_source, 'insee' as container"), null, defaultInputFields);		
		vObjectService.getSession().put(viewFichierBAS.getSessionName(), viewFichierBAS);
		
		// créer le répertoire cible du download
		Path dirOut = Paths.get(FileSystemManagement.directoryEnvDownload(repertoire, testSandbox1));
		FileUtilsArc.createDirIfNotexist(dirOut.toFile());
		
		// 0 file before download in target directory
		assertEquals(0,dirOut.toFile().listFiles().length);
		
		// download
		vObjectService.download(viewFichierBAS, dirOut.toString(), List.of("test"), List.of(new ArcPreparedStatementBuilder("SELECT 1")));
		
		// 1 file before download in target directory
		assertEquals(1,dirOut.toFile().listFiles().length);
	}

}
