package fr.insee.arc.ws.services.restServices.execute.dao;

import java.sql.Connection;

import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.p4controle.dao.ThreadControleQueryBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ExecuteEngineControleDao {

	private ExecuteEngineControleDao() {
		    throw new IllegalStateException("Utility dao class");
		  }

	public static void execQueryCreateControleTable(Connection connection, String inputTable, String outputTable)
			throws ArcException {
		String query = TableOperations.createTableTravail(inputTable, outputTable,
				ThreadControleQueryBuilder.extraColumnsAddedByControle());

		UtilitaireDao.get(0).executeImmediate(connection, query);
	}

}
