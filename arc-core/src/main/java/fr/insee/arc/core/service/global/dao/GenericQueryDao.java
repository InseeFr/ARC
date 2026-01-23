package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class GenericQueryDao {

	public GenericQueryDao(Connection connection) {
		super();
		this.connection = connection;
	}

	private Connection connection;
	private GenericPreparedStatementBuilder query;
	
	public GenericQueryDao initialize()
	{
		query = new GenericPreparedStatementBuilder();
		return this;
	}
	
	public GenericQueryDao addOperation(GenericPreparedStatementBuilder newQuery)
	{
		query.append(newQuery);
		return this;
	}
	
	public GenericQueryDao addOperation(String newQuery)
	{
		query.append(newQuery);
		return this;
	}
	
	public GenericQueryDao addOperation(StringBuilder newQuery)
	{
		query.append(newQuery);
		return this;
	}
	
	public void execute() throws ArcException {
		UtilitaireDao.get(0).executeRequest(connection, query);
	}	
	
	public void executeAsTransaction() throws ArcException
	{
        UtilitaireDao.get(0).executeRequest(this.connection, query.asTransaction());
	}

	public GenericPreparedStatementBuilder getQuery() {
		return query;
	}

	public void setQuery(GenericPreparedStatementBuilder query) {
		this.query = query;
	}
	
	
}
