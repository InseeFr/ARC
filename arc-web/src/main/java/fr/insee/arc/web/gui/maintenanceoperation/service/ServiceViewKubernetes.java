package fr.insee.arc.web.gui.maintenanceoperation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.service.kubernetes.ApiManageExecutorDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.kubernetes.bo.KubernetesApiResult;

@Service
public class ServiceViewKubernetes extends InteractorMaintenanceOperations {

	public String createDatabases(Model model) {
		
		StringBuilder result = new StringBuilder();

		try {
			List<KubernetesApiResult> results = ApiManageExecutorDatabase.create();
			
			for (KubernetesApiResult r : results)
			{
				result.append(r.toString());
			}
			
		} catch (ArcException e) {
			result.append(e.getMessage());
		}
		
		views.setHttpOutput(result.toString());
		model.addAttribute("httpOutput", views.getHttpOutput());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String deleteDatabases(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

}