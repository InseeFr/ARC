package fr.insee.arc.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.databaseobjects.ColumnEnum;
import fr.insee.arc.core.databaseobjects.TableEnum;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.dao.GererNormeService;
import fr.insee.arc.web.model.NormManagementModel;
import fr.insee.arc.web.service.ArcWebGenericService;
import fr.insee.arc.web.util.ConstanteBD;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererNormeAction extends ArcWebGenericService<NormManagementModel> implements IDbConstant {
	
	private static final String RESULT_SUCCESS = "/jsp/gererNorme.jsp";

	private static final String SELECTED_RULESET_TABLE = "SELECTED_RULESET_TABLE";
	private static final String SELECTED_RULESET_NAME = "SELECTED_RULESET_NAME";

	private static final Logger LOGGER = LogManager.getLogger(GererNormeAction.class);
	
	@Autowired
	private GererNormeService gererNormeService;

	// The norm view
	private VObject viewNorme;

	// The calendar view
	private VObject viewCalendrier;

	// The ruleset view
	private VObject viewJeuxDeRegles;

	// The module selection view
	private VObject viewModules;
	
	// The load rules view
	private VObject viewChargement;

	// The structurize rules view
	private VObject viewNormage;

	// The control rules view
	private VObject viewControle;

	// The filter rules view
	private VObject viewFiltrage;

	// The map to format rules view
	private VObject viewMapping;
	
	// Expression to use in mapping
	private VObject viewExpression;

	// The on ruleset to copy rules
	private VObject viewJeuxDeReglesCopie;
	
	// The action Name
	public static final String ACTION_NAME="normManagement";

	@Override
	public void putAllVObjects(NormManagementModel model) {		
		setViewNorme(vObjectService.preInitialize(model.getViewNorme()));
		setViewCalendrier(vObjectService.preInitialize(model.getViewCalendrier()));
		setViewJeuxDeRegles(vObjectService.preInitialize(model.getViewJeuxDeRegles()));
		setViewModules(vObjectService.preInitialize(model.getViewModules()));
		setViewChargement(vObjectService.preInitialize(model.getViewChargement()));
		setViewNormage(vObjectService.preInitialize(model.getViewNormage()));
		setViewControle(vObjectService.preInitialize(model.getViewControle()));
		setViewFiltrage(vObjectService.preInitialize(model.getViewFiltrage()));
		setViewMapping(vObjectService.preInitialize(model.getViewMapping()));
		setViewExpression(vObjectService.preInitialize(model.getViewExpression()));
		setViewJeuxDeReglesCopie(vObjectService.preInitialize(model.getViewJeuxDeReglesCopie()));
		
		putVObject(getViewNorme(),
				t -> gererNormeService.initializeViewNorme(t, databaseObjectService.getTable(TableEnum.IHM_NORME)));
		//
		putVObject(getViewCalendrier(), t -> gererNormeService.initializeViewCalendar(t, getViewNorme(),
				databaseObjectService.getTable(TableEnum.IHM_CALENDRIER) ));
		//
		putVObject(getViewJeuxDeRegles(), t -> gererNormeService.initializeViewRulesSet(t, getViewCalendrier(),
				databaseObjectService.getTable(TableEnum.IHM_JEUDEREGLE) ));
		//
		putVObject(getViewModules(), t -> gererNormeService.initializeViewModules(t, getViewJeuxDeRegles()));
		//
		putVObject(getViewChargement(), t -> gererNormeService.initializeChargement(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_CHARGEMENT_REGLE) ));
		//
		putVObject(getViewNormage(), t -> gererNormeService.initializeNormage(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_NORMAGE_REGLE) ));
		//
		putVObject(getViewControle(), t -> gererNormeService.initializeControle(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_CONTROLE_REGLE) ));
		//
		putVObject(getViewFiltrage(), t -> gererNormeService.initializeFiltrage(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_FILTRAGE_REGLE) ));
		//
		putVObject(getViewMapping(), t -> gererNormeService.initializeMapping(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_MAPPING_REGLE)  ));
		//
		putVObject(getViewExpression(), t -> gererNormeService.initializeExpression(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_EXPRESSION) ));
		//
		putVObject(getViewJeuxDeReglesCopie(), t -> gererNormeService.initializeJeuxDeReglesCopie(t, getViewJeuxDeRegles(), getViewModules(),
				databaseObjectService.getTable(TableEnum.IHM_JEUDEREGLE) , getScope()));
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	/**
	 * Action trigger by selecting a norm in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectNorme")
	public String selectNorme(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a norm in the GUI.
	 * 
	 * @return success
	 */
	@RequestMapping("/addNorme")
	public String addNorme(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewNorme);
	}

	/**
	 * Action trigger by deleting a norm in the GUI. Cannot delete a active norm
	 *
	 * @return success
	 */
	@RequestMapping("/deleteNorme")
	public String deleteNorme(Model model) {
		
		// Get the gui selection
		Map<String, ArrayList<String>> selection = viewNorme.mapContentSelected();

		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("Norm state : " + etat, LOGGER);
			// Check actived norm (code 1)
			if ("1".equals(etat)) {
				this.viewNorme.setMessage("Caution, cannot delete a activated norm");
			} else {
				this.vObjectService.delete(viewNorme);
			}
		} else {
			this.viewNorme.setMessage("You didn't select anything");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a norm in the GUI. Update the GUI
	 */
	@RequestMapping("/updateNorme")
	public String updateNorme(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewNorme);
	}

	/**
	 * Action trigger by sorting a norm in the GUI. Update the GUI
	 */
	@RequestMapping("/sortNorme")
	public String sortNorme(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewNorme);
	}

	/**
	 * Action trigger by selecting a calendar in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectCalendrier")
	public String selectCalendrier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a calendar in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@RequestMapping("/addCalendrier")
	public String addCalendrier(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewCalendrier);
	}

	/**
	 * Action trigger by deleting a calendar in the GUI. Cannot delete a activated
	 * calendar. Update the GUI and the database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteCalendrier")
	public String deleteCalendrier(Model model) {
		
		// get the selected calendar
		Map<String, ArrayList<String>> selection = viewCalendrier.mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("calendar state: " + etat, LOGGER);
			// Check actived calendar (code 1)
			if ("1".equals(etat)) {
				this.viewCalendrier.setMessage("Caution, cannot delete a active calendar");
			} else {
				this.vObjectService.delete(viewCalendrier);
			}
		} else {
			this.viewJeuxDeRegles.setMessage("You didn't select anything");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a calendar in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@RequestMapping("/updateCalendrier")
	public String updateCalendrier(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewCalendrier);
	}

	@RequestMapping("/sortCalendrier")
	public String sortCalendrier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewCalendrier);
	}

	/**
	 * Action trigger by selecting a rule set in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectJeuxDeRegles")
	public String selectRuleSet(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a rule set in the GUI. Cannot add a rule set in
	 * production state. Update the GUI and the database
	 * 
	 * @return success
	 */
	@RequestMapping("/addJeuxDeRegles")
	public String addRuleSet(Model model) {
		HashMap<String, ArrayList<String>> selection = viewJeuxDeRegles.mapInputFields();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
				this.viewJeuxDeRegles.setMessage("Caution, cannot add a rule set in the PRODUCTION state");
			} else {
				this.vObjectService.insert(viewJeuxDeRegles);
			}
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by deleting a rule set in the GUI. Cannot delete a rule state
	 * in the PRODUCTIONS state. Update the GUI and the database
	 * 
	 * @return sucess
	 */
	@RequestMapping("/deleteJeuxDeRegles")
	public String deleteRuleSet(Model model) {
		
		// Get the selection
		Map<String, ArrayList<String>> selection = viewJeuxDeRegles.mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("State to delete : " + etat, LOGGER);
			// Check production state. If yes cancel the delete and send a message to the
			// user
			if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
				this.viewJeuxDeRegles.setMessage("Caution, cannot delete a rule set in the PRODUCTION state");
			} else {
				this.vObjectService.delete(viewJeuxDeRegles);
			}
		} else {
			this.viewJeuxDeRegles.setMessage("You didn't select anything");
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a rule set in the GUI. Update the GUI and the
	 * database.
	 * 
	 * If the rule set is send to production, send a dummy file in prod which
	 * trigger the initialisation batch.
	 * 
	 * @return success
	 */
	@RequestMapping("/updateJeuxDeRegles")
	public String updateRuleSet(Model model) {
		HashMap<String, ArrayList<String>> selection = viewJeuxDeRegles.mapContentSelected();

		// on les crée dans tous les environnements et tous les entrepots
		// (ca evite les erreurs et car ca ne spécialise aucun environnement dans un
		// role à priori)

		if (!selection.isEmpty()) {
			for (int i = 0; i < selection.get("etat").size(); i++) {
				String etat = selection.get("etat").get(i);
				if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
					gererNormeService.sendRuleSetToProduction(this.viewJeuxDeRegles,
							databaseObjectService.getTable(TableEnum.PILOTAGE_BATCH)
							
							);
				}
			}

			this.vObjectService.update(viewJeuxDeRegles);

		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by sorting the ruleset in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/sortJeuxDeRegles")
	public String sortRuleSet(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewJeuxDeRegles);
	}

	/**
	 * Get all the rules from on rule set, export them in CSV, zip and send to user
	 * 
	 * @return
	 */
	@RequestMapping("/downloadJeuxDeRegles")
	public String downloadJeuxDeRegles(Model model, HttpServletResponse response) {
		Map<String, ArrayList<String>> selection = viewJeuxDeRegles.mapContentSelected();
		if (!selection.isEmpty()) {
			PreparedStatementBuilder requeteRegleChargement = new PreparedStatementBuilder();
			requeteRegleChargement.append(gererNormeService.recupRegle(this.viewJeuxDeRegles,
					databaseObjectService.getTable(TableEnum.IHM_CHARGEMENT_REGLE)
					));
			PreparedStatementBuilder requeteRegleNormage = new PreparedStatementBuilder();
			requeteRegleNormage.append(gererNormeService.recupRegle(this.viewJeuxDeRegles,
					databaseObjectService.getTable(TableEnum.IHM_NORMAGE_REGLE)));
			PreparedStatementBuilder requeteRegleControle = new PreparedStatementBuilder();
			requeteRegleControle.append(gererNormeService.recupRegle(this.viewJeuxDeRegles,
					databaseObjectService.getTable(TableEnum.IHM_CONTROLE_REGLE)));
			PreparedStatementBuilder requeteRegleMapping = new PreparedStatementBuilder();
			requeteRegleMapping.append(gererNormeService.recupRegle(this.viewJeuxDeRegles,
					databaseObjectService.getTable(TableEnum.IHM_MAPPING_REGLE)));
			PreparedStatementBuilder requeteRegleFiltrage = new PreparedStatementBuilder();
			requeteRegleFiltrage.append(gererNormeService.recupRegle(this.viewJeuxDeRegles,
					databaseObjectService.getTable(TableEnum.IHM_FILTRAGE_REGLE)));
			PreparedStatementBuilder requeteRegleExpression = new PreparedStatementBuilder();
			requeteRegleExpression.append(gererNormeService.recupRegle(this.viewJeuxDeRegles,
					databaseObjectService.getTable(TableEnum.IHM_EXPRESSION)));

			ArrayList<String> fileNames = new ArrayList<>();
			fileNames.add("Rules_load");
			fileNames.add("Rules_structurize");
			fileNames.add("Rules_control");
			fileNames.add("Rules_mapping");
			fileNames.add("Rules_filter");
			fileNames.add("Rules_expression");
			this.vObjectService.download(viewJeuxDeRegles, response, fileNames
					, new ArrayList<>(
							Arrays.asList(
									requeteRegleChargement
									, requeteRegleNormage
									, requeteRegleControle
									, requeteRegleMapping
									, requeteRegleFiltrage
									, requeteRegleExpression
									)
							)
					);
			return "none";
		} else {
			this.viewJeuxDeRegles.setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

	}

	
	/**
	 * Action trigger by selecting a module in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectModules")
	public String selectModules(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}
	
	/**
	 * Action trigger when the table of load rule is asked or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectChargement")
	public String selectChargement(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger when the table of load rule is asked or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/addChargement")
	public String addChargement(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewChargement);
	}

	/**
	 * Action trigger by adding a load rule in the GUI. Update the GUI and the
	 * database
	 *
	 * @return
	 */
	@RequestMapping("/deleteChargement")
	public String deleteChargement(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewChargement);
	}

	/**
	 * Action trigger by updating a load rule in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	@RequestMapping("/updateChargement")
	public String updateChargement(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewChargement);
	}

	/**
	 * Action trigger by updating a load rule in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	@RequestMapping("/sortChargement")
	public String sortChargement(Model model) {
		
		this.vObjectService.sort(viewChargement);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by importing load rules in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	@RequestMapping("/importChargement")
	public String importChargement(Model model, MultipartFile fileUploadLoad) {		
		gererNormeService.uploadFileRule(getViewChargement(), viewJeuxDeRegles, fileUploadLoad);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by importing structurize rules in the GUI. Update the GUI and
	 * the database.
	 * 
	 * @return
	 */
	@RequestMapping("/importNormage")
	public String importNormage(Model model, MultipartFile fileUploadStructurize) {
		
		gererNormeService.uploadFileRule(getViewNormage(), viewJeuxDeRegles, fileUploadStructurize);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger when the table of structuize rules is request or refresh.
	 * Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectNormage")
	public String selectNormage(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return success
	 */
	@RequestMapping("/addNormage")
	public String addNormage(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewNormage);
	}

	/**
	 * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteNormage")
	public String deleteNormage(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewNormage);
	}

	/**
	 * Action trigger by updating the structurize rule in the GUI. Update the GUI
	 * and the database
	 * 
	 * @return
	 */
	@RequestMapping("/updateNormage")
	public String updateNormage(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewNormage);
	}

	/**
	 * Action trigger by sorting the structurize rule in the GUI. Update the GUI.
	 * 
	 * @return
	 */

	@RequestMapping("/sortNormage")
	public String sortNormage(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewNormage);
	}

	/**
	 * Action trigger when the table of control rules is request or refresh. Update
	 * the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectControle")
	public String selectControle(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@SQLExecutor
	@RequestMapping("/addControle")
	public String addControle(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewControle);
	}

	/**
	 * Action trigger by deleting a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteControle")
	@SQLExecutor
	public String deleteControle(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewControle);
	}

	/**
	 * Action trigger by updating some control rules in the GUI. Update the GUI and
	 * the database. Before insertion in data base check if the news rules are
	 * coherents
	 * 
	 * @return
	 */
	@RequestMapping("/updateControle")
	@SQLExecutor
	public String updateControle(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewControle);
	}

	/**
	 * Action trigger by sorting a control rules in the GUI. Update the GUI .
	 * 
	 * @return
	 */
	@RequestMapping("/sortControle")
	public String sortControle(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewControle);
	}

	/**
	 * Action trigger by uploading a file with rule
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/importControle")
	@SQLExecutor
	public String importControle(Model model, MultipartFile fileUploadControle) {
		gererNormeService.uploadFileRule(getViewControle(), viewJeuxDeRegles, fileUploadControle);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the loading rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderChargement")
	public String viderChargement(Model model) {
		
		gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
				databaseObjectService.getTable(TableEnum.IHM_CHARGEMENT_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the structure rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderNormage")
	public String viderNormage(Model model) {
		
		gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
				databaseObjectService.getTable(TableEnum.IHM_NORMAGE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the control rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderControle")
	public String viderControle(Model model) {
		
		gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
				databaseObjectService.getTable(TableEnum.IHM_CONTROLE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the filter rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderFiltrage")
	public String viderFiltrage(Model model) {
		
		gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
				databaseObjectService.getTable(TableEnum.IHM_FILTRAGE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Clean the map rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderMapping")
	public String viderMapping(Model model) {
		
		gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
				databaseObjectService.getTable(TableEnum.IHM_MAPPING_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);

	}
	
	/**
	 * Clean the expressions. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderExpression")
	public String viderExpression(Model model) {
		
		gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
				databaseObjectService.getTable(TableEnum.IHM_EXPRESSION));
		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Action trigger when the table of map rules is request or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectFiltrage")
	public String selectFiltrage(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}
	
	/**
	 * Action trigger by updating a filter rule in the GUI. Update the GUI and the
	 * database. Before inserting, the rules are checked
	 * 
	 * @return
	 */
	@RequestMapping("/updateFiltrage")
	@SQLExecutor
	public String updateFiltrage(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewFiltrage);
	}

	
	/**
	 * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteFiltrage")
	public String deleteFiltrage(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewFiltrage);
	}

	
	/**
	 * Action trigger when the table of map rules is request or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectMapping")
	public String selectMapping(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a map rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	@RequestMapping("/addMapping")
	public String addMapping(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewMapping);
	}

	@RequestMapping("/deleteMapping")
	public String deleteMapping(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewMapping);
	}

	/**
	 * Action trigger by updating a map rule in the GUI. Update the GUI and the
	 * database. Before insertion check if the rule is OK
	 * 
	 * @return
	 */
	@RequestMapping("/updateMapping")
	public String updateMapping(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewMapping);
	}

	@RequestMapping("/selectExpression")
	public String selectExpression(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}
	
	@RequestMapping("/addExpression")
	public String addExpression(Model model) {
		String exprNameHeader = ColumnEnum.EXPR_NOM.getColumnName();
		
		// trim the inserted expression
		viewExpression.setInputFieldFor(exprNameHeader, viewExpression.getInputFieldFor(exprNameHeader).trim());
		
		return addLineVobject(model, RESULT_SUCCESS, this.viewExpression);
	}

	@RequestMapping("/updateExpression")
	public String updateExpression(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewExpression);
	}

	@RequestMapping("/sortExpression")
	public String sortExpression(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewExpression);
	}
	
	@RequestMapping("/deleteExpression")
	public String deleteExpression(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewExpression);
	}
	
	@RequestMapping("/importExpression")
	@SQLExecutor
	public String importExpression(Model model, MultipartFile fileUploadExpression) {
		gererNormeService.uploadFileRule(getViewExpression(), viewJeuxDeRegles, fileUploadExpression);
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	/**
	 * Action trigger by uploading a filter rule file
	 * 
	 * @return
	 */
	@RequestMapping("/importFiltrage")
	public String importFiltrage(Model model, MultipartFile fileUploadFilter) {
		
		gererNormeService.uploadFileRule(this.viewFiltrage, this.viewJeuxDeRegles, fileUploadFilter);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action initializing the filter rules
	 * 
	 * @return
	 */
	@RequestMapping("/preGenererRegleFiltrage")
	public String preGenererRegleFiltrage(Model model) {
		try {
			Map<String, ArrayList<String>> selection = viewJeuxDeRegles.mapContentSelected();

			 PreparedStatementBuilder requete= new PreparedStatementBuilder();
			 requete.append("INSERT INTO " + this.viewFiltrage.getTable())
             .append("  " + Format.stringListe(this.viewFiltrage.getHeadersDLabel()))
             .append("  SELECT (SELECT coalesce(max(id_regle),1) FROM " + this.viewFiltrage.getTable() + ")+row_number() over () ,")
             .append("  " + requete.quoteText(selection.get("id_norme").get(0)) + ", ")
             .append("  " + requete.quoteText(selection.get("validite_inf").get(0)) + "::date, ")
             .append("  " + requete.quoteText(selection.get("validite_sup").get(0)) + "::date, ")
             .append("  " + requete.quoteText(selection.get("version").get(0)) + ", ")
             .append("  " + requete.quoteText(selection.get("periodicite").get(0)) + ", ")
             .append("  null,")//
             .append("  null;");
			
			
			 UtilitaireDao.get("arc").executeRequest(null,requete);
		} catch (ArcException e) {
			loggerDispatcher.error(String.format("Error in preGenererRegleFiltrage : %s", e.toString()), LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by importing a map rule file
	 * 
	 * @return
	 */
	@RequestMapping("/importMapping")
	public String importMapping(Model model, MultipartFile fileUploadMap) {
		gererNormeService.uploadFileRule(getViewMapping(), viewJeuxDeRegles, fileUploadMap);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by request the generation of the mapping rule. Will create in
	 * database empty rules for each column in the final model and update the GUI.
	 * 
	 * @return
	 */
	@RequestMapping("/preGenererRegleMapping")
	public String preGenererRegleMapping(Model model) {
		
		try {

			// List hard coded to be sure of the order in the select
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
				requete.append("INSERT INTO " + this.viewMapping.getTable())
					.append("  (id_regle, id_norme, validite_inf, validite_sup,  version , periodicite, variable_sortie, expr_regle_col, commentaire) ")
					.append("  SELECT coalesce((SELECT max(id_regle) FROM " + this.viewMapping.getTable() + "),0)+row_number() over () ,")
					.append(requete.quoteText(viewJeuxDeRegles.mapContentSelected().get(ConstanteBD.ID_NORME.getValue()).get(0))+ ", ")
					.append(requete.quoteText(viewJeuxDeRegles.mapContentSelected().get(ConstanteBD.VALIDITE_INF.getValue()).get(0))+ "::date, ")
					.append(requete.quoteText(viewJeuxDeRegles.mapContentSelected().get(ConstanteBD.VALIDITE_SUP.getValue()).get(0))+ "::date, ")
					.append(requete.quoteText(viewJeuxDeRegles.mapContentSelected().get(ConstanteBD.VERSION.getValue()).get(0))+ ", ")
					.append(requete.quoteText(viewJeuxDeRegles.mapContentSelected().get(ConstanteBD.PERIODICITE.getValue()).get(0))+ ", ")
					.append("  liste_colonne.nom_variable_metier,").append("  null,").append(
							"  null")
					.append("  FROM (")
					.append(ApiService.listeColonneTableMetierSelonFamilleNorme(ApiService.IHM_SCHEMA,
									viewNorme.mapContentSelected().get(ConstanteBD.ID_FAMILY.getValue()).get(0)))
					.append(") liste_colonne");
				
			UtilitaireDao.get("arc").executeRequest(null, requete);
		} catch (ArcException e) {
			loggerDispatcher.error("Error in preGenererRegleMapping", e, LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by sorting the filter rules in the GUI. Update the GUI
	 * 
	 * @return
	 */
	@RequestMapping("/sortFiltrage")
	public String sortFiltrage(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewFiltrage);
	}

	/**
	 * Action trigger by sorting the map rules in the GUI. Update the GUI
	 * 
	 * @return
	 */
	@RequestMapping("/sortMapping")
	public String sortMapping(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewMapping);
	}

	/**
	 * Action trigger by requesting the load rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("selectJeuxDeReglesChargementCopie")
	public String selectJeuxDeReglesChargementCopie(Model model) {
		
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewChargement.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewChargement.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the structurize rules of the register rule set
	 * to copy in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("selectJeuxDeReglesNormageCopie")
	public String selectJeuxDeReglesNormageCopie(Model model) {
		
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewNormage.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewNormage.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the control rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("selectJeuxDeReglesControleCopie")
	public String selectJeuxDeReglesControleCopie(Model model) {
		
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewControle.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewControle.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the filter rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("selectJeuxDeReglesFiltrageCopie")
	public String selectJeuxDeReglesFiltrageCopie(Model model) {
		
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewFiltrage.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewFiltrage.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the map rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("/selectJeuxDeReglesMappingCopie")
	public String selectJeuxDeReglesMappingCopie(Model model) {
		
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewMapping.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewMapping.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	@RequestMapping("/selectJeuxDeReglesExpressionCopie")
	public String selectJeuxDeReglesExpressionCopie(Model model) {
		
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewExpression.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewExpression.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/selectJeuxDeReglesCopie")
	public String selectJeuxDeReglesCopie(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/copieJeuxDeRegles")
	public String copieJeuxDeRegles(Model model) throws ArcException {
		loggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
		// le jeu de regle à copier
		Map<String, ArrayList<String>> selectionOut = viewJeuxDeRegles.mapContentSelected();
		// le nouveau jeu de regle
		Map<String, ArrayList<String>> selectionIn = viewJeuxDeReglesCopie.mapContentSelected();
		HashMap<String, String> type = viewJeuxDeReglesCopie.mapHeadersType();
		if (!selectionIn.isEmpty()) {

			// columns found in all rules tables
			String inCommonColumns=new StringBuilder()
					.append(ConstanteBD.ID_NORME.getValue())
					.append(","+ConstanteBD.PERIODICITE.getValue())
					.append(","+ConstanteBD.VALIDITE_INF.getValue())
					.append(","+ConstanteBD.VALIDITE_SUP.getValue())
					.append(","+ConstanteBD.VERSION.getValue())
					.toString();

			// specific columns = column of the table minus common tables minus id_regle (rules generated id)
			PreparedStatementBuilder getTableSpecificColumns=new PreparedStatementBuilder();
			getTableSpecificColumns.append("\n SELECT string_agg(column_name,',') ");
			getTableSpecificColumns.append("\n FROM information_schema.columns c ");
			getTableSpecificColumns.append("\n WHERE table_schema||'.'||table_name ="+getTableSpecificColumns.quoteText(this.getSelectedJeuDeRegle()));
			getTableSpecificColumns.append("\n AND column_name NOT IN ");
			getTableSpecificColumns.append("\n ('"+inCommonColumns.replace(",", "','")+"','"+ConstanteBD.ID_REGLE.getValue()+"') ");

			String specificColumns=UtilitaireDao.get(poolName).getString(null, getTableSpecificColumns);
			
			// Build the copy query
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
			
			requete.append("INSERT INTO " + this.getSelectedJeuDeRegle() + " ");
			requete.append("(");
			requete.append(inCommonColumns+","+specificColumns);
			requete.append(")");
			
			requete.append("\n SELECT ");
			requete.append(String.join(",", 
					  requete.quoteText(selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0))
					, requete.quoteText(selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0))
					, requete.quoteText(selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0)) + "::date "
					, requete.quoteText(selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0)) + "::date "
					, requete.quoteText(selectionOut.get(ConstanteBD.VERSION.getValue()).get(0))));
			requete.append(","+specificColumns);

			requete.append(" FROM " + this.getSelectedJeuDeRegle() + "  ");

			requete.append(" WHERE ");

			requete.append(String.join(" AND ", //
					// condition about id_norm
					ConstanteBD.ID_NORME.getValue() + requete.sqlEqual(
							selectionIn.get(ConstanteBD.ID_NORME.getValue()).get(0),
							type.get(ConstanteBD.ID_NORME.getValue())),
					ConstanteBD.PERIODICITE.getValue()
							// condition about PERIODICITE
							+ requete
									.sqlEqual(selectionIn.get(ConstanteBD.PERIODICITE.getValue()).get(0),
											type.get(ConstanteBD.PERIODICITE.getValue())),
					ConstanteBD.VALIDITE_INF.getValue()
							// condition about VALIDITE_INF
							+ requete.sqlEqual(selectionIn.get(ConstanteBD.VALIDITE_INF.getValue()).get(0),
									type.get(ConstanteBD.VALIDITE_INF.getValue())),
					ConstanteBD.VALIDITE_SUP.getValue()
							// condition about VALIDITE_SUP
							+ requete
									.sqlEqual(selectionIn.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0),
											type.get(ConstanteBD.VALIDITE_SUP.getValue())),
					ConstanteBD.VERSION.getValue()
							// condition about VERSION
							+ requete.sqlEqual(selectionIn.get(ConstanteBD.VERSION.getValue()).get(0),
									type.get(ConstanteBD.VERSION.getValue()))

			));
			requete.append(" order by "+ConstanteBD.ID_REGLE.getValue()+" ;");
			
			// delete the current rules before the copy
			if (this.getSelectedJeuDeRegle().equals("arc.ihm_chargement_regle")) {
				gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
						databaseObjectService.getTable(TableEnum.IHM_CHARGEMENT_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_normage_regle")) {
				gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
						databaseObjectService.getTable(TableEnum.IHM_NORMAGE_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_controle_regle")) {
				gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
						databaseObjectService.getTable(TableEnum.IHM_CONTROLE_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_filtrage_regle")) {
				gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
						databaseObjectService.getTable(TableEnum.IHM_FILTRAGE_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_mapping_regle")) {
				gererNormeService.emptyRuleTable(this.viewJeuxDeRegles,
						databaseObjectService.getTable(TableEnum.IHM_MAPPING_REGLE));
			}
			
			// excute the copy
			try {
				UtilitaireDao.get("arc").executeRequest(null, requete);
			} catch (ArcException ex) {
				loggerDispatcher.error("Error in copieJeuxDeRegles", ex, LOGGER);
			}
			this.vObjectService.destroy(viewJeuxDeReglesCopie);
		} else {
			loggerDispatcher.info("No rule set choosed", LOGGER);
			this.viewJeuxDeRegles.setMessage("Please choose a ruleset");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public VObject getViewNorme() {
		return this.viewNorme;
	}

	public void setViewNorme(VObject vObjectData) {
		LoggerHelper.debug(LOGGER, "viewNorme.getSelectedLines() h ", vObjectData.getSelectedLines());
		this.viewNorme = vObjectData;
	}

	public VObject getViewCalendrier() {
		return this.viewCalendrier;
	}

	public void setViewCalendrier(VObject viewCalendrier) {
		this.viewCalendrier = viewCalendrier;
	}

	public VObject getViewJeuxDeRegles() {
		return this.viewJeuxDeRegles;
	}

	public void setViewJeuxDeRegles(VObject viewJeuxDeRegles) {
		this.viewJeuxDeRegles = viewJeuxDeRegles;
	}
	
	public VObject getViewModules() {
		return viewModules;
	}

	public void setViewModules(VObject viewModules) {
		this.viewModules = viewModules;
	}
	
	public VObject getViewChargement() {
		return viewChargement;
	}

	public void setViewChargement(VObject viewChargement) {
		this.viewChargement = viewChargement;
	}

	public VObject getViewNormage() {
		return this.viewNormage;
	}

	public void setViewNormage(VObject viewNormage) {
		this.viewNormage = viewNormage;
	}

	public VObject getViewControle() {
		return this.viewControle;
	}

	public void setViewControle(VObject viewControle) {
		this.viewControle = viewControle;
	}

	public VObject getViewMapping() {
		return this.viewMapping;
	}

	public void setViewMapping(VObject viewMapping) {
		this.viewMapping = viewMapping;
	}
	
	public VObject getViewExpression() {
		return viewExpression;
	}

	public void setViewExpression(VObject viewExpression) {
		this.viewExpression = viewExpression;
	}

	public VObject getViewJeuxDeReglesCopie() {
		return this.viewJeuxDeReglesCopie;
	}

	public void setViewJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie) {
		this.viewJeuxDeReglesCopie = viewJeuxDeReglesCopie;
	}

	/**
	 * @return the viewFiltrage
	 */
	public VObject getViewFiltrage() {
		return this.viewFiltrage;
	}

	/**
	 * @param viewFiltrage the viewFiltrage to set
	 */
	public void setViewFiltrage(VObject viewFiltrage) {
		this.viewFiltrage = viewFiltrage;
	}

	/**
	 * @return the selectedJeuDeRegle
	 */
	public String getSelectedJeuDeRegle() {
		return this.viewJeuxDeReglesCopie.getCustomValue(SELECTED_RULESET_TABLE);
	}

}
