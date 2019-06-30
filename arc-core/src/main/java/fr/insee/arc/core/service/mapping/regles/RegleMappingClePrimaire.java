package fr.insee.arc.core.service.mapping.regles;

import java.util.HashSet;
import java.util.Set;

import fr.insee.arc.core.service.mapping.RequeteMapping;
import fr.insee.arc.core.service.mapping.TableMapping;
import fr.insee.arc.core.service.mapping.VariableMapping;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.LoggerHelper;

/**
 *
 * Après interprétation, la règle <code>{pk:nom_table}</code> s'écrira :<br/>
 * <code>row_number() over (PARTITION BY plein de i_xxxx)</code><br/>
 * Comme les {@code i_xxxx} sont les rubriques de la table {@code nom_table}, cette règle doit connaître cette table.
 *
 */
public class RegleMappingClePrimaire extends AbstractRegleMappingSimple {

    protected static final String tokenOrderByWhat = "{:tokenOrderByWhat}";

    protected TableMapping tableMappingIdentifiee;

    private String idFamille;

    /**
     * Comment reconnaître une règle de clef primaire ?
     */
    public final static String regexRegleClefPrimaire = "^\\{pk:.*\\}$";

    /**
     * Pourquoi un milliard ? Parce que c'est grand.
     */
    private static final String UN_MILLIARD = "1000000000";

    /**
     *
     * @param anExpression
     * @param anIdFamille
     * @param aVariableMapping
     */
    public RegleMappingClePrimaire(String anExpression, String anIdFamille, VariableMapping aVariableMapping) {
        super(anExpression, aVariableMapping);
        this.idFamille = anIdFamille;
    }

    /**
     * Si la table {@code XXXX} a un identifiant technique id_YYYY, deux cas :<br/>
     * 1. {@code XXXX} = YYYY, auquel cas l'identifiant technique sert de clef primaire. {@code id_XXXX} se calcule comme une séquence
     * numérique sur les identifiants de rubriques de la table {@code XXXX}.<br/>
     * 2. {@code XXXX} != YYYY, auquel cas l'identifiant technique référence la clef primaire de la table YYYY. id_YYYY se calcule comme une
     * séquence numérique sur les identifiants de rubriques de la table YYYY.<br/>
     * Application pour cette méthode : lors des calculs ultérieurs, la table métier qui utilise cette règle a besoin des identifiants de
     * rubriques de la tables référencée.
     *
     */
    @Override
    public void deriver() {
        for (TableMapping table : this.variableMapping.getEnsembleTableMapping()) {
            if (table//
                    .getNomTableCourt()//
                    .equalsIgnoreCase("mapping_" + this.idFamille//
                            .toLowerCase()//
                            + "_" + this.variableMapping//
                                    .getNomVariable()//
                                    .replace("id_", EMPTY) + "_ok")) {
                this.tableMappingIdentifiee = table;
            }
        }
        /*
         * Attention les tables ne connaissent pas encore leur ensemble d'identifiant la remontée d'information ne s'est pas encore faite
         */
        this.ensembleIdentifiantsRubriques = this.tableMappingIdentifiee.getEnsembleIdentifiantsRubriques();
    }

    /**
     * Attention à la casse des différents éléments
     */
    @Override
    public void deriverTest() {
        String regexDebut = "^\\{pk:mapping_" + this.idFamille;
        String boutDeux = "_ok\\}$";
        String nomVariable = this.getExpression().replaceFirst(regexDebut.toLowerCase(), "id").replaceFirst(boutDeux, EMPTY);
        LoggerHelper.trace(LOGGER, "regexDebut :" + regexDebut + " , Nom variable : " + nomVariable);
        if (!nomVariable.equalsIgnoreCase(this.variableMapping.getNomVariable())) {
            throw new IllegalStateException("La règle de clé primaire pour la variable " + this.getVariableMapping().getNomVariable()
                    + " n'est pas de la forme : \"{\"pk:mapping_<famille>_<variable>_ok\"}\"");
        }
    }

    /**
     * @return 1. Si au moins un identifiant de rubrique pour la table est non null, renvoie un numéro d'ordre basé sur les valeurs
     *         distinctes pour l'ensemble de ces identifiants.<br/>
     *         2. Si tous les identifiants de rubriques utiles pour la table sont null, c'est que la ligne est vide, et donc faut pas
     *         calculer de numéro.
     */
    @Override
    public String getExpressionSQL() {
        Set<String> retenu = new HashSet<>(this.ensembleIdentifiantsRubriques);
        retenu.removeAll(RequeteMapping.setIdSource);
        if (retenu.isEmpty()) {
            return "null::text collate \"C\"";
        }

        StringBuilder returned = new StringBuilder();
        returned.append("row(");
        returned.append(Format.untokenize(retenu, ", "));
        returned.append(")::text collate \"C\"");
        
        return returned.toString();
    }

    /**
     * La règle de clef primaire ne se calcule pas de la même manière pour tous les groupes.<br/>
     * Pourquoi ? Parce que la méthode {@link #getExpressionSQL()} ne calcule un identifiant technique de ligne que sur les identifiants de
     * rubriques non groupe. Du coup, deux lignes très différentes (règles de groupes valorisées différemment) donneraient, avec
     * {@link #getExpressionSQL(Integer)}, une même valeur d'identifiant de ligne.
     */
    @Override
    public String getExpressionSQL(Integer aNumeroGroupe) {
        Set<String> retenu = new HashSet<>(this.ensembleIdentifiantsRubriques);
        retenu.addAll(this.tableMappingIdentifiee.getEnsembleIdentifiantsRubriques(aNumeroGroupe));
        retenu.removeAll(RequeteMapping.setIdSource);
       
        if (retenu.isEmpty()) {
            return "''::text collate \"C\"";
        }

        StringBuilder returned = new StringBuilder();
        returned.append("row(");
        returned.append(Format.untokenize(retenu, ", "));
        returned.append(")::text collate \"C\"");

        return returned.toString();
    }

    public String getExpressionSQLPrepIndice() {
        if (this.tableMappingIdentifiee.isGroupe()) {
            return new StringBuilder("(" + this.variableMapping.getNomVariable() + " + numero_groupe::bigint * " + UN_MILLIARD + "::bigint)")
                    .toString();
        }
        return this.variableMapping.getNomVariable();
    }

    /**
     * @return the tableMappingIdentifiee
     */
    public TableMapping getTableMappingIdentifiee() {
        return this.tableMappingIdentifiee;
    }

}
