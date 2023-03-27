package fr.insee.arc.core.service.handler;

import java.sql.Connection;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ParallelInsert extends Thread {

	private Connection connexion;
	private String query;

	
	
	public ParallelInsert(Connection connexion, String query) {
		super();
		this.connexion = connexion;
		this.query = query;
	}



	@Override
	public void run()
	{
		try {
			UtilitaireDao.get("arc").executeImmediate(this.connexion, query);
		} catch (ArcException e) {
			e.printStackTrace();
		}
	}



	public Connection getConnexion() {
		return connexion;
	}



	public void setConnexion(Connection connexion) {
		this.connexion = connexion;
	}



	public String getQuery() {
		return query;
	}



	public void setQuery(String query) {
		this.query = query;
	}

	
}
