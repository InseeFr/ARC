package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.utils.exception.ArcException;

public class PreparedStatementBuilder {

private StringBuilder query=new StringBuilder();
	
private List<String> parameters=new ArrayList<>();

private static final String BIND_VARIABLE_PLACEHOLDER="  ?  ";

public PreparedStatementBuilder() {
	super();
}


public PreparedStatementBuilder(String query) {
	super();
	this.query.append(query);
}

public PreparedStatementBuilder(StringBuilder query) {
	super();
	this.query = query;
}

public PreparedStatementBuilder append(SQL s)
{
	query.append(s.toString());
	return this;
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

/**
 * Add transaction command to the prepared statement builder
 * @return
 */
public PreparedStatementBuilder asTransaction()
{
	query.insert(0, SQL.BEGIN.toString());
	query.append(SQL.END.toString());
	return this;
}

@Override
public String toString() {
	throw new ArcException("ToString is not allowed for PreparedStatementBuilder");
}


public PreparedStatementBuilder append(PreparedStatementBuilder s)
{
	query.append(s.query);
	parameters.addAll(s.parameters);
	return this;
}


public int length() {
	return query.length();
}


public void setLength(int i) {
	query.setLength(i);
}

/**
 * build the sql expression for equality
 * register the bind variable if the value is not null
 * @param val
 * @param type
 * @return
 */
public String sqlEqual(String val, String type) {
    if (val == null) {
        return " is null ";
    } else {
        return " = " + quoteText(val) + " ::" + type+" ";
    }
}


/**
 * Append a SQL bind variable to query
 * @param p
 * @return
 */
public PreparedStatementBuilder appendQuoteText(String s)
{
	this.append(quoteText(s));
	return this;
}

/**
 * Register and return the SQL bind variable placeholder
 * @param p
 * @return
 */
public String quoteText(String s)
{
	parameters.add(s);
	return BIND_VARIABLE_PLACEHOLDER;
}


/**
 * Return the sql escaped quoted string
 * The bind variable is not registered
 * @param p
 * @return
 */
public String quoteTextWithoutBinding(String p)
{
	return p==null?"NULL":"'"+p.replace("'", "''")+"'";
}

/**
 * return ?,?,? and add the elements of the list as parameters
 * @param liste
 * @return
 */
public StringBuilder sqlListeOfValues(Collection<String> liste)
{
	return sqlListeOfValues(liste, "","");
}

/**
 * convert a javalist of value into a SQL expression and register the bind variables
 * @param liste
 * @param openingBrace
 * @param closingBrace
 * @return
 */
public StringBuilder sqlListeOfValues(Collection<String> liste, String openingBrace, String closingBrace)
{
	StringBuilder requete=new StringBuilder();
	
	boolean first=true;
	for (String s:liste)
	{
		if (first)
		{
			first=false;
		}
		else
		{
			requete.append(",");
		}
		requete.append(openingBrace);
		requete.append(quoteText(s));
		requete.append(closingBrace);
	}
	
	return requete;
}

/**
 * convert a javalist of headers into SQL expression
 * @param liste
 * @return
 */
public StringBuilder sqlListeOfColumns(Collection<String> liste)
{
	return new StringBuilder(String.join(",", liste));
}



// getters

public List<String> getParameters() {
	return parameters;
}


public StringBuilder getQuery() {
	return query;
}


public void setQuery(StringBuilder query) {
	this.query = query;
}


public void setParameters(List<String> parameters) {
	this.parameters = parameters;
}

public String getQueryWithParameters() {
	String q=this.query.toString();
	
	for (String p : this.parameters)
	{
		q = StringUtils.replaceOnce(q ,BIND_VARIABLE_PLACEHOLDER,quoteTextWithoutBinding(p));
	}
	
	return q;
}


}
