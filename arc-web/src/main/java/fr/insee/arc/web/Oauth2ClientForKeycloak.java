package fr.insee.arc.web;

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

import fr.insee.arc.utils.webutils.WebAttributesName;

public class Oauth2ClientForKeycloak {
	
	@Value(WebAttributesName.KEYCLOAK_ATTRIBUTE_REALM)
	protected String keycloakRealm;

	@Value(WebAttributesName.KEYCLOAK_ATTRIBUTE_SERVER)
	private String keycloakServer;

	@Value(WebAttributesName.KEYCLOAK_ATTRIBUTE_RESOURCE)
	private String keycloakResource;

	@Value(WebAttributesName.KEYCLOAK_ATTRIBUTE_CREDENTIALS)
	private String keycloakCredential;

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
	
	protected GrantedAuthoritiesMapper userAuthoritiesMapper() {
	    return (authorities) -> {
	      Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

	      authorities.forEach(
	          authority -> {
	            if (authority instanceof OidcUserAuthority) {
	              OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
	              OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

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
