package fr.insee.arc.core.service.global.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;

public class TableOperations {

	private TableOperations() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER_APISERVICE = LogManager.getLogger(TableOperations.class);

	/**
	 * Créer une table image vide d'une autre table Si le schema est spécifié, la
	 * table est créée dans le schema; sinon elle est crée en temporary
	 *
	 * @param tableIn
	 * @param tableToBeCreated
	 * @return
	 */
	public static GenericPreparedStatementBuilder creationTableResultat(String tableIn, String tableToBeCreated, Boolean... image) {
		
		String where= (image.length == 0 || Boolean.FALSE.equals(image[0]))?"false":"true";

		return FormatSQL.createTableAsSelectWhere(tableIn, tableToBeCreated, where);
	}

	/**
	 * Query to create a target table if the source table has records
	 * 
	 * @param connexion
	 * @param tableIn : table a copier
	 * @param tableIdSource : table cible
	 * @return
	 */
	public static String createTableInherit(String tableIn, String tableIdSource) {
		StaticLoggerDispatcher.info(LOGGER_APISERVICE, "** createTableOK ** : " + tableIdSource);
		return FormatSQL.executeIf(FormatSQL.hasRecord(tableIn), creationTableResultat(tableIn, tableIdSource, true).toString());
	}


	/**
	 * This query created a work table containing the copy of a data of an input table for a given file identifier
	 * @param tableIn
	 * @param tableOut
	 * @param idSource
	 * @param extraCols
	 * @return
	 * @throws ArcException
	 */
	public static String createTableTravailIdSource(String tableIn, String tableOut, String idSource,
			String extraCols) throws ArcException {
		
		return createTableTravail(HashFileNameConversion.tableOfIdSource(tableIn, idSource), tableOut, extraCols);

	}

	/**
	 * This query created a work table containing the copy of a data of an input table input provided output table
	 * @param tableIn : the input table
	 * @param tableOut : the output table
	 * @param extraCols : sql syntax for extracols to be created
	 * sql syntax : defaultvalue_col1 as my_col1, defaultvalue_col2 as my_col2,, ... 
	 * @return
	 * @throws ArcException
	 */
	public static String createTableTravail(String tableIn, String tableOut, String extraCols) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}
		requete.append(
				"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");

		requete.append("\n SELECT * ");

		if (extraCols != null) {
			requete.append(", " + extraCols);
		}

		requete.append("\n FROM " + tableIn + "; ");
		
		return requete.toString();
	}
	
	
	/**
	 * This query created a work table containing the copy of a data of an input table for a given file identifier 
	 * @param tableIn
	 * @param tableOut
	 * @param idSource
	 * @return
	 * @throws ArcException
	 */
	public static String createTableTravailIdSource(String tableIn, String tableOut, String idSource) throws ArcException {
		return createTableTravailIdSource(tableIn, tableOut, idSource, null);
	}
	
	
}
