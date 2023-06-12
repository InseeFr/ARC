package fr.insee.arc.utils.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class FileUtilsArcTest {

	@Rule
	public TemporaryFolder testFolder= new TemporaryFolder();

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(FileUtilsArc.class);
	}
	
	
	@Test
	/** Test if deleteDirectory function delete the target directory and its content
	 * @throws IOException 
	 * @throws ArcException 
	 */
	public void deleteDirectoryTest() throws IOException, ArcException {

		File root=testFolder.newFolder("root");
		File testDir=clearAndCreateTestDirectory(root);
		
		// create files at root level
		new File(testDir,"file1.txt").createNewFile();
		new File(testDir,"file2.txt").createNewFile();
		
		// create dir1 with one file inside
		File dir1 = new File(testDir, "dir1");
		dir1.mkdir();
		new File(dir1,"file11.txt").createNewFile();
		
		// create dir2 empty
		File dir2 = new File(testDir, "dir2");
		dir2.mkdir();
		
		// create dir3 with on directory + file inside
		File dir3 = new File(testDir, "dir3");
		dir3.mkdir();
		
		File dir4 = new File(dir3, "dir4");
		dir4.mkdir();
		
		new File(dir4,"file41.txt").createNewFile();
		
		FileUtilsArc.deleteDirectory(testDir);

		assertEquals(0,root.listFiles().length);
		
	}
	
	@Test(expected = ArcException.class)
	/** Test for not existing directory
	 *  must return false
	 * @throws IOException
	 * @throws ArcException 
	 */
	public void deleteDirectoryTestDirectoryNotExists() throws IOException, ArcException {

		File root=testFolder.newFolder("root");
		File testDir=clearAndCreateTestDirectory(root);
		
		
		File directoryNotExists=new File(testDir, "dirnotexists");
		
		FileUtilsArc.deleteDirectory(directoryNotExists);

	}
	
	
	/**
	 * Test if deleteAndRecreateDirectory erase the content of target directory and recreate it well
	 * @throws IOException
	 * @throws ArcException 
	 */
	@Test
	public void deleteAndRecreateDirectoryTest() throws IOException, ArcException {
		File root=testFolder.newFolder("root");
		File testDir=clearAndCreateTestDirectory(root);
		
		File directoryToTest=new File(testDir, "directoryToTest");
		directoryToTest.mkdir();
		
		File fileInDirectoryToTest=new File(directoryToTest,"fileInDirectoryToTest.txt");
		fileInDirectoryToTest.createNewFile();
		
		File directoryInsideDirectoryToTest=new File(directoryToTest, "subfolderOfDirectoryToTest");
		directoryInsideDirectoryToTest.mkdir();
		
		// file in directory must exists at this time
		assertEquals(true, fileInDirectoryToTest.exists());
		// sub directory inside the directory to test must exists at this time
		assertEquals(true, directoryInsideDirectoryToTest.exists());

		FileUtilsArc.deleteAndRecreateDirectory(directoryToTest);
		
		// directoryToTest must exist
		assertEquals(true, directoryToTest.exists());
		// file in directory must have been erased
		assertEquals(false, fileInDirectoryToTest.exists());
		// sub directory inside the directory to test must had been erased
		assertEquals(false, directoryInsideDirectoryToTest.exists());
	}

	/**
	 * Test is file is file is completly written
	 * @throws IOException
	 * @throws ArcException 
	 */
	@Test
	public void isCompletelyWrittenTest() throws IOException, ArcException
	{
		File root=testFolder.newFolder("root");
		File testDir=clearAndCreateTestDirectory(root);
		
		File fileInDirectoryToTest=new File(testDir,"fileInDirectory.txt");
		fileInDirectoryToTest.createNewFile();
		
		assertEquals(true, FileUtilsArc.isCompletelyWritten(fileInDirectoryToTest));
			
	}
	
	@Test
	public void renameToTest_OK() throws IOException, ArcException
	{
		File root=testFolder.newFolder("root");
		File testDir=clearAndCreateTestDirectory(root);
		
		File fileToRename=new File(testDir,"fileToRename.txt");
		fileToRename.createNewFile();

		// vérifier que le fichier fileToRename existe bien
		assertTrue(fileToRename.exists());

		File fileRenamed=new File(testDir,"fileRenamed.txt");
		// le fichier fileRenamed n'existe pas
		assertFalse(fileRenamed.exists());

		
		// renommage de fileToRename en fileRenamed
		FileUtilsArc.renameTo(fileToRename, fileRenamed);
		
		// le fichier a-t-il été bien renommé ?
				// l'opération de renommage a du fonctionner
		// fileToRename ne doit plus exister
		assertFalse(fileToRename.exists());
		// fileRenamed doit exister
		assertTrue(fileRenamed.exists());
		
	}

	
	@Test
	public void createDirIfNotexistTest() throws IOException, ArcException
	{
		File root=testFolder.newFolder("root");
		File testDir=clearAndCreateTestDirectory(root);
		
		File testDirABC=new File(testDir,"/a/b/c");
		String testDirPath=testDirABC.getAbsoluteFile().toString();
		File testDirB=new File(testDir,"/a/b");
		File testDirA=new File(testDir,"/a");
		
		// Création du répertoire A
		testDirA.mkdir();
		assertTrue(testDirA.exists());
		
		// avant la création les répertoire B et C n'existent pas
		assertFalse(testDirB.exists());
		assertFalse(testDirABC.exists());

		// création des répertoires en cascade
		FileUtilsArc.createDirIfNotexist(testDirPath);
		
		// aprés la création le repértoire intermédiaire B a été créé ainsi que le répertoire final
		assertTrue(testDirA.exists());
		assertTrue(testDirB.exists());
		assertTrue(testDirABC.exists());

	}
	
	private File clearAndCreateTestDirectory(File root) throws ArcException
	{
		File testDir=new File(root,"testDir");
		if (testDir.exists())
		{
			FileUtilsArc.deleteDirectory(testDir);
		}
		testDir.mkdir();
		return testDir;
	}
	
	
}
