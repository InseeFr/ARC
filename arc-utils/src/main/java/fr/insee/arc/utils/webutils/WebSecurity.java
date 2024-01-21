package fr.insee.arc.utils.webutils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

public class WebSecurity {

	@Value("${fr.insee.keycloak.realm}")
	private String keycloakRealm;

	@Value("${fr.insee.keycloak.server}")
	private String keycloakServer;

	@Value("${fr.insee.keycloak.resource}")
	private String keycloakResource;

	@Value("${fr.insee.keycloak.credentials.secret}")
	private String keycloakCredential;

	
	protected void setKeycloak(String keycloakRealm, String keycloakServer, String keycloakResource,
			String keycloakCredential) {
		this.keycloakRealm = keycloakRealm;
		this.keycloakServer = keycloakServer;
		this.keycloakResource = keycloakResource;
		this.keycloakCredential = keycloakCredential;
	}

	protected ClientRegistration keycloakClientRegistration(ClientAuthenticationMethod method) {

		String realmUri = keycloakServer + "/realms/" + keycloakRealm;
		String openIdConnect = "/protocol/openid-connect";

		return ClientRegistration //
				.withRegistrationId(keycloakRealm) //
				.clientId(keycloakResource) //
				.clientSecret(keycloakCredential) //
				.redirectUri("{baseUrl}" + "/login/oauth2/code/" + "{registrationId}") //
				.clientAuthenticationMethod(method) //
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) //
				.issuerUri(realmUri) //
				.scope("openid","profile","email","roles")
				.authorizationUri(realmUri + openIdConnect + "/auth") //
				.tokenUri(realmUri + openIdConnect + "/token") //
				.userInfoUri(realmUri + openIdConnect + "/userinfo") //
				.jwkSetUri(realmUri + openIdConnect + "/certs") //
				.userNameAttributeName("preferred_username") //
				.build();
	}
	
	
	protected boolean isKeycloakActive()
	{
		return keycloakRealm!=null;
	}
	
	protected GrantedAuthoritiesMapper userAuthoritiesMapper() {
	    return (authorities) -> {
	    	
	    	System.out.println("§§§§§§§§§§§§");
	      System.out.println("authorities check");
	      System.out.println(authorities);

	      Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

	      authorities.forEach(
	          authority -> {
	            if (authority instanceof OidcUserAuthority) {
	              OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
	              OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
	              
	              System.out.println("§§§§§§§§§§§§");
	              System.out.println(oidcUserAuthority);
	              System.out.println(userInfo.getFullName());
	              System.out.println(userInfo.getClaims());

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

	
	public String getKeycloakRealm() {
		return keycloakRealm;
	}

	public void setKeycloakRealm(String keycloakRealm) {
		this.keycloakRealm = keycloakRealm;
	}

	public String getKeycloakServer() {
		return keycloakServer;
	}

	public void setKeycloakServer(String keycloakServer) {
		this.keycloakServer = keycloakServer;
	}

	public String getKeycloakResource() {
		return keycloakResource;
	}

	public void setKeycloakResource(String keycloakResource) {
		this.keycloakResource = keycloakResource;
	}

	public String getKeycloakCredential() {
		return keycloakCredential;
	}

	public void setKeycloakCredential(String keycloakCredential) {
		this.keycloakCredential = keycloakCredential;
	}


}
