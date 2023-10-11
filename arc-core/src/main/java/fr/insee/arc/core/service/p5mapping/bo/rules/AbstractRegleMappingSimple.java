package fr.insee.arc.core.service.p5mapping.bo.rules;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.p5mapping.bo.VariableMapping;
import fr.insee.arc.utils.exception.ArcException;

/**
 *
 * Abstraction pour les règles de mapping qui à un enregistrement de la table de départ associent un enregistrement de la table d'arrivée.
 * Tous les symboles terminaux sont des {@link AbstractRegleMappingSimple}, et sont implémentés comme classes statiques de celle-ci.
 *
 */
public abstract class AbstractRegleMappingSimple extends AbstractRegleMapping {

    // separator to catch rubrique
    private static final String SEPARATOR_START_IDENTIFIER_RUBRIQUE="{";
    private static final String SEPARATOR_END_IDENTIFIER_RUBRIQUE="}";

    // separator to catch rubrique which user wants to ignore id
    private static final String SEPARATOR_START_NONIDENTIFIER_RUBRIQUE="#";
    private static final String SEPARATOR_END_NONIDENTIFIER_RUBRIQUE="#";
    
    protected AbstractRegleMappingSimple(String anExpression, VariableMapping aVariableMapping) {
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

        public static final String REGEXP_TO_FIND_IDENTIFIER_RUBRIQUE = "^((?!\\"+SEPARATOR_START_IDENTIFIER_RUBRIQUE+"[a-zA-Z0-9_]+\\"+SEPARATOR_END_IDENTIFIER_RUBRIQUE+").)*$";
        public static final String REGEXP_TO_FIND_NONIDENTIFIER_RUBRIQUE = "^((?!\\"+SEPARATOR_START_NONIDENTIFIER_RUBRIQUE+"[a-zA-Z0-9_]+\\"+SEPARATOR_END_NONIDENTIFIER_RUBRIQUE+").)*$";
        
        public CodeSQL(String anExpression, VariableMapping aVariableMapping) {
            super(anExpression, aVariableMapping);
        }

        @Override
        public void deriver() throws ArcException {
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
        public void deriverTest() throws ArcException {
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
     * Où {@code xxxx} est un nom de rubrique valide ou bien un nom de colonne présent dans la table de la phase précédente.
     */
    public static final class RubriqueMapping extends AbstractRegleMappingSimple {
        public static final String DATABASECOLUMN_START_FOR_RUBRIQUE = "i_";
        public static final String DATABASECOLUMN_START_FOR_VALUE = "v_";


        protected Set<String> ensembleNomsRubriquesReglesExistants;
        protected Set<String> ensembleIdentifiantsRubriquesReglesExistants;

        public RubriqueMapping(String anExpression, VariableMapping aVariableMapping, Set<String> anEnsembleIdentifiantsRubriquesRegles,
                Set<String> anEnsembleNomsRubriquesRegles) {
            super(anExpression, aVariableMapping);
            this.ensembleIdentifiantsRubriquesReglesExistants = anEnsembleIdentifiantsRubriquesRegles;
            this.ensembleNomsRubriquesReglesExistants = anEnsembleNomsRubriquesRegles;
        }



        private static final String REGEXP_EMPTY_IDENTIFIER_RUBRIQUE = SEPARATOR_START_IDENTIFIER_RUBRIQUE+SEPARATOR_END_IDENTIFIER_RUBRIQUE;


        private static final String REGEXP_EMPTY_NONIDENTIFIER_RUBRIQUE = SEPARATOR_START_NONIDENTIFIER_RUBRIQUE+SEPARATOR_END_NONIDENTIFIER_RUBRIQUE;
        
        private static final String PSEUDO_COLUMN_IGNORED_ID="1";
        

        public static final String regexRubriqueMapping = "((\\"+SEPARATOR_START_IDENTIFIER_RUBRIQUE+"[a-zA-Z0-9_]+\\"+SEPARATOR_END_IDENTIFIER_RUBRIQUE+")|(\\"+SEPARATOR_START_NONIDENTIFIER_RUBRIQUE+"[a-zA-Z0-9_]+\\"+SEPARATOR_END_NONIDENTIFIER_RUBRIQUE+"))";
        
        /**
         * Regex un peu plus permissive que nécessaire : la fabrique doit identifier {n'importe quoi} comme une rubrique, charge à la
         * méthode {@link #deriver()} et à la méthode {@link #deriverTest()} de lever les exceptions si besoin est.
         */
        public static final String regexRubriqueMappingAcceptante = "((\\"+SEPARATOR_START_IDENTIFIER_RUBRIQUE+"[a-zA-Z0-9_]+\\"+SEPARATOR_END_IDENTIFIER_RUBRIQUE+")|(\\"+SEPARATOR_START_NONIDENTIFIER_RUBRIQUE+"[a-zA-Z0-9_]+\\"+SEPARATOR_END_NONIDENTIFIER_RUBRIQUE+"))";


        @Override
        public void deriverTest() throws ArcException {
            check();
            String expressionSansAccolades = this.getExpression().substring(ONE, this.getExpression().length() - ONE).toLowerCase(Locale.FRENCH);
            this.expressionSQL = expressionSansAccolades;
            if (ColumnEnum.ID_SOURCE.getColumnName().equalsIgnoreCase(expressionSansAccolades) || expressionSansAccolades.startsWith(DATABASECOLUMN_START_FOR_RUBRIQUE)) {
                this.ensembleIdentifiantsRubriques.add(expressionSansAccolades);
            } else {
                this.ensembleNomsRubriques.add(expressionSansAccolades);
            }
        }

        /**
         * Si la rubrique n'existe pas (n'est pas une colonne de la table de la phase précédente), on remplace son nom par {@code null}.<br/>
         * 1. {@code id_source} va dans les identifiants.<br/>
         * 2. Les {@code v_xxxx} vont dans les noms et leurs {@code i_xxxx} associés vont dans les identifiants.<br/>
         * 3. Les noms qui ne sont pas des {@code v_xxxx} vont dans les noms.<br/>
         * 4. Les {@code i_xxxx} vont dans les identifiants.<br/>
         * 5. Le reste est ce qui n'existe pas.
         */
        @Override
        public void deriver() throws ArcException {
            check();

            // is it a rubrique for id to be ignored ?
            boolean ignoreId=this.getExpression().startsWith(SEPARATOR_START_NONIDENTIFIER_RUBRIQUE)
            		&& this.getExpression().endsWith(SEPARATOR_END_NONIDENTIFIER_RUBRIQUE)
            		;
            
            String expressionWithoutSeparator=ignoreId?this.getExpression().substring(SEPARATOR_START_NONIDENTIFIER_RUBRIQUE.length(), this.getExpression().length() - SEPARATOR_END_NONIDENTIFIER_RUBRIQUE.length())
            		:this.getExpression().substring(SEPARATOR_START_IDENTIFIER_RUBRIQUE.length(), this.getExpression().length() - SEPARATOR_END_IDENTIFIER_RUBRIQUE.length());
            
            this.expressionSQL = expressionWithoutSeparator;
            
            
            /*
             * Traitement de l'id_source
             */
            if (ColumnEnum.ID_SOURCE.getColumnName().equalsIgnoreCase(expressionWithoutSeparator)) {
                this.ensembleIdentifiantsRubriques.add(expressionWithoutSeparator);
            }
            /*
             * Traitement des nomsRubriques
             */
            else if (this.ensembleNomsRubriquesReglesExistants.contains(expressionWithoutSeparator)) {
                this.ensembleNomsRubriques.add(expressionWithoutSeparator);
                /*
                 * Protection pour ne rajouter dans la liste des identifiants i_ que les rubriques provenant de v_
                 */
                if (expressionWithoutSeparator.startsWith(DATABASECOLUMN_START_FOR_VALUE)) {
               		this.ensembleIdentifiantsRubriques.add(ignoreId?PSEUDO_COLUMN_IGNORED_ID:expressionWithoutSeparator.replaceFirst(DATABASECOLUMN_START_FOR_VALUE, DATABASECOLUMN_START_FOR_RUBRIQUE));

                }
            }
            /*
             * Traitement des identifiants
             */
            else if (this.ensembleIdentifiantsRubriquesReglesExistants.contains(expressionWithoutSeparator)) {
                this.ensembleIdentifiantsRubriques.add(ignoreId?PSEUDO_COLUMN_IGNORED_ID:expressionWithoutSeparator);
            }
            /*
             * Cas rebut
             */
            else {
                this.expressionSQL = MAPPING_NULL_EXPRESSION;
            }
        }

        /**
         * Vérifie la bonne forme de l'expression de cette rubrique.
         */
        private void check() {
            if (REGEXP_EMPTY_IDENTIFIER_RUBRIQUE.equalsIgnoreCase(getExpression()) || REGEXP_EMPTY_NONIDENTIFIER_RUBRIQUE.equalsIgnoreCase(getExpression())) {
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
