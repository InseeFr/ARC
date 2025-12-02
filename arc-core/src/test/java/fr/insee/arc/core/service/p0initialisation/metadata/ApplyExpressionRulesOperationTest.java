package fr.insee.arc.core.service.p0initialisation.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplyExpressionRulesOperationTest {


	private ApplyExpressionRulesOperation expressionService;
	private ArrayList<String> headers;
	private ArrayList<String> types;

	@BeforeEach
	public void setUp() {
		expressionService = new ApplyExpressionRulesOperation();
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
		assertTrue(!expressionService.loopInExpressionSet(names, values).isPresent());
	}

	@Test
	public void stillNoLoopInExpressionSet() {
		List<String> names = new ArrayList<>();
		List<String> values = new ArrayList<>();
		names.add("abra");
		values.add("@cadabra@");
		names.add("cadabra");
		values.add("abra");
		assertTrue(!expressionService.loopInExpressionSet(names, values).isPresent());
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

}
