package fr.insee.arc.web;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.ressourceUtils.PropertySourcesHelper;
import fr.insee.arc.web.interceptor.ArcInterceptor;
import fr.insee.arc.web.util.WebLoggerDispatcher;

@EnableWebMvc
@Configuration
@ImportResource("classpath:applicationContext.xml")
@ComponentScan(basePackages = {"fr.insee.arc.web", "fr.insee.arc.core", "fr.insee.arc.utils"})
public class WebConfig implements WebMvcConfigurer {
	
	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
		return PropertySourcesHelper.defaultWebappPropertySourcesConfigurer();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/js/**", "/css/**", "fonts/**", "img/**", "html/**")
			.addResourceLocations("/js/", "/css/", "/fonts/", "/img/", "/html/");
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		registry.addInterceptor(localeChangeInterceptor);

	    registry.addInterceptor(new ArcInterceptor());
	}

	@Bean(name=MultipartFilter.DEFAULT_MULTIPART_RESOLVER_BEAN_NAME)
	public CommonsMultipartResolver multipartResolver() {
	    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
	    multipartResolver.setMaxUploadSize(2147483647);
	    return multipartResolver;
	}

	@Bean
	public LocaleResolver localeResolver() {
		return new SessionLocaleResolver();
	}
	
	@Bean(name = "messageSource")
	public MessageSource getMessageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.addBasenames("messages");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setAlwaysUseMessageFormat(true);
		messageSource.setDefaultLocale(Locale.ENGLISH);
		return messageSource;
	}
	
	@Bean(name="activeLoggerDispatcher")
	public LoggerDispatcher loggerDispatcher() {
		return new WebLoggerDispatcher();
	}

	@Bean
	public ViewResolver getViewResolver() {
		return new InternalResourceViewResolver("/WEB-INF/", ""); 
	}

}
