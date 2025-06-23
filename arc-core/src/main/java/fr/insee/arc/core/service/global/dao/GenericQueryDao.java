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
	
	public GenericQueryDao initialize()
	{
		query = new ArcPreparedStatementBuilder();
		return this;
	}
	
	public GenericQueryDao addOperation(ArcPreparedStatementBuilder newQuery)
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
	
	public void executeWithParameters() throws ArcException {
		UtilitaireDao.get(0).executeRequest(connection, query);
	}	
	
	public void executeAsTransaction() throws ArcException
	{
        UtilitaireDao.get(0).executeRequest(this.connection, query.asTransaction().getQueryWithParameters());
	}

	public ArcPreparedStatementBuilder getQuery() {
		return query;
	}

	public void setQuery(ArcPreparedStatementBuilder query) {
		this.query = query;
	}
	
	
}
