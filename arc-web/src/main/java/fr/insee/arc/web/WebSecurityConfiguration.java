package fr.insee.arc.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.webutils.WebAttributesName;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfiguration extends Oauth2ClientForKeycloak {
	
	private static final Logger LOGGER = LogManager.getLogger(WebSecurityConfiguration.class);

	// register Keycloak oauth2 client for authentification flow
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository(PropertiesHandler properties) {
		if (WebAttributesName.isKeycloakActive(properties.getKeycloakRealm())) {
			StaticLoggerDispatcher.custom(LOGGER, "Keycloak is set for arc-web");

			return new InMemoryClientRegistrationRepository(
					keycloakClientRegistration(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, properties));
		}
		StaticLoggerDispatcher.custom(LOGGER, "Keycloak is NOT set for arc-web");
		return null;
	}

	@Bean
	SecurityFilterChain clientSecurityFilterChain(HttpSecurity http, PropertiesHandler properties) throws Exception {
		
		// oath2 keycloak
		if (WebAttributesName.isKeycloakActive(properties.getKeycloakRealm())) {
			
			http.oauth2Login(o -> o.userInfoEndpoint(u -> u.userAuthoritiesMapper(userAuthoritiesMapper())))
			.authorizeHttpRequests(t -> t.requestMatchers("/secure/**").hasAnyAuthority(PropertiesHandler.getInstance().getAuthorizedRoles()));
			

			// disable debugging screens when property is not set else filter to this role
			if (properties.getDisableDebugGui().isEmpty()) {
				http.authorizeHttpRequests(t -> t.requestMatchers("/debug/**").denyAll());
			}
			else
			{
				http.oauth2Login(o -> o.userInfoEndpoint(u -> u.userAuthoritiesMapper(userAuthoritiesMapper())))
				.authorizeHttpRequests(t -> t.requestMatchers("/debug/**").hasAnyAuthority(PropertiesHandler.getInstance().getDisableDebugGui()));
			}
			
		}
		
		http.authorizeHttpRequests(t -> t.anyRequest().permitAll());

		http.exceptionHandling(e -> e.accessDeniedPage("/denied"));
		
		return http.build();
	}

}