package fr.insee.arc.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.action.ArcAction;
import fr.insee.arc.web.model.ArcModel;
import fr.insee.arc.web.util.VObject;

public class ArcInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LogManager.getLogger(ArcInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		LoggerHelper.trace(LOGGER, "URL: " + request.getRequestURL());
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView == null) {
			return;
		}

		if (handler instanceof HandlerMethod) {
			HandlerMethod springHandler = (HandlerMethod) handler;
			if (springHandler.getBean() instanceof ArcAction) {
				@SuppressWarnings("unchecked")
				ArcAction<ArcModel> arcAction = (ArcAction<ArcModel>) springHandler.getBean();
				// Adds all the vObjects to the model
				if (arcAction.getMapVObject() != null) {
					for (VObject vObject : arcAction.getMapVObject().keySet()) {
						if (vObject != null) {
							modelAndView.getModel().put(vObject.getSessionName(), vObject);
						}
					}
				}
			}
		}
		
	}
}
