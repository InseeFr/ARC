package fr.insee.arc.core.service.p2chargement.operation;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.CSVFormatRules;
import fr.insee.arc.core.service.p2chargement.bo.IdCardChargement;
import fr.insee.arc.core.service.p2chargement.factory.TypeChargement;

public class ParseFormatRulesOperationTest {

	@Test
	public void testParsing() {
		FileIdCard n = new FileIdCard("my_file");
		String formatRules = "<encoding>WIN1252</encoding>\r\n" + "<headers>col</headers>\r\n"
				+ "<quote>E'\2'</quote>\r\n"
				+ "i_00=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('00','10','21','30','31','36','40','50','52','60') then (select max(v.id) from A v where v.id<=u.id and (case length(rtrim(substring(v.v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v.v_col,31,2) end)='00') end\r\n"
				+ "i_10=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('10','21','30','31','36','40','50','52','60') then (select max(v.id) from A v where v.id<=u.id and (case length(rtrim(substring(v.v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v.v_col,31,2) end)='10') end\r\n"
				+ "i_21=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('21','30','31','36','40','50','52','60') then (select max(v.id) from A v where v.id<=u.id and (case length(rtrim(substring(v.v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v.v_col,31,2) end)='21') end\r\n"
				+ "i_30=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('30') then id end\r\n"
				+ "i_31=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('31') then id end\r\n"
				+ "i_36=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('36') then id end\r\n"
				+ "i_40=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('40') then id end\r\n"
				+ "i_50=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('50') then id end\r\n"
				+ "i_52 = case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('52') then id end\r\n"
				+ "i_60=case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('60') then id end\r\n"
				+ "i_col=null::int\r\n"
				+ "/* repere dans le code produit si c'est un dep, une commune ou un enregistrement */\r\n"
				+ "i_article=null::int\r\n"
				+ "v_article=case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end\r\n"
				+ "/* i_dep=dense_rank() over (order by substr(v_col,1,3)) */\r\n"
				+ "/* i_com=case when length(rtrim(substring(v_col,1,16)))>3 then dense_rank() over (order by substr(v_col,1,6)) end */\r\n"
				+ "/* i_logement=case when length(rtrim(substring(v_col,1,16)))>6 then dense_rank() over (order by substr(v_col,1,16)) end */\r\n"
				+ "<where>length(rtrim(substring(v_col,1,16))) != 3 and length(rtrim(substring(v_col,1,16))) != 6\r\n"
				+ "<index>(case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end),id\r\n";

		IdCardChargement r = new IdCardChargement(TypeChargement.PLAT, "E'\1'", formatRules);
		n.setIdCardChargement(r);

		ParseFormatRulesOperation<CSVFormatRules> parseCSV = new ParseFormatRulesOperation<>(n, CSVFormatRules.class);
		parseCSV.parseFormatRules();

		assertEquals("WIN1252", parseCSV.getValue(CSVFormatRules.ENCODING));
		assertEquals("col", parseCSV.getValue(CSVFormatRules.HEADERS));
		
		// crls must go
		assertEquals("i_50", parseCSV.getValues(CSVFormatRules.COLUMN_DEFINITION).get(7));
		assertEquals("case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('50') then id end", parseCSV.getValues(CSVFormatRules.COLUMN_EXPRESSION).get(7));
		
		// check that column=expression formula trims the column and the expression
		assertEquals("i_52", parseCSV.getValues(CSVFormatRules.COLUMN_DEFINITION).get(8));
		assertEquals("case when (case length(rtrim(substring(v_col,1,16))) when 3 then 'DEP' when 6 then 'COM' else substring(v_col,31,2) end) in ('52') then id end", parseCSV.getValues(CSVFormatRules.COLUMN_EXPRESSION).get(8));
		
		assertEquals(" repere dans le code produit si c'est un dep, une commune ou un enregistrement ", parseCSV.getValues(CSVFormatRules.COMMENT).get(0));
		
	}

}
