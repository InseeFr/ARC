package fr.insee.arc.core.service.engine.mapping.regles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.arc.core.service.engine.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.engine.mapping.VariableMapping;
import fr.insee.arc.core.service.engine.mapping.regles.AbstractRegleMappingSimple.RubriqueMapping;
import fr.insee.arc.utils.exception.ArcException;

/**
 *
 * Toute règle du type {@code variable=f(rubriques, args)} avec :<br/>
 * 1. rubriques est un ensemble de rubriques {@code v_xxxx}<br/>
 * 2. args est un ensemble d'expressions de type {@link RegleMappingGlobale}.
 */
public class RegleMappingCorrespondanceFonctionnelle extends AbstractRegleMapping {


    private static final String regexMappingRegleGlobaleOuRubrique = "(" + RegleMappingGlobale.tokenRegexExpressionMappingGlobale + ")|("
            + RubriqueMapping.regexRubriqueMappingAcceptante + ")";
    private static final Pattern patternMappingRegleGlobaleOuRubrique = Pattern.compile(regexMappingRegleGlobaleOuRubrique);

    private RegleMappingFactory regleMappingFactory;

    private List<AbstractRegleMapping> listeTokenRegle;

    private Set<String> ensembleIdentifiantsRubriques;
    private Set<String> ensembleNomsRubriques;

    private boolean isCalcule;

    public RegleMappingCorrespondanceFonctionnelle(RegleMappingFactory aRegleMappingFactory, String anExpression, VariableMapping aVariableMapping) {
        super(anExpression, aVariableMapping);
        this.regleMappingFactory = aRegleMappingFactory;
        this.ensembleIdentifiantsRubriques = new HashSet<>();
        this.ensembleNomsRubriques = new HashSet<>();
        this.listeTokenRegle = new ArrayList<>();
        this.isCalcule = false;
    }

    /**
     * La dérivation se fait en étapes :<br/>
     * 1. Décomposition de l'expression en règles plus simples mais non finales ({@link #decomposerElementsSimple()}).<br/>
     * 2. Dérivation de ces règles plus simples ({@link AbstractRegleMapping#deriver()}). Cette étape fait éventuellement appel à la
     * récursivité.<br/>
     * Exemple : <code>3*{un_nombre}+2*{un_autre_nombre}+5</code><br/>
     * L'étape 1 fournit trois tokens : <code>3*</code> qui est de code SQL, <code>{un_nombre}</code> qui est une rubrique et
     * <code>+2*{un_autre_nombre}+5</code> qui est une règle de correspondance fonctionnelle.<br/>
     * L'étape 2 de dérivation dérive les deux premiers tokens tranquillou. Le dernier repasse dans l'étape 1 lors de l'appel de (
     * {@link #decomposerElementsSimple()}) en début de dérivation.
     */
    @Override
    public final void deriver() throws ArcException {
        this.decomposerElementsSimple();
        for (int i = 0; i < this.listeTokenRegle.size(); i++) {
            this.listeTokenRegle.get(i).deriver();
            this.ensembleIdentifiantsRubriques.addAll(this.listeTokenRegle.get(i).getEnsembleIdentifiantsRubriques());
            this.ensembleNomsRubriques.addAll(this.listeTokenRegle.get(i).getEnsembleNomsRubriques());
        }
    }

    @Override
    public void deriverTest() throws ArcException {
        this.decomposerElementsSimple();
        for (int i = 0; i < this.listeTokenRegle.size(); i++) {
            this.listeTokenRegle.get(i).deriverTest();
            this.ensembleIdentifiantsRubriques.addAll(this.listeTokenRegle.get(i).getEnsembleIdentifiantsRubriques());
            this.ensembleNomsRubriques.addAll(this.listeTokenRegle.get(i).getEnsembleNomsRubriques());
        }
    }

    /**
     * 1. Recherche des règles globales.<br/>
     * 2. Recherche des rubriques.<br/>
     */
    private void decomposerElementsSimple() throws ArcException {
        List<String> triplet = decouperEnTrois(this.getExpression());
        /*
         * Recherche des boucles infinies dûes au fait qu'une accolade n'est pas fermée.
         */
        for (int i = 0; i < triplet.size(); i++) {
            if (triplet.get(i).equals(this.getExpression())) {

                throw new ArcException("L'expression \"" + this.getExpression() + "\" n'est pas valide.");
            }
        }
        if (!triplet.get(ARRAY_FIRST_COLUMN_INDEX).equalsIgnoreCase(empty)) {
            this.listeTokenRegle.add(this.regleMappingFactory.get(triplet.get(ARRAY_FIRST_COLUMN_INDEX), this.variableMapping));
        }
        if (!triplet.get(ARRAY_SECOND_COLUMN_INDEX).equalsIgnoreCase(empty)) {
            this.listeTokenRegle.add(this.regleMappingFactory.get(triplet.get(ARRAY_SECOND_COLUMN_INDEX), this.variableMapping));
        }
        if (!triplet.get(ARRAY_THIRD_COLUMN_INDEX).equalsIgnoreCase(empty)) {
            this.listeTokenRegle.add(this.regleMappingFactory.get(triplet.get(ARRAY_THIRD_COLUMN_INDEX), this.variableMapping));
        }
    }

    private List<String> decouperEnTrois(String anExpression) {
        String boutGauche = empty;
        String boutDroite = empty;
        String boutMilieu = empty;
        Matcher matcher = patternMappingRegleGlobaleOuRubrique.matcher(this.getExpression());
        int start = 0;
        int end = 0;
        if (matcher.find()) {
            start = matcher.start();
            int newEnd = matcher.end();
            if (start > end) {
                boutGauche = this.getExpression().substring(end, start);
            }
            boutMilieu = this.getExpression().substring(start, newEnd);
            end = newEnd;
        }
        if (end < this.getExpression().length()) {
            boutDroite = this.getExpression().substring(end);
        }
        return Arrays.asList(boutGauche, boutMilieu, boutDroite);
    }

    @Override
    public Set<String> getEnsembleIdentifiantsRubriques() {
        return this.ensembleIdentifiantsRubriques;
    }

    @Override
    public Set<String> getEnsembleIdentifiantsRubriques(Integer aNumeroGroupe) {
        return Collections.emptySet();
    }

    @Override
    public String getExpressionSQL() {
        if (!this.isCalcule) {
            StringBuilder expression = new StringBuilder();
            for (int i = 0; i < this.listeTokenRegle.size(); i++) {
                if (i == 0) {
                    expression.append(space);
                }
                expression.append(this.listeTokenRegle.get(i).getExpressionSQL());
            }
            this.expressionSQL = expression.toString();
            this.isCalcule = true;
        }
        return this.expressionSQL;
    }

    @Override
    public String getExpressionSQL(Integer aNumeroGroupe) {
        return this.getExpressionSQL();
    }

    @Override
    public Set<String> getEnsembleNomsRubriques() {
        return this.ensembleNomsRubriques;
    }

    @Override
    public Set<String> getEnsembleNomsRubriques(Integer aNumeroGroupe) {
        return Collections.emptySet();
    }

    @Override
    public Set<Integer> getEnsembleGroupes() {
        return new HashSet<>();
    }

}
