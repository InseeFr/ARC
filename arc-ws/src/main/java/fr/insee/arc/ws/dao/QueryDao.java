package fr.insee.arc.ws.dao;

import java.util.HashMap;
import java.util.List;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.actions.SendResponse;


public interface QueryDao {

		/**Se connecte à la base de données, execute la requête placée en paramètre.
		 * 
		 * @param id Id de la requête.
		 * @param resp Flux dans lequel on écrit la requête.
		 * @param timestamp Identifiant de la requête du client.
		 * @throws ArcException 
		 */
		void doRequest( String id, SendResponse resp, long timestamp ) throws ArcException;

		
		/**
		 * Crée les tables temporaires.
		 * 
		 * @param ids Les ids des requêtes permettent de déterminer le nom des tables temporaires.
		 * @param sqlRequests Les rqupetes sql dont nous voulons créer les images.
		 * @param sessionId Le session id du client permet de déterminer le nom des tables temporaires.
		 * @throws ArcException 
		 */
		void createImage( List<String> ids, HashMap<String, String> sqlRequests, long timestamp ) throws ArcException;
		
}
