package fr.insee.arc.ws.services.rest.generic.pojo;

public class QueryPojo {
	
	// numéro de requete de sortie
	public String query_id;
	
	// label à donner à la requete de sortie
	public String query_name;
	
	// expression sql de la requete
	public String expression;

	// rendu de la reuqtee de sortie : LINE ou COLUMN
	public String query_view;

	
	
	public QueryPojo(String query_id, String query_name, String expression, String query_view) {
		super();
		this.query_id = query_id;
		this.query_name = query_name;
		this.expression = expression;
		this.query_view = query_view;
	}



	public String getQuery_name() {
		return query_name;
	}

	public void setQuery_name(String query_name) {
		this.query_name = query_name;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}



	public String getQuery_id() {
		return query_id;
	}



	public void setQuery_id(String query_id) {
		this.query_id = query_id;
	}



	public String getQuery_view() {
		return query_view;
	}



	public void setQuery_view(String query_view) {
		this.query_view = query_view;
	}

	
	
}
