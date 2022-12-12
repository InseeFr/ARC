package fr.insee.arc.web.webusecases.gerernorme;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.webusecases.gerernorme.service.GererNormeService;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HubControllerGererNorme extends GererNormeService {


/**
 * Controller hub for the gui gererNorme
 */

	
}
