package fr.insee.arc.core.service.p3normage.operation;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p3normage.bo.JoinParser;
import fr.insee.arc.core.service.p3normage.bo.RegleNormage;
import fr.insee.arc.core.service.p3normage.bo.TypeNormage;
import fr.insee.arc.core.service.p3normage.dao.NormageDao;
import fr.insee.arc.core.service.p3normage.querybuilder.DuplicationRulesRegleQueryBuilder;
import fr.insee.arc.core.service.p3normage.querybuilder.UniciteRulesQueryBuilder;
import fr.insee.arc.core.service.p3normage.querybuilder.IndependanceRulesQueryBuilder;
import fr.insee.arc.core.service.p3normage.querybuilder.RelationRulesQueryBuilder;
import fr.insee.arc.core.service.p3normage.querybuilder.SuppressionRulesQueryBuilder;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.Parameter;
import fr.insee.arc.utils.dao.ParameterType;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;

//@formatter:off
/**
 * Class that exposes methods to execute the normage phase
 * 
 * TO REFACTOR
 * 1. to use a serialization format for "jointure"
 * 2. to make a real parser / real object 
 * 3. to avoid any strings manipulation REFACTOR in progress eta december 2023
 */
//@formatter:on
public class NormageOperation {

	private static final Logger LOGGER = LogManager.getLogger(NormageOperation.class);
	private StringBuilder columnToBeAdded = new StringBuilder();

	private Connection connection;

	/**
	 * liste des rubriques présentes dans le fichier idSource et réutilisées dans
	 * les phase en aval Ces rubriques "var" sont à conserver id_norme,
	 * validite_inf, validite_sup, periodicite, var
	 */
	private Map<String, List<String>> rubriqueUtiliseeDansRegles;

	private String tableSource;

	private String tableDestination;

	private String paramBatch;

	private NormageDao dao;

	private FileIdCard fileIdCard;

	// deprecated but requires patch in clients database
	public static final String JOINXML_STRUCTURE_BLOCK = "\n -- structure\n";

	public NormageOperation(Connection connection, FileIdCard fileIdCard,
			Map<String, List<String>> rubriqueUtiliseeDansRegles, String tableSource, String tableDestination,
			String paramBatch) {
		super();
		this.connection = connection;
		this.fileIdCard = fileIdCard;
		this.rubriqueUtiliseeDansRegles = rubriqueUtiliseeDansRegles;
		this.tableSource = tableSource;
		this.tableDestination = tableDestination;
		this.paramBatch = paramBatch;
		dao = new NormageDao(this.connection);
	}

	public void executeEngine() throws ArcException {
		execute();
	}

	public void execute() throws ArcException {

		String jointure = fileIdCard.getJointure();

		if (jointure == null || jointure.equals("")) {
			simpleExecutionWithNoJoinDefined();
		} else {
			complexExecutionWithJoinDefined();
		}

	}

	/**
	 * This method execute the basic engine with no xml join set
	 * 
	 * @throws ArcException
	 */
	private void simpleExecutionWithNoJoinDefined() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "Normage simple sans règles de jointure");

		StringBuilder reqSelect = new StringBuilder();
		reqSelect.append(ColumnEnum.ID_SOURCE) //
				.append(",").append(ColumnEnum.ID) //
				.append(",").append(ColumnEnum.DATE_INTEGRATION) //
				.append(",").append(ColumnEnum.ID_NORME) //
				.append(",").append(ColumnEnum.VALIDITE) //
				.append(",").append(ColumnEnum.PERIODICITE) //
		;

		List<String> listeRubriqueSource = dao.execQuerySelectColumnsFromTable(tableSource);

		Set<String> alreadyAdded = new HashSet<>();

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

				if (listeRubriqueSource.contains("i_" + JoinParser.getCoreVariableName(variable))) {
					alreadyAdded.add("i_" + JoinParser.getCoreVariableName(variable));
					reqSelect.append(", i_" + JoinParser.getCoreVariableName(variable));
				}

				if (listeRubriqueSource.contains("v_" + JoinParser.getCoreVariableName(variable))) {
					alreadyAdded.add("v_" + JoinParser.getCoreVariableName(variable));
					reqSelect.append(", v_" + JoinParser.getCoreVariableName(variable));
				}
			}
		}

		StringBuilder query = new StringBuilder();
		query.append("\n CREATE TEMPORARY TABLE " + tableDestination + " ");
		query.append("\n  AS SELECT ");
		query.append(reqSelect);
		query.append(" FROM " + tableSource + " ;");

		UtilitaireDao.get(0).executeRequest(connection, query);
	}

	/**
	 * This method execute the engine with a join defined the join will be modified
	 * according to the user rules
	 * 
	 * @param jointure
	 * @throws ArcException
	 */
	private void complexExecutionWithJoinDefined() throws ArcException {

		// rework jointure to keep structure only
		this.fileIdCard.setJointure(this.fileIdCard.getJointure().split(JOINXML_STRUCTURE_BLOCK)[0].toLowerCase());

		// ORDRE IMPORTANT
		// on supprime les rubriques inutilisées quand le service est invoqué en batch
		// En ihm, on garde toutes les rubriques
		// pour que les gens qui testent en bac à sable puissent utiliser toutes les
		// rubriques
		if (paramBatch != null) {
			SuppressionRulesQueryBuilder.ajouterRegleSuppression(fileIdCard, rubriqueUtiliseeDansRegles);

			this.fileIdCard.setJointure(SuppressionRulesQueryBuilder.appliquerRegleSuppression(fileIdCard));
		}

		DuplicationRulesRegleQueryBuilder.ajouterRegleDuplication(fileIdCard);

		this.fileIdCard
				.setJointure(DuplicationRulesRegleQueryBuilder.appliquerRegleDuplication(fileIdCard, columnToBeAdded));

		IndependanceRulesQueryBuilder.ajouterRegleIndependance(fileIdCard);

		this.fileIdCard.setJointure(IndependanceRulesQueryBuilder.appliquerRegleIndependance(fileIdCard));

		this.fileIdCard.setJointure(UniciteRulesQueryBuilder.appliquerRegleUnicite(fileIdCard));

		this.fileIdCard.setJointure(RelationRulesQueryBuilder.appliquerRegleRelation(fileIdCard));

		this.fileIdCard.setJointure(optimisation(fileIdCard));

		executerJointure(fileIdCard);

	}

	private String optimisation(FileIdCard fileIdCard) {
		// on enleve l'id
		String r = fileIdCard.getJointure();
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
		Matcher m = p.matcher(fileIdCard.getJointure());
		while (m.find()) {
			analyze.append("\n analyze " + m.group(1) + ";");
		}

		// on intègre le calcul des statistiques des tables utilisées
		r = ManipString.substringBeforeFirst(r, "insert into {table_destination}") + analyze
				+ "\n insert into {table_destination}"
				+ ManipString.substringAfterFirst(r, "insert into {table_destination}");

		// on crée la table destination avec les bonnes colonnes pour la premiere sous
		// jointure
		r = "drop table if exists {table_destination}; create temporary table {table_destination} as SELECT * FROM {table_source} where false; \n "
				+ this.columnToBeAdded + "\n" + r;
		return r;
	}

	private ArcPreparedStatementBuilder applyQueryPlanParametersOnJointure(ArcPreparedStatementBuilder query) {
		ArcPreparedStatementBuilder r = new ArcPreparedStatementBuilder();
		// seqscan on can be detrimental on some rare large file
		r.append("set enable_nestloop=off;\n");
		r.append("commit;");
		r.append(query);
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
	private void executerJointure(FileIdCard fileIdCard) throws ArcException {

		StaticLoggerDispatcher.info(LOGGER, "Normage avec jointure sur " + fileIdCard.getIdSource());

		List<RegleNormage> reglesPartition = fileIdCard.getIdCardNormage().getReglesNormage(TypeNormage.PARTITION);

		if (!reglesPartition.isEmpty()) {
			String element = reglesPartition.get(0).getRubrique();
			int minSize = Integer.parseInt(reglesPartition.get(0).getRubriqueNmcl().split(",")[0]);
			int chunkSize = Integer.parseInt(reglesPartition.get(0).getRubriqueNmcl().split(",")[1]);
			executerJointureWithPartition(fileIdCard, element, minSize, chunkSize);
		} else {
			// No partition rules found; normal execution
			UtilitaireDao.get(0).executeRequest(connection,
					applyQueryPlanParametersOnJointure(replaceQueryParameters(fileIdCard.getJointure(), fileIdCard)));
		}

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
	private void executerJointureWithPartition(FileIdCard fileIdCard, String element, int minSize, int chunkSize)
			throws ArcException {

		StaticLoggerDispatcher.info(LOGGER, "Normage avec règle de partition " + fileIdCard.getIdSource());

		
		String delimiterForBlocInsert = "\n insert into {table_destination} ";
		
		/* get the query blocks */
		String blocCreate = ManipString.substringBeforeFirst(fileIdCard.getJointure(), delimiterForBlocInsert);
		String blocInsert = delimiterForBlocInsert
				+ ManipString.substringAfterFirst(fileIdCard.getJointure(), delimiterForBlocInsert);

		// rework create block to get the number of record in partition if the rubrique
		// is found

		// watch out the independance rules which can put the partition rubrique in
		// another table than t_rubrique
		String partitionTableName = "";
		String partitionIdentifier = " m_" + element + " ";

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

		int total = UtilitaireDao.get(0).getInt(connection, new ArcPreparedStatementBuilder(replaceQueryIdentifier(blocCreate)));

		ArcPreparedStatementBuilder queryInsert = replaceQueryParameters(blocInsert, fileIdCard);

		// partition if and only if enough records
		if (total >= minSize) {
			
			String partitionTableNameWithAllRecords = "all_" + partitionTableName;

			// rename the table to split
			dao.execQueryRenamePartitionTable(partitionTableName, partitionTableNameWithAllRecords);

			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.append("\n drop table if exists " + partitionTableName + ";");
			query.append("\n create temporary table " + partitionTableName + " as ");
			query.append("\n SELECT * FROM " + partitionTableNameWithAllRecords + " ");
			query.append("\n WHERE " + partitionIdentifier + ">=" + query.quoteInt(null));
			query.append("\n AND " + partitionIdentifier + "<" + query.quoteInt(null));
			query.append("\n ;");
			query.append("\n analyze " + partitionTableName + ";");
			query.append(queryInsert);

			query = applyQueryPlanParametersOnJointure(query);

			// iterate through chunks
			int iterate = 1;
			do {

				Parameter<Integer> lowerbound = new Parameter<>(iterate, ParameterType.INT);
				Parameter<Integer> upperbound = new Parameter<>((iterate + chunkSize), ParameterType.INT);

				query.getParameters().set(0, lowerbound);
				query.getParameters().set(1, upperbound);

				UtilitaireDao.get(0).executeRequest(connection, query);

				iterate = iterate + chunkSize;

			} while (iterate <= total);

		} else
		// no partitions needed
		{
			UtilitaireDao.get(0).executeRequest(connection, applyQueryPlanParametersOnJointure(queryInsert));
		}
	}

	
	private String replaceQueryIdentifier(String query)
	{
		return query.replace("{table_source}", tableSource) //
				.replace("{table_destination}", tableDestination);
	}
	
	private ArcPreparedStatementBuilder replaceQueryParameters(String query, FileIdCard fileIdCard) {
		
		query = replaceQueryIdentifier(query);
		
		int numberOfBlockOfParameters = StringUtils.countMatches(query, "'{nom_fichier}'");

		query = query
				.replace("'{nom_fichier}'", GenericPreparedStatementBuilder.BIND_VARIABLE_PLACEHOLDER)
				.replace("'{id_norme}'", GenericPreparedStatementBuilder.BIND_VARIABLE_PLACEHOLDER)
				.replace("'{validite}'", GenericPreparedStatementBuilder.BIND_VARIABLE_PLACEHOLDER)
				.replace("'{periodicite}'", GenericPreparedStatementBuilder.BIND_VARIABLE_PLACEHOLDER)
				;

		ArcPreparedStatementBuilder pstmtQuery = new ArcPreparedStatementBuilder(query);
		
		for (int i= 0; i<numberOfBlockOfParameters; i++ )
		{
			pstmtQuery.addText(fileIdCard.getIdSource());	
			pstmtQuery.addText(fileIdCard.getIdNorme());	
			pstmtQuery.addText(fileIdCard.getValidite());	
			pstmtQuery.addText(fileIdCard.getPeriodicite());	
		}
		
		return pstmtQuery;
	}

}
