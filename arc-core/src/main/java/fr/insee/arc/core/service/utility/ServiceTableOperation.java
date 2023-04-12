package fr.insee.arc.core.service.utility;

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
		requete.append("\n CREATE ");
		if (!tableToBeCreated.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("TABLE " + tableToBeCreated + " ");
		requete.append("" + FormatSQL.WITH_NO_VACUUM + " ");
		requete.append("as SELECT * FROM " + tableIn + " ");
		if (image.length == 0 || image[0] == false) {
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
	 * Creation de la table de travail contenant les données en entrée d'une phase
	 * et pour un fichier donné La table en sortie est temporaire ou unlogged car
	 * elle est volatile et utilisée que durant l'execution de la phase La table en
	 * entrée est dans le ca d'utilisation principale la table résultat des données
	 * en sortie la phase précédente pour le fichier donnée.
	 * 
	 * @param extraColumns
	 * @param tableIn        la table des données en entrée de la phase
	 * @param tableOut       la table des données du fichier en sortie
	 * @param tablePilTemp   la table de pilotage relative à la phase; c'est la
	 *                       liste des fichiers selectionnés pour la phase
	 * @param idSource       le nom du fichier
	 * @param isIdSource     le nom du fichier est-il spécifié ?
	 * @param etatTraitement l'état du traitement si on souhaite crée une table en
	 *                       sortie relative à un état particulier
	 * @return
	 */
	public static String createTableTravail(String extraColumns, String tableIn, String tableOut, String tablePilTemp,
			String... etatTraitement) {
		StringBuilder requete = new StringBuilder();

		requete.append("\n DROP TABLE IF EXISTS " + tableOut + " CASCADE; \n");

		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}

		requete.append(
				"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
		requete.append("( ");
		requete.append("\n    SELECT * " + extraColumns);
		requete.append("\n    FROM " + tableIn + " stk ");
		requete.append("\n    WHERE exists ( SELECT 1  ");
		requete.append("\n            FROM " + tablePilTemp + " pil  ");
		requete.append("\n  where pil." + ColumnEnum.ID_SOURCE.getColumnName() + "=stk."
				+ ColumnEnum.ID_SOURCE.getColumnName() + " ");
		if (etatTraitement.length > 0) {
			requete.append(" AND '" + etatTraitement[0] + "'=ANY(pil.etat_traitement) ");
		}
		requete.append(" ) ");
		requete.append(");\n");

		return requete.toString();
	}

	public static String createTableTravailIdSource(String tableIn, String tableOut, String idSource,
			String... extraCols) throws ArcException {
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

		if (extraCols.length > 0) {
			requete.append(", " + extraCols[0]);
		}

		requete.append("\n FROM " + ServiceHashFileName.tableOfIdSource(tableIn, idSource) + "; ");

		return requete.toString();
	}

}
