package fr.insee.arc.web.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.web.WebConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfiguration.class)
@TestMethodOrder(OrderAnnotation.class)
public class MvcActionTest extends InitializeQueryTest {

	@BeforeAll
	public static void setupAll() throws ArcException, SQLException {

		buildPropertiesWithoutScalability("tmp");

		BddPatcherTest.createDatabase();

	}
	
	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	@Order(1)
	public void healthcheckOk() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/healthcheck"))
			.andExpect(MockMvcResultMatchers.status().is(200));
	}
	
	@Test
	@Order(2)
	public void indexOk() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/secure/index"))
				.andExpect(MockMvcResultMatchers.status().is(200));
	}
	
	@Test
	@Order(3)
	public void accessibiliteOk() throws Exception {

		// check if parameters had been set
    	BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);
    	assertEquals("2026-2028", bdParameters.getString(null, "ACCESSIBILITY_STRATEGY_PERIOD"));	
		
    	mockMvc.perform(MockMvcRequestBuilders.get("/secure/accessibilite"))
			.andExpect(MockMvcResultMatchers.status().is(200));

	}
	

}
