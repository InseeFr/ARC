package fr.insee.arc.core.service.p2chargement.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.dao.DateConversion;
import fr.insee.arc.core.service.p2chargement.bo.Norme;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.XMLUtil;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Classe pour charger les fichier CSV. On lit les headers du fichier qu'on va
 * sauvegarder. Puis ligne par ligne on va parser le fichier et le transformer
 * en xml. Attention on va diviser le fichier en entrée en plusieurs fichier en
 * sortie si necessaire.
 * 
 * @author S4LWO8
 *
 */

public class ChargeurCSV implements IChargeur {
	private static final Logger LOGGER = LogManager.getLogger(ChargeurCSV.class);

	private String separateur = ";";
	private String encoding = null;
	private String quote = null;
	private String userDefinedHeaders = null;
	private String[] headers;
	private String fileName;
	private Connection connexion;
	private String tableChargementPilTemp;
	private String currentPhase;
	private Norme norme;
	private String tableTempA;
	private static final String TABLE_TEMP_T = "T";
	private InputStream streamHeader;
	private InputStream streamContent;
	private String env;
	private String validite;
	private final String integrationDate = DateConversion.queryDateConversion(new Date());

	public ChargeurCSV(ThreadChargementService threadChargementService, String fileName) {
		this.fileName = fileName;
		this.connexion = threadChargementService.getConnexion().getExecutorConnection();
		this.tableTempA = threadChargementService.getTableTempA();
		this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
		this.currentPhase = threadChargementService.getCurrentPhase();
		this.streamContent = threadChargementService.filesInputStreamLoad.getTmpInxCSV();
		this.streamHeader = threadChargementService.filesInputStreamLoad.getTmpInxChargement();
		this.env = threadChargementService.getEnvExecution();
		this.norme = threadChargementService.normeOk;
		this.validite = threadChargementService.validite;
	}

	public ChargeurCSV() {
	}

	/**
	 * Ajout du 26/09/2017
	 * 
	 * Charger le csv directement en base avec COPY, on s'occupera des i et v plus
	 * tard
	 * 
	 * @throws ArcException
	 * @throws IOException
	 * @throws ArcException
	 * 
	 * @throws ArcException
	 */

	private void copyCsvFileToDatabase() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** CSVtoBase **");

		java.util.Date beginDate = new java.util.Date();

		StaticLoggerDispatcher.debug(LOGGER,
				String.format("contenu delimiter %s", norme.getRegleChargement().getDelimiter()));
		StaticLoggerDispatcher.debug(LOGGER,
				String.format("contenu format %s", norme.getRegleChargement().getFormat()));

		this.separateur = norme.getRegleChargement().getDelimiter().trim();
		this.encoding = XMLUtil.parseXML(norme.getRegleChargement().getFormat(), "encoding");
		this.userDefinedHeaders = XMLUtil.parseXML(norme.getRegleChargement().getFormat(), "headers");
		this.quote = XMLUtil.parseXML(norme.getRegleChargement().getFormat(), "quote");

		// si le quote est une expression complexe, l'interpreter par postgres
		if (this.quote != null && this.quote.length() > 1 && this.quote.length() < 8) {
			ArcPreparedStatementBuilder req = new ArcPreparedStatementBuilder();
			req.append("SELECT " + this.quote + " ");
			this.quote = UtilitaireDao.get(0).executeRequest(this.connexion, req).get(2).get(0);
		}

		// si le séparateur est une expression complexe, l'interpreter par postgres
		if (this.separateur != null && this.separateur.length() > 1 && this.separateur.length() < 8) {
			ArcPreparedStatementBuilder req = new ArcPreparedStatementBuilder();
			req.append("select " + this.separateur + " ");
			this.separateur = UtilitaireDao.get(0).executeRequest(this.connexion, req).get(2).get(0);
		}

		// si le headers n'est pas spécifié, alors on le cherche dans le fichier en
		// premier ligne
		if (this.userDefinedHeaders == null) {
			try {
				try (InputStreamReader inputStreamReader = new InputStreamReader(streamHeader);
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
						CSVReader readerCSV = new CSVReader(bufferedReader,
								(separateur == null) ? ';' : separateur.charAt(0));) {
					this.headers = getHeader(readerCSV);
				} finally {
					streamHeader.close();
				}
			} catch (IOException fileReadException) {
				throw new ArcException(fileReadException, ArcExceptionMessage.FILE_READ_FAILED, this.fileName);
			}
		} else {
			this.headers = userDefinedHeaders.replaceAll(" ", "").split(",");
		}

		// Make headers format to be database compliant
		for (int i = 0; i < headers.length; i++) {
			headers[i] = Format.toBdVal(headers[i]);
		}

		// On crée la table dans laquelle on va copier le tout
		initializeCsvTableContainer();

		importCsvDataToTable();

		java.util.Date endDate = new java.util.Date();

		StaticLoggerDispatcher.info(LOGGER, "** CSVtoBase temps**" + (endDate.getTime() - beginDate.getTime()) + " ms");

	}

	/**
	 * Start the postgres copy in commande with the right parameters
	 * 
	 * @param quote the csv separator
	 * @throws ArcException
	 */
	private void importCsvDataToTable() throws ArcException {

		try {
			try {
				StringBuilder columnForCopy = new StringBuilder();
				columnForCopy.append("(");
				for (String nomCol : this.headers) {
					columnForCopy.append("" + nomCol + " ,");
				}
				columnForCopy.setLength(columnForCopy.length() - 1);
				columnForCopy.append(")");

				UtilitaireDao.get(0).importing(connexion, TABLE_TEMP_T, columnForCopy.toString(), streamContent,
						this.userDefinedHeaders == null, this.separateur, this.quote, this.encoding);
			} finally {
				streamContent.close();
			}
		} catch (IOException fileReadException) {
			throw new ArcException(fileReadException, ArcExceptionMessage.FILE_READ_FAILED, this.fileName);
		}
	}

	/**
	 * restructure a flat file
	 * 
	 * @throws ArcException
	 */
	private void applyFormat() throws ArcException {
		String format = norme.getRegleChargement().getFormat();
		if (format != null && !format.isEmpty()) {
			format = format.trim();
			String[] lines = format.split("\n");

			ArrayList<String> cols = new ArrayList<>();
			ArrayList<String> exprs = new ArrayList<>();
			ArrayList<String> wheres = new ArrayList<>();

			ArrayList<String> joinTable = new ArrayList<>();
			ArrayList<String> joinType = new ArrayList<>();
			ArrayList<String> joinClause = new ArrayList<>();
			ArrayList<String> joinSelect = new ArrayList<>();
			ArrayList<String> partitionExpression = new ArrayList<>();
			ArrayList<String> indexExpression = new ArrayList<>();

			for (String line : lines) {
				if (line.startsWith("<join-table>")) {
					joinTable.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<join-type>")) {
					joinType.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<join-clause>")) {
					joinClause.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<join-select")) {
					joinSelect.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<where>")) {
					wheres.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<partition-expression>")) {
					partitionExpression.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<index>")) {
					indexExpression.add(ManipString.substringAfterFirst(line, ">").trim());
				} else if (line.startsWith("<encoding>")) {
				} else if (line.startsWith("<headers>")) {
				} else if (line.startsWith("<quote>")) {
				} else if (line.startsWith("/*")) {

				} else {
					cols.add(ManipString.substringBeforeFirst(line, "=").trim());
					exprs.add(ManipString.substringAfterFirst(line, "=").trim());
				}
			}

			/*
			 * jointure
			 * 
			 */

			StringBuilder req;

			if (!indexExpression.isEmpty()) {
				req = new StringBuilder();
				for (int i = 0; i < indexExpression.size(); i++) {
					req.append("CREATE INDEX idx" + i + "_chargeurcsvidxrule ON " + this.tableTempA + "("
							+ indexExpression.get(i) + ");\n");
				}
				UtilitaireDao.get(0).executeImmediate(connexion, req);
			}

			if (!joinTable.isEmpty()) {
				req = new StringBuilder();

				req.append("\n DROP TABLE IF EXISTS TTT; ");
				req.append("\n CREATE TEMPORARY TABLE TTT AS ");

				// On renumérote les lignes après jointure pour etre cohérent
				req.append("\n SELECT  (row_number() over ())::int as id$new$, l.* ");
				for (int i = 0; i < joinTable.size(); i++) {
					req.append("\n , v" + i + ".* ");
				}
				req.append("FROM  " + this.tableTempA + " l ");

				for (int i = 0; i < joinTable.size(); i++) {
					// if schema precised in table name, keep it, if not , add execution schema to
					// tablename

					joinTable.set(i,
							joinTable.get(i).contains(".") ? joinTable.get(i) : this.env + "." + joinTable.get(i));

					// récupération des colonnes de la table
					List<String> colsIn = UtilitaireDao.get(0)
							.executeRequest(this.connexion,
									new ArcPreparedStatementBuilder(
											"select " + joinSelect.get(i) + " from " + joinTable.get(i) + " limit 0"))
							.get(0);

					// join type
					req.append("\n " + joinType.get(i) + " ");

					req.append("\n (SELECT ");
					// build column name to be suitable to load process aka : i_col, v_col
					boolean start = true;
					for (int j = 0; j < colsIn.size(); j++) {
						if (start) {
							start = false;
						} else {
							req.append("\n ,");
						}

						req.append("null::int as i_" + colsIn.get(j) + ", " + colsIn.get(j) + " as v_" + colsIn.get(j)
								+ " ");

					}
					req.append("\n FROM " + joinTable.get(i) + " ");
					req.append("\n ) v" + i + " ");
					req.append("\n ON " + joinClause.get(i) + " ");

				}
				req.append("\n ;");
				req.append("\n ALTER TABLE TTT DROP COLUMN id; ");
				req.append("\n ALTER TABLE TTT RENAME COLUMN id$new$ TO id; ");
				req.append("\n DROP TABLE " + this.tableTempA + ";");
				req.append("\n ALTER TABLE TTT RENAME TO " + this.tableTempA + ";");
				UtilitaireDao.get(0).executeImmediate(connexion, req);
			}

			/*
			 * recalcule de colonnes si une colonne existe déjà, elle est écrasée sinon la
			 * nouvelle colonne est créée
			 */
			if (!cols.isEmpty()) {
				List<String> colsIn = UtilitaireDao.get(0)
						.executeRequest(this.connexion,
								new ArcPreparedStatementBuilder("select * from " + this.tableTempA + " limit 0"))
						.get(0);

				String renameSuffix = "$new$";
				String partitionNumberPLaceHolder = "#pn#";
				req = new StringBuilder();

				// Creation de la table
				req.append("\n DROP TABLE IF EXISTS TTT; ");
				req.append("\n CREATE TEMPORARY TABLE TTT AS ");
				req.append("\n SELECT w.* FROM ");
				req.append("\n (SELECT v.* ");
				for (int i = 0; i < cols.size(); i++) {
					// si on trouve dans l'expression le suffix alors on sait qu'on a voulu
					// préalablement calculer la valeur
					if (exprs.get(i).contains(renameSuffix)) {
						req.append("\n ,");
						req.append(exprs.get(i).replace(partitionNumberPLaceHolder, "0::bigint"));
						req.append(" as ");
						req.append(cols.get(i) + renameSuffix + " ");
					}
				}
				req.append("\n FROM ");
				req.append("\n (SELECT u.* ");
				for (int i = 0; i < cols.size(); i++) {
					if (!exprs.get(i).contains(renameSuffix)) {
						req.append("\n ,");
						req.append(exprs.get(i).replace(partitionNumberPLaceHolder, "0::bigint"));
						req.append(" as ");
						req.append(cols.get(i) + renameSuffix + " ");
					}
				}
				req.append("\n FROM " + this.tableTempA + " u ) v ) w ");
				req.append("\n WHERE false ");
				for (String s : wheres) {
					req.append("\n AND (" + s + ")");
				}
				req.append(";");
				UtilitaireDao.get(0).executeImmediate(connexion, req);

				// Itération

				// si pas de partition, nbIteration=1
				boolean partitionedProcess = (partitionExpression.size() > 0);

				// default value of the maximum records per partition
				int partitionSize = DataObjectService.MAX_NUMBER_OF_RECORD_PER_PARTITION;

				int nbPartition = 1;
				// creation de l'index de partitionnement
				if (partitionedProcess) {
					// comptage rapide su échantillon à 1/10000 pour trouver le nombre de partiton
					nbPartition = UtilitaireDao.get(0).getInt(connexion,
							new ArcPreparedStatementBuilder("select ((count(*)*10000)/" + partitionSize + ")+1 from "
									+ this.tableTempA + " tablesample system(0.01)"));

					req = new StringBuilder();
					req.append("\n CREATE INDEX idx_a on " + this.tableTempA + " ((abs(hashtext("
							+ partitionExpression.get(0) + "::text)) % " + nbPartition + "));");
					UtilitaireDao.get(0).executeImmediate(connexion, req);
				}

				int nbIteration = nbPartition;

				for (int part = 0; part < nbIteration; part++) {
					req = new StringBuilder();
					req.append("\n INSERT INTO TTT ");
					req.append("\n SELECT w.* FROM ");
					req.append("\n (SELECT v.* ");
					for (int i = 0; i < cols.size(); i++) {
						// si on trouve dans l'expression le suffix alors on sait qu'on a voulu
						// préalablement calculer la valeur
						if (exprs.get(i).contains(renameSuffix)) {
							req.append("\n ,");
							req.append(exprs.get(i).replace(partitionNumberPLaceHolder, part + "000000000000::bigint"));
							req.append(" as ");
							req.append(cols.get(i) + renameSuffix + " ");
						}
					}
					req.append("\n FROM ");
					req.append("\n (SELECT u.* ");
					for (int i = 0; i < cols.size(); i++) {
						if (!exprs.get(i).contains(renameSuffix)) {
							req.append("\n ,");
							req.append(exprs.get(i).replace(partitionNumberPLaceHolder, part + "000000000000::bigint"));
							req.append(" as ");
							req.append(cols.get(i) + renameSuffix + " ");
						}
					}
					req.append("\n FROM " + this.tableTempA + " u ");
					if (partitionedProcess) {
						req.append("\n WHERE abs(hashtext(" + partitionExpression.get(0) + "::text)) % " + nbPartition
								+ "=" + part + " ");
					}
					req.append("\n ) v ) w ");
					req.append("\n WHERE true ");
					for (String s : wheres) {
						req.append("\n AND (" + s +")");
					}
					req.append(";");
					UtilitaireDao.get(0).executeImmediate(connexion, req);
				}

				req = new StringBuilder();
				for (int i = 0; i < cols.size(); i++) {

					if (colsIn.contains(cols.get(i))) {
						req.append("\n ALTER TABLE TTT DROP COLUMN " + cols.get(i) + ";");
					}
					req.append("\n ALTER TABLE TTT RENAME COLUMN " + cols.get(i) + renameSuffix + " TO " + cols.get(i)
							+ ";");
				}

				req.append("\n DROP TABLE " + this.tableTempA + ";");
				req.append("\n ALTER TABLE TTT RENAME TO " + this.tableTempA + ";");

				UtilitaireDao.get(0).executeImmediate(connexion, req);
			}
		}
	}

	/**
	 * Create the table where the csv file data will be stored
	 * 
	 * @throws ArcException
	 */
	private void initializeCsvTableContainer() throws ArcException {
		StringBuilder req = new StringBuilder();
		req.append("DROP TABLE IF EXISTS " + TABLE_TEMP_T + " ;");
		req.append(" \nCREATE TEMPORARY TABLE " + TABLE_TEMP_T + " (");

		for (String nomCol : this.headers) {
			req.append("\n\t " + nomCol + " text,");
		}

		req.append("\n\t id SERIAL");
		req.append(");");

		UtilitaireDao.get(0).executeImmediate(connexion, req);
	}

	/**
	 * @param bufferedReader
	 * @return
	 * @return
	 * @throws IOException
	 */
	private String[] getHeader(CSVReader readerCSV) throws IOException {

		return readerCSV.readNext();
	}

	/**
	 * Transform the raw data table from csv file (col1, col2, col3, ...) to an ARC
	 * standard table (col1, col2, col3, ...) becomes (i_col1, v_col1, i_col2,
	 * v_col2, i_col3, v_col3, ...) where i are line index and v the value also meta
	 * data column (filename, norme, validite) are added to the ARC standard table
	 * 
	 * @throws ArcException
	 */
	private void transformCsvDataToArcData() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** FlatBaseToIdedFlatBase **");
		java.util.Date beginDate = new java.util.Date();

		StringBuilder req = new StringBuilder();
		req.append("DROP TABLE IF EXISTS " + tableTempA + ";");
		req.append("CREATE ");
		if (!tableTempA.contains(".")) {
			req.append("TEMPORARY ");
		} else {
			req.append(" ");
		}

		req.append(" TABLE " + this.tableTempA);
		req.append(" AS (SELECT ");
		req.append("\n '" + this.fileName + "'::text collate \"C\" as " + ColumnEnum.ID_SOURCE.getColumnName());
		req.append("\n ,id::integer as id");
		req.append("\n ," + integrationDate + "::text collate \"C\" as date_integration ");
		req.append("\n ,'" + this.norme.getIdNorme() + "'::text collate \"C\" as id_norme ");
		req.append("\n ,'" + this.norme.getPeriodicite() + "'::text collate \"C\" as periodicite ");
		req.append("\n ,'" + this.validite + "'::text collate \"C\" as validite ");
		req.append("\n ,0::integer as nombre_colonne");

		req.append("\n , ");

		for (int i = 0; i < this.headers.length; i++) {
			req.append("id as " + Format.toBdId(Format.toBdRemovePrefix(headers[i])) + ", " + headers[i] + ",");
		}

		req.setLength(req.length() - 1);

		req.append("\n FROM " + TABLE_TEMP_T + ");");
		req.append("DROP TABLE IF EXISTS " + TABLE_TEMP_T + ";");

		UtilitaireDao.get(0).executeImmediate(this.connexion, req);

		applyFormat();

		StringBuilder requeteBilan = new StringBuilder();
		requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName, this.currentPhase,
				TraitementEtat.OK.toString(), null));

		UtilitaireDao.get(0).executeBlock(this.connexion, requeteBilan);

		java.util.Date endDate = new java.util.Date();
		StaticLoggerDispatcher
				.info(LOGGER, "** FlatBaseToIdedFlatBase temps**" + (endDate.getTime() - beginDate.getTime()) + " ms");

	}

	@Override
	public void initialisation() {

	}

	@Override
	public void finalisation() {

	}

	@Override
	public void execution() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "execution");

		// On met le fichier en base
		copyCsvFileToDatabase();

		// on retraduit le fichier à la mode ARC avec des i_ et v_ pour chacune des
		// colonnes et en ajoutant les meta data
		transformCsvDataToArcData();

	}

	@Override
	public void charger() throws ArcException {
		initialisation();
		execution();
		finalisation();

	}

}