package fr.insee.arc.web.gui.query.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class ModelQuery implements ArcModel {

	private VObject viewQuery;
	private VObject viewTable;

	private String myQuery;
	private String mySchema;
	
	public ModelQuery() {
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
