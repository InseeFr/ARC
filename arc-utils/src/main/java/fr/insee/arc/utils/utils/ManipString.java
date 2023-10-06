package fr.insee.arc.utils.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class ManipString implements IConstanteCaractere {

    private static final Logger LOGGER = LogManager.getLogger(ManipString.class);

    public static final String patternForRubrique = "([^}{,]*)";

    public static final String patternForRubriqueWithBrackets = "\\{"+patternForRubrique+"\\}";

    public static final String patternForIdRubrique = "(i\\_[^}{,]*)";

    public static final String patternForIdRubriqueWithBrackets = "\\{"+patternForIdRubrique+"\\}";

    

	ManipString() {
	}
    
    /**
     * Build a temporary name for archive entry names
     * @param e
     * @return
     */
    public static String redoEntryName(String e)
    {
    	return e.replace("/", "§").replace("\\","§");
    }

    
    /**
     * Transforme une chaine de caractère avec séparateur en List&lt;String&gt;.
     * La méthode en profite pour enlever les blancs inutiles et mettre en minuscule.
     * @param listInString
     * @param separateur
     * @return une liste de string trimée en minuscule
     */
    public static List<String> stringToList (String listInString, String separateur){
        List<String> arrayWithSpace = new ArrayList<>(Arrays.asList(listInString.split(separateur)));
        return arrayWithSpace.stream()//
                             .map(t-> t.trim().toLowerCase())//
                             .collect(Collectors.toList());
    }
    
    
    /**
     * return null String if trim expression if empty
     * @param element
     * @return
     */
    public static String nullIfEmptyTrim(String element)
    {
    	return (element == null || element.trim().equals(""))?null:element;
    }
    
    
    
    /**
     * Convert a variadic elements into an arrayList
     * @param elements
     * @return
     */
    public static List<String> stringToList(String... elements)
    {
    	return new ArrayList<>(Arrays.asList(elements));
    }

    
    /**
     * Transforme une chaîne de caractères en list, utilisant le separateur indiqué
     * @param aChainedList
     * @param aSeparator
     * @return
     */
    public static List<String> splitAndCleanList(String aChainedList, String aSeparator){//
    	return Arrays.asList(aChainedList.split(aSeparator))//
    				 .stream()//
				     .map(t->t.trim())//
				     .collect(Collectors.toList());//
    }
    
    
    /**
     * Extrait les morceaux définis par le pattern, sachant que le pattern contient un groupe.
     * @param aPattern
     * @param aChaine
     * @return
     */
    public static List<String> extractFromPatternWithOneGroup(String aPattern, String aChaine) {
        List<String> returned = new ArrayList<>();
        if(aChaine == null) return returned;
        Pattern pattern = Pattern.compile(aPattern);
        Matcher matcher = pattern.matcher(aChaine);
        while(matcher.find()) {
            returned.add(matcher.group(1));
        }
        return returned;
    }
    
    public static String extractFirst(String regex, String value){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        String param = "";
        if(matcher.find()){
            param = value.substring(matcher.start()+1, matcher.end()-1);
        }
        return param;
    }
    
    
    /**
     * chaine de travail
     */

    /**
     * Pour détecter l'ensemble des rubrique d'un chaine de caractère, détection sur les { }, sans doublon
     *
     * @param cond
     *            , la chaine de caractère à lire
     * @return une liste de rubrique
     */
    public static List<String> extractRubriques(final String cond) {
    	
    	
    	List<String> listRubrique = new ArrayList<>();
        boolean isTrue = true;
        if (StringUtils.isBlank(cond)){
            return listRubrique;
        }
        String replaced = cond;
        while (isTrue) {
            String rubrique = extractBetweenBrackets(replaced);
            if (rubrique.length() == 0) {
                // plus rien à trouver
                isTrue = false;
            } else {
                if (rubrique.matches(patternForRubrique)) 
                {
                	listRubrique.add(rubrique);
                }
                replaced = replaced.replace("{" + rubrique + "}", "");
            }

        }
        return listRubrique;
    }


    /**
     * Null si la chaine n'est pas un entier
     * @param s
     * @return
     */
    public static Integer parseInteger(String s)
    {
    	try {
    		return Integer.parseInt(s);
    	} catch (Exception e)
    	{
    		return null;
    	}
    }


    /**
     * Remplace toutes les occurences {@code t} de {@code regex} dans {@code input} par {@code consumer.execute(t)}.
     *
     * @param regex
     * @param input
     * @param replacement
     * @return
     */
    public static final String regexpReplace(String regex, String input, Function<String, String> consumer) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder returned = new StringBuilder();
        int positionCourante = 0;
        while (matcher.find()) {
            returned.append(input.substring(positionCourante, matcher.start()));
            returned.append(consumer.apply(input.substring(matcher.start(), matcher.end())));
            positionCourante = matcher.end();
        }
        returned.append(input.substring(positionCourante));
        LoggerHelper.debugAsComment(LOGGER, "Transformation de", input, "en",returned.toString());
        return returned.toString();
    }


    public static String extractBetweenBrackets(String condition) {
        String rubrique = "";
        int i = condition.indexOf("{", 0);
        int j = condition.indexOf("}", i + 1);
        if (i >= 0) {
            rubrique = condition.substring(i + 1, j);
        }
        return rubrique;
    }

    /**
     *
     * @param input
     * @return
     */
    public static String replaceNull(String input) {
        return input == null ? "" : input;
    }

    static public String extractAllRubrique(String condition) {

        return condition.replaceAll(patternForRubriqueWithBrackets, "$1");
    }

    static public String extractAllRubriqueJson(String condition) {
        Matcher m = Pattern.compile(patternForRubriqueWithBrackets).matcher(condition);

        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(condition.substring(last, m.start()));
            sb.append("(data->>'" + m.group(1).toLowerCase() + "')");
            last = m.end();
        }
        sb.append(condition.substring(last));
        return sb.toString();

    }

    /** Returns true if a String object is either null or strictly empty.*/
    public static boolean isStringNull(String a) {
        return (a == null || a.isEmpty());
    }

    public static boolean compareStringWithNull(String a, String b) {
        boolean equals = false;
        if (isStringNull(a) && isStringNull(b)) {
            equals = true;
        } else if (!isStringNull(a) && !isStringNull(b) && a.equals(b)) {
            equals = true;
        }
        return equals;
    }

    public static boolean contains(Enumeration<?> e, String test) {

        boolean r = false;
        while (e.hasMoreElements() && !r) {
            String key = (String) e.nextElement();
            r = key.equals(test);
        }
        return r;
    }


    /**
     * Gets the substring before the first occurrence of a separator
     *
     * @param str
     * @param separator
     * @return
     */
    public static String substringBeforeFirst(String str, String separator) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(separator)) {
            return str;
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Gets the substring before the last occurrence of a separator
     *
     * @param str
     * @param separator
     * @return
     */
    public static String substringBeforeLast(String str, String separator) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(separator)) {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Renvoie la sous chaine de caractère après le premier séparateur riri_fifi_loulou donne fifi_loulou
     *
     * @param str
     * @param separator
     * @return
     */
    public static String substringAfterFirst(String str, String separator) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(separator)) {
            return str;
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * Gets the substring before the last occurrence of a separator
     *
     * @param str
     * @param separator
     * @return
     */
    public static String substringAfterLast(String str, String separator) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(separator)) {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(pos + separator.length());
    }

    public static String readFileAsString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder();
        try(BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(),Charset.forName("UTF-8"));)
        {
	        char[] buf = new char[1024];
	        int numRead = 0;
	        while ((numRead = reader.read(buf)) != -1) {
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	        }
        }
        return fileData.toString();
    }

    /**
     * padding à gauche
     *
     * @param in
     *            : chaine en entrée
     * @param pattern
     *            : le pattern à pad
     * @param precision
     *            : on pad sur combien de caractere ?
     * @return
     */
    public static String padLeft(String in, String pattern, int precision) {

        String pad = "";
        for (int i = 0; i < precision; i++) {
            pad = pad + pattern;
        }
        return pad.substring(0, pad.length() - in.length()) + in;
    }

    public static String translateAscii(String s) {

        if (s != null) {

            String sUpper = s.toUpperCase();
            final StringBuilder sb = new StringBuilder(sUpper.length() * 2);

            final StringCharacterIterator iterator = new StringCharacterIterator(sUpper);

            char ch = iterator.current();
            Boolean space = false;

            while (ch != CharacterIterator.DONE) {
                // A-Z : on garde
                if ((int) ch > 64 && (int) ch < 91) {
                    sb.append(ch);
                    space = false;
                }
                // 0-9 : on garde
                else if ((int) ch > 47 && (int) ch < 58) {
                    sb.append(ch);
                    space = false;
                }
                // caractères spéciaux : on transforme
                else if (Character.toString(ch).equals("À")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Á")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Â")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Ã")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Ä")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Å")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Æ")) {
                    sb.append("A");
                    space = false;
                } else if (Character.toString(ch).equals("Ç")) {
                    sb.append("C");
                    space = false;
                } else if (Character.toString(ch).equals("È")) {
                    sb.append("E");
                    space = false;
                } else if (Character.toString(ch).equals("É")) {
                    sb.append("E");
                    space = false;
                } else if (Character.toString(ch).equals("Ê")) {
                    sb.append("E");
                    space = false;
                } else if (Character.toString(ch).equals("Ë")) {
                    sb.append("E");
                    space = false;
                } else if (Character.toString(ch).equals("Ì")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("Í")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("Î")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("Ï")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("Ð")) {
                    sb.append("D");
                    space = false;
                } else if (Character.toString(ch).equals("Ñ")) {
                    sb.append("N");
                    space = false;
                } else if (Character.toString(ch).equals("Ò")) {
                    sb.append("O");
                    space = false;
                } else if (Character.toString(ch).equals("Ó")) {
                    sb.append("O");
                    space = false;
                } else if (Character.toString(ch).equals("Ô")) {
                    sb.append("O");
                    space = false;
                } else if (Character.toString(ch).equals("Õ")) {
                    sb.append("O");
                    space = false;
                } else if (Character.toString(ch).equals("Ö")) {
                    sb.append("O");
                    space = false;
                } else if (Character.toString(ch).equals("Ø")) {
                    sb.append("0");
                    space = false;
                } else if (Character.toString(ch).equals("Ù")) {
                    sb.append("U");
                    space = false;
                } else if (Character.toString(ch).equals("Ú")) {
                    sb.append("U");
                    space = false;
                } else if (Character.toString(ch).equals("Û")) {
                    sb.append("U");
                    space = false;
                } else if (Character.toString(ch).equals("Ü")) {
                    sb.append("U");
                    space = false;
                } else if (Character.toString(ch).equals("Ý")) {
                    sb.append("Y");
                    space = false;
                }
                // else if(Character.toString(ch).equals("#")){sb.append("#"); space=false;}
                else if (Character.toString(ch).equals("Œ")) {
                    sb.append("#");
                    space = false;
                }
                // Les caractères non trouvés sont remplacés par un espace ou supprimés :
                // on ne doit pas avoir deux espaces qui se suivent dans la chaine finale
                else if (!space) {
                    space = true;
                    sb.append(" ");
                }

                ch = iterator.next();
            }

            return sb.toString().trim();
        } else {
            return null;
        }
    }

}

