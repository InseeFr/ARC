package fr.insee.arc.web.gui.maintenanceparametre.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.maintenanceparametre.dao.MaintenanceParametreDao;
import fr.insee.arc.web.gui.maintenanceparametre.model.ModelMaintenanceParametre;

public class InteractorMaintenanceParameters extends ArcWebGenericService<ModelMaintenanceParametre, MaintenanceParametreDao>  {

	protected static final String RESULT_SUCCESS = "jsp/maintenanceParameters.jsp";
	
	private static final Logger LOGGER = LogManager.getLogger(InteractorMaintenanceParameters.class);

	@Autowired
    protected ModelMaintenanceParametre views;
	
	@Override
	protected void putAllVObjects(ModelMaintenanceParametre arcModel) {
				
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