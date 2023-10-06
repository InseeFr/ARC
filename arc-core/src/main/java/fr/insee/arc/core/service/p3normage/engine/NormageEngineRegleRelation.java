package fr.insee.arc.core.service.p3normage.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;

public class NormageEngineRegleRelation {

	private static final Logger LOGGER = LogManager.getLogger(NormageEngineRegleRelation.class);

	
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
	protected static String appliquerRegleRelation(Map<String, List<String>> regle, String jointure) {

		StaticLoggerDispatcher.info(LOGGER, "appliquerRegleRelation()");

		String returned = jointure;
		// extraction de la clause select
		String blocCreate = ManipString.substringBeforeFirst(returned, "\n insert into {table_destination}");

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
								&& NormageEngineGlobal.testRubriqueInCreate(line, rubriqueNmcl)) {
							// extraction du nom de la table
							listTableNmcl.add(NormageEngineGlobal.getTable(line));

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

		String viewAndInsert = "";
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
							+ " as g_rub," + NormageEngineGlobal.getFather(NormageEngineGlobal.getLine(blocCreate, listRubriqueNmcl.get(l)))
							+ " as g_pere from " + NormageEngineGlobal.getTable(blocCreate, listRubriqueNmcl.get(l)) + ") xx where "
							+ listRubrique.get(l) + "=g_rub and "
							+ NormageEngineGlobal.iToM(NormageEngineGlobal.getFather(NormageEngineGlobal.getLine(blocCreate, listRubriqueNmcl.get(l)))) + "=g_pere) ";

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

	
}
