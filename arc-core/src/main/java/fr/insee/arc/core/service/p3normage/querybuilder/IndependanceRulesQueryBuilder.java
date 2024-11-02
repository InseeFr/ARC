package fr.insee.arc.core.service.p3normage.querybuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p3normage.bo.JoinParser;
import fr.insee.arc.core.service.p3normage.bo.RegleNormage;
import fr.insee.arc.core.service.p3normage.bo.TypeNormage;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.ManipString;

public class IndependanceRulesQueryBuilder {

	private static final Logger LOGGER = LogManager.getLogger(IndependanceRulesQueryBuilder.class);

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
	public static void ajouterRegleIndependance(FileIdCard fileIdCard) {
		StaticLoggerDispatcher.info(LOGGER, "ajouterRegleIndependance()");

		String blocCreate = ManipString.substringBeforeFirst(fileIdCard.getJointure(), "insert into {table_destination}");
		Set<String> rubriqueExclusion = new HashSet<>();
		Map<String, String> rubriquesAvecRegleDIndependance = new HashMap<>();

		// pour toutes les règles de relation,
		
		List<RegleNormage> reglesNormage = fileIdCard.getIdCardNormage().getReglesNormage();
		
		for (int j = 0; j < reglesNormage.size(); j++) {

			TypeNormage type = reglesNormage.get(j).getTypeNormage();

			// cas 1 on met en indépendant les règles en relation
			// en gros on parcours les regles de relation
			// on va exclure les bloc en relation des blocs à calculer comme indépendants
			if (type.equals(TypeNormage.RELATION)) {
				String rubrique = reglesNormage.get(j).getRubrique().toLowerCase();
				String rubriqueNmcl = reglesNormage.get(j).getRubriqueNmcl().toLowerCase();

				String rubriqueM = JoinParser.getM(blocCreate, rubrique);
				String rubriqueNmclM = JoinParser.getM(blocCreate, rubriqueNmcl);

				if (rubriqueM != null && rubriqueNmclM != null) {
					List<String> parentM = JoinParser.getParentsTree(blocCreate, rubriqueM, true);
					List<String> parentNmclM = JoinParser.getParentsTree(blocCreate, rubriqueNmclM, true);

					// on exclus tout les blocs présent dans les listes jusqu'a ce qu'on trouve
					// l'élément en commun
					String pppc = JoinParser.pppc(blocCreate, rubriqueM, rubriqueNmclM);

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
			if (type.equals(TypeNormage.CARTESIAN)) {
				String rubrique = reglesNormage.get(j).getRubrique().toLowerCase();
				String rubriqueM = JoinParser.getM(blocCreate, rubrique);
				rubriqueExclusion.add(rubriqueM);
			}

			if (type.equals(TypeNormage.INDEPENDANCE)) {
				String rubrique = reglesNormage.get(j).getRubrique().toLowerCase();
				String rubriqueNmcl = reglesNormage.get(j).getRubriqueNmcl().toLowerCase();

				// ignore rule if rubrique nmcl not found
				if (JoinParser.getLine(blocCreate, rubriqueNmcl)!=null)
				{
					rubriquesAvecRegleDIndependance.put(JoinParser.anyToM(rubrique), rubriqueNmcl);
				}
			}

		}

		// rubriques which had been declared explicitly with independance rules are
		// remove from exclusion
		for (String k : rubriquesAvecRegleDIndependance.keySet()) {
			rubriqueExclusion.remove(k);
		}

		// ARC compute which rubriques are independant and set the independance rules
		List<String> r = new ArrayList<>();
		addIndependanceToChildren(r, fileIdCard, blocCreate, JoinParser.getM(blocCreate),
				rubriquesAvecRegleDIndependance, rubriqueExclusion);

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
	public static String appliquerRegleIndependance(FileIdCard fileIdCard) throws ArcException {

		StaticLoggerDispatcher.info(LOGGER, "appliquerRegleIndependance()");

		String returned = fileIdCard.getJointure();

		String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");
		String blocInsert = " insert into {table_destination} "
				+ ManipString.substringAfterFirst(returned, "insert into {table_destination} ");

		List<RegleNormage> reglesBlocIndependance = fileIdCard.getIdCardNormage().getReglesNormage(TypeNormage.BLOC_INDEPENDANCE);
		
		for (int j = 0; j < reglesBlocIndependance.size(); j++) {


				String[] rubriqueRegle = reglesBlocIndependance.get(j).getRubrique().replace(" ", "").toLowerCase().split(",");
				String[] rubriqueNmclRegle = reglesBlocIndependance.get(j).getRubriqueNmcl().replace(" ", "").toLowerCase()
						.split(",");

				List<String> rubrique = new ArrayList<>();
				Map<String, String> rubriqueNmcl = new HashMap<>();

				// ne garder que les rubriques qui existent dans la requete
				// vérifier qu'elles ont le même pere
				String fatherSav = null;
				for (int i = 0; i < rubriqueRegle.length; i++) {
					if (blocCreate.contains(" " + rubriqueRegle[i] + " ")) {
						String rubriqueM = "m_" + JoinParser.getCoreVariableName(rubriqueRegle[i]);

						// si on trouve la rubrique mais qu'elle n'est pas identifiant de bloc (m_), on
						// sort
						if (!blocCreate.contains(rubriqueM)) {
							throw new ArcException(ArcExceptionMessage.NORMAGE_INDEPENDANCE_BLOC_INVALID_IDENTIFIER,
									rubriqueRegle[i]);
						}

						rubriqueRegle[i] = rubriqueM;
						rubrique.add(rubriqueRegle[i]);
						rubriqueNmcl.put(rubriqueRegle[i], rubriqueNmclRegle[i]);

						if (fatherSav == null) {
							fatherSav = JoinParser.getFatherM(blocCreate, rubriqueRegle[i]);
						} else {
							if (!fatherSav.equals(JoinParser.getFatherM(blocCreate, rubriqueRegle[i]))) {
								throw new ArcException(ArcExceptionMessage.NORMAGE_INDEPENDANCE_BLOC_INVALID_FATHER,
										rubriqueRegle[i]);
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

					Map<String, String> table = new HashMap<>();
					Map<String, String> pere = new HashMap<>();
					Map<String, String> autreCol = new HashMap<>();

					StringBuilder blocCreateNew = new StringBuilder();
					StringBuilder blocInsertNew = new StringBuilder();

					for (int i = 0; i < lines.length; i++) {
						boolean changed = false;

						for (String r : rubrique) {
							if (JoinParser.testRubriqueInCreate(lines[i], r)) {
								table.put(r, JoinParser.getTable(lines[i]));
								pere.put(r, JoinParser.getFather(lines[i]));
								autreCol.put(r, JoinParser.getOthers(lines[i]));

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
	private static boolean calculerTableIndependance(StringBuilder blocRequete, boolean nullTableRequired,
			List<String> rubrique, Map<String, String> rubriqueNmcl, Map<String, String> table,
			Map<String, String> pere, Map<String, String> autreCol) {
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
	 * A partir du blocCreate, determine récursivement les enfant d'une rubrique
	 * "m_<***>"
	 * 
	 * @param r
	 * @param blocCreate
	 * @param mRubrique
	 * @param regle
	 * @param rubriquesAvecRegleDIndependance
	 * @param norme
	 * @param periodicite
	 * @param exclusion
	 */
	private static void addIndependanceToChildren(List<String> r, FileIdCard fileIdCard, String blocCreate, String mRubrique, Map<String, String> rubriquesAvecRegleDIndependance,
			Set<String> exclusion) {
		
		List<String> s = JoinParser.getChildren(blocCreate, mRubrique);
		
		if (!s.isEmpty()) {
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
				fileIdCard.getIdCardNormage().addRegleNormage(new RegleNormage(TypeNormage.BLOC_INDEPENDANCE, rubriqueContent.toString(), rubriqueNmclContent.toString()));
			}

			for (String rub : s) {
				addIndependanceToChildren(r, fileIdCard, blocCreate, rub, rubriquesAvecRegleDIndependance, exclusion);
			}

		}

	}

}
