package fr.insee.arc.web;

import static org.springframework.security.config.Customizer.withDefaults;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

//	@Bean
//	public WebSecurityCustomizer webSecurityCustomizer() {
//	    DefaultHttpFirewall firewall = new DefaultHttpFirewall();
//	    return (web) -> web.httpFirewall(firewall);
//	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("xx").roles("ADMIN").build();
		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {

		http.authorizeRequests().requestMatchers("/", "/home", "/logged").permitAll()
		.requestMatchers("/index").authenticated().and().httpBasic();

//		http.authorizeHttpRequests(ex -> 
//		ex.requestMatchers("/", "/home").permitAll()
//		.requestMatchers("/index").hasAnyRole("ADMIN")
//		);
//				.authorizeHttpRequests().
//				.anyRequest().hasRole("ADMIN")).httpBasic(withDefaults());
		return http.build();
	}

//	   private String clientId="arc";
//	    private String clientSecret = "LD5IFLCQFFvN3gvg7emTbfkrFL1bleCE";
//
//	    @Bean
//	    public ClientRegistrationRepository clientRegistrationRepository() {
//	        return new InMemoryClientRegistrationRepository(this.keycloakClientRegistration());
//	    }
//
////	    /oauth2/authorization/agent-insee-interne
//	    
//	    private ClientRegistration keycloakClientRegistration() {
//	        return ClientRegistration.withRegistrationId("agent-insee-interne")
//	                .clientId(clientId)
//	                .clientSecret(clientSecret)
//	                .redirectUri("{baseUrl}"+"/"+"logged")
//	                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//	                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//	        		.issuerUri("http://localhost:18080/realms/agent-insee-interne")
//	        		.scope("openid", "profile")
////	                .scope("openid", "profile", "email", "address", "phone")
////	                .clientName("arc")
//	                .authorizationUri("http://localhost:18080/realms/agent-insee-interne/protocol/openid-connect/auth")
//	                .tokenUri("http://localhost:18080/realms/agent-insee-interne/protocol/openid-connect/token")
//	        		.userInfoUri( "http://localhost:18080/realms/agent-insee-interne/protocol/openid-connect/userinfo")
//	                .jwkSetUri("http://localhost:18080/realms/agent-insee-interne/protocol/openid-connect/certs")
//	                .build();
//	    }
//	    
//		@Bean
//	    SecurityFilterChain clientSecurityFilterChain(HttpSecurity http)
//	            throws Exception {
//			http.authorizeRequests()
//			.requestMatchers("/", "/home", "/logged")
//	        .permitAll()
//	        .requestMatchers("/index")
//	        .authenticated()
//	        .and()
//	        .oauth2Login()
//	        .and()
//	        .httpBasic();
//	        return http.build();
//	    }    

//	   @Bean
//	    SecurityFilterChain clientSecurityFilterChain(HttpSecurity http)
//	            throws Exception {
//
//	        http.addFilterBefore(new LoginPageFilter(), DefaultLoginPageGeneratingFilter.class);
//	        http.oauth2Login(withDefaults());
//	        http.csrf(AbstractHttpConfigurer::disable);
//	        http.cors(cors -> {
//	                cors.configurationSource(corsConfigurationSource());
//	        });
//	        http.authorizeHttpRequests(ex -> ex
//	                        .requestMatchers("/", "/logged/**", "/oauth2/**").permitAll()
//	                        .anyRequest().authenticated());
//	        return http.build();
//	    }
//
//
//	    private CorsConfigurationSource corsConfigurationSource() {
//	        // Very permissive CORS config...
//	        final var configuration = new CorsConfiguration();
//	        configuration.setAllowedOrigins(Arrays.asList("*"));
//	        configuration.setAllowedMethods(Arrays.asList("*"));
//	        configuration.setAllowedHeaders(Arrays.asList("*"));
//	        configuration.setExposedHeaders(Arrays.asList("*"));
//
//	        // Limited to API routes (neither actuator nor Swagger-UI)
//	        final var source = new UrlBasedCorsConfigurationSource();
//	        source.registerCorsConfiguration("/arc-web/**", configuration);
//
//	        return source;
//	    }
//
//	    static class LoginPageFilter extends GenericFilterBean {
//	        @Override
//	        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//	            final var auth = SecurityContextHolder.getContext().getAuthentication();
//	            if (auth != null
//	                    && auth.isAuthenticated()
//	                    && !(auth instanceof AnonymousAuthenticationToken)
//	                    && ((HttpServletRequest) request).getRequestURI().equals("/logged")) {
//	                ((HttpServletResponse) response).sendRedirect("/");
//	            }
//	            chain.doFilter(request, response);
//	        }
//	    }
//
//	    @Bean
//	    GrantedAuthoritiesMapper userAuthoritiesMapper() {
//	        return (authorities) -> {
//	            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
//
//	            authorities.forEach(authority -> {
//	                if (authority instanceof OidcUserAuthority oidcAuth) {
//	                    oidcAuth.getIdToken().getClaimAsStringList("roles")
//	                            .forEach(a -> mappedAuthorities.add(new SimpleGrantedAuthority(a)));
//	                } else if (authority instanceof OAuth2UserAuthority oauth2Auth) {
//	                    ((List<String>) oauth2Auth.getAttributes().getOrDefault("roles", List.of()))
//	                            .forEach(a -> mappedAuthorities.add(new SimpleGrantedAuthority(a)));
//
//	                }
//	            });
//
//	            return mappedAuthorities;
//	        };
//	    }

	// .requestMatchers(new AntPathRequestMatcher("/"))

//	@Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//		http
//		.securityMatcher("/", "/oauth2/authorization/**")
//		.authorizeHttpRequests(r-> r.anyRequest().permitAll())
//		.securityMatcher("/index", "/selectNorme")
//		.authorizeHttpRequests(r-> {
//			try {
//				r.anyRequest().authenticated().and().oauth2Login();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		})
//		
//		;

//		   http.authorizeRequests()
//		   .requestMatchers(new AntPathRequestMatcher("/"), new AntPathRequestMatcher("/oauth2/authorization/**"))
//           .permitAll()
//           .anyRequest()
//           .authenticated()
//           .and()
//           .oauth2Login();

//		   return http.build();

//		http
//		.securityMatcher("/index/**")                                   
//		.authorizeHttpRequests(authorize -> {
//			try {
//				authorize
//					.anyRequest().authenticated().and().oauth2Login((oauth2Login) -> oauth2Login
//							.userInfoEndpoint((userInfo) -> userInfo
//									.userAuthoritiesMapper(grantedAuthoritiesMapper())
//								)
//							);
//			} catch (Exception e) {
//			}
//		}
//		);
//		return  http.build();
//    }
//	
//	 static class LoginPageFilter extends GenericFilterBean {
//	        @Override
//	        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//	            final var auth = SecurityContextHolder.getContext().getAuthentication();
//	            if (auth != null
//	                    && auth.isAuthenticated()
//	                    && !(auth instanceof AnonymousAuthenticationToken)
//	                    && ((HttpServletRequest) request).getRequestURI().equals("/login")) {
//	                ((HttpServletResponse) response).sendRedirect("/");
//	            }
//	            chain.doFilter(request, response);
//	        }
//	    }
//
//
//	
//	private GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
//		return (authorities) -> {
//			Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
//
//			authorities.forEach((authority) -> {
//				GrantedAuthority mappedAuthority;
//
//				if (authority instanceof OidcUserAuthority) {
//					OidcUserAuthority userAuthority = (OidcUserAuthority) authority;
//					mappedAuthority = new OidcUserAuthority(
//						"OIDC_USER", userAuthority.getIdToken(), userAuthority.getUserInfo());
//					System.out.println("§§§§§§§");
//					System.out.println(userAuthority.getIdToken());
//					
//					
//				} else if (authority instanceof OAuth2UserAuthority) {
//					OAuth2UserAuthority userAuthority = (OAuth2UserAuthority) authority;
//					mappedAuthority = new OAuth2UserAuthority(
//						"OAUTH2_USER", userAuthority.getAttributes());
//					
//					System.out.println("§§§§§§§");
//					System.out.println(userAuthority.getAttributes());
//					
//				} else {
//					mappedAuthority = authority;
//				}
//
//				mappedAuthorities.add(mappedAuthority);
//			});
//
//			return mappedAuthorities;
//		};
//	}

//	@Bean
//    @Order(2)                                                        
//    public SecurityFilterChain securityFilterChain2(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity.authorizeHttpRequests().requestMatchers("/index").authenticated().and().oauth2Login()
//      ;
//       return  httpSecurity.build();
//    }
//	

	//
//	
//	@Bean
//	public JwtDecoder jwtDecoder() {
//		return JwtDecoders.fromIssuerLocation("http://localhost:18080");
//	}

//    @Bean
//	@Order(1)                                                        
//	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
//
//    	
//		   http.authorizeRequests()
//		   .requestMatchers("/", "/home", "/logged")
//        .permitAll()
//        .requestMatchers("/index")
//        .authenticated()
//        .and()
//        .oauth2Login()
//        .and()
//        .httpBasic();
//    	
//    	
//    	return http.build();

//		 http.authorizeRequests()
//		   .requestMatchers(new AntPathRequestMatcher("/"), new AntPathRequestMatcher("/oauth2/authorization/**"))
//        .permitAll()
//        .anyRequest()
//        .authenticated()
//        .and()
//        .oauth2Login();

//    	http
//    	.authorizeRequests()
//    	.requestMatchers("/", "/home" ).permitAll().and()
//    	.authorizeRequests()
//    	.requestMatchers("/index").hasAnyAuthority("ADMIN").and().httpBasic();	
//    	return http.build();

//    	http
//		.securityMatcher("/**")                                   
//		.authorizeHttpRequests(authorize -> authorize
//			.anyRequest().permitAll()
//		)
//		.httpBasic(withDefaults());
//	return http.build();

//    	http.authorizeHttpRequests(ex -> ex
//                .requestMatchers("/", "public/**", "/public/**").permitAll()
//                .and().anyRequest().hasRole("ADMIN")
//                ).httpBasic(withDefaults());
//    	return http.build();

//    	http
//    	.securityMatcher("public/**")
//    	.authorizeHttpRequests(authorize -> authorize
//		.anyRequest().permitAll()
//	).httpBasic(withDefaults());
//    	return http.build();

//    	http.securityMatcher("/").authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
//    	.securityMatcher("/index").authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("ADMIN"))
//    	.httpBasic(withDefaults());

//    	http.securityMatcher("/selectNorme").authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("ADMIN"))
//    	.httpBasic(withDefaults());

//    	http.authorizeRequests().requestMatchers(new AntPathRequestMatcher("/")).permitAll();
//    	http.authorizeRequests().requestMatchers(new AntPathRequestMatcher("/**")).hasAnyAuthority("ADMIN");
//    	
//		http
//			.securityMatcher("/**")                                   
//			.authorizeHttpRequests(authorize -> authorize
//				.anyRequest().permitAll()
//			)
//			.httpBasic(withDefaults());
//		return http.build();
//	}

//	
//	@Bean
//	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf().requireCsrfProtectionMatcher(keycloakCsrfRequestMatcher())
//                .and()
//                .sessionManagement()
//                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
//                .and()
//                .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
//                .addFilterBefore(keycloakAuthenticationProcessingFilter(), LogoutFilter.class)
//                .addFilterAfter(keycloakSecurityContextRequestFilter(), SecurityContextHolderAwareRequestFilter.class)
//                .addFilterAfter(keycloakAuthenticatedActionsRequestFilter(), KeycloakSecurityContextRequestFilter.class)
//                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
//                .and()
//                .logout()
//                .addLogoutHandler(keycloakLogoutHandler())
//                .logoutUrl("/sso/logout").permitAll()
//                .logoutSuccessUrl("/");
//        http
//		.securityMatcher("/index/**")                                   
//		.authorizeHttpRequests(authorize -> authorize
//			.anyRequest().authenticated()
//		)
//		.httpBasic(withDefaults());
//        
//        return http.build();
//    }
//
//
//	
//	@Override
//	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
//		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
//	}
//    

}