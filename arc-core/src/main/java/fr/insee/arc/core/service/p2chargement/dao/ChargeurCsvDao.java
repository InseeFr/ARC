package fr.insee.arc.core.service.p2chargement.dao;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.thread.ThreadTemporaryTable;
import fr.insee.arc.core.service.p2chargement.bo.CSVFileAttributes;
import fr.insee.arc.core.service.p2chargement.bo.CSVFormatRules;
import fr.insee.arc.core.service.p2chargement.operation.ParseFormatRulesOperation;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ChargeurCsvDao {

	private Sandbox sandbox;
	private FileIdCard fileIdCard;
	private CSVFileAttributes fileAttributes;
	private ParseFormatRulesOperation<CSVFormatRules> parser;
	private String tmpChargementBrut;
	private String tmpChargementArc;

	public ChargeurCsvDao(Sandbox sandbox, CSVFileAttributes fileAttributes, FileIdCard fileIdCard,
			ParseFormatRulesOperation<CSVFormatRules> parser) {
		this.sandbox = sandbox;
		this.fileAttributes = fileAttributes;
		this.fileIdCard = fileIdCard;
		this.parser = parser;
		this.tmpChargementArc = ThreadTemporaryTable.TABLE_TEMP_CHARGEMENT_A;
		this.tmpChargementBrut = ThreadTemporaryTable.TABLE_TEMP_CHARGEMENT_B;
	}

	private static final int SINGLE_FULL_PARTITION = 1;

	/**
	 * evaluate by posgres a character expression
	 * 
	 * @param expression
	 * @return
	 * @throws ArcException
	 */
	public String execQueryEvaluateCharExpression(String expression) throws ArcException {
		// si le quote est une expression complexe, l'interpreter par postgres
		if (expression != null && expression.length() > 1 && expression.length() < 8) {
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.append("SELECT " + expression + " ");
			return UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query).get(2).get(0);
		} else {
			return expression;
		}
	}

	/**
	 * Create the table where the csv file data will be stored
	 * 
	 * @throws ArcException
	 */
	public void initializeCsvTableContainer() throws ArcException {
		StringBuilder req = new StringBuilder();
		req.append("DROP TABLE IF EXISTS " + this.tmpChargementBrut + ";");

		req.append("CREATE TEMPORARY TABLE " + this.tmpChargementBrut + " (");
		for (String nomCol : fileAttributes.getHeadersV()) {
			req.append(nomCol).append(" text,");
		}
		req.append("id SERIAL");
		req.append(");");

		UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), req);
	}

	public void execQueryCopyCsv(InputStream streamContent) throws ArcException {

		// tuple des headers
		String columns = "(" + StringUtils.join(fileAttributes.getHeadersV(), SQL.COMMA.getSqlCode()) + ")";

		boolean ignoreFirstLine = (parser.getValue(CSVFormatRules.HEADERS) == null);

		String separateur = fileIdCard.getIdCardChargement().getDelimiter();

		String quote = parser.getValue(CSVFormatRules.QUOTE);

		String encoding = parser.getValue(CSVFormatRules.ENCODING);
		encoding = (encoding==null) ? StandardCharsets.UTF_8.name() : encoding;
		
		String fileEncoding = parser.getValue(CSVFormatRules.FILE_ENCODING);
		
		InputStream inputStreamToCopyInDatabase;
		
		// convert stream from "file_encoding" to "encoding" if the "file_encoding" parameter is set 
		if (fileEncoding!=null)
		{
			try {
				inputStreamToCopyInDatabase = ReaderInputStream.builder().setReader(new InputStreamReader(streamContent, fileEncoding)).setCharset(encoding).get();
			} catch (Exception e) {
				throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, fileIdCard.getIdSource());
			} 
		}
		else
		{
			inputStreamToCopyInDatabase = streamContent;
		}

		UtilitaireDao.get(0).importing(this.sandbox.getConnection(), this.tmpChargementBrut,
				columns, inputStreamToCopyInDatabase, ignoreFirstLine, separateur, quote, encoding);

	}

	/**
	 * Create the final table of loaded file with metadata and all required columns
	 * (i_col, v_col)
	 * 
	 * @throws ArcException
	 */
	public void execQueryCreateContainerWithArcMetadata() throws ArcException {
		ArcPreparedStatementBuilder req = new ArcPreparedStatementBuilder();
		req.append("DROP TABLE IF EXISTS " + this.tmpChargementArc + ";");
		req.append("CREATE TEMPORARY TABLE " + this.tmpChargementArc);
		req.append(" AS (SELECT ");
		req.appendText(fileIdCard.getIdSource()).append("::text collate \"C\" as "+ ColumnEnum.ID_SOURCE.getColumnName());
		req.append(",").append("id::integer");
		req.append(",").append(fileIdCard.getIntegrationDate()).append("::text collate \"C\" as date_integration ");
		req.append(",").appendText(fileIdCard.getIdNorme()).append("::text collate \"C\" as id_norme ");
		req.append(",").appendText(fileIdCard.getPeriodicite()).append("::text collate \"C\" as periodicite ");
		req.append(",").appendText(fileIdCard.getValidite()).append("::text collate \"C\" as validite ");
		req.append(",0::integer as nombre_colonne");
		req.append(",");

		for (int i = 0; i < fileAttributes.getHeadersI().length; i++) {
			req.append("id as " + fileAttributes.getHeadersI()[i] + ", " + fileAttributes.getHeadersV()[i] + ",");
		}

		req.setLength(req.length() - 1);

		req.append(SQL.FROM).append(this.tmpChargementBrut).append(");");
		
		req.append("DROP TABLE IF EXISTS " + this.tmpChargementBrut + ";");

		UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), req);
	}

	public void execQueryApplyIndexRules() throws ArcException {
		if (parser.getValues(CSVFormatRules.INDEX).isEmpty()) {
			return;
		}

		StringBuilder query = new StringBuilder();
		for (int i = 0; i < parser.getValues(CSVFormatRules.INDEX).size(); i++) {
			query.append("CREATE INDEX idx" + i + "_chargeurcsvidxrule ON " + this.tmpChargementArc
					+ "(" + parser.getValues(CSVFormatRules.INDEX).get(i) + ");\n");
		}
		UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query);
	}

	/**
	 * Join the load table with the join tables declared in rules
	 * 
	 * @throws ArcException
	 */
	public void execQueryApplyJoinRules() throws ArcException {
		if (parser.getValues(CSVFormatRules.JOIN_TABLE).isEmpty()) {
			return;
		}

		StringBuilder query = new StringBuilder();

		query.append("\n DROP TABLE IF EXISTS TTT; ");
		query.append("\n CREATE TEMPORARY TABLE TTT AS ");

		// On renumérote les lignes après jointure pour etre cohérent
		query.append("\n SELECT  (row_number() over ())::int as id$new$, l.* ");
		for (int i = 0; i < parser.getValues(CSVFormatRules.JOIN_TABLE).size(); i++) {
			query.append("\n , v" + i + ".* ");
		}
		query.append("FROM  " + this.tmpChargementArc + " l ");

		// apply joint table
		queryForJoinTables(query);

		query.append("\n ;");
		query.append("\n ALTER TABLE TTT DROP COLUMN id; ");
		query.append("\n ALTER TABLE TTT RENAME COLUMN id$new$ TO id; ");
		query.append("\n DROP TABLE " + this.tmpChargementArc + ";");
		query.append("\n ALTER TABLE TTT RENAME TO " + this.tmpChargementArc + ";");
		UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query);
	}

	/**
	 * Applique les expressions de colonnes données dans les regles pour créer de
	 * nouvelle colonne ou modifier des anciennes Recalcule une colonnes si elle
	 * existe déjà (elle est écrasée) sinon la nouvelle colonne est créée Ce calcul
	 * peut etre réalisé en plusieurs blocs si une clé de partitionnement est
	 * définie dans les règles
	 * 
	 */
	public void execQueryApplyColumnsExpressionRules() throws ArcException {
		if (parser.getValues(CSVFormatRules.COLUMN_DEFINITION).isEmpty()) {
			return;
		}

		execQueryCreateContainerWithNewColumnsExpressionRules();

		// Itération

		// default value of the maximum records per partition
		int numberOfPartition = execQueryComputeNumberOfPartition();

		execQueryCreatePartitionIndex(numberOfPartition);

		execQueryInsertDataWithNewColumnsExpressionRules(numberOfPartition);

	}

	/**
	 * Compute the join query with the join tables declared in rules
	 * 
	 * @param query
	 * @throws ArcException
	 */
	private void queryForJoinTables(StringBuilder query) throws ArcException {
		// pour chaque table de jointure précisées dans les reqles
		for (int i = 0; i < parser.getValues(CSVFormatRules.JOIN_TABLE).size(); i++) {

			applySchemaToJoinTableIfNeeded(i);

			// récupération des colonnes de la table de jointure
			// et consitution de la requete
			List<String> colsIn = execQuerySelectColumnsFromJoinTable(i);

			// join type
			query.append("\n " + parser.getValues(CSVFormatRules.JOIN_TYPE).get(i) + " ");

			query.append("\n (SELECT ");
			// build column name to be suitable to load process aka : i_col, v_col
			boolean start = true;
			for (int j = 0; j < colsIn.size(); j++) {
				if (start) {
					start = false;
				} else {
					query.append("\n ,");
				}

				query.append("null::int as i_" + colsIn.get(j) + ", " + colsIn.get(j) + " as v_" + colsIn.get(j) + " ");

			}
			query.append("\n FROM " + parser.getValues(CSVFormatRules.JOIN_TABLE).get(i) + " ");
			query.append("\n ) v" + i + " ");
			query.append("\n ON " + parser.getValues(CSVFormatRules.JOIN_CLAUSE).get(i) + " ");

		}
	}

	/**
	 * if the schema is defined in the joint table name, keep it, if not , add the
	 * execution schema
	 */
	private void applySchemaToJoinTableIfNeeded(int jointTableIndex) {
		parser.getValues(CSVFormatRules.JOIN_TABLE).set(jointTableIndex, ViewEnum.getFullName(this.sandbox.getSchema(),
				parser.getValues(CSVFormatRules.JOIN_TABLE).get(jointTableIndex)));
	}

	private List<String> execQuerySelectColumnsFromJoinTable(int jointTableIndex) throws ArcException {
		return UtilitaireDao.get(0)
				.executeRequest(sandbox.getConnection(),
						new ArcPreparedStatementBuilder(
								"select " + parser.getValues(CSVFormatRules.JOIN_SELECT).get(jointTableIndex) + " from " + parser.getValues(CSVFormatRules.JOIN_TABLE).get(jointTableIndex) + " limit 0"))
				.get(0);
	}

	/**
	 * crée une tbale vide avec les colonnes d'expression
	 * 
	 * @throws ArcException
	 */
	private void execQueryCreateContainerWithNewColumnsExpressionRules() throws ArcException {

		if (parser.getValues(CSVFormatRules.COLUMN_DEFINITION).isEmpty()) {
			return;
		}

		StringBuilder query = new StringBuilder();

		// Creation de la table vide avant insert
		query.append("\n DROP TABLE IF EXISTS TTT; ");
		query.append("\n CREATE TEMPORARY TABLE TTT AS ");
		query.append("\n SELECT w.* FROM ");
		query.append("\n (SELECT v.* ");

		queryColumnsExpression(query, true, 0);

		query.append("\n FROM ");
		query.append("\n (SELECT u.* ");

		queryColumnsExpression(query, false, 0);

		query.append("\n FROM " + this.tmpChargementArc + " u ) v ) w ");
		query.append("\n WHERE false ");
		for (String s : parser.getValues(CSVFormatRules.FILTER_WHERE)) {
			query.append("\n AND (" + s + ")");
		}
		query.append(";");
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query);

	}

	private void queryColumnsExpression(StringBuilder req, boolean useRenameSuffix, int partitionNumber) {
		for (int i = 0; i < parser.getValues(CSVFormatRules.COLUMN_DEFINITION).size(); i++) {
			// si on trouve dans l'expression le suffix alors on sait qu'on a voulu
			// préalablement calculer la valeur

			boolean hasRenameSuffix = parser.getValues(CSVFormatRules.COLUMN_EXPRESSION).get(i)
					.contains(Delimiters.RENAME_SUFFIX);

			if ((useRenameSuffix ? hasRenameSuffix : !hasRenameSuffix)) {
				req.append("\n ,");
				req.append(parser.getValues(CSVFormatRules.COLUMN_EXPRESSION).get(i)
						.replace(Delimiters.PARTITION_NUMBER_PLACEHOLDER, partitionNumber + "000000000000::bigint"));
				req.append(" as ");
				req.append(parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(i) + Delimiters.RENAME_SUFFIX + " ");
			}
		}
	}

	/*
	 * compute a number of partition for the load table
	 */
	private int execQueryComputeNumberOfPartition() {

		if (parser.getValues(CSVFormatRules.PARTITION_EXPRESSION).isEmpty()) {
			// 1 single full partition if no partition defined in rules
			return SINGLE_FULL_PARTITION;
		}

		int partitionSize = DataObjectService.MAX_NUMBER_OF_RECORD_PER_PARTITION;

		// comptage rapide sur échantillon à 1/10000 pour trouver le nombre de partiton
		return UtilitaireDao.get(0).getInt(sandbox.getConnection(),
				new ArcPreparedStatementBuilder("select ((count(*)*10000)/" + partitionSize + ")+1 from "
						+ this.tmpChargementArc + " tablesample system(0.01)"));
	}

	/**
	 * create a hash balanced index on partition key
	 * 
	 * @param nbPartition
	 * @throws ArcException
	 */
	private void execQueryCreatePartitionIndex(int nbPartition) throws ArcException {

		// if only one partition, no need to index, return
		if (nbPartition == SINGLE_FULL_PARTITION) {
			return;
		}

		StringBuilder query = new StringBuilder();
		query.append("\n CREATE INDEX idx_partition_by_arc on " + this.tmpChargementArc
				+ " ((abs(hashtext(" + parser.getValues(CSVFormatRules.PARTITION_EXPRESSION).get(0) + "::text)) % "
				+ nbPartition + "));");
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query);
	}

	/**
	 * insert into the container table the data with new column expressions /
	 * optionnally by partition chunnk
	 * 
	 * @param numberOfPartition
	 * @throws ArcException
	 */
	private void execQueryInsertDataWithNewColumnsExpressionRules(int numberOfPartition) throws ArcException {

		// iterate over partition
		for (int part = 0; part < numberOfPartition; part++) {

			execQueryInsertDataWithNewColumnsExpressionRulesInPartition(numberOfPartition, part);
		}

		execQueryRebuildColumns();
	}

	/**
	 * insert data in partition
	 * 
	 * @param numberOfPartition
	 * @param part
	 * @throws ArcException
	 */
	private void execQueryInsertDataWithNewColumnsExpressionRulesInPartition(int numberOfPartition, int part)
			throws ArcException {
		StringBuilder req = new StringBuilder();
		req.append("\n INSERT INTO TTT ");
		req.append("\n SELECT w.* FROM ");
		req.append("\n (SELECT v.* ");

		queryColumnsExpression(req, true, part);

		req.append("\n FROM ");
		req.append("\n (SELECT u.* ");

		queryColumnsExpression(req, false, part);

		req.append("\n FROM " + this.tmpChargementArc + " u ");

		// add partition key if more than one partition
		if (numberOfPartition > SINGLE_FULL_PARTITION) {
			req.append("\n WHERE abs(hashtext(" + parser.getValues(CSVFormatRules.PARTITION_EXPRESSION).get(0)
					+ "::text)) % " + numberOfPartition + "=" + part + " ");
		}
		req.append("\n ) v ) w ");
		req.append("\n WHERE true ");

		// add filter where clause
		for (String s : parser.getValues(CSVFormatRules.FILTER_WHERE)) {
			req.append("\n AND (" + s + ")");
		}
		req.append(";");
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), req);
	}

	/**
	 * rebuild columns if a columns defined in rule is same as a source column,
	 * source columns is deleted and replaced by the column defnied in rules
	 * 
	 * @throws ArcException
	 */
	private void execQueryRebuildColumns() throws ArcException {

		List<String> colsIn = execQuerySelectColumnsFromLoadTable();

		StringBuilder query = new StringBuilder();
		for (int i = 0; i < parser.getValues(CSVFormatRules.COLUMN_DEFINITION).size(); i++) {

			if (colsIn.contains(parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(i))) {
				query.append("\n ALTER TABLE TTT DROP COLUMN "
						+ parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(i) + ";");
			}
			query.append("\n ALTER TABLE TTT RENAME COLUMN " + parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(i)
					+ Delimiters.RENAME_SUFFIX + " TO " + parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(i)
					+ ";");
		}

		query.append("\n DROP TABLE " + this.tmpChargementArc + ";");
		query.append("\n ALTER TABLE TTT RENAME TO " + this.tmpChargementArc + ";");

		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query);
	}

	List<String> execQuerySelectColumnsFromLoadTable() throws ArcException {
		return UtilitaireDao.get(0).getColumns(sandbox.getConnection(), this.tmpChargementArc);
	}

	public void execQueryBilan(String tableChargementPilTemp, TraitementPhase currentPhase) throws ArcException {

		ArcPreparedStatementBuilder requeteBilan = new ArcPreparedStatementBuilder();
		requeteBilan.append(ApiService.pilotageMarkIdsource(tableChargementPilTemp, fileIdCard.getIdSource(),
				currentPhase, TraitementEtat.OK, null));

		UtilitaireDao.get(0).executeBlock(sandbox.getConnection(), requeteBilan);
	}

}
