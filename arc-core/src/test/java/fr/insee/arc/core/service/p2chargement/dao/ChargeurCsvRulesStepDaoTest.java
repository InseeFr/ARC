package fr.insee.arc.core.service.p2chargement.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.p2chargement.bo.CSVFormatRules;
import fr.insee.arc.core.service.p2chargement.operation.ParseFormatRulesOperation;

class ChargeurCsvRulesStepDaoTest {

	@Test
	void dispatchColumnSimpleExpressionByStepTest() {
		
		String formatRules = """
				v_depot=substring(v_col,1,32)
				v_step2=v_step1$new2$||v_depot$new$
				v_step1=v_depot$new$||'step1'
				v_fichier=substring(v_vol,40,10)
				<where>v_step2$new3$=1
				<where>v_depot$new$=1
				""";
		
		ParseFormatRulesOperation<CSVFormatRules> parseCSV = new ParseFormatRulesOperation<>(formatRules, CSVFormatRules.class);
		parseCSV.parseFormatRules();
		
		ChargeurCsvRulesStepDao z = new ChargeurCsvRulesStepDao(parseCSV,"source_table");
		
		z.queryColumnExpression(0, "AND false");
		
		// there are 3 execution steps
		assertEquals(3, z.getDispatchColumnExpressionByStep().keySet().size());
		
		// in step 0, var#0 (v_depot) and var#3 (v_fichier) will be computed
		assertEquals(Arrays.asList(0, 3), z.getDispatchColumnExpressionByStep().get(0));

		// in step 1, var#2 (v_step1) will be computed
		assertEquals(Arrays.asList(2), z.getDispatchColumnExpressionByStep().get(1));
		
		// in step 2, var#1 (v_step2) will be computed
		assertEquals(Arrays.asList(1), z.getDispatchColumnExpressionByStep().get(2));

		// columns name
		assertEquals(Arrays.asList("v_depot","v_fichier","v_step1", "v_step2"), z.getColumns());

		// columns renamed during calculation by step
		assertEquals(Arrays.asList("v_depot$new$","v_fichier$new$","v_step1$new2$", "v_step2$new3$"), z.getColumnsRenamed());
		
	}
	
}
