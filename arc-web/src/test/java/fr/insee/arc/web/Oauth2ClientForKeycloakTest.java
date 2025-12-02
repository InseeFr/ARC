package fr.insee.arc.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class Oauth2ClientForKeycloakTest {

	@Test
	public void authorizeDebugGuiAccessTest() {
		
		PropertiesHandler ph=PropertiesHandler.getInstance();
		
		// single user
		ph.setDisableDebugGui("ARC_ADMIN@user1");

		// grant access only to user1 but not to user3
		assertTrue(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user1"));
		assertFalse(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user3"));
		
		// the security group name is ARC_ADMIN
		assertEquals("ARC_ADMIN", Oauth2ClientForKeycloak.getDebugGuiAccessSecurityGroupName());

		 
		// multi user
		ph.setDisableDebugGui("ARC_ADMIN@user1@user2");
		// grant access only to user1 but not to user3
		assertTrue(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user1"));
		assertTrue(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user2"));
		assertFalse(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user3"));
		
		
		// no user provided, grant access
		ph.setDisableDebugGui("ARC_SUPER_ADMIN");
		assertTrue(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user1"));
		assertTrue(Oauth2ClientForKeycloak.authorizeDebugGuiDecision("user3"));
		
		// the security group name is ARC_SUPER_ADMIN
		assertEquals("ARC_SUPER_ADMIN", Oauth2ClientForKeycloak.getDebugGuiAccessSecurityGroupName());
		
	}

}
