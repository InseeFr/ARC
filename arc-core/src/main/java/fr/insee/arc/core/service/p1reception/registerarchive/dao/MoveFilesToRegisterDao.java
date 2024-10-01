package fr.insee.arc.core.service.p1reception.registerarchive.dao;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import io.micrometer.common.util.StringUtils;

public class MoveFilesToRegisterDao {

	public MoveFilesToRegisterDao(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}

	private Sandbox sandbox;

	public void registerArchive(String entrepot, String archiveName) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.INSERT_INTO, ViewEnum.PILOTAGE_ARCHIVE.getFullName(sandbox.getSchema()));
		query.build(query.tupleOfColumn(ColumnEnum.ENTREPOT.getColumnName(), ColumnEnum.NOM_ARCHIVE.getColumnName()));
		query.build(SQL.VALUES);
		query.build(query.tupleOfValues(entrepot, archiveName));
		query.build(SQL.END_QUERY);

		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query);

	}

	private String execQuerySelectReglesPrioriteFromEntrepot(String entrepot) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.REGLE_PRIORITE, SQL.FROM, ViewEnum.IHM_ENTREPOT.getFullName());
		query.build(SQL.WHERE, ColumnEnum.ID_ENTREPOT, '=', query.quoteText(entrepot));
		return UtilitaireDao.get(0).getString(this.sandbox.getConnection(), query);
	}

	/**
	 * Sort archives located in directory according to the user priority rules defined in entrepot gui
	 */
	public File[] sortArchives(String entrepot, File[] archives) throws ArcException {
		String reglePrioriteForEntrepot = execQuerySelectReglesPrioriteFromEntrepot(entrepot);

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, ViewEnum.T1, SQL.END_QUERY);
		query.build(SQL.CREATE, SQL.TEMPORARY, SQL.TABLE, ViewEnum.T1, "("
				, ColumnEnum.I, " ", TypeEnum.BIGINT, "," //
				,ColumnEnum.ARCHIVE_NAME, " ", TypeEnum.TEXT, "," //
				, ColumnEnum.ARCHIVE_SIZE, " ", TypeEnum.BIGINT, "," //
				, ColumnEnum.ARCHIVE_DATE, " ", TypeEnum.BIGINT //
				, ")", SQL.END_QUERY);

		UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query);

		query = new ArcPreparedStatementBuilder();
		for (int i = 0; i < archives.length; i++) {

			File f = archives[i];
			query.build(SQL.INSERT_INTO, ViewEnum.T1, SQL.VALUES, "("
					, query.quoteText(i + ""), SQL.CAST_OPERATOR, TypeEnum.BIGINT, "," //
					, query.quoteText(f.getName()), "," //
					, query.quoteText(f.getTotalSpace() + ""), SQL.CAST_OPERATOR, TypeEnum.BIGINT, "," //
					, query.quoteText(f.lastModified() + ""), SQL.CAST_OPERATOR, TypeEnum.BIGINT //
					, ")", SQL.END_QUERY);

			if ((i - 1) % 100 == 0) {
				UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query);
				query = new ArcPreparedStatementBuilder();
			}
		}
		if (query.getQuery().length()>0)
		{
			UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query);
		}
		
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.I, SQL.FROM, ViewEnum.T1, SQL.ORDER_BY);
		query.build(StringUtils.isBlank(reglePrioriteForEntrepot)? "" : reglePrioriteForEntrepot+",");
		query.build(ColumnEnum.ARCHIVE_NAME + " ASC ");
		List<String> orderedIndexes = new GenericBean(UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query)).getColumnValues(ColumnEnum.I.getColumnName());
		
		File[] sortedArchives = new File[archives.length];
		int index = 0;
		for (String orderedIndex:orderedIndexes)
		{
			sortedArchives[index]=archives[Integer.parseInt(orderedIndex)];
		}
		
		return sortedArchives;

	}

}
