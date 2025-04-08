package fr.insee.arc.core.service.p2chargement.operation;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.bo.IChargeur;
import fr.insee.arc.core.service.p2chargement.dao.ChargeurXMLDao;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.service.p2chargement.xmlhandler.XMLHandlerCharger4;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.SecuredSaxParser;

/**
 * Classe chargeant les fichiers Xml. Utiliser l'api SAX pour parser les
 * fichiers
 * 
 * @author S4LWO8
 *
 */
public class ChargeurXml implements IChargeur {
	private static final Logger LOGGER = LogManager.getLogger(ChargeurXml.class);
	
	
	private Sandbox sandbox;
	private FileIdCard fileIdCard;
	private String tableChargementPilTemp;
	private TraitementPhase currentPhase;
	private InputStream f;

	// temporary table where data will be loaded by the XML SAX engine
	private String tableTempA;
	private String rapport = null;
	private String jointure;

	private ChargeurXMLDao dao;

	
	/**
	 * constructor with thread object
	 * 
	 * @param threadChargementService
	 * @param fileName
	 */
	public ChargeurXml(ThreadChargementService threadChargementService) {
		this.sandbox = new Sandbox(threadChargementService.getConnexion().getExecutorConnection(),
				threadChargementService.getEnvExecution());
		this.fileIdCard = threadChargementService.getFileIdCard();
		this.tableTempA = threadChargementService.getTableTempA();

		this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
		this.currentPhase = threadChargementService.getCurrentExecutedPhase();
		this.f = threadChargementService.getFilesInputStreamLoad().getTmpInxChargement();
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

		// create the data container where data will be inserted
		dao.execQueryCreateTemporaryLoadDataTable(this.tableTempA);
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
		XMLHandlerCharger4 handler = new XMLHandlerCharger4(sandbox.getConnection(), fileIdCard, this.tableTempA);

		// appel du parser et gestion d'erreur
		try {
			SAXParser saxParser = SecuredSaxParser.buildSecuredSaxParser();
			saxParser.parse(f, handler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			ArcException businessException = new ArcException(e, ArcExceptionMessage.XML_SAX_PARSING_FAILED,
					fileIdCard.getIdSource()).logMessageException();
			rapport = businessException.getMessage();
			throw businessException;
		}

		this.jointure = handler.getJointure();

		java.util.Date endDate = new java.util.Date();
		StaticLoggerDispatcher.info(LOGGER, "** excecution temps" + (endDate.getTime() - beginDate.getTime()) + " ms");

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

}
