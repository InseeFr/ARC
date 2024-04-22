package fr.insee.arc.utils.webutils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Component
public class ExceptionHandlerReport {
	
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
	
	public ModelAndView reportException(Exception ex)
	{
		ModelAndView model = new ModelAndView();
		model.setView(new MappingJackson2JsonView());
		
		ErrorReport e = new ErrorReport(ex.getMessage(), ex.getStackTrace()[0]);
		model.addObject("Error", e);
		return model;
	}

}
