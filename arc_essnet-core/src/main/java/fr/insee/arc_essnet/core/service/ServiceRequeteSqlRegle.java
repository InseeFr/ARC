package fr.insee.arc_essnet.core.service;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.model.RegleControleEntity;
import fr.insee.arc_essnet.core.model.RuleSets;
import fr.insee.arc_essnet.utils.utils.FormatSQL;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.arc_essnet.utils.utils.ManipString;

@Component
public class ServiceRequeteSqlRegle {

	public String tableResultat;
	public String tableTempData;
	public String tableTempMark;

	private static final Logger logger = Logger.getLogger(ServiceRequeteSqlRegle.class);

	/**
	 * Code SQL pour la création d'une table temporaire spécifique à un jeu de règle
	 *
	 * @param jdr
	 * @param table
	 * @return
	 */
	public String initTemporaryTable(RuleSets jdr, String table) {
		this.tableResultat = table;
		this.tableTempData = table + "_data";
		this.tableTempMark = table + "_mark";

		StringBuilder sb = new StringBuilder();
		sb.append(dropControleTemporaryTables());

		// creation de la table de marquage
		sb.append("CREATE ");
		if (!this.tableTempMark.contains(".")) {
			sb.append("TEMPORARY ");
		}
		else
		{
			sb.append(" ");
		}

		sb.append("TABLE " + this.tableTempMark + " "+FormatSQL.WITH_NO_VACUUM+" AS ");
		sb.append("select id_source, id, null::text collate \"C\" as brokenrules from " + table + " where 1=0 ;\n");

		sb.append("CREATE ");
		if (!this.tableTempData.contains(".")) {
			sb.append("TEMPORARY ");
		}
		else
		{
			sb.append(" ");
		}

		sb.append("TABLE " + this.tableTempData + " "+FormatSQL.WITH_NO_VACUUM+" AS ");
		sb.append("(	SELECT * ");
		sb.append("		FROM " + this.tableResultat + " ");
		// sb.append("		WHERE todo='1' ");
		// sb.append("			AND id_norme = '{1}'::text ");
		sb.append("		WHERE id_norme = '" + jdr.getIdNorme() + "'::text  ");
		sb.append("			AND periodicite = '" + jdr.getPeriodicite() + "'::text ");
		sb.append("			AND to_date(validite,'yyyy-mm-dd')>='" + jdr.getValiditeInfString() + "'::date ");
		sb.append("			AND to_date(validite,'yyyy-mm-dd')<='" + jdr.getValiditeSupString() + "'::date ");
		sb.append(");");

//		sb.append("\n create index idx1_"+ManipString.substringAfterFirst(tableTempData,".")+" on "+tableTempData+" (id_source);");
//		sb.append("\n analyze "+tableTempData+" (id_source);");

	//	LoggerDispatcher.info("Ma requête pour la création d'une table temporaire spécifique à un jeu de règle: " + sb,logger);

		return sb.toString();
	}

	/**
	 * update final des controles et broken rules depuis la table mark vers la table resultat
	 *
	 * @return
	 */
	public String markTableResultat() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		// On fait peter les watts; grosse requete en cas d'erreur
//		sb.append("update " + this.tableResultat + " v set brokenrules=w.brokenrules, controle= '1' from ");
//		sb.append("\n (select id_source, id, string_to_array(brokenrules,',') as brokenrules ");
//		sb.append("\n  from ( select a.id_source, a.id, string_agg(b.brokenrules,',') as brokenrules ");
//		sb.append("\n   from (select id_source, id from " + this.tableTempMark + " group by id_source, id) a,  ");
//		sb.append("\n   " + this.tableTempMark + " b ");
//		sb.append("\n  where a.id_source=b.id_source and a.id=b.id group by a.id_source, a.id ) u ) w ");
//		sb.append("\n where v.id_source=w.id_source and v.id=w.id ; ");
		
		sb.append("\n UPDATE " + this.tableResultat + " v set brokenrules=w.brokenrules, controle= '1' FROM ");
		sb.append("\n (SELECT id_source, id, array_agg(brokenrules) as brokenrules ");
		sb.append("\n  FROM " + this.tableTempMark + " ");
		sb.append("\n GROUP BY id_source, id ) w ");
		sb.append("\n  WHERE v.id_source=w.id_source AND v.id=w.id ; ");
		
		return sb.toString();
	}

	public String dropControleTemporaryTables() {
		return FormatSQL.dropTable(this.tableTempData).toString() + FormatSQL.dropTable(this.tableTempMark).toString();
	}

	/**
	 * Controle si le contenu d'une rubrique est bien numérique
	 * Cette méthode inscrit les erreurs trouvées dans une table spécifique tableTempMark
	 * @param reg
	 * @return
	 */
	public String ctlIsNumeric(RegleControleEntity reg) {
		String requete = "";
		//requete = requete + preAction(reg);
		String cond = conditionLongueur(reg);
		requete = requete + "WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " "
				+ "			WHERE ({2} ~ '^-?\\d*(\\.\\d+)?$') IS FALSE " + cond + ") "
				+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		// + "UPDATE {0} a SET  brokenrules=array_append(brokenrules,'{1}'::text) "
		// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
		// + "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere());
		return requete;
	}

	/**
	 * Permet une correction automatique de la table avant controle
	 * Attention, la table contrôlée tableTempData ne sert qu'au controle pour alimenter la table tableTempMark,
	 * toute modification sur celle ci ne sera pas répercuter sur les données en output
	 * Pour cette raison, il faut deux UPDATE (une pour les données d'output et une pour les données qui servent de controle)
	 * @param reg
	 * @return
	 */
//	private String preAction(RegleControleEntity reg) {
//		StringBuilder requete = new StringBuilder();
//		if (!StringUtils.isEmpty(reg.getPreAction())) {
//			// UPDATE sur la table des données
//			requete.append(" UPDATE " + reg.getTable());
//			requete.append(" SET " + reg.getRubriquePere() + "=" + ManipString.extractAllRubrique(reg.getPreAction())+ ";");
//			// UPDATE sur la table sur laquelle les controles vont être réellement passés
//			// tableTempData est une extrait de la table CONTROLE_ENCOURS
//			requete.append("UPDATE " + this.tableTempData);
//			requete.append(" SET " + reg.getRubriquePere() + "=" + ManipString.extractAllRubrique(reg.getPreAction())+ ";");
//		}
//		return requete.toString();
//	}

	public String ctlIsDate(RegleControleEntity reg) {
		String requete = "";
		//requete = requete + preAction(reg);

		requete = requete + "WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " "
				// + "			WHERE 	CASE	WHEN {2}=to_char(to_date({2},'{3}'),'{3}') THEN false "
				+ "			WHERE 	CASE	WHEN arc.isdate({2},'{3}') THEN false " + "							WHEN {2} is null THEN false"
				+ "							ELSE true " + "					END) " + "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		// + "UPDATE {0} a SET brokenrules=array_append(brokenrules,'{1}'::text) "
		// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
		// + "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere(), reg.getCondition());
		return requete;
	}

	public String ctlIsAlphanum(RegleControleEntity reg) {
		String requete = "";
		//requete = requete + preAction(reg);
		String cond = conditionLongueur(reg);
		requete = requete + "WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " "
				+ "			WHERE 1=2 " + cond + ") " + "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		// + "UPDATE {0} a SET brokenrules=array_append(brokenrules,'{1}'::text) "
		// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
		// + "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle());
		return requete;
	}

	public String ctlCardinalite(RegleControleEntity reg) {
		String requete = "";
		String cond = conditionCardinalite(reg);
		requete = "WITH " + "trav as (SELECT id_source, {2} " + "			FROM (SELECT DISTINCT id_source, {2}, {3} FROM "
				+ this.tableTempData + " WHERE {2} IS NOT NULL AND {3} IS NOT NULL) as foo " + "			GROUP BY id_source,{2} " + "			HAVING " + cond + "), "
				+ "ctl AS (SELECT a.id_source, a.id FROM " + this.tableTempData + " a "
//				+ "				INNER JOIN trav ON a.{2}=trav.{2} AND a.id_source=trav.id_source) "
				+ "				INNER JOIN trav ON row(a.{2})::text collate \"C\"=row(trav.{2})::text collate \"C\" AND a.id_source=trav.id_source) "
				+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		// + "UPDATE {0} a SET brokenrules=array_append(brokenrules,'{1}'::text) "
		// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
		// + "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere(), reg.getRubriqueFils());
	//	System.out.println("ma requete : " + requete);
		return requete;
	}

	public String ctlCardinaliteSansMembre(RegleControleEntity reg, String rubrique) {
		String requete = "";
		requete = "WITH " + "ctl AS (	SELECT id_source, id "
				+ "						FROM " + this.tableTempData + " "
				+ "						WHERE {2} is not null) "
				+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
				//+ "UPDATE {0} a SET brokenrules=array_append(brokenrules,'{1}'::text) "
				// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
				//+ "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle(), rubrique);
		return requete;
	}

	/**
	 * Application d'une règle de type CONDITION
	 * il faut filtrer et caster les rubriques contenu dans la clause
	 * @param reg
	 * @param mapRubrique
	 * @return
	 */
	public String ctlCondition(RegleControleEntity reg, Map<String, RegleControleEntity> mapRubrique) {
		String filtre = writeFiltre(mapRubrique);
//		LoggerDispatcher.info("Ma condition avant : " + reg.getCondition(),logger);
		String cond = rewriteCondition(mapRubrique, reg.getCondition());
//		LoggerDispatcher.info("Ma condition après : " + cond,logger);
		String requete = "WITH " + " ctl AS ( select id_source, id from (SELECT id_source, id, "
				+" CASE WHEN " + filtre + " THEN CASE WHEN " + cond + " THEN FALSE ELSE TRUE END ELSE FALSE END as condition_a_tester " + " FROM " + this.tableTempData + " "
				+") ww where condition_a_tester ) "
				+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		// + "UPDATE {0} a SET brokenrules=array_append(brokenrules,'{1}'::text) "
		// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
		// + "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle());
//		LoggerDispatcher.info("Ma requete de CONDITION :" + requete,logger);
		return requete;
	}

	/**
	 * ecriture du bout de requete SQL qui permet d'implémenter la condition de cardinalité
	 *
	 * @param reg
	 * @return
	 */
	public String conditionCardinalite(RegleControleEntity reg) {
		String cond = "";
		String rubrique = reg.getRubriquePere();
		String rubriqueF = reg.getRubriqueFils();
		String borneInf = reg.getBorneInf();
		String borneSup = reg.getBorneSup();
//		if (borneSup == null) {
//			cond = "count({0})<{1}";
//			cond = getRequete(cond, rubrique, borneInf);
//		} else {
//			cond = "count({0})<{1} OR count({0})>{2}";
//			cond = getRequete(cond, rubrique, borneInf, borneSup);
//		}
//
		if (borneSup == null) {
			cond = "count({0})<{2} OR count({1})<{2}";
			cond = getRequete(cond, rubrique, rubriqueF,borneInf);
		} else {
			cond = "count({0})<{2} OR count({1})<{2} OR count({0})>{3} OR count({1})>{3}";
			cond = getRequete(cond, rubrique, rubriqueF,borneInf, borneSup);
		}
		return cond;
	}

	/**
	 * ecriture du bout de requete SQL qui permet de remplacer les noms de rubrique par les cast qui vont bien
	 * La clé de la mapRubrique est forcément en MAJUSCULE d'où la mise en majuscule de condiction0
	 *
	 * @param mapRubrique
	 * @param condition0
	 * @return le résultat est en MAJUSCULE
	 */
	public String rewriteCondition(Map<String, RegleControleEntity> mapRubrique, String condition0) {
		LoggerDispatcher.debug("Je rentre dans la méthode rewriteCondition", logger);
		// Passage en MAJUSCULE car la map contient des elements en majuscule
		// bétonnage du code pour que le .uppercase ne lève pas de null pointerException
		String cond;
		if (condition0 == null) {
			return condition0;
		}
		cond = condition0.toUpperCase();
		String type = "";
		String rubrique = "";
		String format = "";
		for (Entry<String, RegleControleEntity> entry : mapRubrique.entrySet()) {
			LoggerDispatcher.debug("A l'intérieur de la boucle FOR", logger);
			type = entry.getValue().getIdClasse().trim();
			rubrique = entry.getKey().trim();
			LoggerDispatcher.debug("Mon type : " + type + ", ma rubrique : " + rubrique, logger);
			switch (type) {
			case "NUM":
				cond = cond.replace("{" + rubrique + "}", "cast(" + rubrique + " as numeric)");
				LoggerDispatcher.debug("la nouvelle condition : " + cond, logger);
				break;
			case "DATE":

				format = entry.getValue().getCondition().trim();
				LoggerDispatcher.debug("format vaut : " + format, logger);
				cond = cond.replace("{" + rubrique + "}", "to_date(" + rubrique + ",'" + format + "')");
				LoggerDispatcher.debug("la nouvelle condition : " + cond, logger);
				break;
			default:
				cond = cond.replace("{" + rubrique + "}", rubrique);
				break;
			}
			format = "";
		}
		return cond;
	}

	/**
	 * ecriture du bout de requete SQL qui permet de filtrer les enregistrements avec une syntaxe correcte
	 * Attention, les valeurs NULL ne sont pas évaluées dans les fonction SQL, du coup elle ne renvoie ni true ni false.
	 * Dans ce cas de filtre pour les règle de condition, il faut que le test sur le fait que ce soit une DATE ou un NUMERIC renvoie TRUE.
	 * @param mapRubrique
	 * @return
	 */
	public String writeFiltre(Map<String, RegleControleEntity> mapRubrique) {
		String filtre = "";
		String type = "";
		String rubrique = "";
		String format = "";

		int i = 0;

		for (Entry<String, RegleControleEntity> entry : mapRubrique.entrySet()) {
			type = entry.getValue().getIdClasse().trim();
			rubrique = entry.getKey().trim();
			format = entry.getValue().getCondition();

			if (i > 0) {
				filtre = filtre + " AND ";
			}

			switch (type) {
			case "NUM":
				//filtre = filtre + " ({0} ~ '^-?\\d*(\\.\\d+)?$') ";
				filtre = filtre + " (case when {1} IS NULL then true when {0} IS NULL then true else {0} ~ '^-?\\d*(\\.\\d+)?$' end) ";
				filtre = getRequete(filtre, rubrique, "i_"+ManipString.substringAfterFirst(rubrique,"_"));
				break;
			case "DATE":
				// filtre=filtre+" {0}=to_char(to_date({0},'{1}'),'{1}') ";
				//filtre = filtre + " arc.isdate({0},'{1}') ";
				filtre = filtre + " (case when {1} IS NULL then true when {0} IS NULL then true else arc.isdate({0},'{2}') end )";
				filtre = getRequete(filtre, rubrique, "i_"+ManipString.substringAfterFirst(rubrique,"_"), format);
				break;
			default:
				filtre = filtre + " TRUE ";
				break;
			}
			i++;
		}

		if (i == 0) {
			filtre = filtre + " TRUE ";
		}

		LoggerDispatcher.debug("Mon filtre est le suivant : " + filtre, logger);
		return filtre;
	}

	/**
	 * Fonction pour insérer un paramètre simplement et éviter le découpage de chaine "...."+var+"..."
	 * Les élements à remplacer sont entre {...}
	 * @param req
	 * @param args
	 * @return
	 */
	public String getRequete(String req, String... args) {
		String s = req;
		String exp = "";
		for (int i = 0; i < args.length; i++) {
			exp = "{" + i + "}";
			s = s.replace(exp, args[i]);
		}
		return s;
	}

	/**
	 * En fonction des bornes renseignées de la règle, la requête ne sera pas la même
	 * Atention, la valeur null ne renvoie rien dans les fonctions SQL, du coup pas de test de longueur possible, sauf en protégeant avec un coalesce.
	 * @param borneInf
	 * @param borneSup
	 * @return
	 */
	public String conditionLongueur(RegleControleEntity reg) {
		String cond = "";
		String rubrique = reg.getRubriquePere();
		String borneInf = reg.getBorneInf();
		String borneSup = reg.getBorneSup();
		if (borneInf != null && borneSup != null) {
			// si l'identifiant est null on check pas et on marque pas
			// si la valeur est null on check pas et on marque pas (la condition est forcément vérifiée dans ce cas
			// sinon on fait la vérification habituelle
			//cond = "OR char_length(regexp_replace({0}, '^-', ''))<{1} OR char_length(regexp_replace({0}, '^-', ''))>{2}";
			cond = "OR case when {3} is null then false when {0} is null then false else (char_length(regexp_replace({0}, '^-', ''))<{1} OR char_length(regexp_replace({0}, '^-', ''))>{2}) end";
			cond = getRequete(cond, rubrique, borneInf, borneSup, "i_"+ManipString.substringAfterFirst(rubrique,"_"));
		} else if (borneInf == null && borneSup != null) {
			//cond = "OR char_length(regexp_replace({0}, '^-', ''))>{1}";
			
			// si l'identifiant est null on check pas et on marque pas
			// si la valeur est null, on marque pas (on ne dépassera jamais une borne sup)
			// sinon on fait la vérification habituelle
			cond = "OR case when {2} is null then false when {0} is null then false else char_length(regexp_replace({0}, '^-', ''))>{1}";
			cond = getRequete(cond, rubrique, borneSup, "i_"+ManipString.substringAfterFirst(rubrique,"_"));
		} else if (borneInf != null && borneSup == null) {
			//cond = "OR char_length(regexp_replace({0}, '^-', ''))<{1}";
			
			// si la borne inf vaut 0 : on check rien
			// si l'identifiant est null on check pas et marque pas
			// si la valeur est null on marque : on est forcément en dessous la bornInf
			// sinon on fait la vérification habituelle
			cond = "OR case when {1}=0 then false when case when {2} is null then false when {0} is null then true else char_length(regexp_replace({0}, '^-', ''))<{1}";
			cond = getRequete(cond, rubrique, borneInf, "i_"+ManipString.substringAfterFirst(rubrique,"_"));
		}
		return cond;
	}

	public String getTableResultat() {
		return this.tableResultat;
	}

	public void setTableResultat(String tableResultat) {
		this.tableResultat = tableResultat;
	}

	public String getTableTempData() {
		return this.tableTempData;
	}

	public void setTableTempData(String tableTempData) {
		this.tableTempData = tableTempData;
	}

	public String getTableTempMark() {
		return this.tableTempMark;
	}

	public void setTableTempMark(String tableTempMark) {
		this.tableTempMark = tableTempMark;
	}






}