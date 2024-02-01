package fr.insee.arc.web.gui.maintenanceoperation.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.service.kubernetes.ApiKubernetesService;
import fr.insee.arc.core.service.kubernetes.bo.KubernetesServiceResult;

@Service
public class ServiceViewKubernetes extends InteractorMaintenanceOperations {

	public String createDatabases(Model model) throws IOException {
		// récupération du token
		// kubectl exec arc-web-99ff74866-95lvz -t cat /var/run/secrets/kubernetes.io/serviceaccount/token
		String token = "Bearer "
				+ new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")),
						StandardCharsets.UTF_8);

		KubernetesServiceResult result = ApiKubernetesService.execute(views.getUrl(),
				views.getHttpType(), token, views.getJson());

		views.setHttpOutput(result.getResponse());
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