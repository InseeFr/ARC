package fr.insee.arc.core.service.p5mapping.bo.rules;

import java.sql.Connection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.p5mapping.bo.TableMapping;
import fr.insee.arc.core.service.p5mapping.bo.VariableMapping;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

/**
 *
 * Toute expression dont l'évaluation est immutable (sur une exécution donnée du mapping), comme par exemple :<br/>
 * <code>SELECT max(une_variable) FROM une_table</code>
 *
 */
public class RegleMappingGlobale extends AbstractRegleMappingSimple {

    /**
     * Comment reconnaître une règle globale ?
     */
    public static final String REGEX_REGLE_GLOBALE = "^\\{:.*\\}$";

    private static final String TOKEN_DEBUT_TABLE = "{:";
    private static final String TOKEN_FIN_TABLE = "}";
    private static final String TOKEN_REGEXDEBUT = "^\\{:";
    private static final String TOKEN_REGEX_FIN = "\\}$";
    private static final String TOKEN_REGEX_DEBUT_OU_FIN = "(" + TOKEN_REGEXDEBUT + ")|(" + TOKEN_REGEX_FIN + ")";
    private static final String TOKEN_REGEX_EXPRESSION_ECHAPPEE = "[^\\{:\\}]+";
    private static final String TOKEN_REGEX_EXPRESSION_TABLE = "\\{:" + TOKEN_REGEX_EXPRESSION_ECHAPPEE + "\\}";
    public static final String TOKEN_REGEX_EXPRESSION_MAPPING_GLOBALE = "\\{:" + TOKEN_REGEX_EXPRESSION_ECHAPPEE + "(" + TOKEN_REGEX_EXPRESSION_TABLE + "|"
            + TOKEN_REGEX_EXPRESSION_ECHAPPEE + ")*\\}";

    private Connection connexion;

    private String environnement;

    private Set<TableMapping> ensembleTableMapping;

    public RegleMappingGlobale(Connection aConnexion, String anExpression, String anEnvironnement, Set<TableMapping> someTablesMapping,
                               VariableMapping aVariableMapping) {
        super(anExpression, aVariableMapping);
        this.environnement = anEnvironnement;
        this.connexion = aConnexion;
        this.ensembleTableMapping = someTablesMapping;
    }

    /**
     * <code>{:requete qui renvoie un résultat unique avec des {:nom table} dedans}</code>
     */
    @Override
    public void deriver() throws ArcException {
        String requete = this.obtenirRequeteExecutable();
        /*
         * La requête doit contenir uniquement du SQL, sans "{" ni "}".
         */
        if (!requete.matches(CodeSQL.REGEXP_TO_FIND_IDENTIFIER_RUBRIQUE) || !requete.matches(CodeSQL.REGEXP_TO_FIND_NONIDENTIFIER_RUBRIQUE)) {
            throw new ArcException(ArcExceptionMessage.MAPPING_EXPRESSION_REFERS_NON_EXISTING_TABLES, this.getExpression());
        }
        this.expressionSQL = UtilitaireDao.get(0).getString(this.connexion, new ArcPreparedStatementBuilder(requete));
    }

    @Override
    public void deriverTest() throws ArcException {
        String intermediaire = this.getExpression().replaceAll(TOKEN_REGEX_DEBUT_OU_FIN, empty);
        Pattern pattern = Pattern.compile("\\{:[^\\{:\\}]+\\}");
        Matcher matcher = pattern.matcher(intermediaire);
        StringBuilder returned = new StringBuilder();
        int end = 0;
        while (matcher.find()) {
            int start = matcher.start();
            if (start > end) {
                returned.append(intermediaire.substring(end, start));
            }
            returned.append(this.environnement + "." + intermediaire.substring(start + TWO, matcher.end() - ONE));
            end = matcher.end();
        }
        returned.append(intermediaire.substring(end));
        this.expressionSQL = UtilitaireDao.get(0).getString(this.connexion, new ArcPreparedStatementBuilder(returned));
    }

    private final static String tokenTable(String nomCourt) {
        return TOKEN_DEBUT_TABLE + nomCourt + TOKEN_FIN_TABLE;
    }

    private String obtenirRequeteExecutable() {
        String returned = this.getExpression().replaceAll(TOKEN_REGEX_DEBUT_OU_FIN, empty);
        for (TableMapping table : this.ensembleTableMapping) {
            returned = returned.replace(tokenTable(table.getNomTableCourt()), ViewEnum.getFullName(environnement, table.toString()));
        }
        return returned;
    }

    @Override
    public String getExpressionSQL() {
        return this.expressionSQL;
    }

    @Override
    public String getExpressionSQL(Integer aNumeroGroupe) {
        return this.getExpressionSQL();
    }

}