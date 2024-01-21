package fr.insee.arc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.webutils.WebSecurity;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration extends WebSecurity {
	static final int example = 3;

	
	
	// register Keycloak oauth2 client for authentification flow
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(keycloakClientRegistration(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));
	}
	
	@Autowired
	private PropertiesHandler properties;
	
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("xxx").roles("ADMIN").build();
		return new InMemoryUserDetailsManager(user);
	}


	@Bean
	SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {

		switch (example) {

		// example 1 deprecated method
		// ok
		case 1:
			http.authorizeRequests().requestMatchers("/", "/home").permitAll().requestMatchers("/secure/**")
					.hasRole("ADMIN").and().httpBasic();
			break;

		// example 2 lambda
		// ko
		case 2:
			http.authorizeHttpRequests(
					ex -> {
						try {
							ex //
							.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll() // dispatchers to match
							.requestMatchers("/", "/home", "/oauth2/**").permitAll()
							.requestMatchers("/secure/**").hasAnyAuthority(properties.getAuthorizedRoles())
							.and()
							.oauth2Login()
							.userInfoEndpoint()
							.userAuthoritiesMapper(userAuthoritiesMapper());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
			);

			http.httpBasic(Customizer.withDefaults());

			break;

		case 3:
			if (isKeycloakActive()) {
//				http.authorizeRequests().requestMatchers("/**").permitAll();
				
//				http.oauth2Login(withDefaults());
//				http.authorizeHttpRequests(ex -> ex.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
//                        .requestMatchers( "/", "/login/**", "/oauth2/**").permitAll()
//                        .requestMatchers("/secure/**").hasRole("ADMIN").anyRequest().authenticated()
//                        );
//					return http.build();

        
				http.authorizeRequests().requestMatchers("/", "/home", "/oauth2/**").permitAll()
						.requestMatchers("/secure/**").hasAnyAuthority(properties.getAuthorizedRoles()).and()
						.oauth2Login()
						.userInfoEndpoint()
						.userAuthoritiesMapper(userAuthoritiesMapper())
						;
				
			} else {
				http.authorizeRequests().requestMatchers("/", "/home", "/oauth2/**").permitAll()
						.requestMatchers("/secure/**").permitAll();
			}
			break;

		default:
			break;

		}

		return http.build();
	}


	
}