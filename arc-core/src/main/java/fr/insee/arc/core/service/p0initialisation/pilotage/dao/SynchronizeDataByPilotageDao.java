package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.dao.TableMetadata;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class SynchronizeDataByPilotageDao {

	private SynchronizeDataByPilotageDao() {
		throw new IllegalStateException("static dao : most method could be used by functional interface to be sent on several connections");
	}

	/**
	 * remove temporary states from pilotage table
	 * 
	 * @param connection
	 * @param envExecution
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	public static void resetEtapePilotageDao(Connection connection, String envExecution) throws ArcException {
		String tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		requete.append("DELETE FROM " + tablePil + " WHERE etat_traitement='{ENCOURS}';");

		requete.append(PilotageOperations.queryResetPreviousPhaseMark(tablePil, null, null));

		requete.append("WITH tmp_1 as (select " + ColumnEnum.ID_SOURCE.getColumnName() + ", max(");
		new StringBuilder();
		requete.append("case ");
		for (TraitementPhase p : TraitementPhase.values()) {
			requete.append("when phase_traitement='" + p.toString() + "' then " + p.ordinal() + " ");
		}
		requete.append("end ) as p ");
		requete.append("FROM " + tablePil + " ");
		requete.append("GROUP BY " + ColumnEnum.ID_SOURCE.getColumnName() + " ");
		requete.append("having max(etape)=0 ) ");
		requete.append("update " + tablePil + " a ");
		requete.append("set etape=1 ");
		requete.append("from tmp_1 b ");
		requete.append(
				"where a." + ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName() + " ");
		requete.append("and a.phase_traitement= case ");
		for (TraitementPhase p : TraitementPhase.values()) {
			requete.append("when p=" + p.ordinal() + " then '" + p.toString() + "' ");
		}
		requete.append("end ; ");

		UtilitaireDao.get(0).executeBlock(connection, requete);
	}

	/**
	 * rebuild to defragment pilotage table
	 * 
	 * @param connexion
	 * @param envExecution
	 * @throws ArcException
	 */
	public static void rebuildPilotageDao(Connection connexion, String envExecution) throws ArcException {
		
		String tablePilotageFichier = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);
		String tablePilotageArchive = ViewEnum.PILOTAGE_ARCHIVE.getFullName(envExecution);
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		// transactionnal bloc !!important to do that in a single transaction!! 
		query.build(SQL.BEGIN);
		query.append(TableMetadata.rebuildTable(tablePilotageFichier, new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, TableMetadata.queryIndexesInformations(tablePilotageFichier)))));
		query.append(TableMetadata.rebuildTable(tablePilotageArchive));
		query.build(SQL.COMMIT, SQL.END_QUERY);
		
		UtilitaireDao.get(0).executeRequest(connexion, query);
	}

	
	/**
	 * Récupere toutes les tables temporaires d'un environnement
	 *
	 * @param env
	 * @return
	 */
	@SqlInjectionChecked
	public static ArcPreparedStatementBuilder requeteListAllTemporaryTablesInEnv(String envExecution) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(FormatSQL.tableExists(ViewEnum.getFullName(envExecution, "%" + FormatSQL.TMP+ "%")));
		return requete;
	}


	/**
	 * materialize on executor nod a table containing the list of idSource provided
	 * @param executorConnection
	 * @param idSourceToDelete
	 * @throws ArcException
	 */
	public static void execQueryMaterializeOnExecutorIdSource(Connection executorConnection,
			String tableOfIdSource, List<String> idSourceToDelete) throws ArcException {
		
		GenericBean gb = new GenericBean(ColumnEnum.ID_SOURCE.getColumnName(), TypeEnum.TEXT.getTypeName(),
				idSourceToDelete);
		
		// materialize
		CopyObjectsToDatabase.execCopyFromGenericBean(executorConnection, tableOfIdSource, gb);

		}

	
	/**
	 * Delete the records from a target data table according to a given list of id_source
	 * to delete
	 * 
	 * @param executorConnection
	 * @param targetDataTable
	 * @throws ArcException
	 */
	public static void deleteDataRecordsFoundInIdSource(Connection executorConnection, String tableOfIdSource, String targetDataTable) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.DELETE, targetDataTable, SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.EXISTS);
		query.build("(");
		query.build(SQL.SELECT, SQL.FROM, tableOfIdSource, SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_A), "=", ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_B));
		query.build(")");
		query.build(SQL.END_QUERY);
		UtilitaireDao.get(0).executeRequest(executorConnection, query);
	}



	/**
	 * Delete the records from a target data table that are not found in a given list of id_source
	 * 
	 * @param executorConnection
	 * @param targetDataTable
	 * @throws ArcException
	 */
	public static void keepDataRecordsFoundInIdSourceOnly(Connection executorConnection, String tableOfIdsource, String targetDataTable) throws ArcException {
		
		String targetDataTableImg = FormatSQL.imageObjectName(targetDataTable);
		String targetDataTableIdentifier = FormatSQL.extractTableNameToken(targetDataTable);
		
		// recreate full table instead of delete as large volume of data may be involved
		// this operation occurs once a week
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.build(FormatSQL.analyzeSecured(tableOfIdsource));
		
		query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, targetDataTableImg, SQL.END_QUERY);
		
		query.build(SQL.CREATE, SQL.TABLE, targetDataTableImg, FormatSQL.WITH_NO_VACUUM, SQL.AS);
		query.build(SQL.SELECT, "*", SQL.FROM, targetDataTable, SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.EXISTS);
		query.build("(");
		query.build(SQL.SELECT, SQL.FROM, tableOfIdsource, SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_A), "=", ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_B));
		query.build(")");
		query.build(SQL.END_QUERY);
		
		query.build(SQL.DROP, SQL.TABLE, targetDataTable, SQL.END_QUERY);
		query.build(SQL.ALTER, SQL.TABLE, targetDataTableImg, SQL.RENAME_TO, targetDataTableIdentifier, SQL.END_QUERY);
		
		query.build(SQL.ANALYZE, targetDataTable, SQL.END_QUERY);
		
		UtilitaireDao.get(0).executeRequest(executorConnection, query);
	}


	public static void dropTable(Connection executorConnection, String table) {
		UtilitaireDao.get(0).dropTable(executorConnection, table);
	}

	public static void dropDataTables(Connection executorConnection, List<String> dataTablesToDrop) {
		UtilitaireDao.get(0).dropTable(executorConnection, dataTablesToDrop);
	}


	/**
	 * drop the unused temporary table on the target connection / environment
	 * 
	 * @param targetConnexion
	 * @throws ArcException
	 */
	public static void dropUnusedTemporaryTablesOnConnection(Connection targetConnexion, String envExecution) throws ArcException {
		GenericBean g = new GenericBean(
				UtilitaireDao.get(0).executeRequest(targetConnexion, SynchronizeDataByPilotageDao.requeteListAllTemporaryTablesInEnv(envExecution)));
		if (!g.mapContent().isEmpty()) {
			List<String> envTables = g.mapContent().get("table_name");
			for (String nomTable : envTables) {
				UtilitaireDao.get(0).executeBlock(targetConnexion, FormatSQL.dropTable(nomTable));
			}
		}
	}

	/**
	 * Create the table TABLES_WITH_RECORDS 
	 * This table is meant to held the name of the mapping tables containing at least one record 
	 * @param targetConnexion
	 * @param envExecution
	 * @throws ArcException 
	 */
	public static void unregisterTablesWithRecord(Connection targetConnexion, String envExecution) throws ArcException {
	
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(FormatSQL.dropTable(ViewEnum.TABLES_WITH_RECORDS.getFullName(envExecution)));
		query.build(SQL.CREATE, SQL.TABLE, ViewEnum.TABLES_WITH_RECORDS.getFullName(envExecution)); 
		query.build("(",query.sqlDDLOfColumnsFromModel(ViewEnum.TABLES_WITH_RECORDS),")");
		
		UtilitaireDao.get(0).executeRequest(targetConnexion, query);
		
	}

	/**
	 * insert into TABLES_WITH_RECORDS the name of the mapping tables containing at least one record 
	 * @param targetConnexion
	 * @param envExecution
	 * @param dataTables
	 * @throws ArcException 
	 */
	public static void registerTablesWithRecord(Connection targetConnexion, String envExecution, List<String> dataTables) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.INSERT_INTO, ViewEnum.TABLES_WITH_RECORDS.getFullName(envExecution));
		query.build("(", query.sqlListeOfColumnsFromModel(ViewEnum.TABLES_WITH_RECORDS), ")");
		query.build(SQL.VALUES);
		query.build(dataTables.stream().map(dataTable -> "(" + query.quoteText(dataTable) + ")" ).collect( Collectors.joining( "," )));
		query.build(SQL.END_QUERY);
		UtilitaireDao.get(0).executeRequest(targetConnexion, query);
		
	}

	/**
	 * test if table is empty
	 * @param connection
	 * @param nomTable
	 * @return
	 */
	public static boolean isDataTableHasRecords(Connection connection, String nomTable) {
		try {
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.build(SQL.SELECT, SQL.FROM, nomTable, SQL.LIMIT, "1");
			return UtilitaireDao.get(0).hasResults(connection, query);
		} catch (ArcException e) {
			return false;
		}
	}

	
	/**
	 * Query the mapping tables saved in TABLES_WITH_RECORDS : the ones that has at least one record
	 * @param targetConnexion
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQueryMappingTablesWithRecords(Connection targetConnexion, String envExecution) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.NOM_TABLE_METIER , SQL.FROM, ViewEnum.TABLES_WITH_RECORDS.getFullName(envExecution));
		return new GenericBean(UtilitaireDao.get(0).executeRequest(targetConnexion, query)).getColumnValues(ColumnEnum.NOM_TABLE_METIER.getColumnName());
	}
	
}
