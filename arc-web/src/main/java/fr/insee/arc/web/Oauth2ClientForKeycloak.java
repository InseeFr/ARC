package fr.insee.arc.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class Oauth2ClientForKeycloak {

	private static final String SCOPE_OPENID = "openid";
	private static final String SCOPE_PROFILE = "profile";
	private static final String SCOPE_EMAIL = "email";
	private static final String SCOPE_ROLES = "roles";
	private static final String[] SCOPES = new String[] { SCOPE_OPENID, SCOPE_PROFILE, SCOPE_EMAIL, SCOPE_ROLES };

	private static final String CLAIM_ROLES = "roles";
	private static final String CLAIM_GROUPS = "groups";
	private static final String CLAIM_REALM_ACCESS = "realm_access";

	public static final String CLAIM_USERNAME = "preferred_username";
	public static final String CLAIM_USERNAME_PREFIX = "@";

	/**
	 * Configure the oauth2 client to access correclty the oauth2 entry points of
	 * the keycloak server.
	 * The list of the oauth2 entry points of the keycloak
	 * server are shown in the "realm-settings" menu > "OpenID Endpoint Configuration"
	 * of the keycloak admin console
	 * 
	 * @param method
	 * @param properties
	 * @return
	 */
	protected ClientRegistration keycloakClientRegistration(ClientAuthenticationMethod method,
			PropertiesHandler properties) {

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
				.userNameAttributeName(CLAIM_USERNAME) //
				.build();
	}

	/**
	 * Retrieve from the oauth2 client the security group of the user
	 * 
	 * @return
	 */
	protected GrantedAuthoritiesMapper userAuthoritiesMapper() {
		return authorities -> {
			Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

			authorities.forEach(authority -> {
				if (authority instanceof OidcUserAuthority oidcUserAuthority) {

					OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

					@SuppressWarnings("unchecked")
					List<String> roles = (List<String>) ObjectUtils.firstNonNull(
							userInfo.getClaimAsStringList(CLAIM_ROLES), userInfo.getClaimAsStringList(CLAIM_GROUPS),
							userInfo.getClaimAsMap(CLAIM_REALM_ACCESS) == null ? null
									: userInfo.getClaimAsMap(CLAIM_REALM_ACCESS).get(CLAIM_ROLES));
					List<SimpleGrantedAuthority> groupAuthorities = roles.stream().map(SimpleGrantedAuthority::new)
							.toList();
					mappedAuthorities.addAll(groupAuthorities);
				}
			});

			return mappedAuthorities;
		};
	}

	/**
	 * Check if user is allowed to access debugging gui User name returned by oauth2
	 * is matched with the users declared
	 * 
	 * @return
	 */
	protected AuthorizationManager<RequestAuthorizationContext> debugGuiAccessAtUserLevel() {
		return (auth, o) -> new AuthorizationDecision(authorizeDebugGuiDecision(auth.get().getName()));
	}

	/**
	 * When fr.insee.gui.debug.disable property is declared with the format
	 * "ARC_SECURITY_GROUPE_NAME@preferedUser1@preferedUser2" then check if the user
	 * name returned by oauth2 is found in the fr.insee.gui.debug.disable property
	 * 
	 * When fr.insee.gui.debug.disable property is declared with the format
	 * "ARC_SECURITY_GROUPE_NAME" then user matching is not required to allow access
	 * 
	 * @param debugGuiSecurityGroupProperties
	 * @param preferedUser
	 * @return true if access is granted
	 */
	protected static boolean authorizeDebugGuiDecision(String preferedUser) {
		String debugGuiSecurityGroupProperties = PropertiesHandler.getInstance().getDisableDebugGui();

		if (debugGuiSecurityGroupProperties.contains(CLAIM_USERNAME_PREFIX)) {

			// add @ at the end to catch last user correctly
			debugGuiSecurityGroupProperties = debugGuiSecurityGroupProperties + CLAIM_USERNAME_PREFIX;

			// if @user_name@ is found in debugGuiSecurityGroup
			return debugGuiSecurityGroupProperties
					.contains(CLAIM_USERNAME_PREFIX + preferedUser + CLAIM_USERNAME_PREFIX);
		}

		return true;
	}

	/**
	 * Return the name of security group declared in the fr.insee.gui.debug.disable
	 * property
	 * 
	 * @return
	 */
	protected static String getDebugGuiAccessSecurityGroupName() {
		return StringUtils.tokenizeToStringArray(PropertiesHandler.getInstance().getDisableDebugGui(),
				CLAIM_USERNAME_PREFIX)[0];
	}

}
