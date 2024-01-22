package fr.insee.arc.ws.services.restServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
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

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WsSecurityConfiguration {

	@Value("${fr.insee.keycloak.realm}")
	private String keycloakRealm;

	@Value("${fr.insee.keycloak.server}")
	private String keycloakServer;

	@Value("${fr.insee.keycloak.resource}")
	private String keycloakResource;

	@Value("${fr.insee.keycloak.credentials.secret}")
	private String keycloakCredential;

	public JwtAuthenticationConverter jwtAuthenticationConverterForKeycloak() {
		Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
			
			System.out.println(jwt.getClaims());
			
			Map<String, Object> resourceAccess = jwt.getClaim("realm_access");

			System.out.println("§§§§§§§§§§§");
			System.out.println(resourceAccess);

			Object rolesRaw = resourceAccess.get("roles");
			ArrayList<String> roles = (ArrayList<String>) rolesRaw;
			
			System.out.println(roles);

			
//			
//			
//			LinkedTreeMap<String, List<String>> clientRoleMap = (LinkedTreeMap<String, List<String>>) roles;
//
//			System.out.println("§§§§§§§§§§§");
//			System.out.println(clientRoleMap);
//
//			List<String> clientRoles = new ArrayList<>(resourceAccess.get("roles"));

			return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		};

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

		return jwtAuthenticationConverter;
	}

	/**
	 * return all jakarta dispatcher to be able to do the matching
	 * @return
	 */
	public static DispatcherType[] allDispatchers()
	{
		return new DispatcherType[] {DispatcherType.FORWARD};
	}
	
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http.csrf().disable(); // NOSONAR disable csrf because of API mode
//		http.authorizeRequests() //
//				.requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.toString())).permitAll()
//				.requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
//				.requestMatchers(new AntPathRequestMatcher("/execute/**")).fullyAuthenticated().and()
//				.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwtResourceServer -> {
//					jwtResourceServer
////        		.decoder(jwtDecoder())
////        		.jwkSetUri(keycloakServer + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs")
//							.jwtAuthenticationConverter(jwtAuthenticationConverterForKeycloak());
//					System.out.println(keycloakServer + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs");
//				})
//
//				);
//        http.authorizeHttpRequests(auth -> 
//            auth.dispatcherTypeMatchers(allDispatchers()).permitAll() //
//            .requestMatchers(new AntPathRequestMatcher("/**")).permitAll() //
//			.requestMatchers(new AntPathRequestMatcher("/execute/**")).fullyAuthenticated()
//        );
        
    	http.authorizeRequests() //
		.requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.toString())).permitAll()
		.requestMatchers(new AntPathRequestMatcher("/execute/**")).authenticated()
		.and()
        .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwtResourceServer -> {
			jwtResourceServer
//    		.decoder(jwtDecoder())
//    		.jwkSetUri(keycloakServer + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs")
						.jwtAuthenticationConverter(jwtAuthenticationConverterForKeycloak());
        }))
        ;
		
		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		System.out.println(keycloakServer + "/realms/" + keycloakRealm);
		return JwtDecoders.fromIssuerLocation(keycloakServer + "/realms/" + keycloakRealm);
	}

//	@Autowired
//	private KeycloakLogoutHandler loh;
//
//	@Bean
//	public ClientRegistrationRepository clientRegistrationRepository() {
//		return new InMemoryClientRegistrationRepository(keycloakClientRegistration(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));
//	}
//	
//	
//	@Bean
//	public WebSecurityCustomizer webSecurityCustomizer() {
//		if (!isKeycloakActive()) {
//			return (web) -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/**"));
//		} else {
//			// Do not apply Spring Security to the dataretrieval webservice (old servlet)
//			return (web) -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/webservice/**"));
//		}
//	}
//	
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
//
//    	http.authorizeRequests() //
//		.requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.toString())).permitAll()
//		.requestMatchers(new AntPathRequestMatcher("/execute/**")).fullyAuthenticated()
//		;
//
//        // by default spring security oauth2 client does not support PKCE for confidential clients for auth code grant flow,
//        // we explicitly enable the PKCE customization here.
//        http.oauth2Client(o2cc -> {
//            var oauth2AuthRequestResolver = new DefaultOAuth2AuthorizationRequestResolver( //
//                    clientRegistrationRepository, //
//                    OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI //
//            );
//            oauth2AuthRequestResolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
//            o2cc.authorizationCodeGrant(customizer -> {
//                customizer.authorizationRequestResolver(oauth2AuthRequestResolver);
//            });
//        });
//
//        http.oauth2Login(o2lc -> {
//            o2lc.userInfoEndpoint(customizer -> {
//                customizer.userAuthoritiesMapper(userAuthoritiesMapper());
//            });
//        });
//        http.logout(lc -> {
//            lc.addLogoutHandler(loh);
//        });
//
//        return http.build();
//    }

//	@Bean
//    Converter<Jwt, List<SimpleGrantedAuthority>> authoritiesConverter(@Value("${fr.insee.keycloak.resource}") String clientId) {
//        return jwt -> {
//        	System.out.println("§§§§§§§");
//        	
//            final var resourceAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", Map.of());
//            final var clientAccess = (Map<String, Object>) resourceAccess.getOrDefault(clientId, Map.of());
//            final var clientRoles = (List<String>) clientAccess.getOrDefault("roles", List.of());
//            
//            // Concat more claims if you need roles for other clients or realm ones
//            return clientRoles.stream().map(SimpleGrantedAuthority::new).toList();
//        };
//    }
//	
//	@Bean
//    Converter<Jwt, JwtAuthenticationToken> authenticationConverter(Converter<Jwt, ? extends Collection<? extends GrantedAuthority>> authoritiesConverter) {
//        return jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt), jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME));
//    }

//	@Bean
//	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//		http.csrf().disable(); // NOSONAR disable csrf because of API mode
//		http.authorizeRequests() //
//				.requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.toString())).permitAll()
//				.requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
//				.requestMatchers(new AntPathRequestMatcher("/execute/**")).fullyAuthenticated().and().oauth2Login()
//				.tokenEndpoint().accessTokenResponseClient(null);
//		return http.build();
//	}

//	@Bean
//	SecurityFilterChain securityFilterChain(
//            HttpSecurity http,
//            Converter<Jwt, ? extends AbstractAuthenticationToken> authenticationConverter)
//            throws Exception {
////		if (!isKeycloakActive()) {
////			http.authorizeRequests().requestMatchers(new AntPathRequestMatcher("/**")).permitAll();
////		} else {
//		
//        http.oauth2ResourceServer(resourceServer -> 
//        resourceServer.jwt(
//        		jwtResourceServer -> {jwtResourceServer
//        		.decoder(jwtDecoder())
//        		.jwkSetUri(keycloakServer + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs")
//        		.jwtAuthenticationConverter(authenticationConverter);
//        		System.out.println(keycloakServer + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs");
//        		}
//        		)
//        
//        		);
//        
//		
//		http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//		
//			http.csrf().disable(); // NOSONAR disable csrf because of API mode
//			http.authorizeRequests() //
//			.requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.toString())).permitAll()
//			.requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
//			.requestMatchers(new AntPathRequestMatcher("/execute/**")).fullyAuthenticated()
//			;
//			
//			
//			
////			http.oauth2ResourceServer(o -> o.jwt(j-> 
////					j.jwkSetUri(keycloakServer + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs")
////					.jwtAuthenticationConverter(jwtAuthenticationConverterForKeycloak())
////					.decoder(jwtDecoder())
////					))
////			;
////		}
//		
//		return http.build();
//		
//	}

//	public JwtDecoder jwtDecoder() {
//		System.out.println(keycloakServer + "/realms/" + keycloakRealm);
//		return JwtDecoders.fromIssuerLocation(keycloakServer + "/realms/" + keycloakRealm);
//	}
//
//	public JwtAuthenticationConverter jwtAuthenticationConverterForKeycloak() {
//		Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
//
//			System.out.println("$$$$$$$$$$$$$$");
//
//			Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
//
//			Object client = resourceAccess.get("demo-client");
//
//			LinkedTreeMap<String, List<String>> clientRoleMap = (LinkedTreeMap<String, List<String>>) client;
//
//			List<String> clientRoles = new ArrayList<>(clientRoleMap.get("roles"));
//
//			return clientRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
//		};
//
//		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//
//		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
//
//		return jwtAuthenticationConverter;
//	}

	/**
	 * Redirect using 307 instead of 302 to avoid unwanted post => get redirects and
	 * loss of the post body.
	 */
//	private SecureChannelProcessor httpsRedirect307() {
//		final RedirectStrategy redirectStrategy = new RedirectStrategy() {
//			@Override
//			public void sendRedirect(HttpServletRequest request, HttpServletResponse response,
//					String url) throws IOException {
//				response.addHeader("Location", response.encodeRedirectURL(url));
//				response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
//				response.flushBuffer();
//			}
//		};
//
//		final RetryWithHttpsEntryPoint entryPoint = new RetryWithHttpsEntryPoint();
//		entryPoint.setRedirectStrategy(redirectStrategy);
//
//		final SecureChannelProcessor secureChannelProcessor = new SecureChannelProcessor();
//		secureChannelProcessor.setEntryPoint(entryPoint);
//		return secureChannelProcessor;
//	}

//	
//	
//	@Override
//	public void configure(WebSecurity web) throws Exception {
//		
//		// Temporary fix to allow post on http without keycloak on
//		if (!isKeycloakActive()) {
//			web.ignoring().antMatchers("/**");
//		}
//		else
//		{
//			// Do not apply Spring Security to the other webservice
//			web.ignoring().antMatchers("/webservice/**");
//		}
//	}
//	
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//
//		if (!isKeycloakActive()) {
//			http.authorizeRequests().antMatchers("/**").permitAll();
//		} else {
//			String[] authorizedRoles = properties.getAuthorizedRoles();
//
//			super.configure(http);
//			http.authenticationProvider(keycloakAuthenticationProvider())
//			// Force https
//			.requiresChannel().anyRequest().requiresSecure().channelProcessors(Arrays.asList(httpsRedirect307()))
//			.and()
//			.csrf().disable()
//			// Keycloak filter
//			.addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class).addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class).exceptionHandling()
//			.authenticationEntryPoint(authenticationEntryPoint())
//			.and()
//			// Public
//			.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
//			.antMatchers(HttpMethod.GET, "/healthcheck").permitAll()
//			.antMatchers(HttpMethod.GET, "/version").permitAll()
//			.antMatchers(HttpMethod.GET, "/hello").permitAll()
//			// Semi-public
//			.antMatchers(HttpMethod.GET, "/testLoggers").authenticated();
//			// Restricted
//			if (authorizedRoles.length == 0) {
//				http.authorizeRequests().antMatchers(HttpMethod.GET, "/generateSandbox").authenticated()
//				.antMatchers(HttpMethod.POST, "/execute/service/**").authenticated()
//				.antMatchers(HttpMethod.POST, "/execute/engine/**").authenticated();
//			} else {
//				http.authorizeRequests().antMatchers(HttpMethod.GET, "/generateSandbox").hasAnyAuthority(authorizedRoles)
//				.antMatchers(HttpMethod.POST, "/execute/service/**").hasAnyAuthority(authorizedRoles)
//				.antMatchers(HttpMethod.POST, "/execute/engine/**").hasAnyAuthority(authorizedRoles);
//			}
//			http.authorizeRequests().anyRequest().denyAll();
//		}
//	}
//
//
//	/** Redirect using 307 instead of 302 to avoid unwanted post => get redirects
//	 *  and loss of the post body.*/
//	private SecureChannelProcessor httpsRedirect307() {
//		final RedirectStrategy redirectStrategy = new RedirectStrategy() {
//			@Override
//			public void sendRedirect(HttpServletRequest request, HttpServletResponse response,
//					String url) throws IOException {
//				response.addHeader("Location", response.encodeRedirectURL(url));
//				response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
//				response.flushBuffer();
//			}
//		};
//
//		final RetryWithHttpsEntryPoint entryPoint = new RetryWithHttpsEntryPoint();
//		entryPoint.setRedirectStrategy(redirectStrategy);
//
//		final SecureChannelProcessor secureChannelProcessor = new SecureChannelProcessor();
//		secureChannelProcessor.setEntryPoint(entryPoint);
//		return secureChannelProcessor;
//	}

}
