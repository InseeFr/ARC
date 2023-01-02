package fr.insee.arc.web.gui.maintenanceparametre.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.ArcWebGenericService;
import fr.insee.arc.web.gui.maintenanceparametre.model.ModelMaintenanceParametre;


@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorMaintenanceParameters extends ArcWebGenericService<ModelMaintenanceParametre>  {

	protected static final String RESULT_SUCCESS = "/jsp/maintenanceParameters.jsp";

	@Autowired
    protected ModelMaintenanceParametre views;

	@Override
	protected void putAllVObjects(ModelMaintenanceParametre arcModel) {
		views.setViewParameters(this.vObjectService.preInitialize(arcModel.getViewParameters()));
		
		putVObject(views.getViewParameters(), t -> initializeParameters());
	}

    public void initializeParameters() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(views.getViewParameters(), new ArcPreparedStatementBuilder("SELECT row_number() over (order by description,key,val) as i, key ,val, description FROM arc.parameter"),  "arc.parameter", defaultInputFields);
    }

	@Override
	public String getActionName() {
		return "MaintenanceParameters";
	}

    
    
}