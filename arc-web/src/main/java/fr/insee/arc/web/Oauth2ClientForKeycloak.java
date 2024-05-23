package fr.insee.arc.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class Oauth2ClientForKeycloak {

	private static final String SCOPE_OPENID = "openid";
	private static final String SCOPE_PROFILE = "profile";
	private static final String SCOPE_EMAIL = "email";
	private static final String SCOPE_ROLES = "roles";
	private static final String[] SCOPES = new String[] {SCOPE_OPENID, SCOPE_PROFILE, SCOPE_EMAIL, SCOPE_ROLES};
	
	private static final String CLAIM_ROLES = "roles";
	private static final String CLAIM_GROUPS = "groups";
	private static final String CLAIM_REALM_ACCESS = "realm_access";

	
	protected ClientRegistration keycloakClientRegistration(ClientAuthenticationMethod method, PropertiesHandler properties) {

		String realmUri = properties.getKeycloakServer() + "/realms/" + properties.getKeycloakRealm();
		String openIdConnect = "/protocol/openid-connect";

		return ClientRegistration //
				.withRegistrationId(properties.getKeycloakRealm()) //
				.clientId(properties.getKeycloakResource()) //
				.clientSecret(properties.getKeycloakCredential()) //
				.redirectUri("{baseUrl}" + "/login/oauth2/code/" + "{registrationId}") //
				.clientAuthenticationMethod(method) //
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) //
				.issuerUri(realmUri) //
				.scope(SCOPES).authorizationUri(realmUri + openIdConnect + "/auth") //
				.tokenUri(realmUri + openIdConnect + "/token") //
				.userInfoUri(realmUri + openIdConnect + "/userinfo") //
				.jwkSetUri(realmUri + openIdConnect + "/certs") //
				.userNameAttributeName("preferred_username") //
				.build();
	}

	protected GrantedAuthoritiesMapper userAuthoritiesMapper() {
		return (authorities) -> {
			Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

			authorities.forEach(authority -> {
				if (authority instanceof OidcUserAuthority oidcUserAuthority) {
					
					OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
					
					@SuppressWarnings("unchecked")
					List<String> roles = (List<String>) ObjectUtils.firstNonNull(
							userInfo.getClaimAsStringList(CLAIM_ROLES),
							userInfo.getClaimAsStringList(CLAIM_GROUPS),
							userInfo.getClaimAsMap(CLAIM_REALM_ACCESS)==null ? null : userInfo.getClaimAsMap(CLAIM_REALM_ACCESS).get(CLAIM_ROLES));

					List<SimpleGrantedAuthority> groupAuthorities = roles.stream()
							.map(SimpleGrantedAuthority::new).toList();
					mappedAuthorities.addAll(groupAuthorities);
				}
			});

			return mappedAuthorities;
		};
	}
	
	
	
}
