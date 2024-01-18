package fr.insee.arc.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import static org.springframework.security.config.Customizer.withDefaults;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
	static final int example = 3;

//	@Value("${keycloak.file:}")
//	private String keycloakFile;
//
//	@Value("${keycloak.resource:}")
//	private String keycloakResource;

	@Value("${fr.insee.keycloak.realm}")
	private String keycloakRealm;

	@Value("${fr.insee.keycloak.server}")
	private String keycloakServer;

	@Value("${fr.insee.keycloak.resource}")
	private String keycloakResource;

	@Value("${fr.insee.keycloak.credentials.secret}")
	private String keycloakCredential;
	
	@Autowired
	private PropertiesHandler properties;

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("xxx").roles("ADMIN").build();
		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() throws IOException {
		return new InMemoryClientRegistrationRepository(this.keycloakClientRegistration());
	}

	public static String readFileToString(File f) throws IOException {
		return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
	}

	private ClientRegistration keycloakClientRegistration() {

		String realmUri = keycloakServer + "/realms/" + keycloakRealm;
		String openIdConnect = "/protocol/openid-connect";

		return ClientRegistration //
				.withRegistrationId(keycloakRealm) //
				.clientId(keycloakResource) //
				.clientSecret(keycloakCredential) //
				.redirectUri("{baseUrl}" + "/login/oauth2/code/" + "{registrationId}") //
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) //
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) //
				.issuerUri(realmUri) //
				.scope("openid","profile","email","roles","web-origins")
				.authorizationUri(realmUri + openIdConnect + "/auth") //
				.tokenUri(realmUri + openIdConnect + "/token") //
				.userInfoUri(realmUri + openIdConnect + "/userinfo") //
				.jwkSetUri(realmUri + openIdConnect + "/certs") //
				.userNameAttributeName("preferred_username") //
				.build();
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
					ex -> ex.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll().requestMatchers("/").permitAll()
							.requestMatchers("/secure/**").authenticated()
			);

			http.httpBasic(Customizer.withDefaults());

			break;

		case 3:
			if (keycloakRealm != null) {
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

	private GrantedAuthoritiesMapper userAuthoritiesMapper() {
	    return (authorities) -> {
	      Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

	      authorities.forEach(
	          authority -> {
	            if (authority instanceof OidcUserAuthority) {
	              OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
	              OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
	              
//	              System.out.println("§§§§§§§§§§§§");
//	              System.out.println(oidcUserAuthority);
//	              System.out.println(userInfo.getFullName());
//	              System.out.println(userInfo.getClaims());

	              List<String> roles = userInfo.getClaimAsStringList("roles");
	              if (roles==null)
	              {
	            	  roles = userInfo.getClaimAsStringList("groups");
	              }
	              
	              List<SimpleGrantedAuthority> groupAuthorities =
	            		  roles.stream()
	                      .map(g -> new SimpleGrantedAuthority(g))
	                      .toList();
	              mappedAuthorities.addAll(groupAuthorities);
	            }
	          });

	      return mappedAuthorities;
	    };
	  }
	
}