package fr.insee.arc.core.service.engine.mapping.regles;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.service.engine.mapping.VariableMapping;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;

/**
 *
 * Abstraction pour les règles de mapping. Pour obtenir l'expression SQL d'une règle : {@link #deriver()}.
 *
 */
public abstract class AbstractRegleMapping implements IDbConstant, IConstanteCaractere, IConstanteNumerique {

    protected static final Logger LOGGER = LogManager.getLogger(AbstractRegleMapping.class);

    public static final String exprNull = "null";

    public static final String regexdebutEchappement = "^\\{";
    public static final String regexFinEchappement = "\\}$";

    public static final String regexDebutOuFinEchappement = "(" + regexdebutEchappement + ")|(" + regexFinEchappement + ")";

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
     * @throws Exception
     */
    public abstract void deriver() throws Exception;

    /**
     * Met à jour {@code expressionDerivee} dans le cas d'utilisation &laquo;&nbsp;mise à jour d'une règle dans l'écran de
     * normes&nbsp;&raquo;.
     *
     * @throws Exception
     */
    public abstract void deriverTest() throws Exception;

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
    public abstract String getExpressionSQL();

    /**
     *
     * @param aNumeroGroupe
     * @return the expressionSQL
     */
    public abstract String getExpressionSQL(Integer aNumeroGroupe);

    /**
     * @return the variableMapping
     */
    public VariableMapping getVariableMapping() {
        return this.variableMapping;
    }

}
