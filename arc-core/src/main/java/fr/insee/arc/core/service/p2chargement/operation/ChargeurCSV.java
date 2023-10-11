package fr.insee.arc.core.service.p2chargement.operation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;

import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.bo.CSVFileAttributes;
import fr.insee.arc.core.service.p2chargement.bo.CSVFormatRules;
import fr.insee.arc.core.service.p2chargement.bo.IChargeur;
import fr.insee.arc.core.service.p2chargement.dao.ChargeurCsvDao;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
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
	private TraitementPhase currentPhase;

	private InputStream streamHeader;
	private InputStream streamContent;

	private Sandbox sandbox;
	private FileIdCard fileIdCard;
	private CSVFileAttributes fileAttributes;

	private ParseFormatRulesOperation<CSVFormatRules> parser;

	private ChargeurCsvDao dao;

	public ChargeurCSV(ThreadChargementService threadChargementService) {

		this.sandbox = new Sandbox(threadChargementService.getConnexion().getExecutorConnection(),
				threadChargementService.getEnvExecution());
		this.fileIdCard = threadChargementService.getFileIdCard();
		this.fileAttributes = new CSVFileAttributes();
		this.parser = new ParseFormatRulesOperation<>(fileIdCard, CSVFormatRules.class);
		this.dao = new ChargeurCsvDao(this.sandbox, this.fileAttributes, this.fileIdCard, this.parser);

		this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
		this.currentPhase = threadChargementService.getCurrentPhase();
		this.streamContent = threadChargementService.getFilesInputStreamLoad().getTmpInxCSV();
		this.streamHeader = threadChargementService.getFilesInputStreamLoad().getTmpInxChargement();

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
				String.format("contenu delimiter %s", fileIdCard.getIdCardChargement().getDelimiter()));
		StaticLoggerDispatcher.debug(LOGGER,
				String.format("contenu format %s", fileIdCard.getIdCardChargement().getFormat()));

		// update delimiter
		fileIdCard.getIdCardChargement()
				.setDelimiter(ObjectUtils.firstNonNull(
						dao.execQueryEvaluateCharExpression(fileIdCard.getIdCardChargement().getDelimiter().trim()),
						Delimiters.DEFAULT_CSV_DELIMITER));

		// update quote
		parser.setValue(CSVFormatRules.QUOTE,
				dao.execQueryEvaluateCharExpression(parser.getValue(CSVFormatRules.QUOTE)));

		computeHeaders();

		// On crée la table dans laquelle on va copier le tout
		dao.initializeCsvTableContainer();

		importCsvDataToTable();

		StaticLoggerDispatcher.info(LOGGER, "** CSVtoBase end **");
	}

	/**
	 * compute the headers if no headers defined, read headers from file else set
	 * headers from the userDefinedHeaders defined in rules Headers read from files
	 * or from rules are transformed by setting v_ as prefix as ARC naming
	 * convention col1, col2, ... become v_col1, v_col2, ...
	 * 
	 * @param userDefinedHeaders
	 * @param separateur
	 * @throws ArcException
	 */
	private void computeHeaders() throws ArcException {
		String userDefinedHeaders = parser.getValue(CSVFormatRules.HEADERS);
		String csvDelimiter = fileIdCard.getIdCardChargement().getDelimiter();

		// si le headers n'est pas spécifié, alors on le cherche dans le fichier en
		// premier ligne
		if (userDefinedHeaders == null) {
			try {
				try (InputStreamReader inputStreamReader = new InputStreamReader(streamHeader);
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
						CSVReader readerCSV = new CSVReader(bufferedReader, csvDelimiter.charAt(0));) {

					String[] headers = getHeader(readerCSV);
					registerHeaders(headers);

				} finally {
					streamHeader.close();
				}
			} catch (IOException fileReadException) {
				throw new ArcException(fileReadException, ArcExceptionMessage.FILE_READ_FAILED,
						fileIdCard.getFileName());
			}
		} else {
			String[] headers = Format.tokenizeAndTrim(userDefinedHeaders, Delimiters.HEADERS_DELIMITER);
			registerHeaders(headers);
		}
	}

	private void registerHeaders(String[] headers) {
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
			throw new ArcException(fileReadException, ArcExceptionMessage.FILE_READ_FAILED,
					this.fileIdCard.getFileName());
		}
	}

	/**
	 * restructure a flat file
	 * 
	 * @throws ArcException
	 */
	private void applyFormat() throws ArcException {

		dao.execQueryApplyIndexRules();

		dao.execQueryApplyJoinRules();

		dao.execQueryApplyColumnsExpressionRules();

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

		dao.execQueryBilan(this.tableChargementPilTemp, this.currentPhase);

		java.util.Date endDate = new java.util.Date();
		StaticLoggerDispatcher.info(LOGGER,
				"** FlatBaseToIdedFlatBase temps**" + (endDate.getTime() - beginDate.getTime()) + " ms");

	}

	@Override
	public void initialisation() {
		// no operation
	}

	@Override
	public void finalisation() {
		// no operation
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
