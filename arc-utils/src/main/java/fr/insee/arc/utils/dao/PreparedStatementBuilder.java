package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.List;

public class PreparedStatementBuilder {

private StringBuilder query=new StringBuilder();
	
private List<String> parameters=new ArrayList<String>();

public PreparedStatementBuilder(String query) {
	super();
	this.query.append(query);
}

public PreparedStatementBuilder(StringBuilder query) {
	super();
	this.query = query;
}

public String quoteText(String s)
{
	parameters.add(s);
	return "?";
}

public List<String> getParameters() {
	return parameters;
}

public PreparedStatementBuilder append(String s)
{
	query.append(s);
	return this;
}

public PreparedStatementBuilder append(StringBuilder s)
{
	query.append(s);
	return this;
}

@Override
public String toString() {
	return query.toString();
}


}
