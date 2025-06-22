package fr.insee.arc.web.gui.all.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.Oauth2ClientForKeycloak;
import fr.insee.arc.web.gui.all.dao.ArcWebGenericDao;
import fr.insee.arc.web.gui.all.dao.IDao;
import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.home.HomeAction;
import fr.insee.arc.web.gui.index.service.IndexAction;

/**
 * An abstract class that all the controllers using VObject should extend. 
 * Contains general methods called automatically before the controller (({@link ArcWebGenericService#initialize()}) or
 * manually at the end ({@link ArcWebGenericService#generateDisplay()}).
 * 
 * 
 * @author Pépin Rémi
 *
 */
public abstract class ArcWebGenericService<T extends ArcModel, D extends IDao> implements IConstanteCaractere {

	private static final Logger LOGGER = LogManager.getLogger(ArcWebGenericService.class);
	
	protected static final String POOLNAME = "arc"; 

	@Autowired
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
	private ArcWebGenericDao arcWebGenericDao;

	private Map<String, String> envMap;
	
	@Autowired
	protected DataObjectService dataObjectService;
	
	@Autowired
	protected D dao;

	/**
	 * Contains a map with the table names
	 */

	/**
	 * The scope of the page defines the {@link VObject} to display
	 */
	private String scope;

	/** Map linking the VObject and their initialization method.*/
	private Map<VObject, Consumer<VObject>> mapVObject = new HashMap<>();

	/** List listing the VObject in the order they should be initialized. */
	private List<VObject> listVObjectOrder = new ArrayList<>();

	/** State of the database */
	private boolean isDataBaseOK;

	/** Selected environment.*/
	private String bacASable;

	/** Is the current environment a production environment?*/
	private boolean isEnvProd;
	/** Is the current environment must be executed as a batch ? What is the batch mode ? */
	private String isEnvBatch;
	
	/** Are the Kubernetes options enabled?*/
	private boolean isKube;

	protected boolean isRefreshMonitoring = false;
	
	/** user idep **/
	private String userName;

	/**
	 * Completes the received request parameters with the information 
	 * that has been persisted in the session if available. 
	 * Defines them as class attribute for convenience.
	 * Associates each VObject with its initialization method in {@link mapVObject}.
	 * 
	 * <br/> Example :  
	 * <pre>public void putAllVObjects(ClientModel arcModel) {
	 * 	setViewClient(vObjectService.preInitialize(arcModel.getViewClient()));
	 * 	putVObject(getViewClient(), t -> initializeClient());
	 * }</pre>
	 *
	 */
	protected abstract void putAllVObjects(T arcModel);

	/**
	 * @return the name of the current controller, mostly for logging
	 */
	protected abstract String getActionName();

	/** Runs the generic initialization (status, scope, VObject,...) 
	 * on all requests in ArcAction or ArcAction subclasses. 
	 * Adds some generic information to the model.
	 * VObject themselves are added to the model later by {@link ArcWebGenericService#generateDisplay()}.
	 * @throws ArcException */
	@ModelAttribute
    public void initializeModel(@ModelAttribute T arcModel, Model model,
    		@RequestParam(required = false) String bacASable,
			@RequestParam(required = false) String scope, Authentication auth) throws ArcException {
		LoggerHelper.trace(LOGGER, getActionName());

		// no action required for unsecured page
		if(getActionName().equals(HomeAction.ACTION_NAME))
		{
			return;
		}
		
		// initialize the database on secure page index
		if (getActionName().equals(IndexAction.ACTION_NAME))
		{
	    	// run the initialization script
			new BddPatcher().bddScript(null);
		}
		
		
		// get declared sandboxes
		this.envMap= arcWebGenericDao.getSandboxList();
		List<String> keys=new ArrayList<>(this.envMap.keySet());

		if (this.bacASable == null) {
			// by default bacASable is the first element of the linkedhashmap
			if (!keys.isEmpty())
			{
				this.bacASable = keys.get(0);
			}
		}

		// updating current sandbox from request
		if (bacASable != null && !bacASable.equals(this.bacASable)) {
			loggerDispatcher.info(String.format("env selected %s", bacASable), LOGGER);
			this.bacASable = bacASable;
		}
		
		// security : this.bacASable value must be in environment list retrieved from database
		if (!keys.contains(this.bacASable))
		{
			this.bacASable="arc_bas1";	
		}
		
		this.dataObjectService.setSandboxSchema(this.bacASable);
		
		Sandbox s = new Sandbox(null, this.bacASable);
		
		this.isEnvProd = s.isEnvSetForProduction();
		this.isEnvBatch = s.computeBatchMode();

		this.isKube = PropertiesHandler.getInstance().getKubernetesExecutorNumber() > 0;
		
		dao.initialize(vObjectService, dataObjectService);
		
		this.scope = scope;
    	initialize(arcModel);
    	refreshGenericModelAttributes(model);
    	extraModelAttributes(model);

    	// retrieve userName and remote ip adress to log
    	trackUserInformations(auth);
    	
    }

	

	/**
	 * used to log a non standard VObjectServiceAction
	 * @param data
	 */
	public void trackThisAction() {
		LoggerHelper.action(LOGGER, this.userName + " : "+Thread.currentThread().getStackTrace()[2].getMethodName());
	}
	
	public void trackThisAction(String extraInfo) {
		LoggerHelper.action(LOGGER, this.userName + " : "+Thread.currentThread().getStackTrace()[2].getMethodName()+ " on " + extraInfo );
	}
	
	
	/**
	 * register user informations (name and ip adress)
	 * @param auth
	 */
	private void trackUserInformations(Authentication token) {
		
		if (token == null)
		{
	    	vObjectService.setUserName("anonymous");
	    	return;
		}
		
		OAuth2AuthenticationToken oauth2 = (OAuth2AuthenticationToken) token;
		this.userName = oauth2.getPrincipal().getAttribute(Oauth2ClientForKeycloak.CLAIM_USERNAME);
        WebAuthenticationDetails webDetails = (WebAuthenticationDetails) token.getDetails();
        this.userName+="@"+webDetails.getRemoteAddress();
    	
        vObjectService.setUserName(userName);
	}

	/**
	 * Fills the model with some attributes expected on (almost) all pages
	 * @param model
	 */
	private void refreshGenericModelAttributes(Model model) {
		model.addAttribute("envMap", getEnvMap());
    	model.addAttribute("bacASable", getBacASable());
    	model.addAttribute("isDataBaseOK", isDataBaseOk());
    	model.addAttribute("version", getVersion());
    	model.addAttribute("isEnvProd", isEnvProd());
    	model.addAttribute("application", getApplication());
    	model.addAttribute("isKube", isKube());
	}
	
	/** Adds (if overridden) more attributes to the model.*/
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

	private void initializeArcActionWithProperties() {	 
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
			
			arcWebGenericDao.execQueryTestDatabaseConnection();
			
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
	 * Get all the {@link VObjectService} in the {@link ArcWebGenericService#listVObjectOrder} and generated the needed one
	 * @param resultSuccess 
	 * 
	 * @return
	 */
	public String generateDisplay(Model model, String successUri) {
		LoggerHelper.debug(LOGGER, "generateDisplay()", getScope());
		// Initialize required VObjects according to scope
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
	 * Finishes the request treatment by refreshing the {@link VObject} and returns the uri.
	 * @param model the model that will be refreshed
	 * @param uri URI to the JSP
	 * @return uri
	 */
	protected String basicAction(Model model, String uri) {
		LoggerHelper.debug(LOGGER, String.join(" ** basicAction() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		return generateDisplay(model, uri);
	}

	/**
	 * Updates a {@link VObject} in database, refreshes the info and finishes the request.
	 * @param model 
	 * @param successUri URI to the JSP
	 * @param theVObjectToUpdate	 * 
	 * @return 
	 */
	protected String updateVobject(Model model, String successUri, VObject theVObjectToUpdate) {
		LoggerHelper.debug(LOGGER, String.join(" ** updateVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		vObjectService.update(theVObjectToUpdate);
		return generateDisplay(model, successUri);
	}

	/**
	 * Adds a line in a {@link VObject} in database, refreshes the info and finishes the request.
	 * @param model 
	 * @param successUri URI to the JSP	 * 
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
	 * Deletes a line in a {@link VObject} in database, refreshes the info and finishes the request.
	 * @param model 
	 * @param successUri URI to the JSP	
	 * @param theVObjectToUpdate
	 *            theVObjectToUpdate
	 * 
	 * @return
	 */
	protected String deleteLineVobject(Model model, String successUri, VObject theVObjectToUpdate) {
		LoggerHelper.debug(LOGGER, String.join(" ** deleteLineVobject() called by %s **",
				Thread.currentThread().getStackTrace()[2].getMethodName()));
		Map<String, List<String>> selection = theVObjectToUpdate.mapContentSelected();
		if (!selection.isEmpty()) {
			vObjectService.delete(theVObjectToUpdate);
		} else {
			theVObjectToUpdate.setMessage("vObject.delete.noSelection");
		}
		return generateDisplay(model, successUri);
	}

	/**
	 * Sorts a {@link VObject} for display and finishes the request.
	 * @param model 
	 * @param successUri URI to the JSP	
	 * @param theVObjectToSort
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
	
	public String getVersion() {
		return properties.lightVersionInformation().toString();
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

	public String isEnvBatch() {
		return isEnvBatch;
	}

	public boolean isDataBaseOk() {
		return isDataBaseOK;
	}

	public void setDataBaseOk(boolean isDataBaseOK) {
		this.isDataBaseOK = isDataBaseOK;
	}
	
	public boolean isKube() {
		return isKube;
	}

	public void setKube(boolean isKube) {
		this.isKube = isKube;
	}

	protected Session getSession() {
		return session;
	}

}