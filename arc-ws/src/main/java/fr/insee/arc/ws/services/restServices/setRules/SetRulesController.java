package fr.insee.arc.ws.services.rest.changerules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.ws.services.rest.changerules.pojo.ChangeRulesPojo;

@RestController
public class ChangeRulesController {
	
	@Autowired
	private LoggerDispatcher loggerDispatcher;
	
    private static final Logger LOGGER = LogManager.getLogger(ChangeRulesController.class);
	
	@RequestMapping(value = "/changeRules/{sandbox}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> changeRulesClient(
			@RequestBody(required = true) ChangeRulesPojo bodyPojo
	) throws SQLException
	{
		
		if (bodyPojo.targetRule.equals("NORM"))
		{
			replaceRulesDAO(bodyPojo,"arc.norme","id_norme");
		}

		
		if (bodyPojo.targetRule.equals("CALENDAR"))
		{
			replaceRulesDAO(bodyPojo,"arc.calendrier","id_norme","periodicite","validite_inf","validite_sup");
		}
		
		if (bodyPojo.targetRule.equals("RULESET"))
		{
			replaceRulesDAO(bodyPojo,"arc.calendrier", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("LOAD"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_chargement_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version", "id_regle");
		}
		
		if (bodyPojo.targetRule.equals("CONTROL"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_controle_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("STRUCTURIZE"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_normage_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("FILTER"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_filtrage_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("MAPPING"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_mapping_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		return ResponseEntity.status(HttpStatus.OK).body("OK");

	}
	
	
	/**
	 * replace = delete + insert
	 * @param bodyPojo
	 * @throws SQLException 
	 */
	public void replaceRulesDAO(ChangeRulesPojo bodyPojo, String tablename, String...primaryKeys) throws SQLException
	{
		StringBuilder requete=new StringBuilder();
	
		requete.append(deleteRulesQuery(bodyPojo, tablename, primaryKeys));
		requete.append(insertRulesQuery(bodyPojo, tablename, primaryKeys));
		
		UtilitaireDao.get("arc").executeBlock(null, requete);
	}
	
	
	/**
	 * Query to insert rules
	 * @param bodyPojo
	 * @param tablename
	 * @param primaryKeys
	 * @return
	 */
	public StringBuilder insertRulesQuery(ChangeRulesPojo bodyPojo, String tablename, String...primaryKeys)
	{
		StringBuilder requete=new StringBuilder();
		List<String> columns=new ArrayList<>(bodyPojo.content.keySet());

		
		// fetch data to insert
		for (int i=0;i<bodyPojo.content.get(columns.get(0)).getData().size();i++)
		{
			requete.append("\n INSERT INTO "+tablename+" (");
			requete.append(String.join(", ", columns));
			requete.append(")");
			requete.append("\n  VALUES ");
			
			List<String> s=new ArrayList<>();
			
			for (String col:columns)
			{
				s.add(bodyPojo.content.get(col).getData().get(i));
			}
			
			requete.append("\n ('");
			requete.append(String.join("','", s));
			requete.append("'); ");
			
		}
		return requete;
	}
	
	/**
	 * Query to delete rules
	 * @param bodyPojo
	 * @param tablename
	 * @param primaryKeys
	 * @return
	 */
	public StringBuilder deleteRulesQuery(ChangeRulesPojo bodyPojo, String tablename, String...primaryKeys)
	{
		StringBuilder requete=new StringBuilder();
		List<String> columns=new ArrayList<>(bodyPojo.content.keySet());
		
		// delete
		HashSet<String> distinct=new HashSet<String>();

		for (int i=0;i<bodyPojo.content.get(columns.get(0)).getData().size();i++)
		{
		StringBuilder requete1=new StringBuilder();
		
		requete1.append("\n DELETE FROM "+tablename+" ");
		
		for (String pk:primaryKeys)
		{
			requete1.append("\n WHERE "+pk+" ='"+bodyPojo.content.get(pk).getData().get(i)+"';");
		}
		
		
		distinct.add(requete1.toString());
		}
		
		for (String req:distinct)
		{
			requete.append(req);
		}
		return requete;
	}
	
}
