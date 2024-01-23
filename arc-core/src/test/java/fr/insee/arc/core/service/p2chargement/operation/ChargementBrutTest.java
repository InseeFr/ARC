package fr.insee.arc.core.service.p2chargement.operation;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.BoundedBufferedReader;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.ManipString;

public class ChargementBrutTest extends ChargementBrut {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	
	
	@Test
	public void boundedBufferedReaderTestLimit() throws IOException, ArcException {
		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();
		
		File fileTest = new File(repertoire, "test");
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/siera_ano.xml"),
				fileTest.toPath());
		
		String line1;
		String line2;
		String line3;
		
		 try(	FileInputStream is = new FileInputStream(fileTest);
				 InputStreamReader isr = new InputStreamReader(is);
		    		BufferedReader br = new BufferedReader(isr);)
		 {
			 line1 = br.readLine();
			 line2 = br.readLine();
			 line3 = br.readLine();
		 }
		 
		 try(	FileInputStream is = new FileInputStream(fileTest);
				 InputStreamReader isr = new InputStreamReader(is);
		    		BoundedBufferedReader br = new BoundedBufferedReader(isr);)
		 {
			 assertEquals(line1.substring(0, 5), br.readLine(5));
			 assertEquals(line2, br.readLine(10000));
			 assertEquals(line3.substring(0, 10), br.readLine(10));
		 }
		 
		 FileUtilsArc.deleteDirectory(root);
		 
	}
	
	@Test
	public void requeteFichierBrutalementTest() throws IOException, ArcException {
		File root = testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();
		
		File fileTest = new File(repertoire, "test");
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/siera_ano.xml"),
				fileTest.toPath());
		
		this.maxNumberOfLinesToRead=10;
		this.maxNumberOfCharacterByLineToRead=5;
		
		String query;
		
		 try(	FileInputStream is = new FileInputStream(fileTest);
				 InputStreamReader isr = new InputStreamReader(is);
		    		BoundedBufferedReader br = new BoundedBufferedReader(isr);)
		 {
			 query = requeteFichierBrutalement("siera_ano", br);
		 }
		
		 System.out.println(query);
		 
		 // extract id from query result
		 // the test checks implicitly that maxNumberOfCharacterByLineToRead=5
		 int id= Integer.valueOf(ManipString.substringBeforeLast(ManipString.substringAfterLast(query, "UNION ALL SELECT 'siera_ano',"),",'<n4ds'"));
		 
		 assertEquals(maxNumberOfLinesToRead-1, id);

	}
	
	
	
	
}
