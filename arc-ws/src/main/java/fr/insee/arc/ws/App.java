package fr.insee.arc.ws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.Record;
import fr.insee.arc.ws.services.rest.changerules.pojo.ChangeRulesPojo;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {

    	testUpdateNorme();

    }
    
    
    public static void testUpdateNorme() throws Exception
    {
    	
    	URL url = new URL("http://localhost:8080/arc-ws/changeRules/arc_bas2/");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String charset = "UTF-8";
		
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept-Charset", charset);
		conn.setRequestProperty("Content-Type", "application/json; utf-8"); 

		
		ChangeRulesPojo rules=new ChangeRulesPojo();
		rules.content= new HashMap<String, Record>();
		
		rules.content.put("id_norme", new Record("text",  new ArrayList<String>( Arrays.asList("v002"))));
		rules.content.put("periodicite", new Record("text",  new ArrayList<String>( Arrays.asList("A"))));
		rules.content.put("def_norme", new Record("text",  new ArrayList<String>( Arrays.asList("test"))));
		rules.content.put("def_validite", new Record("text",  new ArrayList<String>( Arrays.asList("test"))));
		rules.content.put("etat", new Record("text",  new ArrayList<String>( Arrays.asList("1"))));
		rules.content.put("id_famille", new Record("text",  new ArrayList<String>( Arrays.asList("miur563"))));

		
		rules.targetRule="NORME";
		
		
		JSONObject body=new JSONObject(rules);
		
		System.out.println(body);
		
		
		
//		String input = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
//		System.out.println(input);
		
		try (OutputStream os = conn.getOutputStream())
		{
		os.write(body.toString().getBytes(charset));
		}
//		System.out.println("sendRequest");

		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
				+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		String output;
//		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			System.out.println(output);
		}

		conn.disconnect();

	  }
    	
    	
}
    


