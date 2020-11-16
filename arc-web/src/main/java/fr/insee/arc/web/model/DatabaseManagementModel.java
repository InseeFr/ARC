package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewQuery;
import fr.insee.arc.web.model.viewobjects.ViewTable;
import fr.insee.arc.web.util.VObject;

public class DatabaseManagementModel implements ArcModel {

	private VObject viewQuery;
	private VObject viewTable;

	private String myQuery;
	private String mySchema;
	
	public DatabaseManagementModel() {
		this.viewQuery = new ViewQuery();
		this.viewTable = new ViewTable();
	}
	
	public VObject getViewQuery() {
		return viewQuery;
	}
	public void setViewQuery(VObject viewQuery) {
		this.viewQuery = viewQuery;
	}
	public VObject getViewTable() {
		return viewTable;
	}
	public void setViewTable(VObject viewTable) {
		this.viewTable = viewTable;
	}

	public String getMyQuery() {
		return myQuery;
	}

	public void setMyQuery(String myQuery) {
		this.myQuery = myQuery;
	}

	public String getMySchema() {
		return mySchema;
	}

	public void setMySchema(String mySchema) {
		this.mySchema = mySchema;
	}
	
}
