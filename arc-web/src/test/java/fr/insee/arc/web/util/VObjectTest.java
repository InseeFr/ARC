package fr.insee.arc.web.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class VObjectTest {
	
	private VObject defaultTestVobject;
	private ArrayList<ArrayList<String>> defaultContent;
	
	@Before
	public void before() {
		this.defaultTestVobject = new VObject();
		ArrayList<String> headersDLabel = new ArrayList<>();
		headersDLabel.add("first_field");
		headersDLabel.add("second_field");
		this.defaultTestVobject.setHeadersDLabel(headersDLabel);
		ArrayList<String> headersDType = new ArrayList<>();
		headersDType.add("text");
		headersDType.add("date");
		this.defaultTestVobject.setHeadersDType(headersDType);
		this.defaultContent = new ArrayList<>();
		this.defaultContent.add(new ArrayList<String>());
		this.defaultContent.get(0).add("first_field_value1");
		this.defaultContent.get(0).add("second_field_value1");
		this.defaultContent.add(new ArrayList<String>());
		this.defaultContent.get(1).add("first_field_value2");
		this.defaultContent.get(1).add("second_field_value2");
		this.defaultTestVobject.setSavedContent(TableObject.as(this.defaultContent));
	}

	@Test
	public void listContentOk() {
		assertThat(defaultTestVobject.listContent(), equalTo(defaultContent));
	}

	@Test
	public void mapContentOk() {
		assertThat(defaultTestVobject.mapContent().get("first_field").get(1), 
				equalTo("first_field_value2"));
		assertThat(defaultTestVobject.mapContent().get("second_field").get(0), 
				equalTo("second_field_value1"));
	}

	@Test
	public void listContentBeforeUpdateOk() {
		ArrayList<ArrayList<String>> newContent = new ArrayList<>();
		newContent.add(new ArrayList<>());
		newContent.get(0).add("first_field_new");
		newContent.get(0).add("second_field_value1");
		newContent.add(new ArrayList<>());
		newContent.get(1).add("first_field_value2");
		newContent.get(1).add("second_field_value2");
		this.defaultTestVobject.setContent(TableObject.as(newContent));
		assertThat(defaultTestVobject.listContentBeforeUpdate().get(0).get(0), equalTo("first_field_value1"));
		assertThat(defaultTestVobject.listContentBeforeUpdate().size(), equalTo(1));
	}

	@Test
	public void listContentAfterUpdateOk() {
		ArrayList<ArrayList<String>> newContent = new ArrayList<>();
		newContent.add(new ArrayList<>());
		newContent.get(0).add(null);
		newContent.get(0).add(null);
		newContent.add(new ArrayList<>());
		newContent.get(1).add("first_field_new");
		newContent.get(1).add("second_field_value2");
		this.defaultTestVobject.setContent(TableObject.as(newContent));
		assertThat(defaultTestVobject.listContentAfterUpdate().get(0).get(0), equalTo("first_field_new"));
		assertThat(defaultTestVobject.listContentAfterUpdate().size(), equalTo(1));
	}

	@Test
	public void listContentSelectedOk() {
		ArrayList<Boolean> selectedLines = new ArrayList<>();
		selectedLines.add(null);
		selectedLines.add(true);
		this.defaultTestVobject.setSelectedLines(selectedLines);
		assertThat(defaultTestVobject.listContentSelected().get(0).get(0), equalTo("first_field_value2"));
		assertThat(defaultTestVobject.listContentSelected().size(), equalTo(1));
	}

	@Test
	public void listHeadersSelectedOk() {
		ArrayList<Boolean> selectedColumns = new ArrayList<>();
		selectedColumns.add(null);
		selectedColumns.add(true);
		this.defaultTestVobject.setSelectedColumns(selectedColumns);
		assertThat(defaultTestVobject.listHeadersSelected().get(0), equalTo("second_field"));
		assertThat(defaultTestVobject.listHeadersSelected().size(), equalTo(1));
	}

}
