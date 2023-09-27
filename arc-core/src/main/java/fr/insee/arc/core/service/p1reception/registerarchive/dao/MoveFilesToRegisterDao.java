package fr.insee.arc.core.service.p1reception.registerarchive.dao;

import java.sql.Connection;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class MoveFilesToRegisterDao {
	
	/**
	 * select archive container that had been flagged as to be replayed
	 * 
	 * @param connection
	 * @param schema
	 * @throws ArcException
	 */
	public static List<String> execQuerySelectDatawarehouses(Connection connection) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.ID_ENTREPOT, SQL.FROM, ViewEnum.IHM_ENTREPOT.getFullName());

		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query))
				.getColumnValues(ColumnEnum.ID_ENTREPOT.getColumnName());

	}
	
	
	public static void registerArchive(Sandbox sandbox, String entrepot, String archiveName) throws ArcException {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.INSERT_INTO, ViewEnum.PILOTAGE_ARCHIVE.getFullName(sandbox.getSchema()));
		query.build(query.tupleOfColumn(ColumnEnum.ENTREPOT.getColumnName(), ColumnEnum.NOM_ARCHIVE.getColumnName()));
		query.build(SQL.VALUES);
		query.build(query.tupleOfValues(entrepot, archiveName));
		query.build(SQL.END_QUERY);
		
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query);
//		
		
//		UtilitaireDao.get(0).executeBlock(sandbox.getConnection(),
//				"INSERT INTO " + TableNaming.dbEnv(sandbox.getSchema())
//						+ "pilotage_archive (entrepot,nom_archive) values ('" + entrepot + "','" + reworkInstance.getReworkedArchiveName()
//						+ "'); ");
	}

}
