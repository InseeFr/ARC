package fr.insee.arc.web.gui.pilotage.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.minio.S3Template;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PilotageDao extends VObjectHelperDao {
	
	public void initializePilotageBAS(VObject viewPilotageBAS) {

		// the most recent files processed must be shown first by default
		// set this default order
		if (viewPilotageBAS.getHeaderSortDLabels() == null) {
			viewPilotageBAS
					.setHeaderSortDLabels(new ArrayList<>(Arrays.asList(ColumnEnum.DATE_ENTREE.getColumnName())));
			viewPilotageBAS.setHeaderSortDOrders(new ArrayList<>(Arrays.asList(false)));
		}

		// filtering is only allowed on first column
		if (viewPilotageBAS.getFilterFields()!=null && viewPilotageBAS.getFilterFields().size()>1)
		{
			viewPilotageBAS.setFilterFields(viewPilotageBAS.getFilterFields().subList(0, 1));
		}

		viewPilotageBAS.setNoCount(true);
		viewPilotageBAS.setNoLimit(true);

		Map<String, String> defaultInputFields = new HashMap<>();

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT date_entree ");
		for (TraitementPhase phase : TraitementPhase.getListPhaseReportedInBas()) {
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
				this.vObjectService.buildFilter(viewPilotageBAS.getFilterFields(), viewPilotageBAS.getHeadersDLabel(), viewPilotageBAS.getHeadersDType()));
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
	
	/**
	 * dao call to get sandbox description
	 * @throws ArcException 
	 */
	public String getSandboxDescription(String bacASable) throws ArcException {
		ViewEnum dataEtatJeuderegle = ViewEnum.EXT_ETAT_JEUDEREGLE;
		// query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.ENV_DESCRIPTION, SQL.FROM, dataEtatJeuderegle.getFullName());
		query.build(SQL.WHERE, "replace(id,'.','_') = ", query.quoteText(bacASable));
		// return list
		return UtilitaireDao.get(0).getString(null, query);
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
		Map<String, String> defaultInputFields = new HashMap<>();
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
			Map<String, String> defaultInputFields = new HashMap<>();
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
		boolean existsEntrepot = UtilitaireDao.get(0).hasResults(null, FormatSQL.tableExists(dataModelEntrepotBAS.getFullName()));
		if (existsEntrepot) {
			requete.append("select id_entrepot from ");
			requete.append(dataObjectService.getView(dataModelEntrepotBAS));
		} else {
			requete.append("select ''::text as id_entrepot");
		}
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewEntrepotBAS, requete, null, defaultInputFields);

	}

	public void initializeFichierBAS(VObject viewFichierBAS, VObject viewPilotageBAS, VObject viewRapportBAS) {
		Map<String, List<String>> selectionLigne = viewPilotageBAS.mapContentSelected();
		List<String> selectionColonne = viewPilotageBAS.listHeadersSelected();
		Map<String, List<String>> selectionLigneRapport = viewRapportBAS.mapContentSelected();

		if (!selectionLigne.isEmpty() && !selectionColonne.isEmpty()) {

			Map<String, String> defaultInputFields = new HashMap<>();

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
			Map<String, String> type = viewRapportBAS.mapHeadersType();
			Map<String, String> defaultInputFields = new HashMap<>();

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

	
	/**
	 * build query to return download stream 
	 * @param response
	 * @param viewArchiveBAS
	 * @param selection
	 * @param sandboxRootDirectory
	 * @throws ArcException 
	 */
	public void execQueryDownloadArchive(HttpServletResponse response, VObject viewArchiveBAS, List<String> selection, String sandboxRootDirectory) throws ArcException {

		ArcPreparedStatementBuilder querySelection = new ArcPreparedStatementBuilder();

		querySelection.append("select distinct alias_de_table.nom_archive as nom_fichier from (");
		querySelection.append(viewArchiveBAS.getMainQuery());
		querySelection.append(") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(viewArchiveBAS.getFilterFields(),
				viewArchiveBAS.getHeadersDLabel(), viewArchiveBAS.getHeadersDType()));

		if (selection!=null) {
			querySelection.append(" AND nom_archive IN " + Format.sqlListe(selection) + " ");
		}

		List<String> listRepertoire = new ArrayList<>();

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT DISTINCT entrepot FROM (");
		requete.append(viewArchiveBAS.getMainQuery());
		requete.append(") alias_de_table ");

		GenericBean g = new GenericBean(UtilitaireDao.get(0).executeRequest(null, requete));
		String entrepot = g.mapContent().get("entrepot").get(0);
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE");

		
		this.vObjectService.downloadEnveloppe(viewArchiveBAS, response, querySelection, sandboxRootDirectory,
				listRepertoire);
		
	}
	
	public File downloadFichierBAS(VObject viewFichierBAS, String dirOut,
			String dirIn, String bacASable) throws ArcException {
		// récupération de la liste des id_source
		Map<String, List<String>> selection = viewFichierBAS.mapContentSelected();
		ArcPreparedStatementBuilder query = vObjectService.queryView(viewFichierBAS);
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionner
		//
		if (!selection.isEmpty()) {
			query.build(SQL.AND, ColumnEnum.ID_SOURCE, SQL.IN, "(", query.sqlListeOfValues(selection.get(ColumnEnum.ID_SOURCE.getColumnName())), ") ");
		}

		// optimisation pour avoir des bloc successifs sur la même archive
		query.build(SQL.ORDER_BY, ColumnEnum.CONTAINER);

		return vObjectService.downloadXML(viewFichierBAS, dirOut, query, dirIn, bacASable, TraitementPhase.RECEPTION.toString());
		
	}
	
	public ArcPreparedStatementBuilder queryUpdateToDelete(VObject viewFichierBAS, String code) {
		Map<String, List<String>> selection = viewFichierBAS.mapContentSelected();
		ArcPreparedStatementBuilder querySelection = requestSelectToMark(viewFichierBAS, selection);
		return requeteUpdateToMark(querySelection, code);
	}
	
	public void execQueryUpdateToDelete(ArcPreparedStatementBuilder updateToDelete) throws ArcException {
		UtilitaireDao.get(0).executeRequest(null, updateToDelete);
	}
	
	public File downloadBdBAS(VObject viewFichierBAS, String dirOut,
			List<String> tableDownload, TraitementPhase phase, TraitementEtat etat, String date) throws ArcException {
		
		// List of queries that will be executed to download
		List<ArcPreparedStatementBuilder> tableauRequete = new ArrayList<>();
		// Name of the file containing the data download
		List<String> fileNames = new ArrayList<>();
		
		
		ArcPreparedStatementBuilder requete;
		
		for (String t : tableDownload) {
			if (phase.equals(TraitementPhase.MAPPING))
			{
				requete = new ArcPreparedStatementBuilder();
				requete.append("WITH prep as ( ");
				requete.append("SELECT id_source FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
				requete.append("\n WHERE phase_traitement=" + requete.quoteText(phase.toString()) + " ");
				requete.append("\n AND etat_traitement=" + requete.quoteText(etat.getSqlArrayExpression()) + "::text[] ");
				requete.append("\n AND date_entree=" + requete.quoteText(date) + " ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!viewFichierBAS.mapContentSelected().isEmpty()) {
					requete.append("\n AND id_source IN (");
					requete.append(
							requete.sqlListeOfValues(viewFichierBAS.mapContentSelected().get("id_source")));
					requete.append(")");
				}
				requete.append(" ) ");
				requete.append("\n SELECT * from " + t
						+ " a where exists (select 1 from prep b where a.id_source=b.id_source) ");

				tableauRequete.add(requete);
				fileNames.add(t);
				
			}
			else
			{
				requete = new ArcPreparedStatementBuilder();
				requete.append("SELECT id_source FROM " + dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER));
				requete.append("\n WHERE phase_traitement=" + requete.quoteText(phase.toString()) + " ");
				requete.append("\n AND etat_traitement=" + requete.quoteText(etat.getSqlArrayExpression()) + "::text[] ");
				requete.append("\n AND date_entree=" + requete.quoteText(date) + " ");

				// Si des fichiers ont été selectionnés, on ajoute a la requete la liste des
				// fichiers
				if (!viewFichierBAS.mapContentSelected().isEmpty()) {
					requete.append("AND id_source IN (");
					requete.append(requete.sqlListeOfValues(viewFichierBAS.mapContentSelected().get("id_source")));
					requete.append(")");
				}

				List<String> idSources = new GenericBean(UtilitaireDao.get(0).executeRequest(null, requete))
						.mapContent().get("id_source");

				// for each files, generate the download query
				for (String idSource : idSources) {
					tableauRequete.add(new ArcPreparedStatementBuilder(
							"SELECT * FROM " + HashFileNameConversion.tableOfIdSource(t, idSource)));
					fileNames.add(t + "_" + idSource);
				}
			}
		}
		return this.vObjectService.download(viewFichierBAS, dirOut, fileNames, tableauRequete);
	}
	
	public File downloadEnvelopeBAS(VObject viewFichierBAS, String dirOut,
			String chemin, List<String> listRepertoire) {
		// récupération de la liste des noms d'enloppe
		Map<String, List<String>> selection = viewFichierBAS.mapContentSelected();
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("select distinct alias_de_table.container as nom_fichier from (");
		query.append(viewFichierBAS.getMainQuery());
		query.append(") alias_de_table ");
		query.append(vObjectService.buildFilter(viewFichierBAS.getFilterFields(), viewFichierBAS.getHeadersDLabel(), viewFichierBAS.getHeadersDType()));
		if (!selection.isEmpty()) {
			query.append(" AND container IN (" + query.sqlListeOfValues(selection.get("container")) + ") ");
		}
		return vObjectService.downloadEnveloppe(viewFichierBAS, dirOut, query, chemin, listRepertoire);
	}
	
	
	
	/**
	 * copy the downloaded file to s3 bucket /bas/download/
	 * @param fOut
	 * @param bacASable
	 * @param s3
	 * @throws ArcException
	 * @throws IOException
	 */
	public void copyDownloadedFileToS3(File fOut, String bacASable, S3Template s3) throws ArcException, IOException {
	
		// s3 operations
		String s3PathOut = bacASable.toUpperCase() + File.separator + "DOWNLOAD/";
		s3.createDirectory(s3PathOut);
		s3.upload(fOut, s3PathOut + fOut.getName());
		
		// delete file on filesystem
		Files.delete(fOut.toPath());
		
	}

	/**
	 * Prepare a request selecting the line to change when marking files for
	 * deletion/replay.
	 */
	private ArcPreparedStatementBuilder requestSelectToMark(VObject viewFichierBAS, Map<String, List<String>> selection) {
		ArcPreparedStatementBuilder querySelection = new ArcPreparedStatementBuilder();
		querySelection.append("select distinct container, id_source from (");
		querySelection.append(viewFichierBAS.getMainQuery());
		querySelection.append(") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(viewFichierBAS.getFilterFields(),
				viewFichierBAS.getHeadersDLabel(), viewFichierBAS.getHeadersDType()));
		// si la selection de fichiers n'est pas vide, on se restreint aux fichiers
		// sélectionné
		if (!selection.isEmpty()) {
			// concaténation des informations
			List<String> infoConcatenee = new ArrayList<>();
			List<String> listContainer = selection.get("container");
			List<String> listIdSource = selection.get("id_source");

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
		updateToDelete.append("SET to_delete=" + updateToDelete.quoteText(value) + " ");
		updateToDelete.append(
				"WHERE EXISTS (SELECT 1 FROM prep WHERE a.container=prep.container AND a.id_source=prep.id_source); ");
		return updateToDelete;
	}
	
	public void execQueryUpdateEnvDescription(VObject viewPilotageBAS, String envDescription, String bacASable) throws ArcException {
		ArcPreparedStatementBuilder envQuery = new ArcPreparedStatementBuilder();
		envQuery.append("update arc.ext_etat_jeuderegle set env_description = ");
		envQuery.append(envQuery.quoteText(viewPilotageBAS.getCustomValue(envDescription)));
		envQuery.append("where replace(id,'.','_') = ");
		envQuery.append(envQuery.quoteText(bacASable));
		UtilitaireDao.get(0).executeRequest(null, envQuery);
	}
	
	public String execQueryTime() throws ArcException {
		return UtilitaireDao.get(0).getString(null, new ArcPreparedStatementBuilder("SELECT last_init FROM "+dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)));
	}

	public String execQueryState() throws ArcException {
		return UtilitaireDao.get(0).getString(null, new ArcPreparedStatementBuilder("SELECT case when operation='O' then 'active' else 'inactive' end FROM "+dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)));
	}
	
	public void execQueryDelayBatchTime() throws ArcException {
		UtilitaireDao.get(0).executeRequest(null, 
				new ArcPreparedStatementBuilder("UPDATE "+dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)+" set last_init=to_char(current_date + interval '7 days','yyyy-mm-dd')||':22';"));
	}
	
	public void execQueryForwardBatchTime() throws ArcException {
		UtilitaireDao.get(0).executeRequest(null,
				new ArcPreparedStatementBuilder("UPDATE "+dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)+" set last_init=to_char(current_date-interval '1 days','yyyy-mm-dd')||':22';"));
	}
	
	public void execQueryToggleOn() throws ArcException {
		UtilitaireDao.get(0).executeRequest(null, new ArcPreparedStatementBuilder("UPDATE "+dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)+" set operation='O'; "));
	}
	
	public void execQueryToggleOff() throws ArcException {
		UtilitaireDao.get(0).executeRequest(null, new ArcPreparedStatementBuilder("UPDATE "+dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)+" set operation='N'; "));
	}
	
	
	public VObjectService getvObjectService() {
		return vObjectService;
	}

	public void setvObjectService(VObjectService vObjectService) {
		this.vObjectService = vObjectService;
	}

	public DataObjectService getDataObjectService() {
		return dataObjectService;
	}

	public void setDataObjectService(DataObjectService dataObjectService) {
		this.dataObjectService = dataObjectService;
	}

}
