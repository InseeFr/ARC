package fr.insee.arc.core.service.p2chargement.dao;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXParseException;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.XMLColumns;
import fr.insee.arc.core.service.p2chargement.xmlhandler.ParallelInsert;
import fr.insee.arc.core.service.p2chargement.xmlhandler.TreeFunctions;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.textUtils.FastList;
import fr.insee.arc.utils.utils.FormatSQL;

public class HandlerXMLDao {

	public HandlerXMLDao(Connection connexion, FileIdCard fileIdCard, String tempTableA, FastList<String> allCols,
			Map<String, Integer> colData, Map<Integer, Integer> tree, Map<Integer, Boolean> treeNode) {
		super();
		this.connexion = connexion;
		this.fileIdCard = fileIdCard;
		this.tempTableA = tempTableA;
		this.allCols = allCols;
		this.colData = colData;
		this.tree = tree;
		this.treeNode = treeNode;
	}

	private static final String ALTER = "ALTER";
	public static final String HEADER = "$h";


	private FileIdCard fileIdCard;

	private String tempTableA;

	private FastList<String> allCols = new FastList<>();
	private Map<String, Integer> colData = new HashMap<>();

	// relation pere/fils enre les balises de l'arbre
	private Map<Integer, Integer> tree = new HashMap<>();
	// la balse est elle un noeud dans l'arbre i.e avec plusieurs balises filles
	private Map<Integer, Boolean> treeNode = new HashMap<>();

	// private
	private int idLigne = 0;
	private Connection connexion;
	private ParallelInsert pi;
	private Map<String, StringBuilder> requetes = new HashMap<>();
	private int requetesLength = 0;

	Map<Integer, Boolean> doNotinsert;

	// conserve les identifiants/valeurs déjà insérées pour ne pas les réinsérer à
	// nouveau
	private Map<Integer, String> keepLast = new HashMap<>();

	/**
	 * Requete d'insertion des données parsées. On souhaite profiter au mieux de la
	 * commande insert multi value : INSERT into (cols) values (data1), (data2) ...
	 * ;
	 *
	 *
	 * @param aRequete
	 * @param tempTableI
	 * @param fileName
	 * @param lineCols
	 * @param lineIds
	 * @param lineValues
	 * @throws SAXParseException
	 */
	public void insertQueryBuilder(List<Integer> lineCols, List<Integer> lineIds, List<String> lineValues) {

		recordWhatAlreadyBeenInsertAndUpdateRecordsWithNewInsertion(lineCols, lineIds, lineValues);

		buildInsertQuery(lineCols, lineIds, lineValues);
	}

	/**
	 * Build the insert query
	 * 
	 * @param lineCols
	 * @param lineIds
	 * @param lineValues
	 */
	private void buildInsertQuery(List<Integer> lineCols, List<Integer> lineIds, List<String> lineValues) {
		StringBuilder req = new StringBuilder();
		StringBuilder req2 = new StringBuilder();
		this.idLigne++;

		req.append(SQL.INSERT_INTO).append(tempTableA).append("(") //
				.append(XMLColumns.getShort(ColumnEnum.ID_SOURCE)) //
				.append(",") //
				.append(XMLColumns.getShort(ColumnEnum.ID_SAX)) //
				.append(",") //
				.append(XMLColumns.getShort(ColumnEnum.DATE_INTEGRATION)) //
				.append(",") //
				.append(XMLColumns.getShort(ColumnEnum.ID_NORME)) //
				.append(",") //
				.append(XMLColumns.getShort(ColumnEnum.PERIODICITE)) //
				.append(",") //
				.append(XMLColumns.getShort(ColumnEnum.VALIDITE)); //

		req2.append("('").append(fileIdCard.getIdSource()).append("',").append(this.idLigne).append(",")
				.append(this.fileIdCard.getIntegrationDate()).append(",'").append(this.fileIdCard.getIdNorme())
				.append("','").append(this.fileIdCard.getPeriodicite()).append("','")
				.append(this.fileIdCard.getValidite()).append("'");

		for (int i = 0; i < lineCols.size(); i++) {

			// si le bloc est a reinséré (le pere n'est pas retrouvé dans doNotInsert
			// ou si la colonne est un noeud, on procède à l'insertion

			if (doNotinsert.get(this.tree.get(lineCols.get(i))) == null || this.treeNode.get(lineCols.get(i)) != null) {

				req.append(",i").append(lineCols.get(i));
				req2.append(",").append(lineIds.get(i));

				if (lineValues.get(i) != null) {
					req.append(",v").append(lineCols.get(i));
					req2.append(",'").append(lineValues.get(i)).append("'");
				}
			}
		}

		req.append(")values");
		req2.append(")");

		String reqString = req.toString();
		addQuery(reqString, req2);

	}

	private void recordWhatAlreadyBeenInsertAndUpdateRecordsWithNewInsertion(List<Integer> lineCols,
			List<Integer> lineIds, List<String> lineValues) {
		Map<Integer, String> keep = new HashMap<>();

		// enregistre la liste des colonnes/valeurs relatives à un noeud de l'arbre xml
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < lineCols.size(); i++) {
			s.setLength(0);
			s.append("{").append(lineIds.get(i)).append("}");
			if (lineValues.get(i) != null) {
				s.append("[").append(lineValues.get(i)).append("]");
			}

			if (keep.get(this.tree.get(lineCols.get(i))) == null) {
				keep.put(this.tree.get(lineCols.get(i)), s.toString());
			} else {
				s.append(keep.get(this.tree.get(lineCols.get(i))));
				keep.put(this.tree.get(lineCols.get(i)), s.toString());
			}

		}

		doNotinsert = new HashMap<>();
		for (Map.Entry<Integer, String> entry : this.keepLast.entrySet()) {

			if (entry.getValue().equals(keep.get(entry.getKey()))) {
				doNotinsert.put(entry.getKey(), true);
			}
		}

		this.keepLast = keep;
	}

	private void addQuery(String key, String value) {
		addQuery(key, new StringBuilder(value));
	}

	private void addQuery(String key, StringBuilder value) {
		if (requetes.get(key) != null) {
			requetes.get(key).append(key.equals(ALTER) ? "" : ",").append(value);
		} else {
			requetes.put(key, value);
		}
		requetesLength = requetesLength + value.length();
	}

	/**
	 * Wait for the parallel insert to be done and report a sax exception if an
	 * error occured
	 * 
	 * @throws SAXParseException
	 */
	private void waitForParallelInsertAndReport() throws SAXParseException {
		try {
			pi.waitAndReport();
		} catch (ArcException e) {
			throw new SAXParseException(e.getMessage(), "", "", idLigne, 0);
		}
	}

	private String computeFinalQuery() {

		StringBuilder result = new StringBuilder();
		result.append((requetes.get(ALTER) != null) ? requetes.get(ALTER) : "");

		for (String s : requetes.keySet()) {
			if (!s.equals(ALTER)) {
				result.append(s).append(requetes.get(s)).append(";");
			}
		}

		this.requetes = new HashMap<>();
		this.requetesLength = 0;

		return result.toString();

	}

	public void execQueryInsert() throws SAXParseException {

		if (this.requetesLength > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {

			String query = computeFinalQuery();

			waitForParallelInsertAndReport();

			pi = new ParallelInsert(this.connexion, query);
			pi.start();

		}

	}

	/**
	 * For optimization purpose, the columns of the load table had been shortened
	 * Build the query to retrieve the long columns name of the load table A
	 * 
	 * @param aRequete
	 */
	private void renameColumns(StringBuilder aRequete) {
		for (int i = 0; i < XMLColumns.tempTableAColumnsShortName.size(); i++) {
			aRequete.append(
					"\n ALTER TABLE " + this.tempTableA + " RENAME " + XMLColumns.tempTableAColumnsShortName.get(i)
							+ " TO " + XMLColumns.tempTableAColumnsLongName.get(i) + ";");
		}

		for (int i = 0; i < this.allCols.size(); i++) {
			aRequete.append(
					"\n ALTER TABLE " + this.tempTableA + " RENAME i" + i + " TO i_" + this.allCols.get(i) + ";");
			if (colData.get(this.allCols.get(i)) != null) {
				aRequete.append(
						"\n ALTER TABLE " + this.tempTableA + " RENAME v" + i + " TO v_" + this.allCols.get(i) + ";");
			}
		}

	}

	public void initializeInsert() {
		// intialisation de la connexion
		// creation de la table
		this.pi = new ParallelInsert(this.connexion, null);
	}

	public void addValueColumn(String currentTag) {

		addQuery(ALTER, "alter table " + this.tempTableA + " add v" + this.allCols.indexOf(currentTag) + " "
				+ TypeEnum.TEXT.getTypeName() + ";");
	}

	public void addIdColumn(String currentTag) {
		addQuery(ALTER, "alter table " + this.tempTableA + " add i" + this.allCols.indexOf(currentTag) + " "
				+ TypeEnum.INTEGER.getTypeName() + ";");
	}

	/**
	 * Permet de générer la requête SQL de normage TODO : serializer un objet en
	 * JSON
	 */
	public String requeteJointureXML(Map<Integer, Integer> colDist) {

		// construction de la requete de jointure
		StringBuilder req = new StringBuilder();

		int[][] arr = TreeFunctions.getTreeArrayByDistance(this.tree, colDist);
		StringBuilder reqCreate = new StringBuilder(" \n");

		StringBuilder reqInsert = new StringBuilder();
		reqInsert.append(" INSERT INTO {table_destination} (id," + ColumnEnum.ID_SOURCE.getColumnName()
				+ ",date_integration,id_norme,validite,periodicite");

		StringBuilder reqSelect = new StringBuilder();
		reqSelect.append("\n SELECT row_number() over (), ww.* FROM (");
		reqSelect.append("\n SELECT '{nom_fichier}'," + this.fileIdCard.getIntegrationDate()
				+ ",'{id_norme}','{validite}','{periodicite}'");

		StringBuilder reqFrom = new StringBuilder();

		int d = 0;

		for (int i = 0; i < arr.length; i++) {

			// pour chaque noeud

			if (arr[i][2] == 1) {

				String leaf = TreeFunctions.getLeafs(arr[i][1], arr, this.colData, this.allCols);

				// créer les vues
				String leafMax = TreeFunctions.getLeafsMax(arr[i][1], arr, this.colData, this.allCols);
				reqCreate.append("CREATE TEMPORARY TABLE t_" + this.allCols.get(arr[i][1]) + " as (select i_"
						+ this.allCols.get(arr[i][1]) + " as m_" + this.allCols.get(arr[i][1]) + " ");
				if (arr[i][0] >= 0) {
					reqCreate.append(
							", i_" + this.allCols.get(arr[i][0]) + " as i_" + this.allCols.get(arr[i][0]) + " ");
				}
				reqCreate.append(TreeFunctions.getLeafsSpace(arr[i][1], arr, this.colData, this.allCols));
				reqCreate.append(" FROM (SELECT i_" + this.allCols.get(arr[i][1]) + " ");
				reqCreate.append(leafMax);
				reqCreate.append(" FROM {table_source} where i_" + this.allCols.get(arr[i][1])
						+ " is not null group by i_" + this.allCols.get(arr[i][1]) + ") a ");
				if (arr[i][0] >= 0) {
					reqCreate.append(" , (SELECT DISTINCT i_" + this.allCols.get(arr[i][1]) + " as pivot, i_"
							+ this.allCols.get(arr[i][0]) + " FROM {table_source} where i_"
							+ this.allCols.get(arr[i][1]) + " is not null) b ");
					reqCreate.append(" where a.i_" + this.allCols.get(arr[i][1]) + " = b.pivot ");
				}
				reqCreate.append("); \n");

				// la table vide faite a partir de la table du bloc; ca permet de faire
				// simplement des jointures externe avec vide dedans
				reqCreate.append("CREATE TEMPORARY TABLE t_" + this.allCols.get(arr[i][1])
						+ "_null as (select * from t_" + this.allCols.get(arr[i][1]) + " where false); \n");

				// générer la clause select
				reqInsert.append(",i_" + this.allCols.get(arr[i][1]) + leaf);
				reqSelect.append(",m_" + this.allCols.get(arr[i][1]) + leaf);

				// générer la clause from

				if (arr[i][0] == -1) {
					reqFrom.append("t_" + this.allCols.get(arr[i][1]));
				} else {

					if (d != arr[i][3] && d > 0) {
						reqFrom.append("\n ) ");
						reqFrom.insert(0, "\n (");
					}

					reqFrom.append(
							"\n left join t_" + this.allCols.get(arr[i][1]) + " on m_" + this.allCols.get(arr[i][0])
									+ "=t_" + this.allCols.get(arr[i][1]) + ".i_" + this.allCols.get(arr[i][0]));
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

		return FormatSQL.quoteTextWithoutEnclosings(req.toString());

	}

	public void execQueryInsertFinal(boolean multileaf) throws SAXParseException {
	
		StringBuilder requete = new StringBuilder(computeFinalQuery());
		renameColumns(requete);
		
		if (multileaf) {
			multiLeafUpdate(requete);
		}
	
		waitForParallelInsertAndReport();
	
		pi = new ParallelInsert(this.connexion, requete.toString());
		pi.start();
		waitForParallelInsertAndReport();
	}


	/**
	 * synchronize header identifier if they are null
	 * @param aRequete
	 */
	private void multiLeafUpdate(StringBuilder aRequete) {
		// gestion des rubriques multiple
		for (int i = 0; i < this.allCols.size(); i++) {
			if (this.allCols.get(i).endsWith(HEADER)) {
				String headerCol = this.allCols.get(i);
				String col = this.allCols.get(i).replace(HEADER, "");

				aRequete.append("\n UPDATE " + this.tempTableA + " SET i_" + headerCol + "=i_" + col + " WHERE i_"
						+ headerCol + " IS NULL and i_" + col + " IS NOT NULL;");
			}
		}

	}

}
