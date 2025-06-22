package fr.insee.arc.utils.webutils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ExceptionHandlerReport {
	
	private static final String ERROR_KEY_TO_DISPLAY = "Error";
	
	class ErrorReport {

		public ErrorReport(String message) {
			super();
			this.message = message;
		}
		
		private String message;
		
		public String getMessage() {
			return message;
		}

	}

	public ModelAndView reportException(HttpServletRequest request)
	{
		HttpHeaders httpHeaders = Collections.list(request.getHeaderNames())
			    .stream()
			    .collect(Collectors.toMap(
			        Function.identity(),
			        h -> Collections.list(request.getHeaders(h)),
			        (oldValue, newValue) -> newValue,
			        HttpHeaders::new
			    ));
		return reportException(new NoHandlerFoundException(request.getMethod(), request.getRequestURI(),
				httpHeaders));
	}
	
	public ModelAndView reportException(Exception ex)
	{
		ModelAndView model = new ModelAndView();
		model.setView(new MappingJackson2JsonView());
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
