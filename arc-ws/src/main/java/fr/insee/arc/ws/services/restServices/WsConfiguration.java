package fr.insee.arc.ws.services.restServices;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fr.insee.arc.utils.ressourceUtils.PropertySourcesHelper;

@Configuration
@EnableWebMvc
@ImportResource("/WEB-INF/applicationContext.xml")
@ComponentScan({ "fr.insee.arc.ws.services.restServices", "fr.insee.arc.utils" })
public class WsConfiguration implements WebMvcConfigurer {
	
	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) throws IOException {
		return PropertySourcesHelper.defaultWebappPropertySourcesConfigurer(env);
	}

	@Override
    public void configureHandlerExceptionResolvers(
             List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new WsExceptionHandlerResolver());
    }
	
}
