package fr.insee.arc.web.webusecases.export.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.webusecases.export.service.HubServiceExport;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HubControllerExport extends HubServiceExport {

// hub controller class for export GUI

}
