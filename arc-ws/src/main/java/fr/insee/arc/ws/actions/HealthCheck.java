package fr.insee.arc.ws.actions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;


public class HealthCheck {
	
	private HealthCheck() {
		// static class
	}

	/** Test the database connection and print the result in the response. */
	public static void status(HttpServletResponse response) throws IOException{

		StringBuilder responseContent=new StringBuilder();
		
		
		try {
			UtilitaireDao.get("arc", 1).executeRequest(null, new PreparedStatementBuilder("select true"));			
			responseContent.append("Status : 0\n");
			responseContent.append("Configuration is OK");
		} catch (Exception e) {
			responseContent.append("Status : 201\n");
			responseContent.append("Database connection failed");
		}
		try (ServletOutputStream outputStream = response.getOutputStream()){
			outputStream.write(responseContent.toString().getBytes(StandardCharsets.UTF_8));
		}
	}
	
}
