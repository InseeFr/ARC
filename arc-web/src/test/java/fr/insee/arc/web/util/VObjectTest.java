package fr.insee.arc.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.arc.web.gui.all.util.TableObject;
import fr.insee.arc.web.gui.all.util.VObject;

public class VObjectTest {
	
	private VObject defaultTestVobject;
	private List<List<String>> defaultContent;
	
	@BeforeEach
	public void before() {
		this.defaultTestVobject = new VObject();
		List<String> headersDLabel = new ArrayList<>();
		headersDLabel.add("first_field");
		headersDLabel.add("second_field");
		this.defaultTestVobject.setHeadersDLabel(headersDLabel);
		List<String> headersDType = new ArrayList<>();
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
		assertEquals(defaultTestVobject.listContent(),defaultContent);
	}

	@Test
	public void mapContentOk() {
		assertEquals(defaultTestVobject.mapContent().get("first_field").get(1), 
				"first_field_value2");
		assertEquals(defaultTestVobject.mapContent().get("second_field").get(0), 
				"second_field_value1");
	}

	@Test
	public void listContentBeforeUpdateOk() {
		List<List<String>> newContent = new ArrayList<>();
		newContent.add(new ArrayList<>());
		newContent.get(0).add("first_field_new");
		newContent.get(0).add("second_field_value1");
		newContent.add(new ArrayList<>());
		newContent.get(1).add("first_field_value2");
		newContent.get(1).add("second_field_value2");
		this.defaultTestVobject.setContent(TableObject.as(newContent));
		assertEquals(defaultTestVobject.listContentBeforeUpdate().get(0).get(0), "first_field_value1");
		assertEquals(defaultTestVobject.listContentBeforeUpdate().size(), 1);
	}

	@Test
	public void listContentUpdatedContentOk() {
		List<List<String>> newContent = new ArrayList<>();
		newContent.add(new ArrayList<>());
		newContent.get(0).add(null);
		newContent.get(0).add(null);
		newContent.add(new ArrayList<>());
		newContent.get(1).add("first_field_new");
		newContent.get(1).add("second_field_value2");
		this.defaultTestVobject.setContent(TableObject.as(newContent));
		assertEquals(defaultTestVobject.listUpdatedContent().get(0).get(0), "first_field_value1");
		assertEquals(defaultTestVobject.listUpdatedContent().get(0).get(1), "second_field_value1");
		assertEquals(defaultTestVobject.listUpdatedContent().get(1).get(0), "first_field_new");
		assertEquals(defaultTestVobject.listUpdatedContent().get(1).get(1), "second_field_value2");
		assertEquals(defaultTestVobject.listUpdatedContent().size(), 2);
	}
	
	@Test
	public void listContentOnlyUpdatedContentOk() {
		List<List<String>> newContent = new ArrayList<>();
		newContent.add(new ArrayList<>());
		newContent.get(0).add(null);
		newContent.get(0).add(null);
		newContent.add(new ArrayList<>());
		newContent.get(1).add("first_field_new");
		newContent.get(1).add("second_field_value2");
		this.defaultContent.add(new ArrayList<String>());
		this.defaultContent.get(1).add("first_field_value3");
		this.defaultContent.get(1).add("second_field_value3");
		this.defaultTestVobject.setSavedContent(TableObject.as(this.defaultContent));
		this.defaultTestVobject.setContent(TableObject.as(newContent));
		assertEquals(defaultTestVobject.listOnlyUpdatedContent().get(0).get(0), "first_field_new");
		assertEquals(defaultTestVobject.listOnlyUpdatedContent().get(0).get(1), "second_field_value2");
		assertEquals(defaultTestVobject.listOnlyUpdatedContent().size(), 1);
	}

	@Test
	public void listContentUpdatedOk() {
		List<List<String>> newContent = new ArrayList<>();
		newContent.add(new ArrayList<>());
		newContent.get(0).add(null);
		newContent.get(0).add(null);
		newContent.add(new ArrayList<>());
		newContent.get(1).add("first_field_new");
		newContent.get(1).add("second_field_value2");
		this.defaultTestVobject.setContent(TableObject.as(newContent));
		assertEquals(defaultTestVobject.listContentAfterUpdate().get(0).get(0), "first_field_new");
		assertEquals(defaultTestVobject.listContentAfterUpdate().size(), 1);
	}

	@Test
	public void listContentSelectedOk() {
		List<Boolean> selectedLines = new ArrayList<>();
		selectedLines.add(null);
		selectedLines.add(true);
		this.defaultTestVobject.setSelectedLines(selectedLines);
		assertEquals(defaultTestVobject.listContentSelected().get(0).get(0), "first_field_value2");
		assertEquals(defaultTestVobject.listContentSelected().size(), 1);
	}

	@Test
	public void listHeadersSelectedOk() {
		List<Boolean> selectedColumns = new ArrayList<>();
		selectedColumns.add(null);
		selectedColumns.add(true);
		this.defaultTestVobject.setSelectedColumns(selectedColumns);
		assertEquals(defaultTestVobject.listHeadersSelected().get(0), "second_field");
		assertEquals(defaultTestVobject.listHeadersSelected().size(), 1);
	}

}
