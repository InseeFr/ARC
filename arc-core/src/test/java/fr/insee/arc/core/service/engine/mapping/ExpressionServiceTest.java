package fr.insee.arc.core.service.engine.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.insee.arc.utils.structure.GenericBean;

public class ExpressionServiceTest {

	private ExpressionService expressionService;
	private ArrayList<String> headers;
	private ArrayList<String> types;

	@Before
	public void setUp() {
		expressionService = new ExpressionService();
		headers = new ArrayList<>();
		headers.add("expr_nom");
		headers.add("expr_valeur");
		types = new ArrayList<>();
		types.add("text");
		types.add("text");
	}
	
	//loopInExpressionSet
	
	@Test
	public void noLoopInExpressionSet() {
		List<String> names = new ArrayList<>();
		List<String> values = new ArrayList<>();
		names.add("abra");
		values.add("cadabra");
		names.add("cadabra");
		values.add("abra");
		assertTrue(expressionService.loopInExpressionSet(names, values).isEmpty());
	}

	@Test
	public void stillNoLoopInExpressionSet() {
		List<String> names = new ArrayList<>();
		List<String> values = new ArrayList<>();
		names.add("abra");
		values.add("@cadabra@");
		names.add("cadabra");
		values.add("abra");
		assertTrue(expressionService.loopInExpressionSet(names, values).isEmpty());
	}

	@Test
	public void loopInExpressionSet() {
		List<String> names = new ArrayList<>();
		List<String> values = new ArrayList<>();
		names.add("abra");
		values.add("@cadabra@");
		names.add("cadabra");
		values.add("@abra@");
		assertEquals("@abra@->@cadabra@->@abra@", expressionService.loopInExpressionSet(names, values).get());
	}

	@Test
	public void distantloopInExpressionSet() {
		List<String> names = new ArrayList<>();
		List<String> values = new ArrayList<>();
		names.add("abra");
		values.add("@cadabra@");
		names.add("abr");
		values.add("@abra@cabra");
		names.add("cadabra");
		values.add("ab@raca@bra");
		names.add("raca");
		values.add("@abr@acabra");
		assertEquals("@abra@->@cadabra@->@raca@->@abr@->@abra@", expressionService.loopInExpressionSet(names, values).get());
	}
	
	//applyTo
	
	@Test
	public void noSubstitution() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("wolf");
		content.get(0).add("dog");
		assertEquals("the cat", 
				expressionService.applyTo("the cat", new GenericBean(headers, types, content)));
	}

	@Test
	public void noSubstitutionWithArobase() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("wolf");
		content.get(0).add("dog");
		assertEquals("the @cat@ and the @duck@", 
				expressionService.applyTo("the @cat@ and the @duck@", new GenericBean(headers, types, content)));
	}

	@Test
	public void simpleSubstitution() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("wolf");
		content.get(0).add("dog");
		assertEquals("the dog", 
				expressionService.applyTo("the @wolf@", new GenericBean(headers, types, content)));
	}

	@Test
	public void twoSeparateSubstitutions() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("wolf");
		content.get(0).add("dog");
		content.add(new ArrayList<String>());
		content.get(1).add("lion");
		content.get(1).add("cat");
		assertEquals("the cat and the dog", 
				expressionService.applyTo("the @lion@ and the @wolf@", new GenericBean(headers, types, content)));
	}
	
	@Test
	public void noWrongArobaseSubstitutions() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("wolf");
		content.get(0).add("dog");
		content.add(new ArrayList<String>());
		content.get(1).add("lion");
		content.get(1).add("cat");
		content.add(new ArrayList<String>());
		content.get(2).add("andthe");
		content.get(2).add("orthe");
		assertEquals("thecatandthedog", 
				expressionService.applyTo("the@lion@andthe@wolf@", new GenericBean(headers, types, content)));
	}

	@Test
	public void recursiveSubstitution() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("animals");
		content.get(0).add("@lion@ and the @wolf@");
		content.add(new ArrayList<String>());
		content.get(1).add("wolf");
		content.get(1).add("dog");
		content.add(new ArrayList<String>());
		content.get(2).add("lion");
		content.get(2).add("cat");
		assertEquals("the cat and the dog", 
				expressionService.applyTo("the @animals@", new GenericBean(headers, types, content)));
	}

	@Test
	public void recursiveSubstitutionAnyOrder() {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		content.add(new ArrayList<String>());
		content.get(0).add("wolf");
		content.get(0).add("dog");
		content.add(new ArrayList<String>());
		content.get(1).add("lion");
		content.get(1).add("cat");
		content.add(new ArrayList<String>());
		content.get(2).add("animals");
		content.get(2).add("@lion@ and the @wolf@");
		assertEquals("the cat and the dog", 
				expressionService.applyTo("the @animals@", new GenericBean(headers, types, content)));
	}
}
