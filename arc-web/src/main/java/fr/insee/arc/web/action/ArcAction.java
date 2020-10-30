package fr.insee.arc.web.action;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.files.FileUtils;
import fr.insee.arc.utils.queryhandler.UtilitaireDAOIhmQueryHandler;
import fr.insee.arc.utils.queryhandler.UtilitaireDAOQueryHandler;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.model.ArcModel;
import fr.insee.arc.web.model.SessionParameters;
import fr.insee.arc.web.util.Session;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

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
public abstract class ArcAction<T extends ArcModel> implements IConstanteCaractere {

	private static final Logger LOGGER = LogManager.getLogger(ArcAction.class);

	protected static final String NONE = "none";
	protected static final String POOLNAME = "arc"; 
	protected static final int NUMBER_OF_SANDBOXES = 8;

	@Autowired
	@Qualifier("properties")
	protected PropertiesHandler properties;

	@Autowired
	private Session session;

	@Autowired
	@Qualifier("defaultVObjectService")
	protected VObjectService vObjectService;

	@Autowired
    @Qualifier("activeLoggerDispatcher")
    protected LoggerDispatcher loggerDispatcher;

	protected String repertoire;

	private Map<String, String> envMap;


	/**
	 * The object htat will run the SQL query
	 */
	@Autowired
	@Qualifier("queryHandler")
	private UtilitaireDAOIhmQueryHandler queryHandler;

	/**
	 * Contain a map with the table names
	 */
	protected BddTable bddTable;

	/**
	 * scope of the page, know which {@link VObjectService} is to display
	 */
	private String scope;

	private Map<VObject, Consumer<? super VObject>> mapVObject = new HashMap<>();
	private List<VObject> listVObjectOrder = new ArrayList<>();

	/** State of the database */
	private boolean isDataBaseOK;

	/** Selected environment.*/
	private String bacASable;

	/** Is the current environment a production environment?*/
	private boolean isEnvProd;

	protected boolean isRefreshMonitoring = false;

    /**
	 * Liste de tous les VObject sur lesquels des opérations standard seront
	 * effectuées au chargement de la page.
	 *
	 */
	protected abstract void putAllVObjects(T model);

	/**
	 * @return the name of the current controller
	 */
	protected abstract String getActionName();

	/** Run the generic initialization (status, VObject, ...) and add the relevant objects to the model.*/
	@ModelAttribute
    public void initializeModel(@ModelAttribute T arcModel, Model model,
    		@RequestParam(required = false) String bacASable,
			@RequestParam(required = false) String scope) {
		LoggerHelper.trace(LOGGER, getActionName());
		if (this.bacASable == null) {
			this.bacASable = properties.getSchemaReference() + "_BAS1";
		}
		if (bacASable != null && !bacASable.equals(this.bacASable)) {
			loggerDispatcher.info(String.format("env selected %s", bacASable), LOGGER);
			this.bacASable = bacASable;
		}
		this.isEnvProd = checkEnv(this.bacASable);
		this.bddTable = new BddTable(this.bacASable);
		this.bddTable.export(getSession().asMap());
		this.scope = scope;
		
    	initialize(arcModel, model);
    	refreshGenericModelAttributes(model);
    }

	protected void refreshGenericModelAttributes(Model model) {
		model.addAttribute("envMap", getEnvMap());
    	model.addAttribute("bacASable", getBacASable());
    	model.addAttribute("isDataBaseOK", isDataBaseOk());
    	model.addAttribute("version", getVersion());
    	model.addAttribute("isEnvProd", isEnvProd());
    	model.addAttribute("application", getApplication());
	}

	private void initialize(T arcModel, Model model) {
		LoggerHelper.debug(LOGGER, String.join(" ** initialize() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		listVObjectOrder = new ArrayList<>();
		mapVObject = new HashMap<>();
		initializeArcActionWithProperties();
		getDataBaseStatus();
		putAllVObjects(arcModel);
	}

	@SuppressWarnings("unchecked")
	private void initializeArcActionWithProperties() {	    
		if (getSession().get(SessionParameters.ENV_MAP) == null) {
			this.envMap = new LinkedHashMap<String, String>();
			for (int i = 1; i <= NUMBER_OF_SANDBOXES; i++) {
				this.envMap.put(properties.getSchemaReference() + "_BAS" + i, "BAS" + i);
			}
			this.envMap.put(properties.getSchemaReference() + "_PROD", "PROD");
			getSession().put(SessionParameters.ENV_MAP, this.envMap);
		}

		this.envMap=(Map<String, String>) getSession().get(SessionParameters.ENV_MAP);
		this.repertoire = properties.getBatchParametersDirectory();
	}

	/**
	 * Get the database status
	 * 
	 * @return
	 */
	protected boolean getDataBaseStatus() {
		LoggerHelper.debug(LOGGER, "getDataBaseStatus()");
		// test the database connection
		try {
			// we only want one try
			queryHandler.setMaxRetry(1);
			queryHandler.executeUpdate("select true", UtilitaireDAOQueryHandler.OnException.THROW);
			queryHandler.restsetMaxRetry();
			setDataBaseOk(true);
	
		} catch (Exception e) {
			setDataBaseOk(false);
		}
		return isDataBaseOk();
	
	}

	protected Consumer<? super VObject> putVObject(VObject vObject, Consumer<? super VObject> initialize) {
		this.listVObjectOrder.add(vObject);
		return this.mapVObject.put(vObject, initialize);
	}

	//TODO: move somewhere else, if used
	public static String convertLongToDate(Long l) {
		Date d = new Date(l);
		return new SimpleDateFormat(EDateFormat.YYYY_MM_DD_HH_MM_SS.getValue()).format(d);
	}

	/**
	 * 
	 * Get all the {@link VObjectService} in the {@link ArcAction#listVObjectOrder} and generated the needed one
	 * @param resultSuccess 
	 * 
	 * @return
	 */
	public String generateDisplay(String successUri) {
		LoggerHelper.debug(LOGGER, "getScope()", getScope());
		Boolean defaultWhenNoScope = true;
		for (VObject vObject : getListVObjectOrder()) {
			LoggerHelper.debug(LOGGER, "entry.getKey()", vObject.getTable());
			vObjectService.setActivation(vObject, getScope());

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
		return successUri;
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
	 * {@link #modifierContenuVObject(VObjectService, String, String)}
	 * 
	 * @param aVObject
	 * @param colonne
	 * @throws ParseException
	 */
	protected void modifierContenuDateVObject(HttpSession session, VObject aVObject, String colonne) throws ParseException {
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
	protected static void modifierContenuVObject(HttpSession session, VObject aVObject, String colonne, String val) {
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
	public void downloadFichier(HttpServletResponse response, String aNomFichier, String aNomTableImage, String aMessageErreur, boolean withTypes)
			throws IOException {
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
	 * Methode which only update the {@link VObjectService}. Quit dummy.
	 * 
	 * @return
	 */
	protected String basicAction(String successUri) {
		LoggerHelper.debug(LOGGER, String.join(" ** basicAction() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		return generateDisplay(successUri);
	}

	/**
	 * Method to update a {@link VObjectService}. Change record in the database and the
	 * gui
	 * @param theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String updateVobject(String successUri, VObject theVObjectToUpdate) {
		LoggerHelper.debug(LOGGER, String.join(" ** updateVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.update(theVObjectToUpdate);
		return generateDisplay(successUri);
	}

	/**
	 * Method to add a line in a {@link VObjectService}. Change record in the database and
	 * the gui
	 * 
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * @return
	 */
	protected String addLineVobject(String successUri, VObject theVObjectToUpdate, AttributeValue... attributeValues) {
		LoggerHelper.debug(LOGGER, String.join(" ** addLineVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.insert(theVObjectToUpdate, attributeValues);
		return generateDisplay(successUri);
	}

	/**
	 * Method to delete a line in a {@link VObjectService}. Change reccord in the database
	 * and the gui
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String deleteLineVobject(String successUri, VObject theVObjectToUpdate) {
		LoggerHelper.debug(LOGGER, String.join(" ** deleteLineVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		Map<String, ArrayList<String>> selection = theVObjectToUpdate.mapContentSelected();
		if (!selection.isEmpty()) {
			vObjectService.delete(theVObjectToUpdate);
		} else {
			theVObjectToUpdate.setMessage("Please select some lines to delete");
		}
		return generateDisplay(successUri);
	}

	/**
	 * Method to sort lines in a {@link VObjectService}. Change reccord order in the gui
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String sortVobject(String successUri, VObject theVObjectToSort) {
		LoggerHelper.debug(LOGGER, String.join(" ** sortVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.sort(theVObjectToSort);
		return generateDisplay(successUri);
	}

	/**
	 * renvoi si l'environnement est un de production
	 * 
	 * @return
	 */
	public boolean isProd() {
		return BddTable.SCHEMA_ARC_PROD.equalsIgnoreCase(getBacASable());
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
	public final Map<VObject, Consumer<? super VObject>> getMapVObject() {
		return this.mapVObject;
	}

	/**
	 * @param mapVObject
	 *            the mapVObject to set
	 */

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

	/** Return true if the environment is a production environment.*/
	private boolean checkEnv(String env) {
		// always false for now
		return false;
	}
	
	public String getVersion() {
		return properties.getVersion();
	}

	public String getApplication() {
		return properties.getApplication();
	}

	public Map<String, String> getEnvMap() {
		return envMap;
	}

	public String getBacASable() {
		return bacASable;
	}
	
	public void setBacASable(String bacASable) {
		this.bacASable = bacASable;
	}

	public boolean isEnvProd() {
		return isEnvProd;
	}
	
	public boolean isDataBaseOk() {
		return isDataBaseOK;
	}

	public void setDataBaseOk(boolean isDataBaseOK) {
		this.isDataBaseOK = isDataBaseOK;
	}

	protected Session getSession() {
		return session;
	}
}