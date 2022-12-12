package fr.insee.arc.web.webusecases.export.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.webusecases.export.services.ExportService;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HubControllerExport extends ExportService {

// hub controller class for export GUI

}
