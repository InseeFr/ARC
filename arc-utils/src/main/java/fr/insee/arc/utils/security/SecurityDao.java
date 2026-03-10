package fr.insee.arc.utils.security;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class SecurityDao {

	private SecurityDao() {
		throw new IllegalStateException("SecurityDao class");
	}

	public static String validateEnvironnement(String unsafe) throws ArcException
	{
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT "+query.quoteText(unsafe)+" as bas_name FROM arc.ext_etat_jeuderegle where isenv and lower(replace(id,'.','_')) = "+query.quoteText(unsafe.toLowerCase()));
		String result = UtilitaireDao.get(0).getString(null, query);
		
		return validateOrThrow(result, unsafe);
	}
	
	public static String validateOrThrow(String result, String unsafe) throws ArcException {
		if (result==null)
		{
			throw new ArcException(ArcExceptionMessage.WS_INVALID_PARAMETER, unsafe);
		}
		return result;
	}
	
}
