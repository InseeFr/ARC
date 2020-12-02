package fr.insee.arc.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import fr.insee.arc.utils.ressourceUtils.PropertySourcesHelper;

@Configuration
@ImportResource("classpath:applicationContext.xml")
public class BatchConfig {

	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		PropertySourcesHelper fetcher = new PropertySourcesHelper();
		List<String> expectedPaths = new ArrayList<>();
		if (System.getProperty("properties.path") != null) {
			expectedPaths.add("file:${properties.path}/*.properties");
		}
		expectedPaths.add("classpath*:fr/insee/config/*.properties");
		fetcher.configure(configurer, expectedPaths.toArray(new String[0]));
		configurer.setIgnoreUnresolvablePlaceholders(true);
		configurer.setIgnoreResourceNotFound(true);
		return configurer;
	}

}
