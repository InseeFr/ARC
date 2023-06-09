package fr.insee.arc.web.gui.pilotage.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.pilotage.dao.PilotageDao;
import fr.insee.arc.web.gui.pilotage.model.ModelPilotage;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.LineObject;
import fr.insee.arc.web.util.VObject;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorPilotage extends ArcWebGenericService<ModelPilotage> {

	protected static final String ENV_DESCRIPTION = "envDescription";

	protected static final String ENTRY_DATE = "date_entree";

	private static final String ACTION_NAME = "EnvManagement";

	protected static final String RESULT_SUCCESS = "jsp/gererPilotageBAS.jsp";

	private static final Logger LOGGER = LogManager.getLogger(InteractorPilotage.class);

	@Autowired
	protected MessageSource messageSource;

	private PilotageDao dao;

	/**
	 * Liste des phase pour générer les boutons d'actions executer et retour arriere
	 * sur chaque phase.
	 */
	private List<TraitementPhase> listePhase;

	@Autowired
	protected ModelPilotage views;

	public InteractorPilotage() {
		this.setListePhase(TraitementPhase.getListPhaseC());
	}

	@ModelAttribute
	public void specificModelAttributes(Model model) {
		model.addAttribute("listePhase", listePhase);
	}

	@Override
	public void putAllVObjects(ModelPilotage arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		dao = new PilotageDao(vObjectService, dataObjectService);

		views.setViewPilotageBAS(vObjectService.preInitialize(arcModel.getViewPilotageBAS()));
		views.setViewRapportBAS(vObjectService.preInitialize(arcModel.getViewRapportBAS()));
		views.setViewArchiveBAS(vObjectService.preInitialize(arcModel.getViewArchiveBAS()));
		views.setViewEntrepotBAS(vObjectService.preInitialize(arcModel.getViewEntrepotBAS()));
		views.setViewFichierBAS(vObjectService.preInitialize(arcModel.getViewFichierBAS()));

		// If the sandbox changed, have to destroy all table and re create later
		if (this.isRefreshMonitoring) {
			vObjectService.destroy(views.getViewPilotageBAS());
			vObjectService.destroy(views.getViewRapportBAS());
			vObjectService.destroy(views.getViewArchiveBAS());
			vObjectService.destroy(views.getViewEntrepotBAS());
			vObjectService.destroy(views.getViewFichierBAS());
			this.isRefreshMonitoring = false;
		}

		putVObject(views.getViewPilotageBAS(), t -> initializePilotageBAS(t));
		putVObject(views.getViewRapportBAS(), t -> initializeRapportBAS());
		putVObject(views.getViewArchiveBAS(), t -> initializeArchiveBAS());
		putVObject(views.getViewEntrepotBAS(), t -> initializeEntrepotBAS());
		putVObject(views.getViewFichierBAS(), t -> initializeFichierBAS());

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	// visual des Pilotages du bac à sable
	public void initializePilotageBAS(VObject viewPilotageBAS) {
		LoggerHelper.debug(LOGGER, "* initializePilotageBAS *");

		dao.initializePilotageBAS(viewPilotageBAS);

		ArrayList<String> columns = viewPilotageBAS.getHeadersDLabel();
		Map<String, ColumnRendering> columnRendering = viewPilotageBAS.getConstantVObject().columnRender;

		// for all columns, set rendering visibility to false
		for (int i = 1; i < columns.size(); i++) {
			ColumnRendering renderAttributes = new ColumnRendering(false, ManipString.translateAscii(columns.get(i)),
					null, "text", null, false);
			columnRendering.put(columns.get(i), renderAttributes);
		}

		// now display the columns only which have positive values

		for (LineObject l : viewPilotageBAS.getContent()) {
			for (int i = 1; i < columns.size(); i++) {
				if (!l.getD().get(i).equals("0")) {
					columnRendering.get(columns.get(i)).visible = true;
				}
			}
		}

		this.vObjectService.initialiserColumnRendering(viewPilotageBAS, columnRendering);
		this.vObjectService.applyColumnRendering(viewPilotageBAS, columns);

		// display comment for the sandbox
		ArcPreparedStatementBuilder envQuery = new ArcPreparedStatementBuilder();
		envQuery.append("select env_description from arc.ext_etat_jeuderegle where replace(id,'.','_') = ");
		envQuery.append(envQuery.quoteText(getBacASable()));

		try {
			String envDescription = UtilitaireDao.get(0).getString(null, envQuery);
			viewPilotageBAS.setCustomValue(ENV_DESCRIPTION, envDescription);
		} catch (ArcException e) {
			loggerDispatcher.error("Error in initializePilotageBAS", e, LOGGER);
		}
	}

	// visual des Pilotages du bac à sable
	public void initializeRapportBAS() {
		LoggerHelper.debug(LOGGER, "* initializeRapportBAS *");
		HashMap<String, String> defaultInputFields = new HashMap<>();

		if (views.getViewRapportBAS().getHeaderSortDLabels() == null) {
			views.getViewRapportBAS().setHeaderSortDLabels(new ArrayList<>(Arrays.asList(ENTRY_DATE)));
			views.getViewRapportBAS().setHeaderSortDOrders(new ArrayList<>(Arrays.asList(false)));
		}

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(
				"select date_entree, phase_traitement, array_to_string(etat_traitement,'$') as etat_traitement, rapport, count(1) as nb ");
		requete.append("from " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
		requete.append(" where rapport is not null ");
		requete.append("group by date_entree, phase_traitement, etat_traitement, rapport ");
		requete.append("order by date_entree asc ");
		this.vObjectService.initialize(views.getViewRapportBAS(), requete, null, defaultInputFields);
	}

	/**
	 * Initialisation de la vue sur la table contenant la liste des fichiers du
	 * répertoire d'archive
	 */
	public void initializeArchiveBAS() {
		LoggerHelper.debug(LOGGER, "* /* initializeArchiveBAS  */ *");
		String entrepotLecture = this.views.getViewEntrepotBAS().getCustomValue("entrepotLecture");
		if (entrepotLecture != null && !entrepotLecture.equals("")) {
			HashMap<String, String> defaultInputFields = new HashMap<>();

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append("select * from " + dataObjectService.getView(ViewEnum.PILOTAGE_ARCHIVE) + " where entrepot="
					+ requete.quoteText(entrepotLecture) + " ");

			this.vObjectService.initialize(views.getViewArchiveBAS(), requete, null, defaultInputFields);
		} else {

			this.vObjectService.destroy(views.getViewArchiveBAS());
		}
	}

	public void initializeEntrepotBAS() {
		LoggerHelper.debug(LOGGER, "* initializeEntrepotBAS *");

		HashMap<String, String> defaultInputFields = new HashMap<>();
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		try {
			if (Boolean.TRUE
					.equals(UtilitaireDao.get(0).hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot")))) {
				requete.append("select id_entrepot from arc.ihm_entrepot");
			} else {
				requete.append("select ''::text as id_entrepot");
			}
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, "error when initialize repository", e);
		}

		this.vObjectService.initialize(this.views.getViewEntrepotBAS(), requete, null, defaultInputFields);
	}

	// visual des Fichiers
	public void initializeFichierBAS() {
		LoggerHelper.debug(LOGGER, "initializeFichierBAS");

		Map<String, ArrayList<String>> selectionLigne = views.getViewPilotageBAS().mapContentSelected();
		ArrayList<String> selectionColonne = views.getViewPilotageBAS().listHeadersSelected();
		Map<String, ArrayList<String>> selectionLigneRapport = views.getViewRapportBAS().mapContentSelected();

		if (!selectionLigne.isEmpty() && !selectionColonne.isEmpty()) {

			HashMap<String, String> defaultInputFields = new HashMap<>();

			String phase = ManipString.substringBeforeLast(selectionColonne.get(0), "_").toUpperCase();
			String etat = ManipString.substringAfterLast(selectionColonne.get(0), "_").toUpperCase();

			// get the file with the selected date_entree, state, and phase_tratement
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append(
					"SELECT container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
			requete.append(" FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " ");
			requete.append(" WHERE date_entree" + requete.sqlEqual(selectionLigne.get(ENTRY_DATE).get(0), "text"));
			requete.append(" AND array_to_string(etat_traitement,'$')" + requete.sqlEqual(etat, "text"));
			requete.append(" AND phase_traitement" + requete.sqlEqual(phase, "text"));

			this.vObjectService.initialize(views.getViewFichierBAS(), requete, null, defaultInputFields);

		} else if (!selectionLigneRapport.isEmpty()) {
			HashMap<String, String> type = views.getViewRapportBAS().mapHeadersType();
			HashMap<String, String> defaultInputFields = new HashMap<>();

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append(
					"SELECT container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
			requete.append(" FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " ");
			requete.append(
					" WHERE date_entree" + requete.sqlEqual(selectionLigneRapport.get(ENTRY_DATE).get(0), "text"));
			requete.append(" AND array_to_string(etat_traitement,'$')" + requete
					.sqlEqual(selectionLigneRapport.get("etat_traitement").get(0), type.get("etat_traitement")));
			requete.append(" AND phase_traitement" + requete
					.sqlEqual(selectionLigneRapport.get("phase_traitement").get(0), type.get("phase_traitement")));
			requete.append(" AND rapport"
					+ requete.sqlEqual(selectionLigneRapport.get("rapport").get(0), type.get("rapport")));

			this.vObjectService.initialize(views.getViewFichierBAS(), requete, null, defaultInputFields);

		} else {
			this.vObjectService.destroy(views.getViewFichierBAS());
		}
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

}
