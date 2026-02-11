package fr.insee.arc.utils.dao;

import fr.insee.arc.utils.utils.FormatSQL;

public class ModeRequeteImpl {
	    
    public static final int TIME_OUT_SQL_EN_HEURE = 100;
	
    public static final String  COLLAPSE_JOIN_LIMIT = "10000";
    

	private ModeRequeteImpl() {
		throw new IllegalStateException("Utility class");
	}

	public static ModeRequete[] arcModeRequeteIHM() {
		return new ModeRequete[] { ModeRequete.SEQSCAN_OFF, ModeRequete.MATERIAL_OFF };
	}
	
	
    /**
     * Configuration de la base de données pour des petites requetes
     *
     * @param defaultSchema
     * @return requete
     */
    public static GenericPreparedStatementBuilder arcModeRequeteEngine(String defaultSchema)
    {
    	GenericPreparedStatementBuilder query=new GenericPreparedStatementBuilder();
    	query
    	.append("set statement_timeout="+ (3600000 * TIME_OUT_SQL_EN_HEURE) + ";")
    	.append(FormatSQL.setConfig("search_path", defaultSchema.toLowerCase() + ",public"))
    	.append("COMMIT;")
    	;
    	return query;
    }

}
