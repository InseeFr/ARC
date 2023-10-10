package fr.insee.arc.ws.services.restServices.execute;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteQueryModel;
import fr.insee.arc.ws.services.restServices.execute.view.DataSetView;
import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

public class ExecuteRulesDao {

/**
 * get the webService rules from the database
 * @param p
 * @param serviceName
 * @param serviceId
 * @return
 * @throws ArcException 
 */
public static void fillRules(Connection c, ExecuteParameterModel p, String serviceName, int serviceId) throws ArcException
{
	GenericBean gb;
	
	// récupération des règles de retour du webservice
	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
	requete.append("select a.service_name, a.call_id, a.service_type, replace(a.environment,'.','_') as environment, a.target_phase, a.norme, a.validite, a.periodicite, b.query_id, b.query_name, b.expression, b.query_view");
	requete.append("\n from arc.ihm_ws_context a left outer join arc.ihm_ws_query b ");
	requete.append("\n on a.service_name=b.service_name and a.call_id=b.call_id ");
	requete.append("\n where a.service_name=" + requete.quoteText(serviceName) + " ");
	requete.append("\n and a.call_id= " + serviceId + " ");
	requete.append("\n order by query_id ");
	requete.append("\n ;");

	// Récupération des parametres
	gb=new GenericBean(UtilitaireDao.get(0).executeRequest(c, requete));
	Map<String, List<String>> m=gb.mapContent();

	if (!m.isEmpty())
	{
		p.serviceType = p.serviceType == null ? m.get("service_type").get(0) : p.serviceType;
		p.sandbox = p.sandbox == null ? m.get("environment").get(0) : p.sandbox;
		p.targetPhase = p.targetPhase == null ? m.get("target_phase").get(0) : p.targetPhase;
		p.norme = p.norme == null ? m.get("norme").get(0) : p.norme;
		p.validite = p.validite == null ? m.get("validite").get(0) : p.validite;
		p.periodicite = p.periodicite == null ? m.get("periodicite").get(0) : p.periodicite;
	
		p.fileName = p.fileName == null ? "f.xml" : p.fileName;
	
		p.queries=new ArrayList<ExecuteQueryModel>();
		
		for (int i=0;i<m.get("service_name").size();i++)
		{
			ExecuteQueryModel e=new ExecuteQueryModel(m.get("query_id").get(i), m.get("query_name").get(i), m.get("expression").get(i), m.get("query_view").get(i));
			
			p.queries.add(e);
		}
	}
}

public static void buildResponse(Connection c, ExecuteParameterModel p, ReturnView r, Date firstContactDate) throws ArcException
{
	r.setReceptionTime(firstContactDate);
	r.setReturnTime(new Date());
	
	r.setDataSetView(new ArrayList<DataSetView>());
	
	// searchpath to the current sandbow to be able to query rules of the sandbox simply and without any risk of confusion with user rules
	UtilitaireDao.get(0).executeImmediate(c,"SET search_path=public, "+p.sandbox.replace(".", "_")+", arc; ");
	
	if (p.queries!=null)
	{
		for (int i=0;i<p.queries.size();i++)
		{
		DataSetView ds=new DataSetView(
				Integer.parseInt(p.queries.get(i).query_id)
				,p.queries.get(i).query_name
				,new GenericBean(UtilitaireDao.get(0).executeRequest(c, new ArcPreparedStatementBuilder(p.queries.get(i).expression))).mapRecord()
				);
			r.getDataSetView().add(ds);
		}
	}
}

	
}
