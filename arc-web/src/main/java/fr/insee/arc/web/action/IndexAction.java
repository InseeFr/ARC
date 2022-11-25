package fr.insee.arc.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.webutils.WebUtils;
import fr.insee.arc.web.model.NoModel;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IndexAction extends ArcAction<NoModel> {

	static final String ACTION_NAME = "index";
	private static final String RESULT_SUCCESS = "jsp/index.jsp";

	@RequestMapping({ "/index" })
	public String index(Model model, HttpServletRequest request) {
		getSession().put("console", "");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/healthcheck")
	public ResponseEntity<Map<String, Object>> healthcheck() {
		Map<String, Object> map = new HashMap<>();
		boolean status = WebUtils.getHealthCheckStatus(map);
		if (!status) {
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@Override
	public void putAllVObjects(NoModel arcModel) {
		// no vObject in this controller
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

}