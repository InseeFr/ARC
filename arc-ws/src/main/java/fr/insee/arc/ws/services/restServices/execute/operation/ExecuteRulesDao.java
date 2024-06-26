package fr.insee.arc.ws.services.restServices.execute.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SecurityDao;
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

	// security : validate sandbox
	p.sandbox= SecurityDao.validateEnvironnement(p.sandbox);

	
}

public static void buildResponse(Connection c, ExecuteParameterModel p, ReturnView r, Date firstContactDate) throws ArcException
{
	r.setReceptionTime(firstContactDate);
	r.setReturnTime(new Date());
	r.setDataSetView(new ArrayList<>());
	
	// searchpath to the current sandbow to be able to query rules of the sandbox simply and without any risk of confusion with user rules
	String bas=Patch.normalizeSchemaName(p.sandbox);
	String postgresSearchPath ="public, "+bas+", arc"; 
	ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
	query.append("select set_config('search_path', "+query.quoteText(postgresSearchPath)+", false)");
	UtilitaireDao.get(0).executeRequest(c, query);
	
	
	
	if (p.queries!=null)
	{
		for (int i=0;i<p.queries.size();i++)
		{
		query = new ArcPreparedStatementBuilder();
		query.append("CALL public.safe_select("+query.quoteText(p.queries.get(i).expression)+"); SELECT * from safe_select");
		DataSetView ds=new DataSetView(
				Integer.parseInt(p.queries.get(i).query_id)
				,p.queries.get(i).query_name
				,new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapRecord()
				);
			r.getDataSetView().add(ds);
		}
	}
}

	
}
