package fr.insee.arc.core.service.p2chargement.engine;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.bo.NormeFichier;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.p2chargement.bo.Norme;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.service.p2chargement.xmlhandler.XMLComplexeHandlerCharger;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.textUtils.FastList;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Pair;
import fr.insee.arc.utils.utils.SecuredSaxParser;





/**
 * Classe chargeant les fichiers Xml. Utiliser l'api SAX pour parser les fichiers
 * 
 * @author S4LWO8
 *
 */
public class ChargeurXmlComplexe implements IChargeur{
    private static final Logger LOGGER = LogManager.getLogger(ChargeurXmlComplexe.class);
    private String fileName;
    private Connection connexion;
    private String tableChargementPilTemp;
    private String tableChargementRegle;
    private String currentPhase;
    private Norme norme;
    private String validite;
    private InputStream f;    
    
    private ArrayList<Pair<String,String>> format; 

    // temporary table where data will be loaded by the XML SAX engine
    private String tableTempA;
    private FastList<String> tempTableAColumnsLongName=new FastList<>(Arrays.asList(ColumnEnum.ID_SOURCE.getColumnName(),"id","date_integration","id_norme","periodicite","validite"));
    private FastList<String> tempTableAColumnsShortName=new FastList<>(Arrays.asList("m0","m1","m2","m3","m4","m5"));
    private FastList<String> tempTableAColumnsType=new FastList<>(Arrays.asList("text collate \"C\"","int","text collate \"C\"","text collate \"C\"","text collate \"C\"","text collate \"C\""));

    private String rapport;
    private Boolean error = false;
    public String jointure;
    
    public ChargeurXmlComplexe(ThreadChargementService threadChargementService, String fileName) {
        this.fileName = fileName;
        this.connexion = threadChargementService.getConnexion().getExecutorConnection();
        this.tableTempA = threadChargementService.getTableTempA();
        this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
        this.currentPhase = threadChargementService.getCurrentPhase();
        this.f =  threadChargementService.filesInputStreamLoad.getTmpInxChargement();
        this.norme = threadChargementService.normeOk;
        this.validite = threadChargementService.validite;
        this.tableChargementRegle=threadChargementService.getTableChargementRegle();
    }

    
    public ChargeurXmlComplexe(Connection connexion, String fileName, InputStream f, String tableOut, String norme, String periodicite, String validite, String tableRegle) {
    	this.fileName = fileName;
        this.connexion = connexion;
        this.tableTempA = tableOut;
        this.norme=new Norme(norme, periodicite, null, null);
        this.validite = validite;
        this.f=f;
        this.tableChargementRegle=tableRegle;
    }
    
    
    /**
     * Autonomous execution with parameters constructor
     * @throws ArcException 
     */
    public void executeEngine() throws ArcException {
    	initialisation();
    	execution();
    }
    

    @Override
    public void initialisation() {
        StaticLoggerDispatcher.info(LOGGER, "** requeteCreateA **");

        java.util.Date beginDate = new java.util.Date();

        NormeFichier normeFichier=new NormeFichier(this.norme.getIdNorme(), validite, this.norme.getPeriodicite());
        this.format=new ArrayList<>();
        
        // voir avec Pierre comment factoriser ce genre de truc
        try {
			HashMap<String,ArrayList<String>> regle = RulesOperations.getBean(this.connexion,RulesOperations.getRegles(tableChargementRegle, normeFichier));
			if (regle.get("format").get(0)!=null) {
				for (String rule:regle.get("format").get(0).split("\n"))
				{
					this.format.add(new Pair<>(rule.split(",")[0].trim(),rule.split(",")[1].trim()));
				}
			}
        } catch (ArcException e1) {
		}

        
        StringBuilder requete = new StringBuilder();
        requete.append(FormatSQL.dropTable(this.tableTempA));
        requete.append("CREATE ");

        if (!this.tableTempA.contains(".")) {
            requete.append("TEMPORARY ");
        } else {
            requete.append(" ");
        }

        // la table temporaire A : ids|id|d|data|nombre_colonnes
        // data : contiendra les données chargé au format text séparée par des virgules
        // nombre_colonnes : contiendra le nombre de colonne contenue dans data, nécessaire pour compléter la ligne avec des virgules

        requete.append(" TABLE " + this.tableTempA + " (");
        boolean noComma=true;
        for (int i=0;i<tempTableAColumnsLongName.size();i++)
        {
        	if (noComma)
        	{
        		noComma=false;
        	}
        	else
        	{
        		requete.append(",");
        	}
        	requete.append(tempTableAColumnsShortName.get(i)+" "+tempTableAColumnsType.get(i)+" ");
        }
     	requete.append(") ");
     	requete.append(FormatSQL.WITH_NO_VACUUM);
     	requete.append(";");

        try {
			UtilitaireDao.get(0).executeImmediate(this.connexion, requete);
		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER,"Error in ChargeurXML.initialisation()");
		}
        java.util.Date endDate = new java.util.Date();
        
        StaticLoggerDispatcher.info(LOGGER, "** requeteCreateA en " + (endDate.getTime() - beginDate.getTime()) + " ms **");

        
    }

    @Override
    public void finalisation() {
        StringBuilder requeteBilan = new StringBuilder();
        if (error) {
            requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName, this.currentPhase, TraitementEtat.KO.toString(),
                    rapport));
        } else {
            requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName, this.currentPhase, TraitementEtat.OK.toString(),
                    rapport, this.jointure));
        }

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
        XMLComplexeHandlerCharger handler = new XMLComplexeHandlerCharger();
        handler.fileName = fileName;
        handler.connexion = connexion;
        handler.tempTableA = this.tableTempA;
        handler.start = 0;
        handler.sizeLimit=0;
        handler.normeCourante = norme;
        handler.validite = validite;
        handler.tempTableAColumnsLongName=this.tempTableAColumnsLongName;
        handler.tempTableAColumnsShortName=this.tempTableAColumnsShortName;
        handler.format=this.format;

        // appel du parser et gestion d'erreur
        try {
            SAXParser saxParser = SecuredSaxParser.buildSecuredSaxParser();
            saxParser.parse(f, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            error = true;
            ArcException businessException = new ArcException(e, ArcExceptionMessage.XML_SAX_PARSING_FAILED, this.fileName).logMessageException();
            rapport = businessException.getMessage().replace("'", "''");
            throw businessException;
        }

        this.jointure=handler.jointure;
        
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
    
}
