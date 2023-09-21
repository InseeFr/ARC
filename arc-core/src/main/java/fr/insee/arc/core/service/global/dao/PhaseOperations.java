package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class PhaseOperations {

	private PhaseOperations() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * retrieve phase token from database object name
	 * @param tableName
	 * @return
	 */
	public static TraitementPhase extractPhaseFromTableName(String tableName)
	{
		return TraitementPhase.valueOf(ManipString.substringBeforeFirst(FormatSQL.extractTableNameToken(tableName) , "_")
				.toUpperCase());
	}
	
	/**
	 * retrieve state token from database object name
	 * @param tableName
	 * @return
	 */
	public static TraitementEtat extractEtatFromTableName(String tableName)
	{
		return TraitementEtat.valueOf(ManipString.substringAfterLast(tableName, "_").toUpperCase());
	}
	
	/**
	 * recupere toutes les tables d'état d'un envrionnement
	 *
	 * @param env
	 * @return
	 */
	private static ArcPreparedStatementBuilder selectPhaseDataTablesFoundInEnv(String env) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		TraitementPhase[] phase = TraitementPhase.values();
		
		boolean insert = false;

		for (int i = 0; i < phase.length; i++) {
			if (insert) {
				requete.append(" UNION ALL ");
			}
			ArcPreparedStatementBuilder r = selectDataTablesFoundInPhaseAndEnv(env, phase[i].toString());
			insert = (r.length() > 0);
			requete.append(r);
		}
		return requete;
	}
	
	/**
	 * retrieve phase data tables in the given environment sandbox schema
	 * return an empty list if not found
	 * @param connection
	 * @param schema
	 * @return
	 * @throws ArcException
	 */
	public static List<String> selectPhaseDataTablesFoundInEnv(Connection connection, String env) throws ArcException {
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection,
				PhaseOperations.selectPhaseDataTablesFoundInEnv(env))).getColumnValues(ColumnEnum.TABLE_NAME.getColumnName());
	}

	/**
	 * retrieve the children table for a given 
	 * @param connection
	 * @param phaseTemplateTable
	 * @return
	 * @throws ArcException
	 */
	public static List<String> selectAllChildrenPhaseDataTables(Connection connection, String phaseTemplateTable) throws ArcException {
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection,
				FormatSQL.tableExists(phaseTemplateTable + "\\_" + HashFileNameConversion.CHILD_TABLE_TOKEN + "\\_%"))).getColumnValues(ColumnEnum.TABLE_NAME.getColumnName());
	}
	
	/**
	 * Retrieve the filename idSource of a dataTable
	 * @param connection
	 * @param dataTable
	 * @return
	 * @throws ArcException
	 */
	public static String selectIdSourceOfChildDataTable(Connection connection, String dataTable) throws ArcException
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.ID_SOURCE, SQL.FROM, dataTable, SQL.LIMIT, "1");
		return UtilitaireDao.get(0).getString(connection, query);
	}
	
	
	
	/**
	 * retrieve the list of documents (i.e. files referenced in the field "id_source") in the data table 
	 * @param dataTable
	 * @return
	 * @throws ArcException 
	 */
	public static List<String> selectIdSourceOfDataTable(Connection connection, String dataTable) throws ArcException
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(); 
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.ID_SOURCE.getColumnName(), SQL.FROM, dataTable);
		
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query)).getColumnValues(ColumnEnum.ID_SOURCE.getColumnName());
	}
	
	
	/**
	 * Query phase data tables
	 * @param env
	 * @param phase
	 * @return
	 */
	private static ArcPreparedStatementBuilder selectDataTablesFoundInPhaseAndEnv(String env, String phase) {
		// Les tables dans l'environnement sont de la forme
		TraitementEtat[] etat = TraitementEtat.values();
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		for (int j = 0; j < etat.length; j++) {
			if (!etat[j].equals(TraitementEtat.ENCOURS)) {
				if (j > 0) {
					requete.append(" UNION ALL ");
				}
				requete.append(FormatSQL.tableExists(TableNaming.dbEnv(env) + phase + "%\\_" + etat[j]));
			}
		}
		return requete;
	}
	
}
