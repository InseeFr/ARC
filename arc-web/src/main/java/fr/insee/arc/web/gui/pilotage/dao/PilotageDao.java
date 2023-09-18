package fr.insee.arc.web.gui.pilotage.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

public class PilotageDao extends VObjectHelperDao {

	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public PilotageDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}

	public void initializePilotageBAS(VObject viewPilotageBAS) {

		// the most recent files processed must be shown first by default
		// set this default order
		if (viewPilotageBAS.getHeaderSortDLabels() == null) {
			viewPilotageBAS
					.setHeaderSortDLabels(new ArrayList<>(Arrays.asList(ColumnEnum.DATE_ENTREE.getColumnName())));
			viewPilotageBAS.setHeaderSortDOrders(new ArrayList<>(Arrays.asList(false)));
		}

		viewPilotageBAS.setNoCount(true);
		viewPilotageBAS.setNoLimit(true);

		HashMap<String, String> defaultInputFields = new HashMap<>();

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT date_entree ");
		for (TraitementPhase phase : TraitementPhase.listPhasesAfterPhase(TraitementPhase.RECEPTION)) {
			for (TraitementEtat etat : new ArrayList<>(Arrays.asList(TraitementEtat.valuesByOrdreAffichage()))) {
				String columnName = phase.toString().toLowerCase() + "_" + etat.toString().toLowerCase();
				requete.append("\n, max(CASE WHEN phase_traitement='" + phase + "' and etat_traitement='"
						+ etat.getSqlArrayExpression() + "' THEN n ELSE 0 END) as " + columnName + " ");
			}

		}
		requete.append("\n FROM (");
		requete.append("\n SELECT date_entree, phase_traitement, etat_traitement, count(*) as n ");
		requete.append("\n FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " b ");
		requete.append("\n WHERE date_entree IN ( ");
		requete.append(
				"\n SELECT DISTINCT date_entree FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " a ");
		requete.append(
				this.vObjectService.buildFilter(viewPilotageBAS.getFilterFields(), viewPilotageBAS.getHeadersDLabel()));
		requete.append("\n AND phase_traitement='" + TraitementPhase.RECEPTION + "' ");
		requete.append(this.vObjectService.buildOrderBy(viewPilotageBAS.getHeaderSortDLabels(),
				viewPilotageBAS.getHeaderSortDOrders()));
		requete.append(this.vObjectService.buildLimit(viewPilotageBAS,
				this.vObjectService.pageManagement(null, viewPilotageBAS)));
		requete.append("\n ) ");
		requete.append("\n GROUP BY date_entree, phase_traitement, etat_traitement ");
		requete.append(") ttt ");
		requete.append("group by date_entree ");

		this.vObjectService.initialize(viewPilotageBAS, requete, null, defaultInputFields);

	}

	public void initializeRapportBAS(VObject viewRapportBAS) {
		ViewEnum dataModelRapportBAS = ViewEnum.PILOTAGE_FICHIER;
		// view query
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(
				"select date_entree, phase_traitement, array_to_string(etat_traitement,'$') as etat_traitement, rapport, count(1) as nb ");
		requete.append("from " + dataObjectService.getView(dataModelRapportBAS));
		requete.append(" where rapport is not null ");
		requete.append("group by date_entree, phase_traitement, etat_traitement, rapport ");
		requete.append("order by date_entree asc ");
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewRapportBAS, requete, null, defaultInputFields);
	}

	public void initializeArchiveBAS(VObject viewArchiveBAS, VObject viewEntrepotBAS) {
		String entrepotLecture = viewEntrepotBAS.getCustomValue("entrepotLecture");
		if (entrepotLecture != null && !entrepotLecture.equals("")) {
			ViewEnum dataModelArchiveBAS = ViewEnum.PILOTAGE_ARCHIVE;
			// view query
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append("select * from " + dataObjectService.getView(dataModelArchiveBAS) + " where entrepot="
					+ requete.quoteText(entrepotLecture) + " ");
			// default value
			HashMap<String, String> defaultInputFields = new HashMap<>();
			// initialize vobject
			vObjectService.initialize(viewArchiveBAS, requete, null, defaultInputFields);
		} else {
			vObjectService.destroy(viewArchiveBAS);
		}
	}

	public void initializeEntrepotBAS(VObject viewEntrepotBAS) throws ArcException {
		ViewEnum dataModelEntrepotBAS = ViewEnum.IHM_ENTREPOT;
		// view query
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		if (Boolean.TRUE.equals(UtilitaireDao.get(0).hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot")))) {
			requete.append("select id_entrepot from ");
			requete.append(dataObjectService.getView(dataModelEntrepotBAS));
		} else {
			requete.append("select ''::text as id_entrepot");
		}
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewEntrepotBAS, requete, null, defaultInputFields);

	}

	public void initializeFichierBAS(VObject viewFichierBAS, VObject viewPilotageBAS, VObject viewRapportBAS) {
		Map<String, ArrayList<String>> selectionLigne = viewPilotageBAS.mapContentSelected();
		ArrayList<String> selectionColonne = viewPilotageBAS.listHeadersSelected();
		Map<String, ArrayList<String>> selectionLigneRapport = viewRapportBAS.mapContentSelected();

		if (!selectionLigne.isEmpty() && !selectionColonne.isEmpty()) {

			HashMap<String, String> defaultInputFields = new HashMap<>();

			String phase = ManipString.substringBeforeLast(selectionColonne.get(0), "_").toUpperCase();
			String etat = ManipString.substringAfterLast(selectionColonne.get(0), "_").toUpperCase();

			// get the file with the selected date_entree, state, and phase_tratement
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append(
					"SELECT container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
			requete.append(" FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " ");
			requete.append(" WHERE date_entree" + requete.sqlEqual(selectionLigne.get("date_entree").get(0), TypeEnum.TEXT.getTypeName()));
			requete.append(" AND array_to_string(etat_traitement,'$')" + requete.sqlEqual(etat, TypeEnum.TEXT.getTypeName()));
			requete.append(" AND phase_traitement" + requete.sqlEqual(phase, TypeEnum.TEXT.getTypeName()));

			this.vObjectService.initialize(viewFichierBAS, requete, null, defaultInputFields);

		} else if (!selectionLigneRapport.isEmpty()) {
			HashMap<String, String> type = viewRapportBAS.mapHeadersType();
			HashMap<String, String> defaultInputFields = new HashMap<>();

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append(
					"SELECT container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure ");
			requete.append(" FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER) + " ");
			requete.append(
					" WHERE date_entree" + requete.sqlEqual(selectionLigneRapport.get("date_entree").get(0), TypeEnum.TEXT.getTypeName()));
			requete.append(" AND array_to_string(etat_traitement,'$')" + requete
					.sqlEqual(selectionLigneRapport.get("etat_traitement").get(0), type.get("etat_traitement")));
			requete.append(" AND phase_traitement" + requete
					.sqlEqual(selectionLigneRapport.get("phase_traitement").get(0), type.get("phase_traitement")));
			requete.append(" AND rapport"
					+ requete.sqlEqual(selectionLigneRapport.get("rapport").get(0), type.get("rapport")));

			vObjectService.initialize(viewFichierBAS, requete, null, defaultInputFields);

		} else {
			vObjectService.destroy(viewFichierBAS);
		}
	}

}
