package fr.insee.arc.ws.services.restServices.setRules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.ws.services.restServices.setRules.pojo.SetRulesPojo;

@RestController
public class SetRulesController {
	
	@RequestMapping(value = "/setRules", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> changeRulesClient(
			@RequestBody(required = true) SetRulesPojo bodyPojo
	)
	{
		 JSONObject response = new JSONObject();

		try {
		
		if (bodyPojo.targetRule.equals("model"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_famille", "id_famille");
		}
		
		if (bodyPojo.targetRule.equals("model_tables"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_mod_table_metier", "id_famille", "nom_table_metier");
		}
		
		if (bodyPojo.targetRule.equals("model_variables"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_mod_variable_metier", "id_famille", "nom_table_metier", "nom_variable_metier");
		}
		
		if (bodyPojo.targetRule.equals("warehouse"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_entrepot", "id_entrepot");
		}
		
		if (bodyPojo.targetRule.equals("sandbox"))
		{
			replaceRulesDAO(bodyPojo,"arc.ext_etat_jeuderegle", "id");
		}
		
		if (bodyPojo.targetRule.equals("norm"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_norme","id_norme","periodicite");
		}
		
		if (bodyPojo.targetRule.equals("calendar"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_calendrier","id_norme","periodicite","validite_inf","validite_sup");
		}
		
		if (bodyPojo.targetRule.equals("ruleset"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_jeuderegle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("load"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_chargement_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("control"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_controle_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("structure"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_normage_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("filter"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_filtrage_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		
		if (bodyPojo.targetRule.equals("map"))
		{
			replaceRulesDAO(bodyPojo,"arc.ihm_mapping_regle", "id_norme", "periodicite", "validite_inf", "validite_sup", "version");
		}
		

		response.put("status", "OK");

		}
		catch (Exception e)
		{
			response.put("status", "KO");
		}

		return ResponseEntity.status(HttpStatus.OK).body(response.toString());

	}
	
	
	/**
	 * replace = delete + insert
	 * @param bodyPojo
	 * @throws SQLException 
	 */
	public void replaceRulesDAO(SetRulesPojo bodyPojo, String tablename, String...primaryKeys) throws SQLException
	{
		PreparedStatementBuilder requete=new PreparedStatementBuilder();
	
		requete.append(deleteRulesQuery(bodyPojo, tablename, primaryKeys));
		requete.append(insertRulesQuery(bodyPojo, tablename, primaryKeys));
		
		System.out.println(requete.getQuery());
		System.out.println(requete.getParameters());

		UtilitaireDao.get("arc").executeRequest(null, requete);
	}
	
	
	/**
	 * Query to insert rules
	 * @param bodyPojo
	 * @param tablename
	 * @param primaryKeys
	 * @return
	 */
	public PreparedStatementBuilder insertRulesQuery(SetRulesPojo bodyPojo, String tablename, String...primaryKeys)
	{
		PreparedStatementBuilder requete=new PreparedStatementBuilder();
		List<String> columns=new ArrayList<>(bodyPojo.content.keySet());
		
		// fetch data to insert
		for (int i=0;i<bodyPojo.content.get(columns.get(0)).getData().size();i++)
		{
			requete.append("\n INSERT INTO "+tablename+" (");
			requete.append(String.join(", ", columns));
			requete.append(")");
			requete.append("\n  VALUES (");
			
			boolean first=true;
			
			for (String col:columns)
			{
				if (first)
				{
					first=false;
				}
				else
				{
					requete.append(",");
				}
				
				if (bodyPojo.content.get(col).getData().isEmpty())
				{
					return new PreparedStatementBuilder();
				}
				
				requete.append(requete.quoteText(bodyPojo.content.get(col).getData().get(i)));
				requete.append("::");
				requete.append(bodyPojo.content.get(col).getDataType());
			}
			
			requete.append(") ON CONFLICT DO NOTHING ; ");
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
	public PreparedStatementBuilder deleteRulesQuery(SetRulesPojo bodyPojo, String tablename, String...primaryKeys)
	{
		PreparedStatementBuilder requete=new PreparedStatementBuilder();
		List<String> columns=new ArrayList<>(bodyPojo.content.keySet());
				
		for (int i=0;i<bodyPojo.content.get(columns.get(0)).getData().size();i++)
		{
			requete.append("\n DELETE FROM "+tablename+" WHERE true ");
			
			for (String pk:primaryKeys)
			{
				System.out.println(pk);
				requete.append("\n AND "+pk+" ="+requete.quoteText(bodyPojo.content.get(pk).getData().get(i))+"");
				requete.append("::");
				requete.append(bodyPojo.content.get(pk).getDataType());
			}
			requete.append("\n ;");
		}
		
		return requete;
	}
	
}
