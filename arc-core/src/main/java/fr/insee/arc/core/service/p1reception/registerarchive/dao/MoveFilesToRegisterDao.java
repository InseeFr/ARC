package fr.insee.arc.core.service.p1reception.registerarchive.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

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

}
