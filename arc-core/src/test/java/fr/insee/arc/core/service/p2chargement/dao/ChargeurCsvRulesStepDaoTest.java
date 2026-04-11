package fr.insee.arc.core.service.p2chargement.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.p2chargement.bo.CSVFormatRules;
import fr.insee.arc.core.service.p2chargement.operation.ParseFormatRulesOperation;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

class ChargeurCsvRulesStepDaoTest extends InitializeQueryTest {

	
	@BeforeAll
	public static void setup() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build("CREATE TEMPORARY TABLE source_table AS ");
		query.build("SELECT 1 as id, 'DATA-DATA-DATA' as v_col ");
		query.build(";");
		
		u.executeRequest(c, query);
	}
	
	@Test
	void dispatchColumnsAndFiltersExpressionByStepTest() throws ArcException {
		
		String formatRules = """
				v_depot=id+1
				v_step1=v_depot$new$+1
				v_fichier=substring(v_col,1,4)
				<where>v_depot$new$%2=0
				v_step2$new2$=v_depot$new$-1
				<where>v_step2$new3$=1
				""";
		
		ParseFormatRulesOperation<CSVFormatRules> parseCSV = new ParseFormatRulesOperation<>(formatRules, CSVFormatRules.class);
		parseCSV.parseFormatRules();
		
		ChargeurCsvRulesStepDao z = new ChargeurCsvRulesStepDao(parseCSV,"source_table");
		
		ArcPreparedStatementBuilder query = z.queryColumnExpression(0, "AND true");
	
		// there are 3 execution steps
		assertEquals(3, z.getDispatchColumnExpressionByStep().keySet().size());
		
		// in step 0, var#0 (v_depot) and var#3 (v_fichier) will be computed
		assertEquals(Arrays.asList(0, 2), z.getDispatchColumnExpressionByStep().get(0));

		// in step 1, var#2 (v_step1) will be computed
		assertEquals(Arrays.asList(1), z.getDispatchColumnExpressionByStep().get(1));
		
		// in step 2, var#1 (v_step2) will be computed
		assertEquals(Arrays.asList(3), z.getDispatchColumnExpressionByStep().get(2));

		// columns name
		assertEquals(Arrays.asList("v_depot","v_fichier","v_step1", "v_step2"), z.getColumns());

		// columns renamed during calculation by step
		assertEquals(Arrays.asList("v_depot$new$","v_fichier$new$","v_step1$new2$", "v_step2$new3$"), z.getColumnsRenamed());

		GenericBean gb = new GenericBean(u.executeRequest(c, query));
		
		assertEquals(Arrays.asList("id", "v_col", "v_depot$new$","v_fichier$new$","v_step1$new2$", "v_step2$new3$"), gb.getHeaders());
		assertEquals(Arrays.asList("1", "DATA-DATA-DATA", "2", "DATA", "3", "1"), gb.getContent().get(0));
		
	}
	
	@Test
	void dispatchColumnsOnlyExpressionByStepTest() throws ArcException {
		
		String formatRules = 
				"""
				<encoding>UTF-8</encoding>
				<headers>col</headers>
				<quote>E'\2'</quote>
				<where>id%2=1
				""";
		
		ParseFormatRulesOperation<CSVFormatRules> parseCSV = new ParseFormatRulesOperation<>(formatRules, CSVFormatRules.class);
		parseCSV.parseFormatRules();
		
		ChargeurCsvRulesStepDao z = new ChargeurCsvRulesStepDao(parseCSV,"source_table");
		
		// check that generated query is correct
		ArcPreparedStatementBuilder query = z.queryColumnExpression(0, null);
		GenericBean gb = new GenericBean(u.executeRequest(c, query));

		// no headers added as just filter rules
		assertEquals(Arrays.asList("id", "v_col"), gb.getHeaders());
		assertEquals(1, gb.getContent().size());

	}
	
	@Test
	void dispatchFilterOnlyExpressionByStepTest() throws ArcException {
		
		String formatRules = 
				"""
				<encoding>WINDOWS1252</encoding>
				<headers>col</headers>
				<quote>E'\2'</quote>
				i_date_ref=null::int
				v_date_ref=substring(v_col,1,4)
				/* i_tt=substring(v_vol,40,10) */
				""";
		
		ParseFormatRulesOperation<CSVFormatRules> parseCSV = new ParseFormatRulesOperation<>(formatRules, CSVFormatRules.class);
		parseCSV.parseFormatRules();
		
		ChargeurCsvRulesStepDao z = new ChargeurCsvRulesStepDao(parseCSV,"source_table");
		
		ArcPreparedStatementBuilder query = z.queryColumnExpression(0, null);
		GenericBean gb = new GenericBean(u.executeRequest(c, query));
		assertEquals(Arrays.asList("id", "v_col", "i_date_ref$new$", "v_date_ref$new$"), gb.getHeaders());


	}
	
	@Test
	void renameVariableTest()
	{
		ChargeurCsvRulesStepDao z = new ChargeurCsvRulesStepDao(null,"source_table");

		assertEquals("",z.variableSuffixForStep(0));
		assertEquals("$new$",z.variableSuffixForStep(1));
		assertEquals("$new5$",z.variableSuffixForStep(5));
		
	}
	
	@Test
	void columnRawExpressionTest()
	{
		assertEquals("v_date_ref=substring(v_col,1,4)", CSVFormatRules.columnRawExpression("v_date_ref", "substring(v_col,1,4)"));
	}
	
}
