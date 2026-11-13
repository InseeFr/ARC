package fr.insee.arc.web.gui.accessibilite.controller;


import fr.insee.arc.web.gui.accessibilite.dao.AccessibiliteDao;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import fr.insee.arc.web.gui.all.model.NoModel;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ControllerAccessibilite extends ArcWebGenericService<NoModel, AccessibiliteDao> {

    public static final String ACTION_NAME = "accessibilite";
    private static final String RESULT_SUCCESS = "jsp/accessibilite.jsp";

    @RequestMapping("/secure/accessibilite")
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

    @Override
    public void extraModelAttributes(Model model) {
        model.addAttribute("accessibilityMail", properties.getAccessibilityMail());
        model.addAttribute("accessibilitySiteUrl", properties.getAccessibilitySiteUrl());
        model.addAttribute("accessibilitySchemaPeriod", properties.getAccessibilitySchemaPeriod());
        model.addAttribute("accessibilityRaniMail", properties.getAccessibilityRaniMail());
    }
}
