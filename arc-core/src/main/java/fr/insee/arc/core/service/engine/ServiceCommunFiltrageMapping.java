package fr.insee.arc.core.service.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class ServiceCommunFiltrageMapping {
	
	private ServiceCommunFiltrageMapping() {
		throw new IllegalStateException("Utility class");
	}
	
    private static final Logger LOGGER = LogManager.getLogger(ServiceCommunFiltrageMapping.class);
    
    
    private static final String DEBUT_REQUETE_RESULTAT_UNIQUE = "{:";
    private static final int LONGUEUR_DEBUT_REQUETE_RESULTAT_UNIQUE = DEBUT_REQUETE_RESULTAT_UNIQUE.length();
    private static final String FIN_REQUETE_RESULTAT_UNIQUE = "}";
    private static final int LONGUEUR_FIN_REQUETE_RESULTAT_UNIQUE = FIN_REQUETE_RESULTAT_UNIQUE.length();
    private static final String REGEXP_REQUETE_RESULTAT_UNIQUE = "\\{:.*\\}";

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
     * @throws ArcException
     */
    public static void parserRegleGlobale(Connection aConnexion, String aEnvExecution,
            HierarchicalView aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle, String aNomColonneRegle)
            throws ArcException {
        if (LOGGER.isInfoEnabled()) {
            StaticLoggerDispatcher.info(
                    "Début du parsing des règles portant sur des valeurs globales (count sur une table...)", LOGGER);
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
            if (LOGGER.isDebugEnabled()) {
                StaticLoggerDispatcher.debug("Le parsing valorise la règle "
                        + expressionRegleInterprete
                        + " à : "
                        + aNormeToPeriodiciteToValiditeInfToValiditeSupToVariableToRegle.getLevel(aNomColonneRegle)
                                .get(i).getLocalRoot(), LOGGER);
            }
        }
        if (LOGGER.isInfoEnabled()) {
            StaticLoggerDispatcher.info("Fin du parsing des règles portant sur des valeurs globales", LOGGER);
        }
    }

    /**
     * Parse une règle de type global (par exécution partielle si besoin)
     *
     * @param aConnexion
     * @param anExpressionRegle
     * @param aEnvExecution
     * @return la règle parsée et exécutée partiellement
     * @throws ArcException
     */
    private static String traiterRegleGlobale(Connection aConnexion, String anExpressionRegle, String aEnvExecution)
            throws ArcException {
        String returned = anExpressionRegle;
        Pattern pattern = Pattern.compile(REGEXP_REQUETE_RESULTAT_UNIQUE);
        // Matcher pour {:expression}
        Matcher trouverRequeteValeurGlobale = pattern.matcher(returned);
        // Tant que cette expression {:expression} est trouvée
        while (trouverRequeteValeurGlobale.find()) {
            String expressionRequeteValeurGlobaleAvecAccolades = trouverRequeteValeurGlobale.group(0);
            // Intérieur de l'expression
            String expressionRequeteValeurGlobale = expressionRequeteValeurGlobaleAvecAccolades.substring(
                    LONGUEUR_DEBUT_REQUETE_RESULTAT_UNIQUE, expressionRequeteValeurGlobaleAvecAccolades.length()
                            - LONGUEUR_FIN_REQUETE_RESULTAT_UNIQUE);
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
                                + nomGeneriqueTableMetierTrouve.substring(LONGUEUR_DEBUT_REQUETE_RESULTAT_UNIQUE,
                                        nomGeneriqueTableMetierTrouve.length() - LONGUEUR_FIN_REQUETE_RESULTAT_UNIQUE));
            }
            String valeurRequete = UtilitaireDao.get("arc").getString(aConnexion, new ArcPreparedStatementBuilder(requeteValeurGlobaleAvecVraiNomTableMetier));
            returned = returned.replace(expressionRequeteValeurGlobaleAvecAccolades,
                    valeurRequete == null ? "null" : valeurRequete);
        }
        return returned;
    }

    /**
     * return distinct column of a table in a set
     * @param aConnexion
     * @param table
     *
     * @return
     * @throws ArcException
     */
    public static Set<String> calculerListeColonnes(Connection aConnexion, String aTable) throws ArcException {
		return new HashSet<>(UtilitaireDao.get("arc").getColumns(aConnexion, new ArrayList<>(), aTable));
    }
    
    
}