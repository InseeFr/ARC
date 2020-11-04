package fr.insee.arc.web;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
public class WebSecurityConfig  extends KeycloakWebSecurityConfigurerAdapter {
	
	@Value("${keycloak.file:}")
	private String keycloakFile;

	@Value("${keycloak.resource:}")
	private String keycloakResource;


	/** Overrides KeycloakWebSecurityConfigurerAdapter 
	 *  to dynamically set the config file (if present).*/
	@Bean
	@Override
    protected AdapterDeploymentContext adapterDeploymentContext() throws Exception {
        AdapterDeploymentContextFactoryBean factoryBean;
        if (!keycloakFile.isEmpty()) {
             factoryBean = new AdapterDeploymentContextFactoryBean(new FileSystemResource(keycloakFile));
        } else if (!keycloakResource.isEmpty()) {
            factoryBean = new AdapterDeploymentContextFactoryBean(new ClassPathResource(keycloakResource));
        } else {
        	return new AdapterDeploymentContext();
        }
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
	

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requiresChannel().anyRequest().requiresSecure();
		if (isKeycloakActive()) {
			super.configure(http);
			http
			// public
			.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
			.antMatchers("/", "/index").permitAll()
			.antMatchers("/css/**", "fonts/**", "img/**", "/js/**").permitAll()
			// authenticated
			.antMatchers(HttpMethod.GET, "/*").authenticated()
			.antMatchers(HttpMethod.POST, "/*").authenticated()
			// everything else
			.anyRequest().denyAll();
		}
	}

	/** Returns true if Keycloak authentification should be used.*/
	private boolean isKeycloakActive() {
		return !keycloakFile.isEmpty() || !keycloakResource.isEmpty();
	}

	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(keycloakAuthenticationProvider());
	}

}