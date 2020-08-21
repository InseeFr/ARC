package fr.insee.arc.core.service.engine.controle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.RegleControleEntity;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;


@Component
public class ServiceRequeteSqlRegle {

	/** Translations in SQL of the XSD date format ([-]CCYY-MM-DD[Z|(+|-)hh:mm]).*/
	
	private static final HashMap<String,String[]> XSD_DATE_RULES
	=new HashMap<String,String[]>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6787891391781422042L;

		{
			put(ControleRegleService.XSD_DATE_NAME.toLowerCase()
					,new String[] {
							"YYYY-MM-DD"
							,"YYYY-MM-DD\"Z\""
							// TODO : CHECK
							// it is not really the absolute hour and minute but a relative time zone delta
							// TZ parameter would be a better implementation but need more info and likely not needed
							//				,"YYYY-MM-DD+HH24:MI"
							//				"YYYY-MM-DD-HH24:MI"	
							}
			);

			put(ControleRegleService.XSD_DATETIME_NAME.toLowerCase()
					,new String[] {
							"YYYY-MM-DD\"T\"HH24:MI:SS"
							,"YYYY-MM-DD\"T\"HH24:MI:SS\"Z\""
							}
			);

			
			put(ControleRegleService.XSD_TIME_NAME.toLowerCase()
					,new String[] {
							"HH24:MI:SS"
							}
			);
			
		}
		
	};

	
	public String tableResultat;
	public String tableTempData;
	public String tableTempMark;

	private static final Logger logger = LogManager.getLogger(ServiceRequeteSqlRegle.class);

	/**
	 * Code SQL pour la création d'une table temporaire spécifique à un jeu de règle
	 *
	 * @param jdr
	 * @param table
	 * @return
	 */
	public String initTemporaryTable(JeuDeRegle jdr, String table) {
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

	/**
	 * Requête de contrôle si le contenu d'une rubrique est une date conforme à un format.
	 * Les formats attendus dans la règle sont des formats valides PostgreSQL sauf pour "XSD_DATE"
	 *  qui correspond au format date XSD.
	 * Cette méthode inscrit les erreurs trouvées dans une table spécifique tableTempMark
	 * @param reg la règle à appliquer
	 * */
	public String ctlIsDate(RegleControleEntity reg) {
		StringBuilder reqBuilder = new StringBuilder("WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " ");
		String requete = "";
		//requete = requete + preAction(reg);
		if (reg.getCondition().equalsIgnoreCase(ControleRegleService.XSD_DATE_NAME)
			||	reg.getCondition().equalsIgnoreCase(ControleRegleService.XSD_DATETIME_NAME)
			||	reg.getCondition().equalsIgnoreCase(ControleRegleService.XSD_TIME_NAME)	
				) {
			reqBuilder.append("			WHERE 	CASE	WHEN (");
			for (int i = 0 ; i < XSD_DATE_RULES.get(reg.getCondition().toLowerCase()).length ; i++) {
				if (i > 0) {
					reqBuilder.append(" or ");
				}
				reqBuilder.append("arc.isdate({2}, '" + XSD_DATE_RULES.get(reg.getCondition().toLowerCase())[i] + "')");
			}
			reqBuilder.append(") THEN false "
					+ "							WHEN {2} is null THEN false"
					+ "							ELSE true " + "					END) " + "INSERT into {0} select id_source, id, '{1}' from ctl; ");
		requete = getRequete(reqBuilder.toString(), this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere());
		} 
		else {
			reqBuilder.append(
					// + "			WHERE 	CASE	WHEN {2}=to_char(to_date({2},'{3}'),'{3}') THEN false "
					  "			WHERE 	CASE	WHEN arc.isdate({2},'{3}') THEN false " + "							WHEN {2} is null THEN false"
					+ "							ELSE true " + "					END) " + "INSERT into {0} select id_source, id, '{1}' from ctl; ");
			// + "UPDATE {0} a SET brokenrules=array_append(brokenrules,'{1}'::text) "
			// + "UPDATE {0} a SET  brokenrules=brokenrules||','||'{1}' "
			// + "FROM ctl b WHERE a.id_source=b.id_source and a.id=b.id and exists (select 1 from ctl limit 1); ";

			requete = getRequete(reqBuilder.toString(), this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere(), reg.getCondition());
		}
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


	/**
	 * Structure control
	 * check if all the tags found in the file are in the ruleset 
	 * and in the same order as in the ruleset
	 * @param structure
	 * @param jdr
	 * @param tableControleRegle
	 * @return
	 */
	public String ctlStructure(RegleControleEntity reg, String structure, String tableControleRegle) {
	
		StringBuilder requete=new StringBuilder();
		requete.append("\n with"); 
		requete.append("\n tmp_jointure as (");
		requete.append("\n select split_part(b.e,' ',1) as pere, split_part(b.e,' ',2) as pere_id, split_part(b.e,' ',3) as fils, n  from unnest(('{'||'"+structure+"'||'}')::text[]) with ordinality b(e,n)");
		requete.append("\n )");
		requete.append("\n , tmp_num as (");
		requete.append("\n select pere, pere_id, fils, row_number() over (partition by pere, pere_id order by n) as rk from (");
		requete.append("\n select pere, pere_id, fils, coalesce(lag(fils) over (partition by pere, pere_id order by n),'*') as prev_fils, n from tmp_jointure");
		requete.append("\n ) vv where fils!=prev_fils");
		requete.append("\n )");
		requete.append("\n , tmp_regle as (");
		requete.append("\n select rubrique_pere as p, rubrique_fils as f, xsd_ordre from "+tableControleRegle+" a ");
		requete.append("\n where xsd_ordre is not null");
		requete.append("\n AND EXISTS (SELECT 1 FROM (SELECT id_norme, periodicite, validite FROM "+this.tableTempData+" LIMIT 1) b "); 
		requete.append("\n WHERE a.id_norme=b.id_norme ");
		requete.append("\n AND a.periodicite=b.periodicite "); 
		requete.append("\n AND to_date(b.validite,'YYYY-MM-DD')>=a.validite_inf "); 
		requete.append("\n AND to_date(b.validite,'YYYY-MM-DD')<=a.validite_sup) ");
		requete.append("\n )");
		requete.append("\n , tmp_check as (");
		requete.append("\n select pere, pere_id, fils from (");
		requete.append("\n select a.pere, a.pere_id, a.fils, rk, dense_rank() over (partition by p, a.pere_id order by xsd_ordre) as r");
		requete.append("\n from tmp_num a left join tmp_regle b");
		requete.append("\n on a.pere=b.p and a.fils=b.f");
		requete.append("\n ) vv");
		requete.append("\n where rk!=r");
		requete.append("\n )");
		requete.append("\n INSERT into "+this.tableTempMark+" SELECT id_source, id, "+reg.getIdRegle()+" FROM "+ this.tableTempData + " ");
		requete.append("\n WHERE EXISTS (SELECT from tmp_check);");
		return requete.toString();
	}
	
	public String ctlCardinalite(RegleControleEntity reg, List<String> listRubriqueExpr, List<String> ListRubriqueTable) {
		
		// on retire le pere et le fils de la liste d'expr
		// on pourrait tout traiter pareil (pere, fils et autres...) mais vraiment pas optimisé
		listRubriqueExpr.remove(reg.getRubriquePere().toUpperCase());
		listRubriqueExpr.remove(reg.getRubriqueFils().toUpperCase());
		
		String cond = conditionCardinalite(reg);

		
		StringBuilder requete=new StringBuilder();
		requete.append("WITH null_transform AS (");
		requete.append("	SELECT id_source");
		requete.append("	,"+reg.getRubriquePere());

		// rubrique fille
		// cas 1 : présente dans la table
		// cas 2 : non present dans la table et identifiante
		// cas 3 : non present dans la table et valeur
		if (ListRubriqueTable.contains(reg.getRubriqueFils()))
		{
			requete.append("	,"+ reg.getRubriqueFils() +" ");
		}
		else
		{
			if (reg.getRubriqueFils().toLowerCase().startsWith("i_"))
			{
				requete.append("	, null::int as "+ reg.getRubriqueFils() +" ");
			}
			else
			{
				requete.append("	, null::text as "+ reg.getRubriqueFils() +" ");
			}
		}
		
		// rubriques de l'expression
		for (String s:listRubriqueExpr)
		{
			if (ListRubriqueTable.contains(s))
			{
			// TODO faut vraiment qu'on fasse une méthode pour les i_, v_...
				
				// on refait les identifiants pour que 
				// la valeur -1 corresponde au fait que la rubrique n'existe pas (null sur tout le groupe ou inexistante)
				// la valeur 0 corresponde au fait que c'est null
				if (s.toLowerCase().startsWith("i_"))
				{
					requete.append(", case when max("+s+") over (partition by "+reg.getRubriquePere()+") is null then -1 else "+s+" end as "+s+" ");
				}
				else
				{
					requete.append(","+s+" ");
				}
			}
			else
			{
				if (s.toLowerCase().startsWith("i_"))
				{
					requete.append(", -1::int as "+s+" ");
				}
				else
				{
					requete.append(", null::text as "+s+" ");
				}
			}
		}
		requete.append(" FROM "+ this.tableTempData + " ");
		requete.append(" ) ");
		
		requete.append(" , trav AS (");
		requete.append(" 	SELECT id_source, {2} FROM ");
		requete.append(" 		(SELECT DISTINCT id_source, {2}, {3} FROM ");
		
		
		requete.append("		(SELECT id_source, {2}, {3}, {2} IS NOT NULL ");
		
		// condition is written as a boolean inside select clause to be able to use windows aggregate functions
		if (reg.getCondition()!=null)
		{
			String condition =reg.getCondition();
			
			// On va recoder la condition à ce niveau
			// les i_ null présent dans l'expression vont etre recodé en 0
			condition=condition.replaceAll(ManipString.patternForIdRubriqueWithBrackets, "(case when $1 is null then 0 else $1 end)");
			
			condition=ManipString.extractAllRubrique(condition);
			
			requete.append("AND ("+condition+") ");
		}
		requete.append("AS condition");
		requete.append(" FROM null_transform ) foo0 ");
		requete.append(" WHERE condition ");
				
		requete.append(") foo ");
		requete.append(" 	GROUP BY id_source,{2} HAVING " + cond + ") ");
		requete.append(" , ctl AS (");
		requete.append("	SELECT a.id_source, a.id FROM " + this.tableTempData + " a ");
		requete.append(" 	INNER JOIN trav ON row(a.{2})::text collate \"C\"=row(trav.{2})::text collate \"C\" ");
		requete.append(" 	AND a.id_source=trav.id_source ");
		requete.append(" ) ");
		requete.append(" INSERT into {0} select id_source, id, '{1}' from ctl; ");

		return getRequete(requete.toString(), this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere(), reg.getRubriqueFils());
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
	 * Requête de contrôle si le contenu d'une rubrique est conforme à une expression régulière.
	 * Cette méthode inscrit les erreurs trouvées dans une table spécifique tableTempMark
	 * @param reg la règle à appliquer
	 * */
	public String ctlMatchesRegexp(RegleControleEntity reg) {
		String requete = "WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " "
		+ "			WHERE ({2} ~ '{3}') IS FALSE ) "
		+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere(), reg.getCondition());
		return requete;
	}

	/**
	 * Requête de contrôle si le contenu d'une rubrique correspond à une liste de valeurs acceptées.
	 * La liste peut être soit :
	 * <ul><li>une liste brute sql, ex : {@code 'Valeur 1', 'Valeur 2', Valeur 3'}</li>
	 * <li> une requête renvoyant la liste, ex : {@code select valeurs from nmcl_valeurs_55}</li></ul>
	 * Cette méthode inscrit les erreurs trouvées dans une table spécifique tableTempMark.
	 * @param reg la règle à appliquer
	 * */
	public String ctlIsValueIn(RegleControleEntity reg) {
		String requete = "WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " "
		+ "			WHERE {2} not in ({3}) ) "
		+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle(), reg.getRubriquePere(), reg.getCondition());
		return requete;
	}

	/** Requête de contrôle qui ne remonte jamais d'erreur.*/
	public String ctlAlwaysTrue(RegleControleEntity reg) {
		String requete = "WITH " + "ctl AS (	SELECT id_source,id " + "			FROM " + this.tableTempData + " "
				+ "			WHERE false ) "
				+ "INSERT into {0} select id_source, id, '{1}' from ctl; ";
		requete = getRequete(requete, this.tableTempMark, reg.getIdRegle());
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

		if (borneSup == null && borneInf!=null)
		{
			cond = "count({1})<{2}";
			cond = getRequete(cond, rubrique, rubriqueF, borneInf);
		} 
		
		if (borneSup != null && borneInf==null)
		{
			cond = "count({1})>{2}";
			cond = getRequete(cond, rubrique, rubriqueF, borneSup);
		}
		
		if (borneSup != null && borneInf!=null)
		{
			cond = "count({1})<{2} OR count({1})>{3}";
			cond = getRequete(cond, rubrique, rubriqueF, borneInf, borneSup);
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
		if (args.length == 0) {
			return req;
		}
		/* Long-winded solution to avoid involuntary recursion,
		 * e.g. getRequete("A{0}B{1}", "{1}", "zz") should return "A{1}Bzz" and not "AzzBzz".
		 * Two steps : decomposition of the string, then reconstruction with insertion of the replacements.*/
		LinkedList<String> decomposedString = new LinkedList<>();
		decomposedString.add(req);
		LinkedList<String> remplacementStrings = new LinkedList<>();
		for (int i = 0; i < args.length ; i++) {
			for (int j = 0; j < decomposedString.size() ; j++) {
				int indexOf;
				String stringToDecompose = decomposedString.get(j);
				String stringToRemplace = "{" + i +"}";
				while ((indexOf = stringToDecompose.indexOf(stringToRemplace)) != -1){
					String part1 = stringToDecompose.substring(0, indexOf);
					String part2 = stringToDecompose.substring(indexOf + stringToRemplace.length(), stringToDecompose.length());
					decomposedString.add(j, part2);
					decomposedString.remove(j+1);
					decomposedString.add(j, part1);
					remplacementStrings.add(j, args[i]);
					stringToDecompose = decomposedString.get(j);
				}
			}
		}
		StringBuilder finalString = new StringBuilder();
		for (int i = 0; i < decomposedString.size() ; i++) {
			finalString.append(decomposedString.get(i));
			if (remplacementStrings.size() > i) {
				finalString.append(remplacementStrings.get(i));
			}
		}
		return finalString.toString();
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