package fr.insee.arc.core.service.p4controle.dao;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.service.global.thread.ThreadTemporaryTable;
import fr.insee.arc.core.service.p4controle.bo.ControleMarkCode;
import fr.insee.arc.core.service.p4controle.bo.ControleXsdCode;
import fr.insee.arc.core.service.p4controle.bo.RegleControle;
import fr.insee.arc.core.service.p4controle.bo.XsdDate;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

@Component
public class ControleRegleDao {

	private String tableResultat;
	private String tableTempData;

	private static final Logger logger = LogManager.getLogger(ControleRegleDao.class);

	/**
	 * Code SQL pour la création d'une table temporaire spécifique à un jeu de règle
	 *
	 * @param jdr
	 * @param table
	 * @return
	 */
	public String initTemporaryTable(String table) {
		this.tableResultat = table;
		this.tableTempData = table;

		StringBuilder sb = new StringBuilder();
		sb.append(dropControleTemporaryTables());

		// creation de la table de marquage
		sb.append("CREATE TEMPORARY TABLE " + ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP + " " + FormatSQL.WITH_NO_VACUUM + " AS ");
		sb.append("select id, null::text as brokenrules, null::text as controle from " + table + " where false ;\n");

		// creation de la table meta
		sb.append("CREATE TEMPORARY TABLE " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + " " + FormatSQL.WITH_NO_VACUUM + " AS ");
		sb.append("select null::text as brokenrules, null::boolean as blocking, null::text as controle where false;\n");

		// total count of record in the table to evaluate the error ratio
		sb.append("CREATE TEMPORARY TABLE " + ThreadTemporaryTable.TABLE_CONTROLE_ROW_TOTAL_COUNT_TEMP + " " + FormatSQL.WITH_NO_VACUUM + " AS ");
		sb.append("select count(1)::numeric as n FROM " + table + ";\n");

		return sb.toString();
	}

	/**
	 * update final des controles et broken rules depuis la table mark vers la table
	 * resultat
	 *
	 * @return
	 */
	public String markTableResultat() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n UPDATE " + this.tableResultat + " v set brokenrules=w.brokenrules, controle= w.controle FROM ");
		sb.append("\n (SELECT id, array_agg(brokenrules) as brokenrules, min(controle) as controle ");
		sb.append("\n FROM " + ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP + " ");
		sb.append("\n GROUP BY id ) w ");
		sb.append("\n WHERE v.id=w.id");
		sb.append(";");

		// tag all record to exclude if file is blocked
		sb.append("\n UPDATE " + this.tableResultat + " v set controle='"
				+ ControleMarkCode.RECORD_WITH_ERROR_TO_EXCLUDE.getCode() + "'");
		sb.append("\n WHERE EXISTS (SELECT FROM " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + " where blocking)");
		sb.append(";");

		return sb.toString();
	}

	public String dropControleTemporaryTables() {
		return FormatSQL.dropTable(ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP).toString();
	}

	/**
	 * insert the source file line and the rules number whom evaluation is false in
	 * the mark table insert the count of line with false evaluation and the rules
	 * number in the meta table
	 * 
	 * @return
	 */
	private String insertBloc(String blockingThreshold, String errorRowProcessing, String regleId) {
		// map from gui user action code to database control code
		Map<String, String> mapRowProcessing = new HashMap<>();
		mapRowProcessing.put(ControleMarkCode.ERROR_ROW_PROCESSING_KEEP.getCode(), ControleMarkCode.RECORD_WITH_ERROR_TO_KEEP.getCode());
		mapRowProcessing.put(ControleMarkCode.ERROR_ROW_PROCESSING_EXCLUDE.getCode(), ControleMarkCode.RECORD_WITH_ERROR_TO_EXCLUDE.getCode());

		StringBuilder requete = new StringBuilder();

		// tableTempMark contains the id of records with an error, the error id and the
		// error type (keep or not the record in final output)
		// tableTempMeta contains the errors id, their types, and their results of the
		// blockingThreshold evaluation (to know if the file must be fully rejected or
		// not if a particular error occurs)

		// case for no blocking threshold
		// by default not blocking
		if (blockingThreshold == null || blockingThreshold.isEmpty()) {
			requete.append("\n, ins as (INSERT into " + ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP + " select id, '" + regleId + "', '"
					+ mapRowProcessing.get(errorRowProcessing) + "' as controle from ctl returning true) ");
			requete.append("\n INSERT into " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + " SELECT '" + regleId + "', false, '"
					+ mapRowProcessing.get(errorRowProcessing) + "' as controle where EXISTS (SELECT FROM ins); ");
			return requete.toString();
		}

		// case for threshold
		// parse threshold
		Pattern pattern = Pattern.compile("^(>|>=)([0123456789.]+)([%u])$");
		Matcher matcher = pattern.matcher(blockingThreshold);
		matcher.find();

		requete.append("\n, ins as (INSERT into " + ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP + " ");
		requete.append("SELECT id, '" + regleId + "'");

		// mark record to keep or exclude
		requete.append(",'" + mapRowProcessing.get(errorRowProcessing) + "'");

		requete.append("FROM ctl RETURNING true) ");

		// insert into meta table
		requete.append("\n INSERT into " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + " SELECT * FROM (SELECT '" + regleId + "',");

		// ratio or count evaluation
		requete.append("((count(*)::numeric");
		if (matcher.group(3).equals("%")) {
			requete.append("*100.0/(select n from " + ThreadTemporaryTable.TABLE_CONTROLE_ROW_TOTAL_COUNT_TEMP + ")");
		}
		requete.append(")");

		// operator
		requete.append(matcher.group(1));

		// compare to threshold
		requete.append(matcher.group(2));

		requete.append(")");

		// any record to keep or exclude ?
		requete.append(",'" + mapRowProcessing.get(errorRowProcessing) + "'");

		// calculate error only if there are any errors marked
		requete.append("\n FROM ins) zz WHERE EXISTS (SELECT FROM ins);");

		return requete.toString();
	}

	/**
	 * Controle si le contenu d'une rubrique est bien numérique Cette méthode
	 * inscrit les erreurs trouvées dans une table spécifique tableTempMark
	 * 
	 * @param reg
	 * @return
	 */
	public String ctlIsNumeric(RegleControle reg) {
		String cond = conditionLongueur(reg);
		String requete = "WITH ctl AS (	SELECT id FROM " + this.tableTempData + " "
				+ " WHERE ({2} ~ '^-?\\d*(\\.\\d+)?$') IS FALSE " + cond + ") "
				+ insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle()));
		requete = getRequete(requete, ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()), reg.getRubriquePere());
		return requete;
	}

	/**
	 * Requête de contrôle si le contenu d'une rubrique est une date conforme à un
	 * format. Les formats attendus dans la règle sont des formats valides
	 * PostgreSQL sauf pour "XSD_DATE" qui correspond au format date XSD. Cette
	 * méthode inscrit les erreurs trouvées dans une table spécifique tableTempMark
	 * 
	 * @param reg la règle à appliquer
	 */
	public String ctlIsDate(RegleControle reg) {
		StringBuilder reqBuilder = new StringBuilder("WITH ctl AS (SELECT id FROM " + this.tableTempData + " ");
		String requete = "";
		if (reg.getCondition().equalsIgnoreCase(ControleXsdCode.XSD_DATE_NAME)
				|| reg.getCondition().equalsIgnoreCase(ControleXsdCode.XSD_DATETIME_NAME)
				|| reg.getCondition().equalsIgnoreCase(ControleXsdCode.XSD_TIME_NAME)) {
			reqBuilder.append("\n WHERE CASE WHEN ( ");
			for (int i = 0; i < XsdDate.XSD_DATE_RULES.get(reg.getCondition().toLowerCase()).length; i++) {
				if (i > 0) {
					reqBuilder.append(" OR ");
				}
				reqBuilder.append(
						"arc.isdate({2}, '" + XsdDate.XSD_DATE_RULES.get(reg.getCondition().toLowerCase())[i] + "')");
			}
			reqBuilder.append(") THEN false ");
			reqBuilder.append("\n WHEN {2} is null THEN false ");
			reqBuilder.append("\n ELSE true END) ");
			reqBuilder.append(insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle())));
			requete = getRequete(reqBuilder.toString(), ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()), reg.getRubriquePere());
		} else {
			reqBuilder.append("\n WHERE CASE WHEN arc.isdate({2},'{3}') THEN false");
			reqBuilder.append("\n WHEN {2} is null THEN false ");
			reqBuilder.append("\n ELSE true END) ");
			reqBuilder.append(insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle())));
			requete = getRequete(reqBuilder.toString(), ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()), reg.getRubriquePere(),
					reg.getCondition());
		}
		return requete;
	}

	public String ctlIsAlphanum(RegleControle reg) {
		String cond = conditionLongueur(reg);
		String requete = "WITH ctl AS (SELECT id FROM " + this.tableTempData + " WHERE false " + cond + ")"
				+ insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle()));
		requete = getRequete(requete, ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()));
		return requete;
	}

	public String ctlCardinalite(RegleControle reg, List<String> listRubriqueExpr,
			List<String> listRubriqueTable) {

		// on retire le pere et le fils de la liste d'expr
		// on pourrait tout traiter pareil (pere, fils et autres...) mais vraiment pas
		// optimisé
		listRubriqueExpr.remove(reg.getRubriquePere().toUpperCase());
		listRubriqueExpr.remove(reg.getRubriqueFils().toUpperCase());

		String cond = conditionCardinalite(reg);

		StringBuilder requete = new StringBuilder();
		requete.append("WITH null_transform AS (");
		requete.append("SELECT " + reg.getRubriquePere());

		// rubrique fille
		// cas 1 : présente dans la table
		// cas 2 : non present dans la table et identifiante
		// cas 3 : non present dans la table et valeur
		if (listRubriqueTable.contains(reg.getRubriqueFils())) {
			requete.append("," + reg.getRubriqueFils() + " ");
		} else {
			if (reg.getRubriqueFils().toLowerCase().startsWith("i_")) {
				requete.append(", null::int as " + reg.getRubriqueFils() + " ");
			} else {
				requete.append(", null::text as " + reg.getRubriqueFils() + " ");
			}
		}

		// rubriques de l'expression
		for (String s : listRubriqueExpr) {
			if (listRubriqueTable.contains(s)) {
				// on refait les identifiants pour que
				// la valeur -1 corresponde au fait que la rubrique n'existe pas (null sur tout
				// le groupe ou inexistante)
				// la valeur 0 corresponde au fait que c'est null
				if (s.toLowerCase().startsWith("i_")) {
					requete.append(", case when max(" + s + ") over (partition by " + reg.getRubriquePere()
							+ ") is null then -1 else " + s + " end as " + s + " ");
				} else {
					requete.append("," + s + " ");
				}
			} else {
				if (s.toLowerCase().startsWith("i_")) {
					requete.append(", -1::int as " + s + " ");
				} else {
					requete.append(", null::text as " + s + " ");
				}
			}
		}
		requete.append(" FROM " + this.tableTempData + " ");
		requete.append(" ) ");

		requete.append(" , trav AS (");
		requete.append(" SELECT {2} FROM ");
		requete.append("  (SELECT DISTINCT {2}, {3} FROM ");

		requete.append("   (SELECT {2}, {3}, {2} IS NOT NULL ");

		// condition is written as a boolean inside select clause to be able to use
		// windows aggregate functions
		if (reg.getCondition() != null) {
			String condition = reg.getCondition();

			// On va recoder la condition à ce niveau
			// les i_ null présent dans l'expression vont etre recodé en 0
			condition = condition.replaceAll(ManipString.patternForIdRubriqueWithBrackets,
					"(case when $1 is null then 0 else $1 end)");

			condition = ManipString.extractAllRubrique(condition);

			requete.append("AND (" + condition + ") ");
		}
		requete.append("AS condition");
		requete.append(" FROM null_transform ) foo0 ");
		requete.append(" WHERE condition ");

		requete.append(") foo ");
		requete.append(" GROUP BY {2} HAVING " + cond + ") ");
		requete.append(" , ctl AS (");
		requete.append("  SELECT a.id FROM " + this.tableTempData + " a ");
		requete.append("  INNER JOIN trav ON row(a.{2})::text collate \"C\"=row(trav.{2})::text collate \"C\" ");
		requete.append(" ) ");
		requete.append(insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle())));

		return getRequete(requete.toString(), ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()), reg.getRubriquePere(),
				reg.getRubriqueFils());
	}

	/**
	 * Application d'une règle de type CONDITION il faut filtrer et caster les
	 * rubriques contenu dans la clause
	 * 
	 * @param reg
	 * @param mapRubrique
	 * @return
	 */
	public String ctlCondition(RegleControle reg, Map<String, RegleControle> mapRubrique) {
		String filtre = writeFiltre(mapRubrique);
		String cond = rewriteCondition(mapRubrique, reg.getCondition());
		String requete = "WITH ctl AS ( select id from (SELECT id, " + " CASE WHEN " + filtre + " THEN CASE WHEN "
				+ cond + " THEN FALSE ELSE TRUE END ELSE FALSE END as condition_a_tester " + " FROM "
				+ this.tableTempData + " " + ") ww where condition_a_tester ) "
				+ insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle()));
		requete = getRequete(requete, ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()));
		return requete;
	}

	/**
	 * Requête de contrôle si le contenu d'une rubrique est conforme à une
	 * expression régulière. Cette méthode inscrit les erreurs trouvées dans une
	 * table spécifique tableTempMark
	 * 
	 * @param reg la règle à appliquer
	 */
	public String ctlMatchesRegexp(RegleControle reg) {
		String requete = "WITH ctl AS (SELECT id FROM " + this.tableTempData + " " + "WHERE ({2} ~ '{3}') IS FALSE ) "
				+ insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle()));
		requete = getRequete(requete, ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()), reg.getRubriquePere(), reg.getCondition());
		return requete;
	}

	/**
	 * Requête de contrôle si le contenu d'une rubrique correspond à une liste de
	 * valeurs acceptées. La liste peut être soit :
	 * <ul>
	 * <li>une liste brute sql, ex : {@code 'Valeur 1', 'Valeur 2', Valeur 3'}</li>
	 * <li>une requête renvoyant la liste, ex :
	 * {@code select valeurs from nmcl_valeurs_55}</li>
	 * </ul>
	 * Cette méthode inscrit les erreurs trouvées dans une table spécifique
	 * tableTempMark.
	 * 
	 * @param reg la règle à appliquer
	 */
	public String ctlIsValueIn(RegleControle reg) {
		String requete = "WITH " + "ctl AS ( SELECT id " + " FROM " + this.tableTempData + " "
				+ "WHERE {2} not in ({3}) ) "
				+ insertBloc(reg.getSeuilBloquant(), reg.getTraitementLignesErreur(), Integer.toString(reg.getIdRegle()));
		requete = getRequete(requete, ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP, Integer.toString(reg.getIdRegle()), reg.getRubriquePere(), reg.getCondition());
		return requete;
	}

	/**
	 * ecriture du bout de requete SQL qui permet d'implémenter la condition de
	 * cardinalité
	 *
	 * @param reg
	 * @return
	 */
	private String conditionCardinalite(RegleControle reg) {
		String cond = "";
		String rubrique = reg.getRubriquePere();
		String rubriqueF = reg.getRubriqueFils();
		String borneInf = reg.getBorneInf();
		String borneSup = reg.getBorneSup();

		if (borneSup == null && borneInf != null) {
			cond = "count({1})<{2}";
			cond = getRequete(cond, rubrique, rubriqueF, borneInf);
		}

		if (borneSup != null && borneInf == null) {
			cond = "count({1})>{2}";
			cond = getRequete(cond, rubrique, rubriqueF, borneSup);
		}

		if (borneSup != null && borneInf != null) {
			cond = "count({1})<{2} OR count({1})>{3}";
			cond = getRequete(cond, rubrique, rubriqueF, borneInf, borneSup);
		}

		return cond;
	}

	/**
	 * ecriture du bout de requete SQL qui permet de remplacer les noms de rubrique
	 * par les cast qui vont bien La clé de la mapRubrique est forcément en
	 * MAJUSCULE d'où la mise en majuscule de condiction0
	 *
	 * @param mapRubrique
	 * @param condition0
	 * @return le résultat est en MAJUSCULE
	 */
	private String rewriteCondition(Map<String, RegleControle> mapRubrique, String condition0) {
		StaticLoggerDispatcher.debug(logger, "Je rentre dans la méthode rewriteCondition");
		// Passage en MAJUSCULE car la map contient des elements en majuscule
		if (condition0 == null) {
			return condition0;
		}
		
		// uppercase the condition because the map contains uppercase key
		String condition = Pattern.compile("\\{[^\\{\\}]*}").matcher(condition0).replaceAll(occ -> occ.group().toUpperCase());
		
		for (Entry<String, RegleControle> entry : mapRubrique.entrySet()) {
			StaticLoggerDispatcher.debug(logger, "A l'intérieur de la boucle FOR");
			
			String rubrique = entry.getKey().trim();
			StaticLoggerDispatcher.debug(logger, "Ma rubrique : " + rubrique);
			
			switch (entry.getValue().getTypeControle()) {
			case NUM:
				condition = condition.replace("{" + rubrique + "}", "cast(" + rubrique + " as numeric)");
				StaticLoggerDispatcher.debug(logger, "la nouvelle condition : " + condition);
				break;
			case DATE:
				String format = entry.getValue().getCondition().trim();
				
				StaticLoggerDispatcher.debug(logger, "format vaut : " + format);
				condition = condition.replace("{" + rubrique + "}", "to_date(" + rubrique + ",'" + format + "')");
				StaticLoggerDispatcher.debug(logger, "la nouvelle condition : " + condition);
				break;
			default:
				condition = condition.replace("{" + rubrique + "}", rubrique);
				break;
			}
		}
		return condition;
	}

	/**
	 * ecriture du bout de requete SQL qui permet de filtrer les enregistrements
	 * avec une syntaxe correcte Attention, les valeurs NULL ne sont pas évaluées
	 * dans les fonction SQL, du coup elle ne renvoie ni true ni false. Dans ce cas
	 * de filtre pour les règle de condition, il faut que le test sur le fait que ce
	 * soit une DATE ou un NUMERIC renvoie TRUE.
	 * 
	 * @param mapRubrique
	 * @return
	 */
	private String writeFiltre(Map<String, RegleControle> mapRubrique) {
		String filtre = "";

		int i = 0;

		for (Entry<String, RegleControle> entry : mapRubrique.entrySet()) {
			String rubrique = entry.getKey().trim();
			String format = entry.getValue().getCondition();

			if (i > 0) {
				filtre = filtre + " AND ";
			}

			switch (entry.getValue().getTypeControle()) {
			case NUM:
				filtre = filtre
						+ " (case when {1} IS NULL then true when {0} IS NULL then true else {0} ~ '^-?\\d*(\\.\\d+)?$' end) ";
				filtre = getRequete(filtre, rubrique, "i_" + ManipString.substringAfterFirst(rubrique, "_"));
				break;
			case DATE:
				filtre = filtre
						+ " (case when {1} IS NULL then true when {0} IS NULL then true else arc.isdate({0},'{2}') end )";
				filtre = getRequete(filtre, rubrique, "i_" + ManipString.substringAfterFirst(rubrique, "_"), format);
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

		StaticLoggerDispatcher.debug(logger, "Mon filtre est le suivant : " + filtre);
		return filtre;
	}

	/**
	 * Fonction pour insérer un paramètre simplement et éviter le découpage de
	 * chaine "...."+var+"..." Les élements à remplacer sont entre {...}
	 * 
	 * @param req
	 * @param args
	 * @return
	 */
	private String getRequete(String req, String... args) {
		if (args.length == 0) {
			return req;
		}
		/*
		 * Long-winded solution to avoid involuntary recursion, e.g.
		 * getRequete("A{0}B{1}", "{1}", "zz") should return "A{1}Bzz" and not "AzzBzz".
		 * Two steps : decomposition of the string, then reconstruction with insertion
		 * of the replacements.
		 */
		LinkedList<String> decomposedString = new LinkedList<>();
		decomposedString.add(req);
		LinkedList<String> remplacementStrings = new LinkedList<>();
		for (int i = 0; i < args.length; i++) {
			for (int j = 0; j < decomposedString.size(); j++) {
				int indexOf;
				String stringToDecompose = decomposedString.get(j);
				String stringToRemplace = "{" + i + "}";
				while ((indexOf = stringToDecompose.indexOf(stringToRemplace)) != -1) {
					String part1 = stringToDecompose.substring(0, indexOf);
					String part2 = stringToDecompose.substring(indexOf + stringToRemplace.length(),
							stringToDecompose.length());
					decomposedString.add(j, part2);
					decomposedString.remove(j + 1);
					decomposedString.add(j, part1);
					remplacementStrings.add(j, args[i]);
					stringToDecompose = decomposedString.get(j);
				}
			}
		}
		StringBuilder finalString = new StringBuilder();
		for (int i = 0; i < decomposedString.size(); i++) {
			finalString.append(decomposedString.get(i));
			if (remplacementStrings.size() > i) {
				finalString.append(remplacementStrings.get(i));
			}
		}
		return finalString.toString();
	}

	/**
	 * En fonction des bornes renseignées de la règle, la requête ne sera pas la
	 * même Atention, la valeur null ne renvoie rien dans les fonctions SQL, du coup
	 * pas de test de longueur possible, sauf en protégeant avec un coalesce.
	 * 
	 * @param borneInf
	 * @param borneSup
	 * @return
	 */
	private String conditionLongueur(RegleControle reg) {
		String cond = "";
		String rubrique = reg.getRubriquePere();
		String borneInf = reg.getBorneInf();
		String borneSup = reg.getBorneSup();
		if (borneInf != null && borneSup != null) {
			// si l'identifiant est null on check pas et on marque pas
			// si la valeur est null on check pas et on marque pas (la condition est
			// forcément vérifiée dans ce cas
			// sinon on fait la vérification habituelle
			cond = "OR case when {3} is null then false when {0} is null then false else (char_length(regexp_replace({0}, '^-', ''))<{1} OR char_length(regexp_replace({0}, '^-', ''))>{2}) end";
			cond = getRequete(cond, rubrique, borneInf, borneSup,
					"i_" + ManipString.substringAfterFirst(rubrique, "_"));
		} else if (borneInf == null && borneSup != null) {

			// si l'identifiant est null on check pas et on marque pas
			// si la valeur est null, on marque pas (on ne dépassera jamais une borne sup)
			// sinon on fait la vérification habituelle
			cond = "OR case when {2} is null then false when {0} is null then false else char_length(regexp_replace({0}, '^-', ''))>{1} end";
			cond = getRequete(cond, rubrique, borneSup, "i_" + ManipString.substringAfterFirst(rubrique, "_"));
		} else if (borneInf != null && borneSup == null) {

			// si la borne inf vaut 0 : on check rien
			// si l'identifiant est null on check pas et marque pas
			// si la valeur est null on marque : on est forcément en dessous la bornInf
			// sinon on fait la vérification habituelle
			cond = "OR case when {1}=0 then false when {2} is null then false when {0} is null then true else char_length(regexp_replace({0}, '^-', ''))<{1} end";
			cond = getRequete(cond, rubrique, borneInf, "i_" + ManipString.substringAfterFirst(rubrique, "_"));
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

}