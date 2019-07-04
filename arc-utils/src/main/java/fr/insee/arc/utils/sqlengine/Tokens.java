package fr.insee.arc.utils.sqlengine;

import fr.insee.arc.utils.sqlengine.model.IToken;

// TODO rename the constants and the string associated
// see the related issue : https://github.com/InseeFr/ARC/issues/7#issuecomment-508509524
public class Tokens {
    /**
     * Schema token
     */
    public static final IToken TOK_SCHEMA = new StringToken("tok_schema");
    /**
     * Unqualified complete table name token
     */
    public static final IToken TOK_TABLE_NAME = new StringToken("tok_table_name");
    /**
     * Token used to indicate a temporary table, for example: <code>$tmp$1569925756$0523</code>
     */
    public static final IToken TOK_SUFFIX_TEMP = new StringToken("tok_suffix_temp");
    /**
     * Phase name token
     */
    public static final IToken TOK_NOM_PHASE = new StringToken("tok_nom_phase");
    /**
     * Prefix token
     */
    public static final IToken TOK_PREFIX = new StringToken("tok_prefix");
    /**
     * Comment token
     */
    public static final IToken TOK_COMMENT = new StringToken("tok_comment");
    /**
     * Campaign token
     */
    public static final IToken TOK_CAMPAGNE = new StringToken("tok_campagne");
    /**
     * Sample token
     */
    public static final IToken TOK_SAMPLE = new StringToken("tok_sample");

    /**
     * Filter token
     */
    public static final IToken TOK_FILTRE = new StringToken("tok_filtre");

    /**
     * Environment token
     */
    public static final IToken TOK_ENV = new StringToken("tok_env");

    /**
     * Number token
     */
    public static final IToken TOK_NUMERO = new StringToken("tok_numero");

    /**
     * Business token
     */
    public static final IToken TOK_METIER = new StringToken("tok_metier");

}
