package fr.insee.arc.core.service.kubernetes.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.insee.arc.core.service.kubernetes.bo.AccessToken;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildAccessToken {

	private BuildAccessToken() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Retrieve token from pod. If fails, use token set in properties
	 * @return
	 * @throws ArcException 
	 */
	public static AccessToken retrieve() throws ArcException
	{
		PropertiesHandler properties = PropertiesHandler.getInstance();
		
		// token can be retrieve in pod
		// kubectl exec <pod_name> -t cat /var/run/secrets/kubernetes.io/serviceaccount/token
		String token;
		try {
			token = new String(Files.readAllBytes(Paths.get(properties.getKubernetesApiTokenPath())),
							StandardCharsets.UTF_8);
		} catch (IOException e) {
			token = properties.getKubernetesApiTokenValue();
		}
		
		if (token==null) {
			throw new ArcException(ArcExceptionMessage.ACCESS_TOKEN_NOT_EXIST);
		}
		
		return new AccessToken(token);
		
	}
	
}
