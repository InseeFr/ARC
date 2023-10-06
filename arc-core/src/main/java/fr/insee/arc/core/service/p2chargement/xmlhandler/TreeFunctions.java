package fr.insee.arc.core.service.p2chargement.xmlhandler;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import fr.insee.arc.utils.textUtils.FastList;

public class TreeFunctions {


    private TreeFunctions() {
    	throw new IllegalStateException("Tree Utility class for XML Handlers");
	}

	/**
     * renvoie un tableau ordonné selon la distance à la racine du pere
     * pere -> fils -> noeud(1) ou feuille(2) -> distance à la racine du
     * pere
     *
     * @param tree
     * @param colDist
     * @return
     */
    public static int[][] getTreeArrayByDistance(Map<Integer, Integer> tree, Map<Integer, Integer> colDist) {
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

    public static String getLeafs(Integer arr2, int[][] arr, Map<String, Integer> colData, FastList<String> allCols) {
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

    public static String getLeafsSpace(Integer arr2, int[][] arr, Map<String, Integer> colData, FastList<String> allCols) {
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

    public static String getLeafsMax(Integer arr2, int[][] arr, Map<String, Integer> colData, FastList<String> allCols) {
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
	
}
