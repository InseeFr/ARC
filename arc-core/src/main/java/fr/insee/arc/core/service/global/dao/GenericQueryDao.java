package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class GenericQueryDao {

	public GenericQueryDao(Connection connection) {
		super();
		this.connection = connection;
	}

	private Connection connection;
	private ArcPreparedStatementBuilder query;
	
	public void initialize()
	{
		query = new ArcPreparedStatementBuilder();
	}
	
	public void addOperation(ArcPreparedStatementBuilder newQuery)
	{
		query.append(newQuery);
	}
	
	public void addOperation(String newQuery)
	{
		query.append(new ArcPreparedStatementBuilder(newQuery));
	}
	
	public void addOperation(StringBuilder newQuery)
	{
		query.append(new ArcPreparedStatementBuilder(newQuery));
	}
	
	
	public void executeAsTransaction() throws ArcException
	{
        UtilitaireDao.get(0).executeBlock(this.connection, query.getQueryWithParameters());
        initialize();
	}
	
}
