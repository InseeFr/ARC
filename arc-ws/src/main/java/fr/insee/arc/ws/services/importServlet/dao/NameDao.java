package fr.insee.arc.ws.services.importServlet.dao;

import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;

public class NameDao {

	
	public static List<List<String>> execQuerySelectMetadata(TableToRetrieve table) throws ArcException
	{
		return UtilitaireDao.get(table.getNod().getIndex()).executeRequest(null,new ArcPreparedStatementBuilder("select * from " + table.getTableName() + " where false "));
	}
			
	
}
