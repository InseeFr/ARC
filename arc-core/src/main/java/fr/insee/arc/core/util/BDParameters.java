package fr.insee.arc.core.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;

public class BDParameters {

    private static final Logger LOGGER = LogManager.getLogger(BDParameters.class);

	
	private static final String parameterTable="arc.parameter";
	private static final String getParameterQuery = "SELECT val FROM "+parameterTable+" ";

	private static PreparedStatementBuilder parameterQuery(String key) {
		PreparedStatementBuilder requete=new PreparedStatementBuilder();
		requete.append(getParameterQuery+ " WHERE key="+requete.quoteText(key));
		return requete;
	}

	public static String getString(Connection c, String key) {
		String r = null;
		try {
			r = UtilitaireDao.get("arc").getString(c, parameterQuery(key));
		} catch (Exception e) {
	        // Création de la table de parametre
			StringBuilder requete=new StringBuilder();
			
	        requete.append("\n CREATE SCHEMA IF NOT EXISTS arc; ");
			
	        requete.append("\n CREATE TABLE IF NOT EXISTS arc.parameter ");
	        requete.append("\n ( ");
	        requete.append("\n key text, ");
	        requete.append("\n val text, ");
	        requete.append("\n CONSTRAINT parameter_pkey PRIMARY KEY (key) ");
	        requete.append("\n ); ");
	        
	        
	        try {
				UtilitaireDao.get("arc").executeImmediate(c, requete);
			} catch (SQLException e1) {
				StaticLoggerDispatcher.error("Error on selecting key in parameter table", LOGGER);
			}
		}
		return r;
	}

	public static String getString(Connection c, String key, String defaultValue) {
		String s = getString(c, key);
		if (s==null)
		{
			insertDefaultValue(c, key, defaultValue);
		}
		return s == null ? defaultValue : s;
	}

	public static Integer getInt(Connection c, String key) {
		String val=getString(c, key);
		return val==null?null:Integer.parseInt(val);
	}

	public static Integer getInt(Connection c, String key, Integer defaultValue) {
		Integer s = getInt(c, key);
		if (s==null)
		{
			insertDefaultValue(c, key, ""+defaultValue);
		}
		return s == null ? defaultValue : s;
	}
	
	public static void insertDefaultValue(Connection c,String key, String defaultValue)
	{
		try {
			UtilitaireDao.get("arc").executeImmediate(c,"INSERT INTO "+parameterTable+" values ('"+key+"','"+defaultValue+"');");
		} catch (SQLException e) {
			StaticLoggerDispatcher.error("Error on inserting key in parameter table", LOGGER);
		}
	}
	
	public static void setString(Connection c,String key, String val)
	{
		try {
			PreparedStatementBuilder requete=new PreparedStatementBuilder();
			requete.append("UPDATE  "+parameterTable+" set val="+requete.quoteText(val)+" where key="+requete.quoteText(key)+" ");
			UtilitaireDao.get("arc").executeRequest(c,requete);
			
		} catch (SQLException e) {
			StaticLoggerDispatcher.error("Error on updating key in parameter table", LOGGER);
		}
	}

	/** Insert or update the value for that key.*/
	public static void setValue(Connection c, String key, String value) {
		PreparedStatementBuilder request=new PreparedStatementBuilder();

		request.append("INSERT INTO "+ parameterTable +" (key,val) VALUES (");
		request.append(request.quoteText(key));
		request.append(",");
		request.append(request.quoteText(value));
		request.append("')");
		request.append("  ON CONFLICT (key) DO UPDATE SET val =");
		request.append(request.quoteText(value));
		request.append("  WHERE EXCLUDED.key=");
		request.append(request.quoteText(key));
		request.append(";");
		
		try {
			UtilitaireDao.get("arc").executeRequest(c, request);
		} catch (SQLException e) {
			StaticLoggerDispatcher.error("Error on updating key in parameter table", LOGGER);
		}

	}
	
}
