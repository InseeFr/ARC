package fr.insee.arc.utils.webutils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.Sanitize;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class ExceptionHandlerReport {
	
	private static final String ERROR_KEY_TO_DISPLAY = "Error";

	public ModelAndView reportException(HttpServletRequest request)
	{
		return reportException(new ArcException(ArcExceptionMessage.INVALID_HTTP_REQUEST,
				Sanitize.htmlParameter(request.getMethod())
				, Sanitize.htmlParameter(request.getRequestURI()))
				);
	}
	
	public ModelAndView reportException(Exception ex)
	{
		ModelAndView model = new ModelAndView();
		model.setView(new JacksonJsonView());
		ErrorReport e = new ErrorReport(ex.getMessage());
		model.addObject(ERROR_KEY_TO_DISPLAY, e);
		return model;
	}

	public ResponseEntity<Map<String, Object>> reportExceptionAsMap(Exception ex) {
		Map<String, Object> map = new HashMap<>();
		ErrorReport e = new ErrorReport(ex.getMessage());
		map.put(ERROR_KEY_TO_DISPLAY, e);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
