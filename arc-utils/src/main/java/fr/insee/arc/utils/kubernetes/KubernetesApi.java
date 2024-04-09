package fr.insee.arc.utils.kubernetes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

import fr.insee.arc.utils.kubernetes.bo.KubernetesApiResult;

public class KubernetesApi {

	private KubernetesApi() {
		throw new IllegalStateException("Utility class");
	}

	public static KubernetesApiResult execute(String urlProvided, HttpMethod httpMethod, String token,
			String json) {
		int responseCode = -1;
		StringBuilder response = new StringBuilder();

		try {
			String tokenBearer = "Bearer " + token;

			X509TrustManager x = new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					// selfsigned cetificate
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					// selfsigned cetificate
				}
			};

			TrustManager[] trustAllCerts = new TrustManager[] { x };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("TLSv1.3");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			URL url = new URL(urlProvided);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod(httpMethod.toString());

			con.setRequestProperty("Authorization", tokenBearer);
			con.setRequestProperty("Accept", "application/json");

			if (json != null && !json.isBlank()) {
				con.setRequestProperty("Content-Type", "application/json");
				con.setDoOutput(true);
				OutputStream os = con.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
				
				JSONObject j=new JSONObject(json);
				
				osw.write(j.toString());
				osw.flush();
				osw.close();
				os.close();
			}

			responseCode = con.getResponseCode();

			response.append("Response code : " + responseCode);
			response.append("\n");

			InputStream is = con.getInputStream();
			if (is != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {

			response.append(ExceptionUtils.getStackTrace(e));
		}

		return new KubernetesApiResult(httpMethod.toString()+" on "+urlProvided, responseCode, response.toString());
	}

}
