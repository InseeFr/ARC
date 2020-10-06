package fr.insee.arc.web.interceptor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class LocaleInterceptor implements Interceptor {
	
	private static String LOCALE_SESSION_ATTRIBUTE = "current_locale";
	
	private Locale defaultLocale = Locale.ENGLISH;

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void init() {
		// do nothing
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		String parameter = request.getParameter("request_locale");
		if (parameter != null) {
			request.getSession().setAttribute(LOCALE_SESSION_ATTRIBUTE, Locale.forLanguageTag(parameter));
		}
		Object sessionLocale = request.getSession().getAttribute(LOCALE_SESSION_ATTRIBUTE);
		if (sessionLocale == null) {
			request.getSession().setAttribute(LOCALE_SESSION_ATTRIBUTE, defaultLocale);
		}
		return invocation.invoke();
	}

}
