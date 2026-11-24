package fr.insee.arc.web.gui.accessibilite.service;

import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.web.gui.accessibilite.dao.AccessibiliteDao;
import fr.insee.arc.web.gui.all.model.NoModel;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;

public class ServiceViewAccessibilite extends ArcWebGenericService<NoModel, AccessibiliteDao>
{


    public static final String ACTION_NAME = "accessibilite";
    
    private static final String RESULT_SUCCESS = "jsp/accessibilite.jsp";
    
    public String accessibilite(Model model) {
    	return generateDisplay(model, RESULT_SUCCESS);
    }
    

    @Override
    public void putAllVObjects(NoModel arcModel) {
        // no vObject in this controller
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    
    public static String getContextUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
          .build()
          .toUriString();
    }

    @Override
    public void extraModelAttributes(Model model) {
    	BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);
    	model.addAttribute("accessibilityMailArc", bdParameters.getString(null, "ACCESSIBILITY_MAIL_MAINTENANCE"));
        model.addAttribute("accessibilityMailReferent", bdParameters.getString(null, "ACCESSIBILITY_MAIL_REFERENT"));
        model.addAttribute("accessibilityStrategyUrl", bdParameters.getString(null, "ACCESSIBILITY_STRATEGY_URL"));
        model.addAttribute("accessibilityStrategyPeriod", bdParameters.getString(null, "ACCESSIBILITY_STRATEGY_PERIOD"));

        model.addAttribute("contextUrl", getContextUrl());
    }
    
}
