package fr.insee.arc.utils.sqlengine;

import fr.insee.arc.utils.sqlengine.model.IToken;

public class Tokens
{
    /**
     * Token pour le nom du schéma
     */
    public static final IToken TOK_SCHEMA = new StringToken("tok_schema");
    /**
     * Token pour le nom complet non qualifié de la table
     */
    public static final IToken TOK_TABLE_NAME = new StringToken("tok_table_name");
    /**
     * Token pour le suffixe indiquant que la table est temporaire comme par
     * exemple <code>$tmp$1569925756$0523</code>
     */
    public static final IToken TOK_SUFFIX_TEMP = new StringToken("tok_suffix_temp");
    /**
     * Token pour le nom de la phase
     */
    public static final IToken TOK_NOM_PHASE = new StringToken("tok_nom_phase");
    /**
     * Token pour n'importe quel préfixe
     */
    public static final IToken TOK_PREFIX = new StringToken("tok_prefix");
    /**
     * Token pour marquer un commentaire
     */
    public static final IToken TOK_COMMENT = new StringToken("tok_comment");
    /**
     * Token pour marquer la campagne
     */
    public static final IToken TOK_CAMPAGNE = new StringToken("tok_campagne");
    /**
     * Token pour marquer la échantillon
     */
    public static final IToken TOK_SAMPLE = new StringToken("tok_sample");
    
    /**
     * Token pour marquer le filtre
     */
    public static final IToken TOK_FILTRE = new StringToken("tok_filtre");
    
    /**
     * Token pour marquer l'environnement
     */
    public static final IToken TOK_ENV = new StringToken("tok_env");
    
    /**
     * Token pour marquer un numéro
     */
    public static final IToken TOK_NUMERO = new StringToken("tok_numero");
    
    /**
     * Token pour marquer un numéro
     */
    public static final IToken TOK_METIER = new StringToken("tok_metier");
    
}
