package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class GenericPreparedStatementBuilder {

	private StringBuilder query = new StringBuilder();

	private List<Parameter<?>> parameters = new ArrayList<>();

	private static final String BIND_VARIABLE_PLACEHOLDER = "  ?  ";

	public GenericPreparedStatementBuilder() {
		super();
	}

	public GenericPreparedStatementBuilder(String query) {
		super();
		this.query.append(query);
	}

	public GenericPreparedStatementBuilder(StringBuilder query) {
		super();
		this.query = query;
	}

	public GenericPreparedStatementBuilder build(Object... queryElements) {
		for (Object queryElement : queryElements) {
			query.append(queryElement);
		}
		return this;
	}

	public GenericPreparedStatementBuilder append(SQL s) {
		query.append(s.toString());
		return this;
	}

	public GenericPreparedStatementBuilder append(String s) {
		query.append(s);
		return this;
	}

	public GenericPreparedStatementBuilder append(int s) {
		query.append(Integer.toString(s));
		return this;
	}

	public GenericPreparedStatementBuilder append(StringBuilder s) {
		query.append(s);
		return this;
	}

	/**
	 * Add transaction command to the prepared statement builder
	 * 
	 * @return
	 */
	public GenericPreparedStatementBuilder asTransaction() {
		query.insert(0, SQL.BEGIN.toString());
		query.append(SQL.END.toString());
		return this;
	}

	@Override
	public String toString() {
		return getQueryWithParameters();
	}

	public GenericPreparedStatementBuilder append(GenericPreparedStatementBuilder s) {
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
	 * build the sql expression for equality register the bind variable if the value
	 * is not null
	 * 
	 * @param val
	 * @param type
	 * @return
	 */
	public String sqlEqual(String val, String type) {
		if (val == null) {
			return " is null ";
		} else {
			return " = " + quoteText(val) + " ::" + type + " ";
		}
	}

	/**
	 * Append a SQL bind variable to query
	 * 
	 * @param p
	 * @return
	 */
	public GenericPreparedStatementBuilder appendQuoteText(String s) {
		this.append(quoteText(s));
		return this;
	}

	/**
	 * Register and return the SQL bind variable placeholder
	 * 
	 * @param p
	 * @return
	 */
	public String quoteText(String s) {
		parameters.add(new Parameter<>(s, ParameterType.STRING));
		return BIND_VARIABLE_PLACEHOLDER;
	}

	/**
	 * Register and return the SQL bind variable placeholder
	 * 
	 * @param p
	 * @return
	 */
	public String quoteInt(Integer s) {
		parameters.add(new Parameter<>(s, ParameterType.INT));
		return BIND_VARIABLE_PLACEHOLDER;
	}

	/**
	 * Register and return the SQL bind variable placeholder
	 * 
	 * @param p
	 * @return
	 */
	public String quoteBytes(byte[] s) {
		parameters.add(new Parameter<>(s, ParameterType.BYTES));
		return BIND_VARIABLE_PLACEHOLDER;
	}
	
	/**
	 * Return the sql escaped quoted string The bind variable is not registered
	 * 
	 * @param p
	 * @return
	 */
	public String quoteTextWithoutBinding(String p) {
		return FormatSQL.textToSql(p);
	}

	public String quoteNumberWithoutBinding(String p) {
		return p == null ? "NULL" : p;
	}

	/**
	 * return ?,?,? and add the elements of the list as parameters
	 * 
	 * @param liste
	 * @return
	 */
	public StringBuilder sqlListeOfValues(Collection<String> liste) {
		return sqlListeOfValues(liste, "", "");
	}

	/**
	 * convert a javalist of value into a SQL expression and register the bind
	 * variables
	 * 
	 * @param liste
	 * @param openingBrace
	 * @param closingBrace
	 * @return
	 */
	public StringBuilder sqlListeOfValues(Collection<String> liste, String openingBrace, String closingBrace) {
		StringBuilder requete = new StringBuilder();

		boolean first = true;
		for (String s : liste) {
			if (first) {
				first = false;
			} else {
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
	 * 
	 * @param liste
	 * @return
	 */
	public StringBuilder sqlListeOfColumns(Collection<String> liste) {
		return new StringBuilder(String.join(",", liste));
	}

// getters

	public StringBuilder getQuery() {
		return query;
	}

	public void setQuery(StringBuilder query) {
		this.query = query;
	}

	public List<Parameter<?>> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter<?>> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Return the query with the real real parameters instead of bounded variables
	 * 
	 * @return
	 */
	public String getQueryWithParameters() {
		String q = this.query.toString();

		for (Parameter<?> p : this.parameters) {
			String val = (p.getValue() == null) ? null : ("" + p.getValue());

			if (Arrays.asList(ParameterType.INT).contains(p.getType())) {
				q = StringUtils.replaceOnce(q, BIND_VARIABLE_PLACEHOLDER, quoteNumberWithoutBinding(val));
			} else {
				q = StringUtils.replaceOnce(q, BIND_VARIABLE_PLACEHOLDER, quoteTextWithoutBinding(val));
			}
		}

		return q;
	}

	/**
	 * Return the query to copy a table by its value
	 * 
	 * @return
	 */
	public GenericPreparedStatementBuilder copyFromGenericBean(String tableName, GenericBean gb) {
		// create the table structure
		createWithGenericBean(tableName, gb);
		
		// insert
		insertWithGenericBeanByChunk(tableName, gb, 0, gb.getContent().size());

		return this;
	}

	/**
	 * Generate a query that create the strucutre of table according to GenericBean headers and types
	 * @param tableName
	 * @param gb
	 * @return
	 */
	public GenericPreparedStatementBuilder createWithGenericBean(String tableName, GenericBean gb)
	{
		// drop target table if exists
		query.append(SQL.DROP).append(SQL.TABLE).append(SQL.IF_EXISTS).append(tableName).append(SQL.END_QUERY);
		
		query.append(SQL.CREATE);
		
		if (FormatSQL.isTemporary(tableName)) {
			query.append(SQL.TEMPORARY);
		}

		query.append(SQL.TABLE).append(tableName).append("(");

		boolean first = true;
		for (int i = 0; i < gb.getHeaders().size(); i++) {
			if (first) {
				first = false;
			} else {
				append(",");
			}

			query.append(gb.getHeaders().get(i)).append(" ").append(gb.getTypes().get(i));
		}
		query.append(")").append(SQL.END_QUERY);
		return this;
	}
	
	/**
	 * Generate an insert query according to the content of a GenericBean
	 * @param tableName
	 * @param gb
	 */
	public GenericPreparedStatementBuilder insertWithGenericBeanByChunk(String tableName, GenericBean gb, int chunkStart, int chunkStop)
	{
		if (!gb.getContent().isEmpty()) {

			query.append(SQL.INSERT_INTO).append(tableName).append(SQL.VALUES);
			boolean firstLine = true;
			
			// if chunkstop too high, limit it to the size of generic bean content
			chunkStop = (chunkStop > gb.getContent().size()) ? gb.getContent().size() : chunkStop;

			ArrayList<String> types = gb.getTypes();
		
			for (int i = chunkStart; i < chunkStop; i++) {
				ArrayList<String> line = gb.getContent().get(i);

				if (firstLine) {
					firstLine = false;
				} else {
					query.append(",");
				}

				insertLine(types, line);

			}
			query.append(SQL.END_QUERY);
		}
		return this;
	}
	
	/**
	 * insert in the query the corresponding tuple of a given list of values 
	 * @param types
	 * @param line
	 */
	private void insertLine(List<String> types, List<String> line)
	{
		boolean firstCell = true;

		query.append("(");

		for (int j = 0; j < line.size(); j++) {

			String cell = line.get(j);

			if (firstCell) {
				firstCell = false;
			} else {
				query.append(",");
			}
			// cannot use bind variables here : potentially too many bounded values
			query.append(quoteTextWithoutBinding(cell));
			if (!types.get(j).equals(TypeEnum.TEXT.getTypeName()))
			{
				query.append(SQL.CAST_OPERATOR);
				query.append(types.get(j));
			}
		}
		query.append(")");
	}
	
}
