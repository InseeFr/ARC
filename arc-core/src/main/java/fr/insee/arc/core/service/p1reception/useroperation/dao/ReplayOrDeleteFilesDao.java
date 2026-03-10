package fr.insee.arc.core.service.p1reception.useroperation.dao;

import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementOperationFichier;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ReplayOrDeleteFilesDao {

	public ReplayOrDeleteFilesDao(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}

	private Sandbox sandbox;

	
	
	/**
	 * select archive container that had been flagged as to be replayed
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public List<String> execQuerySelectArchiveToReplay()
			throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.CONTAINER, SQL.FROM,
				ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()));
		
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE, SQL.IN);
		
		query.build(query.tupleOfValues(TraitementOperationFichier.R.getDbValue(), TraitementOperationFichier.RA.getDbValue()));

		return new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query))
				.getColumnValues(ColumnEnum.CONTAINER.getColumnName());

	}

	/**
	 * delete from pilotage archives flagged by the user as to be replayed
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public List<String> execQueryDeleteArchiveToReplay() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(SQL.WITH, ViewEnum.ALIAS_A, SQL.AS, "(");
		query.build(SQL.DELETE, ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()), SQL.AS, ViewEnum.ALIAS_A);
		
		query.build(SQL.WHERE, SQL.EXISTS, "(");
		
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()), SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE.alias(ViewEnum.ALIAS_B), "=",
				query.quoteText(TraitementOperationFichier.RA.getDbValue()));
		query.build(SQL.AND, ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_A), "=",
				ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_B));
		
		query.build(")");
		query.build(SQL.RETURNING, ColumnEnum.ID_SOURCE);
		query.build(")");
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.ID_SOURCE, SQL.FROM, ViewEnum.ALIAS_A);
		
		return new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query)).getColumnValues(ColumnEnum.ID_SOURCE.getColumnName());
	}

	/**
	 * delete from pilotage files flagged by the user as to be deleted
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public List<String> execQueryDeleteFileToDelete() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(SQL.WITH, ViewEnum.ALIAS_A, SQL.AS, "(");

		query.build(SQL.DELETE, ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()), SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.EXISTS, "(");
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()), SQL.AS, ViewEnum.ALIAS_B);
		
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE.alias(ViewEnum.ALIAS_B), "=",
				query.quoteText(TraitementOperationFichier.D.getDbValue()));
		query.build(SQL.AND, ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_A), "=",
				ColumnEnum.ID_SOURCE.alias(ViewEnum.ALIAS_B));
		query.build(SQL.AND, ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_A), "=",
				ColumnEnum.CONTAINER.alias(ViewEnum.ALIAS_B));
		
		query.build(")");
		query.build(SQL.RETURNING, ColumnEnum.ID_SOURCE);
		query.build(")");
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.ID_SOURCE, SQL.FROM, ViewEnum.ALIAS_A);
		
		return new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query)).getColumnValues(ColumnEnum.ID_SOURCE.getColumnName());
	}
}
