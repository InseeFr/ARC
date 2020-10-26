package fr.insee.arc.web.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.model.EnvManagementModel;
import fr.insee.arc.web.model.viewobjects.ViewArchiveBAS;
import fr.insee.arc.web.model.viewobjects.ViewEntrepotBAS;
import fr.insee.arc.web.model.viewobjects.ViewFichierBAS;
import fr.insee.arc.web.model.viewobjects.ViewPilotageBAS;
import fr.insee.arc.web.model.viewobjects.ViewRapportBAS;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectEnvManagementService;

@Controller
public class PilotageBASAction extends ArcAction<EnvManagementModel> {

	private static final String ACTION_NAME = "EnvManagement";

	private static final String RESULT_SUCCESS = "jsp/gererPilotageBAS.jsp";
	
	private static final String ARC = "ARC_";

	private static final String WRITING_REPO = "entrepotEcriture";

	private static final Logger LOGGER = LogManager.getLogger(PilotageBASAction.class);
	
	@Autowired
	private VObjectEnvManagementService viewObjectPilotage;
	
	private VObject viewPilotageBAS = new ViewPilotageBAS();
	
	private VObject viewRapportBAS = new ViewRapportBAS();

	private VObject viewFichierBAS = new ViewFichierBAS();

	private VObject viewEntrepotBAS = new ViewEntrepotBAS();

	private VObject viewArchiveBAS = new ViewArchiveBAS();

	private List<TraitementPhase> listePhase;

	/**
	 * Phase sélectionnée par l'utilisateur
	 */
	private String phaseAExecuter;

	public PilotageBASAction() {	
		this.setListePhase(TraitementPhase.getListPhaseC());	
	}

	@Override
	public void putAllVObjects(EnvManagementModel arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);	
	
		setViewArchiveBAS(vObjectService.preInitialize(arcModel.getViewArchiveBAS()));
		setViewEntrepotBAS(vObjectService.preInitialize(arcModel.getViewEntrepotBAS()));
		setViewFichierBAS(vObjectService.preInitialize(arcModel.getViewFichierBAS()));
		setViewPilotageBAS(vObjectService.preInitialize(arcModel.getViewPilotageBAS()));
		setViewRapportBAS(vObjectService.preInitialize(arcModel.getViewRapportBAS()));	
		
		// If the sandbox changed, have to destroy all table and re create later
		if (this.isRefreshMonitoring) {
			vObjectService.destroy(getViewArchiveBAS());
			vObjectService.destroy(getViewEntrepotBAS());
			vObjectService.destroy(getViewFichierBAS());
			viewObjectPilotage.destroy(getViewPilotageBAS());
			vObjectService.destroy(getViewRapportBAS());
			this.isRefreshMonitoring = false;
		}

		putVObject(getViewArchiveBAS(), t -> initializeArchiveBAS());
		putVObject(getViewEntrepotBAS(), t -> initializeEntrepotBAS());
		putVObject(getViewFichierBAS(), t -> initializeFichierBAS());
		putVObject(getViewPilotageBAS(), t -> initializePilotageBAS());
		putVObject(getViewRapportBAS(), t -> initializeRapportBAS());
	
		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	public void initializeEntrepotBAS() {
		LoggerHelper.debug(LOGGER, "* initializeEntrepotBAS *");

		HashMap<String, String> defaultInputFields = new HashMap<>();
		StringBuilder requete = new StringBuilder();

		try {
			if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot"))) {
				requete.append("select id_entrepot from arc.ihm_entrepot");
			} else {
				requete.append("select ''::text as id_entrepot");
			}
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, "error when initialize repository", e);
		}

		this.vObjectService.initialize(requete.toString(), null, defaultInputFields, this.getViewEntrepotBAS());
	}

	// visual des Pilotages du bac à sable
	public void initializePilotageBAS() {
		LoggerHelper.debug(LOGGER, "* initializePilotageBAS *");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		
		StringBuilder requete = new StringBuilder();
        requete.append("select * from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T)+" order by date_entree desc");
		
		this.viewObjectPilotage.initialize(requete.toString(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T), defaultInputFields, getViewPilotageBAS());
	}

	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * TODO : use liquibase instead
	 * 
	 * @return
	 */
	@RequestMapping("/enterPilotageBAS")
	public String enterPilotageBAS(EnvManagementModel viewObjects) {
		ApiInitialisationService.bddScript(getBacASable(), null);
		return generateDisplay(RESULT_SUCCESS);
	}

	@PostMapping(value = {"/selectPilotageBAS"}, 
			consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String selectPilotageBAS() {
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/sortPilotageBAS")
	public String sortPilotageBAS() {		
		this.vObjectService.sort(getViewPilotageBAS());
		return generateDisplay(RESULT_SUCCESS);

	}

	// visual des Pilotages du bac à sable
	public void initializeRapportBAS() {
		LoggerHelper.debug(LOGGER, "* initializeRapportBAS *");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		StringBuilder requete = new StringBuilder();
		requete.append(
				"select date_entree, phase_traitement, array_to_string(etat_traitement,'$') as etat_traitement, rapport, count(1) as nb ");
		requete.append("from " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
		requete.append(" where rapport is not null ");
		requete.append("group by date_entree, phase_traitement, etat_traitement, rapport ");
		requete.append("order by date_entree asc ");
		this.vObjectService.initialize(requete.toString(), null, defaultInputFields, getViewRapportBAS());
	}

	@RequestMapping("/selectRapportBAS")
	public String selectRapportBAS() {
		
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/sortRapportBAS")
	public String sortRapportBAS() {
		
		this.vObjectService.sort(getViewRapportBAS());
		return generateDisplay(RESULT_SUCCESS);

	}

	@RequestMapping("/informationInitialisationPROD")
    public String informationInitialisationPROD() {
		
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
			String etat=UtilitaireDao.get("arc").getString(null, "SELECT case when operation='O' then 'actif' else 'inactif' end from arc.pilotage_batch;");
			
    		this.getViewPilotageBAS().setMessage("Le batch est "+etat+".\nLe prochain batch d'initialisation est programmé aprés : "+heure);

		} catch (SQLException e) {
			loggerDispatcher.error("Error in informationInitialisationPROD", e, LOGGER);
		}
    	return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/retarderBatchInitialisationPROD")
    @SQLExecutor
    public String retarderBatchInitialisationPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set last_init=to_char(current_date + interval '7 days','yyyy-mm-dd')||':22';");

			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
			this.getViewPilotageBAS().setMessage("Le prochain batch d'initialisation aura lieu ce soir après : "+heure);

			
		} catch (SQLException e) {
			loggerDispatcher.error("Error in retarderInitialisationPROD", e, LOGGER);
		}
    	
    	return generateDisplay(RESULT_SUCCESS);
    }
    
    @RequestMapping("/demanderBatchInitialisationPROD")
    @SQLExecutor
    public String demanderBatchInitialisationPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set last_init=to_char(current_date-interval '1 days','yyyy-mm-dd')||':22';");
			
			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
			this.getViewPilotageBAS().setMessage("Le prochain batch d'initialisation aura lieu dans quelques minutes (après "+heure+") ");

			
		} catch (SQLException e) {
			loggerDispatcher.error("Error in demanderBatchInitialisationPROD", e, LOGGER);
		}
    	
    	return generateDisplay(RESULT_SUCCESS);
    }
    
    @RequestMapping("/toggleOnPROD")
    @SQLExecutor
    public String toggleOnPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set operation='O'; ");
			this.getViewPilotageBAS().setMessage("Production activée ");
		} catch (SQLException e) {
			loggerDispatcher.error("Error in toggleOnPROD", e, LOGGER);
		}
    	return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/toggleOffPROD")
    @SQLExecutor
    public String toggleOffPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set operation='N'; ");
			this.getViewPilotageBAS().setMessage("Production arretée ");
		} catch (SQLException e) {
			loggerDispatcher.error("Error in toggleOffPROD", e, LOGGER);
		}
    	return generateDisplay(RESULT_SUCCESS);
    }
	
	// Actions du bac à sable

	@RequestMapping("/filesUploadBAS")
	public String filesUploadBAS(ArrayList<String> fileUploadFileName, ArrayList<File> fileUpload) {
		LoggerHelper.debug(LOGGER, "* /* filesUploadBAS : " + this.getViewEntrepotBAS().getCustomValues() + " */ *");
		
		if (this.getViewEntrepotBAS().getCustomValues() != null
				&& !this.getViewEntrepotBAS().getCustomValues().get(WRITING_REPO).equals("")
				&& fileUploadFileName != null) {
			String repertoireUpload = this.repertoire + getBacASable().toUpperCase()
					+ File.separator + TraitementPhase.RECEPTION + "_"
					+ this.getViewEntrepotBAS().getCustomValues().get(WRITING_REPO);
			LoggerHelper.trace(LOGGER, "repertoireUpload :", repertoireUpload);
			this.vObjectService.upload(getViewPilotageBAS(), repertoireUpload, fileUploadFileName, fileUpload);
		} else {
			String msg = "";
			if (fileUploadFileName == null) {
				msg = "Erreur : aucun fichier selectionné\n";
				this.getViewPilotageBAS().setMessage("Erreur : aucun fichier selectionné.");
			}

			if (this.getViewEntrepotBAS().getCustomValues() == null
					|| this.getViewEntrepotBAS().getCustomValues().get(WRITING_REPO).equals("")) {
				msg += "Erreur : aucun entrepot selectionné\n";
			}

			this.getViewPilotageBAS().setMessage(msg);
		}
		this.getViewEntrepotBAS().addCustomValue(WRITING_REPO, null);
		// Lancement de l'initialisation dans la foulée
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				getBacASable(), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), "arc.ihm",
				getBacASable(), this.repertoire,
				String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();

		
		
		return generateDisplay(RESULT_SUCCESS);

	}

	/**
	 * Initialisation de la vue sur la table contenant la liste des fichiers du
	 * répertoire d'archive
	 */
	public void initializeArchiveBAS() {
		LoggerHelper.debug(LOGGER, "* /* initializeArchiveBAS  */ *");
		if (this.getViewEntrepotBAS().getCustomValues().containsKey("entrepotLecture")
				&& !this.getViewEntrepotBAS().getCustomValues().get("entrepotLecture").equals("")) {
			HashMap<String, String> defaultInputFields = new HashMap<>();
			
			 StringBuilder requete = new StringBuilder();
			 requete.append("select * from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_ARCHIVE)+" where entrepot='"
	                    + this.getViewEntrepotBAS().getCustomValues().get("entrepotLecture") + "'");

			this.vObjectService.initialize(requete.toString(), null, defaultInputFields, getViewArchiveBAS());
		} else {

			this.vObjectService.destroy(getViewArchiveBAS());
		}
	}

	/**
	 * Fabrication d'une table temporaire avec comme contenu le nom des archives
	 * d'un entrepot donné puis Ouverture d'un VObject sur cette table
	 *
	 * @return
	 */
	@RequestMapping("/visualiserEntrepotBAS")
	public String visualiserEntrepotBAS() {
		
		return generateDisplay(RESULT_SUCCESS);
	}

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	@SQLExecutor
	@RequestMapping("/downloadEnveloppeFromArchiveBAS")
	public String downloadEnveloppeFromArchiveBAS(HttpServletResponse response) {
		
		loggerDispatcher.trace("*** Téléchargement des enveloppes à partir de l'archive ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = getViewArchiveBAS().mapContentSelected();

		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct alias_de_table.nom_archive as nom_fichier from ("
				+ this.getViewArchiveBAS().getMainQuery() + ") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.getViewArchiveBAS().getFilterFields(),
				this.getViewArchiveBAS().getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection.append(" AND nom_archive IN " + Format.sqlListe(selection.get("nom_archive")) + " ");
		}

		loggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(),
				LOGGER);

		ArrayList<String> listRepertoire = new ArrayList<>();
		GenericBean g;
		String entrepot = "";
		try {
			g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
					"SELECT DISTINCT entrepot FROM (" + this.getViewArchiveBAS().getMainQuery() + ") alias_de_table "));
			entrepot = g.mapContent().get("entrepot").get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE");
		String chemin = this.repertoire + File.separator
				+ getBacASable().toString().toUpperCase();
		this.vObjectService.downloadEnveloppe(getViewArchiveBAS(), response, querySelection.toString(), chemin, listRepertoire);
		return "none";
	}

	@RequestMapping("/executerBatch")
	public String executerBatch() {
		loggerDispatcher.debug("executerBatch", LOGGER);
		loggerDispatcher.debug(String.format("Service %s", phaseAExecuter), LOGGER);
		
		ApiInitialisationService.synchroniserSchemaExecution(null, "arc.ihm",
				(String) getBacASable());

		ApiServiceFactory.getService(this.phaseAExecuter, "arc.ihm", (String) getBacASable(),
				this.repertoire, "10000000").invokeApi();
		return generateDisplay(RESULT_SUCCESS);
	}

	/*
	 * Allow the user to select files for the undoBatch fucntionnality
	 */
	public String undoFilesSelection() {
		String selectedSrc = null;

		HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>(
				getViewFichierBAS().mapContentSelected());

		if (m != null && !m.isEmpty() && m.get("id_source") != null) {
			for (int i = 0; i < m.get("id_source").size(); i++) {
				if (selectedSrc != null) {
					selectedSrc += "\n UNION ALL SELECT ";
				} else {
					selectedSrc = "SELECT ";
				}
				selectedSrc += "'" + m.get("id_source").get(i) + "'::text as id_source ";
			}
		}
		return selectedSrc;
	}

	@RequestMapping("/undoBatch")
	public String undoBatch() {
		loggerDispatcher.debug("undoBatch", LOGGER);
		loggerDispatcher.debug(String.format("undo service %s", phaseAExecuter), LOGGER);
		
		
		if (TraitementPhase.valueOf(this.phaseAExecuter).getOrdre()==0)
		{
			resetBAS();
			return generateDisplay(RESULT_SUCCESS);
		}
		
		ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				"arc.ihm", (String) getBacASable(), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			serv.retourPhasePrecedente(TraitementPhase.valueOf(this.phaseAExecuter), undoFilesSelection(),
					new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK, TraitementEtat.KO)));
		} finally {
            serv.finaliser();
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	// Bouton undo

	@RequestMapping("/resetBAS")
	public String resetBAS() {
		
		try {
			ApiInitialisationService.clearPilotageAndDirectories(this.repertoire,
					(String) getBacASable());
		} catch (Exception e) {
			e.printStackTrace();
			this.getViewPilotageBAS().setMessage("Problème : " + e.getMessage());
		}
		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				"arc.ihm", (String) getBacASable(), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			service.resetEnvironnement();
		} finally {
			service.finaliser();
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	// visual des Fichiers
	public void initializeFichierBAS() {
		LoggerHelper.debug(LOGGER, "initializeFichierBAS");
		Map<String, ArrayList<String>> selectionLigne = getViewPilotageBAS().mapContentSelected();
		ArrayList<String> selectionColonne = getViewPilotageBAS().listHeadersSelected();

		Map<String, ArrayList<String>> selectionLigneRapport = getViewRapportBAS().mapContentSelected();

		if (!selectionLigne.isEmpty() && !selectionColonne.isEmpty()) {

			HashMap<String, String> defaultInputFields = new HashMap<>();

			String phase = ManipString.substringBeforeLast(selectionColonne.get(0), "_").toUpperCase();
			String etat = ManipString.substringAfterLast(selectionColonne.get(0), "_").toUpperCase();

			// get the file with the selected date_entree, state, and phase_tratement
			 StringBuilder requete = new StringBuilder();
	            requete.append("select container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
	            requete.append(" FROM "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)+" ");
	            requete.append(" where date_entree" + ManipString.sqlEqual(selectionLigne.get("date_entree").get(0), "text"));
	            requete.append(" and array_to_string(etat_traitement,'$')" + ManipString.sqlEqual(etat, "text"));
	            requete.append(" and phase_traitement" + ManipString.sqlEqual(phase, "text"));
			
			this.vObjectService.initialize(requete.toString(), null, defaultInputFields, getViewFichierBAS());
		} else if (!selectionLigneRapport.isEmpty()) {
			HashMap<String, String> type = getViewRapportBAS().mapHeadersType();
			HashMap<String, String> defaultInputFields = new HashMap<>();
		
            StringBuilder requete = new StringBuilder();
            requete.append("select container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
            requete.append(" from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)+" ");
            requete.append(" where date_entree" + ManipString.sqlEqual(selectionLigneRapport.get("date_entree").get(0), "text"));
            requete.append(" and array_to_string(etat_traitement,'$')"
                    + ManipString.sqlEqual(selectionLigneRapport.get("etat_traitement").get(0), type.get("etat_traitement")));
            requete.append(" and phase_traitement"
                    + ManipString.sqlEqual(selectionLigneRapport.get("phase_traitement").get(0), type.get("phase_traitement")));
            requete.append(" and rapport" + ManipString.sqlEqual(selectionLigneRapport.get("rapport").get(0), type.get("rapport")));
			

			this.vObjectService.initialize(requete.toString(), null, defaultInputFields, getViewFichierBAS());
		} else {
			this.vObjectService.destroy(getViewFichierBAS());
		}
	}

	@RequestMapping("/selectFichierBAS")
	public String selectFichierBAS() {
		
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/sortFichierBAS")
	public String sortFichierBAS() {
		
		this.vObjectService.sort(getViewFichierBAS());
		return generateDisplay(RESULT_SUCCESS);

	}

	@RequestMapping("/downloadFichierBAS")
	public String downloadFichierBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des fichiers ***", LOGGER);
		// récupération de la liste des id_source

		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		StringBuilder querySelection = this.vObjectService.queryView(getViewFichierBAS());
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionner
		//
		if (!selection.isEmpty()) {
			querySelection.append(" AND id_source IN " + Format.sqlListe(selection.get("id_source")) + " ");
		}

		// optimisation pour avoir des bloc successifs sur la même archive
		querySelection.append(" order by container ");

		this.vObjectService.downloadXML(getViewFichierBAS(), response, querySelection.toString(), this.repertoire,
				(String) getBacASable(), TraitementPhase.RECEPTION.toString(),
				TraitementEtat.OK.toString(), TraitementEtat.KO.toString());

		loggerDispatcher.trace("*** Fin du téléchargement des fichiers XML ***", LOGGER);
		generateDisplay(RESULT_SUCCESS);
		return "none";
	}

	/**
	 * Marquage de fichier pour le rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	@RequestMapping("/toRestoreBAS")
	public String toRestoreBAS() {		
		return restore("'R'", "Fichier(s) à rejouer");
	}

	/**
	 * Marquage des archives à rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	@RequestMapping("/toRestoreArchiveBAS")
	public String toRestoreArchiveBAS() {
		return restore("'RA'", "Archives(s) à rejouer");
	}

	private String restore(String code, String messageOk) {
		loggerDispatcher.trace("*** Marquage de fichier à rejouer ***", LOGGER);
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
	
		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.getViewFichierBAS().getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.getViewFichierBAS().getFilterFields(),
				this.getViewFichierBAS().getHeadersDLabel()));
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionnés
		if (!selection.isEmpty()) {
			// concaténation des informations
			ArrayList<String> infoConcatenee = new ArrayList<>();
			ArrayList<String> listContainer = selection.get("container");
			ArrayList<String> listIdSource = selection.get("id_source");
	
			for (int i = 0; i < selection.get("id_source").size(); i++) {
				infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
			}
			querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
		}
	
		StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, code);
		String message;
		try {
	
			UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			message = messageOk;
		} catch (SQLException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "Problème lors de la restauration des fichiers";
		}
	
		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		// Lancement de l'initialisation dans la foulée
		loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				getBacASable(), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), "arc.ihm",
				getBacASable(), this.repertoire,
				String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();
		// Fin du code spécifique aux bacs à sable
		this.getViewPilotageBAS().setMessage(message);
	
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/downloadBdBAS")
	public String downloadBdBAS(HttpServletResponse response) throws Exception {
		Map<String, ArrayList<String>> selectionLigne = getViewPilotageBAS().mapContentSelected();
		ArrayList<String> selectionColonne = getViewPilotageBAS().listHeadersSelected();

		String phase = selectionColonne.get(0).split("_")[0].toUpperCase();
		String etat = selectionColonne.get(0).split("_")[1].toUpperCase();
		String date = selectionLigne.get("date_entree").get(0);

		String[] etatList = etat.split("\\$");
		String etatBdd = "{" + etat.replace("$", ",") + "}";
		
				
		// Sélection des table métiers en fonction de la phase sélectionner (5 pour
		// mapping 1 sinon)
		ArrayList<String> tableDownload = new ArrayList<>();
		try {
			GenericBean g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, ApiInitialisationService
					.requeteListAllTablesEnv(getBacASable())));
			if (!g.mapContent().isEmpty()) {
				ArrayList<String> envTables = g.mapContent().get("table_name");
				System.out.println("Le contenu de ma envTables : " + envTables);
				for (String table : envTables) {
					// selection des tables qui contiennent la phase dans leur nom
					for (int i = 0; i < etatList.length; i++) {
						if (table.toUpperCase().contains("." + phase.toUpperCase() + "_" + etatList[i].toUpperCase())
								&& !tableDownload.contains(table)) {
							tableDownload.add(table);

						}
					}
				}
			}
		} catch (SQLException e) {
			loggerDispatcher.error(e, LOGGER);
		}
		
		// List of queries that will be executed to download
		List<String> tableauRequete=new ArrayList<String>();
		// Name of the file containing the data download
		List<String> fileNames = new ArrayList<>();

		
		for (String t : tableDownload) {
			// Check if the table to download got children
			if (Boolean.TRUE.equals(UtilitaireDao.get("arc").hasResults(null,
					FormatSQL.getAllInheritedTables(ManipString.substringBeforeFirst(t, "."),
							ManipString.substringAfterFirst(t, ".")) + " LIMIT 1"))) {

				// Get the files to download
				StringBuilder requete = new StringBuilder();
				requete.append(
						"select id_source from " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
				requete.append(" where phase_traitement='" + phase + "' ");
				requete.append("AND etat_traitement='" + etatBdd + "' ");
				requete.append("AND date_entree='" + date + "' ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!getViewFichierBAS().mapContentSelected().isEmpty()) {
					ArrayList<String> filesSelected = getViewFichierBAS().mapContentSelected().get("id_source");
					requete.append("AND id_source IN (");
					for (int i = 0; i < filesSelected.size(); i++) {
						if (i > 0) {
							requete.append(",");
						}
						requete.append("'" + filesSelected.get(i) + "'");
					}
					requete.append(")");
				}

				ArrayList<String> idSources = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, requete))
						.mapContent().get("id_source");

				// for each files, generate the download query
				for (String idSource : idSources) {
					tableauRequete.add("SELECT * FROM " + ApiService.tableOfIdSource(t, idSource));
					fileNames.add(t + "_" + idSource);
				}

			}
			// if no children
			else {
				
				StringBuilder requete = new StringBuilder();
				requete.append("with prep as ( ");
				requete.append("select id_source from "
						+ getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
				requete.append(" where phase_traitement='" + phase + "' ");
				requete.append("AND etat_traitement='" + etatBdd + "' ");
				requete.append("AND date_entree='" + date + "' ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!getViewFichierBAS().mapContentSelected().isEmpty()) {
					ArrayList<String> filesSelected = getViewFichierBAS().mapContentSelected().get("id_source");
					requete.append("AND id_source IN (");
					for (int i = 0; i < filesSelected.size(); i++) {
						if (i > 0) {
							requete.append(",");
						}
						requete.append("'" + filesSelected.get(i) + "'");
					}
					requete.append(")");
				}
				requete.append(" ) ");
				requete.append("\n SELECT * from " + t + " a where exists (select 1 from prep b where a.id_source=b.id_source) ");
				tableauRequete.add(requete.toString());
				fileNames.add(t);
			}

		}
		
		this.vObjectService.download(getViewFichierBAS(), response, fileNames, tableauRequete);

		return "none";

	}

	@RequestMapping("/downloadEnveloppeBAS")
	public String downloadEnveloppeBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des enveloppes ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();

		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct alias_de_table.container as nom_fichier from ("
				+ this.getViewFichierBAS().getMainQuery() + ") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.getViewFichierBAS().getFilterFields(),
				this.getViewFichierBAS().getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection.append(" AND container IN " + Format.sqlListe(selection.get("container")) + " ");
		}

		loggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(),
				LOGGER);

		ArrayList<String> listRepertoire = new ArrayList<>();
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.OK);
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.KO);
		String chemin = this.repertoire + File.separator + ARC
				+ getBacASable().toString().toUpperCase();
		this.vObjectService.downloadEnveloppe(getViewFichierBAS(), response, querySelection.toString(), chemin, listRepertoire);
		loggerDispatcher.trace("*** Fin du téléchargement des enveloppes ***", LOGGER);

		return "none";
	}

	/**
	 * Marquage de fichier pour suppression lors de la prochaine initialisation
	 *
	 * @return
	 */
	@RequestMapping("/toDeleteBAS")
	public String toDeleteBAS() {
		
		loggerDispatcher.trace("*** Marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();

		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.getViewFichierBAS().getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.getViewFichierBAS().getFilterFields(),
				this.getViewFichierBAS().getHeadersDLabel()));
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionné
		if (!selection.isEmpty()) {
			// concaténation des informations
			ArrayList<String> infoConcatenee = new ArrayList<>();
			ArrayList<String> listContainer = selection.get("container");
			ArrayList<String> listIdSource = selection.get("id_source");

			for (int i = 0; i < selection.get("id_source").size(); i++) {
				infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
			}
			querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
		}

		StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "'1'");
		String message;
		try {
			UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			message = "Fichier(s) supprimé(s)";
		} catch (SQLException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "Problème lors de la suppression des fichiers";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				getBacASable(), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();

		// Fin du code spécifique aux bacs à sable
		this.getViewPilotageBAS().setMessage(message);

		return generateDisplay(RESULT_SUCCESS);
	}

	/**
	 * Suppression du marquage de fichier pour suppression lors de la prochaine
	 * initialisation
	 *
	 * @return
	 */
	@RequestMapping("/undoActionBAS")
	public String undoActionBAS() {
		
		loggerDispatcher.trace("*** Suppression du marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.getViewFichierBAS().getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.getViewFichierBAS().getFilterFields(),
				this.getViewFichierBAS().getHeadersDLabel()));
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionné
		if (!selection.isEmpty()) {
			// concaténation des informations
			ArrayList<String> infoConcatenee = new ArrayList<>();
			ArrayList<String> listContainer = selection.get("container");
			ArrayList<String> listIdSource = selection.get("id_source");

			for (int i = 0; i < selection.get("id_source").size(); i++) {
				infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
			}
			querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
		}
		// loggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

		StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "null");
		try {

			UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
		} catch (SQLException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	private StringBuilder requeteUpdateToDelete(StringBuilder querySelection, String valeur) {
		StringBuilder updateToDelete = new StringBuilder();
		updateToDelete.append("WITH ");
		updateToDelete.append("prep AS ( ");
		updateToDelete.append(querySelection);
		updateToDelete.append("         ) ");
		updateToDelete.append("UPDATE " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER) + " a ");
		updateToDelete.append("SET to_delete=" + valeur + " ");
		updateToDelete.append(
				"WHERE EXISTS (SELECT 1 FROM prep WHERE a.container=prep.container AND a.id_source=prep.id_source); ");
		return updateToDelete;
	}

	/**
	 * retour arriere d'une phase
	 *
	 * @return
	 */
	@RequestMapping("/resetPhaseBAS")
	public String resetPhaseBAS() {
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		StringBuilder querySelection = this.vObjectService.queryView(getViewFichierBAS());

		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// choisis pour le retour arriere
		//
		if (!selection.isEmpty()) {
			querySelection.append(" AND id_source IN " + Format.sqlListe(selection.get("id_source")) + " ");
		}

		// On recupere la phase
		String phase = getViewFichierBAS().mapContent().get("phase_traitement").get(0);

		// Lancement du retour arrière
		ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				"arc.ihm", getBacASable(), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			serv.retourPhasePrecedente(TraitementPhase.valueOf(phase), querySelection.toString(), null);
		} finally {
			serv.finaliser();
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	/**
	 * Mise à jour de la console dans l'objet struts2
	 */
	@RequestMapping("/resetConsoleBAS")
	public String resetConsoleBAS() {
		
		getSession().put("console", "");
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/updateConsoleBAS")
	public void updateConsoleBAS(HttpServletResponse response) {
		

		if (getSession().get("console") == null) {
			getSession().put("console", "");
		}

		response.setCharacterEncoding("UTF-8");
		PrintWriter out;
		try {
			out = response.getWriter();
			out.write((String) getSession().get("console"));
			out.close();

		} catch (IOException e) {
			loggerDispatcher.error("Error in updateConsoleBAS", e, LOGGER);
		}
		getSession().put("console", "");


	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	public List<TraitementPhase> getListePhase() {
		return listePhase;
	}

	public void setListePhase(List<TraitementPhase> listePhase) {
		this.listePhase = listePhase;
	}

	public String getPhaseAExecuter() {
		return phaseAExecuter;
	}

	public void setPhaseAExecuter(String phaseAExecuter) {
		this.phaseAExecuter = phaseAExecuter;
	}

	private VObject getViewPilotageBAS() {
		return viewPilotageBAS;
	}

	private void setViewPilotageBAS(VObject viewPilotageBAS) {
		this.viewPilotageBAS = viewPilotageBAS;
	}

	private VObject getViewRapportBAS() {
		return viewRapportBAS;
	}

	private void setViewRapportBAS(VObject viewRapportBAS) {
		this.viewRapportBAS = viewRapportBAS;
	}

	private VObject getViewFichierBAS() {
		return viewFichierBAS;
	}

	private void setViewFichierBAS(VObject viewFichierBAS) {
		this.viewFichierBAS = viewFichierBAS;
	}

	private VObject getViewEntrepotBAS() {
		return viewEntrepotBAS;
	}

	private void setViewEntrepotBAS(VObject viewEntrepotBAS) {
		this.viewEntrepotBAS = viewEntrepotBAS;
	}

	private VObject getViewArchiveBAS() {
		return viewArchiveBAS;
	}

	private void setViewArchiveBAS(VObject viewArchiveBAS) {
		this.viewArchiveBAS = viewArchiveBAS;
	}
	
}
