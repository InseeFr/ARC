package fr.insee.arc.core.service.api.query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;

public class ServiceTableOperation {

	private ServiceTableOperation() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER_APISERVICE = LogManager.getLogger(ServiceTableOperation.class);

	/**
	 * Créer une table image vide d'une autre table Si le schema est spécifié, la
	 * table est créée dans le schema; sinon elle est crée en temporary
	 *
	 * @param tableIn
	 * @param tableToBeCreated
	 * @return
	 */
	public static String creationTableResultat(String tableIn, String tableToBeCreated, Boolean... image) {
		StringBuilder requete = new StringBuilder();
		
		requete.append(FormatSQL.dropTable(tableToBeCreated));
		
		requete.append("\n CREATE ");
		if (!tableToBeCreated.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("TABLE " + tableToBeCreated + " ");
		requete.append("" + FormatSQL.WITH_NO_VACUUM + " ");
		requete.append("as SELECT * FROM " + tableIn + " ");
		if (image.length == 0 || Boolean.FALSE.equals(image[0])) {
			requete.append("where 1=0 ");
		}
		requete.append("; ");
		return requete.toString();
	}

	/**
	 * Query to create a target table if the source table has records
	 * 
	 * @param connexion
	 * @param tableIn
	 * @param tableIdSource
	 * @return
	 */
	public static String createTableInherit(String tableIn, String tableIdSource) {
		StaticLoggerDispatcher.info("** createTableOK ** : " + tableIdSource, LOGGER_APISERVICE);

		// si la table in n'est pas vide
		StringBuilder queryToTest = new StringBuilder();
		queryToTest.append("SELECT count(*)>0 FROM (SELECT 1 FROM " + tableIn + " LIMIT 1) u");

		StringBuilder queryToExecute = new StringBuilder();

		// on créé la table héritée que si la table a des enregistrements
		queryToExecute.append("DROP TABLE IF EXISTS " + tableIdSource + ";");
		queryToExecute.append("CREATE TABLE " + tableIdSource + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM "
				+ tableIn + ";");

		return FormatSQL.executeIf(queryToTest, queryToExecute);
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

		requete.append("\n FROM " + ServiceHashFileName.tableOfIdSource(tableIn, idSource) + "; ");
		
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
