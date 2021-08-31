package fr.insee.arc.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class WebServiceRunModulesTest {

	
	public static void main(String[] args) throws Exception {

		int max=8;
		int i=0;
		int delayInMs=0;
		
		File dir=new File("C:/SauvNT/sirene4/fichier");
		for (File f:dir.listFiles())
		{
			i++;
			new Thread() {
				public void run() {
					long start = System.currentTimeMillis();

					try {
						sendRequest(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println(f.getName()+" : "+(System.currentTimeMillis()-start));
				}
			}.start();
			if (i==max)
				{
				break;
				}
			Thread.sleep(delayInMs);
		}
		
	  }
	
	
	public static void sendRequest(String fileName) throws Exception
	{

		URL url = new URL("http://10.243.10.172/sir4arcws/liasse/v2008-11");	
//		URL url = new URL("http://localhost:18080/arc_composite-ws/liasse/v2008-11");
//		URL url = new URL("http://localhost:18080/arc_composite-ws/liasse2/v2008-11");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/xml"); 
	
		String input = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
//		System.out.println(input);
		
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes());
		os.flush();
		
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
