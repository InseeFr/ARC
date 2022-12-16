package fr.insee.arc.web.webusecases.gererfamillenorme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.util.ArcStringUtils;
import fr.insee.arc.web.util.VObject;

@Service
public class ServiceViewVariableMetier extends HubServiceGererFamilleNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewVariableMetier.class);


	public String selectVariableMetier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addVariableMetier(Model model) {

		StringBuilder message = new StringBuilder();
		StringBuilder bloc = new StringBuilder();
		String queryToAddNonExistingVaribales = addNonExistingVariableMetierWithoutSync(message);
		if (!queryToAddNonExistingVaribales.equals(empty)) {
			bloc.append(addNonExistingVariableMetierWithoutSync(message));
			bloc.append(
					synchronizeRegleWithVariableMetier(views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0)));
			executeRequeteMiseAjourTableMetier(message, bloc);
		}
		this.views.getViewVariableMetier().setMessage(message.toString());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortVariableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewVariableMetier());
	}

	public String deleteVariableMetier(Model model) {
		StringBuilder message = new StringBuilder();
		StringBuilder bloc = new StringBuilder();
		bloc.append(deleteVariableMetierWithoutSync(views.getViewVariableMetier().mapContentSelected(),
				views.getViewVariableMetier().listContentSelected(), false));
		bloc.append(synchronizeRegleWithVariableMetier(views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0)));
		executeRequeteMiseAjourTableMetier(message, bloc);
		this.views.getViewVariableMetier().setMessage(message.toString());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateVariableMetier(Model model) {
		try {

			StringBuilder message = new StringBuilder();
			StringBuilder requete = new StringBuilder();

			HashMap<String, ArrayList<String>> mBefore = views.getViewVariableMetier().mapContentBeforeUpdate();
			List<ArrayList<String>> lBefore = views.getViewVariableMetier().listContentBeforeUpdate();

			List<ArrayList<String>> lAfter = views.getViewVariableMetier().listContentAfterUpdate();
			int nameIndex = this.views.getViewVariableMetier().getHeadersDLabel().indexOf(MODEL_VARIABLE_NAME);

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
							"and id_famille='" + views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0) + "'; ");

					// mise à jour du nom de la variable dans la table de règle
					requete.append("\n");
					requete.append("update arc.ihm_mapping_regle a set variable_sortie='" + nameAfter + "' ");
					requete.append("where variable_sortie='" + nameBefore + "' ");
					requete.append(
							"and exists (select from arc.ihm_norme b where a.id_norme=b.id_norme and b.id_famille='"
									+ views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0) + "'); ");

					// mise à jour du nom de la variable dans les tables des environements
					StringBuilder requeteListeEnvironnement = new StringBuilder(
							"SELECT distinct replace(id,'.','_') FROM arc.ext_etat_jeuderegle where isenv");
					List<String> listeEnvironnement = UtilitaireDao.get("arc").getList(null, requeteListeEnvironnement,
							new ArrayList<String>());

					for (String envName : listeEnvironnement) {
						for (int k = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; k < mBefore.size(); k++) {
							String nomVeridique = envName + "." + this.views.getViewVariableMetier().getHeadersDLabel().get(k);

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
			if (isModificationOk(message, this.views.getViewVariableMetier().mapUpdatedContent())) {
				requete.append(addExistingVariableMetierWithoutSync(message,
						this.views.getViewVariableMetier().listOnlyUpdatedContent()));
				requete.append(mettreAJourInformationsVariables(this.views.getViewVariableMetier()));
				requete.append(synchronizeRegleWithVariableMetier(
						views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0)));
				executeRequeteMiseAjourTableMetier(message, requete);
			}

			this.views.getViewVariableMetier().setMessage(message.toString());

		} catch (Exception e) {
			this.views.getViewVariableMetier().setMessage(e.getMessage());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	

	private static final void executeRequeteMiseAjourTableMetier(StringBuilder message, StringBuilder requete) {
		try {
			UtilitaireDao.get("arc").executeBlock(null, requete);
			message.append("La mise a jour a réussi");
		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in GererFamilleNormeAction.executeRequeteMiseAjourTableMetier", LOGGER);
			message.append("La mise a jour a échoué");
		}
	}
	

	/**
	 * Ajoute une variable métier par INSERT (la variable métier va être ajoutée)
	 *
	 * @param message
	 */
	private String addNonExistingVariableMetierWithoutSync(StringBuilder message) {
		StringBuilder requete = new StringBuilder();
		boolean blank = true;
		for (int i = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; i < this.views.getViewVariableMetier().getInputFields().size(); i++) {
			if (StringUtils.isNotBlank(this.views.getViewVariableMetier().getInputFields().get(i))
			// && this.views.getViewVariableMetier().getInputFields().get(i).equals("oui")
			) {

				// au moins une table est renseignée
				blank = false;

				String nomVariableMetier = this.views.getViewVariableMetier().getInputFieldFor(MODEL_VARIABLE_NAME);
				this.views.getViewVariableMetier().setInputFieldFor(MODEL_VARIABLE_NAME,
						ArcStringUtils.cleanUpVariable(nomVariableMetier));

				if (checkIsValide(this.views.getViewVariableMetier().getInputFields())) {
					requete.append("INSERT INTO arc." + IHM_MOD_VARIABLE_METIER + " (");
					StringBuilder values = new StringBuilder();
					for (int j = 0; j < NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; j++) {
						if (j > 0) {
							requete.append(", ");
							values.append(", ");
						}
						requete.append(this.views.getViewVariableMetier().getHeadersDLabel().get(j));
						values.append("'" + this.views.getViewVariableMetier().getInputFields().get(j) + "'::"
								+ this.views.getViewVariableMetier().getHeadersDType().get(j));
					}
					requete.append(", nom_table_metier) VALUES ("
							+ values.append(", '" + this.views.getViewVariableMetier().getHeadersDLabel().get(i)) + "'::text);\n");
				} else {
					message.append("La variable " + this.views.getViewVariableMetier().getInputFields().get(1)
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
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
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
			for (int j = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; j < views.getViewVariableMetier().mapContentAfterUpdate(i)
					.size(); j++) {
				/**
				 * Si une variable est à "oui" pour cette table alors qu'elle n'y était pas...
				 */
				if (StringUtils.isNotBlank(listContent.get(i).get(j))
						&& StringUtils.isBlank(views.getViewVariableMetier().listContentBeforeUpdate().get(i).get(j))) {
					/**
					 * ... on l'ajoute
					 */
					requete.append("INSERT INTO arc." + IHM_MOD_VARIABLE_METIER + " (");
					StringBuilder values = new StringBuilder();
					for (int k = 0; k < NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; k++) {
						if (k > 0) {
							requete.append(", ");
							values.append(", ");
						}
						requete.append(this.views.getViewVariableMetier().getHeadersDLabel().get(k));
						values.append(//
								((listContent.get(i).get(k) == null) ? "null" : ("'" + listContent.get(i).get(k) + "'"))//
										+ "::" + this.views.getViewVariableMetier().getHeadersDType().get(k));
					}
					requete.append(", nom_table_metier) VALUES ("
							+ values.append(", '" + this.views.getViewVariableMetier().getHeadersDLabel().get(j)) + "'::text);\n");
				}
			}
		}
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
			for (int i = NUMBER_OF_COLUMN_TABLE_VARIABLE_METIER; i < map.size(); i++) {
				if (StringUtils.isBlank(arrayList.get(j).get(i)) || !onlyWhereBlank) {
					listeTable.append("[" + this.views.getViewVariableMetier().getHeadersDLabel().get(i) + "]");
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

	
}
