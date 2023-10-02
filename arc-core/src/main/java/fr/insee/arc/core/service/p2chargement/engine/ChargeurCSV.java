package fr.insee.arc.core.service.p2chargement.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.bo.Delimiters;
import fr.insee.arc.core.service.p2chargement.bo.FileAttributes;
import fr.insee.arc.core.service.p2chargement.bo.FormatRulesCsv;
import fr.insee.arc.core.service.p2chargement.bo.Norme;
import fr.insee.arc.core.service.p2chargement.dao.ChargeurCsvDao;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.format.Format;

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

	private String tableChargementPilTemp;
	private String currentPhase;
	
	private InputStream streamHeader;
	private InputStream streamContent;

	private Sandbox sandbox;
	private FileAttributes fileAttributes;
	private Norme norme;
	private ParseFormatRulesOperation<FormatRulesCsv> parser; 

	private ChargeurCsvDao dao;
	

	public ChargeurCSV(ThreadChargementService threadChargementService, String fileName) {

		this.sandbox=new Sandbox(threadChargementService.getConnexion().getExecutorConnection(), threadChargementService.getEnvExecution());
		this.fileAttributes= new FileAttributes(fileName, threadChargementService.validite);
		this.norme = threadChargementService.normeOk;
		this.parser = new ParseFormatRulesOperation<>(norme, FormatRulesCsv.class);
		
		this.dao = new ChargeurCsvDao(this.sandbox, this.fileAttributes, this.norme, this.parser);

		this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();

		this.currentPhase = threadChargementService.getCurrentPhase();
		this.streamContent = threadChargementService.filesInputStreamLoad.getTmpInxCSV();
		this.streamHeader = threadChargementService.filesInputStreamLoad.getTmpInxChargement();
		
		

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
		StaticLoggerDispatcher.info(LOGGER, "** CSVtoBase begin **");

		StaticLoggerDispatcher.debug(LOGGER,
				String.format("contenu delimiter %s", norme.getRegleChargement().getDelimiter()));
		StaticLoggerDispatcher.debug(LOGGER,
				String.format("contenu format %s", norme.getRegleChargement().getFormat()));


		// update delimiter
		norme.getRegleChargement().setDelimiter(ObjectUtils.firstNonNull(dao.execQueryEvaluateCharExpression(norme.getRegleChargement().getDelimiter().trim()), Delimiters.DEFAULT_CSV_DELIMITER));

		// update quote
		parser.setValue(FormatRulesCsv.QUOTE, dao.execQueryEvaluateCharExpression(parser.getValue(FormatRulesCsv.QUOTE)));

		computeHeaders();

		// On crée la table dans laquelle on va copier le tout
		dao.initializeCsvTableContainer();

		importCsvDataToTable();

		StaticLoggerDispatcher.info(LOGGER, "** CSVtoBase end **");
	}
	
	/**
	 * compute the headers
	 * if no headers defined, read headers from file
	 * else set headers from the userDefinedHeaders defined in rules
	 * Headers read from files or from rules are transformed by setting v_ as prefix as ARC naming convention
	 * col1, col2, ... become v_col1, v_col2, ... 
	 * @param userDefinedHeaders
	 * @param separateur
	 * @throws ArcException
	 */
	private void computeHeaders() throws ArcException
	{
		String userDefinedHeaders = parser.getValue(FormatRulesCsv.HEADERS);
		String csvDelimiter = norme.getRegleChargement().getDelimiter();
		
		// si le headers n'est pas spécifié, alors on le cherche dans le fichier en
		// premier ligne
		if (userDefinedHeaders == null) {
			try {
				try (InputStreamReader inputStreamReader = new InputStreamReader(streamHeader);
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
						CSVReader readerCSV = new CSVReader(bufferedReader, csvDelimiter.charAt(0));) {
						
						String[] headers= getHeader(readerCSV);
						registerHeaders(headers);

				} finally {
					streamHeader.close();
				}
			} catch (IOException fileReadException) {
				throw new ArcException(fileReadException, ArcExceptionMessage.FILE_READ_FAILED, fileAttributes.getFileName());
			}
		} else {
			String[] headers = Format.tokenizeAndTrim(userDefinedHeaders, Delimiters.HEADERS_DELIMITER);
			registerHeaders(headers);
		}
	}
	
	
	private void registerHeaders(String[] headers)
	{
		this.fileAttributes.setHeaders(headers);
		this.fileAttributes.setHeadersI(Format.toBdId(headers));
		this.fileAttributes.setHeadersV(Format.toBdVal(headers));
	}

	/**
	 * Start the postgres copy command with the right parameters
	 * 
	 * @param quote the csv separator
	 * @throws ArcException
	 */
	private void importCsvDataToTable() throws ArcException {
		try {
			try {

				dao.execQueryCopyCsv(streamContent);

			} finally {
				streamContent.close();
			}
		} catch (IOException fileReadException) {
			throw new ArcException(fileReadException, ArcExceptionMessage.FILE_READ_FAILED, this.fileAttributes.getFileName());
		}
	}

	
	
	
	/**
	 * restructure a flat file
	 * 
	 * @throws ArcException
	 */
	private void applyFormat() throws ArcException {
			/*
			 * jointure
			 * 
			 */

			StringBuilder req;

			if (!parser.getValues(FormatRulesCsv.INDEX).isEmpty()) {
				req = new StringBuilder();
				for (int i = 0; i < parser.getValues(FormatRulesCsv.INDEX).size(); i++) {
					req.append("CREATE INDEX idx" + i + "_chargeurcsvidxrule ON " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + "("
							+ parser.getValues(FormatRulesCsv.INDEX).get(i) + ");\n");
				}
				UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), req);
			}

			if (!parser.getValues(FormatRulesCsv.JOIN_TABLE).isEmpty()) {
				req = new StringBuilder();

				req.append("\n DROP TABLE IF EXISTS TTT; ");
				req.append("\n CREATE TEMPORARY TABLE TTT AS ");

				// On renumérote les lignes après jointure pour etre cohérent
				req.append("\n SELECT  (row_number() over ())::int as id$new$, l.* ");
				for (int i = 0; i < parser.getValues(FormatRulesCsv.JOIN_TABLE).size(); i++) {
					req.append("\n , v" + i + ".* ");
				}
				req.append("FROM  " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + " l ");

				for (int i = 0; i < parser.getValues(FormatRulesCsv.JOIN_TABLE).size(); i++) {
					// if schema precised in table name, keep it, if not , add execution schema to
					// tablename
				parser.getValues(FormatRulesCsv.JOIN_TABLE).set(i,
							parser.getValues(FormatRulesCsv.JOIN_TABLE).get(i).contains(".") ? parser.getValues(FormatRulesCsv.JOIN_TABLE).get(i) : this.sandbox.getSchema() + "." + parser.getValues(FormatRulesCsv.JOIN_TABLE).get(i));

					// récupération des colonnes de la table
					List<String> colsIn = UtilitaireDao.get(0)
							.executeRequest(sandbox.getConnection(),
									new ArcPreparedStatementBuilder(
											"select " + parser.getValues(FormatRulesCsv.JOIN_SELECT).get(i) + " from " + parser.getValues(FormatRulesCsv.JOIN_TABLE).get(i) + " limit 0"))
							.get(0);

					// join type
					req.append("\n " + parser.getValues(FormatRulesCsv.JOIN_TYPE).get(i) + " ");

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
					req.append("\n FROM " + parser.getValues(FormatRulesCsv.JOIN_TABLE).get(i) + " ");
					req.append("\n ) v" + i + " ");
					req.append("\n ON " + parser.getValues(FormatRulesCsv.JOIN_CLAUSE).get(i) + " ");

				}
				req.append("\n ;");
				req.append("\n ALTER TABLE TTT DROP COLUMN id; ");
				req.append("\n ALTER TABLE TTT RENAME COLUMN id$new$ TO id; ");
				req.append("\n DROP TABLE " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + ";");
				req.append("\n ALTER TABLE TTT RENAME TO " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + ";");
				UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), req);
			}

			/*
			 * recalcule de colonnes si une colonne existe déjà, elle est écrasée sinon la
			 * nouvelle colonne est créée
			 */
			if (!parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).isEmpty()) {
				List<String> colsIn = UtilitaireDao.get(0)
						.executeRequest(this.sandbox.getConnection(),
								new ArcPreparedStatementBuilder("select * from " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + " limit 0"))
						.get(0);

				String renameSuffix = "$new$";
				String partitionNumberPLaceHolder = "#pn#";
				req = new StringBuilder();

				// Creation de la table
				req.append("\n DROP TABLE IF EXISTS TTT; ");
				req.append("\n CREATE TEMPORARY TABLE TTT AS ");
				req.append("\n SELECT w.* FROM ");
				req.append("\n (SELECT v.* ");

				for (int i = 0; i < parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).size(); i++) {
					// si on trouve dans l'expression le suffix alors on sait qu'on a voulu
					// préalablement calculer la valeur
					if (parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).contains(renameSuffix)) {
						req.append("\n ,");
						req.append(parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).replace(partitionNumberPLaceHolder, "0::bigint"));
						req.append(" as ");
						req.append(parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i) + renameSuffix + " ");
					}
				}
				req.append("\n FROM ");
				req.append("\n (SELECT u.* ");
				for (int i = 0; i < parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).size(); i++) {
					if (!parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).contains(renameSuffix)) {
						req.append("\n ,");
						req.append(parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).replace(partitionNumberPLaceHolder, "0::bigint"));
						req.append(" as ");
						req.append(parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i) + renameSuffix + " ");
					}
				}
				req.append("\n FROM " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + " u ) v ) w ");
				req.append("\n WHERE false ");
				for (String s : parser.getValues(FormatRulesCsv.FILTER_WHERE)) {
					req.append("\n AND (" + s + ")");
				}
				req.append(";");
				UtilitaireDao.get(0).executeImmediate(sandbox.getConnection(), req);

				// Itération

				// si pas de partition, nbIteration=1
				boolean partitionedProcess = !parser.getValues(FormatRulesCsv.PARTITION_EXPRESSION).isEmpty();

				// default value of the maximum records per partition
				int partitionSize = DataObjectService.MAX_NUMBER_OF_RECORD_PER_PARTITION;

				int nbPartition = 1;
				// creation de l'index de partitionnement
				if (partitionedProcess) {
					// comptage rapide sur échantillon à 1/10000 pour trouver le nombre de partiton
					nbPartition = UtilitaireDao.get(0).getInt(sandbox.getConnection(),
							new ArcPreparedStatementBuilder("select ((count(*)*10000)/" + partitionSize + ")+1 from "
									+ ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + " tablesample system(0.01)"));

					req = new StringBuilder();
					req.append("\n CREATE INDEX idx_partition_by_arc on " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + " ((abs(hashtext("
							+ parser.getValues(FormatRulesCsv.PARTITION_EXPRESSION).get(0) + "::text)) % " + nbPartition + "));");
					UtilitaireDao.get(0).executeImmediate(sandbox.getConnection(), req);
				}

				int nbIteration = nbPartition;

				for (int part = 0; part < nbIteration; part++) {
					req = new StringBuilder();
					req.append("\n INSERT INTO TTT ");
					req.append("\n SELECT w.* FROM ");
					req.append("\n (SELECT v.* ");
					for (int i = 0; i < parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).size(); i++) {
						// si on trouve dans l'expression le suffix alors on sait qu'on a voulu
						// préalablement calculer la valeur
						if (parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).contains(renameSuffix)) {
							req.append("\n ,");
							req.append(parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).replace(partitionNumberPLaceHolder, part + "000000000000::bigint"));
							req.append(" as ");
							req.append(parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i) + renameSuffix + " ");
						}
					}
					req.append("\n FROM ");
					req.append("\n (SELECT u.* ");
					for (int i = 0; i < parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).size(); i++) {
						if (!parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).contains(renameSuffix)) {
							req.append("\n ,");
							req.append(parser.getValues(FormatRulesCsv.COLUMN_EXPRESSION).get(i).replace(partitionNumberPLaceHolder, part + "000000000000::bigint"));
							req.append(" as ");
							req.append(parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i) + renameSuffix + " ");
						}
					}
					req.append("\n FROM " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + " u ");
					if (partitionedProcess) {
						req.append("\n WHERE abs(hashtext(" + parser.getValues(FormatRulesCsv.PARTITION_EXPRESSION).get(0) + "::text)) % " + nbPartition
								+ "=" + part + " ");
					}
					req.append("\n ) v ) w ");
					req.append("\n WHERE true ");
					for (String s : parser.getValues(FormatRulesCsv.FILTER_WHERE)) {
						req.append("\n AND (" + s +")");
					}
					req.append(";");
					UtilitaireDao.get(0).executeImmediate(sandbox.getConnection(), req);
				}

				req = new StringBuilder();
				for (int i = 0; i < parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).size(); i++) {

					if (colsIn.contains(parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i))) {
						req.append("\n ALTER TABLE TTT DROP COLUMN " + parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i) + ";");
					}
					req.append("\n ALTER TABLE TTT RENAME COLUMN " + parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i) + renameSuffix + " TO " + parser.getValues(FormatRulesCsv.COLUMN_DEFINITION).get(i)
							+ ";");
				}

				req.append("\n DROP TABLE " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + ";");
				req.append("\n ALTER TABLE TTT RENAME TO " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + ";");

				UtilitaireDao.get(0).executeImmediate(sandbox.getConnection(), req);
			}
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

		// create the container table to tran
		dao.execQueryCreateContainerWithArcMetadata();
		
		applyFormat();

		StringBuilder requeteBilan = new StringBuilder();
		requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileAttributes.getFileName(), this.currentPhase,
				TraitementEtat.OK.toString(), null));

		UtilitaireDao.get(0).executeBlock(sandbox.getConnection(), requeteBilan);

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

		
		// parse formatting rules
		parser.parseFormatRules();		
		
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
