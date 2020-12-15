package fr.insee.arc.web.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;

public class IndexDao {
	
	private static final Logger LOGGER = LogManager.getLogger(IndexDao.class);

	
	/**
	 * Get the sandbox list to be show in GUI
	 */
	public static Map<String, String> getSandboxList()
	{
		PreparedStatementBuilder requete= new PreparedStatementBuilder();
		requete.append("SELECT replace(id,'.','_') as id, upper(substring(id from '\\.(.*)')) as val FROM arc.ext_etat_jeuderegle where isenv order by nullif(substring(id from '[0123456789]+'),'')::int");
		
		HashMap<String, ArrayList<String>> m;
		try {
			m = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, requete)).mapContent();
			
			LinkedHashMap<String, String> envMap=new LinkedHashMap<>();
			
			for (int i=0;i<m.get("id").size();i++)
			{
				envMap.put(m.get("id").get(i), m.get("val").get(i));
			}
			

			return envMap;
			
		} catch (SQLException e) {
			StaticLoggerDispatcher.error("Sandbox list couldn't be initialized", LOGGER);
		}

		return null;
	}
	

}
