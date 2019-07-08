package fr.insee.arc.utils.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.log4j.Logger;

import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class ManipString implements IConstanteCaractere {

    private static final Logger LOGGER = Logger.getLogger(ManipString.class);
    
    
    
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
     * Extrait les morceaux définis par le pattern, sachant que le pattern contient un groupe.
     * @param aPattern
     * @param aChaine
     * @return
     */
    public static List<String> extractFromPatternWithOneGroup(String aPattern, String aChaine) {
        LoggerHelper.trace(LOGGER, "Pattern de détection des tables de nomenclatures dans l'expression du filtre:", aPattern);
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
    static public ArrayList<String> extractRubriques(final String cond) {
        ArrayList<String> listRubrique = new ArrayList<String>();
        boolean isTrue = true;
        if (StringUtils.isBlank(cond)){
            return listRubrique;
        }
        String replaced = cond;
        while (isTrue) {
            String rubrique = extractRubrique(replaced);
            if (rubrique.length() == 0) {
                // plus rien à trouver
                isTrue = false;
            } else {
                listRubrique.add(rubrique);
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
    public static Integer parseNumber(String s)
    {
    	Integer r=null;
    	try {
    		r=Integer.parseInt(s);
    	} catch (Exception e)
    	{
    	}
    	return r;
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

    public static final String[] tokenize(String string, String... tokens) {
        return string.split("[" + Format.untokenize(tokens, EMPTY) + "]");
    }

    /**
     *
     * @param string
     *            une chaîne de caractères comprenant éventuellement des sous-chaînes de {@code tokens}.
     * @param ordinal
     *            un entier
     * @param tokens
     *            un ensemble de chaînes de caractères
     * @return le token de position {@code ordinal} (première position : 0) obtenu après séparation de la chaîne selon les chaînes de
     *         {@code token}.
     */
    public static final String getNthToken(String string, int ordinal, String... tokens) {
        return tokenize(string, tokens)[ordinal];
    }

    static public String extractRubrique(String condition) {
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

        return condition.replaceAll("\\{([^}{]*)\\}", "$1");
    }

    static public String extractAllRubriqueJson(String condition) {
        Matcher m = Pattern.compile("\\{([^}{,]*)\\}").matcher(condition);

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

    public static boolean isStringNull(String a) {
        return (a == null || a.length() == 0);
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

    public static String sqlEqual(String val, String type) {
        if (val == null) {
            return "is null";
        } else {
            return "='" + val.replace("'", "''") + "'::" + type;
        }

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
        StringBuffer fileData = new StringBuffer();
        try( BufferedReader reader = new BufferedReader(new FileReader(filePath)))
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

    public static String padLeft(int in, String pattern, int precision) {
        return padLeft(("" + in), pattern, precision);
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
                } else if (Character.toString(ch).equals("�?")) {
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
                } else if (Character.toString(ch).equals("�?")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("Î")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("�?")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("�?")) {
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
                } else if (Character.toString(ch).equals("�?")) {
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

    public static String translateAsciiWithoutSpace(String s) {

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
                } else if (Character.toString(ch).equals("�?")) {
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
                } else if (Character.toString(ch).equals("�?")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("Î")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("�?")) {
                    sb.append("I");
                    space = false;
                } else if (Character.toString(ch).equals("�?")) {
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
                } else if (Character.toString(ch).equals("�?")) {
                    sb.append("Y");
                    space = false;
                }
                // else if(Character.toString(ch).equals("#")){sb.append("#"); space=false;}
                else if (Character.toString(ch).equals("Œ")) {
                    sb.append("#");
                    space = false;
                }
                // Les caractères non trouvés sont supprimés :

                ch = iterator.next();
            }

            return sb.toString().trim();
        } else {
            return null;
        }
    }

    public static void replaceStringBuilder(StringBuilder sb, String toReplace, String replacement) {
        int index = -1;
        while ((index = sb.lastIndexOf(toReplace)) != -1) {
            sb.replace(index, index + toReplace.length(), replacement);
        }
    }

    /**
     * Renommage d'un nom de fichier par adjounction d'un #1 ou incrémentation du numéro (si #1 =>#2) gestion de l'extension dans le nom
     * (par exemple .dsn)
     *
     * @param nomFic
     * @return
     */
    public static String renomme(String nomFic) {
        String res = "";

        int i_dieze = nomFic.lastIndexOf("_");
        int i_extension = nomFic.lastIndexOf(".");
        if (i_dieze < 0 && i_extension < 0) {// ni dièze ni extension, toto => toto#1
            res = nomFic + "_1";
        } else if (i_dieze < 0 && i_extension > 0) {// cas nominal pas encore de dièze et nom de fichier avec extention, toto.dsn =>
                                                    // toto#1.dsn
            res = nomFic.substring(0, i_extension) + "_1" + nomFic.substring(i_extension);
        } else if (i_dieze > 0 && i_extension > 0) {
            try {// on augmente le numéro de 1 si c'est bien un numéro, toto#1.dsn = > toto#2.dsn
                int i = Integer.parseInt(nomFic.substring(i_dieze + 1, i_extension));
                res = nomFic.substring(0, i_dieze + 1) + (i + 1) + nomFic.substring(i_extension);
            } catch (Exception e) {// entre le dièze et l'extension ce n'est pas un numéro, to#to.dsn => to#to#1.dsn
                res = nomFic.substring(0, i_extension) + "_1" + nomFic.substring(i_extension);
            }
        } else { // dernier cas j'ai un dièze mais pas d'extension
            try {// on augmente le numéro de 1 si c'est bien un numéro, toto#1 => toto#2
                int i = Integer.parseInt(nomFic.substring(i_dieze + 1));
                res = nomFic.substring(0, i_dieze + 1) + (i + 1);
            } catch (Exception e) {// entre le dièze et la fin du nom ce n'est pas un numéro, to#to => to#to#1
                res = nomFic + "_1";
            }
        }

        return res;
    }

    
    /**
     * Rajoute une accolade fermante si, et seulement si, il y en a besoin.
     *
     * @param radical
     * @return
     */
    public static String reformerRadical(String radical) {
	return radical + (radical.endsWith(CLOSING_BRACE) ? EMPTY : CLOSING_BRACE);
    }
    
}
