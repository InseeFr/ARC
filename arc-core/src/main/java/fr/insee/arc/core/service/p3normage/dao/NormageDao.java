package fr.insee.arc.core.service.p3normage.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class NormageDao {

	private Connection connection;

	public NormageDao(Connection connection) {
		this.connection = connection;
	}

	public List<String> execQuerySelectColumnsFromTable(String tableSource) throws ArcException {

		List<String> listeRubriqueSource = new ArrayList<>();
		UtilitaireDao.get(0).getColumns(connection, listeRubriqueSource, tableSource);
		return listeRubriqueSource;
	}

	public void execQueryRenamePartitionTable(String partitionTableName, String partitionTableNameWithAllRecords)
			throws ArcException {

		StringBuilder query = new StringBuilder(
				"alter table " + partitionTableName + " rename to " + partitionTableNameWithAllRecords + ";");
		UtilitaireDao.get(0).executeRequest(connection, query);
	}

}
