package fr.insee.arc.web.gui.maintenanceoperation.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.util.TestLoggers;


@Service
public class ServiceViewOperation extends InteractorMaintenanceOperations {
    
	private static final String ORIGIN="WEB GUI";

    public String generateErrorMessageInLogsOperations(Model model) {
    	TestLoggers.sendLoggersTest(ORIGIN);
		return generateDisplay(model, RESULT_SUCCESS);
    }
    
    public String selectOperations(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
    }
    
}