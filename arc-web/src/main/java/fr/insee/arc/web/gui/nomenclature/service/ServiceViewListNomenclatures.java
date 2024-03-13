package fr.insee.arc.web.gui.nomenclature.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;

@Service
public class ServiceViewListNomenclatures extends InteractorNomenclature {

    private static final Logger LOGGER = LogManager.getLogger(ServiceViewListNomenclatures.class);

    private static final String NMCL_PREFIX = "nmcl_";
    private static final String NOM_TABLE = "nom_table";

    public String selectListNomenclatures(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String addListNomenclatures(Model model) {
		String nomTable = views.getViewListNomenclatures().mapInputFields().get(NOM_TABLE).get(0);
        if (validationNomTable(nomTable)) {
            this.vObjectService.insert(views.getViewListNomenclatures());
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateListNomenclatures(Model model) {
        // vérification que tous les noms de tables updatés soient conformes
    	boolean zeroErreur = true;
    	Map<String, List<String>> selection = views.getViewListNomenclatures().mapContentAfterUpdate();
        if (!selection.isEmpty()) {
        	for (String nomTable : selection.get(NOM_TABLE)) {
                if (nomTable != null && !validationNomTable(nomTable)) {
                    this.views.getViewListNomenclatures().setMessage("nmclManagement.update.invalidName");
                    this.views.getViewListNomenclatures().setMessageArgs(nomTable);
                    zeroErreur = false;
                    break;
                }
            }
        } else {
            this.views.getViewListNomenclatures().setMessage("nmclManagement.update.noNomenclature");
        }
        if (zeroErreur) {
            this.vObjectService.update(this.views.getViewListNomenclatures());
        }

        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortListNomenclatures(Model model) {
        this.vObjectService.sort(views.getViewListNomenclatures());
        return basicAction(model, RESULT_SUCCESS);
    }

    public String deleteListNomenclatures(Model model) {
        try {
            
        	dao.execQueryDeleteListNomenclature(views.getViewListNomenclatures());

            this.vObjectService.delete(views.getViewListNomenclatures());
     
        } catch (ArcException e) {
        	StaticLoggerDispatcher.error(LOGGER, "Error in GererFamilleNormeAction.executeRequeteMiseAjourTableMetier");
        }
        return basicAction(model, RESULT_SUCCESS);
    }

    public String downloadListNomenclatures(Model model, HttpServletResponse response) {
    	
    	Map<String, List<String>> selection = views.getViewListNomenclatures().mapContentSelected();
    	
		if (!selection.isEmpty()) {
			
			String selectedNomenclature=selection.get(NOM_TABLE).get(0);
			
			ArcPreparedStatementBuilder requeteNomenclature = new ArcPreparedStatementBuilder();
			requeteNomenclature.append(SQL.SELECT).append("a.*").append(SQL.FROM);
			requeteNomenclature.append(DataObjectService.getFullTableNameInSchema(SchemaEnum.ARC_METADATA, selectedNomenclature));
			requeteNomenclature.append(" a");
			
			this.vObjectService.download(views.getViewListNomenclatures(), response, Arrays.asList(selectedNomenclature)
					, Arrays.asList(requeteNomenclature)
					);
			return "none";
		} else {
			this.views.getViewListNomenclatures().setMessage("general.noSelection");
			return generateDisplay(model, RESULT_SUCCESS);
		}

    }

    /**
     * upload file to database
     * @param model
     * @param fileUpload
     * @return
     */
    public String importListNomenclatures(Model model, MultipartFile fileUpload) {    	
    	loggerDispatcher.debug("importListNomenclatures",LOGGER);
    	try {
			 		   
 		   if (views.getViewListNomenclatures().mapContentSelected().isEmpty()) {
 	           this.views.getViewListNomenclatures().setMessage("nmclManagement.import.noSelection");
 	           return generateDisplay(model, RESULT_SUCCESS);
 	        }

 	        String nouvelleNomenclature = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
 	        LoggerHelper.debug(LOGGER,"/* Import de la nomenclature : " + nouvelleNomenclature + "*/");

 	        if (StringUtils.isEmpty(nouvelleNomenclature)) {
 	            this.views.getViewListNomenclatures().setMessage("nmclManagement.import.noSelection");
 	           return generateDisplay(model, RESULT_SUCCESS);
 	        }

 	        // Ouverture du fichier
 	        if (fileUpload == null || fileUpload.isEmpty()) {
 	            this.views.getViewListNomenclatures().setMessage("general.import.noFileSelection");
 	           return generateDisplay(model, RESULT_SUCCESS);
 	        }
    		
            dao.importNomenclatureDansBase(views.getViewListNomenclatures(), fileUpload);
        } catch (ArcException ex) {
           	this.views.getViewListNomenclatures().setMessage("nmclManagement.import.error");
        }

        return generateDisplay(model, RESULT_SUCCESS);
    }

    private boolean validationNomTable(String nomTable) {
        if (nomTable == null) {
            this.views.getViewListNomenclatures().setMessage("nmclManagement.validateName.empty");
            return false;
        }

        // Vérification du bon format du nom de la table : nom_millesime
        // doit être de la forme nom_millesime

        if (!nomTable.startsWith(NMCL_PREFIX)) {
            this.views.getViewListNomenclatures().setMessage("nmclManagement.validateName.error.prefix");
            return false;
        }

        if (nomTable.split(Delimiters.SQL_TOKEN_DELIMITER).length < 3) {
            this.views.getViewListNomenclatures().setMessage("nmclManagement.validateName.error.format");
            return false;
        }

        // primary key sur la table arc.ihm_nmcl donc pas besoin de tester que le nom existe déjà

        return true;
    }
    

}
