package fr.insee.arc.web.gui.norme.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewMapping extends InteractorNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewMapping.class);

	/**
	 * Action trigger when the table of map rules is request or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	public String selectMapping(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a map rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	public String addMapping(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	public String deleteMapping(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Action trigger by updating a map rule in the GUI. Update the GUI and the
	 * database. Before insertion check if the rule is OK
	 * 
	 * @return
	 */
	public String updateMapping(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Action trigger by importing a map rule file
	 * 
	 * @return
	 */
	public String importMapping(Model model, MultipartFile fileUploadMap) {
		dao.uploadFileRule(views.getViewMapping(), views.getViewJeuxDeRegles(), fileUploadMap);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the map rules. Update GUI and database
	 * 
	 * @return
	 */
	public String viderMapping(Model model) {

		try {
			dao.emptyRuleTable(this.views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_MAPPING_REGLE));
		} catch (ArcException e) {
			e.logFullException();
		}
		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Action trigger by sorting the map rules in the GUI. Update the GUI
	 * 
	 * @return
	 */
	public String sortMapping(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Action trigger by request the generation of the mapping rule. Will create in
	 * database empty rules for each column in the final model and update the GUI.
	 * 
	 * @return
	 */
	public String preGenererRegleMapping(Model model) {

		try {
			
			dao.execQueryPreGenererRegleMapping(views.getViewNorme(), views.getViewJeuxDeRegles(), views.getViewMapping());

		} catch (ArcException e) {
			loggerDispatcher.error("Error in preGenererRegleMapping", e, LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
