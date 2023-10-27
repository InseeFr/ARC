package fr.insee.arc.core.service.p2chargement.operation;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.bo.IChargeur;
import fr.insee.arc.core.service.p2chargement.dao.ChargeurXMLDao;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.service.p2chargement.xmlhandler.XMLComplexeHandlerCharger;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Pair;
import fr.insee.arc.utils.utils.SecuredSaxParser;

/**
 * Classe chargeant les fichiers Xml. Utiliser l'api SAX pour parser les
 * fichiers
 * 
 * @author S4LWO8
 *
 */
public class ChargeurXmlComplexe implements IChargeur {
	private static final Logger LOGGER = LogManager.getLogger(ChargeurXmlComplexe.class);

	private Sandbox sandbox;
	private String tableChargementPilTemp;
	private TraitementPhase currentPhase;
	private FileIdCard fileIdCard;
	private InputStream f;

	private List<Pair<String, String>> format;

	// temporary table where data will be loaded by the XML SAX engine
	private String tableTempA;
	private String rapport;
	private String jointure;

	private ChargeurXMLDao dao;

	public ChargeurXmlComplexe(ThreadChargementService threadChargementService) {
		
		this.sandbox = new Sandbox(threadChargementService.getConnexion().getExecutorConnection(),
				threadChargementService.getEnvExecution());
		
		this.tableTempA = threadChargementService.getTableTempA();
		this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
		this.currentPhase = threadChargementService.getCurrentPhase();
		this.f = threadChargementService.getFilesInputStreamLoad().getTmpInxChargement();
		this.fileIdCard = threadChargementService.getFileIdCard();
		
		dao = new ChargeurXMLDao(sandbox, fileIdCard);
		
	}

	public ChargeurXmlComplexe(Connection connexion, String envExecution, FileIdCard fileIdCard, InputStream fileInputStream, String tableOut) {
		
		this.sandbox = new Sandbox(connexion, envExecution);
		
		this.fileIdCard = fileIdCard;
		this.tableTempA = tableOut;
		this.f = fileInputStream;
		
		dao = new ChargeurXMLDao(sandbox, fileIdCard);
		
	}

	/**
	 * Autonomous execution with parameters constructor
	 * 
	 * @throws ArcException
	 */
	public void executeEngine() throws ArcException {
		initialisation();
		execution();
	}

	@Override
	public void initialisation() throws ArcException {

		parseFormatRules();

		// create the data container where data will be inserted
		dao.execQueryCreateTemporaryLoadDataTable(this.tableTempA);

	}

	/**
	 * Simple format rules like pere,fils pere,fils pere,fils
	 */
	private void parseFormatRules() {
		this.format = new ArrayList<>();
		if (this.fileIdCard.getIdCardChargement().getFormat() != null) {
			for (String rule : this.fileIdCard.getIdCardChargement().getFormat().split("\n")) {
				this.format.add(new Pair<>(rule.split(Delimiters.HEADERS_DELIMITER)[0].trim(),
						rule.split(Delimiters.HEADERS_DELIMITER)[1].trim()));
			}
		}
	}

	@Override
	public void finalisation() throws ArcException {
		dao.execQueryBilan(tableChargementPilTemp, currentPhase, rapport, jointure);
	}

	@Override
	public void execution() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** execution**");
		java.util.Date beginDate = new java.util.Date();

		// Création de la table de stockage
		XMLComplexeHandlerCharger handler = new XMLComplexeHandlerCharger(sandbox.getConnection(), fileIdCard, this.tableTempA,
				format);
		// appel du parser et gestion d'erreur
		try {
			SAXParser saxParser = SecuredSaxParser.buildSecuredSaxParser();
			saxParser.parse(f, handler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			ArcException businessException = new ArcException(e, ArcExceptionMessage.XML_SAX_PARSING_FAILED,
					this.fileIdCard.getIdSource()).logMessageException();
			rapport = FormatSQL.quoteTextWithoutEnclosings(businessException.getMessage());
			throw businessException;
		}

		this.jointure = handler.getJointure();

		java.util.Date endDate = new java.util.Date();
		StaticLoggerDispatcher.info(LOGGER, "** execution temps" + (endDate.getTime() - beginDate.getTime()) + " ms");

	}

	@Override
	public void charger() throws ArcException {
		initialisation();
		execution();
		finalisation();

	}

	/**
	 * @return the f
	 */
	public InputStream getF() {
		return f;
	}

	/**
	 * @param f the f to set
	 */
	public void setF(InputStream f) {
		this.f = f;
	}

	public String getJointure() {
		return jointure;
	}

}
