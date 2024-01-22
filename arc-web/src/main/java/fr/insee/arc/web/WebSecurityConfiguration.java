package fr.insee.arc.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.webutils.WebAttributesName;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfiguration extends Oauth2ClientForKeycloak {

	// register Keycloak oauth2 client for authentification flow
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		if (WebAttributesName.isKeycloakActive(keycloakRealm)) {
			return new InMemoryClientRegistrationRepository(
					keycloakClientRegistration(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));
		}
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