package fr.insee.arc.core.service.global.dao;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class ServiceDateTest extends InitializeQueryTest {

	@Test
	public void queryDateConversion() throws ArcException, ParseException {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date testDate = format.parse("16/12/1975");
		
		GenericPreparedStatementBuilder gb=new GenericPreparedStatementBuilder();
		gb.append("SELECT "+DateConversion.queryDateConversion(testDate)+" as test_date");
		
		Map<String, List<String>> content= new GenericBean(UtilitaireDao.get(0).executeRequest(c, gb)).mapContent();

		assertEquals("1975-12-16", content.get("test_date").get(0));
		
		
	}

}
