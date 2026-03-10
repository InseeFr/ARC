package fr.insee.arc.web.gui.home;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.NoHandlerFoundException;

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
	public ResponseEntity<Map<String, Object>> denied(Model model, HttpServletRequest request) {
		NoHandlerFoundException ex = new NoHandlerFoundException(HttpMethod.GET.toString(), request.getRequestURI(), HttpHeaders.EMPTY);
		return new ExceptionHandlerReport().reportExceptionAsMap(ex);

	}

}