package fr.insee.arc.core.util;

import java.sql.Connection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;

public class BDParameters {

    private static final Logger LOGGER = LogManager.getLogger(BDParameters.class);
	
	private static final String PARAMETER_TABLE=new DataObjectService().getView(ViewEnum.PARAMETER);
	private static final String PARAMETER_SQL_QUERY = "SELECT val FROM "+PARAMETER_TABLE+" ";

	private static ArcPreparedStatementBuilder parameterQuery(String key) {
		ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
		requete.append(PARAMETER_SQL_QUERY+ " WHERE key="+requete.quoteText(key));
		return requete;
	}

	private ArcDatabase targetDatabase;
	
	
	public BDParameters(ArcDatabase targetDatabase) {
		super();
		this.targetDatabase = targetDatabase;
	}

	private String getString(Connection c, String key) {
		String r = null;
		try {
			r = UtilitaireDao.get(targetDatabase.getIndex()).getString(c, parameterQuery(key));
		} catch (ArcException e) {
			createParameterTableIfNotExists(c);
		}
		return r;
	}
	
	private String getStringNoError(Connection c, String key) {
		String r = null;
		createParameterTableIfNotExists(c);
		try {
			r = UtilitaireDao.get(targetDatabase.getIndex()).getString(c, parameterQuery(key));
		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "Error on selecting key in parameter table");
		}
		return r;
	}
	
	
	private void createParameterTableIfNotExists(Connection c)
	{
		 // Création de la table de parametre
		StringBuilder requete=new StringBuilder();
		
        requete.append("\n CREATE SCHEMA IF NOT EXISTS arc; ");
		
        requete.append("\n CREATE TABLE IF NOT EXISTS ");
        requete.append(PARAMETER_TABLE);
        requete.append("\n ( ");
        requete.append("\n key text, ");
        requete.append("\n val text, ");
        requete.append("\n description text, ");
        requete.append("\n CONSTRAINT parameter_pkey PRIMARY KEY (key) ");
        requete.append("\n ); ");
        
        
        try {
			UtilitaireDao.get(targetDatabase.getIndex()).executeImmediate(c, requete);
		} catch (ArcException e1) {
			StaticLoggerDispatcher.error(LOGGER, "Error on selecting key in parameter table");
		}
	}

	public String getString(Connection c, String key, String defaultValue) {
		String s = getString(c, key);
		if (s==null)
		{
			insertDefaultValue(c, key, defaultValue);
		}
		return s == null ? defaultValue : s;
	}

	public String getStringNoError(Connection c, String key, String defaultValue) {
		String s = getStringNoError(c, key);
		if (s==null)
		{
			insertDefaultValue(c, key, defaultValue);
		}
		return s == null ? defaultValue : s;
	}
	
	
	private Integer getInt(Connection c, String key) {
		String val=getString(c, key);
		return val==null?null:Integer.parseInt(val);
	}

	public Integer getInt(Connection c, String key, Integer defaultValue) {
		Integer s = getInt(c, key);
		if (s==null)
		{
			insertDefaultValue(c, key, ""+defaultValue);
		}
		return s == null ? defaultValue : s;
	}
	
	protected void insertDefaultValue(Connection c,String key, String defaultValue)
	{
		try {
			UtilitaireDao.get(targetDatabase.getIndex()).executeImmediate(c,"INSERT INTO "+PARAMETER_TABLE+" values ('"+key+"','"+defaultValue+"');");
		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "Error on inserting key in parameter table");
		}
	}
	
	
	/**
	 * Update the parameter value and description by key 
	 * @param c
	 * @param key
	 * @param val
	 * @param description
	 */
	public void setString(Connection c,String key, String val, String description)
	{
		try {
			
			ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
			
			requete.append("UPDATE  "+PARAMETER_TABLE+" ");
			requete.append("SET val="+requete.quoteText(val)+" ");
			if (!StringUtils.isBlank(description))
				{
				requete.append(", description="+requete.quoteText(description)+" ");
				}
			requete.append("WHERE key="+requete.quoteText(key)+" ");
			
			
			UtilitaireDao.get(targetDatabase.getIndex()).executeRequest(c,requete);
			
		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "Error on updating key in parameter table");
		}
	}
	
	
	/**
	 * Update the parameter value by key 
	 * @param c
	 * @param key
	 * @param val
	 */
	public void setString(Connection c,String key, String val)
	{
		setString (c,key, val, null);
	}

}
