package fr.insee.arc.core.service.global.dao;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class DataStorage {

	private DataStorage() {
		throw new IllegalStateException("dao class");
	}

	/**
	 * retrieve the data storage identifiers registered in database
	 * 
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectDatastorage(Connection connection) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.ID_ENTREPOT, SQL.FROM, ViewEnum.IHM_ENTREPOT.getFullName());
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query))
				.getColumnValues(ColumnEnum.ID_ENTREPOT.getColumnName());
	}

	/**
	 * register a list of files in a table
	 * 
	 * @param fichiers
	 * @return
	 * @throws ArcException
	 */
	public static void execQueryRegisterFilesInDatabase(Connection connection, List<File> files) throws ArcException {
		List<String> filenames = files.stream().filter(e -> !e.isDirectory()).map(e -> e.getName())
				.collect(Collectors.toList());

		CopyObjectsToDatabase.execCopyFromGenericBean(connection, ViewEnum.TMP_FILES.getTableName(), new GenericBean(
				ColumnEnum.FILE_NAME.getColumnName(), ColumnEnum.FILE_NAME.getColumnType().getTypeName(), filenames));

	}

	/**
	 * given a list of files, return the list of files with no trace in the
	 * pilotage_archive table
	 * 
	 * @param connection
	 * @param sanbox
	 * @return
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectFilesNotInRegisteredArchives(Connection connection, String sanbox)
			throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.FILE_NAME, SQL.FROM, ViewEnum.TMP_FILES, SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.NOT, SQL.EXISTS);
		query.build("(");
		query.build(SQL.SELECT, SQL.FROM, ViewEnum.PILOTAGE_ARCHIVE.getFullName(sanbox), SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.NOM_ARCHIVE.alias(ViewEnum.ALIAS_B), "=",
				ColumnEnum.FILE_NAME.alias(ViewEnum.ALIAS_A));
		query.build(")");

		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query))
				.getColumnValues(ColumnEnum.FILE_NAME.getColumnName());
	}

}
