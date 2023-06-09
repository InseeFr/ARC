package fr.insee.arc.utils.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class Format implements IConstanteCaractere {

	private Format() {
		throw new IllegalStateException("Utility class");
	}

    /**
     * @param array
     *            an ArrayList
     * @return a List
     */
    public static <T> List<List<T>> patch(ArrayList<ArrayList<T>> array) {
        List<List<T>> returned = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            returned.add(array.get(i));
        }
        return returned;
    }

    /**
     * Retourne une {@link String} résultant de la concaténation des éléments de {@code array} intercalés avec {@code separator}
     *
     * @param array
     * @param separator
     * @return
     */
    private static String untokenize(Object[] array, String separator) {
        StringBuilder sb = new StringBuilder(empty);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * Difference avec StringUtils.join(collection, separator) ?????
     *
     * @see #untokenize(String[], String)
     * @param tokens
     * @param separator
     * @return
     */
    public static String untokenize(Collection<?> tokens, String separator) {
        return tokens == null ? empty : untokenize(tokens.toArray(new Object[tokens.size()]), separator);
    }

 
    /**
     * Transforme le nom d'une balise en nom compatible avec une base de donnees</br>
     * Exemple : </br>
     * <code>n4ds:s21.g00.30.001</code> devient <code>s21_g00_30_001</code>
     * @param attribut
     * @return
     */
    public static String toBdRaw(String attribut) {
        return attribut.substring(attribut.lastIndexOf(':') + 1).replaceAll("[^A-Za-z0-9_]", "_");
    }

    public static String toBdId(String attribut) {
        return "i_" + toBdRaw(attribut);

    }

    public static String toBdVal(String attribut) {
        return "v_" + toBdRaw(attribut);
    }

    public static String toBdMain(String attribut) {
        return "m_" + toBdRaw(attribut);
    }
    
    public static String toBdRemovePrefix(String attribut)
    {
    	return attribut.substring(2);
    }
    
    
    public static void removeToIndex(List<String> liste, int fatherIndex) {
    	liste.subList(fatherIndex, liste.size()).clear();
    }

    public static void removeToIndexInt(List<Integer> liste, int fatherIndex) {
    	liste.subList(fatherIndex, liste.size()).clear();
    }

    public static String sqlListe(Collection<String> liste) {
        return new StringBuilder(openingParenthesis).append(quote).append(untokenize(liste, quote + comma + space + quote)).append(quote)
                .append(closingParenthesis).toString();
    }

    public static String stringListe(Collection<String> liste) {
        return new StringBuilder(openingParenthesis).append(untokenize(liste, comma + space)).append(closingParenthesis).toString();
    }

    public static String toUpperCase(String string) {
        return string == null ? null : string.toUpperCase();
    }

}
