package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
import fr.insee.arc.core.service.global.dao.PhaseOperations;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

@Service
public class ServiceViewFichierBAS extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewFichierBAS.class);

	public String selectFichierBAS(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortFichierBAS(Model model) {

		this.vObjectService.sort(views.getViewFichierBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

	public void downloadFichierBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des fichiers ***", LOGGER);
		// récupération de la liste des id_source

		Map<String, ArrayList<String>> selection = views.getViewFichierBAS().mapContentSelected();
		ArcPreparedStatementBuilder querySelection = this.vObjectService.queryView(views.getViewFichierBAS());
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionner
		//
		if (!selection.isEmpty()) {
			querySelection
					.append(" AND id_source IN (" + querySelection.sqlListeOfValues(selection.get("id_source")) + ") ");
		}

		// optimisation pour avoir des bloc successifs sur la même archive
		querySelection.append(" ORDER by container ");

		this.vObjectService.downloadXML(views.getViewFichierBAS(), response, querySelection, this.repertoire,
				getBacASable(), TraitementPhase.RECEPTION.toString(), TraitementEtat.OK.toString(),
				TraitementEtat.KO.toString());

		loggerDispatcher.trace("*** Fin du téléchargement des fichiers XML ***", LOGGER);
	}

	/**
	 * Marquage de fichier pour le rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	public String toRestoreBAS(Model model) {
		return restore(model, "'R'", "managementSandbox.batch.replay.files");
	}

	/**
	 * Marquage des archives à rejouer lors de la prochaine initialisation
	 *
	 * @return
	 */
	public String toRestoreArchiveBAS(Model model) {
		return restore(model, "'RA'", "managementSandbox.batch.replay.archives");
	}

	private String restore(Model model, String code, String messageOk) {
		loggerDispatcher.trace("*** Marquage de fichier à rejouer ***", LOGGER);
		Map<String, ArrayList<String>> selection = views.getViewFichierBAS().mapContentSelected();

		ArcPreparedStatementBuilder querySelection = requestSelectToMark(selection);

		ArcPreparedStatementBuilder updateToDelete = requeteUpdateToMark(querySelection, code);
		String message;
		try {

			UtilitaireDao.get(0).executeRequest(null, updateToDelete);
			message = messageOk;
		} catch (ArcException e) {
			loggerDispatcher.error("Error in PilotageBASAction.restore", LOGGER);
			message = "managementSandbox.batch.replay.error";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		if (!isEnvProd()) {
			// Lancement de l'initialisation dans la foulée
			loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
			ApiServiceFactory
					.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, getBacASable(),
							this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null)
					.invokeApi();
			ApiServiceFactory
					.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, getBacASable(),
							this.repertoire, TraitementPhase.RECEPTION.getNbLigneATraiter(), null)
					.invokeApi();
		}
		this.views.getViewPilotageBAS().setMessage(message);

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public void downloadBdBAS(HttpServletResponse response) throws ArcException {
		Map<String, ArrayList<String>> selectionLigne = views.getViewPilotageBAS().mapContentSelected();
		ArrayList<String> selectionColonne = views.getViewPilotageBAS().listHeadersSelected();
		ArrayList<Integer> selectionIndexColonne = views.getViewPilotageBAS().indexHeadersSelected();

		String phase = TraitementPhase.getPhase(selectionIndexColonne.get(0)).toString();
		String etat = selectionColonne.get(0).split("_")[1].toUpperCase();
		String date = selectionLigne.get(ENTRY_DATE).get(0);

		String[] etatList = etat.split("\\$");
		String etatBdd = "{" + etat.replace("$", ",") + "}";

		// Sélection des table métiers en fonction de la phase sélectionner (5 pour
		// mapping 1 sinon)
		ArrayList<String> tableDownload = new ArrayList<>();
		try {
			
			List<String> dataTables = PhaseOperations.selectPhaseDataTablesFoundInEnv(null, getBacASable());

			if (!dataTables.isEmpty()) {
				for (String table : dataTables) {
					// selection des tables qui contiennent la phase dans leur nom
					for (int i = 0; i < etatList.length; i++) {
						if (ManipString.substringAfterFirst(table.toUpperCase(), ".")
								.startsWith(phase.toUpperCase() + "_")
								&& table.toUpperCase().endsWith("_" + etatList[i].toUpperCase())
								&& !tableDownload.contains(table)) {
							tableDownload.add(table);

						}
					}
				}
			}
		} catch (ArcException e) {
			loggerDispatcher.error(e, LOGGER);
		}

		// List of queries that will be executed to download
		List<ArcPreparedStatementBuilder> tableauRequete = new ArrayList<>();
		// Name of the file containing the data download
		List<String> fileNames = new ArrayList<>();

		ArcPreparedStatementBuilder requete;

		for (String t : tableDownload) {

			// Check if the table to download got children
			requete = new ArcPreparedStatementBuilder();
			requete.append(FormatSQL.getAllInheritedTables(ManipString.substringBeforeFirst(t, "."),
					ManipString.substringAfterFirst(t, ".")));
			requete.append(" LIMIT 1");

			if (Boolean.TRUE.equals(UtilitaireDao.get(0).hasResults(null, requete))) {

				// Get the files to download
				requete = new ArcPreparedStatementBuilder();
				requete.append("SELECT id_source FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
				requete.append("\n WHERE phase_traitement=" + requete.quoteText(phase) + " ");
				requete.append("\n AND etat_traitement=" + requete.quoteText(etatBdd) + "::text[] ");
				requete.append("\n AND date_entree=" + requete.quoteText(date) + " ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!views.getViewFichierBAS().mapContentSelected().isEmpty()) {
					ArrayList<String> filesSelected = views.getViewFichierBAS().mapContentSelected().get("id_source");
					requete.append("AND id_source IN (");
					for (int i = 0; i < filesSelected.size(); i++) {
						if (i > 0) {
							requete.append(",");
						}
						requete.append("'" + filesSelected.get(i) + "'");
					}
					requete.append(")");
				}

				ArrayList<String> idSources = new GenericBean(UtilitaireDao.get(0).executeRequest(null, requete))
						.mapContent().get("id_source");

				// for each files, generate the download query
				for (String idSource : idSources) {
					tableauRequete.add(new ArcPreparedStatementBuilder(
							"SELECT * FROM " + HashFileNameConversion.tableOfIdSource(t, idSource)));
					fileNames.add(t + "_" + idSource);
				}

			}
			// if no children
			else {

				requete = new ArcPreparedStatementBuilder();
				requete.append("WITH prep as ( ");
				requete.append("SELECT id_source FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
				requete.append("\n WHERE phase_traitement=" + requete.quoteText(phase) + " ");
				requete.append("\n AND etat_traitement=" + requete.quoteText(etatBdd) + "::text[] ");
				requete.append("\n AND date_entree=" + requete.quoteText(date) + " ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!views.getViewFichierBAS().mapContentSelected().isEmpty()) {
					requete.append("\n AND id_source IN (");
					requete.append(
							requete.sqlListeOfValues(views.getViewFichierBAS().mapContentSelected().get("id_source")));
					requete.append(")");
				}
				requete.append(" ) ");
				requete.append("\n SELECT * from " + t
						+ " a where exists (select 1 from prep b where a.id_source=b.id_source) ");
				tableauRequete.add(requete);
				fileNames.add(t);
			}

		}

		this.vObjectService.download(views.getViewFichierBAS(), response, fileNames, tableauRequete);
	}

	public String downloadEnveloppeBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des enveloppes ***", LOGGER);
		// récupération de la liste des noms d'enloppe
		Map<String, ArrayList<String>> selection = views.getViewFichierBAS().mapContentSelected();

		initializeFichierBAS(views.getViewFichierBAS(), views.getViewPilotageBAS(), views.getViewRapportBAS());

		ArcPreparedStatementBuilder querySelection = new ArcPreparedStatementBuilder();

		querySelection.append("select distinct alias_de_table.container as nom_fichier from (");
		querySelection.append(this.views.getViewFichierBAS().getMainQuery());
		querySelection.append(") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.views.getViewFichierBAS().getFilterFields(),
				this.views.getViewFichierBAS().getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection
					.append(" AND container IN (" + querySelection.sqlListeOfValues(selection.get("container")) + ") ");
		}

		loggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(),
				LOGGER);

		ArrayList<String> listRepertoire = new ArrayList<>();
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.OK);
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + TraitementEtat.KO);
		String chemin = Paths.get(this.repertoire, getBacASable().toUpperCase()).toString();
		this.vObjectService.downloadEnveloppe(views.getViewFichierBAS(), response, querySelection, chemin,
				listRepertoire);
		loggerDispatcher.trace("*** Fin du téléchargement des enveloppes ***", LOGGER);

		return "none";
	}

	/**
	 * Marquage de fichier pour suppression lors de la prochaine initialisation
	 *
	 * @return
	 */
	public String toDeleteBAS(Model model) {

		loggerDispatcher.trace("*** Marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = views.getViewFichierBAS().mapContentSelected();

		ArcPreparedStatementBuilder querySelection = requestSelectToMark(selection);

		ArcPreparedStatementBuilder updateToDelete = requeteUpdateToMark(querySelection, "'1'");
		String message;
		try {
			UtilitaireDao.get(0).executeRequest(null, updateToDelete);
			message = "managementSandbox.batch.delete.ok";
		} catch (ArcException e) {
			loggerDispatcher.error("Error in PilotageBASAction.toDeleteBAS", LOGGER);
			message = "managementSandbox.batch.delete.error";
		}

		// Attention bout de code spécifique aux bacs à sable, ne surtout pas copier en
		// production
		if (!isEnvProd()) {
			loggerDispatcher.info("Synchronisation de l'environnement  ", LOGGER);
			ApiServiceFactory
					.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, getBacASable(),
							this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter(), null)
					.invokeApi();
		}

		this.views.getViewPilotageBAS().setMessage(message);

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Suppression du marquage de fichier pour suppression lors de la prochaine
	 * initialisation
	 *
	 * @return
	 */
	public String undoActionBAS(Model model) {

		loggerDispatcher.trace("*** Suppression du marquage de fichier à supprimer ***", LOGGER);
		Map<String, ArrayList<String>> selection = views.getViewFichierBAS().mapContentSelected();
		ArcPreparedStatementBuilder querySelection = requestSelectToMark(selection);
		ArcPreparedStatementBuilder updateToDelete = requeteUpdateToMark(querySelection, "null");
		try {

			UtilitaireDao.get(0).executeRequest(null, updateToDelete);
		} catch (ArcException e) {
			loggerDispatcher
					.info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  "
							+ updateToDelete, LOGGER);
			loggerDispatcher.error("Error in PilotageBASAction.undoActionBAS", LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Prepare a request selecting the line to change when marking files for
	 * deletion/replay.
	 */
	private ArcPreparedStatementBuilder requestSelectToMark(Map<String, ArrayList<String>> selection) {
		ArcPreparedStatementBuilder querySelection = new ArcPreparedStatementBuilder();
		querySelection.append("select distinct container, id_source from (");
		querySelection.append(this.views.getViewFichierBAS().getMainQuery());
		querySelection.append(") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.views.getViewFichierBAS().getFilterFields(),
				this.views.getViewFichierBAS().getHeadersDLabel()));
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

	/**
	 * Mark the line selected by querySelection for deletion/replay (depending on
	 * value).
	 */
	private ArcPreparedStatementBuilder requeteUpdateToMark(ArcPreparedStatementBuilder querySelection, String value) {
		ArcPreparedStatementBuilder updateToDelete = new ArcPreparedStatementBuilder();
		updateToDelete.append("WITH ");
		updateToDelete.append("prep AS ( ");
		updateToDelete.append(querySelection);
		updateToDelete.append("         ) ");
		updateToDelete.append("UPDATE " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " a ");
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
	public String resetPhaseBAS(Model model) {
		Map<String, ArrayList<String>> selection = views.getViewFichierBAS().mapContentSelected();
		ArcPreparedStatementBuilder querySelection = this.vObjectService.queryView(views.getViewFichierBAS());

		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// choisis pour le retour arriere
		//
		if (!selection.isEmpty()) {
			querySelection
					.append(" AND id_source IN " + querySelection.sqlListeOfValues(selection.get("id_source")) + " ");
		}

		// On recupere la phase
		String phase = views.getViewFichierBAS().mapContent().get("phase_traitement").get(0);

		// Lancement du retour arrière
		ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				ApiService.IHM_SCHEMA, getBacASable(), this.repertoire,
				TraitementPhase.INITIALISATION.getNbLigneATraiter(), null);
		try {
			serv.retourPhasePrecedente(TraitementPhase.valueOf(phase), querySelection, null);
		} finally {
			serv.finaliser();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
