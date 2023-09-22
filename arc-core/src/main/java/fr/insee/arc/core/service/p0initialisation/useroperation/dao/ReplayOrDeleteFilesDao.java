package fr.insee.arc.core.service.p0initialisation.useroperation.dao;

import java.sql.Connection;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementOperationFichier;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ReplayOrDeleteFilesDao {

	private ReplayOrDeleteFilesDao() {
		throw new IllegalStateException("DAO class");
	}

	/**
	 * select archive container that had been flagged as to be replayed
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectArchiveToReplay(Connection connection, String schema)
			throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.CONTAINER, SQL.FROM,
				ViewEnum.PILOTAGE_FICHIER.getFullName(schema));
		
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE, SQL.IN);
		
		query.build(query.tuple(TraitementOperationFichier.R.getDbValue(), TraitementOperationFichier.RA.getDbValue()));

		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query))
				.getColumnValues(ColumnEnum.CONTAINER.getColumnName());

	}

	/**
	 * delete from pilotage archives flagged by the user as to be replayed
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public static void execQueryDeleteArchiveToReplay(Connection connection, String schema) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.DELETE, ViewEnum.PILOTAGE_FICHIER.getFullName(schema), SQL.AS, ViewEnum.ALIAS_A);
		
		query.build(SQL.WHERE, SQL.EXISTS, "(");
		
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(schema), SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE.alias(ViewEnum.ALIAS_B), "=",
				query.quoteText(TraitementOperationFichier.RA.getDbValue()));
		query.build(SQL.AND, ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_A), "=",
				ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_B));
		
		query.build(")");

		UtilitaireDao.get(0).executeRequest(connection, query);
	}

	/**
	 * delete from pilotage files flagged by the user as to be deleted
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public static void execQueryDeleteFileToDelete(Connection connection, String schema) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(SQL.DELETE, ViewEnum.PILOTAGE_FICHIER.getFullName(schema), SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.EXISTS, "(");
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(schema), SQL.AS, ViewEnum.ALIAS_B);
		
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE.alias(ViewEnum.ALIAS_B), "=",
				query.quoteText(TraitementOperationFichier.D.getDbValue()));
		query.build(SQL.AND, ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_A), "=",
				ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_B));
		query.build(SQL.AND, ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_A), "=",
				ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_B));
		
		query.build(")");
		UtilitaireDao.get(0).executeRequest(connection, query);
	}
}
