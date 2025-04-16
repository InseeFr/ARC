package fr.insee.arc.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import fr.insee.arc.core.service.s3.ArcS3;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class ArcS3Test {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	
	@Before
	public void assumePropertiesNotEmpty() {
		
		try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(BatchConfig.class))
		{
			context.getBean(PropertiesHandler.class);
			PropertiesHandler.getInstance();
		}
		
		Assume.assumeFalse(PropertiesHandler.getInstance().getS3InputApiUri().isEmpty());
	}

	@Test
	public void executeArcS3Tests() throws ArcException, IOException {
		
		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();
		
		scenarioCreateDeleteDir();
		
		scenarioWithExtFile(repertoire);
		
	}
	
	private void scenarioCreateDeleteDir() throws ArcException {
		ArcS3.INPUT_BUCKET.createDirectory("test/"); // créer un répertoire
		assertTrue(ArcS3.INPUT_BUCKET.isDirectory("test/"));
		assertFalse(ArcS3.INPUT_BUCKET.isDirectory("test/.exists"));
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/.exists"));
		assertEquals(1, ArcS3.INPUT_BUCKET.size("test/"));

		ArcS3.INPUT_BUCKET.deleteDirectory("test/"); // supprimer un répertoire
		assertFalse(ArcS3.INPUT_BUCKET.isDirectory("test/"));
	}
	
	private void scenarioWithExtFile(String repertoire) throws ArcException, IOException {
		ArcS3.INPUT_BUCKET.createDirectory("test/foo/"); // créer plusieurs répertoires
		assertTrue(ArcS3.INPUT_BUCKET.isDirectory("test/"));
		assertFalse(ArcS3.INPUT_BUCKET.isDirectory("foo"));
		assertTrue(ArcS3.INPUT_BUCKET.isDirectory("test/foo/"));
		
		File testFile = new File(repertoire, "testFile.txt"); // fichier pour les tests suivants
		testFile.createNewFile();
		FileWriter testFileWriter = new FileWriter(testFile);
		testFileWriter.write("Hello World");
		testFileWriter.close();
		
		ArcS3.INPUT_BUCKET.upload(testFile, "test/testFile.txt"); // uploader un fichier
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/testFile.txt"));
		assertFalse(ArcS3.INPUT_BUCKET.isDirectory("test/testFile.txt"));
		assertEquals(11, ArcS3.INPUT_BUCKET.size("test/testFile.txt"));
		
		ArcS3.INPUT_BUCKET.move("test/testFile.txt", "test/foo/testFile.txt"); // déplacer un fichier
		assertFalse(ArcS3.INPUT_BUCKET.isExists("test/testFile.txt"));
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/foo/testFile.txt"));
		assertEquals(11, ArcS3.INPUT_BUCKET.size("test/foo/testFile.txt"));
		
		ArcS3.INPUT_BUCKET.copy("test/foo/testFile.txt", "test/testFile.txt"); // copier un fichier
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/testFile.txt"));
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/foo/testFile.txt"));
		assertEquals(24, ArcS3.INPUT_BUCKET.size("test/"));
		
		// test des différentes listes
		List<String> listTTTExpected = absolutePathList(Arrays.asList("test/.exists", "test/testFile.txt", "test/foo/.exists", "test/foo/testFile.txt"));
		List<String> listTTTActual = ArcS3.INPUT_BUCKET.listObjectsInDirectory("test/", true, true, true);
		assertTrue(areSameList(listTTTExpected, listTTTActual));
		
		List<String> listTFTExpected = absolutePathList(Arrays.asList("test/testFile.txt", "test/foo/testFile.txt"));
		List<String> listTFTActual = ArcS3.INPUT_BUCKET.listObjectsInDirectory("test/", true, false, true);
		assertTrue(areSameList(listTFTExpected, listTFTActual));
		
		List<String> listFTTExpected = absolutePathList(Arrays.asList("test/.exists", "test/testFile.txt", "test/foo/"));
		List<String> listFTTActual = ArcS3.INPUT_BUCKET.listObjectsInDirectory("test/", false, true, true);
		assertTrue(areSameList(listFTTExpected, listFTTActual));
		
		List<String> listFTFExpected = absolutePathList(Arrays.asList("test/.exists", "test/testFile.txt"));
		List<String> listFTFActual = ArcS3.INPUT_BUCKET.listObjectsInDirectory("test/", false, true, false);
		assertTrue(areSameList(listFTFExpected, listFTFActual));
		
		List<String> listFFTExpected = absolutePathList(Arrays.asList("test/testFile.txt", "test/foo/"));
		List<String> listFFTActual = ArcS3.INPUT_BUCKET.listObjectsInDirectory("test/", false, false, true);
		assertTrue(areSameList(listFFTExpected, listFFTActual));
		
		List<String> listFFFExpected = absolutePathList(Arrays.asList("test/testFile.txt"));
		List<String> listFFFActual = ArcS3.INPUT_BUCKET.listObjectsInDirectory("test/", false, false, false);
		assertTrue(areSameList(listFFFExpected, listFFFActual));

		ArcS3.INPUT_BUCKET.moveDirectory("test/foo/", "test/bar/"); // déplacer/renommer un répertoire
		assertFalse(ArcS3.INPUT_BUCKET.isExists("test/foo/"));
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/bar/"));
		assertFalse(ArcS3.INPUT_BUCKET.isExists("test/foo/testFile.txt"));
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/bar/testFile.txt"));
		
		File testDownloadFile = new File(repertoire, "testDownloadFile.txt");
		ArcS3.INPUT_BUCKET.download("test/bar/testFile.txt", testDownloadFile.getAbsolutePath()); // télécharger un fichier
		assertEquals(11, testDownloadFile.length());

		ArcS3.INPUT_BUCKET.delete("test/testFile.txt"); // supprimer un fichier
		assertFalse(ArcS3.INPUT_BUCKET.isExists("test/testFile.txt"));
		assertTrue(ArcS3.INPUT_BUCKET.isExists("test/bar/testFile.txt"));
		
		ArcS3.INPUT_BUCKET.deleteDirectory("test/"); // supprimer un répertoire
		assertFalse(ArcS3.INPUT_BUCKET.isDirectory("test/"));
		assertFalse(ArcS3.INPUT_BUCKET.isExists("test/bar/testFile.txt"));
	}
	
	private boolean areSameList(List<?> listExpected, List<?> listActual) {
		return listExpected.size() == listActual.size() && listActual.containsAll(listExpected);
	}

	private List<String> absolutePathList(List<String> relativePathList) {
		return relativePathList.stream().map(ArcS3.INPUT_BUCKET::absolutePath).toList();
	}

}
