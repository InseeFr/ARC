package fr.insee.arc.web.gui.entrepot.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.entrepot.dao.EntrepotDao;
import fr.insee.arc.web.gui.entrepot.model.ModelEntrepot;


@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorEntrepot extends ArcWebGenericService<ModelEntrepot, EntrepotDao>  {

	protected static final String RESULT_SUCCESS = "jsp/gererEntrepot.jsp";
	
	private static final Logger LOGGER = LogManager.getLogger(InteractorEntrepot.class);

	@Autowired
    protected ModelEntrepot views;
	
	@Override
	protected void putAllVObjects(ModelEntrepot arcModel) {
				
		views.setViewEntrepot(this.vObjectService.preInitialize(arcModel.getViewEntrepot()));
		
		putVObject(views.getViewEntrepot(), t -> initializeEntrepot(t));
	}

	/**
	 * Initializes {@code ModelEntrepot#viewEntrepot}. Calls dao to create the view.
	 * 
	 * @param viewParameters
	 */
    public void initializeEntrepot(VObject viewEntrepot) {
    	LoggerHelper.debug(LOGGER, "/* initializeEntrepot */");
		dao.initializeViewEntrepot(viewEntrepot);
    }

	@Override
	public String getActionName() {
		return "Entrepot";
	}

    
    
}