package fr.insee.arc.web.controllers.export;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.service.export.ExportService;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ControllerExport extends ExportService {

// hub controller class for export GUI

}
