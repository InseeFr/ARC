package fr.insee.arc.core.service.engine.chargeur;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import fr.insee.arc.core.databaseobjetcs.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.handler.XMLHandlerCharger4;
import fr.insee.arc.core.service.thread.ThreadChargementService;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

/**
 * Classe chargeant les fichiers Xml. Utiliser l'api SAX pour parser les fichiers
 * 
 * @author S4LWO8
 *
 */
public class ChargeurXml implements IChargeur{
    private static final Logger LOGGER = LogManager.getLogger(ChargeurXml.class);
    private String fileName;
    private HashMap<String, Integer> col;
    private Connection connexion;
    private HashMap<String, Integer> colData;
    private List<String> allCols;
    private StringBuilder requeteInsert;
    private String tableChargementPilTemp;
    private String currentPhase;
    private Norme norme;
    private String validite;
    private InputStream f;    

    // temporary table where data will be loaded by the XML SAX engine
    private String tableTempA = "A";
    private ArrayList<String> tempTableAColumnsLongName=new ArrayList<>(Arrays.asList(ColumnEnum.ID_SOURCE.getColumnName(),"id","date_integration","id_norme","periodicite","validite"));
    private ArrayList<String> tempTableAColumnsShortName=new ArrayList<>(Arrays.asList("m0","m1","m2","m3","m4","m5"));
    private ArrayList<String> tempTableAColumnsType=new ArrayList<>(Arrays.asList(ColumnEnum.ID_SOURCE.getColumnType().getTypeCollated(),"int","text collate \"C\"","text collate \"C\"","text collate \"C\"","text collate \"C\""));

    private String rapport = null;
    private Boolean error = false;
    private String jointure;
    
    public ChargeurXml(ThreadChargementService threadChargementService, String fileName) {
        this.fileName = fileName;
        this.col = threadChargementService.getCol();
        this.connexion = threadChargementService.getConnexion();
        this.colData = threadChargementService.getColData();
        this.allCols = threadChargementService.getAllCols();
        this.requeteInsert = threadChargementService.getRequeteInsert();
        this.tableTempA = threadChargementService.getTableTempA();
        this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
        this.currentPhase = threadChargementService.getCurrentPhase();
        this.f =  threadChargementService.filesInputStreamLoad.getTmpInxChargement();
        this.norme = threadChargementService.normeOk;
        this.validite = threadChargementService.validite;
    }

    
    public ChargeurXml(Connection connexion, String fileName, InputStream f, String tableOut, String norme, String periodicite, String validite) {
    	
    	this.col = new HashMap<>();
        this.allCols= new ArrayList<>();
        this.colData= new HashMap<>();
        this.requeteInsert=new StringBuilder();

        this.fileName = fileName;
        this.connexion = connexion;
        this.tableTempA = tableOut;
        this.norme=new Norme(norme, periodicite, null, null);
        this.validite = validite;
        this.f=f;
  }
    
    /**
     * Autonomous execution with parameters constructor
     * @throws Exception 
     */
    public void executeEngine() throws Exception {
    	initialisation();
    	execution();
    }

    @Override
    public void initialisation() {
        StaticLoggerDispatcher.info("** requeteCreateA **", LOGGER);

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
			UtilitaireDao.get("arc").executeBlock(this.connexion, requete);
		} catch (SQLException e) {
		    LoggerHelper.errorAsComment(LOGGER, "ChargeurXML.initialisation - creation failed on the temporary table A which is the temporary recipient for the xml file to be loaded");
		}
        java.util.Date endDate = new java.util.Date();
        
        StaticLoggerDispatcher.info("** requeteCreateA en " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);

        
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
            UtilitaireDao.get("arc").executeBlock(this.connexion, requeteBilan);
        } catch (SQLException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "chargerXml()", LOGGER, ex);
        }
    }

    @Override
    public void execution() throws Exception {
        StaticLoggerDispatcher.info("** execution**", LOGGER);
        java.util.Date beginDate = new java.util.Date();

        // java.util.Date date= new java.util.Date();
        for (String key : col.keySet()) {
            col.put(key, 1);
        }
        
        // Création de la table de stockage
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        XMLHandlerCharger4 handler = new XMLHandlerCharger4();
        // handler.tableSchema=tableSchema;
        // handler.tableName=tableName;
        handler.fileName = fileName;
        handler.connexion = connexion;
        handler.col = col;
        handler.colData = colData;
        handler.allCols = allCols;
        handler.requete = requeteInsert;
        handler.tempTableA = this.tableTempA;
        handler.start = 0;
        handler.sizeLimit=0;
        handler.normeCourante = norme;
        handler.validite = validite;
        handler.tempTableAColumnsLongName=this.tempTableAColumnsLongName;
        handler.tempTableAColumnsShortName=this.tempTableAColumnsShortName;

        // appel du parser et gestion d'erreur
        try {
            /*
             * On desactive les les doctypes externe, ainsi que les external-parameter-entity et external-general-entity
             * -> Protection contre attaque XXE (Sécurité)
             */
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            SAXParser saxParser = saxParserFactory.newSAXParser();

            saxParser.parse(f, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            error = true;
            LoggerHelper.errorAsComment(LOGGER, "ChargeurXml.execution() - SAX parser failed to parse the xml file");
            rapport = e.getMessage().replace("'", "''");
            handler.requete.setLength(0);
            throw e;
        }

        this.jointure=handler.jointure;
        
        java.util.Date endDate = new java.util.Date();
        StaticLoggerDispatcher.info("** excecution temps" + (endDate.getTime() - beginDate.getTime()) + " ms", LOGGER);

        

    }

    @Override
    public void charger() throws Exception {
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
