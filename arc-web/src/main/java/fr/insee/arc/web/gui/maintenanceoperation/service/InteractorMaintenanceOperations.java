package fr.insee.arc.web.gui.maintenanceoperation.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.maintenanceoperation.dao.MaintenanceOperationDao;
import fr.insee.arc.web.gui.maintenanceoperation.model.MaintenanceOperationsModel;


@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorMaintenanceOperations extends ArcWebGenericService<MaintenanceOperationsModel,MaintenanceOperationDao>  {

	protected static final String RESULT_SUCCESS = "jsp/maintenanceOperations.jsp";

	@Autowired
	protected MaintenanceOperationsModel views;
	
	@Override
	protected void putAllVObjects(MaintenanceOperationsModel arcModel) {
		
		views.setNumberOfExecutorDatabase(arcModel.getNumberOfExecutorDatabase());	

		views.setUrl(arcModel.getUrl());
		views.setHttpType(arcModel.getHttpType());
		views.setToken(arcModel.getToken());
		views.setJson(arcModel.getJson());
		views.setHttpOutput(null);
		
		views.setViewOperations(this.vObjectService.preInitialize(arcModel.getViewOperations()));
		views.setViewKubernetes(this.vObjectService.preInitialize(arcModel.getViewKubernetes()));
		
		putVObject(views.getViewOperations(), t -> initializeOperations());
		putVObject(views.getViewKubernetes(), t -> initializeKube());
	}
	
	@Override
	public void extraModelAttributes(Model model) {
		model.addAttribute("numberOfExecutorDatabase", views.getNumberOfExecutorDatabase());
		model.addAttribute("url", views.getUrl());
		model.addAttribute("httpType", views.getHttpType());
		model.addAttribute("token", views.getToken());
		model.addAttribute("json", views.getJson());
		model.addAttribute("httpOutput", views.getHttpOutput());
	}


    public void initializeOperations() {
        Map<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(views.getViewOperations(), new ArcPreparedStatementBuilder("SELECT true"),  "arc.operations", defaultInputFields);
    }
    
    public void initializeKube() {
    	Map<String, String> defaultInputFields = new HashMap<>();
    	this.vObjectService.initialize(views.getViewKubernetes(), new ArcPreparedStatementBuilder("SELECT true"),  "arc.kube", defaultInputFields);
    }

	@Override
	public String getActionName() {
		return "manageOperations";
	}
    
}