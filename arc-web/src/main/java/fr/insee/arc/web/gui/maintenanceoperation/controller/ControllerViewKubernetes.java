package fr.insee.arc.web.gui.maintenanceoperation.controller;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceoperation.service.ServiceViewKubernetes;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

@Controller
public class ControllerViewKubernetes extends ServiceViewKubernetes {
    
    @RequestMapping("/secure/executeService")
    public String executeServiceAction(Model model) {
		return executeService(model);
    }

    @RequestMapping("/secure/createDatabases")
    public String createDatabasesAction(Model model) throws IOException {
		return createDatabases(model);
    }
    
    @RequestMapping("/secure/deleteDatabases")
    public String deleteDatabasesAction(Model model) throws KeyManagementException, InvalidKeyException, NoSuchAlgorithmException, ErrorResponseException, IllegalArgumentException, InsufficientDataException, InternalException, InvalidResponseException, ServerException, XmlParserException, IOException {
		return testMinio(model);
    }

}