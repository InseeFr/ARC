package fr.insee.arc.utils.webutils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomExceptionHandlerResolver implements HandlerExceptionResolver {
	
	class ErrorReport {

		public ErrorReport(String message, StackTraceElement stack) {
			super();
			this.message = message;
			this.stack = stack;
		}
		
		private String message;
		private StackTraceElement stack;
		
		public String getMessage() {
			return message;
		}
		public StackTraceElement getStack() {
			return stack;
		}
	}
	

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		ModelAndView model = new ModelAndView();
		model.setView(new MappingJackson2JsonView());
		
		ErrorReport e = new ErrorReport(ex.getMessage(), ex.getStackTrace()[0]);
		model.addObject("Error", e);
		return model;
	}
}
