package fr.insee.arc.core.service.api.query;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
		gb.append("SELECT "+ServiceDate.queryDateConversion(testDate)+" as test_date");
		
		HashMap<String, ArrayList<String>> content= new GenericBean(UtilitaireDao.get("arc").executeRequest(c, gb)).mapContent();

		assertEquals("1975-12-16", content.get("test_date").get(0));
		
		
	}

}
