package fr.insee.arc.ws.services.restServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.webutils.WebAttributesName;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WsSecurityConfiguration {

	private static final Logger LOGGER = LogManager.getLogger(WsSecurityConfiguration.class);

	public JwtAuthenticationConverter jwtAuthenticationConverterForKeycloak() {
		Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
			Map<String, Object> resourceAccess = jwt.getClaim("realm_access");
			Object rolesRaw = resourceAccess.get("roles");
			@SuppressWarnings("unchecked")
			ArrayList<String> roles = (ArrayList<String>) rolesRaw;
			return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		};

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, PropertiesHandler properties) throws Exception {
		if (WebAttributesName.isKeycloakActive(properties.getKeycloakRealm())) {
			http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwtResourceServer -> jwtResourceServer
					.jwtAuthenticationConverter(jwtAuthenticationConverterForKeycloak())))
					.authorizeHttpRequests(t -> t.requestMatchers(new AntPathRequestMatcher("/execute/**") , new AntPathRequestMatcher("/webservice/**"))
							.hasAnyAuthority(PropertiesHandler.getInstance().getAuthorizedRoles()));
		}
		else
		{
			http.csrf(t-> t.requireCsrfProtectionMatcher(new AntPathRequestMatcher("/execute/**")).disable());
		}

		http.authorizeHttpRequests(t -> t.anyRequest().permitAll());

		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder(PropertiesHandler properties) {
		if (WebAttributesName.isKeycloakActive(properties.getKeycloakRealm())) {
			StaticLoggerDispatcher.custom(LOGGER, "Keycloak is set for arc-ws");
			return JwtDecoders.fromIssuerLocation(properties.getKeycloakServer() + "/realms/" + properties.getKeycloakRealm());
		}
		StaticLoggerDispatcher.custom(LOGGER, "Keycloak is NOT set for arc-ws");
		return null;
	}

}
