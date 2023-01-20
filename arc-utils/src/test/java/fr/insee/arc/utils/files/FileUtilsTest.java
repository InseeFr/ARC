package fr.insee.arc.utils.files;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilsTest {

	@Rule
	public TemporaryFolder testFolder= new TemporaryFolder();
		
	
	@Test
	public void deleteDirectoryTest() throws IOException {

		File root=testFolder.newFolder("root");
		
		File testDir=new File(root,"testDir");
		testDir.mkdir();
		
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
		
		FileUtils.deleteDirectory(testDir);

		assertEquals(root.listFiles().length,0);
		
	}

}
