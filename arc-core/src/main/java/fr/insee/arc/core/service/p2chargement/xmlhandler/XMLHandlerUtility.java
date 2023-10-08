package fr.insee.arc.core.service.p2chargement.xmlhandler;

import java.util.Arrays;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.utils.textUtils.FastList;
import fr.insee.arc.utils.utils.FormatSQL;

public class XMLHandlerUtility {

	// this lists are build to do a map between long large column names and short internal column name used by the loader
	// only for optimization purpose
	// as we cannot use postgres copy command
	// Usin copy commeand could require double parse, put table in memory (not really possible for large file, ~ 40Go) so stream requires)...
	public static final FastList<ColumnEnum> tempTableAColumnsLongName = new FastList<>(Arrays.asList(
			ColumnEnum.ID_SOURCE
			, ColumnEnum.ID_SAX
			, ColumnEnum.DATE_INTEGRATION
			, ColumnEnum.ID_NORME
			, ColumnEnum.PERIODICITE
			, ColumnEnum.VALIDITE));
	
	public static final FastList<String> tempTableAColumnsShortName = new FastList<>(
			Arrays.asList("m0", "m1", "m2", "m3", "m4", "m5"));
	
	/**
	 * Permet de générer la requête SQL pour le normage
	 * Pas de serialization car demande métier de la MOA de voir et de pouvoir exécuter la requete générée
	 * @return 
	 */
	public static String buildQueryJointureXML(String integrationDate, Map<Integer, Integer> tree, Map<Integer, Integer> colDist, FastList<String> allCols, Map<String, Integer> colData) {

		// construction de la requete de jointure
		StringBuilder req = new StringBuilder();

		int[][] arr = TreeFunctions.getTreeArrayByDistance(tree, colDist);
		StringBuilder reqCreate = new StringBuilder(" \n");

		StringBuilder reqInsert = new StringBuilder();
		reqInsert.append(" INSERT INTO {table_destination} (id," + ColumnEnum.ID_SOURCE.getColumnName()
				+ ",date_integration,id_norme,validite,periodicite");

		StringBuilder reqSelect = new StringBuilder();
		reqSelect.append("\n SELECT row_number() over (), ww.* FROM (");
		reqSelect.append("\n SELECT '{nom_fichier}'," + integrationDate + ",'{id_norme}','{validite}','{periodicite}'");

		StringBuilder reqFrom = new StringBuilder();

		int d = 0;

		for (int i = 0; i < arr.length; i++) {

			// pour chaque noeud

			if (arr[i][2] == 1) {

				String leaf = TreeFunctions.getLeafs(arr[i][1], arr, colData, allCols);

				// créer les vues
				String leafMax = TreeFunctions.getLeafsMax(arr[i][1], arr, colData, allCols);
				reqCreate.append("CREATE TEMPORARY TABLE t_" + allCols.get(arr[i][1]) + " as (select i_"
						+ allCols.get(arr[i][1]) + " as m_" + allCols.get(arr[i][1]) + " ");
				if (arr[i][0] >= 0) {
					reqCreate.append(
							", i_" + allCols.get(arr[i][0]) + " as i_" + allCols.get(arr[i][0]) + " ");
				}
				reqCreate.append(TreeFunctions.getLeafsSpace(arr[i][1], arr, colData, allCols));
				reqCreate.append(" FROM (SELECT i_" + allCols.get(arr[i][1]) + " ");
				reqCreate.append(leafMax);
				reqCreate.append(" FROM {table_source} where i_" + allCols.get(arr[i][1])
						+ " is not null group by i_" + allCols.get(arr[i][1]) + ") a ");
				if (arr[i][0] >= 0) {
					reqCreate.append(" , (SELECT DISTINCT i_" + allCols.get(arr[i][1]) + " as pivot, i_"
							+ allCols.get(arr[i][0]) + " FROM {table_source} where i_"
							+ allCols.get(arr[i][1]) + " is not null) b ");
					reqCreate.append(" where a.i_" + allCols.get(arr[i][1]) + " = b.pivot ");
				}
				reqCreate.append("); \n");

				// la table vide faite a partir de la table du bloc; ca permet de faire
				// simplement des jointures externe avec vide dedans
				reqCreate.append("CREATE TEMPORARY TABLE t_" + allCols.get(arr[i][1])
						+ "_null as (select * from t_" + allCols.get(arr[i][1]) + " where false); \n");

				// générer la clause select
				reqInsert.append(",i_" + allCols.get(arr[i][1]) + leaf);
				reqSelect.append(",m_" + allCols.get(arr[i][1]) + leaf);

				// générer la clause from

				if (arr[i][0] == -1) {
					reqFrom.append("t_" + allCols.get(arr[i][1]));
				} else {

					if (d != arr[i][3] && d > 0) {
						reqFrom.append("\n ) ");
						reqFrom.insert(0, "\n (");
					}

					reqFrom.append(
							"\n left join t_" + allCols.get(arr[i][1]) + " on m_" + allCols.get(arr[i][0])
									+ "=t_" + allCols.get(arr[i][1]) + ".i_" + allCols.get(arr[i][0]));
				}

				d = arr[i][3];
			}
		}

		reqInsert.append("\n )");

		reqFrom.insert(0, "\n FROM ");
		reqFrom.append("\n WHERE true ) ww ");

		req.append(reqCreate);
		req.append(reqInsert);
		req.append(reqSelect);
		req.append(reqFrom);

		return FormatSQL.quoteText(req.toString());

	}
	
	
}
