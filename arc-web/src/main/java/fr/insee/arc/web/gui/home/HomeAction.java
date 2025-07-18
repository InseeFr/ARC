package fr.insee.arc.web.gui.home;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.webutils.ExceptionHandlerReport;
import fr.insee.arc.web.gui.all.model.NoModel;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.home.dao.HomeDao;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HomeAction extends ArcWebGenericService<NoModel, HomeDao> {

	public static final String ACTION_NAME = "home";
	private static final String RESULT_SUCCESS = "jsp/home.jsp";
	
	@RequestMapping("/home")
	public String home(Model model) {
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
	
	@RequestMapping("/denied")
	public ResponseEntity<Map<String, Object>> denied(HttpServletRequest request) {
		AccessDeniedException ex = new AccessDeniedException(request.getRequestURI().toString());
		return new ExceptionHandlerReport().reportExceptionAsMap(ex);
	}

}