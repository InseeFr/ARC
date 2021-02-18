package fr.insee.arc.web.action;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.util.BDParameters;
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
import fr.insee.arc.web.dao.IndexDao;
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

	private static final String DEFAULT_PRODUCTION_ENVIRONMENTS="[\"arc_prod\"]";
	
	protected static final String NONE = "none";
	protected static final String POOLNAME = "arc"; 

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
	
	@Autowired
	private IndexDao indexDao;

	protected Map<String, String> envMap;


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

	private Map<VObject, Consumer<VObject>> mapVObject = new HashMap<>();
	private List<VObject> listVObjectOrder = new ArrayList<>();

	/** State of the database */
	private boolean isDataBaseOK;

	/** Selected environment.*/
	protected String bacASable;

	/** Is the current environment a production environment?*/
	private boolean isEnvProd;

	protected boolean isRefreshMonitoring = false;

    /**
	 * Liste de tous les VObject sur lesquels des opérations standard seront
	 * effectuées au chargement de la page.
	 *
	 */
	protected abstract void putAllVObjects(T arcModel);

	/**
	 * @return the name of the current controller
	 */
	protected abstract String getActionName();

	/** Runs the generic initialization (status, VObject, ...) 
	 * and adds some generic info to the model.
	 * (VObject themselves are added to the model by ArcInterceptor)*/
	@ModelAttribute
    public void initializeModel(@ModelAttribute T arcModel, Model model,
    		@RequestParam(required = false) String bacASable,
			@RequestParam(required = false) String scope) {
		LoggerHelper.trace(LOGGER, getActionName());
		
		if (getActionName().equals(IndexAction.ACTION_NAME))
		{
	    	// run the initialization script
			ApiInitialisationService.bddScript(null);
		}

		// adding production sandbox to session
		if (this.envMap == null) {
			this.envMap= indexDao.getSandboxList();
			getSession().put(SessionParameters.ENV_MAP, this.envMap);
		}
		if (this.bacASable == null) {
			// by default bacASable is the first element of the linkedhashmap
			List<String> keys=new ArrayList<>(((LinkedHashMap<String,String>) this.envMap).keySet());
			this.bacASable = keys.get(0);
		}

		// updating current sandbox from request
		if (bacASable != null && !bacASable.equals(this.bacASable)) {
			loggerDispatcher.info(String.format("env selected %s", bacASable), LOGGER);
			this.bacASable = bacASable;
		}
		this.isEnvProd = checkEnv(this.bacASable);
		this.bddTable = new BddTable(this.bacASable);
		this.bddTable.export(getSession().asMap());
		this.scope = scope;
		
    	initialize(arcModel);
    	refreshGenericModelAttributes(model);
    	extraModelAttributes(model);
    }

	protected void refreshGenericModelAttributes(Model model) {
		model.addAttribute("envMap", getEnvMap());
    	model.addAttribute("bacASable", getBacASable());
    	model.addAttribute("isDataBaseOK", isDataBaseOk());
    	model.addAttribute("version", getVersion());
    	model.addAttribute("isEnvProd", isEnvProd());
    	model.addAttribute("application", getApplication());
    	model.addAttribute("userManagementActive", properties.isLdapActive());
	}
	
	/** Adds more controller-specific attributes the model.*/
	public void extraModelAttributes(Model model) {
		// nothing by default
	}

	private void initialize(T arcModel) {
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
		this.envMap=(LinkedHashMap<String, String>) getSession().get(SessionParameters.ENV_MAP);
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
			queryHandler.resetMaxRetry();
			setDataBaseOk(true);
	
		} catch (Exception e) {
			setDataBaseOk(false);
		}
		return isDataBaseOk();
	
	}

	protected Consumer<VObject> putVObject(VObject vObject, Consumer<VObject> initialize) {
		this.listVObjectOrder.add(vObject);
		return this.mapVObject.put(vObject, initialize);
	}

	/**
	 * 
	 * Get all the {@link VObjectService} in the {@link ArcAction#listVObjectOrder} and generated the needed one
	 * @param resultSuccess 
	 * 
	 * @return
	 */
	public String generateDisplay(Model model, String successUri) {
		LoggerHelper.debug(LOGGER, "generateDisplay()", getScope());
		// Initialize required VObjects
		boolean defaultWhenNoScope = true;
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

		// Stores VObject in model
		for (VObject vObject : getMapVObject().keySet()) {
			if (vObject != null) {
				model.addAttribute(vObject.getSessionName(), vObject);
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
						FormatSQL.modeleDeDonneesTable(aNomTableImage),
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
	protected String basicAction(Model model, String successUri) {
		LoggerHelper.debug(LOGGER, String.join(" ** basicAction() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		return generateDisplay(model, successUri);
	}

	/**
	 * Method to update a {@link VObjectService}. Change record in the database and the
	 * gui
	 * @param theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String updateVobject(Model model, String successUri, VObject theVObjectToUpdate) {
		LoggerHelper.debug(LOGGER, String.join(" ** updateVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.update(theVObjectToUpdate);
		return generateDisplay(model, successUri);
	}

	/**
	 * Method to add a line in a {@link VObjectService}. Change record in the database and
	 * the gui
	 * 
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * @return
	 */
	protected String addLineVobject(Model model, String successUri, VObject theVObjectToUpdate, AttributeValue... attributeValues) {
		LoggerHelper.debug(LOGGER, String.join(" ** addLineVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.insert(theVObjectToUpdate, attributeValues);
		return generateDisplay(model, successUri);
	}

	/**
	 * Method to delete a line in a {@link VObjectService}. Change reccord in the database
	 * and the gui
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String deleteLineVobject(Model model, String successUri, VObject theVObjectToUpdate) {
		LoggerHelper.debug(LOGGER, String.join(" ** deleteLineVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		Map<String, ArrayList<String>> selection = theVObjectToUpdate.mapContentSelected();
		if (!selection.isEmpty()) {
			vObjectService.delete(theVObjectToUpdate);
		} else {
			theVObjectToUpdate.setMessage("Please select some lines to delete");
		}
		return generateDisplay(model, successUri);
	}

	/**
	 * Method to sort lines in a {@link VObjectService}. Change reccord order in the gui
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String sortVobject(Model model, String successUri, VObject theVObjectToSort) {
		LoggerHelper.debug(LOGGER, String.join(" ** sortVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.sort(theVObjectToSort);
		return generateDisplay(model, successUri);
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
	public final Map<VObject, Consumer<VObject>> getMapVObject() {
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
		JSONArray j=new JSONArray(BDParameters.getString(null, "ArcAction.productionEnvironments",DEFAULT_PRODUCTION_ENVIRONMENTS));
		Set<String> found=new HashSet<>();
		
		j.forEach(item -> {
            if (item.toString().equals(env))
            {
            	found.add(item.toString());
            }
        });
		return !found.isEmpty();
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