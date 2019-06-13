package fr.insee.arc_essnet.core.service.chargeur;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import org.junit.Before;
import org.junit.Test;

import com.opencsv.CSVReader;

import fr.insee.arc_essnet.core.model.Norme;
import fr.insee.arc_essnet.core.util.RegleChargement;
import fr.insee.arc_essnet.core.util.TypeChargement;

public class LoaderCSVTest {

    private static final String FOLDER = "src/test/resources/fichier_chargement/";
    private static final File FILENAME_NOQUOTE = new File(FOLDER + "fichier_test_noquote.csv");
    private static final File FILENAME_QUOTE = new File(FOLDER + "fichier_test_quote.csv");
    private static final File FILENAME_HEADER = new File(FOLDER + "fichier_test_header.csv");
    private static final File FILENAME_HEADER_COMMA_NOQUOTE = new File(FOLDER + "fichier_test_header_comma_noquote.csv");
    private LoaderCSV chargeurCsv = new LoaderCSV();
    
    
    @Before
    public void initialiser(){
        
    }
    
    
    /*
     * on détecte bien une quote 
     */
    @Test
    public void testGetQuote() throws Exception {
        //GIVEN
        //On récupère le fichier
        FileReader fr = new FileReader(FILENAME_QUOTE);
        
        //WHEN
        
        String quoteActual = chargeurCsv.getQuote(fr);
              
        //THEN
        
        assertEquals("\"",quoteActual);
    }
    
    /*
     * on détecte rien
     */
    @Test
    public void testGetNOQuote() throws Exception {
        //GIVEN
        //On récupère le fichier
        FileReader fr = new FileReader(FILENAME_NOQUOTE);
        
        //WHEN
        
        String quoteActual = chargeurCsv.getQuote(fr);
              
        //THEN
        
        assertEquals(null,quoteActual);
    }

    @Test
        public void testReadHeaders() throws Exception {
            //GIVEN
            BufferedReader bf = new BufferedReader(new FileReader(FILENAME_HEADER));
            CSVReader readerCSV = new CSVReader(bf, ';');
            //WHEN
            
            String[] headersActual = chargeurCsv.readHeaders(readerCSV);
            //THEN
            
            String[] headersExpected = new String[]{"titre1", "titre2", "titre3", "titre4", "titre5"};
            
            assertArrayEquals(headersExpected, headersActual);
        }


    @Test
        public void testDetermineHeaders() throws Exception {
    	//GIVEN
    	chargeurCsv.setStreamHeader(new FileInputStream(FILENAME_HEADER));
    	RegleChargement actualRegleChargement = new RegleChargement(TypeChargement.PLAT, ";", "\"");
    	Norme actualNorme = new Norme();
    	actualNorme.setRegleChargement(actualRegleChargement);
    	chargeurCsv.setCurrentNorme(actualNorme);
    
    	
    	//WHEN
    	chargeurCsv.determineHeaders();
    	
    	//THEN
    	 String[] headersExpected = new String[]{"titre1", "titre2", "titre3", "titre4", "titre5"};
    	 assertArrayEquals(headersExpected, chargeurCsv.getHeaders());
        }

    @Test
    public void testDetermineHeadersNoQuote() throws Exception {
	//GIVEN
	chargeurCsv.setStreamHeader(new FileInputStream(FILENAME_HEADER));
	RegleChargement actualRegleChargement = new RegleChargement(TypeChargement.PLAT, ";", null);
	Norme actualNorme = new Norme();
	actualNorme.setRegleChargement(actualRegleChargement);
	chargeurCsv.setCurrentNorme(actualNorme);

	
	//WHEN
	chargeurCsv.determineHeaders();
	
	//THEN
	 String[] headersExpected = new String[]{"titre1", "titre2", "titre3", "titre4", "titre5"};
	 assertArrayEquals(headersExpected, chargeurCsv.getHeaders());
    }
    
    @Test
    public void testDetermineHeadersNoQuoteInFile() throws Exception {
	//GIVEN
	chargeurCsv.setStreamHeader(new FileInputStream(FILENAME_HEADER));
	RegleChargement actualRegleChargement = new RegleChargement(TypeChargement.PLAT, ";", null);
	Norme actualNorme = new Norme();
	actualNorme.setRegleChargement(actualRegleChargement);
	chargeurCsv.setCurrentNorme(actualNorme);

	
	//WHEN
	chargeurCsv.determineHeaders();
	
	//THEN
	 String[] headersExpected = new String[]{"titre1", "titre2", "titre3", "titre4", "titre5"};
	 assertArrayEquals(headersExpected, chargeurCsv.getHeaders());
    }
    
    @Test
    public void testDetermineHeadersErrrorSeparatorDeclaration() throws Exception {
	//GIVEN
	chargeurCsv.setStreamHeader(new FileInputStream(FILENAME_HEADER_COMMA_NOQUOTE));
	RegleChargement actualRegleChargement = new RegleChargement(TypeChargement.PLAT, ";", "\"");
	Norme actualNorme = new Norme();
	actualNorme.setRegleChargement(actualRegleChargement);
	chargeurCsv.setCurrentNorme(actualNorme);

	
	//WHEN
	chargeurCsv.determineHeaders();
	
	//THEN
	 String[] headersExpected = new String[]{"titre1", "titre2", "titre3", "titre4", "titre5"};
	 assertThat(chargeurCsv.getHeaders()).doesNotContain(headersExpected);
    }

}
