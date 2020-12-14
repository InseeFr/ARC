package fr.insee.arc.ws.services.rest.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.ws.services.rest.execute.pojo.ExecuteParameterPojo;
import fr.insee.arc.ws.services.rest.execute.pojo.ExecuteQueryPojo;
import fr.insee.arc.ws.services.rest.execute.view.DataSetView;
import fr.insee.arc.ws.services.rest.execute.view.ReturnView;

public class ExecuteRulesDao {

/**
 * get the webService rules from the database
 * @param p
 * @param serviceName
 * @param serviceId
 * @return
 * @throws SQLException 
 */
public static void fillRules(Connection c, ExecuteParameterPojo p, String serviceName, int serviceId) throws SQLException
{
	GenericBean gb;
	
	// récupération des règles de retour du webservice
	PreparedStatementBuilder requete = new PreparedStatementBuilder();
	requete.append("select a.service_name, a.call_id, a.service_type, replace(a.environment,'.','_') as environment, a.target_phase, a.norme, a.validite, a.periodicite, b.query_id, b.query_name, b.expression, b.query_view");
	requete.append("\n from arc.ihm_ws_context a, arc.ihm_ws_query b ");
	requete.append("\n where a.service_name=b.service_name and a.call_id=b.call_id ");
	requete.append("\n and a.service_name=" + requete.quoteText(serviceName) + " ");
	requete.append("\n and a.call_id= " + serviceId + " ");
	requete.append("\n order by query_id ");
	requete.append("\n ;");

	// Récupération des parametres
	gb=new GenericBean(UtilitaireDao.get("arc").executeRequest(c, requete));
	HashMap<String, ArrayList<String>> m=gb.mapContent();
				
	p.serviceType = p.serviceType == null ? m.get("service_type").get(0) : p.serviceType;
	p.sandbox = p.sandbox == null ? m.get("environment").get(0) : p.sandbox;
	p.targetPhase = p.targetPhase == null ? m.get("target_phase").get(0) : p.targetPhase;
	p.targetPhase = p.targetPhase == null ? m.get("target_phase").get(0) : p.targetPhase;
	p.norme = p.norme == null ? m.get("norme").get(0) : p.norme;
	p.validite = p.validite == null ? m.get("validite").get(0) : p.validite;
	p.periodicite = p.periodicite == null ? m.get("periodicite").get(0) : p.periodicite;

	p.fileName = p.fileName == null ? "f.xml" : p.fileName;

	p.queries=new ArrayList<ExecuteQueryPojo>();
	
	for (int i=0;i<m.get("service_name").size();i++)
	{
		ExecuteQueryPojo e=new ExecuteQueryPojo(m.get("query_id").get(i), m.get("query_name").get(i), m.get("expression").get(i), m.get("query_view").get(i));
		
		p.queries.add(e);
	}
}

public static void buildResponse(Connection c, ExecuteParameterPojo p, ReturnView r, Date firstContactDate) throws SQLException
{
	r.setReceptionTime(firstContactDate);
	r.setReturnTime(new Date());
	
	r.setDataSetView(new ArrayList<DataSetView>());
	
	// searchpath to the current sandbow to be able to query rules of the sandbox simply and without any risk of confusion with user rules
	UtilitaireDao.get("arc").executeImmediate(c,"SET search_path=public, "+p.sandbox+", arc; ");
	
	for (int i=0;i<p.queries.size();i++)
	{
	
	DataSetView ds=new DataSetView(
			Integer.parseInt(p.queries.get(i).query_id)
			,p.queries.get(i).query_name
			,new GenericBean(UtilitaireDao.get("arc").executeRequest(c, new PreparedStatementBuilder(p.queries.get(i).expression))).mapRecord()
			);
		r.getDataSetView().add(ds);
	}
}

	
}
