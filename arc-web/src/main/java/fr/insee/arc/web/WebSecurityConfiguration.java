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
	public ClientRegistrationRepository clientRegistrationRepository() {
		if (WebAttributesName.isKeycloakActive(keycloakRealm)) {
			StaticLoggerDispatcher.custom(LOGGER, "Keycloak is set for arc-web");

			return new InMemoryClientRegistrationRepository(
					keycloakClientRegistration(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));
		}
		StaticLoggerDispatcher.custom(LOGGER, "Keycloak is NOT set for arc-web");
		return null;
	}

	@Bean
	SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
		if (WebAttributesName.isKeycloakActive(keycloakRealm)) {
			http.authorizeRequests().requestMatchers("/secure/**")
					.hasAnyAuthority(PropertiesHandler.getInstance().getAuthorizedRoles()) //
					.and().oauth2Login().userInfoEndpoint().userAuthoritiesMapper(userAuthoritiesMapper());

		}
		return http.build();
	}

}