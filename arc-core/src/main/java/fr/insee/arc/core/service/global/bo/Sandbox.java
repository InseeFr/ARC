package fr.insee.arc.core.service.global.bo;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.database.ArcDatabase;

public class Sandbox {

	private static final String DEFAULT_PRODUCTION_ENVIRONMENTS="[\"arc_prod\"]";
	
	private Connection connection;
	
	private String schema;

	
	/**
	 * instanciate a sandbox reference for execution
	 * @param connection
	 * @param schema
	 */
	public Sandbox(Connection connection, String schema) {
		super();
		this.connection = connection;
		this.schema = schema;
	}

	public Connection getConnection() {
		return connection;
	}

	public String getSchema() {
		return schema;
	}
	

	public boolean isEnvSetForProduction() {
		return isEnvSetForProduction(this.schema);
	}
	
	/** Return true if the environment is defined as a production environment.*/
	public static boolean isEnvSetForProduction(String env) {
		JSONArray j=new JSONArray(new BDParameters(ArcDatabase.COORDINATOR).getString(null, "ArcAction.productionEnvironments",DEFAULT_PRODUCTION_ENVIRONMENTS));
		Set<String> found=new HashSet<>();
		
		j.forEach(item -> {
            if (item.toString().equals(env))
            {
            	found.add(item.toString());
            }
        });
		return !found.isEmpty();
	}
	
	public static String isEnvSetForBatch(String env) {
		JSONArray j=new JSONArray(new BDParameters(ArcDatabase.COORDINATOR).getString(null, "ArcAction.batchMode", "[]"));
		Set<String> found=new HashSet<>();
		
		j.forEach(item -> {
            if (item.toString().equals(env))
            {
            	found.add(item.toString());
            }
        });
		return (found.isEmpty() ? null : "1");
	}

}
