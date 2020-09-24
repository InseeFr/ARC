package fr.insee.arc.core.service.engine.mapping.regles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import fr.insee.arc.core.service.engine.mapping.VariableMapping;

/**
 *
 * Abstraction pour les règles de mapping qui à un enregistrement de la table de départ associent un enregistrement de la table d'arrivée.
 * Tous les symboles terminaux sont des {@link AbstractRegleMappingSimple}, et sont implémentés comme classes statiques de celle-ci.
 *
 */
public abstract class AbstractRegleMappingSimple extends AbstractRegleMapping {

    public AbstractRegleMappingSimple(String anExpression, VariableMapping aVariableMapping) {
        super(anExpression, aVariableMapping);
        this.ensembleIdentifiantsRubriques = new HashSet<>();
        this.ensembleNomsRubriques = new HashSet<>();
    }

    protected Set<String> ensembleIdentifiantsRubriques;
    protected Set<String> ensembleNomsRubriques;

    /**
     *
     * Représente un bout de code SQL comme par exemple {@code "CASE WHEN "} ou encore {@code ", 5 * "}.
     *
     */
    public static final class CodeSQL extends AbstractRegleMappingSimple {

        public static final String regexRegleCodeSQL = "[^\\{\\}]+";

        public CodeSQL(String anExpression, VariableMapping aVariableMapping) {
            super(anExpression, aVariableMapping);
        }

        @Override
        public void deriver() throws Exception {
            this.expressionSQL = this.getExpression();
        }

        @Override
        public String getExpressionSQL() {
            return this.expressionSQL;
        }

        @Override
        public String getExpressionSQL(Integer aNumeroGroupe) {
            return this.getExpressionSQL();
        }

        @Override
        public void deriverTest() throws Exception {
            this.deriver();
        }
    }

    /**
     *
     * Morceau de règle qui représente une rubrique échappée entre accolades.<br/>
     * Un tel morceau de règle se définit en compréhension/intension comme l'une des expression suivantes :<br/>
     * 1. <code>{v_xxxx}</code><br/>
     * 2. <code>{i_xxxx}</code><br/>
     * 3. <code>xxxx</code><br/>
     * Où {@code xxxx} est un nom de rubrique valide ou bien un nom de colonne présent dans la table de filtrage.
     */
    public static final class RubriqueMapping extends AbstractRegleMappingSimple {
        public static final String regexDebutIdentifiant = "i_";
        public static final String regexDebutNom = "v_";

        private static final String emptyBrackets = "{}";

        protected Set<String> ensembleNomsRubriquesReglesExistants;
        protected Set<String> ensembleIdentifiantsRubriquesReglesExistants;

        public RubriqueMapping(String anExpression, VariableMapping aVariableMapping, Set<String> anEnsembleIdentifiantsRubriquesRegles,
                Set<String> anEnsembleNomsRubriquesRegles) {
            super(anExpression, aVariableMapping);
            this.ensembleIdentifiantsRubriquesReglesExistants = anEnsembleIdentifiantsRubriquesRegles;
            this.ensembleNomsRubriquesReglesExistants = anEnsembleNomsRubriquesRegles;
        }

        public static final String regexRubriqueMapping = "\\{[a-zA-Z0-9_]*\\}";
        /**
         * Regex un peu plus permissive que nécessaire : la fabrique doit identifier {n'importe quoi} comme une rubrique, charge à la
         * méthode {@link #deriver()} et à la méthode {@link #deriverTest()} de lever les exceptions si besoin est.
         */
        public static final String regexRubriqueMappingAcceptante = "\\{[^\\{\\}]*\\}";
        public static final Pattern patternRubriqueMapping = Pattern.compile(regexRubriqueMappingAcceptante);
        private static final int ONE = 1;
        private static final String tokenIdSource = "id_source";

        /**
         *
         * @param nomRubrique
         * @return <code>{</code>{@code <nomRubrique>}<code>}</code>
         */
        public static final String rubriqueEchappee(String nomRubrique) {
            return new StringBuilder(openingBrace + nomRubrique + closingBrace).toString();
        }

        @Override
        public void deriverTest() throws Exception {
            check();
            String expressionSansAccolades = this.getExpression().substring(ONE, this.getExpression().length() - ONE).toLowerCase(Locale.FRENCH);
            this.expressionSQL = expressionSansAccolades;
            if (tokenIdSource.equalsIgnoreCase(expressionSansAccolades) || expressionSansAccolades.startsWith(regexDebutIdentifiant)) {
                this.ensembleIdentifiantsRubriques.add(expressionSansAccolades);
            } else {
                this.ensembleNomsRubriques.add(expressionSansAccolades);
            }
        }

        /**
         * Si la rubrique n'existe pas (n'est pas une colonne de la table de filtrage), on remplace son nom par {@code null}.<br/>
         * 1. {@code id_source} va dans les identifiants.<br/>
         * 2. Les {@code v_xxxx} vont dans les noms et leurs {@code i_xxxx} associés vont dans les identifiants.<br/>
         * 3. Les noms qui ne sont pas des {@code v_xxxx} vont dans les noms.<br/>
         * 4. Les {@code i_xxxx} vont dans les identifiants.<br/>
         * 5. Le reste est ce qui n'existe pas.
         */
        @Override
        public void deriver() throws Exception {
            check();
            String expressionSansAccolades = this.getExpression().substring(ONE, this.getExpression().length() - ONE);
            this.expressionSQL = expressionSansAccolades;
            /*
             * Traitement de l'id_source
             */
            if (tokenIdSource.equalsIgnoreCase(expressionSansAccolades)) {
                this.ensembleIdentifiantsRubriques.add(expressionSansAccolades);
            }
            /*
             * Traitement des nomsRubriques
             */
            else if (this.ensembleNomsRubriquesReglesExistants.contains(expressionSansAccolades)) {
                this.ensembleNomsRubriques.add(expressionSansAccolades);
                /*
                 * Protection pour ne rajouter dans la liste des identifiants i_ que les rubriques provenant de v_
                 */
                if (expressionSansAccolades.startsWith(regexDebutNom)) {
                    this.ensembleIdentifiantsRubriques.add(expressionSansAccolades.replaceFirst(regexDebutNom, regexDebutIdentifiant));
                }
            }
            /*
             * Traitement des identifiants
             */
            else if (this.ensembleIdentifiantsRubriquesReglesExistants.contains(expressionSansAccolades)) {
                this.ensembleIdentifiantsRubriques.add(expressionSansAccolades);
            }
            /*
             * Cas rebut
             */
            else {
                this.expressionSQL = exprNull;
            }
        }

        /**
         * Vérifie la bonne forme de l'expression de cette rubrique.
         */
        private void check() {
            if (emptyBrackets.equalsIgnoreCase(getExpression())) {
                throw new IllegalArgumentException("Pour la variable " + this.variableMapping.getNomVariable() + " ::= "
                        + this.variableMapping.getExpressionRegle().getExpression() + ".\nUne " + this.getClass().getName()
                        + " ne peut pas recevoir d'expression vide.");
            }
            if (!getExpression().matches(regexRubriqueMapping)) {
                throw new IllegalArgumentException("Pour la variable " + this.variableMapping.getNomVariable() + " ::= "
                        + this.variableMapping.getExpressionRegle().getExpression() + ".\nUne " + this.getClass().getName()
                        + " doit être de la forme " + regexRubriqueMapping + ".");
            }
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

    @Override
    public Set<String> getEnsembleIdentifiantsRubriques() {
        return this.ensembleIdentifiantsRubriques;
    }

    @Override
    public Set<String> getEnsembleIdentifiantsRubriques(Integer aNumeroGroupe) {
        return Collections.emptySet();
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
        return new HashSet<Integer>();
    }
}
