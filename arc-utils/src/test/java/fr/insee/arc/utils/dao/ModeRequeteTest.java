package fr.insee.arc.utils.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ModeRequeteTest {

@Test
public void untokenize1() {
	assertEquals("set enable_nestloop=off;set enable_hashagg=on;\n",ModeRequete.untokenize(ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON).getQueryWithParameters());
}

@Test
public void configureQuery1() {
	assertEquals("set enable_nestloop=off;set enable_hashagg=on;\n"
			,ModeRequete.configureQuery(null,ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON).getQueryWithParameters());
}


@Test
public void configureQuery2() {
	assertEquals("set enable_nestloop=off;set enable_hashagg=on;\n"
			,ModeRequete.configureQuery(new GenericPreparedStatementBuilder(),ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON).getQueryWithParameters());
}

@Test
public void configureQuery3() {
	assertEquals("set enable_nestloop=off;set enable_hashagg=on;\nselect 1;"
			,ModeRequete.configureQuery(new GenericPreparedStatementBuilder("select 1;"),ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON).getQueryWithParameters());
}

	
}
