package fr.insee.arc.web.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.util.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.web.model.ManageParametersModel;
import fr.insee.arc.web.util.VObject;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ManageParametersAction extends ArcAction<ManageParametersModel>  {

	private static final String RESULT_SUCCESS = "/jsp/manageParameters.jsp";

	private static final Logger LOGGER = LogManager.getLogger(ManageParametersAction.class);

    private VObject viewParameters;

	@Override
	protected void putAllVObjects(ManageParametersModel arcModel) {
		setViewParameters(this.vObjectService.preInitialize(arcModel.getViewParameters()));
		
		putVObject(getViewParameters(), t -> initializeParameters());
	}

    public void initializeParameters() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(viewParameters, new PreparedStatementBuilder("SELECT description, key ,val FROM arc.parameter"),  "arc.parameter", defaultInputFields);
    }

    @RequestMapping("/selectParameters")
    public String selectParameters(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addParameters")
    public String addParameters(Model model) {
        this.vObjectService.insert(viewParameters);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteParameters")
    public String deleteParameters(Model model) {
         this.vObjectService.delete(viewParameters);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateParameters")
    public String updateParameters(Model model) {
        this.vObjectService.update(viewParameters);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortParameters")
    public String sortParameters(Model model) {
        this.vObjectService.sort(viewParameters);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/startParameters")
    public String startParameters(Model model) throws Exception {
          	
    	
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public VObject getViewParameters() {
        return this.viewParameters;
    }

    public void setViewParameters(VObject viewParameters) {
        this.viewParameters = viewParameters;
    }


	@Override
	public String getActionName() {
		return "manageParameters";
	}

    
    
}