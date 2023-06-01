package fr.insee.arc.core.service.engine.normage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;

public class NormageEngineRegleDuplication {

	private static final Logger LOGGER = LogManager.getLogger(NormageEngineRegleDuplication.class);

	
	/**
	 * Ajouter les règles de duplication si dans une relation, je trouve un "." dans
	 * une rubrique de nomenclature, j'ordonne la duplication et je rectifie la
	 * regle de relation
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @throws ArcException
	 */
	protected static void ajouterRegleDuplication(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) {
		StaticLoggerDispatcher.info("ajouterRegleDuplication()", LOGGER);

		// pour toutes les règles de relation,
		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);

			// en gros on parcours les regles de relation
			// on va exclure les bloc en relation des blocs à calculer comme indépendants

			if (type.equals("relation")) {
				String rubrique = regle.get("rubrique").get(j).toLowerCase();
				String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();

				String rub = ManipString.substringAfterFirst(rubriqueNmcl, ".");
				String alias = ManipString.substringBeforeFirst(rubriqueNmcl, ".");

				// la duplication ne peut se faire que :
				// 1- sur une rubrique de nomenclature
				// 2- si les rubriques existent dans la requete initiale
				// 3- l'alias = la chaine avant le point de la rubrique nomenclature
				if (rubriqueNmcl.contains(".") && jointure.contains(" " + rubrique + " ")
						&& jointure.contains(" " + rub + " ")) {

					// modifier la regle de rubrique_nmcl : alias.rubrique devient rubrique_alias
					regle.get("rubrique_nmcl").set(j, rub + "_" + alias);

					// ajout de la règle
					regle.get("id_regle").add("G" + System.currentTimeMillis());
					regle.get("id_norme").add(norme);
					regle.get("periodicite").add(periodicite);
					regle.get("validite_inf").add("1900-01-01");
					regle.get("validite_sup").add("3000-01-01");
					regle.get("id_classe").add("duplication");
					regle.get("rubrique").add(rub);
					regle.get("rubrique_nmcl").add(alias);
				}

			}

			if (type.equals("unicité")) {
				String rubrique = regle.get("rubrique").get(j).toLowerCase();

				String rub = ManipString.substringAfterFirst(rubrique, ".");
				String alias = ManipString.substringBeforeFirst(rubrique, ".");

				// la duplication ne peut se faire que :
				// 1- sur une rubrique de nomenclature
				// 2- si les rubriques existent dans la requete initiale
				// 3- l'alias = la chaine avant le point de la rubrique nomenclature
				if (rubrique.contains(".")) {
					// modifier la regle de rubrique : alias.rubrique devient rubrique_alias
					regle.get("rubrique").set(j, rub + "_" + alias);

				}

			}

		}

	}
	
	protected static String appliquerRegleDuplication(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure, StringBuilder columnToBeAdded) {

		StaticLoggerDispatcher.info("appliquerRegleDuplication()", LOGGER);

		String returned = jointure;

		String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");
		String blocInsert = " insert into {table_destination} "
				+ ManipString.substringAfterFirst(returned, "insert into {table_destination} ");

		for (int j = 0; j < regle.get("id_regle").size(); j++) {

			String type = regle.get("id_classe").get(j);

			if (type.equals("duplication")) {
				String rubrique = regle.get("rubrique").get(j).toLowerCase();
				String alias = regle.get("rubrique_nmcl").get(j).toLowerCase();

				// retrouver la table qui doit faire l'objet d'une duplication
				String rubriqueM = NormageEngineGlobal.getM(blocCreate, rubrique);

				// si on trouve une rubrique mère
				if (rubriqueM != null) {
					ArrayList<String> aTraiter = NormageEngineGlobal.getChildrenTree(blocCreate, rubriqueM);
					aTraiter.add(0, rubriqueM);

					StringBuilder blocCreateNew = new StringBuilder();
					StringBuilder blocInsertNew = new StringBuilder();
					HashSet<String> colonnesAAjouter = new HashSet<>();

					String[] lines = blocCreate.split("\n");
					for (int i = 0; i < lines.length; i++) {
						String rubriqueLine = NormageEngineGlobal.getM(lines[i]);
						String pereLine = NormageEngineGlobal.getFather(lines[i]);
						String tableLine = NormageEngineGlobal.getTable(lines[i]);

						if (aTraiter.contains(rubriqueLine)) {
							String pattern = " as ([^ ()]*) ";

							// on a trouvé et on effectue les remplacements
							// on remplace le nom des colonnes (ajout de l'alias) avant le premier from
							// on remplace le nom de la table (ajout de l'alias)
							String newLine = ManipString.substringBeforeFirst(lines[i], " from ")
									.replaceAll(pattern, " as $1_" + alias + " ")
									.replace(" " + tableLine + " ", " " + tableLine + "_" + alias + " ") + " from "
									+ ManipString.substringAfterFirst(lines[i], " from ");

							//
							Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(ManipString.substringBeforeFirst(lines[i], " from "));
							while (m.find()) {
								colonnesAAjouter.add(m.group(1) + "_" + alias);
							}

							// cas particulier : ne pas changer le pere de la table maitre à dupliquer
							// on ne change le pere que pour les tables enfants

							if (aTraiter.indexOf(rubriqueLine) == 0) {
								newLine = newLine.replace(" as " + pereLine + "_" + alias + " ",
										" as " + pereLine + " ");
								colonnesAAjouter.remove(pereLine + "_" + alias);
							}

							blocCreateNew.append(newLine + "\n");
							blocCreateNew.append("create temporary table " + tableLine + "_" + alias
									+ "_null as (select * from " + tableLine + "_" + alias + " where false); " + "\n");
						}

						blocCreateNew.append(lines[i] + "\n");

						for (String c : colonnesAAjouter) {
							c = NormageEngineGlobal.mToI(c);

							if (!blocCreate.contains(" add " + c + " ")
									&& !blocCreateNew.toString().contains(" add " + c + " ")) {
								if (c.startsWith("i_")) {
									String addCol = "do $$ begin alter table {table_destination} add " + c
											+ " integer; exception when others then end; $$;";
									if (!columnToBeAdded.toString().contains(addCol)) {
										columnToBeAdded.append(addCol);
									}
								} else {
									String addCol = "do $$ begin alter table {table_destination} add " + c
											+ " text; exception when others then end; $$;";
									if (!columnToBeAdded.toString().contains(addCol)) {
										columnToBeAdded.append(addCol);
									}
								}
							}
						}

					}

					blocCreate = blocCreateNew.toString();

					// bloc insert
					// faut ajouter les variables et les relations

					lines = blocInsert.split("\n");
					for (int i = 0; i < lines.length; i++) {
						// insertion des variables
						blocInsertNew.append(lines[i]);

						// ligne des inserts
						// la colonne m_ devient i_
						if (i == 0) {
							for (String c : colonnesAAjouter) {
								c = NormageEngineGlobal.mToI(c);
								if (!lines[i].contains("," + c)) {
									blocInsertNew.append("," + c);
								}
							}
						}

						// ligne du select
						// on ne change pas m_ en i_
						if (i == 3) {
							for (String c : colonnesAAjouter) {
								if (!lines[i].contains("," + c)) {
									blocInsertNew.append("," + c);
								}
							}
						}

						blocInsertNew.append("\n");

						// ajout de la relation
						if (i > 3) {
							for (int k = 0; k < aTraiter.size(); k++) {
								if (lines[i].contains(" t_" + NormageEngineGlobal.getCoreVariableName(aTraiter.get(k)) + " ")) {
									// remplacement du nom de la table
									String newLine = lines[i]
											.replace(" t_" + NormageEngineGlobal.getCoreVariableName(aTraiter.get(k)) + " ",
													" t_" + NormageEngineGlobal.getCoreVariableName(aTraiter.get(k)) + "_" + alias + " ")
											.replace("=t_" + NormageEngineGlobal.getCoreVariableName(aTraiter.get(k)) + ".",
													"=t_" + NormageEngineGlobal.getCoreVariableName(aTraiter.get(k)) + "_" + alias + ".");
									// si enfant, on change aussi le nom des variables
									if (k > 0) {
										newLine = newLine.replace("=", "_" + alias + "=") + "_" + alias;
									}
									blocInsertNew.append(newLine + "\n");
								}
							}

						}

					}

					blocInsert = blocInsertNew.toString();
				}
			}
		}

		returned = blocCreate.replaceAll("\n$", "") + "\n" + blocInsert.replaceAll("\n$", "");

		return returned;
	}
	
}
