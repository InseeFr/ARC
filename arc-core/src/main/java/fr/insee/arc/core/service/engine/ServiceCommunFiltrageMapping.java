package fr.insee.arc.core.service.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

public class ServiceCommunFiltrageMapping {
    private static final Logger logger = LogManager.getLogger(ServiceCommunFiltrageMapping.class);
    public static final String debutRequeteResultatUnique = "{:";
    public static final int longueurDebutRequeteResultatUnique = debutRequeteResultatUnique.length();
    public static final String finRequeteResultatUnique = "}";
    public static final int longueurFinRequeteResultatUnique = finRequeteResultatUnique.length();
    public static final String regexpRequeteResultatUnique = "\\{:.*\\}";
    public static final String regexpRegleResultatUnique = ".*" + regexpRequeteResultatUnique + ".*";

    /**
     * Parcourt l'ensemble des règles à la recherche de règles globales de la forme
     * <code>{:expression en fonction de rubriques et de {:tables externes}}</code> <br/>
     * Pour chacune de ces règles, l'expression est remplacée par sa valeur effective dans la règle.<br/>
     * C'est fait en deux étapes :<br/>
     * 1. Remplacement de chaque nom de <code>{:table externe}</code> par son vrai nom dépendant du contexte d'exécution<br/>
     * 2. Remplacement de l'expression qui entoure ces <code>{:table externe}</code> par sa valeur dans le contexte
     *
     *
     * @param aConnexion
     * @param aEnvExecution
     * @param aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle
     * @param aNomColonneRegle
     * @throws SQLException
     */
    public static void parserRegleGlobale(Connection aConnexion, String aEnvExecution,
            HierarchicalView aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle, String aNomColonneRegle)
            throws SQLException {
        if (logger.isInfoEnabled()) {
            StaticLoggerDispatcher.info(
                    "Début du parsing des règles portant sur des valeurs globales (count sur une table...)", logger);
        }
        // Pour chaque règle
        for (int i = 0; i < aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle.getLevel(aNomColonneRegle)
                .size(); i++) {
            // Récupération de l'expression
            String expressionRegleInterprete = aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle
                    .getLevel(aNomColonneRegle).get(i).getLocalRoot();
            if (expressionRegleInterprete==null){
            	expressionRegleInterprete=IConstanteCaractere.empty;
            }
            aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle
                    .getLevel(aNomColonneRegle)
                    .get(i)
                    .setLocalRoot(
                            ServiceCommunFiltrageMapping.traiterRegleGlobale(aConnexion, expressionRegleInterprete,
                                    aEnvExecution));
            if (logger.isDebugEnabled()) {
                StaticLoggerDispatcher.debug("Le parsing valorise la règle "
                        + expressionRegleInterprete
                        + " à : "
                        + aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle.getLevel(aNomColonneRegle)
                                .get(i).getLocalRoot(), logger);
            }
        }
        if (logger.isInfoEnabled()) {
            StaticLoggerDispatcher.info("Fin du parsing des règles portant sur des valeurs globales", logger);
        }
    }

    /**
     * Parse une règle de type global (par exécution partielle si besoin)
     *
     * @param aConnexion
     * @param anExpressionRegle
     * @param aEnvExecution
     * @return la règle parsée et exécutée partiellement
     * @throws SQLException
     */
    public static String traiterRegleGlobale(Connection aConnexion, String anExpressionRegle, String aEnvExecution)
            throws SQLException {
        String returned = anExpressionRegle;
        Pattern pattern = Pattern.compile(regexpRequeteResultatUnique);
        // Matcher pour {:expression}
        Matcher trouverRequeteValeurGlobale = pattern.matcher(returned);
        // Tant que cette expression {:expression} est trouvée
        while (trouverRequeteValeurGlobale.find()) {
            String expressionRequeteValeurGlobaleAvecAccolades = trouverRequeteValeurGlobale.group(0);
            // Intérieur de l'expression
            String expressionRequeteValeurGlobale = expressionRequeteValeurGlobaleAvecAccolades.substring(
                    longueurDebutRequeteResultatUnique, expressionRequeteValeurGlobaleAvecAccolades.length()
                            - longueurFinRequeteResultatUnique);
            // Matcher pour {:nom générique de table}
            Matcher trouverReferenceTableMetier = pattern.matcher(expressionRequeteValeurGlobale);
            String requeteValeurGlobaleAvecVraiNomTableMetier = expressionRequeteValeurGlobale;
            // Tant qu'une {:nom générique de table} est trouvée
            while (trouverReferenceTableMetier.find()) {
                String nomGeneriqueTableMetierTrouve = trouverReferenceTableMetier.group(0);
                /**
                 * Remplacement de {:nom générique de table} par nom de table dans le contexte
                 */
                requeteValeurGlobaleAvecVraiNomTableMetier = requeteValeurGlobaleAvecVraiNomTableMetier.replace(
                        nomGeneriqueTableMetierTrouve,
                        aEnvExecution
                                + "_"
                                + nomGeneriqueTableMetierTrouve.substring(longueurDebutRequeteResultatUnique,
                                        nomGeneriqueTableMetierTrouve.length() - longueurFinRequeteResultatUnique));
            }
            String valeurRequete = UtilitaireDao.get("arc").getString(aConnexion, requeteValeurGlobaleAvecVraiNomTableMetier);
            returned = returned.replace(expressionRequeteValeurGlobaleAvecAccolades,
                    valeurRequete == null ? "null" : valeurRequete);
        }
        return returned;
    }

    /**
     *
     * @param aConnexion
     * @param table
     *
     * @return
     * @throws SQLException
     */
    public static Set<String> calculerListeColonnes(Connection aConnexion, String aTable) throws SQLException {
        Set<String> returned = new HashSet<String>();
        String token = (aTable.contains(".") ? ("schema_columns.table_schema||'.'||") : "");
        ArrayList<ArrayList<String>> result = UtilitaireDao.get("arc").executeRequest(
                aConnexion,
                new StringBuilder("SELECT DISTINCT column_name").append(
                        "  FROM information_schema.columns schema_columns").append(
                        "  WHERE '" + aTable.toLowerCase() + "'=" + token + "schema_columns.table_name"));
        for (int i = 2; i < result.size(); i++) {
//            if (logger.isTraceEnabled()) {
//                StaticLoggerDispatcher.trace("Rubrique trouvée : " + result.get(i).get(0), logger);
//            }
            returned.add(result.get(i).get(0));
        }
        return returned;
    }
}