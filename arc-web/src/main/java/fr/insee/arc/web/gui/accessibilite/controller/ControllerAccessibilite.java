package fr.insee.arc.web.gui.accessibilite.controller;


import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.gui.accessibilite.service.ServiceViewAccessibilite;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ControllerAccessibilite extends ServiceViewAccessibilite {


    @RequestMapping("/secure/accessibilite")
    public String accessibiliteAction(Model model) {
    	return accessibilite(model);
    }

}
