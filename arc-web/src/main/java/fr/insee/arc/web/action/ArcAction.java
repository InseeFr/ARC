package fr.insee.arc.web.action;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.files.FileUtils;
import fr.insee.arc.utils.queryhandler.UtilitaireDAOIhmQueryHandler;
import fr.insee.arc.utils.queryhandler.UtilitaireDAOQueryHandler;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.ICharacterConstant;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.model.SessionParameters;
import fr.insee.arc.web.util.VObject;
import lombok.Getter;
import lombok.Setter;

/**
 * An abstract class that all the action class must extends. Contain general
 * method that all action class need ({@link ArcAction#initialize()} and
 * {@link ArcAction#generateDisplay()}), and specific one that all class have
 * to override
 * 
 * 
 * @author Pépin Rémi
 *
 */
@Getter
@Setter
public abstract class ArcAction extends Authentifier implements ICharacterConstant {
    private static final Logger LOGGER = Logger.getLogger(ArcAction.class);
    public static final String NONE = "none";
    public static final String POOLNAME = "arc";
    @Autowired
    @Qualifier("properties")
    public  PropertiesHandler properties;
    
    protected String repertoire;

    private List<String> listBas = Arrays.asList("BAS1", "BAS2", "BAS3", "BAS4", "BAS5", "BAS6", "BAS7", "BAS8",
	    "PROD");

    private Map<String, String> envMap ;

    /**
     *
     */
    public static final String MESSAGE_EMPTY = "";
    /**
     *
     */
    public static final String SUCCESS = "success";
    /**
     * The object htat will run the SQL query
     */
    @Autowired
    @Qualifier("queryHandler")
    private UtilitaireDAOIhmQueryHandler queryHandler;
    /**
     * Contain a map with the table name
     */
    protected BddTable bddTable;
    /**
     * Fil d'Ariane
     */
    private List<String> filUrl;
    /**
     * scope of the page, know which {@link VObject} is to display
     */
    private String scope;
    /**
     * Autorised profil list. Currently unused
     */
    public ArrayList<String> autorisedProfil = new ArrayList<>();
    /**
     * 
     */
    private Map<VObject, Consumer<? super VObject>> mapVObject = new HashMap<>();
    private List<VObject> listVObjectOrder = new ArrayList<>();

    protected String userId;

    /**
     * Language for IHM
     */
    public String lang;

    
    /**
     * State of the database
     */
    private boolean isDataBaseOK;

    private String bacASable;

    /**
     * In case of an action have to return an inputStream
     */
    protected InputStream inputStream;
    
    
    protected boolean isRefreshMonitoring = false;
    
    public void initializeArcActionWithProperties() {
	   this.envMap = Stream
		    .of(new String[][] { { properties.getSchemaReference() + "_BAS1", "BAS1" }, { properties.getSchemaReference() + "_BAS2", "BAS2" },
			    { properties.getSchemaReference() + "_BAS3", "BAS3" }, { properties.getSchemaReference() + "_BAS4", "BAS4" },
			    { properties.getSchemaReference() + "_BAS5", "BAS5" }, { properties.getSchemaReference() + "_BAS6", "BAS6" },
			    { properties.getSchemaReference() + "_BAS7", "BAS7" }, { properties.getSchemaReference() + "_BAS8", "BAS8" },
			    { properties.getSchemaReference() + "_PROD", "PROD" }, })
		    .collect(Collectors.toMap(data -> data[0], data -> data[1]));
	   
	  this.repertoire = properties.getRootDirectory();
    }

    public Consumer<? super VObject> putVObject(VObject vObject, Consumer<? super VObject> initialize) {
	this.listVObjectOrder.add(vObject);
	return this.mapVObject.put(vObject, initialize);
    }

    /**
     * Liste de tous les VObject sur lesquels des opérations standard seront
     * effectuées au chargement de la page.
     *
     */
    public abstract void putAllVObjects();

    /**
     * Instancie convenablement les DAOs.
     *
     */
    public abstract void instanciateAllDAOs();

    /**
     * TODO à voir pour mettre dans le setSession
     */
    public abstract void setProfilsAutorises();

    public static String convertLongToDate(Long l) {
	Date d = new Date(l);
	return new SimpleDateFormat(EDateFormat.YYYY_MM_DD_HH_MM_SS.getValue()).format(d);
    }

    public void initialize() {
	LoggerHelper.debug(LOGGER, String.join(" ** initialize() called by %s **",
		Thread.currentThread().getStackTrace()[2].getMethodName()));
	initializeArcActionWithProperties();
	getDataBaseStatus();
	setProfilsAutorises();
	grantAccess(this.autorisedProfil.toArray(new String[0]));
	recupererEnvironnementTravail();
	this.bddTable = new BddTable(getSession().get(SessionParameters.ENV).toString());
	this.bddTable.export(getSession());
	instanciateAllDAOs();
	specificTraitementsPostDAO();
	putAllVObjects();
	/*
	 * Clean VObject message
	 */
	for (Entry<VObject, Consumer<? super VObject>> entry : getMapVObject().entrySet()) {
	    entry.getKey().setMessage(MESSAGE_EMPTY);
	}
    }

    /**
     * 
     */
    public void recupererEnvironnementTravail() {

	// Update the env if a new one is provided
	if (this.bacASable != null) {
	    getSession().put(SessionParameters.ENV, this.bacASable);

	    this.isRefreshMonitoring = true;
	} else {
	    // if the env in session is null initialiaze it
		
		System.out.println(getSession());
		
	    if (getSession().get(SessionParameters.ENV) == null) {
		getSession().put(SessionParameters.ENV,
			properties.getSchemaReference() + "_BAS1");
	    }
	}
	setUser(this.userId);

	LoggerDispatcher.info(String.format("env selected %s", getSession().get(SessionParameters.ENV)), LOGGER);

    }

    public String getEnvironnementTravail() {
	return (String) getSession().get(SessionParameters.ENV);
    }

    /**
     * Get the database status
     * 
     * @return
     */
    public void getDataBaseStatus() {
	LoggerHelper.debug(LOGGER, "getDataBaseStatus()");
	// test the database connection
	try {
	    // we only want one try
	    queryHandler.setMaxRetry(1);
	    queryHandler.executeUpdate("select true", UtilitaireDAOQueryHandler.OnException.THROW);
	    queryHandler.restsetMaxRetry();
	    setIsDataBaseOK(true);

	} catch (Exception e) {
	    setIsDataBaseOK(false);
	}

    }

    /**
     * Run some traitements specific to eceah action class that need instanciated DAO
     */
    protected abstract void specificTraitementsPostDAO();

    /**
     * 
     * Get all the {@link VObject} in the {@link ArcAction#listVObjectOrder} and generated the needed one
     * 
     * @return 
     */
    public String generateDisplay() {
	LoggerHelper.debug(LOGGER, "getScope()", getScope());
	Boolean defaultWhenNoScope = true;
	for (VObject vObject : getListVObjectOrder()) {
	    LoggerHelper.debug(LOGGER, "entry.getKey()", vObject.getTable());
	    vObject.setActivation(getScope());

	    if (getScope() != null && vObject.getIsScoped()) {
		getMapVObject().get(vObject).accept(vObject);
		defaultWhenNoScope = false;
	    }
	}
	if (defaultWhenNoScope) {
	    for (VObject vObject : getListVObjectOrder()) {
		vObject.setIsActive(true);
		vObject.setIsScoped(true);
	    }
	    LoggerHelper.debug(LOGGER, "getListVObjectOrder().size() ", getListVObjectOrder().size());
	    for (VObject vObject : getListVObjectOrder()) {
		getMapVObject().get(vObject).accept(vObject);
	    }
	}
	return SUCCESS;
    }

    /**
     *
     * @param selection
     * @return true si une campagne est sélectionnée, false sinon
     */
    protected boolean checkSizeOfSelection(VObject aVObect, HashMap<String, ArrayList<String>> selection) {
	if (selection.size() > 0) {
	    LoggerHelper.debug(LOGGER, "La sélection est non vide.");
	    return true;
	}
	aVObect.setMessage("Aucune campagne sélectionnée. Sélectionnez une campagne.");
	LoggerHelper.debug(LOGGER, "La sélection est vide.");
	return false;
    }

    /**
     * Change le contenu d'une colonne de {@link #SIMPLE_DATE_FORMAT_IHM} vers
     * {@link #SIMPLE_DATE_FORMAT_SQL}.</br>
     * Attention, <code>colonne</code> peut être vide. TODO appelé la méthode
     * {@link #modifierContenuVObject(VObject, String, String)}
     * 
     * @param aVObject
     * @param colonne
     * @throws ParseException
     */
    protected static void modifierContenuDateVObject(VObject aVObject, String colonne) throws ParseException {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	String contenu = aVObject.mapInputFields().get(colonne).get(0);
	LoggerHelper.debug(LOGGER, "contenu de la colonne ", colonne, ": ", contenu, "test : ",
		StringUtils.isNotBlank(contenu));
	if (StringUtils.isNotBlank(contenu)) {
	    String date = new SimpleDateFormat(EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue())
		    .format(new SimpleDateFormat(EDateFormat.SIMPLE_DATE_FORMAT_IHM.getValue()).parse(contenu));
	    ((VObject) session.getAttribute(aVObject.getSessionName())).getDefaultInputFields().put(colonne, date);
	}
    }

    /**
     * Change le contenu d'un VOject pour </br>
     * - remettre le contenu dans le bon format de la base.</br>
     * - mettre une valeur qui ne vient pas l'IHM
     * 
     * @param aVObject
     * @param colonne
     * @param val
     */
    protected static void modifierContenuVObject(VObject aVObject, String colonne, String val) {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	LoggerHelper.debug(LOGGER, "mise de la valeur : ", val, "dans la colonne :", colonne);
	((VObject) session.getAttribute(aVObject.getSessionName())).getDefaultInputFields().put(colonne, val);
    }

    /**
     * Méthode pour l'export d'une table en format csv zippé
     * 
     * @param aNomFichier
     * @param aNomTableImage
     * @param aMessageErreur
     *            en cas d'échec
     * @param withTypes
     *            défini si on met les types de données dans le header
     * @throws IOException
     */
    public void downloadFichier(String aNomFichier, String aNomTableImage, String aMessageErreur, boolean withTypes)
	    throws IOException {
	HttpServletResponse response = ServletActionContext.getResponse();
	ZipOutputStream aZipOutputStream = null;
	try {
	    response.reset();
	    response.setHeader("Content-Disposition", "attachment; filename=" + aNomFichier + "_export" + ".tar.gz");
	    aZipOutputStream = new ZipOutputStream(response.getOutputStream());
	    ZipEntry entry = new ZipEntry(aNomFichier + FileUtils.EXTENSION_CSV);
	    aZipOutputStream.putNextEntry(entry);
	    /*
	     * Ecriture dans le fichier On écrit le header puis les types si @withTypes est
	     * à true ensuite le contenu
	     */
	    if (withTypes) {
		GenericBean gb = getQueryHandler().execute(UtilitaireDao.EntityProvider.getGenericBeanProvider(),
			FormatSQL.modeleDeDonneesTable(aNomTableImage).toString(),
			UtilitaireDAOQueryHandler.OnException.THROW);
		Map<String, ArrayList<String>> mapModeleDonnees = gb.mapContent();
		StringBuilder headers = new StringBuilder();
		headers.append(mapModeleDonnees.get("attname").stream().collect(Collectors.joining(";")) + "\n");
		headers.append(mapModeleDonnees.get("typname").stream().collect(Collectors.joining(";")) + "\n");
		byte[] bytes = headers.toString().getBytes(Charset.forName("UTF-8"));
		aZipOutputStream.write(bytes);
		UtilitaireDao.get(POOLNAME).exportingWithoutHeader(getQueryHandler().getWrapped(), aNomTableImage,
			aZipOutputStream, true, false);
	    } else {
		UtilitaireDao.get(POOLNAME).exporting(getQueryHandler().getWrapped(), aNomTableImage, aZipOutputStream,
			true, false);
	    }
	    aZipOutputStream.closeEntry();
	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, ex, aMessageErreur);
	} finally {
	    if (aZipOutputStream != null) {
		aZipOutputStream.close();
	    }
	    response.getOutputStream().flush();
	    response.getOutputStream().close();
	}
    }

    /**
     * Methode which only update the {@link VObject}. Quit dummy.
     * 
     * @return
     */
    protected String basicAction() {
	LoggerHelper.debug(LOGGER, String.join(" ** basicAction() called by %s **",
		Thread.currentThread().getStackTrace()[2].getMethodName()));
	initialize();
	return generateDisplay();
    }

    /**
     * Method to update a {@link VObject}. Change reccord in the database and the
     * gui
     * 
     * @param theVObjectToUpdate
     * @return
     */
    protected String updateVobject(VObject theVObjectToUpdate) {
	LoggerHelper.debug(LOGGER, String.join(" ** updateVobject() called by %s **",
		Thread.currentThread().getStackTrace()[2].getMethodName()));
	initialize();
	theVObjectToUpdate.update();
	return generateDisplay();
    }

    /**
     * Method to add a line in a {@link VObject}. Change reccord in the database and
     * the gui
     * 
     * @param theVObjectToUpdate
     *            theVObjectToUpdate
     * @return
     */
    protected String addLineVobject(VObject theVObjectToUpdate, AttributeValue... attributeValues) {
	LoggerHelper.debug(LOGGER, String.join(" ** addLineVobject() called by %s **",
		Thread.currentThread().getStackTrace()[2].getMethodName()));
	initialize();
	theVObjectToUpdate.insertWIP(attributeValues);
	return generateDisplay();
    }

    /**
     * Method to delete a line in a {@link VObject}. Change reccord in the database
     * and the gui
     * 
     * @param theVObjectToUpdate
     *            theVObjectToUpdate
     * @return
     */
    protected String deleteLineVobject(VObject theVObjectToUpdate) {
	LoggerHelper.debug(LOGGER, String.join(" ** deleteLineVobject() called by %s **",
		Thread.currentThread().getStackTrace()[2].getMethodName()));
	initialize();
        Map<String, ArrayList<String>> selection = theVObjectToUpdate.mapContentSelected();
        if (!selection.isEmpty()) {
            theVObjectToUpdate.delete();
        } else {
            theVObjectToUpdate.setMessage("Please select some lines to delete");
        }
	return generateDisplay();
    }

    /**
     * Method to sort lines in a {@link VObject}. Change reccord order in the gui
     * 
     * @param theVObjectToUpdate
     *            theVObjectToUpdate
     * @return
     */
    protected String sortVobject(VObject theVObjectToSort) {
	LoggerHelper.debug(LOGGER, String.join(" ** sortVobject() called by %s **",
		Thread.currentThread().getStackTrace()[2].getMethodName()));
	initialize();
	theVObjectToSort.sort();
	return generateDisplay();
    }

    /**
     *
     * @return Le nom de cette action, pour le fil d'Ariane
     */
    public abstract String getActionName();


    /**
     * renvoi si l'environnement est un de production
     * 
     * @return
     */
    public boolean isProd() {
	return BddTable.SCHEMA_ARC_PROD.equalsIgnoreCase(getEnvironnementTravail());
    }

    public boolean isPlateformeProd() {
	return properties.getIsProd();
    }

    public String getVersion() {
	return properties.getVersion();
    }

    public String getApplication() {
	return properties.getApplication();
    }



    public boolean getIsDataBaseOK() {
	return isDataBaseOK;
    }

    public void setIsDataBaseOK(boolean isDataBaseOK) {
	this.isDataBaseOK = isDataBaseOK;
    }


   public List<String> getFilUrl() {
	return this.filUrl;
   }

   public void setFilUrl(List<String> filUrl) {
	this.filUrl = filUrl;
   }



   /**
    * @return the bddTable
    */
   public final BddTable getBddTable() {
	return this.bddTable;
   }

   /**
    * @param bddTable
    *            the bddTable to set
    */
   public final void setBddTable(BddTable bddTable) {
	this.bddTable = bddTable;
   }

   /**
    * @return the scope
    */
   public final String getScope() {
	return this.scope;
   }

   /**
    * @param scope
    *            the scope to set
    */
   public final void setScope(String scope) {
	this.scope = scope;
   }

   /**
    * @return the mapVObject
    */
   private final Map<VObject, Consumer<? super VObject>> getMapVObject() {
	return this.mapVObject;
   }

   /**
    * @param mapVObject
    *            the mapVObject to set
    */
   private final void setMapVObject(Map<VObject, Consumer<? super VObject>> mapVObject) {
	this.mapVObject = mapVObject;
   }

   public List<VObject> getListVObjectOrder() {
	return this.listVObjectOrder;
   }

   public void setListVObjectOrder(List<VObject> listVObjectOrder) {
	this.listVObjectOrder = listVObjectOrder;
   }

   /**
    * @return the queryHandler
    */
   public final UtilitaireDAOIhmQueryHandler getQueryHandler() {
	return this.queryHandler;
   }

   /**
    * @param queryHandler
    *            the queryHandler to set
    */
   public final void setQueryHandler(UtilitaireDAOIhmQueryHandler queryHandler) {
	this.queryHandler = queryHandler;
   }

   private final String getIdep() {
	return this.userId;
   }

   public String getBacASable() {
	return bacASable;
   }

   public void setBacASable(String bacASable) {
	this.bacASable = bacASable;
   }

   public List<String> getListBas() {
	return listBas;
   }

   public void setListBas(List<String> listBas) {
	this.listBas = listBas;
   }

   public InputStream getInputStream() {
	return inputStream;
   }

   public void setInputStream(InputStream inputStream) {
	this.inputStream = inputStream;
   }

   public Map<String, String> getEnvMap() {
	return envMap;
   }

	public String getLang() {
		return properties.getLang();
	}


}
