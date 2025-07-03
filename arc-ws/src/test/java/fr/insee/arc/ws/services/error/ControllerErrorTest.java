package fr.insee.arc.ws.services.error;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import fr.insee.arc.ws.services.restServices.WsConfiguration;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = WsConfiguration.class)
public class ControllerErrorTest {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void indexOk() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/errors"))
			.andExpect(MockMvcResultMatchers.status().is(200)).andReturn();
		String content = result.getResponse().getContentAsString();
		assertTrue(content.contains("Invalid http request. Method : GET - Uri : /errors"));
	}

}
