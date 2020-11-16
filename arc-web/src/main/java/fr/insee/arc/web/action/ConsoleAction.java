package fr.insee.arc.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.core.util.LoggerDispatcher;

@Controller
public class ConsoleAction {
	
	private static final String CONSOLE_SESSION_NAME = "console";

	private static final Logger LOGGER = LogManager.getLogger(ConsoleAction.class);

	@Autowired
    @Qualifier("activeLoggerDispatcher")
    protected LoggerDispatcher loggerDispatcher;
	
	@RequestMapping("/updateConsole")
	public void updateConsole(HttpSession session, HttpServletResponse response) {
		

		if (session.getAttribute(CONSOLE_SESSION_NAME) == null) {
			session.setAttribute(CONSOLE_SESSION_NAME, "");
		}

		response.setCharacterEncoding("UTF-8");
		try (PrintWriter out = response.getWriter();){
			out.write((String) session.getAttribute(CONSOLE_SESSION_NAME));
		} catch (IOException e) {
			loggerDispatcher.error("Error in updateConsole", e, LOGGER);
		}
		session.setAttribute(CONSOLE_SESSION_NAME, "");
	}
}
