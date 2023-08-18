package fr.insee.arc.core.util;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import fr.insee.arc.core.dataobjects.ArcDatabase;

public class Sandbox {

	private Sandbox() {
		throw new IllegalStateException("Utility class");
	}
	
	private static final String DEFAULT_PRODUCTION_ENVIRONMENTS="[\"arc_prod\"]";

	/** Return true if the environment is a production environment.*/
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
	
}
