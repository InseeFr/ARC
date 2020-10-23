package fr.insee.arc.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.insee.arc.web.action.ArcAction;
import fr.insee.arc.web.model.ArcModel;
import fr.insee.arc.web.util.VObject;

public class ArcInterceptor extends HandlerInterceptorAdapter {
	
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
				for (VObject vObject : arcAction.getMapVObject().keySet()) {
					if (vObject != null) {
						modelAndView.getModel().put(vObject.getSessionName(), vObject);
					}
				}
			}
		}
		
	}
}
