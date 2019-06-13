package fr.insee.arc_essnet.utils.textUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Classe permettant de passer un array postgresql ( du type {A,B,C..}
 * @author S4LWO8
 *
 */
public class ParserArraySql {
    
    public static ArrayList<String> ArraySqlToList(String arraySql){ 
        String temp;
        /*
         * 1. On retire le 1er et dernier caractère
         */
        temp = arraySql.substring(1, arraySql.length()-1);
        
        return  new ArrayList<>(Arrays.asList(temp.split(",")));
        
    }

}
