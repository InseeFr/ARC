package fr.insee.arc.core.service.p3normage.querybuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import fr.insee.arc.utils.utils.ManipString;

public class SuppressionRulesQueryBuilder {

	private static final Logger LOGGER = LogManager.getLogger(SuppressionRulesQueryBuilder.class);

	/**
	 * Ajoute automatiquement des regle de suppression de blocs non utilisés
	 * 
	 * @param regle                      : regle dans lesquels le bloc a été ajouté
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @param rubriqueUtiliseeDansRegles
	 * @throws ArcException
	 */

	public static void ajouterRegleSuppression(FileIdCard fileIdCard, Map<String, List<String>> rubriqueUtiliseeDansRegles)
			throws ArcException {
		// on va jouer au "qui enleve-t-on" ??
		// on va parcourir les regles de normage, controle, mapping et voir
		// quelles sont les rubriques utilisées

		// op 1 : identifier les blocs inutiles

		List<String> listVarUtilisee = new ArrayList<>();
		List<Integer> lineASupprimer = new ArrayList<>();

		for (int j = 0; j < rubriqueUtiliseeDansRegles.get("id_norme").size(); j++) {
			listVarUtilisee.add(rubriqueUtiliseeDansRegles.get("var").get(j));
		}

		String[] lines0 = fileIdCard.getJointure().split("\n");
		int max0 = lines0.length - 1;
		int k0 = max0;
		while (k0 >= 1) {
			String line0 = ManipString.substringBeforeFirst(lines0[k0], "from (select ");

			if (line0.startsWith("create temporary table ") && !line0.contains("_null as (select * from ")) {
				// pour chaque ligne valide, on parcours la liste des variables du controle et
				// du mapping; si on trouve qq'un on ne fait
				// rien
				// sinon on va noter le groupe comme eventuellement à supprimer

				boolean foundVarControleMapping = false;
				for (String varControleMapping : listVarUtilisee) {
					// on teste toutes les variables sauf le pere du bloc (qui est en 2ieme
					// position)
					String line2 = ManipString.substringBeforeFirst(line0, ",") + ","
							+ ManipString.substringAfterFirst(ManipString.substringAfterFirst(line0, ","), ",");

					if (line2.contains(" " + varControleMapping + " ")) {
						foundVarControleMapping = true;
						break;
					}
				}

				// on a trouvé aucune variable de mapping dans ce groupe
				if (!foundVarControleMapping) {
					// extraire la rubrique parente
					String rubriquePere = ManipString
							.substringBeforeFirst(ManipString.substringAfterFirst(line0, "as (select "), " as m_");

					boolean foundRubriquePere = false;
					// faut vérifier qu'on peut bien supprimer la table : aucune table qui suit ne
					// doit contenir la rubrique pere de la
					// table

					Integer k1 = k0 + 1;

					while (k1 <= max0) {
						String line1 = ManipString.substringBeforeFirst(lines0[k1], "from (select ");

						if (!line1.startsWith("create temporary table ")) {
							break;
						}

						// si on retrouve le pere dans une table qui n'est pas à supprimer, la table
						// doit etre gardée
						if (line1.contains(" " + rubriquePere + " ") && !lineASupprimer.contains(k1)) {
							foundRubriquePere = true;
							break;
						}

						k1++;
					}

					if (!foundRubriquePere) {
						String rubriquePereBloc = JoinParser.getCoreVariableName(rubriquePere);

						// ajouter le bloc aux regles de suppression si pas dans la table de regle
						if (!fileIdCard.getIdCardNormage().isAnyRubrique(rubriquePere)
								&& !fileIdCard.getIdCardNormage().isAnyRubriqueNmcl(rubriquePere)
								&& !fileIdCard.getIdCardNormage().isAnyRubrique(rubriquePereBloc)
								&& !fileIdCard.getIdCardNormage().isAnyRubriqueNmcl(rubriquePereBloc)
								) {
							lineASupprimer.add(k0);
							
							fileIdCard.getIdCardNormage().addRegleNormage(new RegleNormage(TypeNormage.DELETION, rubriquePereBloc, null));

						}
					}
				}

			}

			k0--;
		}

		// op 2 : identifier les variables inutilisées
		max0 = lines0.length - 1;
		k0 = max0;
		while (k0 >= 1) {
			String line0 = ManipString.substringBeforeFirst(lines0[k0], "from (select ");

			if (line0.startsWith("create temporary table ") && !line0.contains("_null as (select * from ")
					&& !lineASupprimer.contains(k0)) {
				Pattern p = Pattern.compile(" as [iv][^, (]*");
				Matcher m = p.matcher(line0);
				int nbMatch = 0;
				while (m.find()) {
					nbMatch++;

					// on ne considère ni les identifiant de blocs, ni les peres (donc nbMatch>2 car
					// ceux sont les deux premieres variables
					// d'un bloc)
					if (nbMatch > 2) {
						String rubrique = JoinParser.getCoreVariableName(ManipString.substringAfterFirst(m.group(), " as "));
						String rubriqueI = "i_" + rubrique;
						String rubriqueV = "v_" + rubrique;

						if (!listVarUtilisee.contains(rubriqueI) && !listVarUtilisee.contains(rubriqueV)
								
								&& !fileIdCard.getIdCardNormage().isAnyRubrique(rubriqueI)
								&& !fileIdCard.getIdCardNormage().isAnyRubrique(rubriqueV)
								&& !fileIdCard.getIdCardNormage().isAnyRubrique(rubrique)

								&& !fileIdCard.getIdCardNormage().isAnyRubriqueNmcl(rubriqueI)
								&& !fileIdCard.getIdCardNormage().isAnyRubriqueNmcl(rubriqueV)
								&& !fileIdCard.getIdCardNormage().isAnyRubriqueNmcl(rubrique)

								) {
							
							fileIdCard.getIdCardNormage().addRegleNormage(new RegleNormage(TypeNormage.DELETION, rubrique, null));
						}
					}
				}

			}
			k0--;

		}

	}

	/**
	 * Modifie la requete pour appliquer les regles de suppression
	 * 
	 * @param regle
	 * @param norme
	 * @param validite
	 * @param periodicite
	 * @param jointure
	 * @return
	 * @throws ArcException
	 */
	public static String appliquerRegleSuppression(FileIdCard fileIdCard) throws ArcException {

		StaticLoggerDispatcher.info(LOGGER, "appliquerRegleSuppression()");

		String returned = fileIdCard.getJointure();

		// ajout des regles
		// parcourt des regles : faut parcourir les suppression d'abord
		
		List<RegleNormage> regleNormageDeletion = fileIdCard.getIdCardNormage().getReglesNormage(TypeNormage.DELETION);
		for (int j = 0; j < regleNormageDeletion.size(); j++) {

			String rubrique = regleNormageDeletion.get(j).getRubrique().toLowerCase();

			returned = appliquerRegleSuppressionCore(returned, rubrique);

		}
		return returned;
	}

	/**
	 * rework the query for a rubrique marked as to be suppressed
	 * 
	 * @param returned
	 * @param rubrique
	 * @return
	 */
	private static String appliquerRegleSuppressionCore(String returned, String rubrique) {
		String[] lines = returned.split("\n");

		List<String> grpAEnlever = new ArrayList<>();
		List<Integer> ligneAEnlever = new ArrayList<>();
		List<String> rubriqueAEnlever = new ArrayList<>();

		// identifier les groupes a enlever de la requete
		identifierGroupeAEnlever(lines, rubrique, grpAEnlever, ligneAEnlever);

		// identifier les rubriques a enlever de la requete
		identifierRubriqueAEnlever(lines, rubrique, grpAEnlever, ligneAEnlever, rubriqueAEnlever);

		// on en termine : calculer la requete
		return calculerRequeteSupression(lines, ligneAEnlever, rubriqueAEnlever);
	}

	private static String calculerRequeteSupression(String[] lines, List<Integer> ligneAEnlever,
			List<String> rubriqueAEnlever) {
		StringBuilder f = new StringBuilder();

		int max = lines.length - 1;

		int k = 0;
		while (k <= max) {
			String line = lines[k];

			line = line + ",";
			for (String r : rubriqueAEnlever) {
				line = line.replace("," + r + ",", ",");
				line = line.replace(", " + r + " as " + r + " ,", ",");
				line = line.replace(", " + r + " as " + r + "  ", " ");
				line = line.replace(",min( " + r + " ) as " + r + ",", ",");
				line = line.replace(",min( " + r + " ) as  " + r + ",", ",");
				line = line.replace(",min( " + r + " ) as " + r + " ", " ");
				line = line.replace(",min( " + r + " ) as  " + r + " ", " ");
			}
			line = line.substring(0, line.length() - 1);

			if (!ligneAEnlever.contains(k)) {
				f.append(line + "\n");
			}

			k++;
		}
		return f.toString();
	}

	private static void identifierRubriqueAEnlever(String[] lines, String rubrique, List<String> grpAEnlever,
			List<Integer> ligneAEnlever, List<String> rubriqueAEnlever) {
		int max = lines.length - 1;

		int k = 1;
		while (k <= max) {
			String line = lines[k];
			if (!line.startsWith("create temporary table ")) {
				break;
			}

			for (String r : grpAEnlever) {
				if (line.startsWith("create temporary table t_" + r + " ")
						|| line.startsWith("create temporary table t_" + r + "_null ")) {

					if (!rubriqueAEnlever.contains("i_" + r)) {
						rubriqueAEnlever.add("i_" + r);
					}

					if (!rubriqueAEnlever.contains("m_" + r)) {
						rubriqueAEnlever.add("m_" + r);
					}

					ligneAEnlever.add(k);

					Pattern p = Pattern.compile(" as [iv][^, (]* ");
					Matcher m = p.matcher(line);
					boolean notFirst = false;
					while (m.find()) {
						// on n'enleve pas l'identifiant technique de la table pere
						if (notFirst) {
							rubriqueAEnlever.add(ManipString.substringAfterFirst(m.group(), " as ").trim());
						} else {
							notFirst = true;
						}
					}

					break;
				}
			}

			k++;

		}

		// on met la rubrique de base : meme si c'est pas un groupe, ca permet d'enlever
		// des colonnes betement
		if (!rubriqueAEnlever.contains("i_" + rubrique)) {
			rubriqueAEnlever.add("i_" + rubrique);
		}

		if (!rubriqueAEnlever.contains("v_" + rubrique)) {
			rubriqueAEnlever.add("v_" + rubrique);
		}
	}

	/**
	 * 
	 * @param lines
	 * @param rubrique
	 * @param grpAEnlever
	 * @param ligneAEnlever
	 */
	private static void identifierGroupeAEnlever(String[] lines, String rubrique, List<String> grpAEnlever,
			List<Integer> ligneAEnlever) {
		int max = lines.length - 1;

		// on va iterer sur les lignes pour identifier les groupes à enlever
		// si le groupe est pere d'autre groupe, faut aussi retirer les autres groupes
		int k = max - 1;

		grpAEnlever.add(rubrique);

		while (k > 0 && !lines[k].startsWith(" from")) {

			// on ne reteste pas les lignes déjà vues
			if (!ligneAEnlever.contains(k) && lines[k].startsWith(" left join ")) {

				String line = lines[k];
				String grpTrouve = null;
				// tester si les rubriques de grpAEnlever sont pere d'une autre rubrique
				for (String r : grpAEnlever) {
					// cas 1 : ajouter la rubrique trouvée à la liste des rubrique a enlever
					if (line.endsWith("i_" + r)) {
						// extraire la rubrique à enlever et la mettre dans variable grpTrouve
						grpTrouve = ManipString.substringBeforeFirst(ManipString.substringAfterLast(line, "=t_"), ".");
						// marquer la ligne à enlever;
						ligneAEnlever.add(k);
						// revenir au début
						k = max - 1;
						break;
					}

					// cas 2 : tester si la rubrique est fille
					if (line.startsWith(" left join t_" + r + " ")) {
						ligneAEnlever.add(k);
						break;
					}

				}

				// code note : grpTrouve added here to the list grpAEnlever because it iterates
				// over grpAEnlever list in last for loop
				if (grpTrouve != null) {
					grpAEnlever.add(grpTrouve);
				}

			}
			k = k - 1;
		}
	}

}
