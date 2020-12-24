package fr.insee.arc.ws.services.restServices;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fr.insee.arc.utils.ressourceUtils.PropertySourcesHelper;

@Configuration
@EnableWebMvc
@ImportResource("/WEB-INF/applicationContext.xml")
@ComponentScan({ "fr.insee.arc.ws.services.rest", "fr.insee.arc.utils" })
public class WebConfig implements WebMvcConfigurer {
	
	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
		return PropertySourcesHelper.defaultWebappPropertySourcesConfigurer();
	}

	
}
