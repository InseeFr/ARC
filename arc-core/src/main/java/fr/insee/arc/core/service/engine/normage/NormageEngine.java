package fr.insee.arc.core.service.engine.normage;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.handler.XMLComplexeHandlerCharger;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;

public class NormageEngine {

	private static final Logger LOGGER = LogManager.getLogger(NormageEngine.class);

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private String columnToBeAdded = "";

	private Connection connection;

	/**
	 * Caractéristique du fichier (idSource) issu du pilotage de ARC
	 * 
	 * idSource : nom du fichier jointure : requete de strucutration id_norme :
	 * identifiant de norme validite : validite periodicite : periodicite
	 */
	private HashMap<String, ArrayList<String>> pilotageIdSource;

	/**
	 * Les regles relatives au fichier (idSource)
	 * 
	 * id_norme text, periodicite text, validite_inf date, validite_sup date,
	 * version text, id_classe text, rubrique text, rubrique_nmcl text, id_regle
	 * integer,
	 */
	private HashMap<String, ArrayList<String>> regleInitiale;

	/**
	 * liste des rubriques présentes dans le fichier idSource et réutilisées dans
	 * les phase en aval Ces rubriques "var" sont à conserver id_norme,
	 * validite_inf, validite_sup, periodicite, var
	 */
	private HashMap<String, ArrayList<String>> rubriqueUtiliseeDansRegles;

	private String tableSource;

	private String tableDestination;

	private String paramBatch;

	public String structure;

	public NormageEngine(Connection connection, HashMap<String, ArrayList<String>> pil,
			HashMap<String, ArrayList<String>> regle, HashMap<String, ArrayList<String>> rubriqueUtiliseeDansRegles,
			String tableSource, String tableDestination, String paramBatch) {
		super();
		this.connection = connection;
		this.pilotageIdSource = pil;
		this.regleInitiale = regle;
		this.rubriqueUtiliseeDansRegles = rubriqueUtiliseeDansRegles;
		this.tableSource = tableSource;
		this.tableDestination = tableDestination;
		this.paramBatch = paramBatch;
	}

	public void executeEngine() throws ArcException {
		execute();
	}

	public void execute() throws ArcException {

		String jointure = pilotageIdSource.get("jointure").get(0);

		if (jointure == null || jointure.equals("")) {
			simpleExecutionWithNoJoinDefined();
		} else {
			complexExecutionWithJoinDefined(jointure);
		}

	}

	/**
	 * This method execute the basic engine with no xml join set
	 * 
	 * @throws ArcException
	 */
	private void simpleExecutionWithNoJoinDefined() throws ArcException {
		StringBuilder reqSelect = new StringBuilder();

		reqSelect.append(ColumnEnum.ID_SOURCE.getColumnName()+", id, date_integration, id_norme, validite, periodicite");

		List<String> listeRubriqueSource = new ArrayList<>();
		UtilitaireDao.get("arc").getColumns(connection, listeRubriqueSource, tableSource);

		HashSet<String> alreadyAdded = new HashSet<>();

		for (String variable : listeRubriqueSource) {
			// pour toutes les variables du fichier commencant par i ou v et qui n'ont pas
			// déjà été ajoutée à la requete
			boolean isVariableAlreadyProcessed = ((variable.startsWith("v_") || variable.startsWith("i_"))
					&& !alreadyAdded.contains(variable));

			// si on est pas en batch (this.paramBatch==null), on garde toutes les colonnes
			// si on est en batch (this.paramBatch!=null), on droit retrouver la rubrique
			// dans les regles pour la conserver
			boolean isVariableUsedInAllRules = (paramBatch == null
					|| (paramBatch != null && rubriqueUtiliseeDansRegles.get("var").contains(variable)));

			if (isVariableAlreadyProcessed && isVariableUsedInAllRules) {
				// on cherche à insérer le duo (i_,v_)
				// on sécurise l'insertion en regardant si la variable existe bien dans la liste
				// des rubrique du fichier source : parfois il n'y a qu'un i_ et pas de v_

				if (listeRubriqueSource.contains("i_" + getCoreVariableName(variable))) {
					alreadyAdded.add("i_" + getCoreVariableName(variable));
					reqSelect.append(", i_" + getCoreVariableName(variable));
				}

				if (listeRubriqueSource.contains("v_" + getCoreVariableName(variable))) {
					alreadyAdded.add("v_" + getCoreVariableName(variable));
					reqSelect.append(", v_" + getCoreVariableName(variable));
				}
			}
		}

		StringBuilder bloc3 = new StringBuilder();
		bloc3.append("\n CREATE TEMPORARY TABLE " + tableDestination + " ");
		bloc3.append("\n  AS SELECT ");
		bloc3.append(reqSelect);
		bloc3.append(" FROM " + tableSource + " ;");

		UtilitaireDao.get("arc").executeImmediate(connection, bloc3);
	}

	/**
	 * This method execute the engine with a join defined the join will be modified
	 * according to the user rules
	 * 
	 * @param jointure
	 * @throws ArcException
	 */
	private void complexExecutionWithJoinDefined(String jointure) throws ArcException {
		// variables locales
		String idSource = pilotageIdSource.get(ColumnEnum.ID_SOURCE.getColumnName()).get(0);
		String norme = pilotageIdSource.get("id_norme").get(0);
		Date validite;
		try {
			validite = this.formatter.parse(pilotageIdSource.get("validite").get(0));
		} catch (ParseException e) {
			throw new ArcException("the validite field is not parsable as date",e);
		}
		String periodicite = pilotageIdSource.get("periodicite").get(0);
		String validiteText = pilotageIdSource.get("validite").get(0);

		// split structure blocks
		String[] ss = jointure.split(XMLComplexeHandlerCharger.JOINXML_STRUCTURE_BLOCK);

		if (ss.length > 1) {
			jointure = ss[0];
			this.structure = ss[1];
		}

		// split query blocks
		int subJoinNumber = 0;
		for (String subJoin : jointure.split(XMLComplexeHandlerCharger.JOINXML_QUERY_BLOCK)) {

			HashMap<String, ArrayList<String>> regle = new HashMap<>();

			for (String key : regleInitiale.keySet()) {
				ArrayList<String> al = new ArrayList<>();
				for (String val : regleInitiale.get(key)) {
					al.add(val);
				}
				regle.put(key, al);
			}

			subJoin = subJoin.toLowerCase();

			// ORDRE IMPORTANT
			// on supprime les rubriques inutilisées quand le service est invoqué en batch
			// (this.paramBatch!=null)
			// i.e. en ihm (this.paramBatch==null) on garde toutes les rubriques
			// pour que les gens qui testent en bac à sable n'aient pas de probleme
			if (paramBatch != null) {
				NormageEngineRegleSupression.ajouterRegleSuppression(regle, norme, validite, periodicite, subJoin, rubriqueUtiliseeDansRegles);

				subJoin = NormageEngineRegleSupression.appliquerRegleSuppression(regle, norme, validite, periodicite, subJoin);
			}

			ajouterRegleDuplication(regle, norme, validite, periodicite, subJoin);

			subJoin = appliquerRegleDuplication(regle, norme, validite, periodicite, subJoin);

			ajouterRegleIndependance(regle, norme, validite, periodicite, subJoin);

			subJoin = appliquerRegleIndependance(regle, norme, validite, periodicite, subJoin);

			subJoin = appliquerRegleUnicite(regle, norme, validite, periodicite, subJoin);

			subJoin = appliquerRegleRelation(regle, norme, validite, periodicite, subJoin);

			// retravaille de la requete pour éliminer UNION ALL
			subJoin = optimisation96(subJoin, subJoinNumber);

			executerJointure(regle, norme, validite, periodicite, subJoin, validiteText, idSource);

			subJoinNumber++;

		}
	}

	private String optimisation96(String jointure, int subjoinNumber) {
		StaticLoggerDispatcher.info("optimisation96()", LOGGER);

		// on enleve l'id
		String r = jointure;
		r = " \n " + r;

		boolean blocInsert = false;
		boolean fieldsToBeInsertedFound = false;

		String[] lines = r.split("\n");
		String insert = null;

		r = "";
		for (int i = 0; i < lines.length; i++) {
			// on garde l'insert
			if (lines[i].contains("insert into {table_destination}")) {
				insert = ";\n" + lines[i];
				if (!lines[i].contains(")")) {
					insert = insert + ")";
				}
				if (!fieldsToBeInsertedFound) {
					fieldsToBeInsertedFound = true;
				}
			}

			if (insert != null && lines[i].contains("UNION ALL")) {
				lines[i] = insert;
			}

			if (blocInsert) {
				lines[i] = lines[i].replace(" select ",
						" select row_number() over () + (select count(*) from {table_destination}),");
			}

			if (lines[i].contains("row_number() over (), ww.*")) {
				lines[i] = "";
				blocInsert = true;

			}

			// on retire l'alias sur les deniere lignes
			if (i == lines.length - 1 || i == lines.length - 2) {
				lines[i] = lines[i].replace(") ww", ";");
			}

			r = r + lines[i] + "\n";

		}

		StringBuilder analyze = new StringBuilder();

		Pattern p = Pattern.compile("create temporary table ([^ ]+) as ");
		Matcher m = p.matcher(jointure);
		while (m.find()) {
			analyze.append("\n analyze " + m.group(1) + ";");
		}

		// on intègre le calcul des statistiques des tables utilisées
		r = ManipString.substringBeforeFirst(r, "insert into {table_destination}") + analyze
				+ "\n insert into {table_destination}"
				+ ManipString.substringAfterFirst(r, "insert into {table_destination}");

		if (subjoinNumber > 0) {
			// on recrée les tables temporaires
			r = r.replaceAll("create temporary table ([^ ]+) as ",
					"drop table if exists $1; create temporary table $1 as ");
		} else {

			// on crée la table destination avec les bonnes colonnes pour la premiere sous
			// jointure
			r = "drop table if exists {table_destination}; create temporary table {table_destination} as SELECT * FROM {table_source} where false; \n "
					+ this.columnToBeAdded + "\n" + r;
		}
		return r;
	}

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
	private void ajouterRegleDuplication(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) throws ArcException {
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

	private String appliquerRegleDuplication(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) throws ArcException {

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
				String rubriqueM = getM(blocCreate, rubrique);

				// si on trouve une rubrique mère
				if (rubriqueM != null) {
					ArrayList<String> aTraiter = getChildrenTree(blocCreate, rubriqueM);
					aTraiter.add(0, rubriqueM);

					StringBuilder blocCreateNew = new StringBuilder();
					StringBuilder blocInsertNew = new StringBuilder();
					HashSet<String> colonnesAAjouter = new HashSet<String>();

					String[] lines = blocCreate.split("\n");
					for (int i = 0; i < lines.length; i++) {
						String rubriqueLine = getM(lines[i]);
						String pereLine = getFather(lines[i]);
						String tableLine = getTable(lines[i]);

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
							c = mToI(c);

							if (!blocCreate.contains(" add " + c + " ")
									&& !blocCreateNew.toString().contains(" add " + c + " ")) {
								if (c.startsWith("i_")) {
									String addCol = "do $$ begin alter table {table_destination} add " + c
											+ " integer; exception when others then end; $$;";
									// blocCreateNew.insert(0,addCol);
									if (!this.columnToBeAdded.contains(addCol)) {
										this.columnToBeAdded = this.columnToBeAdded + addCol;
									}
								} else {
									String addCol = "do $$ begin alter table {table_destination} add " + c
											+ " text; exception when others then end; $$;";
									// blocCreateNew.insert(0,addCol);
									if (!this.columnToBeAdded.contains(addCol)) {
										this.columnToBeAdded = this.columnToBeAdded + addCol;
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
								c = mToI(c);
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
								if (lines[i].contains(" t_" + getCoreVariableName(aTraiter.get(k)) + " ")) {
									// remplacement du nom de la table
									String newLine = lines[i]
											.replace(" t_" + getCoreVariableName(aTraiter.get(k)) + " ",
													" t_" + getCoreVariableName(aTraiter.get(k)) + "_" + alias + " ")
											.replace("=t_" + getCoreVariableName(aTraiter.get(k)) + ".",
													"=t_" + getCoreVariableName(aTraiter.get(k)) + "_" + alias + ".");
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

	/**
	 * On va parse la jointure ; on rend les frères indépendants On exclus de
	 * l'indépendance le pere commun de frere relié par une relation
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @throws ArcException
	 */
	private void ajouterRegleIndependance(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) throws ArcException {
		StaticLoggerDispatcher.info("ajouterRegleIndependance()", LOGGER);

		String blocCreate = ManipString.substringBeforeFirst(jointure, "insert into {table_destination}");
		HashSet<String> rubriqueExclusion = new HashSet<String>();
		HashMap<String, String> rubriquesAvecRegleDIndependance = new HashMap<>();

		// pour toutes les règles de relation,
		for (int j = 0; j < regle.get("id_regle").size(); j++) {

			String type = regle.get("id_classe").get(j);

			// cas 1 on met en indépendant les règles en relation
			// en gros on parcours les regles de relation
			// on va exclure les bloc en relation des blocs à calculer comme indépendants
			if (type.equals("relation")) {
				String rubrique = regle.get("rubrique").get(j).toLowerCase();
				String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();

				String rubriqueM = getM(blocCreate, rubrique);
				String rubriqueNmclM = getM(blocCreate, rubriqueNmcl);

				if (rubriqueM != null && rubriqueNmclM != null) {
					ArrayList<String> parentM = getParentsTree(blocCreate, rubriqueM, true);
					ArrayList<String> parentNmclM = getParentsTree(blocCreate, rubriqueNmclM, true);

					// on exclus tout les blocs présent dans les listes jusqu'a ce qu'on trouve
					// l'élément en commun
					String pppc = pppc(blocCreate, rubriqueM, rubriqueNmclM);

					for (int i = 0; i < parentM.indexOf(pppc); i++) {
						rubriqueExclusion.add(parentM.get(i));
					}

					for (int i = 0; i < parentNmclM.indexOf(pppc); i++) {
						rubriqueExclusion.add(parentNmclM.get(i));
					}
				}
			}

			// cas 3 : deux rubrique déclarées en cartesian
			// les rubriques déclarées en cartésian ne peuvent intégrer un groupe de
			// rubrique indépendante
			if (type.equals("cartesian")) {
				String rubrique = regle.get("rubrique").get(j).toLowerCase();
				String rubriqueM = getM(blocCreate, rubrique);
				rubriqueExclusion.add(rubriqueM);
			}

			if (type.equals("independance")) {
				String rubrique = regle.get("rubrique").get(j).toLowerCase();
				String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();
				rubriquesAvecRegleDIndependance.put(anyToM(rubrique), rubriqueNmcl);
			}

		}

		// rubriques which had been declared explicitly with independance rules are
		// remove from exclusion
		for (String k : rubriquesAvecRegleDIndependance.keySet()) {
			rubriqueExclusion.remove(k);
		}

		// ARC compute which rubriques are independant and set the independance rules
		ArrayList<String> r = new ArrayList<>();
		addIndependanceToChildren(r, blocCreate, getM(blocCreate), regle, rubriquesAvecRegleDIndependance, norme,
				periodicite, rubriqueExclusion);

	}


	
	
	/**
	 * Modifie la requete pour appliquer les regles d'indépendance
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @return
	 * @throws ArcException
	 */
	private String appliquerRegleIndependance(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) throws ArcException {

		StaticLoggerDispatcher.info("appliquerRegleIndependance()", LOGGER);

		String returned = jointure;

		String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");
		String blocInsert = " insert into {table_destination} "
				+ ManipString.substringAfterFirst(returned, "insert into {table_destination} ");

		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);

			if (type.equals("bloc_independance")) {

				String rubriqueRegle[] = regle.get("rubrique").get(j).replace(" ", "").toLowerCase().split(",");
				String rubriqueNmclRegle[] = regle.get("rubrique_nmcl").get(j).replace(" ", "").toLowerCase()
						.split(",");

				ArrayList<String> rubrique = new ArrayList<String>();
				HashMap<String, String> rubriqueNmcl = new HashMap<>();

				// ne garder que les rubriques qui existent dans la requete
				// vérifier qu'elles ont le même pere
				String fatherSav = null;
				for (int i = 0; i < rubriqueRegle.length; i++) {
					if (blocCreate.contains(" " + rubriqueRegle[i] + " ")) {
						String m_rubrique = "m_" + getCoreVariableName(rubriqueRegle[i]);

						// si on trouve la rubrique mais qu'elle n'est pas identifiant de bloc (m_), on
						// sort
						if (!blocCreate.contains(m_rubrique)) {
							StaticLoggerDispatcher.info("La rubrique " + rubriqueRegle[i] + " n'identifie pas un bloc",
									LOGGER);
							throw new ArcException("La rubrique " + rubriqueRegle[i] + " n'identifie pas un bloc");
						}

						rubriqueRegle[i] = m_rubrique;
						rubrique.add(rubriqueRegle[i]);
						rubriqueNmcl.put(rubriqueRegle[i], rubriqueNmclRegle[i]);

						if (fatherSav == null) {
							fatherSav = getFatherM(blocCreate, rubriqueRegle[i]);
						} else {
							if (!fatherSav.equals(getFatherM(blocCreate, rubriqueRegle[i]))) {
								StaticLoggerDispatcher.info(
										"La rubrique " + rubriqueRegle[i] + " n'a pas le même pere que les autres",
										LOGGER);
								throw new ArcException(
										"La rubrique " + rubriqueRegle[i] + " n'a pas le même pere que les autres");
							}
						}

					}
				}

				// si 0 ou 1 rubrique, on sort : pas besoin de calculer l'indépendance
				if (rubrique.size() > 1) {
					// modifier le blocCreate

					// 1 ajouter un $ a la fin du nom des table concernée par l'indépendance et le
					// calcul du rang
					String[] lines = blocCreate.split("\n");

					HashMap<String, String> table = new HashMap<String, String>();
					HashMap<String, String> pere = new HashMap<String, String>();
					HashMap<String, String> autreCol = new HashMap<String, String>();

					StringBuilder blocCreateNew = new StringBuilder();
					StringBuilder blocInsertNew = new StringBuilder();

					for (int i = 0; i < lines.length; i++) {
						boolean changed = false;

						for (String r : rubrique) {
							if (testRubriqueInCreate(lines[i], r)) {
								table.put(r, getTable(lines[i]));
								pere.put(r, getFather(lines[i]));
								autreCol.put(r, getOthers(lines[i]));

								// on met le "$" au nom de la table
								blocCreateNew.append(lines[i].replace(" as (select ", "$ as (select ") + "\n");

								// on saute une ligne : on ne veut pas garder la table null
								i++;

								changed = true;
							}
						}

						if (!changed) {
							blocCreateNew.append(lines[i] + "\n");
						}
					}

					boolean isThereAnyValue = calculerTableIndependance(blocCreateNew, false, rubrique, rubriqueNmcl,
							table, pere, autreCol);

					// calculer la table _null pour les jointures
					// c'est plus compliqué si y'a des rubriques avec valeurs car il faut mettre
					// seulement les blocs avec valeurs à null

					if (isThereAnyValue) {
						calculerTableIndependance(blocCreateNew, true, rubrique, rubriqueNmcl, table, pere, autreCol);
					} else {
						blocCreateNew.append("create temporary table " + table.get(rubrique.get(0))
								+ "_null as (select * from " + table.get(rubrique.get(0)) + " where false);\n");
					}

					blocCreate = blocCreateNew.toString();

					// ne reste plus qu'a retirer les conditions de jointure sur les tables
					// supprimées

//                         System.out.println("#"+blocInsert);

					String[] linesI = blocInsert.split("\n");
					for (int i = 0; i < linesI.length; i++) {
						boolean insert = true;
						for (int k = 1; k < rubrique.size(); k++) {
							if (linesI[i].contains(" " + table.get(rubrique.get(k)) + " ")) {
								insert = false;
							}

						}

						if (insert) {
							blocInsertNew.append(linesI[i] + "\n");
						}

					}

					blocInsert = blocInsertNew.toString();

				}
			}

		}

		returned = blocCreate.replaceAll("\n$", "") + "\n" + blocInsert.replaceAll("\n$", "");

		return returned;

	}

	/**
	 * Method that calculate the query to create the tables for independant rules
	 * The goal is to put the lines of independant block front to front For
	 * rubriques with as a value declared in rubriqueNcml, the calculation must
	 * respect the relationship between this values
	 * 
	 * @param blocRequete
	 * @param nullTableRequired
	 * @param rubrique
	 * @param rubriqueNmcl
	 * @param table
	 * @param pere
	 * @param autreCol
	 * @return
	 */
	private boolean calculerTableIndependance(StringBuilder blocRequete, boolean nullTableRequired,
			ArrayList<String> rubrique, HashMap<String, String> rubriqueNmcl, HashMap<String, String> table,
			HashMap<String, String> pere, HashMap<String, String> autreCol) {
		boolean isThereAnyValue = false;

		blocRequete.append(
				"create temporary table " + table.get(rubrique.get(0)) + (nullTableRequired ? "_null" : "") + " as ( ");
		// bloc des valeurs : on met tous les identifiants et valeurs ensemble
		blocRequete.append("with tmp0_value as ( ");
		boolean first = true;
		// les colonnes identifiantes m_
		for (String r0 : rubrique) {
			if (!first) {
				blocRequete.append("union all");
			}

			boolean rubriqueHasNoValue = rubriqueNmcl.get(r0).equals("null");
			// register there is a value declared for null table calculation
			if (!rubriqueHasNoValue) {
				isThereAnyValue = true;
			}
			String rubriqueHasNoValueSQL = rubriqueHasNoValue ? "false as hasValue" : "true as hasValue";

			blocRequete.append(" select " + rubriqueHasNoValueSQL + ", '" + table.get(r0) + "' as u ");
			blocRequete.append(" ," + pere.get(r0) + " as id_pere ");
			blocRequete.append(" ," + rubriqueNmcl.get(r0) + "::text as v ");
			blocRequete.append(" ," + r0 + " as i ");
			blocRequete.append(" from " + table.get(r0) + "$ ");
			if (!rubriqueHasNoValue && nullTableRequired) {
				blocRequete.append(" where false ");
			}
			first = false;
		}
		blocRequete.append(" ) ");

		// bloc des valeurs distinct de valeurs
		blocRequete.append(", tmp0_distinct_value as (select distinct id_pere, v from tmp0_value where hasValue) ");

		//
		blocRequete.append(", tmp0 as ( ");
		blocRequete.append(" select u, id_pere, v, i from tmp0_value where hasValue ");
		blocRequete.append(" union all ");
		blocRequete.append(
				" select a.u, a.id_pere, b.v, a.i from tmp0_value a left join tmp0_distinct_value b on a.id_pere=b.id_pere ");
		blocRequete.append(" where not hasValue ");
		blocRequete.append(" ) ");

		//
		blocRequete.append(" , tmp1 as ( ");
		blocRequete.append(" select row_number() over (partition by id_pere, u, v) as r ");
		blocRequete.append(" , case when v is null then min(v) over (partition by id_pere) else v end as v2 ");
		blocRequete.append(" ,* ");
		blocRequete.append(" from tmp0 ");
		blocRequete.append(" ) ");

		blocRequete.append(" , tmp3 as ( select id_pere, r, v2");
		for (String r0 : rubrique) {
			blocRequete.append(" ,max(case when u='" + table.get(r0) + "' then i else null end) as " + r0 + " ");
		}
		blocRequete.append(" from tmp1 ");
		blocRequete.append(" group by id_pere, r, v2 ");
		blocRequete.append(" ) ");

		blocRequete.append(" , tmp4 as ( select a.id_pere");
		for (String r0 : rubrique) {
			blocRequete.append(" , coalesce(a." + r0 + ", b." + r0 + ") as " + r0 + " ");
		}
		blocRequete.append(" from tmp3 a, tmp3 b");
		blocRequete.append(" where a.id_pere=b.id_pere and row(a.v2)::text=row(b.v2)::text and b.r=1");
		blocRequete.append(" ) ");

		blocRequete.append(" select ");
		first = true;
		for (String r0 : rubrique) {
			if (!first) {
				blocRequete.append(",");
			}
			blocRequete.append(" " + r0 + " as " + r0 + " ");
			first = false;
		}
		blocRequete.append(", id_pere as " + pere.get(rubrique.get(0)) + " ");

		for (String r0 : rubrique) {
			String pattern = " as ([^ ()]*) ";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(autreCol.get(r0));
			while (m.find()) {
				blocRequete.append(", " + m.group(1) + " as " + m.group(1) + " ");
			}
		}

		blocRequete.append(" from tmp4 a ");

		int wi;
		wi = 1;
		for (String r0 : rubrique) {
			blocRequete.append(" left join lateral ( ");
			blocRequete.append(" select ");

			String pattern = " as ([^ ()]*) ";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(autreCol.get(r0));

			first = true;
			while (m.find()) {
				if (!first) {
					blocRequete.append(",");
				}
				blocRequete.append(" " + m.group(1) + " as " + m.group(1) + " ");
				first = false;
			}
			blocRequete.append(" from " + table.get(r0) + "$ b ");
			blocRequete.append(" where a." + r0 + "=b." + r0 + " and a.id_pere=b." + pere.get(r0) + " ");
			blocRequete.append(" ) w" + wi + " on true ");
			wi++;
		}
		blocRequete.append(") ;\n");
		return isThereAnyValue;
	}

	/**
	 * Modifie la requete pour appliquer les regles d'unicite
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @return
	 * @throws ArcException
	 */
	private String appliquerRegleUnicite(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) throws ArcException {
		StaticLoggerDispatcher.info("appliquerRegleUnicite()", LOGGER);

		String returned = jointure;
		// extraction de la clause select

		String viewAndInsert = ManipString.substringBeforeLast(returned, " select ");
		String selectBase = " select " + ManipString.substringAfterLast(returned, " select ");

		String[] lines = returned.split("\n");
		int max = lines.length - 1;

		// on parcourt maintenant les regles d'unicité
		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);
			if (type.equals("unicité")) {

				String rubrique = regle.get("rubrique").get(j).toLowerCase();

				// StaticLoggerDispatcher.info("Filtrage relationnel : "+rubrique+" -
				// "+rubriqueNmcl,
				// logger);

				// vérifier l'existance des rubriques
				if (returned.contains(" " + rubrique + " ")) {

					// parcourir les ligne pour trouver la table correpondant à la rubrique
					int k = 1;
					while (k <= max) {
						String line = lines[k];

						if (line.startsWith(" ")) {
							break;
						}

						if (!line.startsWith("insert ") && !line.contains("$ as (select ")
								&& testRubriqueInCreate(line, rubrique)) {
							// récupère l'identifiant pere du block (c'est l'identifiant juste aprés celui
							// du block m_...)
							String idBlock = getFather(line);

							// déclarer une colonne en nomenclature indique en quelque sorte que c'est une
							// clé dans son groupe :
							// on garde donc des valeurs uniques dans le groupe (pour un meme pere, une
							// seule valeur)
							// et non null (si c'est null, la jointure sera false de toutes façons)
							// si la colonne nomenclature n'a pas déjà été traitée, on fait ce traitement
							// d'unicité
							if (!testRubriqueInCreate(lines[k], "rk_" + rubrique)) {
								lines[k] = ManipString.substringBeforeFirst(line, " (")
										+ " (select * from    (select case when " + rubrique
										+ " is null then 1 else row_number() over (partition by " + idBlock + ","
										+ rubrique + ") end as rk_" + rubrique + " , * from    ("
										+ ManipString.substringAfterFirst(line.replace(";", ""), " (") + " t0_"
										+ rubrique + " ) t1_" + rubrique + " where rk_" + rubrique + "=1);";
							}

							break;
						}

						k++;
					}
				}
			}
		}

		viewAndInsert = "";
		for (int k = 0; k < lines.length; k++) {
			viewAndInsert += lines[k] + "\n";
		}
		viewAndInsert = ManipString.substringBeforeLast(viewAndInsert, " select ");

		returned = viewAndInsert + selectBase;

		return returned;
	}

	/**
	 * Modifie la requete pour appliquer les regles de relation
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @return
	 * @throws ArcException
	 */
	private String appliquerRegleRelation(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure) throws ArcException {

		StaticLoggerDispatcher.info("appliquerRegleRelation()", LOGGER);

		String returned = jointure;
		// extraction de la clause select
		String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");

		String viewAndInsert = ManipString.substringBeforeLast(returned, " select ");
		String selectBase = " select " + ManipString.substringAfterLast(returned, " select ");
		String finBase = "\n)" + ManipString.substringAfterLast(selectBase, ")");
		selectBase = ManipString.substringBeforeLast(selectBase, ")");

		String[] lines = returned.split("\n");
		int max = lines.length - 1;

		// on parcourt maintenant les regles de relation
		ArrayList<String> listRubrique = new ArrayList<String>();
		ArrayList<String> listRubriqueNmcl = new ArrayList<String>();
		ArrayList<String> listTableNmcl = new ArrayList<String>();

		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);
			if (type.equals("relation")) {

				String rubrique = regle.get("rubrique").get(j).toLowerCase();
				String rubriqueNmcl = regle.get("rubrique_nmcl").get(j).toLowerCase();

				// StaticLoggerDispatcher.info("Filtrage relationnel : "+rubrique+" -
				// "+rubriqueNmcl,
				// logger);

				// cérifier l'existance des rubriques
				if (returned.contains(" " + rubriqueNmcl + " ") && returned.contains(" " + rubrique + " ")) {
					listRubrique.add(rubrique);
					listRubriqueNmcl.add(rubriqueNmcl);

					// parcourir les ligne pour trouver la table correpondant à la rubriqueNcml
					int k = 1;
					while (k <= max) {
						String line = lines[k];

						if (line.startsWith(" ")) {
							break;
						}

						if (!line.startsWith("insert ") && !line.contains("$ as (select ")
								&& testRubriqueInCreate(line, rubriqueNmcl)) {
							// extraction du nom de la table
							listTableNmcl.add(getTable(line));

							break;
						}

						k++;
					}
				}
			}
		}

		// retravailler le viewAndInsert
		// la valeur nomenclature est une clé; prendre le premier enregistrement pour
		// chaque clé
		// on reecrit les with en create temporary table (problème de mémoire) (on fait
		// donc sauter la premiere ligne qui est juste un placeholder)
		// on ajoute une premiere ligne : discard temp

		viewAndInsert = "";
		for (int k = 0; k < lines.length; k++) {
			viewAndInsert += lines[k] + "\n";
		}

		viewAndInsert = ManipString.substringBeforeLast(viewAndInsert, " select ");

		// faire toutes les compositions de jointure possible
		// compter en binaire
		String select;
		int r;

		for (int k = 0; k < Math.pow(2, listRubrique.size()); k++) {
			if (k > 0) {
				viewAndInsert = viewAndInsert + "\n UNION ALL \n";
			}
			select = selectBase;

			// on le fait en deux passages; traiter les r=1 (pas de lien) d'abord puis les
			// r=0 (lien trouvé)
			// pour traiter le cas de test suivant : si s40 est lié à s51 et s40 n'est pas
			// lié à s52
			// alors on garde s40
			int z = k;
			for (int l = 0; l < listRubrique.size(); l++) {
				r = z % 2;
				z = (z - r) / 2;

				if (r == 1) {
					// cas 2 : jointure non trouvée : la rubrique est null ou la rubrique n'a aucune
					// correspondace dans la table de nomenclature
					// on substitue la vue de la rubrique nomenclature à la rubrique vide
					// on ajoute la clause de jointure
					select = select.replace(" " + listTableNmcl.get(l) + " ", " " + listTableNmcl.get(l) + "_null ")
							.replace("=" + listTableNmcl.get(l) + ".", "=" + listTableNmcl.get(l) + "_null.");
					select = select + "\n AND NOT EXISTS (select 1 from (select distinct " + listRubriqueNmcl.get(l)
							+ " as g_rub," + getFather(getLine(blocCreate, listRubriqueNmcl.get(l)))
							+ " as g_pere from " + getTable(blocCreate, listRubriqueNmcl.get(l)) + ") xx where "
							+ listRubrique.get(l) + "=g_rub and "
							+ iToM(getFather(getLine(blocCreate, listRubriqueNmcl.get(l)))) + "=g_pere) ";

				}
			}

			z = k;
			for (int l = 0; l < listRubrique.size(); l++) {
				r = z % 2;
				z = (z - r) / 2;

				if (r == 0) {
					// cas 1 : jointure trouvée
					// on substitue la vue de la rubrique vide à la rubrique nomenclature
					// (on défait ce qu'on a éventuellement fait en r=1)
					// on ajoute la clause de jointure
					select = select.replace(" " + listTableNmcl.get(l) + "_null ", " " + listTableNmcl.get(l) + " ")
							.replace("=" + listTableNmcl.get(l) + "_null.", "=" + listTableNmcl.get(l) + ".");
					select = select + "\n AND " + listRubrique.get(l) + "=" + listRubriqueNmcl.get(l) + " ";
				}
			}

			viewAndInsert = viewAndInsert + select;

		}

		returned = viewAndInsert + finBase;

		return returned;

	}

	private Integer excludeFileonTimeOut(HashMap<String, ArrayList<String>> regle) {
		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);
			if (type.equals("exclusion")) {
				return Integer.parseInt(regle.get("rubrique").get(j));
			}
		}
		return null;
	}

	private String applyQueryPlanParametersOnJointure(String query, Integer statementTimeOut) {
		return applyQueryPlanParametersOnJointure(new ArcPreparedStatementBuilder(query), statementTimeOut).getQuery()
				.toString();
	}

	private ArcPreparedStatementBuilder applyQueryPlanParametersOnJointure(ArcPreparedStatementBuilder query,
			Integer statementTimeOut) {
		ArcPreparedStatementBuilder r = new ArcPreparedStatementBuilder();
		// seqscan on can be detrimental on some rare large file
		r.append("set enable_nestloop=off;\n");
		r.append((statementTimeOut == null) ? "" : "set statement_timeout=" + statementTimeOut.toString() + ";\n");
		r.append("commit;");
		r.append(query);
		r.append((statementTimeOut == null) ? "" : "reset statement_timeout;");
		r.append("set enable_nestloop=on;\n");

		r.setQuery(new StringBuilder(r.getQuery().toString().replace(" insert into ", "commit; insert into ")));
		return r;
	}

	/**
	 * execute query with partition if needed
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @param validiteText
	 * @param idSource
	 * @return
	 * @throws ArcException
	 */
	private void executerJointure(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure, String validiteText, String idSource) throws ArcException {

		// only first partition rule is processed
		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);
			if (type.equals("partition")) {

				String element = regle.get("rubrique").get(j);
				int minSize = Integer.parseInt(regle.get("rubrique_nmcl").get(j).split(",")[0]);
				int chunkSize = Integer.parseInt(regle.get("rubrique_nmcl").get(j).split(",")[1]);
				executerJointureWithPartition(regle, norme, validite, periodicite, jointure, validiteText, idSource,
						element, minSize, chunkSize);
				return;
			}
		}

		// No partition found; normal execution
		UtilitaireDao.get("arc").executeImmediate(connection, applyQueryPlanParametersOnJointure(
				replaceQueryParameters(jointure, norme, validite, periodicite, jointure, validiteText, idSource),
				null));

	}

	/**
	 * execute the query through the partition rubrique @element if their is enough
	 * records @minSize the query is executed by part, the number max of records is
	 * set by @chunkSize
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @param validiteText
	 * @param idSource
	 * @param element
	 * @param minSize
	 * @param chunkSize
	 * @throws ArcException
	 */
	private void executerJointureWithPartition(HashMap<String, ArrayList<String>> regle, String norme, Date validite,
			String periodicite, String jointure, String validiteText, String idSource, String element, int minSize,
			int chunkSize) throws ArcException {
		/* get the query blocks */
		String blocCreate = ManipString.substringBeforeFirst(jointure, "\n insert into {table_destination} ");
		String blocInsert = "\n insert into {table_destination} "
				+ ManipString.substringAfterFirst(jointure, "\n insert into {table_destination} ");

		// rework create block to get the number of record in partition if the rubrique
		// is found

		// watch out the independance rules which can put the partition rubrique in
		// another table than t_rubrique
		String partitionTableName = "";
		String partitionIdentifier = " m_" + element + " ";

		Integer statementTimeOut = excludeFileonTimeOut(regle);

		if (blocCreate.contains(partitionIdentifier)) {
			// get the tablename
			partitionTableName = ManipString.substringBeforeFirst(
					ManipString.substringAfterLast(ManipString.substringBeforeLast(blocCreate, partitionIdentifier),
							"create temporary table "),
					" as ");
			blocCreate = blocCreate + "select max(" + partitionIdentifier + ") from " + partitionTableName;
		} else {
			blocCreate = blocCreate + "select 0";
		}

		blocCreate = replaceQueryParameters(blocCreate, norme, validite, periodicite, jointure, validiteText,
				idSource);
		blocInsert = replaceQueryParameters(blocInsert, norme, validite, periodicite, jointure, validiteText,
				idSource);

		int total = UtilitaireDao.get("arc").getInt(connection, new ArcPreparedStatementBuilder(blocCreate));

		// partition if and only if enough records
		if (total >= minSize) {

			String partitionTableNameWithAllRecords = "all_" + partitionTableName;

			// rename the table to split
			StringBuilder bloc3 = new StringBuilder(
					"alter table " + partitionTableName + " rename to " + partitionTableNameWithAllRecords + ";");
			UtilitaireDao.get("arc").executeImmediate(connection, bloc3);

			ArcPreparedStatementBuilder bloc4 = new ArcPreparedStatementBuilder();
			bloc4.append("\n drop table if exists " + partitionTableName + ";");
			bloc4.append("\n create temporary table " + partitionTableName + " as select * from "
					+ partitionTableNameWithAllRecords + " where " + partitionIdentifier + ">=?::int and "
					+ partitionIdentifier + "<?::int;");
			bloc4.append("\n analyze " + partitionTableName + ";");
			bloc4.append(blocInsert);

			bloc4 = applyQueryPlanParametersOnJointure(bloc4, statementTimeOut);

			// iterate through chunks
			int iterate = 1;
			do {

				bloc4.setParameters(Arrays.asList("" + (iterate), "" + (iterate + chunkSize)));

				UtilitaireDao.get("arc").executeRequest(connection, bloc4);

				iterate = iterate + chunkSize;

			} while (iterate <= total);

		} else
		// no partitions needed
		{
			UtilitaireDao.get("arc").executeImmediate(connection,
					applyQueryPlanParametersOnJointure(blocInsert, statementTimeOut));
		}
	}

	private String replaceQueryParameters(String query, String norme, Date validite, String periodicite,
			String jointure, String validiteText, String idSource) {
		return query.toString().replace("{table_source}", tableSource).replace("{table_destination}", tableDestination)
				.replace("{id_norme}", norme).replace("{validite}", validiteText).replace("{periodicite}", periodicite)
				.replace("{nom_fichier}", idSource);
	}

	/**
	 * determine le pere d'une rubrique "m_<***>" dans un bloc
	 * 
	 * @param bloc
	 * @param rubrique
	 * @return
	 */
	private String getFather(String bloc, String rubrique) {
		return ManipString
				.substringBeforeFirst(
						ManipString.substringAfterFirst(
								ManipString.substringBeforeFirst(
										ManipString.substringAfterFirst(bloc, " as " + rubrique + " "), " from ("),
								" as "),
						" ,")
				.trim();
	}

	/**
	 * determine le pere d'une rubrique "m_<***>" dans un bloc
	 * 
	 * @param bloc
	 * @param rubrique
	 * @return
	 */
	private String getFatherM(String bloc, String rubrique) {
		String r = getFather(bloc, rubrique);
		if (!r.equals("")) {
			r = "m_" + getCoreVariableName(r);
		} else {
			r = rubrique;
		}
		return r;

	}

	/**
	 * A partir du blocCreate, determine l'arbre vers une rubrique "m_<***>"
	 * 
	 * @param blocCreate
	 * @param mRubrique
	 * @return
	 */
	private ArrayList<String> getParentsTree(String blocCreate, String mRubrique, boolean... keep) {
		ArrayList<String> r = new ArrayList<String>();
		r.add(mRubrique);
		String s;

		while (!(s = getFatherM(blocCreate, r.get(r.size() - 1))).equals(r.get(r.size() - 1))) {
			r.add(s);
		}

		if (!(keep.length > 0 && keep[0])) {
			r.remove(0);
		}

		return r;

	}

	/**
	 * A partir du blocCreate, determine récursivement les enfant d'une rubrique
	 * "m_<***>"
	 * 
	 * @param blocCreate
	 * @param mRubrique
	 * @return
	 */
	private ArrayList<String> getChildrenTree(String blocCreate, String mRubrique) {
		ArrayList<String> s = new ArrayList<>();
		getChildrenTree(s, blocCreate, mRubrique);
		return s;
	}

	/**
	 * A partir du blocCreate, determine récursivement les enfant d'une rubrique
	 * "m_<***>"
	 * @param r
	 * @param blocCreate
	 * @param mRubrique
	 * @param regle
	 * @param rubriquesAvecRegleDIndependance
	 * @param norme
	 * @param periodicite
	 * @param exclusion
	 */
	private void addIndependanceToChildren(ArrayList<String> r, String blocCreate, String mRubrique,
			HashMap<String, ArrayList<String>> regle, HashMap<String, String> rubriquesAvecRegleDIndependance,
			String norme, String periodicite, HashSet<String> exclusion) {
		ArrayList<String> s = getChildren(blocCreate, mRubrique);

		if (s.isEmpty()) {
			return;
		} else {
			r.addAll(s);

			// si on a exclus le bloc, on ne le met pas dans la regle
			int nbRubriqueRetenue = 0;
			StringBuilder rubriqueContent = new StringBuilder();
			StringBuilder rubriqueNmclContent = new StringBuilder();

			for (String z : s) {
				if (!exclusion.contains(z)) {
					if (rubriqueContent.length() > 0) {
						rubriqueContent.append(",");
						rubriqueNmclContent.append(",");
					}
					rubriqueContent.append(z);
					rubriqueNmclContent.append(rubriquesAvecRegleDIndependance.get(z) == null ? "null"
							: rubriquesAvecRegleDIndependance.get(z));

					nbRubriqueRetenue++;
				}

			}

			// ne créer une regle que si y'a plus d'une rubrique retenue; sinon pas la peine
			if (nbRubriqueRetenue > 1) {
				regle.get("id_regle").add("G" + System.currentTimeMillis());
				regle.get("id_norme").add(norme);
				regle.get("periodicite").add(periodicite);
				regle.get("validite_inf").add("1900-01-01");
				regle.get("validite_sup").add("3000-01-01");
				regle.get("id_classe").add("bloc_independance");
				regle.get("rubrique").add(rubriqueContent.toString());
				regle.get("rubrique_nmcl").add(rubriqueNmclContent.toString());

			}

			for (String rub : s) {
				addIndependanceToChildren(r, blocCreate, rub, regle, rubriquesAvecRegleDIndependance, norme,
						periodicite, exclusion);
			}

		}

	}

	/**
	 * A partir du blocCreate, determine récursivement les enfant d'une rubrique
	 * "m_<***>"
	 * 
	 * @param r
	 * @param blocCreate
	 * @param mRubrique
	 */

	private void getChildrenTree(ArrayList<String> r, String blocCreate, String mRubrique) {
		ArrayList<String> s = getChildren(blocCreate, mRubrique);

		if (s.isEmpty()) {
			return;
		} else {
			r.addAll(s);

			for (String rub : s) {
				getChildrenTree(r, blocCreate, rub);
			}

		}

	}

	/**
	 * A partir du blocCreate, determine les enfants d'une rubrique "m_<***>"
	 * 
	 * @param blocCreate
	 * @param mRubrique
	 * @return
	 */
	private ArrayList<String> getChildren(String blocCreate, String mRubrique) {
		ArrayList<String> r = new ArrayList<String>();

		if (mRubrique == null) {
			return r;
		}

		String lines[] = blocCreate.split("\n");

		for (int i = 0; i < lines.length; i++) {

			if (!testRubriqueInCreate(lines[i], mRubrique)
					&& testRubriqueInCreate(lines[i], "i_" + getCoreVariableName(mRubrique))) {
				r.add("m_" + ManipString.substringBeforeFirst(ManipString.substringAfterFirst(lines[i], " as m_"), " ")
						.trim());
			}

		}
		return r;

	}

	/**
	 * variable in arc are in format i_CoreVariableName or v_CoreVariableName this
	 * function exract the core variable name
	 * 
	 * @param fullVariableName
	 * @return
	 */
	protected static String getCoreVariableName(String fullVariableName) {
		return fullVariableName.substring(2);
	}

	/**
	 * la rubrique identifiante <m_...> trouvée dans le bloc
	 * 
	 * @param blocCreate
	 * @return
	 */
	private String getM(String bloc) {
		return "m_" + ManipString.substringBeforeFirst(ManipString.substringAfterFirst(bloc, " as m_"), " ");
	}

	/**
	 * le pere du bloc
	 * 
	 * @param bloc
	 * @return
	 */
	private String getFather(String bloc) {
		return getFather(bloc, getM(bloc));
	}

	/**
	 * la table du bloc
	 * 
	 * @param bloc
	 * @return
	 */
	private String getTable(String bloc) {

		return ManipString.substringBeforeFirst(ManipString.substringAfterFirst(bloc, "create temporary table "),
				" as ");

	}

	/**
	 * les autres colonnes du blocs
	 * 
	 * @param bloc
	 * @return
	 */
	private String getOthers(String bloc) {
		String r = ManipString
				.substringBeforeFirst(ManipString.substringAfterFirst(bloc, " as " + getFather(bloc) + " "), " from (");
		if (r.startsWith(",")) {
			r = r.substring(1);
		}
		return r;
	}

	/**
	 * renvoie la ligne a laquelle la rubrique appartient
	 * 
	 * @param blocCreate
	 * @param rubrique
	 * @return
	 */
	private String getLine(String blocCreate, String rubrique) {
		String lines[] = blocCreate.split("\n");

		for (int i = 0; i < lines.length; i++) {

			if (testRubriqueInCreate(lines[i], rubrique)) {
				return lines[i];
			}
		}
		return null;
	}

	/**
	 * l'identifiant de la ligne a laquelle la rubrique appartient
	 * 
	 * @param blocCreate
	 * @param rubrique
	 * @return
	 */
	private String getM(String blocCreate, String rubrique) {
		String lines[] = blocCreate.split("\n");

		for (int i = 0; i < lines.length; i++) {

			if (testRubriqueInCreate(lines[i], rubrique)) {
				return getM(lines[i]);
			}
		}
		return null;
	}

	private String mToI(String rubrique) {
		if (rubrique.startsWith("m_")) {
			return "i_" + getCoreVariableName(rubrique);
		}
		return rubrique;
	}

	private String iToM(String rubrique) {
		if (rubrique.startsWith("i_")) {
			return "m_" + getCoreVariableName(rubrique);
		}
		return rubrique;
	}

	private String anyToM(String rubrique) {

		if (rubrique.startsWith("m_")) {
			return rubrique;
		}

		if (rubrique.startsWith("i_") || rubrique.startsWith("v_")) {
			return "m_" + getCoreVariableName(rubrique);
		}

		return "m_" + rubrique;
	}

	/**
	 * le nom de la table de la ligne a laquelle la rubrique appartient
	 * 
	 * @param blocCreate
	 * @param rubrique
	 * @return
	 */
	private String getTable(String blocCreate, String rubrique) {
		String lines[] = blocCreate.split("\n");

		for (int i = 0; i < lines.length; i++) {

			if (testRubriqueInCreate(lines[i], rubrique)) {
				return getTable(lines[i]);
			}
		}
		return null;
	}

	/**
	 * test si la rubrique est dans le bloc fourni
	 * 
	 * @param bloc
	 * @param rubrique
	 * @return
	 */
	private boolean testRubriqueInCreate(String bloc, String rubrique) {
		return ManipString.substringBeforeFirst(bloc, " from (").contains(" as " + rubrique + " ");
	}

	/**
	 * pppc = plus petit pere en commun renvoir le pppc
	 * 
	 * @param blocCreate
	 * @param rubriqueM
	 * @param rubriqueNmclM
	 * @return
	 */
	private String pppc(String blocCreate, String rubriqueM, String rubriqueNmclM) {

		ArrayList<String> rubriqueParents = getParentsTree(blocCreate, rubriqueM, true);
		ArrayList<String> rubriqueNmclParents = getParentsTree(blocCreate, rubriqueNmclM, true);

		for (int k = 0; k < rubriqueNmclParents.size(); k++) {
			for (int l = 0; l < rubriqueParents.size(); l++) {
				if (rubriqueParents.get(l).equals(rubriqueNmclParents.get(k))) {
					return rubriqueNmclParents.get(k);
				}

			}
		}

		return null;

	}

}
