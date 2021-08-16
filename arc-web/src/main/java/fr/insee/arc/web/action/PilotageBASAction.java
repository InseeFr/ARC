package fr.insee.arc.web.action;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
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

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PilotageBASAction extends ArcAction<EnvManagementModel> {

	private static final String ENV_DESCRIPTION = "envDescription";

	private static final String ENTRY_DATE = "date_entree";

	private static final String ACTION_NAME = "EnvManagement";

	private static final String RESULT_SUCCESS = "jsp/gererPilotageBAS.jsp";

	private static final String WRITING_REPO = "entrepotEcriture";

	private static final Logger LOGGER = LogManager.getLogger(PilotageBASAction.class);
	
	@Autowired
	private MessageSource messageSource;
	
	private VObject viewPilotageBAS = new ViewPilotageBAS();
	
	private VObject viewRapportBAS = new ViewRapportBAS();

	private VObject viewFichierBAS = new ViewFichierBAS();

	private VObject viewEntrepotBAS = new ViewEntrepotBAS();

	private VObject viewArchiveBAS = new ViewArchiveBAS();

	private List<TraitementPhase> listePhase;

	public PilotageBASAction() {	
		this.setListePhase(TraitementPhase.getListPhaseC());	
	}
	
	@ModelAttribute
	public void specificModelAttributes(Model model) {
		model.addAttribute("listePhase", listePhase);
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
			vObjectService.destroy(getViewPilotageBAS());
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

	// visual des Pilotages du bac à sable
	public void initializePilotageBAS() {
		LoggerHelper.debug(LOGGER, "* initializePilotageBAS *");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
        requete.append("select * from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T)+" order by date_entree desc");
		
		this.vObjectService.initialize(
				getViewPilotageBAS(), requete, 
				getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T), defaultInputFields,
				this::reworkPilotageContent);
		
		PreparedStatementBuilder envQuery = new PreparedStatementBuilder();
		envQuery.append("select env_description from arc.ext_etat_jeuderegle where replace(id,'.','_') = ");
		envQuery.append(envQuery.quoteText(getBacASable()));
		try {
			String envDescription = UtilitaireDao.get(POOLNAME).getString(null, envQuery);
			getViewPilotageBAS().setCustomValue(ENV_DESCRIPTION, envDescription);
		} catch (SQLException e) {
			loggerDispatcher.error("Error in initializePilotageBAS", e, LOGGER);
		}
		
	}
	
	private ArrayList<ArrayList<String>> reworkPilotageContent(ArrayList<ArrayList<String>> content) {
		GenericBean g = new GenericBean(content);
		HashMap<String, ArrayList<String>> mapContent = g.mapContent();
		HashMap<String, String> mt = g.mapTypes();

		ArrayList<String> newHeaders = new ArrayList<>();

		ArrayList<ArrayList<String>> newContent = new ArrayList<>();

		// ne garder les colonnes avec au moins un enregistrements dedans

		newHeaders.add(ENTRY_DATE);

		for (Map.Entry<String, ArrayList<String>> entry : mapContent.entrySet()) {
			boolean toKeep = false;
			int i = 0;

			while (i < entry.getValue().size() && !entry.getKey().equals(ENTRY_DATE)) {

				if (Integer.parseInt(entry.getValue().get(i)) > 0) {
					toKeep = true;
				}
				i++;
			}

			if (toKeep) {
				newHeaders.add(entry.getKey());
			}
		}

		// ordonner les colonnes selon la phase et l'etat
		Collections.sort(newHeaders, new Comparator<String>() {
			public int compare(String a, String b) {
				String phaseA = ManipString.substringBeforeLast(a, "_").toUpperCase();
				String etatA = ManipString.substringAfterLast(a, "_").toUpperCase();

				String phaseB = ManipString.substringBeforeLast(b, "_").toUpperCase();
				String etatB = ManipString.substringAfterLast(b, "_").toUpperCase();

				//Check if one of the two headers is date_entree. Because date_entree is not a TypeTraitementPhase
				try {
					TraitementPhase.valueOf(phaseA);
				} catch (Exception e) {
					return -1;
				}

				try {
					TraitementPhase.valueOf(phaseB);
				} catch (Exception e) {
					return 1;
				}

				if (TraitementPhase.valueOf(phaseA).getOrdre() > TraitementPhase.valueOf(phaseB).getOrdre()) {
					return 1;
				}
				if (TraitementPhase.valueOf(phaseA).getOrdre() < TraitementPhase.valueOf(phaseB).getOrdre()) {
					return -1;
				}
				if (TraitementEtat.valueOf(etatA).getOrdre() > TraitementEtat.valueOf(etatB).getOrdre()) {
					return 1;
				}
				if (TraitementEtat.valueOf(etatA).getOrdre() < TraitementEtat.valueOf(etatB).getOrdre()) {
					return -1;
				}
				return 0;

			}
		});

		// ajouter les coloonnes
		newContent.add(newHeaders);

		// ajout des types des colonnes
		ArrayList<String> newTypes = new ArrayList<>();

		for (int j = 0; j < newHeaders.size(); j++) {
			newTypes.add(mt.get(newHeaders.get(j)));
		}

		newContent.add(newTypes);

		// ajout du contenu relatifs aux colonnes
		if (!mapContent.isEmpty()) {
			for (int k = 0; k < mapContent.get(newHeaders.get(0)).size(); k++) {
				ArrayList<String> newLine = new ArrayList<>();
				for (int j = 0; j < newHeaders.size(); j++) {
					newLine.add(mapContent.get(newHeaders.get(j)).get(k));
				}

				newContent.add(newLine);
			}
		}

		return newContent;
	}

	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * TODO : use liquibase instead
	 * 
	 * @return
	 */
	@RequestMapping("/enterPilotageBAS")
	public String enterPilotageBAS(Model model) {
		ApiInitialisationService.bddScript(null, new String[] {getBacASable()});
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@PostMapping(value = {"/selectPilotageBAS"}
//	,consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
	)
	public String selectPilotageBAS(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	@PostMapping("/updateEnvDescription")
	public String updateEnvDescription(Model model) {
		PreparedStatementBuilder envQuery = new PreparedStatementBuilder();
		envQuery.append("update arc.ext_etat_jeuderegle set env_description = ");
		envQuery.append(envQuery.quoteText(getViewPilotageBAS().getCustomValue(ENV_DESCRIPTION)));
		envQuery.append("where replace(id,'.','_') = ");
		envQuery.append(envQuery.quoteText(getBacASable()));
	try {
		UtilitaireDao.get(POOLNAME).executeRequest(null, envQuery);
	} catch (SQLException e) {		
		loggerDispatcher.error("Error in updateEnvDescription", e, LOGGER);
	}
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	@RequestMapping("/sortPilotageBAS")
	public String sortPilotageBAS(Model model) {		
		this.vObjectService.sort(getViewPilotageBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

	// visual des Pilotages du bac à sable
	public void initializeRapportBAS() {
		LoggerHelper.debug(LOGGER, "* initializeRapportBAS *");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
		requete.append(
				"select date_entree, phase_traitement, array_to_string(etat_traitement,'$') as etat_traitement, rapport, count(1) as nb ");
		requete.append("from " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
		requete.append(" where rapport is not null ");
		requete.append("group by date_entree, phase_traitement, etat_traitement, rapport ");
		requete.append("order by date_entree asc ");
		this.vObjectService.initialize(getViewRapportBAS(), requete, null, defaultInputFields);
	}

	@RequestMapping("/selectRapportBAS")
	public String selectRapportBAS(Model model) {
		
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortRapportBAS")
	public String sortRapportBAS(Model model) {
		
		this.vObjectService.sort(getViewRapportBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

	@RequestMapping("/informationInitialisationPROD")
    public String informationInitialisationPROD(Model model, HttpServletRequest request) {
    	try {
			String time = UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("SELECT last_init from arc.pilotage_batch"));
			String state = UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("SELECT case when operation='O' then 'active' else 'inactive' end from arc.pilotage_batch;"));
			state = messageSource.getMessage("managementSandbox.batch.status." + state, null, request.getLocale());
    		this.getViewPilotageBAS().setMessage("managementSandbox.batch.status");
    		this.getViewPilotageBAS().setMessageArgs(state, time);

		} catch (SQLException e) {
			loggerDispatcher.error("Error in informationInitialisationPROD", e, LOGGER);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/retarderBatchInitialisationPROD")
    public String retarderBatchInitialisationPROD(Model model) {
    	try {
			UtilitaireDao.get("arc").executeRequest(null, 
					new PreparedStatementBuilder("UPDATE arc.pilotage_batch set last_init=to_char(current_date + interval '7 days','yyyy-mm-dd')||':22';"));

			String time = UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("SELECT last_init from arc.pilotage_batch"));
			this.getViewPilotageBAS().setMessage("managementSandbox.batch.init.time");
			this.getViewPilotageBAS().setMessageArgs(time);

			
		} catch (SQLException e) {
			loggerDispatcher.error("Error in retarderInitialisationPROD", e, LOGGER);
		}
    	
    	return generateDisplay(model, RESULT_SUCCESS);
    }
    
    @RequestMapping("/demanderBatchInitialisationPROD")
    public String demanderBatchInitialisationPROD(Model model) {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, new PreparedStatementBuilder("UPDATE arc.pilotage_batch set last_init=to_char(current_date-interval '1 days','yyyy-mm-dd')||':22';"));
			
			String time = UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("SELECT last_init from arc.pilotage_batch"));
			this.getViewPilotageBAS().setMessage("managementSandbox.batch.init.time");
			this.getViewPilotageBAS().setMessageArgs(time);

			
		} catch (SQLException e) {
			loggerDispatcher.error("Error in demanderBatchInitialisationPROD", e, LOGGER);
		}
    	
    	return generateDisplay(model, RESULT_SUCCESS);
    }
    
    @RequestMapping("/toggleOnPROD")
    public String toggleOnPROD(Model model) {
    	try {
			UtilitaireDao.get("arc").executeRequest(null, new PreparedStatementBuilder("UPDATE arc.pilotage_batch set operation='O'; "));
			this.getViewPilotageBAS().setMessage("managementSandbox.batch.status.switch.on");
		} catch (SQLException e) {
			loggerDispatcher.error("Error in toggleOnPROD", e, LOGGER);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/toggleOffPROD")
    public String toggleOffPROD(Model model) {
    	try {
			UtilitaireDao.get("arc").executeRequest(null, new PreparedStatementBuilder("UPDATE arc.pilotage_batch set operation='N'; "));
			this.getViewPilotageBAS().setMessage("managementSandbox.batch.status.switch.off");
		} catch (SQLException e) {
			loggerDispatcher.error("Error in toggleOffPROD", e, LOGGER);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }
	
	// Actions du bac à sable

	@RequestMapping("/filesUploadBAS")
	public String filesUploadBAS(Model model) {
		LoggerHelper.debug(LOGGER, "* /* filesUploadBAS : */ *");
		
		String writingRepo = this.getViewEntrepotBAS().getCustomValue(WRITING_REPO);
		if (writingRepo != null
				&& !writingRepo.equals("")
				&& viewPilotageBAS.getFileUpload() != null) {

			String repertoireUpload = Paths.get(
					this.repertoire + getBacASable().toUpperCase(), 
					TraitementPhase.RECEPTION + "_" + writingRepo)
					.toString();
			LoggerHelper.trace(LOGGER, "repertoireUpload :", repertoireUpload);
			this.vObjectService.upload(getViewPilotageBAS(), repertoireUpload);
		} else {
			String msg = "";
			if (viewPilotageBAS.getFileUpload() == null) {
				msg = "Erreur : aucun fichier selectionné\n";
				this.getViewPilotageBAS().setMessage("Erreur : aucun fichier selectionné.");
			}

			if (writingRepo == null || writingRepo.equals("")) {
				msg += "Erreur : aucun entrepot selectionné\n";
			}

			this.getViewPilotageBAS().setMessage(msg);
		}
		this.getViewEntrepotBAS().setCustomValue(WRITING_REPO, null);
		if (!isEnvProd()) {
			// Lancement de l'initialisation dans la foulée
			ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA,
					getBacASable(), this.repertoire,
					String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
			ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA,
					getBacASable(), this.repertoire,
					String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();
		}
		
		
		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Initialisation de la vue sur la table contenant la liste des fichiers du
	 * répertoire d'archive
	 */
	public void initializeArchiveBAS() {
		LoggerHelper.debug(LOGGER, "* /* initializeArchiveBAS  */ *");
		String entrepotLecture = this.getViewEntrepotBAS().getCustomValue("entrepotLecture");
		if (entrepotLecture != null
				&& !entrepotLecture.equals("")) {
			HashMap<String, String> defaultInputFields = new HashMap<>();
			
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
			requete.append("select * from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_ARCHIVE)+" where entrepot="
	                    + requete.quoteText(entrepotLecture) + " ");

			this.vObjectService.initialize(getViewArchiveBAS(), requete, null, defaultInputFields);
		} else {

			this.vObjectService.destroy(getViewArchiveBAS());
		}
	}

	public void initializeEntrepotBAS() {
		LoggerHelper.debug(LOGGER, "* initializeEntrepotBAS *");
	
		HashMap<String, String> defaultInputFields = new HashMap<>();
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
	
		try {
			if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot"))) {
				requete.append("select id_entrepot from arc.ihm_entrepot");
			} else {
				requete.append("select ''::text as id_entrepot");
			}
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, "error when initialize repository", e);
		}
	
		this.vObjectService.initialize(this.getViewEntrepotBAS(), requete, null, defaultInputFields);
	}

	/**
	 * Fabrication d'une table temporaire avec comme contenu le nom des archives
	 * d'un entrepot donné puis Ouverture d'un VObject sur cette table
	 *
	 * @return
	 */
	@RequestMapping("/visualiserEntrepotBAS")
	public String visualiserEntrepotBAS(Model model) {		
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	@SQLExecutor
	@RequestMapping("/downloadEnveloppeFromArchiveBAS")
	public void downloadEnveloppeFromArchiveBAS(HttpServletResponse response) {
		
		loggerDispatcher.trace("*** Téléchargement des enveloppes à partir de l'archive ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = getViewArchiveBAS().mapContentSelected();

		initializeArchiveBAS();
		
		
		PreparedStatementBuilder querySelection = new PreparedStatementBuilder();
		
		querySelection.append("select distinct alias_de_table.nom_archive as nom_fichier from (");
		querySelection.append(this.getViewArchiveBAS().getMainQuery());
		querySelection.append(") alias_de_table ");
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
			
			PreparedStatementBuilder requete=new PreparedStatementBuilder();
			requete.append("SELECT DISTINCT entrepot FROM (");
			requete.append(this.getViewArchiveBAS().getMainQuery());
			requete.append(") alias_de_table ");
			
			g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, requete));
			entrepot = g.mapContent().get("entrepot").get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE");
		String chemin = Paths.get(this.repertoire, getBacASable().toUpperCase()).toString();
		
		this.vObjectService.downloadEnveloppe(getViewArchiveBAS(), response, querySelection, chemin, listRepertoire);
	}

	@RequestMapping("/executerBatch")
	public String executerBatch(Model model, TraitementPhase phaseAExecuter) {
		loggerDispatcher.debug("executerBatch", LOGGER);
		loggerDispatcher.debug(String.format("Service %s", phaseAExecuter), LOGGER);
		
		
		if (!phaseAExecuter.equals(TraitementPhase.INITIALISATION))
		{
			ApiInitialisationService.synchroniserSchemaExecution(null, ApiService.IHM_SCHEMA,
					getBacASable());
		}
		
		ApiServiceFactory.getService(phaseAExecuter.toString(), ApiService.IHM_SCHEMA, getBacASable(),
				this.repertoire, "10000000"
				//,"1" // to set batch mode or not
				).invokeApi();
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/*
	 * Allow the user to select files for the undoBatch fucntionnality
	 */
	public PreparedStatementBuilder undoFilesSelection() {
		PreparedStatementBuilder selectedSrc = new PreparedStatementBuilder();

		HashMap<String, ArrayList<String>> m = new HashMap<>(
				getViewFichierBAS().mapContentSelected());
		
		if (!m.isEmpty() && m.get("id_source") != null) {
			for (int i = 0; i < m.get("id_source").size(); i++) {
				if (selectedSrc.length()>0) {
					selectedSrc.append("\n UNION ALL SELECT ");
				} else {
					selectedSrc.append("SELECT ");
				}
				selectedSrc.append(" " + selectedSrc.quoteText(m.get("id_source").get(i)) + "::text as id_source ");
			}
		}
		return selectedSrc;
	}

	@RequestMapping("/undoBatch")
	public String undoBatch(Model model, TraitementPhase phaseAExecuter) {
		loggerDispatcher.debug("undoBatch", LOGGER);
		loggerDispatcher.debug(String.format("undo service %s", phaseAExecuter), LOGGER);
		
		ApiService.backToTargetPhase(phaseAExecuter, getBacASable(), this.repertoire, undoFilesSelection());

		return generateDisplay(model, RESULT_SUCCESS);
	}

	// Bouton undo

	@RequestMapping("/resetBAS")
	public String resetBAS(Model model) {
		
		ApiService.resetBAS(getBacASable(), this.repertoire);

		return generateDisplay(model, RESULT_SUCCESS);
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
				PreparedStatementBuilder requete = new PreparedStatementBuilder();
	            requete.append("SELECT container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
	            requete.append(" FROM "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)+" ");
	            requete.append(" WHERE date_entree" + requete.sqlEqual(selectionLigne.get(ENTRY_DATE).get(0), "text"));
	            requete.append(" AND array_to_string(etat_traitement,'$')" + requete.sqlEqual(etat, "text"));
	            requete.append(" AND phase_traitement" + requete.sqlEqual(phase, "text"));
			
			this.vObjectService.initialize(getViewFichierBAS(), requete, null, defaultInputFields);
			
		} else if (!selectionLigneRapport.isEmpty()) {
			HashMap<String, String> type = getViewRapportBAS().mapHeadersType();
			HashMap<String, String> defaultInputFields = new HashMap<>();
		
			PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("SELECT container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
            requete.append(" FROM "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)+" ");
            requete.append(" WHERE date_entree" + requete.sqlEqual(selectionLigneRapport.get(ENTRY_DATE).get(0), "text"));
            requete.append(" AND array_to_string(etat_traitement,'$')"
                    + requete.sqlEqual(selectionLigneRapport.get("etat_traitement").get(0), type.get("etat_traitement")));
            requete.append(" AND phase_traitement"
                    + requete.sqlEqual(selectionLigneRapport.get("phase_traitement").get(0), type.get("phase_traitement")));
            requete.append(" AND rapport" + requete.sqlEqual(selectionLigneRapport.get("rapport").get(0), type.get("rapport")));
			

			this.vObjectService.initialize(getViewFichierBAS(), requete, null, defaultInputFields);
			
		} else {
			this.vObjectService.destroy(getViewFichierBAS());
		}
	}

	@RequestMapping("/selectFichierBAS")
	public String selectFichierBAS(Model model) {
		
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortFichierBAS")
	public String sortFichierBAS(Model model) {
		
		this.vObjectService.sort(getViewFichierBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

	@RequestMapping("/downloadFichierBAS")
	public void downloadFichierBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des fichiers ***", LOGGER);
		// récupération de la liste des id_source

		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		PreparedStatementBuilder querySelection = this.vObjectService.queryView(getViewFichierBAS());
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionner
		//
		if (!selection.isEmpty()) {
			querySelection.append(" AND id_source IN (" + querySelection.sqlListe(selection.get("id_source")) + ") ");
		}

		// optimisation pour avoir des bloc successifs sur la même archive
		querySelection.append(" ORDER by container ");

		this.vObjectService.downloadXML(getViewFichierBAS(), response, querySelection, this.repertoire,
				(String) getBacASable(), TraitementPhase.RECEPTION.toString(),
				TraitementEtat.OK.toString(), TraitementEtat.KO.toString());

		loggerDispatcher.trace("*** Fin du téléchargement des fichiers XML ***", LOGGER);
	}

	/**
	 * Marquage de fichier pour le rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	@RequestMapping("/toRestoreBAS")
	public String toRestoreBAS(Model model) {		
		return restore(model, "'R'", "managementSandbox.batch.replay.files");
	}

	/**
	 * Marquage des archives à rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	@RequestMapping("/toRestoreArchiveBAS")
	public String toRestoreArchiveBAS(Model model) {
		return restore(model, "'RA'", "managementSandbox.batch.replay.archives");
	}

	private String restore(Model model, String code, String messageOk) {
		loggerDispatcher.trace("*** Marquage de fichier à rejouer ***", LOGGER);
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
	
		PreparedStatementBuilder querySelection = requestSelectToMark(selection);
	
		PreparedStatementBuilder updateToDelete = requeteUpdateToMark(querySelection, code);
		String message;
		try {
	
			UtilitaireDao.get("arc").executeRequest(null, updateToDelete);
			message = messageOk;
		} catch (SQLException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "managementSandbox.batch.replay.error";
		}
	
		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		if (!isEnvProd()) {
		// Lancement de l'initialisation dans la foulée
			loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
			ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA,
					getBacASable(), this.repertoire,
					String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
			ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA,
					getBacASable(), this.repertoire,
					String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();
		}
		this.getViewPilotageBAS().setMessage(message);
	
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/downloadBdBAS")
	public void downloadBdBAS(HttpServletResponse response) throws Exception {
		Map<String, ArrayList<String>> selectionLigne = getViewPilotageBAS().mapContentSelected();
		ArrayList<String> selectionColonne = getViewPilotageBAS().listHeadersSelected();
		ArrayList<Integer> selectionIndexColonne = getViewPilotageBAS().indexHeadersSelected();

		String phase = TraitementPhase.getPhase(selectionIndexColonne.get(0)).toString();
		String etat = selectionColonne.get(0).split("_")[1].toUpperCase();
		String date = selectionLigne.get(ENTRY_DATE).get(0);

		String[] etatList = etat.split("\\$");
		String etatBdd = "{" + etat.replace("$", ",") + "}";
		
				
		// Sélection des table métiers en fonction de la phase sélectionner (5 pour
		// mapping 1 sinon)
		ArrayList<String> tableDownload = new ArrayList<>();
		try {
			GenericBean g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, 
					ApiInitialisationService.requeteListAllTablesEnv(getBacASable())
					));
			if (!g.mapContent().isEmpty()) {
				ArrayList<String> envTables = g.mapContent().get("table_name");
				for (String table : envTables) {
					// selection des tables qui contiennent la phase dans leur nom
					for (int i = 0; i < etatList.length; i++) {
						if (ManipString.substringAfterFirst(table.toUpperCase(),".").startsWith(phase.toUpperCase() + "_")
								&& table.toUpperCase().endsWith("_" + etatList[i].toUpperCase())
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
		List<PreparedStatementBuilder> tableauRequete=new ArrayList<>();
		// Name of the file containing the data download
		List<String> fileNames = new ArrayList<>();

		PreparedStatementBuilder requete;
		
		for (String t : tableDownload) {
			
			// Check if the table to download got children
			requete=new PreparedStatementBuilder();
			requete.append(FormatSQL.getAllInheritedTables(ManipString.substringBeforeFirst(t, "."),
					ManipString.substringAfterFirst(t, ".")));
			requete.append(" LIMIT 1");
			
			
			if (Boolean.TRUE.equals(UtilitaireDao.get("arc").hasResults(null,requete))) {

				// Get the files to download
				requete = new PreparedStatementBuilder();
				requete.append(
						"SELECT id_source FROM " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
				requete.append("\n WHERE phase_traitement=" + requete.quoteText(phase) + " ");
				requete.append("\n AND etat_traitement=" + requete.quoteText(etatBdd) + "::text[] ");
				requete.append("\n AND date_entree=" + requete.quoteText(date) + " ");

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
					tableauRequete.add(new PreparedStatementBuilder("SELECT * FROM " + ApiService.tableOfIdSource(t, idSource)));
					fileNames.add(t + "_" + idSource);
				}

			}
			// if no children
			else {
				
				requete = new PreparedStatementBuilder();
				requete.append("WITH prep as ( ");
				requete.append("SELECT id_source FROM "
						+ getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
				requete.append("\n WHERE phase_traitement=" + requete.quoteText(phase) + " ");
				requete.append("\n AND etat_traitement=" + requete.quoteText(etatBdd) + "::text[] ");
				requete.append("\n AND date_entree=" + requete.quoteText(date) + " ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!getViewFichierBAS().mapContentSelected().isEmpty()) {
					requete.append("\n AND id_source IN (");
					requete.append(requete.sqlListe(getViewFichierBAS().mapContentSelected().get("id_source")));
					requete.append(")");
				}
				requete.append(" ) ");
				requete.append("\n SELECT * from " + t + " a where exists (select 1 from prep b where a.id_source=b.id_source) ");
				tableauRequete.add(requete);
				fileNames.add(t);
			}

		}
		
		this.vObjectService.download(getViewFichierBAS(), response, fileNames, tableauRequete);
	}

	@RequestMapping("/downloadEnveloppeBAS")
	public String downloadEnveloppeBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des enveloppes ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		
		initializeFichierBAS();
		
		PreparedStatementBuilder querySelection = new PreparedStatementBuilder();
	
		querySelection.append("select distinct alias_de_table.container as nom_fichier from (");
		querySelection.append(this.getViewFichierBAS().getMainQuery());
		querySelection.append(") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.getViewFichierBAS().getFilterFields(),
				this.getViewFichierBAS().getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection.append(" AND container IN (" + querySelection.sqlListe(selection.get("container")) + ") ");
		}

		loggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(),
				LOGGER);

		ArrayList<String> listRepertoire = new ArrayList<>();
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.OK);
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.KO);
		String chemin = Paths.get(this.repertoire, getBacASable().toUpperCase()).toString();
		this.vObjectService.downloadEnveloppe(getViewFichierBAS(), response, querySelection, chemin, listRepertoire);
		loggerDispatcher.trace("*** Fin du téléchargement des enveloppes ***", LOGGER);

		return "none";
	}

	/**
	 * Marquage de fichier pour suppression lors de la prochaine initialisation
	 *
	 * @return
	 */
	@RequestMapping("/toDeleteBAS")
	public String toDeleteBAS(Model model) {
		
		loggerDispatcher.trace("*** Marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();

		PreparedStatementBuilder querySelection = requestSelectToMark(selection);

		PreparedStatementBuilder updateToDelete = requeteUpdateToMark(querySelection, "'1'");
		String message;
		try {
			UtilitaireDao.get("arc").executeRequest(null, updateToDelete);
			message = "managementSandbox.batch.delete.ok";
		} catch (SQLException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "managementSandbox.batch.delete.error";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		if (!isEnvProd()) {
			loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
			ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA,
					getBacASable(), this.repertoire,
					String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
		}

		this.getViewPilotageBAS().setMessage(message);

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Suppression du marquage de fichier pour suppression lors de la prochaine
	 * initialisation
	 *
	 * @return
	 */
	@RequestMapping("/undoActionBAS")
	public String undoActionBAS(Model model) {
		
		loggerDispatcher.trace("*** Suppression du marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		PreparedStatementBuilder querySelection = requestSelectToMark(selection);
		PreparedStatementBuilder updateToDelete = requeteUpdateToMark(querySelection, "null");
		try {

			UtilitaireDao.get("arc").executeRequest(null, updateToDelete);
		} catch (SQLException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/** Prepare a request selecting the line to change when marking files for deletion/replay.*/
	private PreparedStatementBuilder requestSelectToMark(Map<String, ArrayList<String>> selection) {
		PreparedStatementBuilder querySelection = new PreparedStatementBuilder();
		querySelection.append("select distinct container, id_source from (");
		querySelection.append(this.getViewFichierBAS().getMainQuery());
		querySelection.append(") alias_de_table ");
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
		return querySelection;
	}

	/** Mark the line selected by querySelection for deletion/replay (depending on value).*/
	private PreparedStatementBuilder requeteUpdateToMark(PreparedStatementBuilder querySelection, String value) {
		PreparedStatementBuilder updateToDelete = new PreparedStatementBuilder();
		updateToDelete.append("WITH ");
		updateToDelete.append("prep AS ( ");
		updateToDelete.append(querySelection);
		updateToDelete.append("         ) ");
		updateToDelete.append("UPDATE " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER) + " a ");
		updateToDelete.append("SET to_delete=" + value + " ");
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
	public String resetPhaseBAS(Model model) {
		Map<String, ArrayList<String>> selection = getViewFichierBAS().mapContentSelected();
		PreparedStatementBuilder querySelection = this.vObjectService.queryView(getViewFichierBAS());

		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// choisis pour le retour arriere
		//
		if (!selection.isEmpty()) {
			querySelection.append(" AND id_source IN " + querySelection.sqlListe(selection.get("id_source")) + " ");
		}

		// On recupere la phase
		String phase = getViewFichierBAS().mapContent().get("phase_traitement").get(0);

		// Lancement du retour arrière
		ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				ApiService.IHM_SCHEMA, getBacASable(), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			serv.retourPhasePrecedente(TraitementPhase.valueOf(phase), querySelection, null);
		} finally {
			serv.finaliser();
		}
		return generateDisplay(model, RESULT_SUCCESS);
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
