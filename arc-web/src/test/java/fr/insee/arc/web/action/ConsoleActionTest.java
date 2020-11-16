package fr.insee.arc.web.action;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import fr.insee.arc.web.WebConfig;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfig.class)
public class ConsoleActionTest {

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(ConsoleAction.class).build();
	}

	@Test
	public void consoleOk() throws Exception {
		String expected = "Console content";
		mockMvc.perform(MockMvcRequestBuilders.get("/updateConsole").sessionAttr("console", expected))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(expected));
	}

	@Test
	public void emptyConsoleOk() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/updateConsole"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(""));
	}

}
