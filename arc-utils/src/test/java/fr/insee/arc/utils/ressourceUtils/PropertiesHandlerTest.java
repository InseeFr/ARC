package fr.insee.arc.utils.ressourceUtils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PropertiesHandlerTest {

	@Test
	void test() {
		
		PropertiesHandler p = new PropertiesHandler();
		
		p.setDatabaseUrl("{0=>\"db1\"},{1=>\"db2\"}");
		p.setDatabaseUsername("{0=>\"db1\"},{1=>\"db2\"}");
		p.setDatabasePassword("{0=>\"db1\"},{1=>\"db2\"}");
		p.setDatabaseDriverClassName("{0=>\"db1\"},{1=>\"db2\"}");
		
		p.connectionProperties().forEach(t->System.out.println(t.getDatabaseUrl()));
		p.connectionPropertiesDebug().forEach(t->System.out.println(t.getDatabaseUrl()));
		
		p.setDatabaseUrl("jdbc://host_coordinator:5432/db_coordinator");
		p.setDatabaseUsername("user_coordinator");
		p.setDatabasePassword("p_coord");
		p.setDatabaseDriverClassName("postgres");
		
		p.setKubernetesExecutorNumber(3);
		p.setKubernetesExecutorLabel("pg-arc-kub");
		p.setKubernetesExecutorDatabase("db");
		p.setKubernetesExecutorPort("5432");
		p.setKubernetesExecutorUser("user_kub");

		p.connectionProperties().forEach(t->System.out.println(t.getDatabaseUrl()));
		p.connectionPropertiesDebug().forEach(t->System.out.println(t.getDatabaseUrl()));
		p.connectionProperties().forEach(t->System.out.println(t.getDatabaseUrl()));

		
		assertTrue(true);
	}

}
