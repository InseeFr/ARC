package fr.insee.arc.utils.dao;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ModeRequeteTest {

@Test
public void untokenize1() {
	assertEquals("set enable_nestloop = off;\nset enable_hashagg = on;\n",ModeRequete.untokenize(ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON));
}

@Test
public void configureQuery1() {
	assertEquals("set enable_nestloop = off;\nset enable_hashagg = on;\nset extra_float_digits=0;"
			,ModeRequete.configureQuery(null,ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON));
}

@Test
public void configureQuery2() {
	assertEquals("set enable_nestloop = off;\nset enable_hashagg = on;\nset extra_float_digits=0;"
			,ModeRequete.configureQuery("",ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON));
}

@Test
public void configureQuery3() {
	assertEquals("set enable_nestloop = off;\nset enable_hashagg = on;\nset extra_float_digits=0;select 1;"
			,ModeRequete.configureQuery("select 1;",ModeRequete.NESTLOOP_OFF,ModeRequete.HASHAGG_ON));
}

	
}
