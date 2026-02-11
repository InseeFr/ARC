package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Strings;

import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class GenericPreparedStatementBuilder {

	private StringBuilder query = new StringBuilder();

	private List<Parameter<?>> parameters = new ArrayList<>();

	public static final String BIND_VARIABLE_PLACEHOLDER = "  ?  ";

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
			if (queryElement instanceof GenericPreparedStatementBuilder q)
			{
				this.append(q);
				continue;
			}
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
	 * append a bind variable "  ?  " to query and register parameter as String/text type
	 * @param s
	 * @return
	 */
	public GenericPreparedStatementBuilder appendText(String s) {
		query.append(this.quoteText(s));
		return this;
	}
	
	/**
	 * append a text without binding a variable
	 * @param s
	 * @return
	 */
	public GenericPreparedStatementBuilder appendTextWithoutBinding(String s) {
		query.append(FormatSQL.quoteText(s));
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
	 * Register and return the SQL bind variable placeholder
	 * 
	 * @param p
	 * @return
	 */
	public String quoteText(String s) {
		addText(s);
		return BIND_VARIABLE_PLACEHOLDER;
	}

	/**
	 * add a text parameter to current parameter list
	 * @param s
	 */
	public void addText(String s) {
		parameters.add(new Parameter<>(s, ParameterType.STRING));
	}

	
	/**
	 * Register and return the SQL bind variable placeholder
	 * 
	 * @param p
	 * @return
	 */
	public String quoteInt(Integer s) {
		addInt(s);
		return BIND_VARIABLE_PLACEHOLDER;
	}

	/**
	 * add an Integer parameter to current parameter list
	 * @param s
	 */
	public void addInt(Integer s) {
		parameters.add(new Parameter<>(s, ParameterType.INT));
	}

	/**
	 * Register and return the SQL bind variable placeholder
	 * 
	 * @param p
	 * @return
	 */
	public String quoteBytes(byte[] s) {
		addBytes(s);
		return BIND_VARIABLE_PLACEHOLDER;
	}
	
	/**
	 * add an bytes[] parameter to current parameter list
	 * @param s
	 */
	public void addBytes(byte[] s) {
		parameters.add(new Parameter<>(s, ParameterType.BYTES));
	}
	
	/**
	 * Return the sql escaped quoted string The bind variable is not registered
	 * 
	 * @param p
	 * @return
	 */
	public String quoteTextWithoutBinding(String p) {
		return FormatSQL.quoteText(p);
	}

	public String quoteNumberWithoutBinding(String p) {
		return p == null ? "NULL" : p;
	}

	
	
	// return a tuple of values (val1, val2, ... valn)
	public StringBuilder tupleOfValues(String...liste) {
		StringBuilder requete = new StringBuilder();
		requete.append("(").append(sqlListeOfValues(Arrays.asList(liste))).append(")");
		return requete;
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

	public StringBuilder tupleOfColumn(String...liste) {
		StringBuilder requete = new StringBuilder();
		requete.append("(").append(sqlListeOfColumns(Arrays.asList(liste))).append(")");
		return requete;
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
				q = Strings.CS.replaceOnce(q, BIND_VARIABLE_PLACEHOLDER, quoteNumberWithoutBinding(val));
			} else {
				q = Strings.CS.replaceOnce(q, BIND_VARIABLE_PLACEHOLDER, quoteTextWithoutBinding(val));
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
	 * Build the query to create the table structure
	 * by default, the target table will be destroyed
	 * @param tableName
	 * @param gb
	 * @return
	 */
	public GenericPreparedStatementBuilder createWithGenericBean(String tableName, GenericBean gb)
	{
		return createWithGenericBean(tableName, gb, true);
	}
	
	/**
	 * Generate a query that create the structure of table according to GenericBean headers and types
	 * @param tableName
	 * @param gb
	 * @return
	 */
	public GenericPreparedStatementBuilder createWithGenericBean(String tableName, GenericBean gb, boolean replaceTargetTable)
	{
		// drop target table if exists
		if (replaceTargetTable)
		{
			query.append(SQL.DROP).append(SQL.TABLE).append(SQL.IF_EXISTS).append(tableName).append(SQL.END_QUERY);
		}
		
		query.append(SQL.CREATE);
		
		if (FormatSQL.isTemporary(tableName)) {
			query.append(SQL.TEMPORARY);
		}

		query.append(SQL.TABLE).append(SQL.IF_NOT_EXISTS).append(tableName).append("(");

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
			
			// if chunk stop too high, limit it to the size of generic bean content
			chunkStop = (chunkStop > gb.getContent().size()) ? gb.getContent().size() : chunkStop;

			List<String> types = gb.getTypes();
		
			for (int i = chunkStart; i < chunkStop; i++) {
				List<String> line = gb.getContent().get(i);

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

			this.appendText(cell);
			if (!types.get(j).equals(TypeEnum.TEXT.getTypeName()))
			{
				query.append(SQL.CAST_OPERATOR);
				query.append(types.get(j));
			}
		}
		query.append(")");
	}
	
	public GenericPreparedStatementBuilder appendNewLine(Object s) {
		query.append(SQL.NEW_LINE);
		query.append(s);
		return this;
	}
	
}
