package fr.insee.arc.web;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfiguration.class, WebSecurityConfiguration.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
    
	
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
    	registration.setMultipartConfig(new MultipartConfigElement(""));
    }

}