package fr.insee.arc.web.gui.maintenanceoperation.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.maintenanceoperation.model.MaintenanceOperationsModel;


@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorMaintenanceOperations extends ArcWebGenericService<MaintenanceOperationsModel>  {

	protected static final String RESULT_SUCCESS = "/jsp/maintenanceOperations.jsp";

	@Autowired
	protected MaintenanceOperationsModel views;
	
	@Override
	protected void putAllVObjects(MaintenanceOperationsModel arcModel) {
		views.setViewOperations(this.vObjectService.preInitialize(arcModel.getViewOperations()));
		
		putVObject(views.getViewOperations(), t -> initializeOperations());
	}

    public void initializeOperations() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(views.getViewOperations(), new ArcPreparedStatementBuilder("SELECT true"),  "arc.operations", defaultInputFields);
    }

	@Override
	public String getActionName() {
		return "manageOperations";
	}
    
}