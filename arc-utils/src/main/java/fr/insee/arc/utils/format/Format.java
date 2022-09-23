package fr.insee.arc.utils.format;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class Format implements IConstanteCaractere {

    @SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(Format.class);
    
    public static final String removeJSONForbiddenChars(String string) {
        if (StringUtils.isBlank(string)) {
            return string;
        }
        return string.replaceAll("[\r\n\t]", " ");
    }

    public static <T> int size(ArrayList<T> o)
    {
		return o==null?0:o.size();
    }
    
    /**
     *
     * @param list
     * @return un hashcode pour la liste
     */
    public static final int hashCode(List<? extends Object> list) {
        int returned = 0;
        for (int i = 0; i < list.size(); i++) {
            returned = 31 * returned + list.get(i).hashCode();
        }
        return returned;

    }

    /**
     * @param array
     *            an ArrayList
     * @return a List
     */
    public static <T> List<List<T>> patch(ArrayList<ArrayList<T>> array) {
        List<List<T>> returned = new ArrayList<List<T>>();
        for (int i = 0; i < array.size(); i++) {
            returned.add(array.get(i));
        }
        return returned;
    }

    /**
     * Renvoie "{k}"
     *
     * @param k
     * @return
     */
    private static String numeroArgumentPourChaine(int k) {
        return new StringBuilder("{").append(k).append("}").toString();
    }

    /**
     * Remplace les occurences {k} de s par args[k]. Exemple : s = Bonjour, {0}, comment va {1}, args = {toi, ton chien} renvoie Bonjour,
     * toi, comment va ton chien
     *
     * @param s
     * @param args
     * @return
     */
    public static String parseStringAvecArguments(String s, String... args) {
        String parsedString = new String(s);
        for (int i = 0; i < args.length; i++) {
            parsedString = parsedString.replace(numeroArgumentPourChaine(i), args[i]);
        }
        return parsedString;
    }

    /**
     * Retourne une {@link String} résultant de la concaténation des éléments de {@code array} intercalés avec {@code separator}
     *
     * @param array
     * @param separator
     * @return
     */
    public static String untokenize(Object[] array, String separator) {
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
     *
     *
     * Concatène les éléments de toutes les listes de {@code listes}, comme le ferait la méthode
     * {@link Format#untokenize(Collection, String)}. L'ordre des éléments de la chaîne retournée correspond à celui d'un parcourt en
     * profondeur de l'arbre {@code listes}.
     *
     * @param listes
     * @param separator
     * @return
     */
    public static String untokenize(List<? extends List<String>> listes, String separator) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < listes.size(); i++) {
            String token = untokenize(listes.get(i), separator);
            if (StringUtils.isNotEmpty(token)) {
                tokens.add(token);
            }
        }
        return untokenize(tokens, separator);
    }

    public static String untokenize(Object[] array, String avantTout, String avantToken, String apresToken, String separateur, String apresTout) {
        return new StringBuilder(avantTout)//
                .append(avantToken)//
                .append(untokenize(array, new StringBuilder(apresToken + separateur + avantToken).toString()))//
                .append(array.length > 0 ? apresToken : empty)//
                .append(apresTout)//
                .toString();
    }

    public static String untokenize(Collection<?> array, String avantTout, String avantToken, String apresToken, String separateur, String apresTout) {
        return untokenize(array.toArray(), avantTout, avantToken, apresToken, separateur, apresTout);
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

        for (int i = liste.size() - 1; i >= fatherIndex; i--) {
            liste.remove(i);
        }
    }

    public static void removeToIndexInt(List<Integer> liste, int fatherIndex) {
        for (int i = liste.size() - 1; i >= fatherIndex; i--) {
            liste.remove(i);
        }
    }

    public static String asLetters(int num) {
        StringBuilder sb = new StringBuilder();
        int tempNum = num;
        while (tempNum > 0) {

            if (tempNum % 52 >= 26) {
                sb.append((char) ('a' + ((tempNum % 52) - 26)));
            } else {
                sb.append((char) ('A' + (tempNum % 52)));
            }

            tempNum /= 52;
        }
        return sb.toString();
    }

    public static String getHash(String donnees) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(donnees.getBytes());

        StringBuilder hashString = new StringBuilder();

        for (int i = 0; i < hash.length; i++) {
            hashString.append(asLetters(hash[i] + 129));
            // hashString.append(Integer.toString(hash[i]+129));
        }

        return hashString.toString();

    }

    public static int[][] getTreeArray(HashMap<Integer, Integer> tree) {
        int[][] arr = new int[tree.size()][3];

        int i = 0;

        for (Map.Entry<Integer, Integer> entry : tree.entrySet()) {
            if (tree.containsValue(entry.getKey())) {
                // noeud -> 1
                arr[i] = new int[] { (Integer) entry.getValue(), (Integer) entry.getKey(), 1 };
            } else {
                // feuille ->2
                arr[i] = new int[] { (Integer) entry.getValue(), (Integer) entry.getKey(), 2 };
            }

            i++;

        }

        return arr;

    }

    /**
     * renvoie un tableau ordonné selon la distance à la racine du pere pere -> fils -> noeud(1) ou feuille(2) -> distance à la racine du
     * pere
     *
     * @param tree
     * @param colDist
     * @return
     */
    public static int[][] getTreeArrayByDistance(HashMap<Integer, Integer> tree, HashMap<Integer, Integer> colDist) {
        int[][] arr = new int[tree.size()][4];

        int i = 0;

        for (Map.Entry<Integer, Integer> entry : tree.entrySet()) {
            if (tree.containsValue(entry.getKey())) {
                // noeud -> 1
                arr[i] = new int[] { entry.getValue(), entry.getKey(), 1, colDist.get(entry.getValue()) };
            } else {
                // feuille ->2
                arr[i] = new int[] { entry.getValue(), entry.getKey(), 2, colDist.get(entry.getValue()) };
            }

            i++;

        }
        Arrays.sort(arr, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return Integer.compare(o1[3], o2[3]);
            }
        });

        return arr;

    }

    public static String getLeafs(Integer arr2, int[][] arr, HashMap<String, Integer> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {
                result.append(",i_" + allCols.get(arr[j][1]));
                if (colData.get(allCols.get(arr[j][1])) != null) {
                    result.append(",v_" + allCols.get(arr[j][1]));
                }
            }
        }
        return result.toString();

    }
    public static String getLeafs2(Integer arr2, int[][] arr, HashMap<String, Boolean> colData, List<String> allCols) {
	StringBuilder result = new StringBuilder();
	
	for (int j = 0; j < arr.length; j++) {
	    if (arr[j][0] == arr2 && arr[j][2] == 2) {
		result.append(",i_" + allCols.get(arr[j][1]));
		if (colData.get(allCols.get(arr[j][1])) != false) {
		    result.append(",v_" + allCols.get(arr[j][1]));
		}
	    }
	}
	return result.toString();
	
    }

    public static String getLeafsSpace(Integer arr2, int[][] arr, HashMap<String, Integer> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {
                result.append(", i_" + allCols.get(arr[j][1]) + " as i_" + allCols.get(arr[j][1])+" ");
                if (colData.get(allCols.get(arr[j][1])) != null) {
                    result.append(", v_" + allCols.get(arr[j][1]) + " as v_" + allCols.get(arr[j][1])+" ");
                }
            }
        }
        return result.toString();

    }

    public static String getLeafsMax(Integer arr2, int[][] arr, HashMap<String, Integer> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {
                result.append(",min( i_" + allCols.get(arr[j][1]) + " ) as i_" + allCols.get(arr[j][1]));
                if (colData.get(allCols.get(arr[j][1])) != null) {
                    result.append(",min( v_" + allCols.get(arr[j][1]) + " ) as  v_" + allCols.get(arr[j][1]));
                }
            }
        }
        return result.toString();

    }




    public static String getLeafsSpace2(Integer arr2, int[][] arr, HashMap<String, Boolean> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {
                result.append(", i_" + allCols.get(arr[j][1]) + " as i_" + allCols.get(arr[j][1])+" ");
                if (colData.get(allCols.get(arr[j][1])) != false) {
                    result.append(", v_" + allCols.get(arr[j][1]) + " as v_" + allCols.get(arr[j][1])+" ");
                }
            }
        }
        return result.toString();

    }

    public static String getLeafsMax2(Integer arr2, int[][] arr, HashMap<String, Boolean> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {

                result.append(",min( i_" + allCols.get(arr[j][1]) + " ) as i_" + allCols.get(arr[j][1]));
                if (colData.get(allCols.get(arr[j][1])) != false) {
                    result.append(",min( v_" + allCols.get(arr[j][1]) + " ) as  v_" + allCols.get(arr[j][1]));

//                    result.append(",min( (data).i_" + allCols.get(arr[j][1]) + " ) as i_" + allCols.get(arr[j][1]));
//                    if (colData.get(allCols.get(arr[j][1])) != null) {
//                        result.append(",min( (data).v_" + allCols.get(arr[j][1]) + " ) as  v_" + allCols.get(arr[j][1]));
//
//                    }
                }
            }

        }
        return result.toString();

    }

    public static String getLeafsMaxWhenNull(Integer arr2, int[][] arr, HashMap<String, Integer> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {
                result.append(",first_value(i_" + allCols.get(arr[j][1]) + ") over (partition by i_" + allCols.get(arr2) + " order by i_"
                        + allCols.get(arr[j][1]) + " nulls last) as i_" + allCols.get(arr[j][1]));
                if (colData.get(allCols.get(arr[j][1])) != null) {
                    result.append(",first_value(v_" + allCols.get(arr[j][1]) + ") over (partition by i_" + allCols.get(arr2) + " order by v_"
                            + allCols.get(arr[j][1]) + " nulls last) as v_" + allCols.get(arr[j][1]));
                }

            }
        }
        return result.toString();

    }
    public static String getLeafsMaxWhenNull2(Integer arr2, int[][] arr, HashMap<String, Boolean> colData, List<String> allCols) {
        StringBuilder result = new StringBuilder();

        for (int j = 0; j < arr.length; j++) {
            if (arr[j][0] == arr2 && arr[j][2] == 2) {
                result.append(",first_value(i_" + allCols.get(arr[j][1]) + ") over (partition by i_" + allCols.get(arr2) + " order by i_"
                        + allCols.get(arr[j][1]) + " nulls last) as i_" + allCols.get(arr[j][1]));
                if (colData.get(allCols.get(arr[j][1])) != false) {
                    result.append(",first_value(v_" + allCols.get(arr[j][1]) + ") over (partition by i_" + allCols.get(arr2) + " order by v_"
                            + allCols.get(arr[j][1]) + " nulls last) as v_" + allCols.get(arr[j][1]));
                }

            }
        }
        return result.toString();

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

    public static String toLowerCase(String string) {
        return string == null ? null : string.toLowerCase();
    }

    public static String convertirFormatDate(String aDateAsString, String aSourceSimpleDateFormat, String aTargetSimpleDateFormat) throws ParseException {
        return new SimpleDateFormat(aTargetSimpleDateFormat).format(new SimpleDateFormat(aSourceSimpleDateFormat).parse(aDateAsString));
    }

    public static final Long toLong(String aLong) {
        return StringUtils.isBlank(aLong)? null:Long.parseLong(aLong);
    }

    @SafeVarargs
    public static <T> T coalesce(T... objects) {
        return Arrays.asList(objects).stream().filter((t) -> t != null).findFirst().orElse(null);
    }

}
