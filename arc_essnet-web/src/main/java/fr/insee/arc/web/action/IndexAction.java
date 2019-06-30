package fr.insee.arc.web.action;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.utils.LoggerHelper;

@Component
@Results({ @Result(name = "success", location = "/jsp/index.jsp"),
	@Result(name = "status", type = "stream", params = { "contentType", "text/plain" }) })
public class IndexAction extends ArcAction {

    private static final Logger LOGGER = Logger.getLogger(IndexAction.class);
    private static final String ACTION_NAME = "Index";


    /**
     * Pour récupérer le choix de la norme
     **/

    @Action(value = "/index")
    public String index() {
	LoggerHelper.trace(LOGGER, getActionName());
	initialize();
	return generateDisplay();
    }

    @Action(value = "/accueil")
    public String accueil() {
	LoggerHelper.trace(LOGGER, getActionName());
	initialize();
	return generateDisplay();
    }

    @Action(value = "/status")
    public String status() {
	getDataBaseStatus();
	JSONObject status = new JSONObject();

	if (this.getIsDataBaseOK()) {
	    status.put("code", 0);
	    status.put("commentary", "Database OK");

	} else {
	    status.put("code", 201);
	    status.put("commentary", "Database connection failed");

	}

	this.inputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));
	return "status";
    }



    @Override
    public void putAllVObjects() {
	// no Vobject in this page

    }

    @Override
    public void instanciateAllDAOs() {
	// TODO Auto-generated method stub

    }

    @Override
    public void setProfilsAutorises() {
	// TODO Auto-generated method stub

    }

    @Override
    protected void specificTraitementsPostDAO() {
	// TODO Auto-generated method stub

    }

    @Override
    public String getActionName() {
	return ACTION_NAME;
    }

}
