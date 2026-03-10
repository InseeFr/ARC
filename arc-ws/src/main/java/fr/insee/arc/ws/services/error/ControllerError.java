package fr.insee.arc.ws.services.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import fr.insee.arc.utils.webutils.ExceptionHandlerReport;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ControllerError {

	@RequestMapping("/errors")
	public ModelAndView errors(HttpServletRequest request) {
		return new ExceptionHandlerReport().reportException(request);
	}

}
