package fr.insee.arc.core.service.p2chargement.xmlhandler;

import java.sql.Connection;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ParallelInsert extends Thread {

	private Connection connexion;
	private ArcPreparedStatementBuilder query;
	private ArcException threadException;

	public ParallelInsert(Connection connexion, ArcPreparedStatementBuilder query) {
		super();
		this.connexion = connexion;
		this.query = query;
	}

	@Override
	public void run() {
		try {
			UtilitaireDao.get(0).executeRequest(this.connexion, query);
		} catch (ArcException e) {
			this.threadException = e;
		}
	}
	
    
	public void waitAndReport() throws ArcException
	{
		try {
			this.join();
		} catch (InterruptedException e) {
			this.interrupt();
			throw new ArcException(ArcExceptionMessage.LOAD_PARALLEL_INSERT_THREAD_FAILED);
		}
				
		if (this.getThreadException()!=null)
		{
			throw new ArcException(ArcExceptionMessage.LOAD_PARALLEL_INSERT_THREAD_FAILED);
		}
	}

	public Connection getConnexion() {
		return connexion;
	}

	public void setConnexion(Connection connexion) {
		this.connexion = connexion;
	}

	public void setQuery(ArcPreparedStatementBuilder query) {
		this.query = query;
	}

	public ArcException getThreadException() {
		return threadException;
	}

}
