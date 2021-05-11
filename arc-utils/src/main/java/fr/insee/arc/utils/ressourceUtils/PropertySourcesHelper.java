package fr.insee.arc.utils.ressourceUtils;

import java.io.IOException;

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;

/** Utility class to fetch property sources and add them to a PropertySourcesPlaceholderConfigurer object.*/
public class PropertySourcesHelper {


	/** Adds the internal properties and all other properties defined through the propertiesPaths argument.
	 * The priority order for property resolutions is based on the order of propertiesPaths. */
	public PropertySourcesPlaceholderConfigurer configure(PropertySourcesPlaceholderConfigurer configurer,
			ConfigurableEnvironment env, String... propertiesPaths) throws IOException {
		MutablePropertySources propertySources = env.getPropertySources();
        for (String path : propertiesPaths) {
        	Resource[] classPathPropertyFiles = getAllPropertiesIn(path);
            setPropertySourcesFromRessources(propertySources, classPathPropertyFiles);
        }
		configurer.setPropertySources(propertySources);
		return configurer;
	}
	
	private Resource[] getAllPropertiesIn(String path) throws IOException {
	    return new PathMatchingResourcePatternResolver()
	            .getResources(path);
	}

	private void setPropertySourcesFromRessources(MutablePropertySources propertySources, Resource[] propertiesFiles)
			throws IOException {
		for (Resource propertyFile : propertiesFiles) {
            propertySources.addLast(new ResourcePropertySource(new EncodedResource(propertyFile)));
        }
	}

	public static PropertySourcesPlaceholderConfigurer defaultWebappPropertySourcesConfigurer(ConfigurableEnvironment env)
			throws IOException {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		PropertySourcesHelper fetcher = new PropertySourcesHelper();
		String tomcatDir = System.getProperty("catalina.base");
		if (tomcatDir != null) {
			fetcher.configure(configurer, env,
					"file:" + tomcatDir + "/webapps/*.properties",
					"file:" + tomcatDir + "/wtpwebapps/*.properties",
					"classpath*:fr/insee/config/*.properties");
		} else {
			fetcher.configure(configurer, env, "classpath*:fr/insee/config/*.properties");
		}
		configurer.setIgnoreUnresolvablePlaceholders(true);
		configurer.setIgnoreResourceNotFound(true);
		return configurer;
	}

	
}
