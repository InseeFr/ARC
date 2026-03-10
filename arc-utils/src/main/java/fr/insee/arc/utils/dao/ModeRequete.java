package fr.insee.arc.utils.dao;

public enum ModeRequete {
    NESTLOOP_ON("set enable_nestloop=on;"), //
    NESTLOOP_OFF("set enable_nestloop=off;"), //
    HASH_JOIN_ON("set enable_hashjoin=on;"), //
    HASH_JOIN_OFF("set enable_hashjoin=off;"), //
    MATERIAL_ON("set enable_material=on;"), //
    MATERIAL_OFF("set enable_material=off;"), //    
    MERGE_JOIN_ON("set enable_mergejoin=on;"), //
    MERGE_JOIN_OFF("set enable_mergejoin=off;"), //
    SEQSCAN_ON("set enable_seqscan=on;"), //
    SEQSCAN_OFF("set enable_seqscan=off;"), //
    HASHAGG_ON("set enable_hashagg=on;"), //
    HASHAGG_OFF("set enable_hashagg=off;"), //
    EXTRA_FLOAT_DIGIT("set extra_float_digits=0;")
    ;
   
	private String expression;

    private ModeRequete(String anExpression) {
        this.expression = anExpression;
    }

    @Override
    public String toString() {
        return this.expression;
    }

    public String expr() {
        return this.expression;
    }
    

    /**
     * untokenize the configuration modes to be used in the query
     * @param modes
     * @return
     */
	public static GenericPreparedStatementBuilder untokenize(ModeRequete... modes) {
		GenericPreparedStatementBuilder returned = new GenericPreparedStatementBuilder();
		for (int i = 0; i < modes.length; i++) {
			returned.append(modes[i].expr());
		}
		return returned.append("\n");
	}


    
    /**
     * Configure query with modes
     * Extra float digit modes in activated by default
     * @param requete
     * @param modes
     * @return
     */
    public static GenericPreparedStatementBuilder configureQuery(GenericPreparedStatementBuilder requete, ModeRequete... modes)
    {
    	GenericPreparedStatementBuilder query=new GenericPreparedStatementBuilder();
		// user defined configuration modes
		query.append(ModeRequete.untokenize(modes));

		// query
		if (requete!=null)
		{
			query.append(requete);
		}
		
		return query;
		
    }
    
}
