package fr.insee.arc.ws.services.rest;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitalizer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected String getServletName() {
		return "sirene";
	}
	
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/*" };
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {WebConfig.class};
    }

}