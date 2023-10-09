package fr.insee.arc.ws.services.importServlet.dao;

import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class NameDao {

	
	public static List<List<String>> execQuerySelectMetadata(String tableName) throws ArcException
	{
		return UtilitaireDao.get(0).executeRequest(null,new ArcPreparedStatementBuilder("select * from " + tableName + " where false "));
	}
			
	
}
