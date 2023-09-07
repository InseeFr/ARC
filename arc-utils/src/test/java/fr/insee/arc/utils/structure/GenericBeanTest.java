package fr.insee.arc.utils.structure;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class GenericBeanTest {

	private GenericBean genericBean;
	/** Defined by default as {"col1", "col2"}*/
	private ArrayList<String> headers;
	/** Defined by default as {"text", "text"}*/
	private ArrayList<String> types;
	
	@Before
	public void setUp() {
		headers = new ArrayList<>();
		headers.add("col1");
		headers.add("col2");
		types = new ArrayList<>();
		types.add("text");
		types.add("text");
	}
	
	@Test
	public void mapIndexOk() {
		genericBean = new GenericBean(headers, types, new ArrayList<ArrayList<String>>());
		HashMap<String, Integer> mapIndex = genericBean.mapIndex();
		assertEquals(0, mapIndex.get("col1").intValue());
		assertEquals(1, mapIndex.get("col2").intValue());
	}

	@Test
	public void mapTypes() {
		//with
		types = new ArrayList<>();
		types.add("text");
		types.add("int");
		//when
		genericBean = new GenericBean(headers, types, new ArrayList<ArrayList<String>>());
		HashMap<String, String> mapTypes = genericBean.mapTypes();
		//then
		assertEquals("text", mapTypes.get("col1"));
		assertEquals("int", mapTypes.get("col2"));
	}
	
	@Test
	public void mapContentOk() {
		//with
		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		content.add(new ArrayList<>());
		content.get(0).add("col1_val1");
		content.get(0).add("col2_val1");
		content.add(new ArrayList<>());
		content.get(1).add("col1_val2");
		content.get(1).add("col2_val2");
		//when
		genericBean = new GenericBean(headers, types, content);
		HashMap<String, ArrayList<String>> mapContent = genericBean.mapContent();
		//then
		assertEquals("col1_val1", mapContent.get("col1").get(0));
		assertEquals("col1_val2", mapContent.get("col1").get(1));
		assertEquals("col2_val1", mapContent.get("col2").get(0));
		assertEquals("col2_val2", mapContent.get("col2").get(1));
	}

	@Test
	public void mapContentEmpty() {
		genericBean = new GenericBean(headers, types, new ArrayList<ArrayList<String>>());
		assertTrue(genericBean.mapContent().isEmpty());
		assertNull(genericBean.mapContent().get("col"));
	}
	
	@Test
	public void empty() {
		//with
		genericBean = new GenericBean(headers, types, new ArrayList<ArrayList<String>>());
		//then
		assertTrue(genericBean.isEmpty());
	}
	
	@Test
	public void notEmpty() {
		//with
		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		content.add(new ArrayList<>());
		content.get(0).add("col1_val1");
		content.get(0).add("col2_val1");
		genericBean = new GenericBean(headers, types, content);
		//then
		assertTrue(!genericBean.isEmpty());
	}

	@Test
	public void reworkListAsContentTest() {
		
		// initalize a list a record value
		List<String> records = new ArrayList<String>(Arrays.asList("1","2","3"));
		
		// rework the list as a GenericBean content
		ArrayList<ArrayList<String>> content = GenericBean.reworkListAsContent(records);
		assertEquals("1", content.get(0).get(0));
		assertEquals("2", content.get(1).get(0));
		assertEquals("3", content.get(2).get(0));
		assertEquals(3, content.size());
		assertEquals(1, content.get(0).size());
		assertEquals(1, content.get(1).size());
		assertEquals(1, content.get(2).size());
		
		// test the constructor with signle column, single type and list of record
		genericBean= new GenericBean("id", "int", records);
		
		// a single column GenericBean must be set
		assertEquals(1, genericBean.mapContent().keySet().size());
		ArrayList<String> gbRecords= genericBean.mapContent().get("id");
		
		// we must retrieve exactly the list of records when quering the column name id
		assertEquals(records.size(), gbRecords.size());
		gbRecords.removeAll(records);
		assertEquals(0, gbRecords.size());

	}
	
}
