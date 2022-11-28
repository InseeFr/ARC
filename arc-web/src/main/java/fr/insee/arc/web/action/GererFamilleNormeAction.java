package fr.insee.arc.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.serviceinteractif.ddi.DDIModeler;
import fr.insee.arc.core.serviceinteractif.ddi.DDIParser;
import fr.insee.arc.core.serviceinteractif.ddi.dao.DDIInsertDAO;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.model.FamilyManagementModel;
import fr.insee.arc.web.model.viewobjects.ViewVariableMetier;
import fr.insee.arc.web.service.ArcWebGenericService;
import fr.insee.arc.web.util.ArcStringUtils;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererFamilleNormeAction extends ArcWebGenericService<FamilyManagementModel> {

	private static final String MODEL_VARIABLE_NAME = "nom_variable_metier";

	private static final String NOM_TABLE_METIER = "nom_table_metier";

	private static final String ID_FAMILLE = "id_famille";

	private static final String ID_APPLICATION = "id_application";

	private static final String RESULT_SUCCESS = "jsp/gererFamilleNorme.jsp";

	private static final String IHM_MOD_VARIABLE_METIER = "ihm_mod_variable_metier";

	private static final Logger LOGGER = LogManager.getLogger(GererFamilleNormeAction.class);
	private static final int numberOfColumnTableVariableMetier = 5;

	private VObject viewFamilleNorme;

	private VObject viewClient;

	private VObject viewHostAllowed;

	private VObject viewTableMetier;

	private VObject viewVariableMetier;

	@Override
	public String getActionName() {
		return "familyManagement";
	}

	@Override
	public void putAllVObjects(FamilyManagementModel arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		setViewClient(vObjectService.preInitialize(arcModel.getViewClient()));
		setViewFamilleNorme(vObjectService.preInitialize(arcModel.getViewFamilleNorme()));
		setViewTableMetier(vObjectService.preInitialize(arcModel.getViewTableMetier()));
		setViewHostAllowed(vObjectService.preInitialize(arcModel.getViewHostAllowed()));

		setViewVariableMetier(vObjectService.preInitialize(arcModel.getViewVariableMetier()));

		putVObject(getViewFamilleNorme(), t -> initializeFamilleNorme());
		putVObject(getViewClient(), t -> initializeClient());
		putVObject(getViewTableMetier(), t -> initializeTableMetier());
		putVObject(getViewHostAllowed(), t -> initializeHostAllowed());
		putVObject(getViewVariableMetier(), t -> initializeVariableMetier());

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	/*
	 * FAMILLES DE NORMES
	 */
	private void initializeFamilleNorme() {
		System.out.println("/* initializeFamilleNorme */");
		HashMap<String, String> defaultInputFields = new HashMap<String, String>();
		this.vObjectService.initialize(viewFamilleNorme,
				new PreparedStatementBuilder(
						"select " + ID_FAMILLE + " from arc.ihm_famille order by " + ID_FAMILLE + ""),
				"arc.ihm_famille", defaultInputFields);
	}

	@RequestMapping("/selectFamilleNorme")
	public String selectFamilleNorme(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/addFamilleNorme")
	public String addFamilleNorme(Model model) {
		// Clean up spaces
		String nomFamilleNorme = viewFamilleNorme.getInputFieldFor(ID_FAMILLE);
		viewFamilleNorme.setInputFieldFor(ID_FAMILLE, nomFamilleNorme.trim());
		return addLineVobject(model, RESULT_SUCCESS, getViewFamilleNorme());
	}

	@RequestMapping("/deleteFamilleNorme")
	public String deleteFamilleNorme(Model model) {

		PreparedStatementBuilder query = new PreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(viewFamilleNorme));
		query.append(synchronizeRegleWithVariableMetier(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0)));
		query.asTransaction();

		try {
			UtilitaireDao.get("arc").executeRequest(null, query);
		} catch (ArcException e) {
			this.viewFamilleNorme.setMessage("La suppression des tables a échoué");
		}

		return deleteLineVobject(model, RESULT_SUCCESS, getViewFamilleNorme());
	}

	@RequestMapping("/updateFamilleNorme")
	public String updateFamilleNorme(Model model) {
		return updateVobject(model, RESULT_SUCCESS, getViewFamilleNorme());
	}

	@RequestMapping("/sortFamilleNorme")
	public String sortFamilleNorme(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewFamilleNorme());
	}

	@RequestMapping("/downloadFamilleNorme")
	public String downloadFamilleNorme(Model model, HttpServletResponse response) {

		Map<String, ArrayList<String>> selection = viewFamilleNorme.mapContentSelected();

		if (!selection.isEmpty()) {

			String selectedFamille = selection.get(ID_FAMILLE).get(0);

			PreparedStatementBuilder requeteTableMetier = new PreparedStatementBuilder();
			requeteTableMetier.append("SELECT a.* ");
			requeteTableMetier.append("FROM arc.ihm_mod_table_metier a ");
			requeteTableMetier.append("WHERE " + ID_FAMILLE + "=");
			requeteTableMetier.appendQuoteText(selectedFamille);

			PreparedStatementBuilder requeteVariableMetier = new PreparedStatementBuilder();
			requeteVariableMetier.append("SELECT a.* ");
			requeteVariableMetier.append("FROM arc.ihm_mod_variable_metier a ");
			requeteVariableMetier.append("WHERE " + ID_FAMILLE + "=");
			requeteVariableMetier.appendQuoteText(selectedFamille);

			ArrayList<PreparedStatementBuilder> queries = new ArrayList<>();
			queries.add(requeteTableMetier);
			queries.add(requeteVariableMetier);

			ArrayList<String> fileNames = new ArrayList<>();
			fileNames.add("modelTables");
			fileNames.add("modelVariables");

			this.vObjectService.download(viewFamilleNorme, response, fileNames, queries);
			return "none";
		} else {
			this.viewFamilleNorme.setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

	}

	@RequestMapping("/importDDI")
	public String importDDI(Model model, MultipartFile fileUploadDDI) throws IOException {
		loggerDispatcher.debug("importDDI", LOGGER);
		try {

			DDIModeler modeler = DDIParser.parse(fileUploadDDI.getInputStream());

			new DDIInsertDAO(this.databaseObjectService).insertDDI(modeler);

		} catch (ArcException e) {
			this.viewFamilleNorme.setMessage(e.getMessage());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/*
	 * CLIENT
	 */
	private void initializeClient() {
		LoggerHelper.info(LOGGER, "/* initializeClient */");
		try {
			Map<String, ArrayList<String>> selection = viewFamilleNorme.mapContentSelected();
			
			
			
			
			if (!selection.isEmpty()) {

				PreparedStatementBuilder requete = new PreparedStatementBuilder();
				requete.append("SELECT id_famille, id_application FROM arc.ihm_client ");
				requete.append("WHERE id_famille=" + requete.quoteText(selection.get(ID_FAMILLE).get(0)));

				HashMap<String, String> defaultInputFields = new HashMap<>();
				defaultInputFields.put(ID_FAMILLE, selection.get(ID_FAMILLE).get(0));

				this.vObjectService.initialize(viewClient, requete, "arc.ihm_client", defaultInputFields);
			} else {
				this.vObjectService.destroy(viewClient);

			}

		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in GererFamilleNormeAction.initializeClient", LOGGER);
		}
	}

	@RequestMapping("/selectClient")
	public String selectClient(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/addClient")
	public String addClient(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, getViewClient());
	}

	/**
	 * Suppression de Client Regle de gestion : impossible de supprimer une Client
	 * active
	 *
	 * @return
	 */
	@RequestMapping("/deleteClient")
	public String deleteClient(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, getViewClient());
	}

	@RequestMapping("/updateClient")
	public String updateClient(Model model) {
		return updateVobject(model, RESULT_SUCCESS, getViewClient());
	}

	@RequestMapping("/sortClient")
	public String sortClient(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewClient());
	}

	/*
	 * TABLES HOSTS AUTORISES
	 */
	private void initializeHostAllowed() {
		try {
			System.out.println("/* initializeHostAllowed */");
			Map<String, ArrayList<String>> selection = viewClient.mapContentSelected();

			if (!selection.isEmpty()) {
				HashMap<String, String> type = viewClient.mapHeadersType();
				PreparedStatementBuilder requete = new PreparedStatementBuilder();
				requete.append("SELECT * FROM arc.ihm_webservice_whitelist");
				requete.append(
						" WHERE id_famille" + requete.sqlEqual(selection.get(ID_FAMILLE).get(0), type.get(ID_FAMILLE)));
				requete.append(" AND id_application"
						+ requete.sqlEqual(selection.get(ID_APPLICATION).get(0), type.get(ID_APPLICATION)));

				HashMap<String, String> defaultInputFields = new HashMap<>();
				defaultInputFields.put(ID_FAMILLE, selection.get(ID_FAMILLE).get(0));
				defaultInputFields.put(ID_APPLICATION, selection.get(ID_APPLICATION).get(0));

				this.vObjectService.initialize(viewHostAllowed, requete, "arc.ihm_webservice_whitelist",
						defaultInputFields);
			} else {
				this.vObjectService.destroy(viewHostAllowed);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in GererFamilleNormeAction.initializeHostAllowed", LOGGER);
		}
	}

	@RequestMapping("/selectHostAllowed")
	public String selectHostAllowed(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/addHostAllowed")
	public String addHostAllowed(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	@RequestMapping("/deleteHostAllowed")
	public String deleteHostAllowed(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	@RequestMapping("/updateHostAllowed")
	public String updateHostAllowed(Model model) {
		return updateVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	@RequestMapping("/sortHostAllowed")
	public String sortHostAllowed(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	/*
	 * TABLES METIER
	 */
	private void initializeTableMetier() {
		try {
			System.out.println("/* initializeTableMetier */");
			Map<String, ArrayList<String>> selection = viewFamilleNorme.mapContentSelected();
			if (!selection.isEmpty()) {
				HashMap<String, String> type = viewFamilleNorme.mapHeadersType();
				PreparedStatementBuilder requete = new PreparedStatementBuilder();
				requete.append("select * from arc.ihm_mod_table_metier");
				requete.append(
						" where id_famille" + requete.sqlEqual(selection.get(ID_FAMILLE).get(0), type.get(ID_FAMILLE)));
				HashMap<String, String> defaultInputFields = new HashMap<>();
				defaultInputFields.put(ID_FAMILLE, selection.get(ID_FAMILLE).get(0));

				this.vObjectService.initialize(viewTableMetier, requete, "arc.ihm_mod_table_metier",
						defaultInputFields);
			} else {
				this.vObjectService.destroy(viewTableMetier);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in GererFamilleNormeAction.initializeTableMetier", LOGGER);
		}
	}

	@RequestMapping("/selectTableMetier")
	public String selectTableMetier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/deleteTableMetier")
	public String deleteTableMetier(Model model) {

		PreparedStatementBuilder query = new PreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(viewTableMetier));
		query.append(synchronizeRegleWithVariableMetier(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0)));
		query.asTransaction();

		try {
			UtilitaireDao.get("arc").executeRequest(null, query);
		} catch (ArcException e) {
			this.viewTableMetier.setMessage("La suppression des tables a échoué");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortTableMetier")
	public String sortTableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewTableMetier());
	}

	/**
	 *
	 * @param idFamille
	 * @return la liste des tables métier associées à {@code idFamille}
	 */
	private static List<String> getListeTableMetierFamille(String idFamille) {
		StringBuilder requete = new StringBuilder("SELECT nom_table_metier\n")
				.append("  FROM arc.ihm_mod_table_metier\n").append("  WHERE id_famille='" + idFamille + "'");
		return UtilitaireDao.get("arc").getList(null, requete, new ArrayList<String>());
	}

	private void initializeVariableMetier() {
		if (CollectionUtils.isNotEmpty(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE))) {
			List<String> listeTableFamille = getListeTableMetierFamille(
					viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
			HashMap<String, ColumnRendering> rendering = ViewVariableMetier
					.getInitialRenderingViewVariableMetier(new HashMap<String, ColumnRendering>());
			rendering.putAll(ViewVariableMetier.getInitialRendering(listeTableFamille));
			this.vObjectService.initialiserColumnRendering(viewVariableMetier, rendering);
			try {
				System.out.println("/* initializeVariableMetier */");
				PreparedStatementBuilder requete = getRequeteListeVariableMetierTableMetier(listeTableFamille,
						viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
				HashMap<String, String> defaultInputFields = new HashMap<>();
				defaultInputFields.put(ID_FAMILLE, viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
				this.vObjectService.initialize(viewVariableMetier, requete, "arc." + IHM_MOD_VARIABLE_METIER,
						defaultInputFields);

			} catch (Exception ex) {
				StaticLoggerDispatcher.error("Error in GererFamilleNormeAction.initializeVariableMetier", LOGGER);
			}

		} else {
			this.vObjectService.destroy(viewVariableMetier);
		}

	}

	/**
	 *
	 * @param listeVariableMetier
	 * @param idFamille
	 * @return La requête permettant d'obtenir le croisement variable*table pour les
	 *         variables de la famille
	 */
	private static PreparedStatementBuilder getRequeteListeVariableMetierTableMetier(List<String> listeTableMetier,
			String idFamille) {

		PreparedStatementBuilder left = new PreparedStatementBuilder("\n (SELECT nom_variable_metier");
		for (int i = 0; i < listeTableMetier.size(); i++) {
			left.append(
					",\n  CASE WHEN '['||string_agg(nom_table_metier,'][' ORDER BY nom_table_metier)||']' LIKE '%['||'"
							+ listeTableMetier.get(i) + "'||']%' then 'x' else '' end " + listeTableMetier.get(i));
		}
		left.append("\n FROM arc." + IHM_MOD_VARIABLE_METIER + " ");
		left.append("\n WHERE id_famille=" + left.quoteText(idFamille));
		left.append("\n GROUP BY nom_variable_metier) left_side");

		PreparedStatementBuilder right = new PreparedStatementBuilder();
		right.append(
				"\n (SELECT id_famille, nom_variable_metier, type_variable_metier, type_consolidation, description_variable_metier\n");
		right.append("\n FROM arc." + IHM_MOD_VARIABLE_METIER + "\n");
		right.append("\n WHERE id_famille=" + right.quoteText(idFamille));
		right.append(
				"\n GROUP BY id_famille, nom_variable_metier, type_variable_metier, type_consolidation, description_variable_metier) right_side");

		PreparedStatementBuilder returned = new PreparedStatementBuilder(
				"SELECT right_side.id_famille, right_side.nom_variable_metier, right_side.type_variable_metier, right_side.type_consolidation, right_side.description_variable_metier");
		for (int i = 0; i < listeTableMetier.size(); i++) {
			returned.append(", " + listeTableMetier.get(i));
		}
		returned.append("\n FROM ");
		returned.append(left);
		returned.append(" INNER JOIN ");
		returned.append(right);

		returned.append("\n ON left_side.nom_variable_metier = right_side.nom_variable_metier");

		return returned;
	}

	@RequestMapping("/selectVariableMetier")
	public String selectVariableMetier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/addVariableMetier")
	public String addVariableMetier(Model model) {

		StringBuilder message = new StringBuilder();
		StringBuilder bloc = new StringBuilder();
		String queryToAddNonExistingVaribales = addNonExistingVariableMetierWithoutSync(message);
		if (!queryToAddNonExistingVaribales.equals(empty)) {
			bloc.append(addNonExistingVariableMetierWithoutSync(message));
			bloc.append(
					synchronizeRegleWithVariableMetier(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0)));
			executeRequeteMiseAjourTableMetier(message, bloc);
		}
		this.viewVariableMetier.setMessage(message.toString());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortVariableMetier")
	public String sortVariableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewVariableMetier());
	}

	@RequestMapping("/deleteVariableMetier")
	public String deleteVariableMetier(Model model) {
		StringBuilder message = new StringBuilder();
		StringBuilder bloc = new StringBuilder();
		bloc.append(deleteVariableMetierWithoutSync(viewVariableMetier.mapContentSelected(),
				viewVariableMetier.listContentSelected(), false));
		bloc.append(synchronizeRegleWithVariableMetier(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0)));
		executeRequeteMiseAjourTableMetier(message, bloc);
		this.viewVariableMetier.setMessage(message.toString());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/updateVariableMetier")
	public String updateVariableMetier(Model model) {
		try {

			StringBuilder message = new StringBuilder();
			StringBuilder requete = new StringBuilder();

			HashMap<String, ArrayList<String>> mBefore = viewVariableMetier.mapContentBeforeUpdate();
			List<ArrayList<String>> lBefore = viewVariableMetier.listContentBeforeUpdate();

			List<ArrayList<String>> lAfter = viewVariableMetier.listContentAfterUpdate();
			int nameIndex = this.viewVariableMetier.getHeadersDLabel().indexOf(MODEL_VARIABLE_NAME);

			for (ArrayList<String> modifiedLine : lAfter) {
				int indexOfVar = nameIndex;
				modifiedLine.set(indexOfVar, ArcStringUtils.cleanUpVariable(modifiedLine.get(indexOfVar)));
			}

			// part 1 : update data field names
			for (int i = 0; i < lAfter.size(); i++) {
				String nameAfter = lAfter.get(i).get(nameIndex);
				String nameBefore = lBefore.get(i).get(nameIndex);
				if (nameAfter != null && !nameBefore.equals(nameAfter)) {
					// mise à jour du nom de la variable dans la table métier
					requete.append("\n");
					requete.append("update arc.ihm_mod_variable_metier set nom_variable_metier='" + nameAfter + "' ");
					requete.append("where nom_variable_metier='" + nameBefore + "' ");
					requete.append(
							"and id_famille='" + viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0) + "'; ");

					// mise à jour du nom de la variable dans la table de règle
					requete.append("\n");
					requete.append("update arc.ihm_mapping_regle a set variable_sortie='" + nameAfter + "' ");
					requete.append("where variable_sortie='" + nameBefore + "' ");
					requete.append(
							"and exists (select from arc.ihm_norme b where a.id_norme=b.id_norme and b.id_famille='"
									+ viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0) + "'); ");

					// mise à jour du nom de la variable dans les tables des environements
					StringBuilder requeteListeEnvironnement = new StringBuilder(
							"SELECT distinct replace(id,'.','_') FROM arc.ext_etat_jeuderegle where isenv");
					List<String> listeEnvironnement = UtilitaireDao.get("arc").getList(null, requeteListeEnvironnement,
							new ArrayList<String>());

					for (String envName : listeEnvironnement) {
						for (int k = numberOfColumnTableVariableMetier; k < mBefore.size(); k++) {
							String nomVeridique = envName + "." + this.viewVariableMetier.getHeadersDLabel().get(k);

							/**
							 * Si la variable est définie pour cette table
							 */
							if (StringUtils.isNotBlank(lBefore.get(i).get(k))) {
								/**
								 * Si la table existe, on tente une suppression de la colonne
								 */
								if (Boolean.TRUE.equals(UtilitaireDao.get("arc").isTableExiste(null, nomVeridique))) {
									/**
									 * Pour cela, la colonne doit exister
									 */
									if (UtilitaireDao.get("arc").isColonneExiste(null, nomVeridique, nameBefore)) {

										requete.append("\n");
										requete.append("ALTER TABLE " + nomVeridique + " RENAME " + nameBefore + " TO "
												+ nameAfter + ";");

									}
								}
							}
						}

					}
				}

			}

			requete.append("\n");

			// part 2 : update the rest of the data model fields
			if (isModificationOk(message, this.viewVariableMetier.mapUpdatedContent())) {
				requete.append(addExistingVariableMetierWithoutSync(message,
						this.viewVariableMetier.listOnlyUpdatedContent()));
				requete.append(mettreAJourInformationsVariables(this.viewVariableMetier));
				requete.append(synchronizeRegleWithVariableMetier(
						viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0)));
				executeRequeteMiseAjourTableMetier(message, requete);
			}

			this.viewVariableMetier.setMessage(message.toString());

		} catch (Exception e) {
			this.viewVariableMetier.setMessage(e.getMessage());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Ajoute une variable métier par INSERT (la variable métier va être ajoutée)
	 *
	 * @param message
	 */
	private String addNonExistingVariableMetierWithoutSync(StringBuilder message) {
		StringBuilder requete = new StringBuilder();
		boolean blank = true;
		for (int i = numberOfColumnTableVariableMetier; i < this.viewVariableMetier.getInputFields().size(); i++) {
			if (StringUtils.isNotBlank(this.viewVariableMetier.getInputFields().get(i))
			// && this.viewVariableMetier.getInputFields().get(i).equals("oui")
			) {

				// au moins une table est renseignée
				blank = false;

				String nomVariableMetier = this.viewVariableMetier.getInputFieldFor(MODEL_VARIABLE_NAME);
				this.viewVariableMetier.setInputFieldFor(MODEL_VARIABLE_NAME,
						ArcStringUtils.cleanUpVariable(nomVariableMetier));

				if (checkIsValide(this.viewVariableMetier.getInputFields())) {
					requete.append("INSERT INTO arc." + IHM_MOD_VARIABLE_METIER + " (");
					StringBuilder values = new StringBuilder();
					for (int j = 0; j < numberOfColumnTableVariableMetier; j++) {
						if (j > 0) {
							requete.append(", ");
							values.append(", ");
						}
						requete.append(this.viewVariableMetier.getHeadersDLabel().get(j));
						values.append("'" + this.viewVariableMetier.getInputFields().get(j) + "'::"
								+ this.viewVariableMetier.getHeadersDType().get(j));
					}
					requete.append(", nom_table_metier) VALUES ("
							+ values.append(", '" + this.viewVariableMetier.getHeadersDLabel().get(i)) + "'::text);\n");
				} else {
					message.append("La variable " + this.viewVariableMetier.getInputFields().get(1)
							+ " existe déjà. Pour la modifier, passez par la ligne correspondante du tableau variable*table.\nAucune variable n'a été ajoutée.\n");
					return empty;
				}
			}
		}

		if (blank) {
			message.append("Vous avez oublié de spécifier les tables cibles pour votre variable");
			return empty;
		}
		return requete.toString();
	}

	private static boolean checkIsValide(List<String> inputFields) {
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
		requete.append("SELECT count(1) FROM arc." + IHM_MOD_VARIABLE_METIER)//
				.append("\n WHERE id_famille=" + requete.quoteText(inputFields.get(0)))//
				.append("\n AND nom_variable_metier=" + requete.quoteText(inputFields.get(1)) + ";");
		return UtilitaireDao.get("arc").getInt(null, requete) == 0;
	}

	/**
	 * Ajoute une variable métier à des tables par UPDATE (la variable existe déjà)
	 *
	 * @param message
	 */
	private String addExistingVariableMetierWithoutSync(StringBuilder message, List<ArrayList<String>> listContent) {
		StringBuilder requete = new StringBuilder();
		/**
		 * Pour chaque ligne à UPDATE
		 */
		for (int i = 0; i < listContent.size(); i++) {
			/**
			 * Et pour l'ensemble des tables métier
			 */
			for (int j = numberOfColumnTableVariableMetier; j < viewVariableMetier.mapContentAfterUpdate(i)
					.size(); j++) {
				/**
				 * Si une variable est à "oui" pour cette table alors qu'elle n'y était pas...
				 */
				if (StringUtils.isNotBlank(listContent.get(i).get(j))
						&& StringUtils.isBlank(viewVariableMetier.listContentBeforeUpdate().get(i).get(j))) {
					/**
					 * ... on l'ajoute
					 */
					requete.append("INSERT INTO arc." + IHM_MOD_VARIABLE_METIER + " (");
					StringBuilder values = new StringBuilder();
					for (int k = 0; k < numberOfColumnTableVariableMetier; k++) {
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace(i + " " + j + " " + k);
						}
						if (k > 0) {
							requete.append(", ");
							values.append(", ");
						}
						requete.append(this.viewVariableMetier.getHeadersDLabel().get(k));
						values.append(//
								((listContent.get(i).get(k) == null) ? "null" : ("'" + listContent.get(i).get(k) + "'"))//
										+ "::" + this.viewVariableMetier.getHeadersDType().get(k));
					}
					requete.append(", nom_table_metier) VALUES ("
							+ values.append(", '" + this.viewVariableMetier.getHeadersDLabel().get(j)) + "'::text);\n");
				}
			}
		}
		return requete.toString();
	}

	private static String synchronizeRegleWithVariableMetier(String idFamille) {
		/**
		 * Sélection des règles à détruire
		 */
		StringBuilder requeteListeSupprRegleMapping = new StringBuilder("DELETE FROM arc.ihm_mapping_regle regle\n");
		requeteListeSupprRegleMapping.append("  WHERE NOT EXISTS (");
		requeteListeSupprRegleMapping
				.append("    SELECT 1 FROM arc." + IHM_MOD_VARIABLE_METIER + " var INNER JOIN arc.ihm_famille fam\n");
		requeteListeSupprRegleMapping.append("    ON var.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.variable_sortie=var.nom_variable_metier\n");
		requeteListeSupprRegleMapping.append("    INNER JOIN arc.ihm_norme norme\n");
		requeteListeSupprRegleMapping.append("    ON norme.id_famille=fam.id_famille\n");
		requeteListeSupprRegleMapping.append("    AND regle.id_norme=norme.id_norme\n");
		requeteListeSupprRegleMapping.append("    WHERE fam.id_famille = '" + idFamille + "'");
		requeteListeSupprRegleMapping.append("  )");
		requeteListeSupprRegleMapping
				.append("    AND EXISTS (SELECT 1 FROM arc.ihm_norme norme INNER JOIN arc.ihm_famille fam");
		requeteListeSupprRegleMapping.append("      ON norme.id_famille=fam.id_famille");
		requeteListeSupprRegleMapping.append("      AND regle.id_norme=norme.id_norme");
		requeteListeSupprRegleMapping.append("      WHERE fam.id_famille = '" + idFamille + "')");
		/**
		 * Sélection des règles à créer
		 */
		StringBuilder requeteListeAddRegleMapping = new StringBuilder("INSERT INTO arc.ihm_mapping_regle (");
		requeteListeAddRegleMapping.append("id_regle");
		requeteListeAddRegleMapping.append(", id_norme");
		requeteListeAddRegleMapping.append(", validite_inf");
		requeteListeAddRegleMapping.append(", validite_sup");
		requeteListeAddRegleMapping.append(", version");
		requeteListeAddRegleMapping.append(", periodicite");
		requeteListeAddRegleMapping.append(", variable_sortie");
		requeteListeAddRegleMapping.append(", expr_regle_col");
		requeteListeAddRegleMapping.append(", commentaire)");
		requeteListeAddRegleMapping
				.append("\n  SELECT (SELECT max(id_regle) FROM arc.ihm_mapping_regle) + row_number() over ()");
		requeteListeAddRegleMapping.append(", norme.id_norme");
		requeteListeAddRegleMapping.append(", calendrier.validite_inf");
		requeteListeAddRegleMapping.append(", calendrier.validite_sup");
		requeteListeAddRegleMapping.append(", jdr.version");
		requeteListeAddRegleMapping.append(", norme.periodicite");
		requeteListeAddRegleMapping.append(", var.nom_variable_metier");
		requeteListeAddRegleMapping.append(", '" + FormatSQL.NULL + "'");
		requeteListeAddRegleMapping.append(", " + FormatSQL.NULL + "::text ");
		requeteListeAddRegleMapping.append("\n  FROM (SELECT DISTINCT id_famille, nom_variable_metier FROM arc."
				+ IHM_MOD_VARIABLE_METIER + ") var INNER JOIN arc.ihm_famille fam");
		requeteListeAddRegleMapping.append("\n    ON var.id_famille=fam.id_famille");
		requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_norme norme");
		requeteListeAddRegleMapping.append("\n    ON fam.id_famille=norme.id_famille");
		requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_calendrier calendrier");
		requeteListeAddRegleMapping
				.append("\n    ON calendrier.id_norme=norme.id_norme AND calendrier.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_jeuderegle jdr");
		requeteListeAddRegleMapping
				.append("\n    ON calendrier.id_norme=jdr.id_norme AND calendrier.periodicite=jdr.periodicite");
		requeteListeAddRegleMapping.append(
				"\n      AND calendrier.validite_inf=jdr.validite_inf AND calendrier.validite_sup=jdr.validite_sup");
		requeteListeAddRegleMapping.append("\n  WHERE fam.id_famille = '" + idFamille + "'");
		requeteListeAddRegleMapping.append("\n    AND lower(jdr.etat) <> 'inactif'");
		requeteListeAddRegleMapping.append("\n    AND lower(calendrier.etat) = '1'");
		requeteListeAddRegleMapping.append("\n    AND NOT EXISTS (");
		requeteListeAddRegleMapping.append("\n      SELECT 1 FROM arc.ihm_mapping_regle regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.variable_sortie=var.nom_variable_metier");
		requeteListeAddRegleMapping.append("\n        AND regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    ) AND EXISTS (");
		requeteListeAddRegleMapping.append("\n      SELECT 1 FROM arc.ihm_mapping_regle regle");
		requeteListeAddRegleMapping.append("\n      WHERE regle.id_norme=norme.id_norme");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
		requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
		requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
		requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
		requeteListeAddRegleMapping.append("\n    )");
		StringBuilder requete = new StringBuilder();
		requete.append(requeteListeAddRegleMapping.toString() + ";\n");
		requete.append(requeteListeSupprRegleMapping.toString() + ";");
		return requete.toString();
	}

	private boolean isModificationOk(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
		return estCeQueLesNomsDeVariablesSontNonNuls(message, mapContentAfterUpdate);
	}

	private static boolean estCeQueLesNomsDeVariablesSontNonNuls(StringBuilder message,
			HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
		for (int i = 0; i < mapContentAfterUpdate.get(MODEL_VARIABLE_NAME).size(); i++) {
			String nomVariable = mapContentAfterUpdate.get(MODEL_VARIABLE_NAME).get(i);
			if (nomVariable == null) {
				message.append("Une variable a un nom null.");
				return false;
			}
		}
		return true;
	}

	/**
	 * Détruit une variable métier dans la table de référence
	 * ihm_mod_variable_metier. Ne détruit pas les colonnes correspondantes dans les
	 * tables d'environnement concernées.
	 *
	 * @param message
	 * @param listContentBeforeUpdate Peut être à null
	 */
	private String deleteVariableMetierWithoutSync(Map<String, ArrayList<String>> map,
			List<ArrayList<String>> arrayList, boolean onlyWhereBlank) {
		StringBuilder delete = new StringBuilder();
		/**
		 * Pour chaque variable :<br/>
		 * 1. Lister les tables<br/>
		 * 2. Supprimer cette colonne des tables listées<br/>
		 * 3. Supprimer cette variable*table de ihm_mod_variable_metier<br/>
		 * 4. Supprimer la règle correspondante de ihm_mapping_regle
		 */
		StringBuilder listeTable = new StringBuilder();
		for (int j = 0; j < map.get(MODEL_VARIABLE_NAME).size(); j++) {
			String nomVariable = map.get(MODEL_VARIABLE_NAME).get(j);
			/**
			 * On prépare la liste des tables comportant effectivement la variable
			 */
			listeTable.setLength(0);
			/**
			 * Pour chaque table trouvée
			 */
			for (int i = numberOfColumnTableVariableMetier; i < map.size(); i++) {
				if (StringUtils.isBlank(arrayList.get(j).get(i)) || !onlyWhereBlank) {
					listeTable.append("[" + this.viewVariableMetier.getHeadersDLabel().get(i) + "]");
				}
			}
			delete.append("DELETE FROM arc." + IHM_MOD_VARIABLE_METIER + " WHERE id_famille='"
					+ map.get(ID_FAMILLE).get(j) + "' AND nom_variable_metier='" + nomVariable + "'::text AND '"
					+ listeTable + "' like '%['||nom_table_metier||']%';\n");
		}
		return delete.toString();
	}

	/**
	 * Update the description fields for variable
	 * 
	 * @param someViewVariableMetier
	 * @return
	 */
	private String mettreAJourInformationsVariables(VObject someViewVariableMetier) {
		StringBuilder requete = new StringBuilder();
		for (int i = 0; i < someViewVariableMetier.listOnlyUpdatedContent().size(); i++) {
			if (i > 0) {
				requete.append("\n");
			}

			HashMap<String, ArrayList<String>> content = someViewVariableMetier.mapOnlyUpdatedContent();
			StringBuilder requeteLocale = new StringBuilder("UPDATE arc." + IHM_MOD_VARIABLE_METIER + " a ");
			requeteLocale.append("\n  SET type_consolidation = ");
			requeteLocale.append(computeMapcontent(content, "type_consolidation", i));
			requeteLocale.append(",\n    description_variable_metier = ");
			requeteLocale.append(computeMapcontent(content, "description_variable_metier", i));
			requeteLocale.append("\n  WHERE id_famille = '"
					+ someViewVariableMetier.mapOnlyUpdatedContent().get(ID_FAMILLE).get(i) + "'");
			requeteLocale.append("\n    AND nom_variable_metier = '"
					+ someViewVariableMetier.mapOnlyUpdatedContent().get(MODEL_VARIABLE_NAME).get(i) + "'");
			requete.append(requeteLocale).append(";");
		}
		return requete.toString();
	}

	private String computeMapcontent(HashMap<String, ArrayList<String>> content, String columnName, int index) {
		if (content.get(columnName) == null || content.get(columnName).get(index) == null) {
			return columnName;
		} else {
			return "'" + content.get(columnName).get(index).replace(quote, quotequote) + "'";
		}
	}

	private static final void executeRequeteMiseAjourTableMetier(StringBuilder message, StringBuilder requete) {
		try {
			UtilitaireDao.get("arc").executeBlock(null, requete);
			message.append("La mise a jour a échouée a réussi");
		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in GererFamilleNormeAction.executeRequeteMiseAjourTableMetier", LOGGER);
			message.append("La mise a jour a échoué");
		}
	}

	private final boolean isNomTableMetierValide(String nomTable) {
		return isNomTableMetierValide(nomTable, TraitementPhase.MAPPING.toString().toLowerCase(),
				viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
	}

	static final boolean isNomTableMetierValide(String nomTable, String phase, String famille) {
		return nomTable.matches("(?i)^" + phase.toLowerCase() + "_" + famille + "_[a-z]([a-z]|[0-9]|_)+_ok$");
	}

	@RequestMapping("/addTableMetier")
	public String addTableMetier(Model model) {
		if (isNomTableMetierValide(viewTableMetier.mapInputFields().get(NOM_TABLE_METIER).get(0))) {
			this.vObjectService.insert(viewTableMetier);
		} else {
			setMessageNomTableMetierInvalide();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	private void setMessageNomTableMetierInvalide() {
		this.viewTableMetier.setMessage("familyManagement.table.error.invalidname");
		this.viewTableMetier.setMessageArgs(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
	}

	/**
	 * @return the viewTableMetier
	 */
	public final VObject getViewTableMetier() {
		return this.viewTableMetier;
	}

	/**
	 * @param vObjectData the viewTableMetier to set
	 */
	public final void setViewTableMetier(VObject vObjectData) {
		this.viewTableMetier = vObjectData;
	}

	/**
	 * @return the viewVariableMetier
	 */
	public final VObject getViewVariableMetier() {
		return this.viewVariableMetier;
	}

	/**
	 * @param viewVariableMetier the viewVariableMetier to set
	 */
	public final void setViewVariableMetier(VObject viewVariableMetier) {
		this.viewVariableMetier = viewVariableMetier;
	}

	/**
	 * @return the viewFamilleNorme
	 */
	public final VObject getViewFamilleNorme() {
		return this.viewFamilleNorme;
	}

	/**
	 * @param vObjectData the viewFamilleNorme to set
	 */
	public final void setViewFamilleNorme(VObject vObjectData) {
		this.viewFamilleNorme = vObjectData;
	}

	/**
	 * @return the viewClient
	 */
	public final VObject getViewClient() {
		return this.viewClient;
	}

	/**
	 * @param viewClient the viewClient to set
	 */
	public final void setViewClient(VObject viewClient) {
		this.viewClient = viewClient;
	}

	public final VObject getViewHostAllowed() {
		return viewHostAllowed;
	}

	public final void setViewHostAllowed(VObject viewHostAllowed) {
		this.viewHostAllowed = viewHostAllowed;
	}

}
