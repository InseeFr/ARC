package fr.insee.arc.core.service.p2chargement.engine;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p2chargement.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.service.p2chargement.xmlhandler.XMLHandlerCharger4;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.textUtils.FastList;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
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
	private FileIdCard fileIdCard;
	private Connection connexion;
	private String tableChargementPilTemp;
	private String currentPhase;
	private InputStream f;

	// temporary table where data will be loaded by the XML SAX engine
	private String tableTempA;
	private FastList<String> tempTableAColumnsLongName = new FastList<>(Arrays.asList(
			ColumnEnum.ID_SOURCE.getColumnName(), "id", "date_integration", "id_norme", "periodicite", "validite"));
	private FastList<String> tempTableAColumnsShortName = new FastList<>(
			Arrays.asList("m0", "m1", "m2", "m3", "m4", "m5"));
	private FastList<String> tempTableAColumnsType = new FastList<>(
			Arrays.asList(ColumnEnum.ID_SOURCE.getColumnType().getTypeCollated(), "int", "text collate \"C\"",
					"text collate \"C\"", "text collate \"C\"", "text collate \"C\""));

	private String rapport = null;
	private String jointure;

	/**
	 * constructor with thread object
	 * 
	 * @param threadChargementService
	 * @param fileName
	 */
	public ChargeurXml(ThreadChargementService threadChargementService) {
		this.fileIdCard = threadChargementService.fileIdCard;
		this.connexion = threadChargementService.getConnexion().getExecutorConnection();
		this.tableTempA = threadChargementService.getTableTempA();
		this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
		this.currentPhase = threadChargementService.getCurrentPhase();
		this.f = threadChargementService.filesInputStreamLoad.getTmpInxChargement();
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
		StaticLoggerDispatcher.info(LOGGER, "** requeteCreateA **");

		java.util.Date beginDate = new java.util.Date();

		StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.dropTable(this.tableTempA));
		requete.append("CREATE ");

		if (!this.tableTempA.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}

		// la tabble temporaire A : ids|id|d|data|nombre_colonnes
		// data : contiendra les données chargé au format text séparée par des virgules
		// nombre_colonnes : contiendra le nombre de colonne contenue dans data,
		// nécessaire pour compléter la ligne avec des virgules

		requete.append(" TABLE " + this.tableTempA + " (");
		boolean noComma = true;
		for (int i = 0; i < tempTableAColumnsLongName.size(); i++) {
			if (noComma) {
				noComma = false;
			} else {
				requete.append(",");
			}
			requete.append(tempTableAColumnsShortName.get(i) + " " + tempTableAColumnsType.get(i) + " ");
		}
		requete.append(") ");
		requete.append(FormatSQL.WITH_NO_VACUUM);
		requete.append(";");

		try {
			UtilitaireDao.get(0).executeBlock(this.connexion, requete);
		} catch (ArcException e) {
			LoggerHelper.errorAsComment(LOGGER,
					"ChargeurXML.initialisation - creation failed on the temporary table A which is the temporary recipient for the xml file to be loaded");
			throw e;
		}
		java.util.Date endDate = new java.util.Date();

		StaticLoggerDispatcher.info(LOGGER,
				"** requeteCreateA en " + (endDate.getTime() - beginDate.getTime()) + " ms **");

	}

	@Override
	public void finalisation() {
		StringBuilder requeteBilan = new StringBuilder();
		requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileIdCard.getFileName(), this.currentPhase,
				TraitementEtat.OK.toString(), rapport, this.jointure));

		try {
			UtilitaireDao.get(0).executeBlock(this.connexion, requeteBilan);
		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "chargerXml()", LOGGER, ex);
		}
	}

	@Override
	public void execution() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** execution**");
		java.util.Date beginDate = new java.util.Date();

		// Création de la table de stockage
		XMLHandlerCharger4 handler = new XMLHandlerCharger4(connexion, fileIdCard, this.tableTempA,
				this.tempTableAColumnsLongName, this.tempTableAColumnsShortName);

		// appel du parser et gestion d'erreur
		try {
			SAXParser saxParser = SecuredSaxParser.buildSecuredSaxParser();
			saxParser.parse(f, handler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			ArcException businessException = new ArcException(e, ArcExceptionMessage.XML_SAX_PARSING_FAILED,
					fileIdCard.getFileName()).logMessageException();
			rapport = businessException.getMessage().replace("'", "''");
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
