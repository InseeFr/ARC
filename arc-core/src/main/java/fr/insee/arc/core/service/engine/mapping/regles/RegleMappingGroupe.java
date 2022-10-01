package fr.insee.arc.core.service.engine.mapping.regles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.arc.core.service.engine.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.engine.mapping.VariableMapping;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.Pair;


/**
 *
 * Cette classe représente les règles à groupes, dont le but est de valoriser une même variable avec {@code N} fonctions. Une ligne en
 * entrée du mapping sera ainsi transformée en {@code N} lignes en sortie.<br/>
 *
 * La syntaxe (donnée ici en compréhension/intension) des règles à groupes est la suivante :<br/>
 * <code>{{une liste d'entiers séparés par des virgules}une règle qui n'est pas à groupes}...{{une autre liste}une autre règle}</code>
 *
 */
public class RegleMappingGroupe extends AbstractRegleMapping {
    /**
     * Comment reconnaître une règle de groupe ?
     */
    public final static String regexRegleGroupe = "^\\{\\{.*\\}";

    private static final String regexExpressionNombre = " *[1-9][0-9]* *";

    private static final String regexSeparateurDeListe = " *, *";

    private static final String regexExpressionListeNombre = regexExpressionNombre + "(," + regexExpressionNombre + ")*";
    private static final String regexExpressionListeNombreAccolade = "\\{" + regexExpressionListeNombre + "\\}";
    private static final Pattern patternExpressionListeNombreAccolade = Pattern.compile(regexExpressionListeNombreAccolade);

    private static final String regexExpressionDebutRegleGroupe = "\\{" + regexExpressionListeNombreAccolade;
    private static final Pattern patternDebutRegleGroupe = Pattern.compile(regexExpressionDebutRegleGroupe);

    /**
     * Cette table associe chaque numéro de groupe à sa règle
     */
    private Map<Integer, AbstractRegleMapping> mapRegleGroupe;

    private RegleMappingFactory regleMappingFactory;

    public RegleMappingGroupe(RegleMappingFactory aRegleMappingFactory, String anExpression, VariableMapping aVariableMapping) {
        super(anExpression, aVariableMapping);
        this.mapRegleGroupe = new HashMap<>();
        this.regleMappingFactory = aRegleMappingFactory;
    }

    /**
     * Pas de gestion des identifiants et noms de rubriques ici. Tout est fait chez les enfants.
     */
    @Override
    public final void deriver() throws ArcException {
        this.decomposerElementsSimple();
        for (Entry<Integer, AbstractRegleMapping> entry : this.mapRegleGroupe.entrySet()) {
            entry.getValue().deriver();
        }
    }

    @Override
    public void deriverTest() throws ArcException {
        this.decomposerElementsSimple();
        for (Entry<Integer, AbstractRegleMapping> entry : this.mapRegleGroupe.entrySet()) {
            entry.getValue().deriverTest();
        }
    }

    /**
     *
     * @param anExpressionGroupe
     *            du type <code>{{1, 2}une règle quelconque}</code>
     * @return
     */
    private Pair<List<Integer>, AbstractRegleMapping> construireRegleGroupe(String anExpressionGroupe) throws ArcException {
        List<Integer> listeGroupe = new ArrayList<>();
        /*
         * Éliminer les accolades de début ou de fin
         */
        String expressionGroupe = anExpressionGroupe.replaceAll(REGEX_DEBUT_OU_FIN_ECHAPPEMENT, empty);
        Matcher matcher = patternExpressionListeNombreAccolade.matcher(expressionGroupe);
        /*
         * Repérer {1, 2}
         */
        if (matcher.find()) {
            /*
             * Récupérer {1, 2} (heu en fait juste 1, 2)
             */
            String expressionListeNumeroGroupe = expressionGroupe.substring(matcher.start() + ONE, matcher.end() - ONE);
            /*
             * Récupérer le reste
             */
            String expressionGroupeReturned = expressionGroupe.substring(matcher.end());
            /*
             * Je récupère les entiers 1 et 2
             */
            for (String numeroGroupe : expressionListeNumeroGroupe.split(regexSeparateurDeListe)) {
                listeGroupe.add(Integer.parseInt(numeroGroupe));
            }
            /*
             * Je renvoie la liste des groupes pour cette règle et cette règle
             */
            return new Pair<List<Integer>, AbstractRegleMapping>(listeGroupe, this.regleMappingFactory.get(expressionGroupeReturned,
                    this.variableMapping));
        }
        throw new ArcException("L'expression " + anExpressionGroupe + " dans la règle " + this.getExpression()
                + " n'est pas reconnue comme valide.");
    }

    private void majMapRegleGroupe(Pair<List<Integer>, AbstractRegleMapping> aPairRegleGroupe) throws ArcException {
        for (int i = 0; i < aPairRegleGroupe.getFirst().size(); i++) {
            if (this.mapRegleGroupe.containsKey(aPairRegleGroupe.getFirst().get(i))) {
                throw new ArcException("L'expression " + this.getExpression() + " comporte plusieurs références au numéro de groupe "
                        + aPairRegleGroupe.getFirst().get(i));
            }
            this.mapRegleGroupe.put(aPairRegleGroupe.getFirst().get(i), aPairRegleGroupe.getSecond());
        }
    }

    private void decomposerElementsSimple() throws ArcException {
        Matcher matcher = patternDebutRegleGroupe.matcher(this.getExpression());
        int end = 0;
        String expressionGroupe;
        /*
         * Traitement de tous les groupes trouvés (ils commencent par "{{") sauf du dernier. Le premier appel à matcher.find() trouve
         * l'indice de début du premier groupe. Le deuxième appel à matcher.find() permet de trouver l'indice de début du deuxième groupe,
         * et donc, de traiter le premier groupe.
         */
        while (matcher.find()) {
            int newStart = matcher.start();
            if (newStart > end) {
                expressionGroupe = this.getExpression().substring(end, newStart);
                Pair<List<Integer>, AbstractRegleMapping> regleGroupe = construireRegleGroupe(expressionGroupe);
                majMapRegleGroupe(regleGroupe);
            }
            end = newStart;
        }
        /*
         * Traitement du dernier groupe trouvé.
         */
        expressionGroupe = this.getExpression().substring(end);
        Pair<List<Integer>, AbstractRegleMapping> regleGroupe = construireRegleGroupe(expressionGroupe);
        majMapRegleGroupe(regleGroupe);
    }

    @Override
    public Set<String> getEnsembleIdentifiantsRubriques() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getEnsembleIdentifiantsRubriques(Integer aNumeroGroupe) {
        if (this.mapRegleGroupe.containsKey(aNumeroGroupe)) {
            /*
             * La sous-règle issue du n-ième morceau de la règle de groupe, n'est plus une règle de groupe. Donc, il ne faut surtout pas
             * indiquer le numéro de groupe dans getEnsembleIdentifiantsRubriques().
             */
            return this.mapRegleGroupe.get(aNumeroGroupe).getEnsembleIdentifiantsRubriques();
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> getEnsembleNomsRubriques() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getEnsembleNomsRubriques(Integer aNumeroGroupe) {
        if (this.mapRegleGroupe.containsKey(aNumeroGroupe)) {
            /*
             * La sous-règle issue du n-ième morceau de la règle de groupe, n'est plus une règle de groupe. Donc, il ne faut surtout pas
             * indiquer le numéro de groupe dans getEnsembleNomsRubriques().
             */
            return this.mapRegleGroupe.get(aNumeroGroupe).getEnsembleNomsRubriques();
        }
        return Collections.emptySet();
    }

    @Override
    public String getExpressionSQL() throws ArcException {
        throw new ArcException("Cette méthode ne devrait pas être appelée par RegleMappingGroupe");
    }

    /**
     *
     * @return l'expression pour le groupe demandé ou bien null.<br/>
     *         Il se peut qu'une règle à groupe ne contienne pas de règle POUR le groupe demandé.<br/>
     *         Par exemple, la variable A a trois groupes 1, 2 et 3 et la variable B a deux groupes 1 et 2. Le groupe 3 existe bien, mais
     *         est valorisé à {@code "null"} pour la variable B.
     * @throws ArcException 
     */
    @Override
    public String getExpressionSQL(Integer aNumeroGroupe) throws ArcException {
        if (this.mapRegleGroupe.containsKey(aNumeroGroupe)) {
            return this.mapRegleGroupe.get(aNumeroGroupe).getExpressionSQL(aNumeroGroupe);
        }
        return MAPPING_NULL_EXPRESSION;
    }

    @Override
    public Set<Integer> getEnsembleGroupes() {
        return this.mapRegleGroupe.keySet();
    }

}
