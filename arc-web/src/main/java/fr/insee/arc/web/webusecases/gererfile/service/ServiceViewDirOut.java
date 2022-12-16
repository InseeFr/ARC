package fr.insee.arc.web.webusecases.gererfile.service;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewDirOut extends HubServiceGererFileAction {

	public String seeDirOut(Model model) {
		Map<String,ArrayList<String>> m=views.getViewDirOut().mapContentSelected();

		if (!m.isEmpty()) {
			if(m.get(IS_DIRECTORY).get(0).equals("true")) {
				views.setDirOut(Paths.get(views.getDirOut(), m.get(VC_FILENAME).get(0)).toString() + File.separator);
				model.addAttribute(DIR_OUT, views.getDirOut());
			}
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortDirOut(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewDirOut());
	}

	public String transferDirOut(Model model) {
		transfer(views.getViewDirOut(), views.getDirOut(), views.getDirIn());
		return generateDisplay(model, RESULT_SUCCESS);
	}


	public String copyDirOut(Model model) {
		copy(views.getViewDirOut(), views.getDirOut(), views.getDirIn());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateDirOut(Model model) {
		rename(views.getViewDirOut(),views.getDirOut());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String addDirOut(Model model) {
		createDirectory(views.getViewDirOut(),views.getDirOut());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String delDirOut(Model model) {
		if (delete(views.getViewDirOut(), views.getDirOut()))
		{
			views.setDirOut(properties.getBatchParametersDirectory());
			model.addAttribute(DIR_OUT, views.getDirOut());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	
	public String downloadDirOut(Model model, HttpServletResponse response) {
		download(response, views.getViewDirOut(), views.getDirOut());
        return "none";
	}
	
	
}
