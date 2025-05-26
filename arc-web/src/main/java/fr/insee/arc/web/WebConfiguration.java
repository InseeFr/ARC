package fr.insee.arc.web;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.ressourceUtils.PropertySourcesHelper;
import fr.insee.arc.web.gui.all.interceptor.ArcInterceptor;
import fr.insee.arc.web.gui.all.util.WebLoggerDispatcher;

@EnableWebMvc
@Configuration
@ImportResource("classpath:applicationContext.xml")
@ComponentScan(basePackages = {"fr.insee.arc.web", "fr.insee.arc.core", "fr.insee.arc.utils"})
public class WebConfiguration implements WebMvcConfigurer {

	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) throws IOException {
		return PropertySourcesHelper.defaultWebappPropertySourcesConfigurer(env);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/js/**", "/css/**", "fonts/**", "img/**", "html/**"
					, "/webjars/**"
					)
			.addResourceLocations("/js/", "/css/", "/fonts/", "/img/", "/html/"
					, "/webjars/" 
					)
			;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		registry.addInterceptor(localeChangeInterceptor);

	    registry.addInterceptor(new ArcInterceptor());
	}
	
	@Bean
	MultipartResolver multipartResolver() {
	    return new StandardServletMultipartResolver();
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
		messageSource.setDefaultLocale(Locale.FRENCH);
		return messageSource;
	}
	
	@Bean(name="activeLoggerDispatcher")
	public LoggerDispatcher loggerDispatcher() {
		return new WebLoggerDispatcher();
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("jsp/home.jsp");
	}
	
	
	@Bean
	public ViewResolver getViewResolver() {
		return new InternalResourceViewResolver("/WEB-INF/", ""); 
	}
	
	@Override
    public void configureHandlerExceptionResolvers(
             List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new WebExceptionHandlerResolver());
    }

	
}
