package fr.insee.arc.web.gui.maintenanceoperation.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;


@Service
public class ServiceViewKubernetes extends InteractorMaintenanceOperations {

    public String createPods(Model model) throws IOException, NoSuchAlgorithmException, KeyManagementException {
    	
    	System.out.println(views.getHttpType());
    	System.out.println(views.getUrl());
    	
    	// récupération du token
    	String tokenBearer = "Bearer "+new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")), StandardCharsets.UTF_8);
    	
    	X509TrustManager x=  new X509TrustManager() {     
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
	            return new X509Certificate[0];
	        } 
	        public void checkClientTrusted( 
	            java.security.cert.X509Certificate[] certs, String authType) {
	        	// selfsigned cetificate
	            } 
	        public void checkServerTrusted( 
	            java.security.cert.X509Certificate[] certs, String authType) {
	        	// selfsigned cetificate
	        }
	    } ;
    	
    	
    	TrustManager[] trustAllCerts = new TrustManager[] { x };

    		// Install the all-trusting trust manager
	    SSLContext sc = SSLContext.getInstance("TLSv1.2"); 
	    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    	URL url = new URL(views.getUrl());
    	HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    	con.setRequestMethod(views.getHttpType());
    	con.setRequestProperty("Authorization", tokenBearer);
    	con.setRequestProperty("Accept", "application/json");
    	con.setRequestProperty("Content-Type", "application/json");

    	if (views.getJson()!=null || !views.getJson().isBlank()) {
	    	OutputStream os = con.getOutputStream();
	    	OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
	    	osw.write(views.getJson());
	    	osw.flush();
	    	osw.close();
	    	os.close();
    	}
    	
    	String result;
    	BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
    	ByteArrayOutputStream buf = new ByteArrayOutputStream();
    	int result2 = bis.read();
    	while(result2 != -1) {
    	    buf.write((byte) result2);
    	    result2 = bis.read();
    	}
    	result = buf.toString();
    	System.out.println(result);
    	views.setHttpOutput(result);
    	
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deletePods(Model model) {
        return generateDisplay(model, RESULT_SUCCESS);
    }
}