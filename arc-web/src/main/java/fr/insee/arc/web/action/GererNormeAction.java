package fr.insee.arc.web.action;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dao.MappingRegleDao;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.core.model.RegleMappingEntity;
import fr.insee.arc.core.model.RuleSets;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ControleRegleService;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.dao.EntityDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.dao.NormManagementDao;
import fr.insee.arc.web.util.ConstanteBD;
import fr.insee.arc.web.util.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererNorme.jsp"),
	@Result(name = "index", location = "/jsp/index.jsp") })
public class GererNormeAction extends ArcAction {

    private static final String SELECTED_JEU_DE_REGLE = "selectedJeuDeRegle";

    private static final Logger LOGGER = Logger.getLogger(GererNormeAction.class);

    @Autowired
    ControleRegleService service;

    // The norm view
    @Autowired
    @Qualifier("viewNorme")
    VObject viewNorme;

    // The calendar view
    @Autowired
    @Qualifier("viewCalendrier")
    VObject viewCalendar;

    // The ruleset view
    @Autowired
    @Qualifier("viewJeuxDeRegles")
    VObject viewRulesSet;

    // The load rules view
    @Autowired
    @Qualifier("viewChargement")
    VObject viewChargement;

    // The structurize rules view
    @Autowired
    @Qualifier("viewNormage")
    VObject viewNormage;

    // The control rules view
    @Autowired
    @Qualifier("viewControle")
    VObject viewControle;

    // The filter rules view
    @Autowired
    @Qualifier("viewFiltrage")
    VObject viewFiltrage;

    // The map to format rules view
    @Autowired
    @Qualifier("viewMapping")
    VObject viewMapping;

    // The on ruleset to copy rules
    @Autowired
    @Qualifier("viewJeuxDeReglesCopie")
    VObject viewJeuxDeReglesCopie;

    /*
     * All the file that the user can upload. The user cannot load multiple file at
     * one, so only one File object is really needed, but multiple files with
     * significant name are better
     */
    private File fileUploadLoad;
    private File fileUploadStructurize;
    private File fileUploadControle;
    private File fileUploadFilter;
    private File fileUploadMap;

    private String fileUploadContentType;
    private String fileUploadFileName;

    @Override
    public void putAllVObjects() {
	putVObject(getViewNorme(), t -> NormManagementDao.initializeViewNorme(t,
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORME)));
	//
	putVObject(getViewCalendrier(), t -> NormManagementDao.initializeViewCalendar(t, getViewNorme(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CALENDRIER)));
	//
	putVObject(getViewJeuxDeRegles(), t -> NormManagementDao.initializeViewRulesSet(t, getViewCalendrier(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_RULESETS)));
	//
	putVObject(getViewChargement(), t -> NormManagementDao.initializeModuleRules(t, getViewJeuxDeRegles(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE)));
	//
	putVObject(getViewNormage(), t -> NormManagementDao.initializeModuleRules(t, getViewJeuxDeRegles(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE)));
	//
	putVObject(getViewControle(), t -> NormManagementDao.initializeModuleRules(t, getViewJeuxDeRegles(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE)));
	//
	putVObject(getViewFiltrage(), t -> NormManagementDao.initializeModuleRules(t, getViewJeuxDeRegles(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE)));
	//
	putVObject(getViewMapping(), t -> NormManagementDao.initializeMapping(t, getViewJeuxDeRegles(),
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE)));
	//
	putVObject(getViewJeuxDeReglesCopie(), t -> NormManagementDao.initializeJeuxDeReglesCopie(t,
		getViewJeuxDeRegles(), getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_RULESETS)));

    }

    @Override
    public void instanciateAllDAOs() {
	// No DAO in this action class

    }

    @Override
    public void setProfilsAutorises() {
	// No profil handling

    }

    @Override
    protected void specificTraitementsPostDAO() {
	// nothing to do

    }

    @Override
    public String getActionName() {
	return null;
    }

    /**
     * Action trigger by selecting a norm in the GUI. Update the GUI
     * 
     * @return success
     */
    @Action(value = "/selectNorme")
    public String selectNorme() {
	return basicAction();
    }

    /**
     * Action trigger by adding a norm in the GUI.
     * 
     * @return success
     */
    @Action(value = "/addNorme")
    public String addNorme() {
	return addLineVobject(this.viewNorme);
    }

    /**
     * Action trigger by deleting a norm in the GUI. Cannot delete a active norm
     *
     * @return success
     */
    @Action(value = "/deleteNorme")
    public String deleteNorme() {
	initialize();
	// Get the gui selection
	Map<String, ArrayList<String>> selection = this.viewNorme.mapContentSelected();

	if (!selection.isEmpty()) {
	    String etat = selection.get("etat").get(0);
	    LoggerDispatcher.info("Norm state : " + etat, LOGGER);
	    // Check actived norm (code 1)
	    if ("1".equals(etat)) {
		this.viewNorme.setMessage("Caution, cannot delete a activated norm");
	    } else {
		this.viewNorme.delete();
	    }
	} else {
	    this.viewNorme.setMessage("You didn't select anything");
	}
	return generateDisplay();
    }

    /**
     * Action trigger by updating a norm in the GUI. Update the GUI
     */
    @Action(value = "/updateNorme")
    public String updateNorme() {
	return updateVobject(this.viewNorme);
    }

    /**
     * Action trigger by sorting a norm in the GUI. Update the GUI
     */
    @Action(value = "/sortNorme")
    public String sortNorme() {
	return sortVobject(this.viewNorme);
    }

    /**
     * Action trigger by selecting a calendar in the GUI. Update the GUI
     * 
     * @return success
     */
    @Action(value = "/selectCalendrier")
    public String selectCalendrier() {
	return basicAction();
    }

    /**
     * Action trigger by adding a calendar in the GUI. Update the GUI and the
     * database
     * 
     * @return success
     */
    @Action(value = "/addCalendrier")
    public String addCalendrier() {
	return addLineVobject(this.viewCalendar);
    }

    /**
     * Action trigger by deleting a calendar in the GUI. Cannot delete a activated
     * calendar. Update the GUI and the database
     * 
     * @return
     */
    @Action(value = "/deleteCalendrier")
    public String deleteCalendrier() {
	initialize();
	// get the selected calendar
	Map<String, ArrayList<String>> selection = this.viewCalendar.mapContentSelected();
	if (!selection.isEmpty()) {
	    String etat = selection.get("etat").get(0);
	    LoggerDispatcher.info("calendar state: " + etat, LOGGER);
	    // Check actived calendar (code 1)
	    if ("1".equals(etat)) {
		this.viewCalendar.setMessage("Caution, cannot delete a active calendar");
	    } else {
		this.viewCalendar.delete();
	    }
	} else {
	    this.viewRulesSet.setMessage("You didn't select anything");
	}
	return generateDisplay();
    }

    /**
     * Action trigger by updating a calendar in the GUI. Update the GUI and the
     * database
     * 
     * @return success
     */
    @Action(value = "/updateCalendrier")
    public String updateCalendrier() {
	return updateVobject(this.viewCalendar);
    }

    @Action(value = "/sortCalendrier")
    public String sortCalendrier() {
	return sortVobject(this.viewCalendar);
    }

    /**
     * Action trigger by selecting a rule set in the GUI. Update the GUI
     * 
     * @return success
     */
    @Action(value = "/selectJeuxDeRegles")
    public String selectRuleSet() {
	return basicAction();
    }

    /**
     * Action trigger by adding a rule set in the GUI. Cannot add a rule set in
     * production state. Update the GUI and the database
     * 
     * @return success
     */
    @Action(value = "/addJeuxDeRegles")
    public String addRuleSet() {
	initialize();
	HashMap<String, ArrayList<String>> selection = this.viewRulesSet.mapInputFields();
	if (!selection.isEmpty()) {
	    String etat = selection.get("etat").get(0);
	    if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
		this.viewRulesSet.setMessage("Caution, cannot add a rule set in the PRODUCTION state");
	    } else {
		this.viewRulesSet.insertWIP();
	    }
	}

	return generateDisplay();
    }

    /**
     * Action trigger by deleting a rule set in the GUI. Cannot delete a rule state
     * in the PRODUCTIONS state. Update the GUI and the database
     * 
     * @return sucess
     */
    @Action(value = "/deleteJeuxDeRegles")
    public String deleteRuleSet() {
	initialize();
	// Get the selection
	Map<String, ArrayList<String>> selection = this.viewRulesSet.mapContentSelected();
	if (!selection.isEmpty()) {
	    String etat = selection.get("etat").get(0);
	    LoggerDispatcher.info("State to delete : " + etat, LOGGER);
	    // Check production state. If yes cancel the delete and send a message to the
	    // user
	    if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
		this.viewRulesSet.setMessage("Caution, cannot delete a rule set in the PRODUCTION state");
	    } else {
		this.viewRulesSet.delete();
	    }
	} else {
	    this.viewRulesSet.setMessage("You didn't select anything");
	}

	return generateDisplay();
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
    @Action(value = "/updateJeuxDeRegles")
    public String updateRuleSet() {
	initialize();
	HashMap<String, ArrayList<String>> selection = this.viewRulesSet.mapSameContentFromPreviousVObject();

	// on les crée dans tous les environnements et tous les entrepots
	// (ca evite les erreurs et car ca ne spécialise aucun environnement dans un
	// role à priori)
	// TODO EXPLAIN

	if (!selection.isEmpty()) {
	    for (int i = 0; i < selection.get("etat").size(); i++) {
		String etat = selection.get("etat").get(i);
		if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
		    NormManagementDao.sendRuleSetToProduction(this.viewRulesSet,
			    getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_BATCH));
		}
	    }

	    this.viewRulesSet.update();

	}

	return generateDisplay();
    }

    /**
     * Action trigger by sorting the ruleset in the GUI. Update the GUI
     * 
     * @return success
     */
    @Action(value = "/sortJeuxDeRegles")
    public String sortRuleSet() {
	return sortVobject(this.viewRulesSet);
    }

    /**
     * Get all the rules from on rule set, export them in CSV, zip and send to user
     * 
     * @return
     */
    @Action(value = "/downloadJeuxDeRegles")
    public String downloadJeuxDeRegles() {
	initialize();
	Map<String, ArrayList<String>> selection = this.viewRulesSet.mapContentSelected();
	if (!selection.isEmpty()) {
	    StringBuilder requeteRegleChargement = new StringBuilder();
	    requeteRegleChargement.append(NormManagementDao.recupRegle(this.viewRulesSet,
		    getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE)));
	    StringBuilder requeteRegleNormage = new StringBuilder();
	    requeteRegleNormage.append(NormManagementDao.recupRegle(this.viewRulesSet,
		    getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE)));
	    StringBuilder requeteRegleControle = new StringBuilder();
	    requeteRegleControle.append(NormManagementDao.recupRegle(this.viewRulesSet,
		    getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE)));
	    StringBuilder requeteRegleMapping = new StringBuilder();
	    requeteRegleMapping.append(NormManagementDao.recupRegle(this.viewRulesSet,
		    getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE)));
	    StringBuilder requeteRegleFiltrage = new StringBuilder();
	    requeteRegleFiltrage.append(NormManagementDao.recupRegle(this.viewRulesSet,
		    getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE)));

	    ArrayList<String> fileNames = new ArrayList<>();
	    fileNames.add("Rules_load");
	    fileNames.add("Rules_structurize");
	    fileNames.add("Rules_control");
	    fileNames.add("Rules_mapping");
	    fileNames.add("Rules_filter");
	    this.viewRulesSet.download(fileNames//
		    , requeteRegleChargement.toString()//
		    , requeteRegleNormage.toString()//
		    , requeteRegleControle.toString()//
		    , requeteRegleMapping.toString()//
		    , requeteRegleFiltrage.toString());
	    return "none";
	} else {
	    this.viewRulesSet.setMessage("You didn't select anything");
	    return generateDisplay();
	}

    }

    /**
     * Action trigger when the table of load rule is asked or refresh. Update the
     * GUI
     * 
     * @return success
     */
    @Action(value = "/selectChargement")
    public String selectChargement() {
	return basicAction();
    }

    /**
     * Action trigger when the table of load rule is asked or refresh. Update the
     * GUI
     * 
     * @return success
     */
    @Action(value = "/addChargement")
    public String addChargement() {
	return addLineVobject(this.viewChargement);
    }

    /**
     * Action trigger by adding a load rule in the GUI. Update the GUI and the
     * database
     *
     * @return
     */
    @Action(value = "/deleteChargement")
    public String deleteChargement() {
	return deleteLineVobject(this.viewChargement);
    }

    /**
     * Action trigger by updating a load rule in the GUI. Update the GUI and the
     * database.
     * 
     * @return
     */
    @Action(value = "/updateChargement")
    public String updateChargement() {
	return updateVobject(this.viewChargement);
    }

    /**
     * Action trigger by updating a load rule in the GUI. Update the GUI and the
     * database.
     * 
     * @return
     */
    @Action(value = "/sortChargement")
    public String sortChargement() {
	initialize();
	this.viewChargement.sort();
	return generateDisplay();
    }

    /**
     * Action trigger by importing load rules in the GUI. Update the GUI and the
     * database.
     * 
     * @return
     */
    @Action(value = "/importChargement")
    public String importChargement() {
	initialize();
	NormManagementDao.uploadFileRule(getViewChargement(), viewRulesSet, this.fileUploadLoad);
	return generateDisplay();
    }

    /**
     * Action trigger by importing structurize rules in the GUI. Update the GUI and
     * the database.
     * 
     * @return
     */
    @Action(value = "/importNormage")
    public String importNormage() {
	initialize();
	NormManagementDao.uploadFileRule(getViewNormage(), viewRulesSet, this.fileUploadStructurize);
	return generateDisplay();
    }

    /**
     * Action trigger when the table of structuize rules is request or refresh.
     * Update the GUI
     * 
     * @return success
     */
    @Action(value = "/selectNormage")
    public String selectNormage() {
	return basicAction();
    }

    /**
     * Action trigger by adding a structurize rule in the GUI. Update the GUI and
     * the database
     * 
     * @return success
     */
    @Action(value = "/addNormage")
    public String addNormage() {
	return addLineVobject(this.viewNormage);
    }

    /**
     * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
     * the database
     * 
     * @return
     */
    @Action(value = "/deleteNormage")
    public String deleteNormage() {
	return deleteLineVobject(this.viewNormage);
    }

    /**
     * Action trigger by updating the structurize rule in the GUI. Update the GUI
     * and the database
     * 
     * @return
     */
    @Action(value = "/updateNormage")
    public String updateNormage() {
	return updateVobject(this.viewNormage);
    }

    /**
     * Action trigger by sorting the structurize rule in the GUI. Update the GUI.
     * 
     * @return
     */

    @Action(value = "/sortNormage")
    public String sortNormage() {
	return sortVobject(this.viewNormage);
    }

    /**
     * Action trigger when the table of control rules is request or refresh. Update
     * the GUI
     * 
     * @return success
     */
    @Action(value = "/selectControle")
    public String selectControle() {
	return basicAction();
    }

    /**
     * Action trigger by adding a control rule in the GUI. Update the GUI and the
     * database
     * 
     * @return success
     */
    @SQLExecutor
    @Action(value = "/addControle")
    public String addControle() {
	initialize();
	LoggerDispatcher.info(String.format("Add rule : %s ", this.viewControle.getInputFields().toString()), LOGGER);
	boolean isToInsert = true;
	Map<String, ArrayList<String>> selection = this.viewRulesSet.mapContentSelected();
	/*
	 * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et
	 * calendrier
	 */
	RuleSets jdr = new RuleSets();
	jdr.setIdNorme(selection.get(ConstanteBD.ID_NORME.getValue()).get(0));
	jdr.setPeriodicite(selection.get(ConstanteBD.PERIODICITE.getValue()).get(0));
	jdr.setValiditeInfString(selection.get(ConstanteBD.VALIDITE_INF.getValue()).get(0),
		EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	jdr.setValiditeSupString(selection.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0),
		EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	jdr.setVersion(selection.get(ConstanteBD.VERSION.getValue()).get(0));
	/* Fabrication de la règle à ajouter */
	ArrayList<RegleControleEntity> listRegle = new ArrayList<>();
	RegleControleEntity reg = new RegleControleEntity(this.viewControle.mapInputFields());
	listRegle.add(reg);
	try {
	    // Fabrication de la table temporaire pour tester l'insertion

	    UtilitaireDao.get("arc").executeRequest(null,
		    NormManagementDao.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
	    // Insertion de cette règle dans la table temporaire
	    isToInsert = this.service.ajouterRegles(jdr, "arc", listRegle);
	} catch (Exception e) {
	    this.viewControle.setMessage(e.toString());
	    LoggerDispatcher.error(String.format("Error in addControle : %s", e.toString()), LOGGER);
	    isToInsert = false;
	}
	// if rule to insert
	if (isToInsert) {

	    // Insert the rule
	    if (this.viewControle.insertWIP()) {
		// if no exception
		LoggerDispatcher.info("New rule inserted", LOGGER);
		this.viewControle.setMessage("New rule inserted");

	    }
	}
	return generateDisplay();
    }

    /**
     * Action trigger by deleting a control rule in the GUI. Update the GUI and the
     * database
     * 
     * @return
     */
    @Action(value = "/deleteControle")
    @SQLExecutor
    public String deleteControle() {
	initialize();
	Map<String, ArrayList<String>> selection = this.viewRulesSet.mapContentSelected();
	/*
	 * Create a RuleSets to keep informations about the norm and calendar
	 */
	RuleSets jdr = new RuleSets();
	jdr.setIdNorme(selection.get(ConstanteBD.ID_NORME.getValue()).get(0));
	jdr.setPeriodicite(selection.get(ConstanteBD.PERIODICITE.getValue()).get(0));
	jdr.setValiditeInfString(selection.get(ConstanteBD.VALIDITE_INF.getValue()).get(0),
		EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	jdr.setValiditeSupString(selection.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0),
		EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	jdr.setVersion(selection.get(ConstanteBD.VERSION.getValue()).get(0));
	try {
	    // Create a temporary table to test the insertion
	    UtilitaireDao.get("arc").executeRequest(null,
		    NormManagementDao.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));

	    // Delete data from the temporary table
	    this.viewControle.delete("arc.test_ihm_" + TraitementTableParametre.CONTROLE_REGLE.toString());

	    // Check if the new rule set is OK
	    this.service.executeABlanc(jdr, "arc", TypeTraitementPhase.CONTROL.toString());

	    // If no error ==> delete
	    this.viewControle.delete();

	} catch (Exception e) {
	    // else => error message
	    this.viewControle.setMessage("Delete a rule from the rule set make it incoherent : " + e.getMessage());
	    LoggerDispatcher.error(String.format("Error in deleteControle : %s", e.toString()), LOGGER);
	}
	return generateDisplay();
    }

    /**
     * Action trigger by updating some control rules in the GUI. Update the GUI and
     * the database. Before insertion in data base check if the news rules are
     * coherents
     * 
     * @return
     */
    @Action(value = "/updateControle")
    @SQLExecutor
    public String updateControle() {
	initialize();
	RuleSets jdr = NormManagementDao.fetchJeuDeRegle(this.viewRulesSet);

	LoggerDispatcher.info("The new rule : " + this.viewControle.listSameContentFromPreviousVObject().toString(), LOGGER);
	ArrayList<RegleControleEntity> listRegleNouv = new ArrayList<>();
	for (int i = 0; i < this.viewControle.listSameContentFromPreviousVObject().size(); i++) {
	    RegleControleEntity reg = new RegleControleEntity(this.viewControle.mapContentBeforeUpdate(i));
	    listRegleNouv.add(reg);
	}
	try {
	    // Create a temporary table to test the updating
	    UtilitaireDao.get("arc").executeRequest(null,
		    NormManagementDao.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));

	    // Delete the update rules
	    this.viewControle.deleteForUpdate("arc.test_ihm_" + TraitementTableParametre.CONTROLE_REGLE.toString());

	    // Check the rules
	    this.service.ajouterRegles(jdr, "arc", listRegleNouv);
	    this.viewControle.setMessage("Rules updated !");
	    this.viewControle.update();
	} catch (Exception e) {
	    this.viewControle.setMessage("Updating the rule set make it incoherent : " + e.toString());
	    LoggerDispatcher.error(String.format("Error in updateControle : %s", e.toString()), LOGGER);

	}
	return generateDisplay();
    }

    /**
     * Action trigger by sorting a control rules in the GUI. Update the GUI .
     * 
     * @return
     */
    @Action(value = "/sortControle")
    public String sortControle() {
	return sortVobject(this.viewControle);
    }

    /**
     * Action trigger by uploading a file with rule
     * 
     * @return
     * @throws IOException
     */
    @Action(value = "/importControle")
    @SQLExecutor
    public String importControle() {
	initialize();
	LoggerDispatcher.info("importControle", LOGGER);
	String fichierRegle = "";
	if (StringUtils.isBlank(this.fileUploadControle.getPath())) {
	    this.viewControle.setMessage("You should choose a file first");
	} else {

	    try {
		fichierRegle = ManipString.readFileAsString(this.fileUploadControle.getPath());
	    } catch (IOException e) {
		LoggerDispatcher.error(String.format("Error with the file in importControle : %s", e.toString()),
			LOGGER);
	    }
	    LoggerDispatcher.info(String.format("I have a file %s character long", fichierRegle.length()), LOGGER);
	    boolean isAjouter = true;
	    Map<String, ArrayList<String>> selection = this.viewRulesSet.mapContentSelected();
	    /*
	     * Create a RuleSet to keep informations about the norm and calendat
	     */
	    RuleSets jdr = new RuleSets();
	    jdr.setIdNorme(selection.get(ConstanteBD.ID_NORME.getValue()).get(0));
	    jdr.setPeriodicite(selection.get(ConstanteBD.PERIODICITE.getValue()).get(0));
	    jdr.setValiditeInfString(selection.get(ConstanteBD.VALIDITE_INF.getValue()).get(0),
		    EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	    jdr.setValiditeSupString(selection.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0),
		    EDateFormat.SIMPLE_DATE_FORMAT_SQL.getValue());
	    jdr.setVersion(selection.get(ConstanteBD.VERSION.getValue()).get(0));
	    ArrayList<RegleControleEntity> listRegle = this.service
		    .miseEnRegleC(ControleRegleService.nomTableRegleControle("arc.test_ihm", true), fichierRegle);
	    try {
		// Create a temporary table to test the importing
		UtilitaireDao.get("arc").executeRequest(null, NormManagementDao
			.createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
		// Insert the rules in the temporary table
		isAjouter = this.service.ajouterRegles(jdr, "arc", listRegle);
	    } catch (Exception e) {
		this.viewControle.setMessage(e.toString());
		LoggerDispatcher.error("Error when importing controle rules " + e.toString(), LOGGER);
		isAjouter = false;
	    }
	    // Import result
	    if (isAjouter) {
		try {
		    // Add the rule in database
		    this.service.ajouterReglesValidees(jdr, "arc", listRegle);
		} catch (Exception e) {
		    LoggerDispatcher.error("Error when importing valid controle rules " + e.toString(), LOGGER);

		}
		LoggerDispatcher.info("New rules inserted", LOGGER);
		this.viewControle.setMessage("New rules inserted");
	    }
	}
	return generateDisplay();
    }

    /**
     * Clean the control rules. Update GUI and database
     * 
     * @return
     */
    @Action(value = "/viderControle")
    public String viderControle() {
	initialize();
	NormManagementDao.emptyRuleTable(this.viewRulesSet,
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CONTROLE_REGLE));
	return generateDisplay();
    }

    /**
     * Clean the filter rules. Update GUI and database
     * 
     * @return
     */
    @Action(value = "/viderFiltrage")
    public String viderFiltrage() {
	initialize();
	NormManagementDao.emptyRuleTable(this.viewRulesSet,
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE));
	return generateDisplay();

    }

    /**
     * Clean the map rules. Update GUI and database
     * 
     * @return
     */
    @Action(value = "/viderMapping")
    public String viderMapping() {
	initialize();
	NormManagementDao.emptyRuleTable(this.viewRulesSet,
		getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE));
	return generateDisplay();

    }

    /**
     * Action trigger by updating a filter rule in the GUI. Update the GUI and the
     * database. Before inserting, the rules are checked
     * 
     * @return
     */
    @Action(value = "/updateFiltrage")
    @SQLExecutor
    public String updateFiltrage() {
	initialize();
	boolean isRegleOk = true;
	LoggerDispatcher.info("The new rules : " + this.viewFiltrage.listSameContentFromCurrentVObject(), LOGGER);
	String exprRegleFiltre = this.viewFiltrage.listSameContentFromCurrentVObject().get(0).get(6);

	StringBuilder message = new StringBuilder();
	try {
	    // Create test table
	    UtilitaireDao.get("arc").executeRequest(null, NormManagementDao
		    .createTableTest("arc.test_ihm_controle_ok", ManipString.extractRubriques(exprRegleFiltre)));

	    UtilitaireDao.get("arc").executeRequest(null,
		    "SELECT * FROM arc.test_ihm_controle_ok WHERE " + ManipString.extractAllRubrique(exprRegleFiltre));
	    LoggerDispatcher.info("La requete de test ? " + "SELECT * FROM arc.test_ihm_controle_ok WHERE "
		    + ManipString.extractAllRubrique(exprRegleFiltre), LOGGER);
	    message.append("Rules updated!");
	} catch (Exception ex) {
	    isRegleOk = false;
	    message.append("Error when inserting the new rules : " + ex.getMessage());
	    LoggerDispatcher.error("Error when inserting the new rules : " + ex.getMessage(), LOGGER);
	}

	this.viewFiltrage.setMessage(message.toString());
	if (isRegleOk) {
	    this.viewFiltrage.update();
	}
	return generateDisplay();

    }

    /**
     * Action trigger when the table of map rules is request or refresh. Update the
     * GUI
     * 
     * @return success
     */
    @Action(value = "/selectMapping")
    public String selectMapping() {
	return basicAction();
    }

    /**
     * Action trigger by adding a map rule in the GUI. Update the GUI and the
     * database
     * 
     * @return
     */
    @Action(value = "/addMapping")
    public String addMapping() {
	return addLineVobject(this.viewMapping);
    }

    @Action(value = "/deleteMapping")
    public String deleteMapping() {
	return deleteLineVobject(this.viewMapping);
    }

    /**
     * Action trigger by updating a map rule in the GUI. Update the GUI and the
     * database. Before insertion check if the rule is OK
     * 
     * @return
     */
    @Action(value = "/updateMapping")
    public String updateMapping() {
	initialize();
	Map<String, ArrayList<String>> afterUpdate = this.viewMapping.mapSameContentFromCurrentVObject();
	boolean isRegleOk = NormManagementDao.testerReglesMapping(this.viewMapping, this.viewRulesSet,
		this.viewNorme, afterUpdate);
	if (isRegleOk) {
	    this.viewMapping.update();
	}
	LOGGER.info("Rules updated");
	return generateDisplay();
    }

    /**
     * Action trigger by uploading a filter rule file
     * 
     * @return
     */
    @Action(value = "/importFiltrage")
    public String importFiltrage() {
	initialize();
	NormManagementDao.uploadFileRule(this.viewFiltrage, this.viewRulesSet, this.fileUploadLoad);
	return generateDisplay();
    }

    /**
     * Action initializing the filter rules
     * 
     * @return
     */
    @Action(value = "/preGenererRegleFiltrage")
    public String preGenererRegleFiltrage() {
	initialize();
	try {
	    UtilitaireDao.get("arc").executeRequest(null,
		    new StringBuilder("INSERT INTO " + this.viewFiltrage.getTable())//
			    .append("  " + Format.stringListe(this.viewFiltrage.getDatabaseColumnsLabel()))//
			    .append("  SELECT (SELECT max(id_regle) FROM " + this.viewFiltrage.getTable()
				    + ")+row_number() over () ,")//
			    .append("  '"
				    + this.viewRulesSet.mapContentSelected().get(ConstanteBD.ID_NORME.getValue()).get(0)
				    + "', ")//
			    .append("  '" + this.viewRulesSet.mapContentSelected()
				    .get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "', ")//
			    .append("  '" + this.viewRulesSet.mapContentSelected()
				    .get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "', ")//
			    .append("  '"
				    + this.viewRulesSet.mapContentSelected().get(ConstanteBD.VERSION.getValue()).get(0)
				    + "', ")//
			    .append("  '" + this.viewRulesSet.mapContentSelected()
				    .get(ConstanteBD.PERIODICITE.getValue()).get(0) + "', ")//
			    .append("  null,")//
			    .append("  null;"));
	} catch (SQLException e) {
	    LoggerDispatcher.error(String.format("Error in preGenererRegleFiltrage : %s", e.toString()), LOGGER);
	}
	return generateDisplay();
    }

    /**
     * Action trigger by importing a map rule file
     * 
     * @return
     */
    @Action(value = "/importMapping")
    public String importMapping() {
	initialize();
	if (StringUtils.isBlank(this.fileUploadMap.getPath())) {
	    this.viewMapping.setMessage("You should choose a file first");
	} else {
	    boolean isRegleOk = false;
	    try {
		Map<String, String> mapVariableToType = new HashMap<>();
		Map<String, String> mapVariableToTypeConso = new HashMap<>();
		NormManagementDao.calculerVariableToType(this.viewNorme, mapVariableToType,
			mapVariableToTypeConso);
		Set<String> variablesAttendues = mapVariableToType.keySet();
		String nomTable = "arc.ihm_mapping_regle";
		List<RegleMappingEntity> listeRegle = new ArrayList<>();
		EntityDao<RegleMappingEntity> dao = new MappingRegleDao();
		dao.setTableName(nomTable);
		dao.setEOLSeparator(true);
		Map<String, ArrayList<String>> reglesAImporter = NormManagementDao.calculerReglesAImporter(
			this.fileUploadMap, listeRegle, dao, mapVariableToType, mapVariableToTypeConso);
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
		isRegleOk = NormManagementDao.testerReglesMapping(this.viewMapping, this.viewRulesSet,
			this.viewNorme, reglesAImporter);
		Map<String, String> map = new HashMap<>();
		map.put("id_regle", "(SELECT max(id_regle)+1 FROM " + nomTable + ")");
		map.put(ConstanteBD.ID_NORME.getValue(),
			this.viewNorme.mapContentSelected().get(ConstanteBD.ID_NORME.getValue()).get(0));
		map.put(ConstanteBD.VALIDITE_INF.getValue(),
			this.viewRulesSet.mapContentSelected().get(ConstanteBD.VALIDITE_INF.getValue()).get(0));
		map.put(ConstanteBD.VALIDITE_SUP.getValue(),
			this.viewRulesSet.mapContentSelected().get(ConstanteBD.VALIDITE_SUP.getValue()).get(0));
		map.put(ConstanteBD.VERSION.getValue(),
			this.viewRulesSet.mapContentSelected().get(ConstanteBD.VERSION.getValue()).get(0));
		map.put(ConstanteBD.PERIODICITE.getValue(),
			this.viewRulesSet.mapContentSelected().get(ConstanteBD.PERIODICITE.getValue()).get(0));
		if (isRegleOk) {
		    // check if each varialbe have a rule
		    RuleSets jdr = NormManagementDao.fetchJeuDeRegle(this.viewRulesSet);
		    StringBuilder bloc = new StringBuilder();
		    /*
		     * DELETE from
		     */
		    bloc.append("DELETE FROM " + nomTable + " WHERE " + jdr.getSqlEquals() + ";");
		    for (int i = 0; i < listeRegle.size(); i++) {
			bloc.append(dao.getInsert(listeRegle.get(i), map));
		    }
		    UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(null, bloc);
		}
	    } catch (Exception ex) {
		LoggerHelper.error(LOGGER, "importMapping()", ex.getStackTrace());
		this.viewMapping.setMessage("Erreur lors de l'import : " + ex.toString());
	    }
	}
	return generateDisplay();
    }

    /**
     * Action trigger by request the generation of the mapping rule. Will create in
     * database empty rules for each column in the final model and update the GUI.
     * 
     * @return
     */
    @Action(value = "/preGenererRegleMapping")
    public String preGenererRegleMapping() {
	initialize();
	try {

	    // List hard coded to be sure of the order in the select
	    StringBuilder requete = new StringBuilder("INSERT INTO " + this.viewMapping.getTable()).append(
		    "  (id_regle, id_norme, validite_inf, validite_sup,  version , periodicite, variable_sortie, expr_regle_col, commentaire) ")
		    .append("  SELECT (SELECT max(id_regle) FROM " + this.viewMapping.getTable()
			    + ")+row_number() over () ,")
		    .append("  '" + this.viewRulesSet.mapContentSelected().get(ConstanteBD.ID_NORME.getValue()).get(0)
			    + "', ")
		    .append("  '"
			    + this.viewRulesSet.mapContentSelected().get(ConstanteBD.VALIDITE_INF.getValue()).get(0)
			    + "', ")
		    .append("  '"
			    + this.viewRulesSet.mapContentSelected().get(ConstanteBD.VALIDITE_SUP.getValue()).get(0)
			    + "', ")
		    .append("  '" + this.viewRulesSet.mapContentSelected().get(ConstanteBD.VERSION.getValue()).get(0)
			    + "', ")
		    .append("  '"
			    + this.viewRulesSet.mapContentSelected().get(ConstanteBD.PERIODICITE.getValue()).get(0)
			    + "', ")
		    .append("  liste_colonne.nom_variable_metier,").append("  null,").append(
			    "  null")
		    .append("  FROM ("
			    + FormatSQL.listeColonneTableMetierSelonFamilleNorme("arc.ihm",
				    this.viewNorme.mapContentSelected().get(ConstanteBD.ID_FAMILY.getValue()).get(0))
			    + ") liste_colonne");
	    UtilitaireDao.get("arc").executeRequest(null, requete);
	} catch (SQLException e) {
	    LoggerDispatcher.error("Error in preGenererRegleMapping", e, LOGGER);
	}
	return generateDisplay();
    }

    /**
     * Action trigger by sorting the filter rules in the GUI. Update the GUI
     * 
     * @return
     */
    @Action(value = "/sortFiltrage")
    public String sortFiltrage() {
	return sortVobject(this.viewFiltrage);
    }

    /**
     * Action trigger by sorting the map rules in the GUI. Update the GUI
     * 
     * @return
     */
    @Action(value = "/sortMapping")
    public String sortMapping() {
	return sortVobject(this.viewMapping);
    }

    /**
     * Action trigger by requesting the load rules of the register rule set to copy
     * in the actual rule set
     * 
     * @return
     */
    @Action(value = "selectJeuxDeReglesChargementCopie")
    public String selectJeuxDeReglesChargementCopie() {
	initialize();
	this.viewJeuxDeReglesCopie.getCustomValues().put(SELECTED_JEU_DE_REGLE, this.viewChargement.getTable());
	return generateDisplay();
    }

    /**
     * Action trigger by requesting the structurize rules of the register rule set
     * to copy in the actual rule set
     * 
     * @return
     */
    @Action(value = "selectJeuxDeReglesNormageCopie")
    public String selectJeuxDeReglesNormageCopie() {
	initialize();
	this.viewJeuxDeReglesCopie.getCustomValues().put(SELECTED_JEU_DE_REGLE, this.viewNormage.getTable());
	return generateDisplay();
    }

    /**
     * Action trigger by requesting the control rules of the register rule set to
     * copy in the actual rule set
     * 
     * @return
     */
    @Action(value = "selectJeuxDeReglesControleCopie")
    public String selectJeuxDeReglesControleCopie() {
	initialize();
	this.viewJeuxDeReglesCopie.getCustomValues().put(SELECTED_JEU_DE_REGLE, this.viewControle.getTable());
	return generateDisplay();
    }

    /**
     * Action trigger by requesting the filter rules of the register rule set to
     * copy in the actual rule set
     * 
     * @return
     */
    @Action(value = "selectJeuxDeReglesFiltrageCopie")
    public String selectJeuxDeReglesFiltrageCopie() {
	initialize();
	this.viewJeuxDeReglesCopie.getCustomValues().put(SELECTED_JEU_DE_REGLE, this.viewFiltrage.getTable());
	return generateDisplay();
    }

    /**
     * Action trigger by requesting the map rules of the register rule set to copy
     * in the actual rule set
     * 
     * @return
     */
    @Action(value = "/selectJeuxDeReglesMappingCopie")
    public String selectJeuxDeReglesMappingCopie() {
	initialize();
	this.viewJeuxDeReglesCopie.getCustomValues().put(SELECTED_JEU_DE_REGLE, this.viewMapping.getTable());
	return generateDisplay();
    }

    @Action(value = "/selectJeuxDeReglesCopie")
    public String selectJeuxDeReglesCopie() {
	return basicAction();
    }

    @Action(value = "/copieJeuxDeRegles")
    public String copieJeuxDeRegles() {
	initialize();
	LoggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
	// le jeu de regle à copier
	Map<String, ArrayList<String>> selectionOut = this.viewRulesSet.mapContentSelected();
	// le nouveau jeu de regle
	Map<String, ArrayList<String>> selectionIn = this.viewJeuxDeReglesCopie.mapContentSelected();
	HashMap<String, String> type = this.viewJeuxDeReglesCopie.mapHeadersType();
	if (!selectionIn.isEmpty()) {
	    StringBuilder requete = new StringBuilder();
	    requete.append("INSERT INTO " + this.getSelectedJeuDeRegle() + " ");
	    if (this.getSelectedJeuDeRegle().equals("arc.ihm_normage_regle")) {
		NormManagementDao.emptyRuleTable(this.viewRulesSet,
			getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_NORMAGE_REGLE));
		requete.append("(");
		requete.append(String.join(",", ConstanteBD.ID_NORME.getValue()//
			, ConstanteBD.PERIODICITE.getValue()//
			, ConstanteBD.VALIDITE_INF.getValue()//
			, ConstanteBD.VALIDITE_SUP.getValue()//
			, ConstanteBD.VERSION.getValue()//
			, ConstanteBD.ID_CLASS.getValue()//
			, ConstanteBD.RUBRIQUE_NMCL.getValue()//
			, ConstanteBD.COMMENTAIRE.getValue()));

		requete.append(")");
		requete.append("SELECT ");
		requete.append(String.join("','", "'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
			, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
			, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date, "//
			, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date, "//
			, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
			, ConstanteBD.ID_CLASS.getValue()//
			, ConstanteBD.RUBRIQUE_NMCL.getValue()//
			, ConstanteBD.COMMENTAIRE.getValue()));

	    } else if (this.getSelectedJeuDeRegle().equals("arc.ihm_controle_regle")) {
		NormManagementDao.emptyRuleTable(this.viewRulesSet,
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
		NormManagementDao.emptyRuleTable(this.viewRulesSet,
			getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_FILTRAGE_REGLE));
		requete.append("(");
		requete.append(String.join(",", ConstanteBD.ID_REGLE.getValue()//
			, ConstanteBD.ID_NORME.getValue()//
			, ConstanteBD.VALIDITE_INF.getValue()//
			, ConstanteBD.VALIDITE_SUP.getValue()//
			, ConstanteBD.VERSION.getValue()//
			, ConstanteBD.PERIODICITE.getValue()//
			, ConstanteBD.EXPR_REGLE_FILTRE.getValue()));

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
		NormManagementDao.emptyRuleTable(this.viewRulesSet,
			getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_MAPPING_REGLE));

		requete.append("(");
		requete.append(String.join(",", ConstanteBD.ID_REGLE.getValue()//
			, ConstanteBD.ID_NORME.getValue()//
			, ConstanteBD.VALIDITE_INF.getValue()//
			, ConstanteBD.VALIDITE_SUP.getValue()//
			, ConstanteBD.VERSION.getValue()//
			, ConstanteBD.VARIABLE_SORTIE.getValue()//
			, ConstanteBD.EXPR_REGLE_COL.getValue(), ConstanteBD.COMMENTAIRE.getValue()));

		requete.append(")");

		requete.append("SELECT ");
		requete.append(String.join(","
			, "row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + ")"
			, "'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
			, "'" + selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0) + "'"//
			, "'" + selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0) + "'::date "//
			, "'" + selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0) + "'::date "//
			, "'" + selectionOut.get(ConstanteBD.VERSION.getValue()).get(0) + "'"//
			, ConstanteBD.VARIABLE_SORTIE.getValue()//
			, ConstanteBD.EXPR_REGLE_COL.getValue(), ConstanteBD.COMMENTAIRE.getValue()));

	    } else if (this.getSelectedJeuDeRegle().equals("arc.ihm_chargement_regle")) {
		NormManagementDao.emptyRuleTable(this.viewRulesSet,
			getBddTable().getQualifedName(BddTable.ID_TABLE_IHM_CHARGEMENT_REGLE));
		requete.append("(");
		requete.append(String.join(","
			, "row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + ")"
			, ConstanteBD.ID_NORME.getValue()//
			, ConstanteBD.VALIDITE_INF.getValue()//
			, ConstanteBD.VALIDITE_SUP.getValue()//
			, ConstanteBD.VERSION.getValue()//
			, ConstanteBD.TYPE_FICHIER.getValue()//
			, ConstanteBD.DELIMITER.getValue(), ConstanteBD.FORMAT.getValue(),
			ConstanteBD.COMMENTAIRE.getValue()));

		requete.append(")");

		requete.append("SELECT ");
		requete.append(String.join(" , ", "'" + selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0) + "'"//
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
		LoggerDispatcher.error("Error in copieJeuxDeRegles", ex, LOGGER);
	    }
	    this.viewJeuxDeReglesCopie.destroy();
	} else {
	    LoggerDispatcher.info("No rule set choosed", LOGGER);
	    this.viewRulesSet.setMessage("Please choose a ruleset");
	}
	return generateDisplay();
    }

    public VObject getViewNorme() {
	return this.viewNorme;
    }

    public void setViewNorme(VObject viewNorme) {
	LoggerHelper.debug(LOGGER, "viewNorme.getSelectedLines() h ", viewNorme.getSelectedLines());
	this.viewNorme = viewNorme;
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

    public String getFileUploadContentType() {
	return this.fileUploadContentType;
    }

    public void setFileUploadContentType(String fileUploadContentType) {
	this.fileUploadContentType = fileUploadContentType;
    }

    public String getFileUploadFileName() {
	return this.fileUploadFileName;
    }

    public void setFileUploadFileName(String fileUploadFileName) {
	this.fileUploadFileName = fileUploadFileName;
    }

    public ControleRegleService getService() {
	return this.service;
    }

    public void setService(ControleRegleService service) {
	this.service = service;
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
     * @param viewFiltrage
     *            the viewFiltrage to set
     */
    public void setViewFiltrage(VObject viewFiltrage) {
	this.viewFiltrage = viewFiltrage;
    }

    /**
     * @return the selectedJeuDeRegle
     */
    public String getSelectedJeuDeRegle() {
	return this.viewJeuxDeReglesCopie.getCustomValues().get(SELECTED_JEU_DE_REGLE);
    }

    public File getFileUploadLoad() {
	return fileUploadLoad;
    }

    public void setFileUploadLoad(File fileUploadLoad) {
	this.fileUploadLoad = fileUploadLoad;
    }

    public File getFileUploadStructurize() {
	return fileUploadStructurize;
    }

    public void setFileUploadStructurize(File fileUploadStructurize) {
	this.fileUploadStructurize = fileUploadStructurize;
    }

    public File getFileUploadControle() {
	return fileUploadControle;
    }

    public void setFileUploadControle(File fileUploadControle) {
	this.fileUploadControle = fileUploadControle;
    }

    public File getFileUploadFilter() {
	return fileUploadFilter;
    }

    public void setFileUploadFilter(File fileUploadFilter) {
	this.fileUploadFilter = fileUploadFilter;
    }

    public File getFileUploadMap() {
	return fileUploadMap;
    }

    public void setFileUploadMap(File fileUploadMap) {
	this.fileUploadMap = fileUploadMap;
    }

}
