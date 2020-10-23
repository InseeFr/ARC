package fr.insee.arc.ws.services.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import fr.insee.arc.core.util.DefaultLoggerDispatcher;
import fr.insee.arc.core.util.LoggerDispatcher;

@Configuration
@EnableWebMvc
@ComponentScan("fr.insee.arc.ws.services.rest")
public class WebAppInitalizer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/*"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{WebAppInitalizer.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[0];
    }
    
    @Bean
    public LoggerDispatcher loggerDispatcher() {
    	return new DefaultLoggerDispatcher();
    }
}