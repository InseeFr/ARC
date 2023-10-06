package fr.insee.arc.web.gui.all.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

@Component
public class ArcWebGenericDao {
	
	private static final Logger LOGGER = LogManager.getLogger(ArcWebGenericDao.class);
	
	
	public void execQueryTestDatabaseConnection() throws ArcException
	{
		UtilitaireDao.get(0).executeRequest(null, new ArcPreparedStatementBuilder("select true"));
	}
	
	/**
	 * Get the sandbox list to be show in GUI.
	 */
	public Map<String, String> getSandboxList()
	{
		ArcPreparedStatementBuilder requete= new ArcPreparedStatementBuilder();
		requete.append("SELECT replace(id,'.','_') as id, upper(substring(id from '\\.(.*)')) as val FROM arc.ext_etat_jeuderegle where isenv order by nullif(substring(id from '[0123456789]+'),'')::int");
		
		Map<String, List<String>> m;
		try {
			m = new GenericBean(UtilitaireDao.get(0).executeRequest(null, requete)).mapContent();
			
			Map<String, String> envMap=new LinkedHashMap<>();
			
			for (int i=0;i<m.get("id").size();i++)
			{
				envMap.put(m.get("id").get(i), m.get("val").get(i));
			}
			

			return envMap;
			
		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "Sandbox list couldn't be initialized");
		}

		return new LinkedHashMap<>();
	}


}
