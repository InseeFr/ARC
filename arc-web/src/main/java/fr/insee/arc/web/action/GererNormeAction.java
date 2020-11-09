package fr.insee.arc.web.action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import fr.insee.arc.core.dao.MappingRegleDao;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.model.RegleMappingEntity;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.engine.controle.ControleRegleService;
import fr.insee.arc.utils.dao.EntityDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.dao.GererNormeDao;
import fr.insee.arc.web.model.NormManagementModel;
import fr.insee.arc.web.util.ConstanteBD;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererNormeAction extends ArcAction<NormManagementModel> implements IDbConstant {
	
	private static final String RESULT_SUCCESS = "/jsp/gererNorme.jsp";

	private static final String SELECTED_RULESET_TABLE = "SELECTED_RULESET_TABLE";
	private static final String SELECTED_RULESET_NAME = "SELECTED_RULESET_NAME";

	private static final Logger LOGGER = LogManager.getLogger(GererNormeAction.class);

	@Autowired
	private ControleRegleService service;
	
	@Autowired
	private GererNormeDao gererNormeDao;

	// The norm view
	private VObject viewNorme;

	// The calendar view
	private VObject viewCalendar;

	// The ruleset view
	private VObject viewRulesSet;

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

	// The on ruleset to copy rules
	private VObject viewJeuxDeReglesCopie;

	@Override
	public void putAllVObjects(NormManagementModel model) {		
		setViewNorme(vObjectService.preInitialize(model.getViewNorme()));
		setViewCalendrier(vObjectService.preInitialize(model.getViewCalendrier()));
		setViewJeuxDeRegles(vObjectService.preInitialize(model.getViewJeuxDeRegles()));
		setViewChargement(vObjectService.preInitialize(model.getViewChargement()));
		setViewNormage(vObjectService.preInitialize(model.getViewNormage()));
		setViewControle(vObjectService.preInitialize(model.getViewControle()));
		setViewFiltrage(vObjectService.preInitialize(model.getViewFiltrage()));
		setViewMapping(vObjectService.preInitialize(model.getViewMapping()));
		setViewJeuxDeReglesCopie(vObjectService.preInitialize(model.getViewJeuxDeReglesCopie()));
		
		putVObject(getViewNorme(),
				t -> gererNormeDao.initializeViewNorme(t, getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORME)));
		//
		putVObject(getViewCalendrier(), t -> gererNormeDao.initializeViewCalendar(t, getViewNorme(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CALENDRIER)));
		//
		putVObject(getViewJeuxDeRegles(), t -> gererNormeDao.initializeViewRulesSet(t, getViewCalendrier(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_RULESETS)));
		//
		putVObject(getViewChargement(), t -> gererNormeDao.initializeChargement(t, getViewJeuxDeRegles(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE), getScope()));
		//
		putVObject(getViewNormage(), t -> gererNormeDao.initializeNormage(t, getViewJeuxDeRegles(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE), getScope()));
		//
		putVObject(getViewControle(), t -> gererNormeDao.initializeControle(t, getViewJeuxDeRegles(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE), getScope()));
		//
		putVObject(getViewFiltrage(), t -> gererNormeDao.initializeFiltrage(t, getViewJeuxDeRegles(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE), getScope()));
		//
		putVObject(getViewMapping(), t -> gererNormeDao.initializeMapping(t, getViewJeuxDeRegles(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE), getScope()));
		//
		putVObject(getViewJeuxDeReglesCopie(), t -> gererNormeDao.initializeJeuxDeReglesCopie(t, getViewJeuxDeRegles(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_RULESETS), getScope()));
	}

	@Override
	public String getActionName() {
		return "normManagement";
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
		return addLineVobject(model, RESULT_SUCCESS, this.viewCalendar);
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
		Map<String, ArrayList<String>> selection = viewCalendar.mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("calendar state: " + etat, LOGGER);
			// Check actived calendar (code 1)
			if ("1".equals(etat)) {
				this.viewCalendar.setMessage("Caution, cannot delete a active calendar");
			} else {
				this.vObjectService.delete(viewCalendar);
			}
		} else {
			this.viewRulesSet.setMessage("You didn't select anything");
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
		return updateVobject(model, RESULT_SUCCESS, this.viewCalendar);
	}

	@RequestMapping("/sortCalendrier")
	public String sortCalendrier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewCalendar);
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
		HashMap<String, ArrayList<String>> selection = viewRulesSet.mapInputFields();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
				this.viewRulesSet.setMessage("Caution, cannot add a rule set in the PRODUCTION state");
			} else {
				this.vObjectService.insert(viewRulesSet);
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
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("State to delete : " + etat, LOGGER);
			// Check production state. If yes cancel the delete and send a message to the
			// user
			if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
				this.viewRulesSet.setMessage("Caution, cannot delete a rule set in the PRODUCTION state");
			} else {
				this.vObjectService.delete(viewRulesSet);
			}
		} else {
			this.viewRulesSet.setMessage("You didn't select anything");
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
		HashMap<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();

		// on les crée dans tous les environnements et tous les entrepots
		// (ca evite les erreurs et car ca ne spécialise aucun environnement dans un
		// role à priori)

		if (!selection.isEmpty()) {
			for (int i = 0; i < selection.get("etat").size(); i++) {
				String etat = selection.get("etat").get(i);
				if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
					gererNormeDao.sendRuleSetToProduction(this.viewRulesSet,
							getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_BATCH));
				}
			}

			this.vObjectService.update(viewRulesSet);

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
		return sortVobject(model, RESULT_SUCCESS, this.viewRulesSet);
	}

	/**
	 * Get all the rules from on rule set, export them in CSV, zip and send to user
	 * 
	 * @return
	 */
	@RequestMapping("/downloadJeuxDeRegles")
	public String downloadJeuxDeRegles(Model model, HttpServletResponse response) {
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		if (!selection.isEmpty()) {
			StringBuilder requeteRegleChargement = new StringBuilder();
			requeteRegleChargement.append(gererNormeDao.recupRegle(this.viewRulesSet,
					getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE)));
			StringBuilder requeteRegleNormage = new StringBuilder();
			requeteRegleNormage.append(gererNormeDao.recupRegle(this.viewRulesSet,
					getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE)));
			StringBuilder requeteRegleControle = new StringBuilder();
			requeteRegleControle.append(gererNormeDao.recupRegle(this.viewRulesSet,
					getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE)));
			StringBuilder requeteRegleMapping = new StringBuilder();
			requeteRegleMapping.append(gererNormeDao.recupRegle(this.viewRulesSet,
					getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE)));
			StringBuilder requeteRegleFiltrage = new StringBuilder();
			requeteRegleFiltrage.append(gererNormeDao.recupRegle(this.viewRulesSet,
					getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE)));

			ArrayList<String> fileNames = new ArrayList<>();
			fileNames.add("Rules_load");
			fileNames.add("Rules_structurize");
			fileNames.add("Rules_control");
			fileNames.add("Rules_mapping");
			fileNames.add("Rules_filter");
			this.vObjectService.download(viewRulesSet, response, fileNames//
					, requeteRegleChargement.toString()//
					, requeteRegleNormage.toString()//
					, requeteRegleControle.toString()//
					, requeteRegleMapping.toString()//
					, requeteRegleFiltrage.toString());
			return "none";
		} else {
			this.viewRulesSet.setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

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
		gererNormeDao.uploadFileRule(getViewChargement(), viewRulesSet, fileUploadLoad);
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
		
		gererNormeDao.uploadFileRule(getViewNormage(), viewRulesSet, fileUploadStructurize);
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
		
		loggerDispatcher.info(String.format("Add rule : %s ", this.viewControle.getInputFields().toString()), LOGGER);
		boolean isToInsert = true;
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		/*
		 * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et
		 * calendrier
		 */
		JeuDeRegle jdr = new JeuDeRegle();
        jdr.setIdNorme(selection.get("id_norme").get(0));
        jdr.setPeriodicite(selection.get("periodicite").get(0));
        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
        jdr.setVersion(selection.get("version").get(0));
        
		/* Fabrication de la règle à ajouter */
		ArrayList<RegleControleEntity> listRegle = new ArrayList<>();
		RegleControleEntity reg = new RegleControleEntity(viewControle.mapInputFields());
		listRegle.add(reg);
		try {
			// Fabrication de la table temporaire pour tester l'insertion

			UtilitaireDao.get("arc").executeRequest(null,
					gererNormeDao.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
			// Insertion de cette règle dans la table temporaire
			isToInsert = this.service.ajouterRegles(jdr, "arc", listRegle);
		} catch (Exception e) {
			this.viewControle.setMessage(e.toString());
			loggerDispatcher.error(String.format("Error in addControle : %s", e.toString()), LOGGER);
			isToInsert = false;
		}
		// if rule to insert
		if (isToInsert) {
			// Insert the rule
			if (this.vObjectService.insert(viewControle)) {
				// if no exception
				loggerDispatcher.info("New rule inserted", LOGGER);
				this.viewControle.setMessage("New rule inserted");

			}
		}
		return generateDisplay(model, RESULT_SUCCESS);
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
		
		try {
			this.vObjectService.delete(viewControle);
		} catch (Exception e) {
			// else => error message
			this.viewControle.setMessage("Delete a rule from the rule set make it incoherent : " + e.getMessage());
			loggerDispatcher.error(String.format("Error in deleteControle : %s", e.toString()), LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
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
		

		JeuDeRegle jdr = gererNormeDao.fetchJeuDeRegle(this.viewRulesSet);
		loggerDispatcher.info("Mes nouvelles données : " + viewControle.listContentAfterUpdate().toString(),
				LOGGER);
		ArrayList<RegleControleEntity> listRegleNouv = new ArrayList<>();
		for (int i = 0; i < viewControle.listContentAfterUpdate().size(); i++) {
			RegleControleEntity reg = new RegleControleEntity(viewControle.mapContentAfterUpdate(i));
			listRegleNouv.add(reg);
		}
		try {
			// Fabrication de la table temporaire pour tester la modifcation
			UtilitaireDao.get("arc").executeRequest(null,
					gererNormeDao.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
			// suppression des lignes modifiées
			this.vObjectService.deleteForUpdate(viewControle, "arc.test_ihm_" + TraitementTableParametre.CONTROLE_REGLE.toString());
			// test du nouveau paquet en passant par la méthode ajouterRegles()
			// afin de lancer la batterie de test (borne_inf<borne_sup etc.)
			this.service.ajouterRegles(jdr, "arc", listRegleNouv);
			this.viewControle.setMessage("Rules updated !");
			this.vObjectService.update(viewControle);
		} catch (Exception e) {
			this.viewControle.setMessage("Updating the rule set make it incoherent : " + e.toString());
			loggerDispatcher.error(String.format("Error in updateControle : %s", e.toString()), LOGGER);
		}

		return generateDisplay(model, RESULT_SUCCESS);
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
		
		loggerDispatcher.info("importControle", LOGGER);
		String fichierRegle = "";
		if (fileUploadControle == null || fileUploadControle.isEmpty()) {
			this.viewControle.setMessage("You should choose a file first");
		} else {

			try {
				fichierRegle = new String(fileUploadControle.getBytes());
			} catch (IOException e) {
				loggerDispatcher.error(String.format("Error with the file in importControle : %s", e.toString()),
						LOGGER);
			}
			loggerDispatcher.info(String.format("I have a file %s character long", fichierRegle.length()), LOGGER);
			boolean isAjouter = true;
			Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
			/*
			 * Create a RuleSet to keep informations about the norm and calendar
			 */
			JeuDeRegle jdr = new JeuDeRegle();
	        jdr.setIdNorme(selection.get("id_norme").get(0));
	        jdr.setPeriodicite(selection.get("periodicite").get(0));
	        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
	        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
	        jdr.setVersion(selection.get("version").get(0));
	        
			ArrayList<RegleControleEntity> listRegle = this.service
					.miseEnRegleC(ControleRegleService.nomTableRegleControle("arc.test_ihm", true), fichierRegle);
			try {
				// Create a temporary table to test the importing
				UtilitaireDao.get("arc").executeRequest(null,
						gererNormeDao.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
				// Insert the rules in the temporary table
				isAjouter = this.service.ajouterRegles(jdr, "arc", listRegle);
			} catch (Exception e) {
				this.viewControle.setMessage(e.toString());
				loggerDispatcher.error("Error when importing controle rules " + e.toString(), LOGGER);
				isAjouter = false;
			}
			// Import result
			if (isAjouter) {
				try {
					// Add the rule in database
					this.service.ajouterReglesValidees(jdr, "arc", listRegle);
				} catch (Exception e) {
					loggerDispatcher.error("Error when importing valid controle rules " + e.toString(), LOGGER);

				}
				loggerDispatcher.info("New rules inserted", LOGGER);
				this.viewControle.setMessage("New rules inserted");
			}
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the loading rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderChargement")
	public String viderChargement(Model model) {
		
		gererNormeDao.emptyRuleTable(this.viewRulesSet,
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the structure rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderNormage")
	public String viderNormage(Model model) {
		
		gererNormeDao.emptyRuleTable(this.viewRulesSet,
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the control rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderControle")
	public String viderControle(Model model) {
		
		gererNormeDao.emptyRuleTable(this.viewRulesSet,
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the filter rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderFiltrage")
	public String viderFiltrage(Model model) {
		
		gererNormeDao.emptyRuleTable(this.viewRulesSet,
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Clean the map rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderMapping")
	public String viderMapping(Model model) {
		
		gererNormeDao.emptyRuleTable(this.viewRulesSet,
				getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);

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
		
		boolean isRegleOk = true;
        loggerDispatcher.info("Contenu de l'update : " + viewFiltrage.listContentAfterUpdate(), LOGGER);
        String exprRegleFiltre = viewFiltrage.listContentAfterUpdate().get(0).get(6);

		StringBuilder message = new StringBuilder();
		try {
			// Create test table
			UtilitaireDao.get("arc").executeRequest(null, gererNormeDao.createTableTest("arc.test_ihm_controle_ok",
					ManipString.extractRubriques(exprRegleFiltre)));

			UtilitaireDao.get("arc").executeRequest(null,
					"SELECT * FROM arc.test_ihm_controle_ok WHERE " + ManipString.extractAllRubrique(exprRegleFiltre));
			loggerDispatcher.info("La requete de test ? " + "SELECT * FROM arc.test_ihm_controle_ok WHERE "
					+ ManipString.extractAllRubrique(exprRegleFiltre), LOGGER);
			message.append("Rules updated!");
		} catch (Exception ex) {
			isRegleOk = false;
			message.append("Error when inserting the new rules : " + ex.getMessage());
			loggerDispatcher.error("Error when inserting the new rules : " + ex.getMessage(), LOGGER);
		}

		this.viewFiltrage.setMessage(message.toString());
		if (isRegleOk) {
			this.vObjectService.update(viewFiltrage);
		}
		return generateDisplay(model, RESULT_SUCCESS);

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
		Map<String, ArrayList<String>> afterUpdate = viewMapping.mapContentAfterUpdate();
		boolean isRegleOk = gererNormeDao.testerReglesMapping(this.viewMapping, this.viewRulesSet, this.viewNorme,
				afterUpdate);
		if (isRegleOk) {
			this.vObjectService.update(viewMapping);
		}
		LOGGER.info("Rules updated");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by uploading a filter rule file
	 * 
	 * @return
	 */
	@RequestMapping("/importFiltrage")
	public String importFiltrage(Model model, MultipartFile fileUploadFilter) {
		
		gererNormeDao.uploadFileRule(this.viewFiltrage, this.viewRulesSet, fileUploadFilter);
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
			Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();

			 UtilitaireDao.get("arc").executeRequest(null, new StringBuilder("INSERT INTO " + this.viewFiltrage.getTable())//
	                    .append("  " + Format.stringListe(this.viewFiltrage.getHeadersDLabel()))//
	                    .append("  SELECT (SELECT coalesce(max(id_regle),1) FROM " + this.viewFiltrage.getTable() + ")+row_number() over () ,")//
	                    .append("  '" + selection.get("id_norme").get(0) + "', ")//
	                    .append("  '" + selection.get("validite_inf").get(0) + "', ")//
	                    .append("  '" + selection.get("validite_sup").get(0) + "', ")//
	                    .append("  '" + selection.get("version").get(0) + "', ")//
	                    .append("  '" + selection.get("periodicite").get(0) + "', ")//
	                    .append("  null,")//
	                    .append("  null;"));
		} catch (SQLException e) {
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
		
		if (fileUploadMap == null || fileUploadMap.isEmpty()) {
			this.viewMapping.setMessage("You should choose a file first");
		} else {
			boolean isRegleOk = false;
			try {
				Map<String, String> mapVariableToType = new HashMap<>();
				Map<String, String> mapVariableToTypeConso = new HashMap<>();
				gererNormeDao.calculerVariableToType(this.viewNorme, mapVariableToType, mapVariableToTypeConso);
				Set<String> variablesAttendues = mapVariableToType.keySet();
				String nomTable = "arc.ihm_mapping_regle";
				List<RegleMappingEntity> listeRegle = new ArrayList<>();
				EntityDao<RegleMappingEntity> dao = new MappingRegleDao();
				dao.setTableName(nomTable);
				dao.setEOLSeparator(true);
				Map<String, ArrayList<String>> reglesAImporter = gererNormeDao.calculerReglesAImporter(
						fileUploadMap, listeRegle, dao, mapVariableToType, mapVariableToTypeConso);
				Set<String> variablesSoumises = new HashSet<>();
				for (int i = 0; i < listeRegle.size(); i++) {
					variablesSoumises.add(Format.toLowerCase(listeRegle.get(i).getVariableSortie()));
				}
				Set<String> variablesAttenduesTemp = new HashSet<>(variablesAttendues);
				variablesAttenduesTemp.removeAll(variablesSoumises);
				if (!variablesAttenduesTemp.isEmpty()) {
					throw new IllegalStateException(
							"Variables " + variablesAttenduesTemp + " do not have format rules.");
				}
				variablesSoumises.removeAll(variablesAttendues);
				if (!variablesSoumises.isEmpty()) {
					throw new IllegalStateException("Variables " + variablesSoumises + " are not in the model.");
				}
				isRegleOk = gererNormeDao.testerReglesMapping(this.viewMapping, this.viewRulesSet, this.viewNorme,
						reglesAImporter);
				Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
				Map<String, String> map = new HashMap<String, String>();
                map.put("id_regle", "(SELECT max(id_regle)+1 FROM " + nomTable + ")");
                map.put("id_norme", viewNorme.mapContentSelected().get("id_norme").get(0));
                map.put("validite_inf", selection.get("validite_inf").get(0));
                map.put("validite_sup", selection.get("validite_sup").get(0));
                map.put("version", selection.get("version").get(0));
                map.put("periodicite", selection.get("periodicite").get(0));
                
				if (isRegleOk) {
					// check if each varialbe have a rule
					JeuDeRegle jdr = gererNormeDao.fetchJeuDeRegle(this.viewRulesSet);
					StringBuilder bloc = new StringBuilder();
					/*
					 * DELETE from
					 */
					bloc.append("DELETE FROM " + nomTable + " WHERE " + jdr.getSqlEquals() + ";");
					for (int i = 0; i < listeRegle.size(); i++) {
						bloc.append(dao.getInsert(listeRegle.get(i), map));
					}
					UtilitaireDao.get(poolName).executeBlock(null, bloc);
				}
			} catch (Exception ex) {
				LoggerHelper.error(LOGGER, "importMapping()", ex.getStackTrace());
				this.viewMapping.setMessage("Erreur lors de l'import : " + ex.toString());
			}
		}
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
			StringBuilder requete = new StringBuilder("INSERT INTO " + this.viewMapping.getTable()).append(
					"  (id_regle, id_norme, validite_inf, validite_sup,  version , periodicite, variable_sortie, expr_regle_col, commentaire) ")
					.append("  SELECT coalesce((SELECT max(id_regle) FROM " + this.viewMapping.getTable()
							+ "),0)+row_number() over () ,")
					.append("  '" + viewRulesSet.mapContentSelected().get(ConstanteBD.ID_NORME.getValue()).get(0)
							+ "', ")
					.append("  '"
							+ viewRulesSet.mapContentSelected().get(ConstanteBD.VALIDITE_INF.getValue()).get(0)
							+ "', ")
					.append("  '"
							+ viewRulesSet.mapContentSelected().get(ConstanteBD.VALIDITE_SUP.getValue()).get(0)
							+ "', ")
					.append("  '" + viewRulesSet.mapContentSelected().get(ConstanteBD.VERSION.getValue()).get(0)
							+ "', ")
					.append("  '"
							+ viewRulesSet.mapContentSelected().get(ConstanteBD.PERIODICITE.getValue()).get(0)
							+ "', ")
					.append("  liste_colonne.nom_variable_metier,").append("  null,").append(
							"  null")
					.append("  FROM ("
							+ FormatSQL.listeColonneTableMetierSelonFamilleNorme("arc.ihm",
									viewNorme.mapContentSelected().get(ConstanteBD.ID_FAMILY.getValue()).get(0))
							+ ") liste_colonne");
			UtilitaireDao.get("arc").executeRequest(null, requete);
		} catch (SQLException e) {
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

	@RequestMapping("/selectJeuxDeReglesCopie")
	public String selectJeuxDeReglesCopie(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/copieJeuxDeRegles")
	public String copieJeuxDeRegles(Model model) {
		loggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
		// le jeu de regle à copier
		Map<String, ArrayList<String>> selectionOut = viewRulesSet.mapContentSelected();
		// le nouveau jeu de regle
		Map<String, ArrayList<String>> selectionIn = viewJeuxDeReglesCopie.mapContentSelected();
		HashMap<String, String> type = viewJeuxDeReglesCopie.mapHeadersType();
		if (!selectionIn.isEmpty()) {
			StringBuilder requete = new StringBuilder();
			requete.append("INSERT INTO " + this.getSelectedJeuDeRegle() + " ");
			if (this.getSelectedJeuDeRegle().equals("arc.ihm_normage_regle")) {
				gererNormeDao.emptyRuleTable(this.viewRulesSet,
						getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE));
				requete.append("(");
				requete.append(String.join(",", ConstanteBD.ID_NORME.getValue()//
						, ConstanteBD.PERIODICITE.getValue()//
						, ConstanteBD.VALIDITE_INF.getValue()//
						, ConstanteBD.VALIDITE_SUP.getValue()//
						, ConstanteBD.VERSION.getValue()//
						, ConstanteBD.ID_CLASS.getValue()//
						, ConstanteBD.RUBRIQUE_NMCL.getValue()//
						, ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.COMMENTAIRE.getValue()));

				requete.append(")");
				requete.append("SELECT ");
				requete.append(String.join(",", "'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
						, ConstanteBD.ID_CLASS.getValue()//
						, ConstanteBD.RUBRIQUE_NMCL.getValue()//
						, ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.COMMENTAIRE.getValue()));

			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_controle_regle")) {
				gererNormeDao.emptyRuleTable(this.viewRulesSet,
						getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE));
				requete.append("(");
				requete.append(String.join(",", ConstanteBD.ID_NORME.getValue()//
						, ConstanteBD.PERIODICITE.getValue()//
						, ConstanteBD.VALIDITE_INF.getValue()//
						, ConstanteBD.VALIDITE_SUP.getValue()//
						, ConstanteBD.VERSION.getValue()//
						, ConstanteBD.ID_CLASS.getValue()//
						, ConstanteBD.RUBRIQUE_PERE.getValue()//
						, ConstanteBD.RUBRIQUE_FILS.getValue()//
						, ConstanteBD.BORNE_INF.getValue()//
						, ConstanteBD.BORNE_SUP.getValue()//
						, ConstanteBD.CONDITION.getValue()//
						, ConstanteBD.PRE_ACTION.getValue()//
						, ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.COMMENTAIRE.getValue()));

				requete.append(")");

				requete.append("SELECT ");
				requete.append(String.join(",", "'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
						, ConstanteBD.ID_CLASS.getValue()//
						, ConstanteBD.RUBRIQUE_PERE.getValue()//
						, ConstanteBD.RUBRIQUE_FILS.getValue()//
						, ConstanteBD.BORNE_INF.getValue()//
						, ConstanteBD.BORNE_SUP.getValue()//
						, ConstanteBD.CONDITION.getValue()//
						, ConstanteBD.PRE_ACTION.getValue()//
						, ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.COMMENTAIRE.getValue()));

			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_filtrage_regle")) {
				gererNormeDao.emptyRuleTable(this.viewRulesSet,
						getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE));
				requete.append("(");
				requete.append(String.join(",", ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.ID_NORME.getValue()//
						, ConstanteBD.PERIODICITE.getValue()//
						, ConstanteBD.VALIDITE_INF.getValue()//
						, ConstanteBD.VALIDITE_SUP.getValue()//
						, ConstanteBD.VERSION.getValue()//
						, ConstanteBD.EXPR_REGLE_FILTRE.getValue()//
						, ConstanteBD.COMMENTAIRE.getValue()));

				requete.append(")");

				requete.append("SELECT ");
				requete.append(String.join(",",
						"row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + ")",
						"'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
						, ConstanteBD.EXPR_REGLE_FILTRE.getValue()//
						, ConstanteBD.COMMENTAIRE.getValue()));

			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_mapping_regle")) {
				gererNormeDao.emptyRuleTable(this.viewRulesSet,
						getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE));

				requete.append("(");
				requete.append(String.join(",", ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.ID_NORME.getValue()//
						, ConstanteBD.PERIODICITE.getValue()//
						, ConstanteBD.VALIDITE_INF.getValue()//
						, ConstanteBD.VALIDITE_SUP.getValue()//
						, ConstanteBD.VERSION.getValue()//
						, ConstanteBD.VARIABLE_SORTIE.getValue()//
						, ConstanteBD.EXPR_REGLE_COL.getValue(), ConstanteBD.COMMENTAIRE.getValue()));

				requete.append(")");

				requete.append("SELECT ");
				requete.append(String.join(",",
						"row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + ")",
						"'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date "//
						, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
						, ConstanteBD.VARIABLE_SORTIE.getValue()//
						, ConstanteBD.EXPR_REGLE_COL.getValue(), ConstanteBD.COMMENTAIRE.getValue()));

			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_chargement_regle")) {
				gererNormeDao.emptyRuleTable(this.viewRulesSet,
						getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE));
				requete.append("(");
				requete.append(String.join(",", ConstanteBD.ID_REGLE.getValue()//
						, ConstanteBD.ID_NORME.getValue()//
						, ConstanteBD.PERIODICITE.getValue()//
						, ConstanteBD.VALIDITE_INF.getValue()//
						, ConstanteBD.VALIDITE_SUP.getValue()//
						, ConstanteBD.VERSION.getValue()//
						, ConstanteBD.TYPE_FICHIER.getValue()//
						, ConstanteBD.DELIMITER.getValue(), ConstanteBD.FORMAT.getValue(),
						ConstanteBD.COMMENTAIRE.getValue()));

				requete.append(")");

				requete.append("SELECT ");
				requete.append(String.join(" , ",
						"row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + ")",
						"'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date"//
						, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date"//
						, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
						, ConstanteBD.TYPE_FICHIER.getValue()//
						, ConstanteBD.DELIMITER.getValue(), ConstanteBD.FORMAT.getValue(),
						ConstanteBD.COMMENTAIRE.getValue()));

			}
			requete.append(" FROM " + this.getSelectedJeuDeRegle() + "  ");

			requete.append(" WHERE ");

			requete.append(String.join(" AND ", //
					// condition about id_norm
					ConstanteBD.ID_NORME.getValue() + ManipString.sqlEqual(
							selectionIn.get(ConstanteBD.ID_NORME.getValue()).get(0),
							type.get(ConstanteBD.ID_NORME.getValue())),
					ConstanteBD.PERIODICITE.getValue()
							// condition about PERIODICITE
							+ ManipString
									.sqlEqual(selectionIn.get(ConstanteBD.PERIODICITE.getValue()).get(0),
											type.get(ConstanteBD.PERIODICITE.getValue())),
					ConstanteBD.VALIDITE_INF.getValue()
							// condition about VALIDITE_INF
							+ ManipString.sqlEqual(selectionIn.get(ConstanteBD.VALIDITE_INF.getValue()).get(0),
									type.get(ConstanteBD.VALIDITE_INF.getValue())),
					ConstanteBD.VALIDITE_SUP.getValue()
							// condition about VALIDITE_SUP
							+ ManipString
									.sqlEqual(selectionIn.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0),
											type.get(ConstanteBD.VALIDITE_SUP.getValue())),
					ConstanteBD.VERSION.getValue()
							// condition about VERSION
							+ ManipString.sqlEqual(selectionIn.get(ConstanteBD.VERSION.getValue()).get(0),
									type.get(ConstanteBD.VERSION.getValue()))

			));

			requete.append(" ;");

			try {
				UtilitaireDao.get("arc").executeRequest(getQueryHandler().getWrapped(), requete);
			} catch (SQLException ex) {
				loggerDispatcher.error("Error in copieJeuxDeRegles", ex, LOGGER);
			}
			this.vObjectService.destroy(viewJeuxDeReglesCopie);
		} else {
			loggerDispatcher.info("No rule set choosed", LOGGER);
			this.viewRulesSet.setMessage("Please choose a ruleset");
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
		return this.viewCalendar;
	}

	public void setViewCalendrier(VObject viewCalendrier) {
		this.viewCalendar = viewCalendrier;
	}

	public VObject getViewJeuxDeRegles() {
		return this.viewRulesSet;
	}

	public void setViewJeuxDeRegles(VObject viewJeuxDeRegles) {
		this.viewRulesSet = viewJeuxDeRegles;
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
