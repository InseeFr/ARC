package fr.insee.arc.core.service.engine.normage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;

public class NormageEngineRegleUnicite {

	private static final Logger LOGGER = LogManager.getLogger(NormageEngineRegleUnicite.class);

	
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
	protected static String appliquerRegleUnicite(HashMap<String, ArrayList<String>> regle, String jointure) {
		StaticLoggerDispatcher.info(LOGGER, "appliquerRegleUnicite()");

		String returned = jointure;
		// extraction de la clause select

		String selectBase = " select " + ManipString.substringAfterLast(returned, " select ");

		String[] lines = returned.split("\n");
		int max = lines.length - 1;

		// on parcourt maintenant les regles d'unicité
		for (int j = 0; j < regle.get("id_regle").size(); j++) {
			String type = regle.get("id_classe").get(j);
			if (type.equals("unicité")) {

				String rubrique = regle.get("rubrique").get(j).toLowerCase();

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
								&& NormageEngineGlobal.testRubriqueInCreate(line, rubrique)) {
							// récupère l'identifiant pere du block (c'est l'identifiant juste aprés celui
							// du block m_...)
							String idBlock = NormageEngineGlobal.getFather(line);

							// déclarer une colonne en nomenclature indique en quelque sorte que c'est une
							// clé dans son groupe :
							// on garde donc des valeurs uniques dans le groupe (pour un meme pere, une
							// seule valeur)
							// et non null (si c'est null, la jointure sera false de toutes façons)
							// si la colonne nomenclature n'a pas déjà été traitée, on fait ce traitement
							// d'unicité
							if (!NormageEngineGlobal.testRubriqueInCreate(lines[k], "rk_" + rubrique)) {
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

		String viewAndInsert = "";
		for (int k = 0; k < lines.length; k++) {
			viewAndInsert += lines[k] + "\n";
		}
		viewAndInsert = ManipString.substringBeforeLast(viewAndInsert, " select ");

		returned = viewAndInsert + selectBase;

		return returned;
	}
	
	
}
