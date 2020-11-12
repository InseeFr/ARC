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
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.model.SessionParameters;
import fr.insee.arc.web.util.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererPilotageBAS8.jsp"),
		@Result(name = "index", location = "/jsp/index.jsp") })
public class PilotageBAS8Action extends ArcAction {

	private static final String ARC = "ARC_";

	private static final String WRITING_REPO = "entrepotEcriture";

	private static final Logger LOGGER = LogManager.getLogger(PilotageBAS8Action.class);

	@Autowired
	@Qualifier("viewPilotageBAS8")
	VObject viewPilotageBAS8;

	@Autowired
	@Qualifier("viewRapportBAS8")
	VObject viewRapportBAS8;

	@Autowired
	@Qualifier("viewFichierBAS8")
	VObject viewFichierBAS8;

	@Autowired
	@Qualifier("viewEntrepotBAS8")
	VObject viewEntrepotBAS8;

	@Autowired
	@Qualifier("viewArchiveBAS8")
	VObject viewArchiveBAS8;

	private List<TraitementPhase> listePhase;
	
	/**
	 * Phase sélectionnée par l'utilisateur
	 */
	private String phaseAExecuter;

	public void initializeEntrepotBAS8() {
		LoggerHelper.debug(LOGGER, "* initializeEntrepotBAS8 *");

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

		this.viewEntrepotBAS8.initialize(requete.toString(), null, defaultInputFields);
	}

	// visual des Pilotages du bac à sable
	public void initializePilotageBAS8() {
		LoggerHelper.debug(LOGGER, "* initializePilotageBAS8 *");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		
		StringBuilder requete = new StringBuilder();
        requete.append("select * from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T)+" order by date_entree desc");
		
		this.viewPilotageBAS8.initialize(requete.toString(),
				getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER_T), defaultInputFields);
	}

	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * TODO : use liquibase instead
	 * 
	 * @return
	 */
	@Action(value = "/enterPilotageBAS8")
	public String enterPilotageBAS8() {
		initialize();
		ApiInitialisationService.bddScript((String) getSession().get(SessionParameters.ENV), null);
		return generateDisplay();
	}

	@Action(value = "/selectPilotageBAS8")
	public String selectPilotageBAS8() {
		initialize();
		return generateDisplay();
	}

	@Action(value = "/sortPilotageBAS8")
	public String sortPilotageBAS8() {
		initialize();
		this.viewPilotageBAS8.sort();
		return generateDisplay();

	}

	// visual des Pilotages du bac à sable
	public void initializeRapportBAS8() {
		LoggerHelper.debug(LOGGER, "* initializeRapportBAS8 *");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		StringBuilder requete = new StringBuilder();
		requete.append(
				"select date_entree, phase_traitement, array_to_string(etat_traitement,'$') as etat_traitement, rapport, count(1) as nb ");
		requete.append("from " + getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
		requete.append(" where rapport is not null ");
		requete.append("group by date_entree, phase_traitement, etat_traitement, rapport ");
		requete.append("order by date_entree asc ");
		this.viewRapportBAS8.initialize(requete.toString(), null, defaultInputFields);
	}

	@Action(value = "/selectRapportBAS8")
	public String selectRapportBAS8() {
		initialize();
		return generateDisplay();
	}

	@Action(value = "/sortRapportBAS8")
	public String sortRapportBAS8() {
		initialize();
		this.viewRapportBAS8.sort();
		return generateDisplay();

	}

	@Action(value = "/informationInitialisationPROD")
    public String informationInitialisationPROD() {
		initialize();
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
			String etat=UtilitaireDao.get("arc").getString(null, "SELECT case when operation='O' then 'actif' else 'inactif' end from arc.pilotage_batch;");
			
    		viewPilotageBAS8.setMessage("Le batch est "+etat+".\nLe prochain batch d'initialisation est programmé aprés : "+heure);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return generateDisplay();
    }

    @Action(value = "/retarderBatchInitialisationPROD")
    @SQLExecutor
    public String retarderBatchInitialisationPROD() {
    	initialize();
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set last_init=to_char(current_date + interval '7 days','yyyy-mm-dd')||':22';");

			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
    		viewPilotageBAS8.setMessage("Le prochain batch d'initialisation aura lieu ce soir après : "+heure);

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return generateDisplay();
    }
    
    @Action(value = "/demanderBatchInitialisationPROD")
    @SQLExecutor
    public String demanderBatchInitialisationPROD() {
    	initialize();
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set last_init=to_char(current_date-interval '1 days','yyyy-mm-dd')||':22';");
			
			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
	    	viewPilotageBAS8.setMessage("Le prochain batch d'initialisation aura lieu dans quelques minutes (après "+heure+") ");

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return generateDisplay();
    }
    
    @Action(value = "/toggleOnPROD")
    @SQLExecutor
    public String toggleOnPROD() {
    	initialize();
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set operation='O'; ");
    		viewPilotageBAS8.setMessage("Production activée ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return generateDisplay();
    }

    @Action(value = "/toggleOffPROD")
    @SQLExecutor
    public String toggleOffPROD() {
    	initialize();
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set operation='N'; ");
    		viewPilotageBAS8.setMessage("Production arretée ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return generateDisplay();
    }
	
	// Actions du bac à sable

	@Action(value = "/filesUploadBAS8")
	public String filesUploadBAS8() {
		LoggerHelper.debug(LOGGER, "* /* filesUploadBAS : " + this.viewEntrepotBAS8.getCustomValues() + " */ *");

		initializeArcActionWithProperties();
		recupererEnvironnementTravail();
		
		if (this.viewEntrepotBAS8.getCustomValues() != null
				&& !this.viewEntrepotBAS8.getCustomValues().get(WRITING_REPO).equals("")
				&& this.viewPilotageBAS8.getFileUploadFileName() != null) {
			String repertoireUpload = this.repertoire + getSession().get(SessionParameters.ENV).toString().toUpperCase()
					+ File.separator + TraitementPhase.RECEPTION + "_"
					+ this.viewEntrepotBAS8.getCustomValues().get(WRITING_REPO);
			LoggerHelper.trace(LOGGER, "repertoireUpload :", repertoireUpload);
			this.viewPilotageBAS8.upload(repertoireUpload);
		} else {
			String msg = "";
			if (this.viewPilotageBAS8.getFileUploadFileName() == null) {
				msg = "Erreur : aucun fichier selectionné\n";
				this.viewPilotageBAS8.setMessage("Erreur : aucun fichier selectionné.");
			}

			if (this.viewEntrepotBAS8.getCustomValues() == null
					|| this.viewEntrepotBAS8.getCustomValues().get(WRITING_REPO).equals("")) {
				msg += "Erreur : aucun entrepot selectionné\n";
			}

			this.viewPilotageBAS8.setMessage(msg);
		}
		this.viewEntrepotBAS8.getCustomValues().put(WRITING_REPO, null);
		// Lancement de l'initialisation dans la foulée
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();

		initialize();
		
		return generateDisplay();

	}

	/**
	 * Initialisation de la vue sur la table contenant la liste des fichiers du
	 * répertoire d'archive
	 */
	public void initializeArchiveBAS8() {
		LoggerHelper.debug(LOGGER, "* /* initializeArchiveBAS  */ *");
		if (this.viewEntrepotBAS8.getCustomValues().containsKey("entrepotLecture")
				&& !this.viewEntrepotBAS8.getCustomValues().get("entrepotLecture").equals("")) {
			HashMap<String, String> defaultInputFields = new HashMap<>();
			
			 StringBuilder requete = new StringBuilder();
			 requete.append("select * from "+getBddTable().getQualifedName(BddTable.ID_TABLE_PILOTAGE_ARCHIVE)+" where entrepot='"
	                    + this.viewEntrepotBAS8.getCustomValues().get("entrepotLecture") + "'");

			this.viewArchiveBAS8.initialize(requete.toString(), null, defaultInputFields);
		} else {

			this.viewArchiveBAS8.destroy();
		}
	}

	/**
	 * Fabrication d'une table temporaire avec comme contenu le nom des archives
	 * d'un entrepot donné puis Ouverture d'un VObject sur cette table
	 *
	 * @return
	 */
	@Action(value = "/visualiserEntrepotBAS8")
	public String visualiserEntrepotBAS8() {
		initialize();
		return generateDisplay();
	}

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	@SQLExecutor
	@Action(value = "/downloadEnveloppeFromArchiveBAS8")
	public String downloadEnveloppeFromArchiveBAS8() {
		initialize();
		LoggerDispatcher.trace("*** Téléchargement des enveloppes à partir de l'archive ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = this.viewArchiveBAS8.mapContentSelected();

		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct alias_de_table.nom_archive as nom_fichier from ("
				+ this.viewArchiveBAS8.getMainQuery() + ") alias_de_table ");
		querySelection.append(this.viewArchiveBAS8.buildFilter(this.viewArchiveBAS8.getFilterFields(),
				this.viewArchiveBAS8.getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection.append(" AND nom_archive IN " + Format.sqlListe(selection.get("nom_archive")) + " ");
		}

		LoggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(),
				LOGGER);

		ArrayList<String> listRepertoire = new ArrayList<>();
		GenericBean g;
		String entrepot = "";
		try {
			g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
					"SELECT DISTINCT entrepot FROM (" + this.viewArchiveBAS8.getMainQuery() + ") alias_de_table "));
			entrepot = g.mapContent().get("entrepot").get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE");
		String chemin = this.repertoire + File.separator
				+ getSession().get(SessionParameters.ENV).toString().toUpperCase();
		this.viewArchiveBAS8.downloadEnveloppe(querySelection.toString(), chemin, listRepertoire);
		return "none";
	}

	@Action(value = "/executerBatch")
	public String executerBatch() {
		LoggerDispatcher.debug("executerBatch", LOGGER);
		LoggerDispatcher.debug(String.format("Service %s", phaseAExecuter), LOGGER);
		initialize();
		ApiInitialisationService.synchroniserSchemaExecution(null, "arc.ihm",
				(String) getSession().get(SessionParameters.ENV));

		ApiServiceFactory.getService(this.phaseAExecuter, "arc.ihm", (String) getSession().get(SessionParameters.ENV),
				this.repertoire, "10000000").invokeApi();
		return generateDisplay();
	}

	/*
	 * Allow the user to select files for the undoBatch fucntionnality
	 */
	public String undoFilesSelection() {
		String selectedSrc = null;

		HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>(
				viewFichierBAS8.mapContentSelected());

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

	@Action(value = "/undoBatch")
	public String undoBatch() {
		LoggerDispatcher.debug("undoBatch", LOGGER);
		LoggerDispatcher.debug(String.format("undo service %s", phaseAExecuter), LOGGER);
		initialize();
		
		if (TraitementPhase.valueOf(this.phaseAExecuter).getOrdre()==0)
		{
			resetBAS8();
			return generateDisplay();
		}
		
		ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				"arc.ihm", (String) getSession().get(SessionParameters.ENV), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			serv.retourPhasePrecedente(TraitementPhase.valueOf(this.phaseAExecuter), undoFilesSelection(),
					new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK, TraitementEtat.KO)));
		} finally {
            serv.finaliser();
		}
		return generateDisplay();
	}

	// Bouton undo

	@Action(value = "/resetBAS8")
	public String resetBAS8() {
		initialize();
		try {
			ApiInitialisationService.clearPilotageAndDirectories(this.repertoire,
					(String) getSession().get(SessionParameters.ENV));
		} catch (Exception e) {
			e.printStackTrace();
			viewPilotageBAS8.setMessage("Problème : " + e.getMessage());
		}
		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				"arc.ihm", (String) getSession().get(SessionParameters.ENV), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			service.resetEnvironnement();
		} finally {
			service.finaliser();
		}
		return generateDisplay();
	}

	// visual des Fichiers
	public void initializeFichierBAS8() {
		LoggerHelper.debug(LOGGER, "initializeFichierBAS8");
		Map<String, ArrayList<String>> selectionLigne = this.viewPilotageBAS8.mapContentSelected();
		ArrayList<String> selectionColonne = this.viewPilotageBAS8.listHeadersSelected();

		Map<String, ArrayList<String>> selectionLigneRapport = this.viewRapportBAS8.mapContentSelected();

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
			
			this.viewFichierBAS8.initialize(requete.toString(), null, defaultInputFields);
		} else if (!selectionLigneRapport.isEmpty()) {

			HashMap<String, String> type = this.viewRapportBAS8.mapHeadersType();
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
			

			this.viewFichierBAS8.initialize(requete.toString(), null, defaultInputFields);
		} else {
			this.viewFichierBAS8.destroy();
		}
	}

	@Action(value = "/selectFichierBAS8")
	public String selectFichierBAS8() {
		initialize();
		return generateDisplay();
	}

	@Action(value = "/sortFichierBAS8")
	public String sortFichierBAS8() {
		initialize();
		this.viewFichierBAS8.sort();
		return generateDisplay();

	}

	@Action(value = "/downloadFichierBAS8")
	public String downloadFichierBAS8() {

		recupererEnvironnementTravail();

		LoggerDispatcher.trace("*** Téléchargement des fichiers ***", LOGGER);
		// récupération de la liste des id_source

		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();
		StringBuilder querySelection = this.viewFichierBAS8.queryView();
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionner
		//
		if (!selection.isEmpty()) {
			querySelection.append(" AND id_source IN " + Format.sqlListe(selection.get("id_source")) + " ");
		}

		// optimisation pour avoir des bloc successifs sur la même archive
		querySelection.append(" order by container ");

		this.viewFichierBAS8.downloadXML(querySelection.toString(), this.repertoire,
				(String) getSession().get(SessionParameters.ENV), TraitementPhase.RECEPTION.toString(),
				TraitementEtat.OK.toString(), TraitementEtat.KO.toString());

		LoggerDispatcher.trace("*** Fin du téléchargement des fichiers XML ***", LOGGER);
		generateDisplay();
		return "none";
	}

	@Action(value = "/downloadBdBAS8")
	public String downloadBdBAS8() throws Exception {

		recupererEnvironnementTravail();

		Map<String, ArrayList<String>> selectionLigne = this.viewPilotageBAS8.mapContentSelected();
		ArrayList<String> selectionColonne = this.viewPilotageBAS8.listHeadersSelected();
		ArrayList<Integer> selectionIndexColonne = this.viewPilotageBAS8.indexHeadersSelected();

		
		String phase = TraitementPhase.getPhase(selectionIndexColonne.get(0)).toString();
		String etat = selectionColonne.get(0).split("_")[1].toUpperCase();
		String date = selectionLigne.get("date_entree").get(0);

		String[] etatList = etat.split("\\$");
		String etatBdd = "{" + etat.replace("$", ",") + "}";
		
				
		// Sélection des table métiers en fonction de la phase sélectionner (5 pour
		// mapping 1 sinon)
		ArrayList<String> tableDownload = new ArrayList<>();
		try {
			GenericBean g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, ApiInitialisationService
					.requeteListAllTablesEnv((String) getSession().get(SessionParameters.ENV))));
			if (!g.mapContent().isEmpty()) {
				ArrayList<String> envTables = g.mapContent().get("table_name");
				System.out.println("Le contenu de ma envTables : " + envTables);
				for (String table : envTables) {
					// selection des tables qui contiennent la phase dans leur nom
					for (int i = 0; i < etatList.length; i++) {
						if (table.toUpperCase().startsWith(((String) getSession().get(SessionParameters.ENV)).toUpperCase()+"." + phase.toUpperCase())
								&& table.toUpperCase().endsWith("_" + etatList[i].toUpperCase())
								&& !tableDownload.contains(table)) {
							tableDownload.add(table);

						}
					}
				}
			}
		} catch (SQLException e) {
			LoggerDispatcher.error(e, LOGGER);
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
				if (!this.viewFichierBAS8.mapContentSelected().isEmpty()) {
					ArrayList<String> filesSelected = this.viewFichierBAS8.mapContentSelected().get("id_source");
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
				if (!this.viewFichierBAS8.mapContentSelected().isEmpty()) {
					ArrayList<String> filesSelected = this.viewFichierBAS8.mapContentSelected().get("id_source");
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
		
		this.viewFichierBAS8.download(fileNames, tableauRequete);

		return "none";

	}

	@Action(value = "/downloadEnveloppeBAS8")
	public String downloadEnveloppeBAS8() {

		recupererEnvironnementTravail();

		LoggerDispatcher.trace("*** Téléchargement des enveloppes ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();

		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct alias_de_table.container as nom_fichier from ("
				+ this.viewFichierBAS8.getMainQuery() + ") alias_de_table ");
		querySelection.append(this.viewFichierBAS8.buildFilter(this.viewFichierBAS8.getFilterFields(),
				this.viewFichierBAS8.getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection.append(" AND container IN " + Format.sqlListe(selection.get("container")) + " ");
		}

		LoggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(),
				LOGGER);

		ArrayList<String> listRepertoire = new ArrayList<>();
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.OK);
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.KO);
		String chemin = this.repertoire + File.separator + ARC
				+ (String) getSession().get(SessionParameters.ENV).toString().toUpperCase();
		this.viewFichierBAS8.downloadEnveloppe(querySelection.toString(), chemin, listRepertoire);
		LoggerDispatcher.trace("*** Fin du téléchargement des enveloppes ***", LOGGER);

		return "none";
	}

	/**
	 * Marquage de fichier pour suppression lors de la prochaine initialisation
	 *
	 * @return
	 */
	@Action(value = "/toDeleteBAS8")
	public String toDeleteBAS8() {
		initialize();
		LoggerDispatcher.trace("*** Marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();

		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.viewFichierBAS8.getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.viewFichierBAS8.buildFilter(this.viewFichierBAS8.getFilterFields(),
				this.viewFichierBAS8.getHeadersDLabel()));
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
			LoggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "Problème lors de la suppression des fichiers";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		LoggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();

		// Fin du code spécifique aux bacs à sable
		this.viewPilotageBAS8.setMessage(message);

		return generateDisplay();
	}

	/**
	 * Suppression du marquage de fichier pour suppression lors de la prochaine
	 * initialisation
	 *
	 * @return
	 */
	@Action(value = "/undoActionBAS8")
	public String undoActionBAS8() {
		initialize();
		LoggerDispatcher.trace("*** Suppression du marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();
		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.viewFichierBAS8.getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.viewFichierBAS8.buildFilter(this.viewFichierBAS8.getFilterFields(),
				this.viewFichierBAS8.getHeadersDLabel()));
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
		// LoggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

		StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "null");
		try {

			UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
		} catch (SQLException e) {
			LoggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
		}
		return generateDisplay();
	}

	/**
	 * Marquage de fichier pour le rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	@Action(value = "/toRestoreBAS8")
	public String toRestoreBAS8() {
		initialize();
		LoggerDispatcher.trace("*** Marquage de fichier à rejouer ***", LOGGER);
		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();
		
		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.viewFichierBAS8.getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.viewFichierBAS8.buildFilter(this.viewFichierBAS8.getFilterFields(),
				this.viewFichierBAS8.getHeadersDLabel()));
		
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
		// LoggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

		StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "'R'");
		String message;
		try {

			UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			message = "Fichier(s) à rejouer";
		} catch (SQLException e) {
			LoggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "Problème lors de la restauration des fichiers";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		// Lancement de l'initialisation dans la foulée
		LoggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();
		// Fin du code spécifique aux bacs à sable
		this.viewPilotageBAS8.setMessage(message);

		return generateDisplay();
	}

	/**
	 * Marquage des archives à rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	@Action(value = "/toRestoreArchiveBAS8")
	public String toRestoreArchiveBAS8() {
		initialize();
		LoggerDispatcher.trace("*** Marquage de fichier à rejouer ***", LOGGER);
		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();

		// Récupération de la sélection de l'utilisateur
		StringBuilder querySelection = new StringBuilder();
		querySelection.append("select distinct container, id_source from (" + this.viewFichierBAS8.getMainQuery()
				+ ") alias_de_table ");
		querySelection.append(this.viewFichierBAS8.buildFilter(this.viewFichierBAS8.getFilterFields(),
				this.viewFichierBAS8.getHeadersDLabel()));
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

		StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "'RA'");
		String message;
		try {

			UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			message = "Archives(s) à rejouer";
		} catch (SQLException e) {
			LoggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			e.printStackTrace();
			message = "Problème lors de la restauration des fichiers";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		// Lancement de l'initialisation dans la foulée
		LoggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.INITIALISATION.getNbLigneATraiter())).invokeApi();
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), "arc.ihm",
				(String) getSession().get(SessionParameters.ENV), this.repertoire,
				String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();
		// Fin du code spécifique aux bacs à sable
		this.viewPilotageBAS8.setMessage(message);

		return generateDisplay();
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
	@Action(value = "/resetPhaseBAS8")
	public String resetPhaseBAS8() {
		initialize();
		Map<String, ArrayList<String>> selection = this.viewFichierBAS8.mapContentSelected();
		StringBuilder querySelection = this.viewFichierBAS8.queryView();

		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// choisis pour le retour arriere
		//
		if (!selection.isEmpty()) {
			querySelection.append(" AND id_source IN " + Format.sqlListe(selection.get("id_source")) + " ");
		}

		// On recupere la phase
		String phase = this.viewFichierBAS8.mapContent().get("phase_traitement").get(0);

		// Lancement du retour arrière
		ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				"arc.ihm", (String) getSession().get(SessionParameters.ENV), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			serv.retourPhasePrecedente(TraitementPhase.valueOf(phase), querySelection.toString(), null);
		} finally {
			serv.finaliser();
		}
		return generateDisplay();
	}

	public VObject getViewPilotageBAS8() {
		return this.viewPilotageBAS8;
	}

	public void setViewPilotageBAS8(VObject viewPilotageBAS8) {
		this.viewPilotageBAS8 = viewPilotageBAS8;
	}

	public VObject getViewRapportBAS8() {
		return this.viewRapportBAS8;
	}

	public void setViewRapportBAS8(VObject viewRapportBAS8) {
		this.viewRapportBAS8 = viewRapportBAS8;
	}

	public VObject getViewFichierBAS8() {
		return this.viewFichierBAS8;
	}

	public void setViewFichierBAS8(VObject viewFichierBAS8) {
		this.viewFichierBAS8 = viewFichierBAS8;
	}

	public VObject getViewEntrepotBAS8() {
		return this.viewEntrepotBAS8;
	}

	public void setViewEntrepotBAS8(VObject viewEntrepotBAS8) {
		this.viewEntrepotBAS8 = viewEntrepotBAS8;
	}

	public VObject getViewArchiveBAS8() {
		return this.viewArchiveBAS8;
	}

	public void setViewArchiveBAS8(VObject viewArchiveBAS8) {
		this.viewArchiveBAS8 = viewArchiveBAS8;
	}

	/**
	 * Mise à jour de la console dans l'objet struts2
	 */
	@Action(value = "/resetConsoleBAS8")
	public String resetConsoleBAS8() {
		initialize();
		HttpSession session = ServletActionContext.getRequest().getSession(false);
		session.setAttribute("console", "");
		return generateDisplay();
	}

	@Action(value = "/updateConsoleBAS8")
	public String updateConsoleBAS8() {
		initialize();
		HttpSession session = ServletActionContext.getRequest().getSession(false);

		if (session.getAttribute("console") == null) {
			session.setAttribute("console", "");
		}

		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("UTF-8");
		PrintWriter out;
		try {
			out = response.getWriter();
			out.write((String) session.getAttribute("console"));
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		session.setAttribute("console", "");

		return "none";

	}

	@Override
	public void putAllVObjects() {
		LoggerDispatcher.debug("putAllVObjects()", LOGGER);

		// If the sandbox changed, have to destory all table and re create later
		if (this.isRefreshMonitoring) {
			getViewArchiveBAS8().destroy();
			getViewEntrepotBAS8().destroy();
			getViewFichierBAS8().destroy();
			getViewPilotageBAS8().destroy();
			getViewRapportBAS8().destroy();
			this.isRefreshMonitoring = false;
		}

		putVObject(getViewArchiveBAS8(), t -> initializeArchiveBAS8());
		putVObject(getViewEntrepotBAS8(), t -> initializeEntrepotBAS8());
		putVObject(getViewFichierBAS8(), t -> initializeFichierBAS8());
		putVObject(getViewPilotageBAS8(), t -> initializePilotageBAS8());
		putVObject(getViewRapportBAS8(), t -> initializeRapportBAS8());

		LoggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	@Override
	public void instanciateAllDAOs() {
		LoggerDispatcher.debug("instanciateAllDAOs()", LOGGER);

		try {
			
			List<TraitementPhase> listePhaseC = new ArrayList<>();
			
			for (TraitementPhase t : TraitementPhase.values()) {
				if (t.getOrdre()>=0) {
					listePhaseC.add(t);
				}
			}
			
			this.setListePhase(listePhaseC);

			
		} catch (Exception e) {
			LoggerDispatcher.error("erreur lors de la récuparation des phases", e, LOGGER);
		}

	}

	@Override
	public void setProfilsAutorises() {
		// for know, all profile are autorised
	}

	@Override
	protected void specificTraitementsPostDAO() {
		// nothing to do here
	}

	@Override
	public String getActionName() {
		return null;
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
	
}
