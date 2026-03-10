package fr.insee.arc.web;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import fr.insee.arc.utils.webutils.ExceptionHandlerReport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WebExceptionHandlerResolver implements HandlerExceptionResolver {

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		return new ExceptionHandlerReport().reportException(ex);
	}
	
}
