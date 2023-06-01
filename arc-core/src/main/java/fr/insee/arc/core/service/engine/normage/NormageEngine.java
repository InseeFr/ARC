package fr.insee.arc.core.service.engine.normage;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import fr.insee.arc.utils.dao.Parameter;
import fr.insee.arc.utils.dao.ParameterType;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.ManipString;

public class NormageEngine {

	private static final Logger LOGGER = LogManager.getLogger(NormageEngine.class);

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private StringBuilder columnToBeAdded = new StringBuilder();

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

				if (listeRubriqueSource.contains("i_" + NormageEngineGlobal.getCoreVariableName(variable))) {
					alreadyAdded.add("i_" + NormageEngineGlobal.getCoreVariableName(variable));
					reqSelect.append(", i_" + NormageEngineGlobal.getCoreVariableName(variable));
				}

				if (listeRubriqueSource.contains("v_" + NormageEngineGlobal.getCoreVariableName(variable))) {
					alreadyAdded.add("v_" + NormageEngineGlobal.getCoreVariableName(variable));
					reqSelect.append(", v_" + NormageEngineGlobal.getCoreVariableName(variable));
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
			throw new ArcException(e,ArcExceptionMessage.NORMAGE_VALIDITE_DATE_PARSE_FAILED,pilotageIdSource.get("validite").get(0));
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
			// En ihm, on garde toutes les rubriques
			// pour que les gens qui testent en bac à sable puissent utiliser toutes les rubriques
			if (paramBatch != null) {
				NormageEngineRegleSupression.ajouterRegleSuppression(regle, norme, periodicite, subJoin, rubriqueUtiliseeDansRegles);

				subJoin = NormageEngineRegleSupression.appliquerRegleSuppression(regle, norme, validite, periodicite, subJoin);
			}

			NormageEngineRegleDuplication.ajouterRegleDuplication(regle, norme, validite, periodicite, subJoin);

			subJoin = NormageEngineRegleDuplication.appliquerRegleDuplication(regle, norme, validite, periodicite, subJoin, columnToBeAdded);

			NormageEngineRegleIndependance.ajouterRegleIndependance(regle, norme, validite, periodicite, subJoin);

			subJoin = NormageEngineRegleIndependance.appliquerRegleIndependance(regle, norme, validite, periodicite, subJoin);

			subJoin = NormageEngineRegleUnicite.appliquerRegleUnicite(regle, subJoin);

			subJoin = NormageEngineRegleRelation.appliquerRegleRelation(regle, subJoin);

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
			bloc4.append("\n create temporary table " + partitionTableName + " as ");
			bloc4.append("\n SELECT * FROM "+ partitionTableNameWithAllRecords + " ");
			bloc4.append("\n WHERE " + partitionIdentifier + ">="+bloc4.quoteInt(null));
			bloc4.append("\n AND "	+ partitionIdentifier + "<"+bloc4.quoteInt(null));
			bloc4.append("\n ;");
			bloc4.append("\n analyze " + partitionTableName + ";");
			bloc4.append(blocInsert);

			bloc4 = applyQueryPlanParametersOnJointure(bloc4, statementTimeOut);

			// iterate through chunks
			int iterate = 1;
			do {
				Parameter<Integer> lowerbound= new Parameter<>(iterate, ParameterType.INT);
				Parameter<Integer> upperbound= new Parameter<>((iterate + chunkSize), ParameterType.INT);
				
				bloc4.getParameters().set(0, lowerbound);
				bloc4.getParameters().set(1, upperbound);
				
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
		return query.replace("{table_source}", tableSource).replace("{table_destination}", tableDestination)
				.replace("{id_norme}", norme).replace("{validite}", validiteText).replace("{periodicite}", periodicite)
				.replace("{nom_fichier}", idSource);
	}




}
