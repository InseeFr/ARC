package fr.insee.arc.core.service.engine.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import fr.insee.arc.core.service.p5mapping.bo.TableMapping;
import fr.insee.arc.core.service.p5mapping.bo.VariableMapping;
import fr.insee.arc.core.service.p5mapping.bo.rules.RegleMappingClePrimaire;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueries;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueriesFactory;

public class RequeteMappingTest {
	
	private MappingQueries mappingRequest;
	
	@Before
	public void setUp() {
		MappingQueriesFactory mappingRuleFactory = new MappingQueriesFactory(null, "", null, null);
		mappingRequest = new MappingQueries(null, mappingRuleFactory, "", null, "", "", 0);
	}

	
	// ordonnerTraitementTableRecursive()
	
	@Test
	public void mappingOrderOneTable() {
		Set<TableMapping> tables = new HashSet<>();
		tables.add(new TableMapping("", "table1"));
		mappingRequest.setEnsembleTableMapping(tables);

		Map<TableMapping, List<TableMapping>> result = mappingRequest.ordonnerTraitementTable();

		assertTrue(result.isEmpty());
	}
	
	@Test
	public void mappingOrderParentAndChild() {

		VariableMapping idParent = new VariableMapping(null, "id_parent", "");
		idParent.setExpressionRegle(new RegleMappingClePrimaire("", "", idParent));
		VariableMapping idChild = new VariableMapping(null, "id_child", "");
		idChild.setExpressionRegle(new RegleMappingClePrimaire("", "", idChild));
		
		Set<TableMapping> tables = new HashSet<>();
		
		TableMapping parent = new TableMapping("", "parent");
		parent.ajouterVariable(idParent);
		parent.construireEnsembleVariablesTypes();
		tables.add(parent);
		
		TableMapping child = new TableMapping("", "child");
		child.ajouterVariable(idParent);
		child.ajouterVariable(idChild);
		child.construireEnsembleVariablesTypes();
		tables.add(child);
		
		mappingRequest.setEnsembleTableMapping(tables);

		Map<TableMapping, List<TableMapping>> result = mappingRequest.ordonnerTraitementTable();

		assertEquals(1, result.size());
		assertEquals(1, result.get(parent).size());
		assertSame(child, result.get(parent).get(0));
	}

}
