package fr.insee.arc.core.service.engine.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import fr.insee.arc.core.service.engine.mapping.regles.RegleMappingClePrimaire;

public class RequeteMappingTest {
	
	private RequeteMapping mappingRequest;
	
	@Before
	public void setUp() {
		RegleMappingFactory mappingRuleFactory = new RegleMappingFactory(null, "", null, null);
		mappingRequest = new RequeteMapping(null, mappingRuleFactory, "", null, "", "", 0);
	}

	
	// ordonnerTraitementTable()
	
	@Test
	public void mappingOrderOneTable() {
		Set<TableMapping> tables = new HashSet<>();
		tables.add(new TableMapping("", "table1", 0));
		mappingRequest.setEnsembleTableMapping(tables);

		HashMap<TableMapping, ArrayList<TableMapping>> result = mappingRequest.ordonnerTraitementTable();

		assertTrue(result.isEmpty());
	}
	
	@Test
	public void mappingOrderParentAndChild() {
		Set<TableMapping> tables = new HashSet<>();
		TableMapping parent = new TableMapping("", "parent", 0);
		tables.add(parent);
		TableMapping child = new TableMapping("", "child", 0);
		VariableMapping idParent = new VariableMapping(null, "id_parent", "");
		idParent.setExpressionRegle(new RegleMappingClePrimaire("", "", idParent));
		child.ajouterVariable(idParent);
		child.construireEnsembleVariablesTypes();
		tables.add(child);
		mappingRequest.setEnsembleTableMapping(tables);

		HashMap<TableMapping, ArrayList<TableMapping>> result = mappingRequest.ordonnerTraitementTable();

		assertEquals(1, result.size());
		assertEquals(1, result.get(parent).size());
		assertSame(child, result.get(parent).get(0));
	}

}
