package fr.insee.arc.web.interceptor;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.insee.arc.utils.utils.LoggerHelper;

public class ArcInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LogManager.getLogger(ArcInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		LoggerHelper.trace(LOGGER, "URL: " + request.getRequestURL());
		request.setCharacterEncoding(StandardCharsets.UTF_8.toString());
		response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
		return super.preHandle(request, response, handler);
	}

}
