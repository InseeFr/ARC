package fr.insee.arc.ws.services.rest;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.channel.RetryWithHttpsEntryPoint;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig  extends KeycloakWebSecurityConfigurerAdapter {

	@Value("${keycloak.file:}")
	private String keycloakFile;

	@Value("${keycloak.resource:}")
	private String keycloakResource;

	@Autowired
	private PropertiesHandler properties;

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
	public void configure(WebSecurity web) throws Exception {
		
		// Temporary fix to allow post on http without keycloak on
		if (!isKeycloakActive()) {
			web.ignoring().antMatchers("/**");
		}
		else
		{
			// Do not apply Spring Security to the other webservice
			web.ignoring().antMatchers("/webservice/**");
		}
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if (!isKeycloakActive()) {
			http.authorizeRequests().antMatchers("/**").permitAll();
		} else {
			String[] authorizedRoles = properties.getAuthorizedRoles();

			super.configure(http);
			http.authenticationProvider(keycloakAuthenticationProvider())
			// Force https
			.requiresChannel().anyRequest().requiresSecure().channelProcessors(Arrays.asList(httpsRedirect307()))
			.and()
			.csrf().disable()
			// Keycloak filter
			.addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class).addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class).exceptionHandling()
			.authenticationEntryPoint(authenticationEntryPoint())
			.and()
			// Public
			.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
			// Semi-public
			.antMatchers(HttpMethod.GET, "/hello").authenticated();
			// Restricted
			if (authorizedRoles.length == 0) {
				http.authorizeRequests().antMatchers(HttpMethod.GET, "/generateSandbox").authenticated()
				.antMatchers(HttpMethod.POST, "/liasse/**").authenticated()
				.antMatchers(HttpMethod.POST, "/liasse2/**").authenticated()
				.antMatchers(HttpMethod.POST, "/execute/engine/**").authenticated();
			} else {
				http.authorizeRequests().antMatchers(HttpMethod.GET, "/generateSandbox").hasAnyAuthority(authorizedRoles)
				.antMatchers(HttpMethod.POST, "/liasse/**").hasAnyAuthority(authorizedRoles)
				.antMatchers(HttpMethod.POST, "/liasse2/**").hasAnyAuthority(authorizedRoles)
				.antMatchers(HttpMethod.POST, "/execute/engine/**").hasAnyAuthority(authorizedRoles);
			}
			http.authorizeRequests().anyRequest().denyAll();
		}
	}


	/** Redirect using 307 instead of 302 to avoid unwanted post => get redirects
	 *  and loss of the post body.*/
	private SecureChannelProcessor httpsRedirect307() {
		final RedirectStrategy redirectStrategy = new RedirectStrategy() {
			@Override
			public void sendRedirect(HttpServletRequest request, HttpServletResponse response,
					String url) throws IOException {
				response.addHeader("Location", response.encodeRedirectURL(url));
				response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
				response.flushBuffer();
			}
		};

		final RetryWithHttpsEntryPoint entryPoint = new RetryWithHttpsEntryPoint();
		entryPoint.setRedirectStrategy(redirectStrategy);

		final SecureChannelProcessor secureChannelProcessor = new SecureChannelProcessor();
		secureChannelProcessor.setEntryPoint(entryPoint);
		return secureChannelProcessor;
	}

	/** Returns true if Keycloak authentification should be used.*/
	private boolean isKeycloakActive() {
		return !keycloakFile.isEmpty() || !keycloakResource.isEmpty();
	}

	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new NullAuthenticatedSessionStrategy();
	}

	/**
	 * Registers the KeycloakAuthenticationProvider with the authentication manager.
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

}
