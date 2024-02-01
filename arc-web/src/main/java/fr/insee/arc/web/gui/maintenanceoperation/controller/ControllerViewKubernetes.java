package fr.insee.arc.web.gui.maintenanceoperation.controller;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceoperation.service.ServiceViewKubernetes;

@Controller
public class ControllerViewKubernetes extends ServiceViewKubernetes {
    
    @RequestMapping("/secure/createPods")
    public String createPodsAction(Model model) throws NoSuchAlgorithmException, IOException, KeyManagementException {
		return createPods(model);
    }

    @RequestMapping("/secure/deletePods")
    public String deletePodsAction(Model model) {
		return deletePods(model);
    }

    
}