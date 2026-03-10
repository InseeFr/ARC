package fr.insee.arc.web.gui.file.service;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewDirIn extends InteractorFile {


	public String seeDirIn(Model model) {
		Map<String,List<String>> m= views.getViewDirIn().mapContentSelected();
		if (!m.isEmpty() && m.get(IS_DIRECTORY).get(0).equals("true"))  {
			views.setDirIn(Paths.get(views.getDirIn(), m.get(VC_FILENAME).get(0)).toString() + File.separator);
			model.addAttribute(DIR_IN, views.getDirIn());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortDirIn(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewDirIn());
	}

	public String transferDirIn(Model model) {
		transfer(views.getViewDirIn(), views.getDirIn(), views.getDirOut());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String copyDirIn(Model model) {
		copy(views.getViewDirIn(), views.getDirIn(), views.getDirOut());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateDirIn(Model model) {
		rename(views.getViewDirIn(), views.getDirIn());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String addDirIn(Model model) {
		createDirectory(views.getViewDirIn(), views.getDirIn());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String delDirIn(Model model) {
		if (delete(views.getViewDirIn(), views.getDirIn()))
		{
			views.setDirIn(properties.getBatchParametersDirectory());
			model.addAttribute(DIR_IN, views.getDirIn());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String downloadDirIn(Model model, HttpServletResponse response) {
		download(response, views.getViewDirIn(), views.getDirIn());
        return "none";
	}
	
}
