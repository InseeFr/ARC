package fr.insee.arc.ws.services.restServices;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WsAppInitalizer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected String getServletName() {
		return "arcws";
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
        return new Class[] {WsConfiguration.class, WsSecurityConfiguration.class };
    }

}