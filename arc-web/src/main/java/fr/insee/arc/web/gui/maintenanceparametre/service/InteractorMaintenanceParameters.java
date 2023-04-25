package fr.insee.arc.web.gui.maintenanceparametre.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.maintenanceparametre.dao.MaintenanceParametreDao;
import fr.insee.arc.web.gui.maintenanceparametre.model.ModelMaintenanceParametre;
import fr.insee.arc.web.util.VObject;


@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorMaintenanceParameters extends ArcWebGenericService<ModelMaintenanceParametre>  {

	protected static final String RESULT_SUCCESS = "/jsp/maintenanceParameters.jsp";
	
	private static final Logger LOGGER = LogManager.getLogger(InteractorMaintenanceParameters.class);

	@Autowired
    protected ModelMaintenanceParametre views;
	
	private MaintenanceParametreDao dao;

	@Override
	protected void putAllVObjects(ModelMaintenanceParametre arcModel) {
		
		dao = new MaintenanceParametreDao(vObjectService, dataObjectService);
		
		views.setViewParameters(this.vObjectService.preInitialize(arcModel.getViewParameters()));
		
		putVObject(views.getViewParameters(), t -> initializeParameters(t));
	}

	/**
	 * Initializes {@code ModelMaintenanceParametre#viewParameters}. Calls dao to create the view.
	 * 
	 * @param viewParameters
	 */
    public void initializeParameters(VObject viewParameters) {
    	LoggerHelper.debug(LOGGER, "/* initializeParameters */");
		dao.initializeViewParameters(viewParameters);
    }

	@Override
	public String getActionName() {
		return "MaintenanceParameters";
	}

    
    
}