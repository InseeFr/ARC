package fr.insee.arc.utils.textUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Parse a SQL array {a, b, c} into an {@link ArrayList}.
 *
 * @author S4LWO8
 *
 * FIXME never used
 */
public class SqlArrayParser {
    
    public static ArrayList<String> SqlArrayToList(String sqlArray){
        /*
         * Removing first and last character
         */
        String temp = sqlArray.substring(1, sqlArray.length()-1);
        
        return  new ArrayList<>(Arrays.asList(temp.split(",")));
        
    }

}
