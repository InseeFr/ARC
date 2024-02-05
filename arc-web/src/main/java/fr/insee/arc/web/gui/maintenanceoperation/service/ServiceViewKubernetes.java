package fr.insee.arc.web.gui.maintenanceoperation.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.service.kubernetes.ApiKubernetesService;
import fr.insee.arc.core.service.kubernetes.bo.KubernetesServiceResult;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

@Service
public class ServiceViewKubernetes extends InteractorMaintenanceOperations {

	public String createDatabases(Model model) throws IOException {
		// récupération du token
		// kubectl exec <pod_name> -t cat /var/run/secrets/kubernetes.io/serviceaccount/token
		String token = "Bearer "
				+ new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")),
						StandardCharsets.UTF_8);

		KubernetesServiceResult result = ApiKubernetesService.execute(views.getUrl(),
				views.getHttpType(), token, views.getJson());

		views.setHttpOutput(result.getResponse());
		model.addAttribute("httpOutput", views.getHttpOutput());
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	public String testMinio(Model model) throws IOException, KeyManagementException, NoSuchAlgorithmException, InvalidKeyException, ErrorResponseException, IllegalArgumentException, InsufficientDataException, InternalException, InvalidResponseException, ServerException, XmlParserException {
		MinioClient minioClient =
			    MinioClient.builder()
			        .endpoint(views.getUrl())
			        .credentials(views.getToken().split(",")[0], views.getToken().split(",")[1])
			        .build();
		
		minioClient.ignoreCertCheck();
		
		StringBuilder response = new StringBuilder();
		
		Iterator<Result<Item>> results = minioClient.listObjects(
			    ListObjectsArgs.builder().bucket(views.getHttpType()).build()).iterator();
		
	      while (results.hasNext()) {
	          Result<Item> el = results.next();
	          response.append(el.get().objectName()+"\n");
	          
	        }
	      
		views.setHttpOutput(response.toString());
		model.addAttribute("httpOutput", views.getHttpOutput());

		return generateDisplay(model, RESULT_SUCCESS);
		
	}	
	
	

	public String executeService(Model model) {
		KubernetesServiceResult result = ApiKubernetesService.execute(views.getUrl(),
				views.getHttpType(), views.getToken(), views.getJson());
		views.setHttpOutput(result.getResponse());
		model.addAttribute("httpOutput", views.getHttpOutput());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String deleteDatabases(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

}