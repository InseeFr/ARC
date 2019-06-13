package fr.insee.arc_essnet.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import fr.insee.arc_essnet.core.model.Norme;
import fr.insee.arc_essnet.core.util.CustomTreeFormat;
import fr.insee.arc_essnet.core.util.RegleChargement;
import fr.insee.arc_essnet.core.util.TypeChargement;

public class CustomTreeFormatTest {

    @Test
    public void testArbreFormatOK() throws Exception {
        // GIVEN
        Norme normeGiven = new Norme();
        normeGiven.setIdNorme("1");
        normeGiven.setRegleChargement(new RegleChargement(
                TypeChargement.XML, "", "<root>"
                        + "\n<a>"
                        + "\n<aa/>"
                        + "\n<ab>"
                        + "\n<aba/>"
                        + "\n</ab>"
                        + "\n</a>"
                        + "\n<b>"
                        + "\n<ba>"
                        + "\n<baa/>"
                        + "\n<bab/>"
                        + "\n</ba>"
                        + "\n</b>"
                        + "\n</root>"));

        CustomTreeFormat arbreFormatActual = new CustomTreeFormat(normeGiven);


        // WHEN
        HashMap<String, String> arbreExpected = new HashMap<String, String>();
        arbreExpected.put("aa", "a");
        arbreExpected.put("aba", "ab");
        arbreExpected.put("baa", "ba");
        arbreExpected.put("bab", "ba");
        arbreExpected.put("ab", "a");
        arbreExpected.put("ba", "b");
        arbreExpected.put("b", "root");
        arbreExpected.put("a", "root");
        arbreExpected.put("root", null);

        // THEN
        
        assertEquals(arbreExpected,arbreFormatActual.getTheTree());
    }
    
    @Test (expected = SAXParseException.class)
    public void testArbreFormat_doublon() throws Exception {
        // GIVEN
        Norme normeGiven = new Norme();
        normeGiven.setIdNorme("1");
        normeGiven.setRegleChargement(new RegleChargement(
                TypeChargement.XML, "", "<root>"
                        + "\n<a>"
                        + "\n<aa/>"
                        + "\n<ab>"
                        + "\n<aba/>"
                        + "\n</ab>"
                        + "\n</a>"
                        + "\n<b>"
                        + "\n<ab>"
                        + "\n<baa/>"
                        + "\n<bab/>"
                        + "\n</ba>"
                        + "\n</b>"
                        + "\n</root>"));



        // WHEN
        
        CustomTreeFormat arbreFormatActual = new CustomTreeFormat(normeGiven);

        // THEN

    }
    
    
    @Test (expected = SAXParseException.class)
    public void testArbreFormat_pbForma() throws Exception {
        // GIVEN
        Norme normeGiven = new Norme();
        normeGiven.setIdNorme("1");
        normeGiven.setRegleChargement(new RegleChargement(
                TypeChargement.XML, "", "<root>"
                        + "\n<a>"
                        + "\n<aa/>"
                        + "\n<ab>"
                        + "\n<aba/>"
                        + "\n</ab>"
                        + "\n</a>"
                        + "\n<b>"
                        + "\n<ab>"
                        + "\n<baa/>"
                        + "\n<bab/>"
                        + "\n</ba>"
                        + "\n</b"
                        + "\n</root>"));



        // WHEN
        
        CustomTreeFormat arbreFormatActual = new CustomTreeFormat(normeGiven);

        // THEN

    }

    
    
    @Test
        public void testFindLeafs() throws Exception {
            //GIVEN
            Norme normeGiven = new Norme();
            normeGiven.setIdNorme("1");
            normeGiven.setRegleChargement(new RegleChargement(
                    TypeChargement.XML, "", "<root>"
                            + "\n<a>"
                            + "\n<aa/>"
                            + "\n<ab>"
                            + "\n<aba/>"
                            + "\n</ab>"
                            + "\n</a>"
                            + "\n<b>"
                            + "\n<ba>"
                            + "\n<baa/>"
                            + "\n<bab/>"
                            + "\n</ba>"
                            + "\n</b>"
                            + "\n</root>"));
    
            CustomTreeFormat arbreFormatActual = new CustomTreeFormat(normeGiven);
            
            //WHEN
            List<String> listeFeuillesExpeted = new ArrayList<String>();
            listeFeuillesExpeted.add("AA");
            listeFeuillesExpeted.add("ABA");
            listeFeuillesExpeted.add("BAB");
            listeFeuillesExpeted.add("BAA");
    
            List<String> listeBrancheExpeted = new ArrayList<String>();
            listeBrancheExpeted.add("A");
            listeBrancheExpeted.add("B");
            listeBrancheExpeted.add("AB");
            listeBrancheExpeted.add("BA");
            listeBrancheExpeted.add("ROOT");
            
            //THEN
            //feuille
            assertTrue(CollectionUtils.isEqualCollection(listeFeuillesExpeted, arbreFormatActual.getEndLeaves()));
            
            //branche
            assertTrue(CollectionUtils.isEqualCollection(listeBrancheExpeted, arbreFormatActual.getBranches()));
        }
    
    

}
