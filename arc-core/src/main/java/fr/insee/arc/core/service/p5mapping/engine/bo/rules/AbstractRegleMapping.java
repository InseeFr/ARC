package fr.insee.arc.core.service.p5mapping.engine.bo.rules;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.p5mapping.engine.bo.VariableMapping;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;

/**
 *
 * Abstraction pour les règles de mapping. Pour obtenir l'expression SQL d'une règle : {@link #deriver()}.
 *
 */
public abstract class AbstractRegleMapping implements IConstanteCaractere, IConstanteNumerique {

    protected static final Logger LOGGER = LogManager.getLogger(AbstractRegleMapping.class);

    /**
     * Règle null pour le mapping. La colonne métier calculée sera vide si elle se voit affectée cette règle
     * Utilisé si les rubriques spécifiées dans le calcul de la rubrique n'existent pas par exemple
     */
    public static final String MAPPING_NULL_EXPRESSION = "null";

    private static final String REGEX_DEBUT_ECHAPPEMENT = "^\\{";
    
    private static final String REGEX_FIN_ECHAPPEMENT = "\\}$";

    public static final String REGEX_DEBUT_OU_FIN_ECHAPPEMENT = "(" + REGEX_DEBUT_ECHAPPEMENT + ")|(" + REGEX_FIN_ECHAPPEMENT + ")";

    private String expression;

    protected String expressionSQL;

    protected VariableMapping variableMapping;

    public AbstractRegleMapping(String anExpression, VariableMapping aVariableMapping) {
        this.variableMapping = aVariableMapping;
        this.expression = anExpression;
    }

    /**
     * @return the expression (expression avant dérivation (i.e. traduction en exécutable SQL))
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * Met à jour {@code expressionDerivee}.
     *
     * @throws ArcException
     */
    public abstract void deriver() throws ArcException;

    /**
     * Met à jour {@code expressionDerivee} dans le cas d'utilisation &laquo;&nbsp;mise à jour d'une règle dans l'écran de
     * normes&nbsp;&raquo;.
     *
     * @throws ArcException
     */
    public abstract void deriverTest() throws ArcException;

    public abstract Set<Integer> getEnsembleGroupes();

    /**
     *
     * @return l'ensemble des identifiants de rubriques non groupes (ou l'ensemble vide si cette règle est dans un groupe).
     */
    public abstract Set<String> getEnsembleIdentifiantsRubriques();

    /**
     *
     * @param aNumeroGroupe
     * @return l'ensemble des identifiants de rubriques du groupe {@code aNumeroGroupe} (ou l'ensemble vide si cette règle n'est pas dans un
     *         groupe).
     */
    public abstract Set<String> getEnsembleIdentifiantsRubriques(Integer aNumeroGroupe);

    /**
     *
     * @return l'ensemble des noms de rubriques non groupes (ou l'ensemble vide si cette règle est dans un groupe).
     */
    public abstract Set<String> getEnsembleNomsRubriques();

    /**
     *
     * @param aNumeroGroupe
     * @return l'ensemble des noms de rubriques du groupe {@code aNumeroGroupe} (ou l'ensemble vide si cette règle n'est pas dans un
     *         groupe).
     */
    public abstract Set<String> getEnsembleNomsRubriques(Integer aNumeroGroupe);

    /**
     * @return the expressionSQL
     */
    public abstract String getExpressionSQL() throws ArcException;

    /**
     *
     * @param aNumeroGroupe
     * @return the expressionSQL
     */
    public abstract String getExpressionSQL(Integer aNumeroGroupe) throws ArcException;

    /**
     * @return the variableMapping
     */
    public VariableMapping getVariableMapping() {
        return this.variableMapping;
    }

}
